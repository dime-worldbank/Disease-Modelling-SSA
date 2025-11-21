package uk.ac.ucl.protecs.sim.loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.sim.ImportExport;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class CovidLogging {
	// tab shortcut
	static String t = LoggingHelperFunctions.tab;
	// age sex breakdown header
	private final static String age_sex_categories = t + "sex" + t + LoggingHelperFunctions.age_categories + "\n";
	
	public class CovidTestReporter implements Steppable{
		
		WorldBankCovid19Sim world;
		boolean firstTimeReporting;
		
		public CovidTestReporter(WorldBankCovid19Sim myWorld) {
			this.world = myWorld;
			this.firstTimeReporting = true;
		}
		
		@Override
		public void step(SimState arg0) {
			int dayOfSimulation = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
			int numberOfTestsPerDay = world.params.number_of_tests_per_day.get(dayOfSimulation);
			// create a function to group the population by sex, age and whether they gave birth
			
			Map<Boolean, Map<Boolean, List<Disease>>> hasTestedPositiveForCovid = new HashMap<>();
			
			for (Disease d: world.human_infections) {
				hasTestedPositiveForCovid
				.computeIfAbsent(d.hasTestedPositive(), k -> new HashMap<>())
				.computeIfAbsent(d.getTestLogged(), k -> new ArrayList<>())
				.add(d);
				
			}
			int numberOfPositiveTests = 0;
			try {
				numberOfPositiveTests = hasTestedPositiveForCovid.get(true).get(false).size();
			}catch (Exception e) {
				numberOfPositiveTests = 0;
			}
			
			double fractionPositive = (double) numberOfPositiveTests / numberOfTestsPerDay;
			String covidTestingOutput = "";

			String t = "\t";
			String outputColumnNames = t + "numberOfTests" + t + "numberOfPositiveTests" + t + "fractionPositive" + "\n";
			if (this.firstTimeReporting) {
				covidTestingOutput += "day" + outputColumnNames + String.valueOf(dayOfSimulation);
				this.firstTimeReporting = false;
			}
			else {
				covidTestingOutput += String.valueOf(dayOfSimulation);
			}
			covidTestingOutput += t + String.valueOf(numberOfTestsPerDay) + t + String.valueOf(numberOfPositiveTests) + t + String.valueOf(fractionPositive) + "\n";
			
			
			ImportExport.exportMe(world.covidTestingOutputFilename, covidTestingOutput, world.timer);
			// to make sure that COVID tests aren't counted more than once, update this infections properties
			for (Disease i: world.human_infections) {
				if((i.getDiseaseType().equals(DISEASE.COVID))) {
					i.confirmTestLogged();
					}
				}
		}
		
	}
	
	// output for casesPerAdminZoneFilename
	public static Steppable ReportCovidCasesByTypeAndLocation(WorldBankCovid19Sim world) {

		return new Steppable() {
				
			@Override
			public void step(SimState arg0) {
				WorldBankCovid19Sim myWorld = (WorldBankCovid19Sim) arg0;
					
				//	create a list of admin zones names to iterate over for our logging
				List <String> adminZoneList = myWorld.params.adminZoneNames;
							
				// create list to store the counts of each category of interest. The number of cases, the cumulative number of cases, 
				// the number of cases by type, the number of recoveries, the number of deaths etc... 
				ArrayList <Integer> covidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> cumCovidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> asymptCovidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> mildCovidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> severeCovidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> criticalCovidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> recoveredCountArray = new ArrayList<Integer>();
				ArrayList<Integer> covidCumulativeDeathCount = new ArrayList<Integer>();
				ArrayList<Integer> covidNewDeathCount = new ArrayList<Integer>();

				// create a function to group the population by location, whether they are alive and if they have covid and if this is a new case
				Map<String, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> location_alive_hasCovid_map = new HashMap<>();
				
				// create a map to count the number of people who have recovered from covid in that admin zone
				Map<String, Map<DISEASE, Map<Boolean, Long>>> location_alive_recovered_map = new HashMap<>();
				
				// create a map to count the number of people who have asymptomatic covid in that admin zone
				Map<String, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> location_asympt_covid_map = new HashMap<>();
				
				// create a map to count the number of people who have mild covid in that admin zone
				Map<String, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> location_mild_covid_map = new HashMap<>();
				
				// create a map to count the number of people who have severe covid in that admin zone
				Map<String, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> location_severe_covid_map = new HashMap<>();

				// create a map to count the number of people who have critical covid in that admin zone
				Map<String, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> location_critical_covid_map = new HashMap<>();
				
				// create a function to group the population by location and count cumulative deaths
				Map<String, Map<DISEASE, Map<Boolean, Long>>> location_cumulative_died_map = new HashMap<>();
				
				// create a function to group the population by location and count cumulative cases
				Map<String, Map<DISEASE, Long>> location_cumulative_covid_map = new HashMap<>();
				
				// create a function to group the population by location and count new deaths
				Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>> location_new_deaths_map = new HashMap<>();
				
				// generate covid summary statistics by type
				for (Disease d: world.human_infections) {
					location_alive_hasCovid_map
					.computeIfAbsent(d.getCurrentAdminZone(), k -> new HashMap<>())
					.computeIfAbsent(d.isHostAlive(), k -> new EnumMap<>(DISEASE.class))
					.computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
					.computeIfAbsent(d.hasRecovered(), k -> new HashMap<>())
					.merge(d.getLogged(), 1l, Long::sum);
					
					location_alive_recovered_map
					.computeIfAbsent(d.getCurrentAdminZone(), k -> new EnumMap<>(DISEASE.class))
					.computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
					.merge(d.hasRecovered(), 1l, Long::sum);
					
					location_asympt_covid_map
					.computeIfAbsent(d.getCurrentAdminZone(), k -> new HashMap<>())
					.computeIfAbsent(d.isHostAlive(), k -> new EnumMap<>(DISEASE.class))
					.computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
					.computeIfAbsent(d.hasAsympt(), k -> new HashMap<>())
					.merge(d.getAsymptLogged(), 1l, Long::sum);
					
					location_mild_covid_map
					.computeIfAbsent(d.getCurrentAdminZone(), k -> new HashMap<>())
					.computeIfAbsent(d.isHostAlive(), k -> new EnumMap<>(DISEASE.class))
					.computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
					.computeIfAbsent(d.hasCritical(), k -> new HashMap<>())
					.merge(d.getAsymptLogged(), 1l, Long::sum);
					
					location_cumulative_died_map
					.computeIfAbsent(d.getCurrentAdminZone(), k -> new EnumMap<>(DISEASE.class))
					.computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
					.merge(d.isCauseOfDeath(), 1l, Long::sum);
					
					location_cumulative_covid_map
					.computeIfAbsent(d.getCurrentAdminZone(), k -> new EnumMap<>(DISEASE.class))
					.merge(d.getDiseaseType(), 1l, Long::sum);
					
					location_new_deaths_map
					.computeIfAbsent(d.getCurrentAdminZone(), k -> new EnumMap<>(DISEASE.class))
					.computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
					.computeIfAbsent(d.isCauseOfDeath(), k -> new HashMap<>())
					.merge(d.getDeathLogged(), 1l, Long::sum);
				}


				//	We now iterate over the admin zones, to find the current state of the epidemic
				for (String zone: adminZoneList) {
					// get the current number of cases in each admin zone
					try {
					covidCountArray.add(location_alive_hasCovid_map.get(zone).get(true).get(DISEASE.COVID).get(false).get(false).intValue());						
					} 
					catch (Exception e) {
						// No one in population met criteria
						covidCountArray.add(0);
					}
					// get the cumulative number of covid cases in the admin zone
					try {
						cumCovidCountArray.add(location_cumulative_covid_map.get(zone).get(DISEASE.COVID).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						cumCovidCountArray.add(0);
					}
					// get the number of asymptomatic covid cases in the admin zone
					try {
						asymptCovidCountArray.add(location_asympt_covid_map.get(zone).get(true).get(DISEASE.COVID).get(true).get(false).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						asymptCovidCountArray.add(0);
					}
					// get the number of mild covid cases in the admin zone
					try {
						mildCovidCountArray.add(location_mild_covid_map.get(zone).get(true).get(DISEASE.COVID).get(true).get(false).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						mildCovidCountArray.add(0);
					}
						// get the number of severe covid cases in the admin zone
					try {
						severeCovidCountArray.add(location_severe_covid_map.get(zone).get(true).get(DISEASE.COVID).get(true).get(false).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						severeCovidCountArray.add(0);
					}
					// get the number of critical covid cases in the admin zone
					try {
						criticalCovidCountArray.add(location_critical_covid_map.get(zone).get(true).get(DISEASE.COVID).get(true).get(false).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						criticalCovidCountArray.add(0);
					}
					// get the number of recoveries  in the admin zone
					try {
						recoveredCountArray.add(location_alive_recovered_map.get(zone).get(DISEASE.COVID).get(true).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						recoveredCountArray.add(0);
					}
					// get the cumultative number of covid deaths in the admin zone
					try {
						covidCumulativeDeathCount.add(location_cumulative_died_map.get(zone).get(DISEASE.COVID).get(true).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						covidCumulativeDeathCount.add(0);
					}
					// get the number of new covid deaths in the admin zone
					try {
						covidNewDeathCount.add(location_new_deaths_map.get(zone).get(DISEASE.COVID).get(true).get(false).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						covidNewDeathCount.add(0);
					}
				}
				// report out findings
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				// name the file
				String covidNumberOutput = "";
				// format the file
				String adminZoneNames = "";
				for (String zone: adminZoneList) {adminZoneNames += t + zone;}
					if (time == 0) {
						covidNumberOutput += "day" + t + "metric" + adminZoneNames + "\n" + String.valueOf(time);
					}
					else {
						covidNumberOutput += String.valueOf(time);
					}
					// store total number of cases in admin zone
					covidNumberOutput += t + "total_cases";
					for (int val: covidCountArray){
						covidNumberOutput += t + String.valueOf(val);
					}
					covidNumberOutput += "\n";
					// store total number of asymptomatic cases in admin zone
					covidNumberOutput += time + t + "total_asympt_cases";
					for (int val: asymptCovidCountArray){
						covidNumberOutput += t + String.valueOf(val);
					}
					covidNumberOutput += "\n";
					// store total number of mild cases in admin zone
					covidNumberOutput += time + t + "total_mild_cases";
					for (int val: mildCovidCountArray){
						covidNumberOutput += t + String.valueOf(val);
					}
					covidNumberOutput += "\n";
					// store total number of severe cases in admin zone
					covidNumberOutput += time + t + "total_severe_cases";
					for (int val: severeCovidCountArray){
						covidNumberOutput += t + String.valueOf(val);
					}
					covidNumberOutput += "\n";
					// store total number of critical cases in admin zone
					covidNumberOutput += time + t + "total_critical_cases";
					for (int val: criticalCovidCountArray){
						covidNumberOutput += t + String.valueOf(val);
					}
					covidNumberOutput += "\n";
					// store total number of recoveries in admin zone
					covidNumberOutput += time + t + "total_recovered";
					for (int val: recoveredCountArray){
						covidNumberOutput += t + String.valueOf(val);
					}
					covidNumberOutput += "\n";
					// store cumulative number of cases in admin zone
					covidNumberOutput += time + t + "cumulative_cases";
					for (int val: cumCovidCountArray){
						covidNumberOutput += t + String.valueOf(val);
					}
					covidNumberOutput += "\n";
					// store cumulative number of deaths in admin zone
					covidNumberOutput += time + t + "cumulative_deaths"; 
					for (int val: covidCumulativeDeathCount){
						covidNumberOutput += t + String.valueOf(val);
					}
					covidNumberOutput += "\n";
					// store total number of new deaths in admin zone
					covidNumberOutput += time + t + "new_deaths";
					for (int val: covidNewDeathCount){
						covidNumberOutput += t + String.valueOf(val);
					}
					covidNumberOutput += "\n";

					// export the file
					
				ImportExport.exportMe(world.covidCasesPerAdminZoneFilename, covidNumberOutput, world.timer);
				}
			};
		}
	
	public static Steppable ResetCovidLoggedProperties(WorldBankCovid19Sim world) {
		return new Steppable() {			
			@Override
			public void step(SimState arg0) {
					// to make sure deaths and cases aren't counted multiple times, update this person's properties
					for (Disease i: world.human_infections) {
							if (i.isOfType(DISEASE.COVID)) {
							if(!i.isHostAlive()) {
								i.confirmDeathLogged();
							}
							if(i.hasAsympt() & !i.getAsymptLogged()) {
								i.confirmAsymptLogged();
							}
							if(i.hasMild() & !i.getMildLogged()) {
								i.confirmMildLogged();
							}
							if(i.hasSevere() & !i.getSevereLogged()) {
								i.confirmSevereLogged();
							}
							if(i.hasCritical() & !i.getCriticalLogged()) {
								i.confirmCriticalLogged();
							}
							i.confirmLogged();
						}
					} 
				}
			};
	}

}