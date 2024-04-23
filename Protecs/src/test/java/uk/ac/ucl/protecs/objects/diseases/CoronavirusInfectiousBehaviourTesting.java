package uk.ac.ucl.protecs.objects.diseases;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.helperFunctions.*;

public class CoronavirusInfectiousBehaviourTesting {
	// ==================================== Testing ==================================================================
	// === These tests are designed to ensure that the transition between different infectious behaviour nodes are ===
	// === happening as they should do. Each of the behaviour nodes are forced into the population and then the ======
	// === infectious behaviour nodes that are meant to be transitioned to via the model's inner workings are checked =
	// === against the infectious behaviour nodes that were activated by the simulation. ==============================
	
	@Test
	public void TestSusceptibleEndpoints() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/test/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		helperFunctions.SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("susceptible"));
		// Set up a duration to run the simulation
		int numDays = 50; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the susceptible node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList("susceptible");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	@Test
	public void TestExposedEndpoints() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/test/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Ensure that no one disease progression occurs beyond the exposed stage
		helperFunctions.HaltDiseaseProgressionAtStage(sim, "Presymptomatic");
		// make sure no one recovers from their infection
		helperFunctions.StopRecoveryHappening(sim);
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		helperFunctions.SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("exposed"));
		// Set up a duration to run the simulation
		int numDays = 50; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the exposed, presymptomatic, asymptomatic and susceptible nodes to be present in the run
		List<String> expectedNodes = Arrays.asList("exposed", "susceptible", "presymptomatic", "asymptomatic");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	@Test
	public void TestPresymptomaticEndpoints() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/test/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		helperFunctions.SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("presymptomatic"));
		// Make Sure no one's disease progresses beyond a Mild infection
		helperFunctions.HaltDiseaseProgressionAtStage(sim, "Mild");
		// Set up a duration to run the simulation
		int numDays = 50; 
		// Make sure no one recovers from COVID
		helperFunctions.StopRecoveryHappening(sim);
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the presymptomatic and mild node to show up in this run
		List<String> expectedNodes = Arrays.asList("presymptomatic", "mild_case");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	@Test
	public void TestAsymptomaticEndpoints() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/test/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		helperFunctions.SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("asymptomatic"));
		// Set up a duration to run the simulation
		int numDays = 50; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the asymptomatic and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("asymptomatic", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	
	@Test
	public void TestMildEndpoints() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/test/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		helperFunctions.SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("mild"));
		// Ensure that no one disease progression occurs beyond the severe stage
		helperFunctions.HaltDiseaseProgressionAtStage(sim, "Severe");
		// Set up a duration to run the simulation
		int numDays = 50; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the severe, critical and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("mild_case", "severe_case", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	@Test
	public void TestSevereEndpoints() {
		// create a simulation and start
		WorldBankCovid19Sim sim =helperFunctions.CreateDummySim("src/test/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		helperFunctions.SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("severe"));
		// Ensure that no one disease progression occurs beyond the critical stage
		helperFunctions.HaltDiseaseProgressionAtStage(sim, "Critical");
		// Set up a duration to run the simulation
		int numDays = 50; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the severe, critical and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("severe_case", "critical_case", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	@Test
	public void TestCriticalEndpoints() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/test/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		helperFunctions.SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("critical"));
		// Set up a duration to run the simulation
		int numDays = 50; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the Critical, dead and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("critical_case", "dead", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	@Test
	public void TestRecoveredEndpoints() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/test/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		helperFunctions.SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("recovered"));
		// Set up a duration to run the simulation
		int numDays = 50; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList("recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	@Test
	public void TestDeadEndpoints() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/test/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		helperFunctions.SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("dead"));
		// Set up a duration to run the simulation
		int numDays = 50; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList("dead");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	
	
    // ================================ Helper functions ==================================================
	
	private List<String> getUniqueNodesInSim(WorldBankCovid19Sim world, int numDaysToRun){
		// This function runs the simulation for a predetermined number of days. Every simulation day, this function will store the 
		// infectious disease nodes that the infections are doing each day.
		// At the end of the simulation, the function returns a list of the unique infectious behaviour nodes that have happened over
		// the course of the simulation.
		
		// Create a list to store the unique node stages that occur in each step
		ArrayList <String> UniqueEachStep = new ArrayList<String> ();
		// Simulate over the time period and get the disease stages present in the simulation
		while(world.schedule.getTime() < Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
			// create a list to store the disease nodes that occur in the simulation
			ArrayList <String> nodesBin = new ArrayList<String>();
			world.schedule.step(world);
			if (world.schedule.getTime() % Params.ticks_per_day == 1.0) {
			for (Infection i: world.infections) {
				nodesBin.add(i.getBehaviourName());
			}
			for (String node: nodesBin.stream().distinct().collect(Collectors.toList())) {
				if (!UniqueEachStep.contains(node)) {UniqueEachStep.add(node);}
			}
		}
		}
				

		// If we halt progression for symptomatic COVID, we don't want to see .
		List<String> UniqueNodes = UniqueEachStep.stream().distinct().collect(Collectors.toList());

		return UniqueNodes;
	}
}
