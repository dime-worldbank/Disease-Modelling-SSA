package uk.ac.ucl.protecs.objects.diseases;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.CoronavirusDiseaseProgressionFramework.CoronavirusBehaviourNodeTitle;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.helperFunctions.HelperFunctions.NodeOption;

public class CoronavirusInfectiousBehaviourTesting {
	// ==================================== Testing ==================================================================
	// === These tests are designed to ensure that the transition between different infectious behaviour nodes are ===
	// === happening as they should do. Each of the behaviour nodes are forced into the population and then the ======
	// === infectious behaviour nodes that are meant to be transitioned to via the model's inner workings are checked =
	// === against the infectious behaviour nodes that were activated by the simulation. ==============================
	private final static String paramsDir = "src/test/resources/";

	@Test
	public void ifThereAreNoCovidInfectionsPeopleStaySusceptible() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.SUSCEPTIBLE), 
				NodeOption.CoronavirusInfectiousBehaviour);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the susceptible node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		// double check by testing the infection properties
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasSevere() | d.hasCritical() | d.isCauseOfDeath() | d.hasRecovered()) {
				Assert.fail();
			}
		}
		
	}
	
	@Test
	public void exposedBehaviourNodesLeadToSusceptiblePresymptomaticAndAsymptomaticOnly() {
		// create a simulation and start

		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Ensure that no one disease progression occurs beyond the exposed stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.PRESYMPTOMATIC);
		// make sure no one recovers from their infection
		HelperFunctions.StopRecoveryHappening(sim);
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.EXPOSED), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the exposed, presymptomatic, asymptomatic and susceptible nodes to be present in the run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.EXPOSED.key, CoronavirusBehaviourNodeTitle.PRESYMPTOMATIC.key, 
				CoronavirusBehaviourNodeTitle.ASYMPTOMATIC.key, CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (d.hasMild() | d.hasSevere() | d.hasCritical() | d.isCauseOfDeath() | d.hasRecovered()) {
				Assert.fail();
			}
		}
	}
	
	@Test
	public void presymptomaticBehaviourNodesLeadToMildCasesOnly() {
		// create a simulation and start

		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.PRESYMPTOMATIC), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Make Sure no one's disease progresses beyond a Mild infection
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.MILD);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Make sure no one recovers from COVID
		HelperFunctions.StopRecoveryHappening(sim);
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the presymptomatic and mild node to show up in this run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.PRESYMPTOMATIC.key, CoronavirusBehaviourNodeTitle.MILD.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasSevere() | d.hasCritical() | d.isCauseOfDeath() | d.hasRecovered()) {
				Assert.fail();
			}
		}
		}
	
	@Test
	public void asymptomaticBehaviourNodesLeadsToRecoveredOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.ASYMPTOMATIC), 
				NodeOption.CoronavirusInfectiousBehaviour);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the asymptomatic and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.ASYMPTOMATIC.key, CoronavirusBehaviourNodeTitle.RECOVERED.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasSevere() | d.hasCritical() | d.isCauseOfDeath()) {
				Assert.fail();
			}
		}
		}
	
	@Test
	public void mildBehaviourNodesLeadToSevereAndRecoveredOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.MILD), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Ensure that no one disease progression occurs beyond the severe stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.SEVERE);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the severe, critical and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.MILD.key, CoronavirusBehaviourNodeTitle.SEVERE.key, 
				CoronavirusBehaviourNodeTitle.RECOVERED.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasCritical() | d.isCauseOfDeath()) {
				Assert.fail();
			}
		}
		}
	
	@Test
	public void confirmMildInfectionsResolveToRecoveredWhenTheyDoNotProgress() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Ensure that no one disease progression occurs beyond the exposed stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.SEVERE);
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.MILD), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getFinalNodesInSim(sim, numDays);
		// we would expect only the recovered behaviour node at the end of the simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasSevere() | d.hasCritical() | d.isCauseOfDeath()) {
				Assert.fail();
			}
		}
	}
	
	@Test
	public void severeBehaviourNodesLeadToCriticalAndRecoveredOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim =HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.SEVERE), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Ensure that no one disease progression occurs beyond the critical stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.CRITICAL);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the severe, critical and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.SEVERE.key, CoronavirusBehaviourNodeTitle.CRITICAL.key, 
				CoronavirusBehaviourNodeTitle.RECOVERED.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasSevere() | d.isCauseOfDeath()) {
				Assert.fail();
			}
		}
		}
	
	@Test
	public void confirmSevereInfectionsResolveToRecoveredWhenTheyDoNotProgress() {
		// create a simulation and start

		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Ensure that no one disease progression occurs beyond the exposed stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.CRITICAL);
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.SEVERE), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getFinalNodesInSim(sim, numDays);
		// we would expect only the recovered node as the final behaviour node in the run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasSevere() | d.hasCritical() | d.isCauseOfDeath()) {
				Assert.fail();
			}
		}
	}
	
	@Test
	public void criticaBehaviourlNodesLeadToDeadOrRecoveredOnly() {
		// create a simulation and start

		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.CRITICAL), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the Critical, dead and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.CRITICAL.key, CoronavirusBehaviourNodeTitle.DEAD.key, 
				CoronavirusBehaviourNodeTitle.RECOVERED.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasSevere() | d.hasCritical()) {
				Assert.fail();
			}
		}
		}
	
	@Test
	public void confirmCriticalInfectionsResolveToRecoveredWhenTheyDoNotProgress() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Ensure that no one disease progression occurs beyond the exposed stage
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.CRITICAL);
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.CRITICAL), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getFinalNodesInSim(sim, numDays);
		// we would expect only the recovered behaviour node at the end of the simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasSevere() | d.hasCritical() | d.isCauseOfDeath()) {
				Assert.fail();
			}
		}
	}
	@Test
	public void recoveredBehaviourNodesStayRecovered() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.RECOVERED), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasSevere() | d.hasCritical() | d.isCauseOfDeath()) {
				Assert.fail();
			}
		}
	}
	@Test
	public void deadBehaviourNodesStayDead() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.DEAD), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.DEAD.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasSevere() | d.hasCritical() | d.hasRecovered()) {
				Assert.fail();
			}
		}
	}
	@Test
	public void ifWeGiveEveryoneAnInfectionEventuallyTheyWillRecoverOrDie() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make sure that people exposed don't revert back to being susceptible
		ForceExposedInfectionsCauseDisease(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.EXPOSED), 
				NodeOption.CoronavirusInfectiousBehaviour);		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getFinalNodesInSim(sim, numDays);
		// we would expect only the recovered or dead node to appear at the end of simulation
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.key, CoronavirusBehaviourNodeTitle.DEAD.key);
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
		for (Disease d: sim.human_infections) {
			if (d.hasAsympt() | d.hasMild() | d.hasSevere() | d.hasCritical()) {
				Assert.fail();
			}
		}
	}
	@Test
	public void ensureNewCovidCasesAreCreated() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "InfectiousBehaviourTestParams.txt");
		sim.start();
		// Make beta large
		sim.params.infection_beta = 10;
		// Make sure that people exposed don't revert back to being susceptible
		ForceExposedInfectionsCauseDisease(sim);
		HelperFunctions.StopRecoveryHappening(sim);
		// seed a number of the specific node to the run
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(0.2, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.EXPOSED), 
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
	
	
	
	private void ForceExposedInfectionsCauseDisease(WorldBankCovid19Sim world) {
		int idx = 0;
		for (double val: world.params.infection_p_sym_by_age) {
			world.params.infection_r_sus_by_age.set(idx, 1.0);
			idx ++;
		}
	}

	
	
	private HashSet<String> getUniqueNodesInSim(WorldBankCovid19Sim world, int numDaysToRun){
		// This function runs the simulation for a predetermined number of days. Every simulation day, this function will store the 
		// infectious disease nodes that the infections are doing each day.
		// At the end of the simulation, the function returns a list of the unique infectious behaviour nodes that have happened over
		// the course of the simulation.
		
		// Create a list to store the unique node stages that occur in each step
		HashSet <String> behaviourNodeBin = new HashSet<String>();
		
		// Simulate over the time period and get the disease stages present in the simulation
		while(world.schedule.getTime() < Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
			// create a list to store the disease nodes that occur in the simulation

			world.schedule.step(world);
			if (world.schedule.getTime() % Params.ticks_per_day == 1.0) {
			for (Disease i: world.human_infections) {
				behaviourNodeBin.add(i.getBehaviourName());
			}

		}
		}
				
		return behaviourNodeBin;
	}
	private List<String> getFinalNodesInSim(WorldBankCovid19Sim world, int numDaysToRun){
		// This function runs the simulation for a predetermined number of days.
		// At the end of the simulation, the function returns a list of the behaviour nodes being 'performed' by the infections.
		
		// Create a list to store the unique node stages that occur in each step
		HashSet <String> behaviourNodeBin = new HashSet<String>();
		
		// Simulate over the time period and get the disease stages present in the simulation
		HelperFunctions.runSimulation(world, numDaysToRun);
		
		for (Disease i: world.human_infections) {
			behaviourNodeBin.add(i.getBehaviourName());
		}
		
		List<String> UniqueNodes = new ArrayList<String>(behaviourNodeBin);

		return UniqueNodes;
	}
}
