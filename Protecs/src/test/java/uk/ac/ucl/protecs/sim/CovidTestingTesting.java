package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.helperFunctions.HelperFunctions.NodeOption;

import org.junit.Before;
import org.junit.Rule;


import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.CoronavirusDiseaseProgressionFramework.CoronavirusBehaviourNodeTitle;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class CovidTestingTesting {
	
	private final static String paramsDir = "src/test/resources/";
	
	private String params = "params_covid_testing";
	
	@Rule
	public TestName testName = new TestName();

	protected int seed;
	protected Random random;
	@Rule
	public TestWatcher watcher = new TestWatcher() {

	    private String timestamp() {
	        return LocalDateTime.now()
	            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
	    }

	    private void logResult(String result, String extra) {
	        try (FileWriter writer = new FileWriter("covid-testing-test-seeds.log", true)) {
	            writer.write(
	                timestamp() +
	                " | Test: " + testName.getMethodName() +
	                " | Params: " + params + ".txt" +
	                " | Seed: " + seed +
	                " | RESULT: " + result +
	                (extra != null ? " | " + extra : "") +
	                "\n"
	            );
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    @Override
	    protected void succeeded(Description description) {
	        logResult("PASSED", null);
	    }

	    @Override
	    protected void failed(Throwable e, Description description) {
	        logResult("FAILED", "Error: " + e.getMessage());
	    }
	};
	
	@Before
	public void setupSeed() throws IOException {
		seed = new java.util.Random().nextInt();;

	    random = new Random(seed);
	}
	
	
	@Test
	public void CheckTestsOnlyHappenForThoseWithSymptomsOfCovid() {
		int seed = (int) this.seed;		

		// set up the simulation
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_covid_testing.txt");
		sim.start();
		int numDays = 1;
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(0.5, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.MILD),
				NodeOption.CoronavirusInfectiousBehaviour);
		HelperFunctions.StopRecoveryHappening(sim);
		HelperFunctions.StopCovidFromSpreading(sim);
		HelperFunctions.runSimulation(sim, numDays);
		List<Disease> hasBeenTested = infectionsTested(sim);
		int numWithBothSpuriousAndSymptomaticCovid = 0;
		for (Disease i: hasBeenTested) {
			if (i.getDiseaseType().equals(DISEASE.COVID)) {
				if (i.getHost().getDiseaseSet().containsKey(DISEASE.COVIDSPURIOUSSYMPTOM.key)) {
					if (i.getHost().getDiseaseSet().get(DISEASE.COVIDSPURIOUSSYMPTOM.key).isSymptomatic()) {
						numWithBothSpuriousAndSymptomaticCovid++;
					}
				}
				
			}
//			if (p.hasCovidSpuriousSymptoms() & p.hasSymptomaticCovid()) {
//				numWithBothSpuriousAndSymptomaticCovid++;
//				}
		}
		Assert.assertTrue(numWithBothSpuriousAndSymptomaticCovid == 0);
		
	}
	

//	@Test
//	public void CheckThereAreNoDifferencesInCaseNumbers() {
//		//Arrange
//		WorldBankCovid19Sim sim_without_testing = helperFunctions.CreateDummySimWithChosenSeed(12, "src/main/resources/params_covid_testing.txt", false, false);
//		sim_without_testing.start();
//		int numDays = 50;
//		while(sim_without_testing.schedule.getTime() < Params.ticks_per_day * numDays && !sim_without_testing.schedule.scheduleComplete()){
//			sim_without_testing.schedule.step(sim_without_testing);
//		}
//		WorldBankCovid19Sim sim_with_testing = helperFunctions.CreateDummySimWithChosenSeed(12, "src/main/resources/params_covid_testing.txt", false, true);
//
//		sim_with_testing.start();
//		while(sim_with_testing.schedule.getTime() < Params.ticks_per_day * numDays && !sim_with_testing.schedule.scheduleComplete()){
//			sim_with_testing.schedule.step(sim_with_testing);
//		}
//		int numWithoutTesting = sim_without_testing.infections.size();
//		System.out.println(sim_without_testing.random.nextDouble());
//		int numWithTesting = sim_with_testing.infections.size();
//		System.out.println(sim_with_testing.random.nextDouble());
//		System.out.println("number without = " + String.valueOf(numWithoutTesting));
//		System.out.println("number with = " + String.valueOf(numWithTesting));
//		Assert.assertTrue(numWithoutTesting == numWithTesting);
//
//	}
	public List<Disease> infectionsTested(WorldBankCovid19Sim world){
		
		Map<Boolean, List<Disease>> propertiesChecked = (Map<Boolean,List<Disease>>) world.human_infections.stream().collect(
	            Collectors.groupingBy(
	              Disease::hasBeenTested,
	                    Collectors.toList()
		            )
	               );
		return propertiesChecked.get(true);
	}
}
