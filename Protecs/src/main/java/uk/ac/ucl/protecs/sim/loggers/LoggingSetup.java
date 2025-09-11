package uk.ac.ucl.protecs.sim.loggers;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.ImportExport;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class LoggingSetup{
	
	// create a function to set up the output file names
	
	public static void setupOutputFileNames(WorldBankCovid19Sim world) {
		// check if the simulation is using demography
		if (world.params.demography) {
			world.otherIncDeathOutputFilename = world.outputFilename + "_Incidence_Of_Other_Death";
			world.birthRateOutputFilename = world.outputFilename + "_Birth_Rate.txt";
		}
		// Check if the covid disease framework has been set up
		if (!(world.covidInfectiousFramework == null)) {
			// covidInfectiousFramework has been loaded, implying the need to log covid cases, setup the file names
			world.covidIncOutputFilename = world.outputFilename + "_Incidence_Of_Covid.txt"; 
			world.covidIncDeathOutputFilename = world.outputFilename + "_Incidence_Of_Covid_Death.txt";
			world.otherIncDeathOutputFilename = world.outputFilename + "_Incidence_Of_Other_Death.txt";
			world.covidCasesPerAdminZoneFilename = world.outputFilename + "_COVID_Cases_Per_Admin_Zone.txt"; 
			world.adminZoneCovidPrevalenceOutputFilename = world.outputFilename + "_Percent_In_Admin_Zone_With_Covid.txt";
			world.covidCountsOutputFilename = world.outputFilename + "_Age_Gender_Demographics_Covid.txt";
			world.covidByEconOutputFilename = world.outputFilename + "_Economic_Status_Covid.txt";
			world.adminZonePercentDiedFromCovidOutputFilename = world.outputFilename + "_Percent_In_Admin_Zone_Died_From_Covid.txt";
			world.adminZonePercentCovidCasesFatalOutputFilename = world.outputFilename + "_Percent_Covid_Cases_Fatal_In_Admin_Zone.txt";
			if (world.params.covidTesting) {
			world.covidTestingOutputFilename = world.outputFilename + "_Covid_Testing.txt";
			}
		}
		
		if (!(world.choleraFramework == null)) {
			world.choleraIncOutputFilename = world.outputFilename + "_Incidence_Of_Cholera.txt"; 
			world.choleraIncDeathOutputFilename = world.outputFilename + "_Incidence_Of_Cholera_Death.txt";
			world.adminZoneCholeraPrevalenceOutputFilename = world.outputFilename + "_Percent_In_Admin_Zone_With_Cholera.txt";
			world.choleraCountsOutputFilename = world.outputFilename + "_Age_Gender_Demographics_Cholera.txt";
			world.choleraByEconOutputFilename = world.outputFilename + "_Economic_Status_Cholera.txt";
			world.adminZonePercentDiedFromCholeraOutputFilename = world.outputFilename + "_Percent_In_Admin_Zone_Died_From_Cholera.txt";
			world.adminZonePercentCholeraCasesFatalOutputFilename = world.outputFilename + "_Percent_Cholera_Cases_Fatal_In_Admin_Zone.txt";

		}
		if (world.workplaces.size() > 0) {
			// only record contact counts if we are not doing perfect mixing
			world.workplaceContactsOutputFilename = world.outputFilename + "_Workplace_Contacts.txt";
			world.communityContactsOutputFilename = world.outputFilename + "_Community_Contacts.txt";
		}

	}
	
	// create a function to schedule logging
	
	public static void scheduleLoggers(WorldBankCovid19Sim world){
		// set up always used logging
		
		// Report on the age sex breakdown of the population (populationOutputFilename)
		world.schedule.scheduleRepeating(DemographyLogging.ReportPopStructure(world), world.param_schedule_reporting, world.params.ticks_per_day);
		
		// Report on the breakdown of population size by space (adminZonePopSizeOutputFilename)
		world.schedule.scheduleRepeating(DemographyLogging.ReportAdminZonePopulationSize(world), world.param_schedule_reporting, world.params.ticks_per_day);
		
		// Report on the age-sex structure of each admin zone (adminZonePopBreakdownOutputFilename)
		world.schedule.scheduleRepeating(DemographyLogging.ReportAdminZoneAgeSexBreakdown(world), world.param_schedule_reporting, world.params.ticks_per_day);
		
		ImportExport.exportMe(world.outputFilename + ".txt", Location.metricNamesToString(), world.timer);
		Steppable reporter = new Steppable(){

			@Override
			public void step(SimState arg0) {
				
				String s = "";
				
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				
				for(Location l: world.adminBoundaries){
					s += time + "\t" + l.metricsToString() + "\n";
					l.refreshMetrics();
				}
				
				ImportExport.exportMe(world.outputFilename + ".txt", s, world.timer);
				
				System.out.println("Day " + time + " finished");
			}
		};
		world.schedule.scheduleRepeating(reporter, world.param_schedule_reporting, world.params.ticks_per_day);
		
		// schedule demography specific loggers
		if (world.params.demography) {
			DemographyLogging logger = new DemographyLogging();
			DemographyLogging.BirthRateReporter birthRateLog = logger.new BirthRateReporter(world);
			// schedule the birth rate reporter (birthRateOutputFilename)
			world.schedule.scheduleOnce(world.params.ticks_per_year, world.param_schedule_reporting, birthRateLog);
			// schedule the 'other deaths' reporter (otherIncDeathOutputFilename)
			world.schedule.scheduleRepeating(DemographyLogging.ReportOtherIncidenceOfDeath(world), world.param_schedule_reporting, world.params.ticks_per_day);	
		}
		
		// schedule covid specific loggers
		if (!(world.covidInfectiousFramework == null)) {
			// Report on the number of cases by type and their location (casesPerAdminZoneFilename)
			Steppable covidCasesbByTypeAndLoc = CovidLogging.ReportCovidCasesByTypeAndLocation(world);
			world.schedule.scheduleRepeating(covidCasesbByTypeAndLoc, world.param_schedule_reporting, world.params.ticks_per_day);
			
			// Report on the percent of the population with COVID by space (adminZoneCovidPrevalenceOutputFilename)
			Steppable percentWithCovid = LoggingHelperFunctions.ReportPercentInAdminZoneWithDisease(world, DISEASE.COVID, world.adminZoneCovidPrevalenceOutputFilename);
			world.schedule.scheduleRepeating(percentWithCovid, world.param_schedule_reporting, world.params.ticks_per_day);
			
			// Report on the incidence of COVID death (covidIncDeathOutputFilename)
			Steppable incCovidDeath = LoggingHelperFunctions.ReportDiseaseIncidenceOfDeath(world, DISEASE.COVID, world.covidIncDeathOutputFilename);
			world.schedule.scheduleRepeating(incCovidDeath, world.param_schedule_reporting, world.params.ticks_per_day);
					
			// Report on the incidence of COVID (covidIncOutputFilename)
			Steppable incCovid = LoggingHelperFunctions.ReportIncidenceOfDisease(world, DISEASE.COVID, world.covidIncOutputFilename);
			world.schedule.scheduleRepeating(incCovid, world.param_schedule_reporting, world.params.ticks_per_day);
					
			// Report on the number of COVID counts in each area (covidCountsOutputFilename)
			Steppable covidCounts = LoggingHelperFunctions.ReportDiseaseCounts(world, DISEASE.COVID, world.covidCountsOutputFilename);
			world.schedule.scheduleRepeating(covidCounts, world.param_schedule_reporting, world.params.ticks_per_day);
					
			// Report on the number of COVID counts in each occupation (covidByEconOutputFilename)
			Steppable covidCountsByOcc = LoggingHelperFunctions.ReportDiseaseCountsByOccupation(world, DISEASE.COVID, world.covidByEconOutputFilename);
			world.schedule.scheduleRepeating(covidCountsByOcc, world.param_schedule_reporting, world.params.ticks_per_day);
					
			// Report on the percent of COVID cases that are fatal per admin zone (adminZonePercentCovidCasesFatalOutputFilename)
			Steppable covidPercFatal = LoggingHelperFunctions.ReportPercentOfDiseaseCasesThatAreFatalPerAdminZone(world, DISEASE.COVID, world.adminZonePercentCovidCasesFatalOutputFilename);
			world.schedule.scheduleRepeating(covidPercFatal, world.param_schedule_reporting, world.params.ticks_per_day);

			// Report on the prevalence of COVID death per admin zone (adminZonePercentDiedFromCovidOutputFilename)
			Steppable CovidPrev = LoggingHelperFunctions.adminZonePercentDiedFromDiseaseOutputFilename(world, DISEASE.COVID, world.adminZonePercentDiedFromCovidOutputFilename);
			world.schedule.scheduleRepeating(CovidPrev, world.param_schedule_reporting, world.params.ticks_per_day);
					
			// Schedule the resetting of COVID reporting properties in the agents
			Steppable covidLoggingReset = CovidLogging.ResetCovidLoggedProperties(world);
			world.schedule.scheduleRepeating(covidLoggingReset, world.param_schedule_reporting_reset, world.params.ticks_per_day);
			
		}
		
		if (!(world.choleraFramework == null)) {
			// Report on the prevalence of cholera
			Steppable choleraPrevalence = LoggingHelperFunctions.ReportPercentInAdminZoneWithDisease(world, DISEASE.CHOLERA, world.adminZoneCholeraPrevalenceOutputFilename);
			world.schedule.scheduleRepeating(choleraPrevalence, world.param_schedule_reporting, world.params.ticks_per_day);
			
			// Report on the incidence of cholera
			Steppable choleraInc = LoggingHelperFunctions.ReportIncidenceOfDisease(world, DISEASE.CHOLERA, world.choleraIncOutputFilename);
			world.schedule.scheduleRepeating(choleraInc, world.param_schedule_reporting, world.params.ticks_per_day);
			
			// Report on the incidence of cholera death
			Steppable choleraIncDeath = LoggingHelperFunctions.ReportDiseaseIncidenceOfDeath(world, DISEASE.CHOLERA, world.choleraIncDeathOutputFilename);
			world.schedule.scheduleRepeating(choleraIncDeath, world.param_schedule_reporting, world.params.ticks_per_day);
			
			// Report on the cholera counts per age and sex
			Steppable choleraCountsByAgeSex = LoggingHelperFunctions.ReportDiseaseCounts(world, DISEASE.CHOLERA, world.choleraCountsOutputFilename);
			world.schedule.scheduleRepeating(choleraCountsByAgeSex, world.param_schedule_reporting, world.params.ticks_per_day);
			
			// Report on cholera cases per occupation
			Steppable choleraCountsByOcc = LoggingHelperFunctions.ReportDiseaseCountsByOccupation(world, DISEASE.CHOLERA, world.choleraByEconOutputFilename);
			world.schedule.scheduleRepeating(choleraCountsByOcc, world.param_schedule_reporting, world.params.ticks_per_day);
			
			// report on the percent who died from cholera
			Steppable CholeraPercentDied = LoggingHelperFunctions.adminZonePercentDiedFromDiseaseOutputFilename(world, DISEASE.CHOLERA, world.adminZonePercentDiedFromCholeraOutputFilename);
			world.schedule.scheduleRepeating(CholeraPercentDied, world.param_schedule_reporting, world.params.ticks_per_day);
			
			// Report on the percent of cholera cases that are fatal per admin zone
			Steppable CholeraPercentDiedPerAdminZone = LoggingHelperFunctions.adminZonePercentDiedFromDiseaseOutputFilename(world, DISEASE.CHOLERA, world.adminZonePercentCholeraCasesFatalOutputFilename);
			world.schedule.scheduleRepeating(CholeraPercentDiedPerAdminZone, world.param_schedule_reporting, world.params.ticks_per_day);

		}
		if (world.workplaces.size() > 0) {
		// Schedule the resetting of COVID reporting properties in the agents 
		world.schedule.scheduleRepeating(SocialContactsLogging.WorkplaceContactsReporter(world), world.param_schedule_reporting, world.params.ticks_per_day);
		world.schedule.scheduleRepeating(SocialContactsLogging.CommunityContactsReporter(world), world.param_schedule_reporting, world.params.ticks_per_day);

		}
		
	}
}