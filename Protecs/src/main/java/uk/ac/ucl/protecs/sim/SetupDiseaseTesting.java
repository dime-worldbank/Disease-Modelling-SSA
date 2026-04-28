package uk.ac.ucl.protecs.sim;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.SpuriousSymptomDiseaseProgressionFramework;
import uk.ac.ucl.protecs.sim.loggers.CovidLogging;

public class SetupDiseaseTesting{
	
	public static void scheduleDiseaseTesting(WorldBankCovid19Sim world) {
		if (world.params.covidTesting) {
			world.spuriousFramework = new SpuriousSymptomDiseaseProgressionFramework(world);
			world.schedule.scheduleRepeating(CovidSpuriousSymptoms.createSymptomObject(world));
			world.schedule.scheduleRepeating(CovidTesting.Testing(world), world.param_schedule_COVID_Testing, world.params.ticks_per_day);
			
			CovidLogging CovidTestLogger = new CovidLogging();
			CovidLogging.CovidTestReporter CovidTestReporter = CovidTestLogger.new CovidTestReporter(world);
			world.schedule.scheduleRepeating(CovidTestReporter, world.param_schedule_reporting, world.params.ticks_per_day);
			}
	}
	
}