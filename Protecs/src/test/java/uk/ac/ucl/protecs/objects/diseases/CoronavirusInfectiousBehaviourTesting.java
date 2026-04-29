package uk.ac.ucl.protecs.objects.diseases;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.CoronavirusDiseaseProgressionFramework;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.CoronavirusDiseaseProgressionFramework.CoronavirusBehaviourNodeTitle;
import uk.ac.ucl.protecs.sim.ImportExport;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.helperFunctions.HelperFunctions.NodeOption;
import uk.ac.ucl.protecs.objects.diseases.Disease.DISEASESTAGE;


import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class CoronavirusInfectiousBehaviourTesting {
	// ==================================== Testing ==================================================================
	// === These tests are designed to ensure that the transition between different infectious behaviour nodes are ===
	// === happening as they should do. Each of the behaviour nodes are forced into the population and then the ======
	// === infectious behaviour nodes that are meant to be transitioned to via the model's inner workings are checked =
	// === against the infectious behaviour nodes that were activated by the simulation. ==============================
	private final static String paramsDir = "src/test/resources/";
	
	
	@Rule
	public TestName testName = new TestName();
	

	protected int seed;
	protected Random random;
	
	private String params;

	@Rule
	public TestWatcher watcher = new TestWatcher() {

	    private String timestamp() {
	        return LocalDateTime.now()
	            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
	    }

	    private void logResult(String result, String extra) {
		    params = "params_InfectiousBehaviourTest";

	        try (FileWriter writer = new FileWriter("coronavirus-infectious-behaviour-test-seeds.log", true)) {
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
		seed = new java.util.Random().nextInt();	    
		random = new Random(seed);
	}

	@Test
	public void ifThereAreNoCovidInfectionsPeopleStaySusceptible() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);
		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.SUSCEPTIBLE), 
				NodeOption.CoronavirusInfectiousBehaviour);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour, 1);
		// we would expect only the susceptible node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		// double check by testing the infection properties
		for (Disease d: sim.human_infections) {
			if (!d.hasDiseaseStage(DISEASESTAGE.SUSCEPTIBLE)) {
				Assert.fail();
			}
		}
		
	}
	
	@Test
	public void haltingAtExposedLeadsToSusceptibleOnly() {
		// create a simulation and start

		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Ensure that no one disease progression occurs beyond the exposed stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.EXPOSED);
		// make sure no one recovers from their infection
		HelperFunctions.StopRecoveryHappening(sim);
		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.EXPOSED), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour, 1);
		// we would expect only the exposed, presymptomatic, asymptomatic and susceptible nodes to be present in the run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.EXPOSED.name(), CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (!d.hasDiseaseStage(DISEASESTAGE.SUSCEPTIBLE)) {
				Assert.fail();
			}
		}
	}
	
	@Test
	public void exposedBehaviourNodesLeadToSusceptiblePresymptomaticAndAsymptomaticOnly() {
		// create a simulation and start

		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Ensure that no one disease progression occurs beyond the exposed stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.PRESYMPTOMATIC);
		// make sure no one recovers from their infection
		HelperFunctions.StopRecoveryHappening(sim);
		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.EXPOSED), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour, 1);
		// we would expect only the exposed, presymptomatic, asymptomatic and susceptible nodes to be present in the run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.EXPOSED.name(), CoronavirusBehaviourNodeTitle.PRESYMPTOMATIC.name(), 
				CoronavirusBehaviourNodeTitle.ASYMPTOMATIC.name(), CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.ASYMPTOMATIC) || d.hasDiseaseStage(DISEASESTAGE.SUSCEPTIBLE) || d.hasDiseaseStage(DISEASESTAGE.PRESYMPTOMATIC) || d.hasDiseaseStage(DISEASESTAGE.NA))) {
				Assert.fail();
			}
		}
	}
	
	@Test
	public void presymptomaticBehaviourNodesLeadToMildCasesOnly() {
		// create a simulation and start

		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// Make everyone have a critical infection
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.PRESYMPTOMATIC), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Make Sure no one's disease progresses beyond a Mild infection
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.MILD);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Make sure no one recovers from COVID
		HelperFunctions.StopRecoveryHappening(sim);
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour, 1);
		// we would expect only the presymptomatic and mild node to show up in this run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.PRESYMPTOMATIC.name(), CoronavirusBehaviourNodeTitle.MILD.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.MILD))) {
				Assert.fail();
			}
		}
		}
	
	@Test
	public void asymptomaticBehaviourNodesLeadsToRecoveredOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// Make everyone have a critical infection
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.ASYMPTOMATIC), 
				NodeOption.CoronavirusInfectiousBehaviour);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour, 1);
		// we would expect only the asymptomatic and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.ASYMPTOMATIC.name(), CoronavirusBehaviourNodeTitle.RECOVERED.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.RECOVERED))) {
				Assert.fail();
			}
		}
		}
	
	@Test
	public void mildBehaviourNodesLeadToSevereAndRecoveredOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// Make everyone have a critical infection
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.MILD), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Ensure that no one disease progression occurs beyond the severe stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.SEVERE);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour, 1);
		// we would expect only the severe, critical and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.MILD.name(), CoronavirusBehaviourNodeTitle.SEVERE.name(), 
				CoronavirusBehaviourNodeTitle.RECOVERED.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.RECOVERED) || d.hasDiseaseStage(DISEASESTAGE.SEVERE))) {
				Assert.fail();
			}
		}
		}
	
	@Test
	public void confirmMildInfectionsResolveToRecoveredWhenTheyDoNotProgress() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Ensure that no one disease progression occurs beyond the exposed stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.SEVERE);
		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.MILD), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = HelperFunctions.getFinalBehaviourNodesInSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour);
		// we would expect only the recovered behaviour node at the end of the simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.RECOVERED))) {
				Assert.fail();
			}
		}
	}
	
	@Test
	public void severeBehaviourNodesLeadToCriticalAndRecoveredOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim =HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// Make everyone have a critical infection
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.SEVERE), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Ensure that no one disease progression occurs beyond the critical stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.CRITICAL);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour, 1);
		// we would expect only the severe, critical and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.SEVERE.name(), CoronavirusBehaviourNodeTitle.CRITICAL.name(), 
				CoronavirusBehaviourNodeTitle.RECOVERED.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.RECOVERED) || d.hasDiseaseStage(DISEASESTAGE.CRITICAL))) {
				Assert.fail();
			}
		}
		}
	
	@Test
	public void confirmSevereInfectionsResolveToRecoveredWhenTheyDoNotProgress() {
		// create a simulation and start

		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Ensure that no one disease progression occurs beyond the exposed stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.CRITICAL);
		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.SEVERE), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = HelperFunctions.getFinalBehaviourNodesInSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour);
		// we would expect only the recovered node as the final behaviour node in the run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.RECOVERED))) {
				Assert.fail();
			}
		}
	}
	
	@Test
	public void criticaBehaviourlNodesLeadToDeadOrRecoveredOnly() {
		// create a simulation and start

		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// Make everyone have a critical infection
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.CRITICAL), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour, 1);
		// we would expect only the Critical, dead and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.CRITICAL.name(), CoronavirusBehaviourNodeTitle.DEAD.name(), 
				CoronavirusBehaviourNodeTitle.RECOVERED.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.RECOVERED) || d.hasDiseaseStage(DISEASESTAGE.CAUSEOFDEATH))) {
				Assert.fail();
			}
		}
		}
	
	@Test
	public void confirmCriticalInfectionsResolveToRecoveredWhenTheyDoNotProgress() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Ensure that no one disease progression occurs beyond the exposed stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.CRITICAL);
		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.CRITICAL), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = HelperFunctions.getFinalBehaviourNodesInSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour);
		// we would expect only the recovered behaviour node at the end of the simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.RECOVERED))) {
				Assert.fail();
			}
		}
	}
	@Test
	public void recoveredBehaviourNodesStayRecovered() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.RECOVERED), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour, 1);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.RECOVERED))) {
				Assert.fail();
			}
		}
	}



	@Test
	public void deadBehaviourNodesStayDead() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.DEAD), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour, 1);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.DEAD.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.CAUSEOFDEATH))) {
				Assert.fail();
			}
		}
	}
	@Test
	public void ifWeGiveEveryoneAnInfectionEventuallyTheyWillRecoverOrDie() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Make sure there are no new infections
		HelperFunctions.StopCovidFromSpreading(sim);
		// Make sure that people exposed don't revert back to being susceptible
		ForceExposedInfectionsCauseDisease(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.EXPOSED), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = HelperFunctions.getFinalBehaviourNodesInSim(sim, numDays, NodeOption.CoronavirusInfectiousBehaviour);
		// we would expect only the recovered or dead node to appear at the end of simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.name(), CoronavirusBehaviourNodeTitle.DEAD.name());
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		// test the DALY calculations run
		ImportExport.exportInfections("covid_infections.txt", sim.human_infections);
		// test the export sim information works
		ImportExport.exportSimInformation(sim, "sim_info_test.txt", seed, sim.agents.size(), numDays);
		// test the reportOnInfected function works
		ImportExport.reportOnInfected(sim.agents);

		for (Disease d: sim.human_infections) {
			if (!(d.hasDiseaseStage(DISEASESTAGE.RECOVERED) || d.hasDiseaseStage(DISEASESTAGE.CAUSEOFDEATH))) {
				Assert.fail();
			}
		}
	}
	@Test
	public void ensureNewCovidCasesAreCreated() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_InfectiousBehaviourTest.txt");
		sim.start();
		loadInfectiousBehaviour(sim);

		// Make beta large
		sim.covidInfectiousFramework.setCovid_infectious_beta(10);
		// Make sure that people exposed don't revert back to being susceptible
		ForceExposedInfectionsCauseDisease(sim);
		HelperFunctions.StopRecoveryHappening(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(0.2, sim, sim.covidInfectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.EXPOSED), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		int number_of_initial_infections = 0;
		for (Disease d: sim.human_infections) {
			if (d.getDiseaseType().equals(DISEASE.COVID)) {
				number_of_initial_infections++;
			}
		}
		HelperFunctions.runSimulation(sim, numDays);
		int final_number_of_infections = 0;
		for (Disease d: sim.human_infections) {
			if (d.getDiseaseType().equals(DISEASE.COVID)) {
				final_number_of_infections++;
			}
		}
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(final_number_of_infections > number_of_initial_infections);
	}
    // ================================ Helper functions ==================================================
	
	private void loadInfectiousBehaviour(WorldBankCovid19Sim sim) {
		sim.covidInfectiousFramework = new CoronavirusDiseaseProgressionFramework(sim);
		sim.covidInfectiousFramework.load_infection_params(sim.params.dataDir + sim.params.infection_transition_params_filename);
	}
	
	private void ForceExposedInfectionsCauseDisease(WorldBankCovid19Sim world) {
		int idx = 0;
		for (double val: world.covidInfectiousFramework.getCovid_infection_p_sym_by_age()) {
			world.covidInfectiousFramework.getCovid_infection_r_sus_by_age().set(idx, 1.0);
			idx ++;
		}
	}
}
