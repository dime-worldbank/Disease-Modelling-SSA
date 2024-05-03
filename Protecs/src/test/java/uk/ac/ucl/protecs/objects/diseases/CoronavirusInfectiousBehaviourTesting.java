package uk.ac.ucl.protecs.objects.diseases;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourNode;
import uk.ac.ucl.protecs.helperFunctions.*;

public class CoronavirusInfectiousBehaviourTesting {
	// ==================================== Testing ==================================================================
	// === These tests are designed to ensure that the transition between different infectious behaviour nodes are ===
	// === happening as they should do. Each of the behaviour nodes are forced into the population and then the ======
	// === infectious behaviour nodes that are meant to be transitioned to via the model's inner workings are checked =
	// === against the infectious behaviour nodes that were activated by the simulation. ==============================
	
	@Test
	public void ifThereAreNoCovidInfectionsPeopleStaySusceptible() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("susceptible"));
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the susceptible node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList("susceptible");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	
	@Test
	public void exposedBehaviourNodesLeadToSusceptiblePresymptomaticAndAsymptomaticOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Ensure that no one disease progression occurs beyond the exposed stage
		HaltDiseaseProgressionAtStage(sim, "Presymptomatic");
		// make sure no one recovers from their infection
		StopRecoveryHappening(sim);
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("exposed"));
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the exposed, presymptomatic, asymptomatic and susceptible nodes to be present in the run
		List<String> expectedNodes = Arrays.asList("exposed", "susceptible", "presymptomatic", "asymptomatic");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	
	@Test
	public void presymptomaticBehaviourNodesLeadToMildCasesOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("presymptomatic"));
		// Make Sure no one's disease progresses beyond a Mild infection
		HaltDiseaseProgressionAtStage(sim, "Mild");
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Make sure no one recovers from COVID
		StopRecoveryHappening(sim);
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the presymptomatic and mild node to show up in this run
		List<String> expectedNodes = Arrays.asList("presymptomatic", "mild_case");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	
	@Test
	public void asymptomaticBehaviourNodesLeadsToRecoveredOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("asymptomatic"));
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the asymptomatic and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("asymptomatic", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	
	@Test
	public void mildBehaviourNodesLeadToSevereAndRecoveredOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("mild"));
		// Ensure that no one disease progression occurs beyond the severe stage
		HaltDiseaseProgressionAtStage(sim, "Severe");
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the severe, critical and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("mild_case", "severe_case", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	
	@Test
	public void confirmMildInfectionsResolveToRecoveredWhenTheyDoNotProgress() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Ensure that no one disease progression occurs beyond the exposed stage
		HaltDiseaseProgressionAtStage(sim, "Severe");
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("mild"));
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getFinalNodesInSim(sim, numDays);
		// we would expect only the exposed, presymptomatic, asymptomatic and susceptible nodes to be present in the run
		List<String> expectedNodes = Arrays.asList("recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	
	@Test
	public void severeBehaviourNodesLeadToCriticalAndRecoveredOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim =helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("severe"));
		// Ensure that no one disease progression occurs beyond the critical stage
		HaltDiseaseProgressionAtStage(sim, "Critical");
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the severe, critical and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("severe_case", "critical_case", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	
	@Test
	public void confirmSevereInfectionsResolveToRecoveredWhenTheyDoNotProgress() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Ensure that no one disease progression occurs beyond the exposed stage
		HaltDiseaseProgressionAtStage(sim, "Critical");
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("severe"));
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getFinalNodesInSim(sim, numDays);
		// we would expect only the exposed, presymptomatic, asymptomatic and susceptible nodes to be present in the run
		List<String> expectedNodes = Arrays.asList("recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	
	@Test
	public void criticaBehaviourlNodesLeadToDeadOrRecoveredOnly() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("critical"));
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the Critical, dead and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("critical_case", "dead", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	
	@Test
	public void confirmCriticalInfectionsResolveToRecoveredWhenTheyDoNotProgress() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Ensure that no one disease progression occurs beyond the exposed stage
		HaltDiseaseProgressionAtStage(sim, "Critical");
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("critical"));
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getFinalNodesInSim(sim, numDays);
		// we would expect only the exposed, presymptomatic, asymptomatic and susceptible nodes to be present in the run
		List<String> expectedNodes = Arrays.asList("recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	@Test
	public void recoveredBehaviourNodesStayRecovered() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("recovered"));
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList("recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	@Test
	public void deadBehaviourNodesStayDead() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("dead"));
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList("dead");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	@Test
	public void ifWeGiveEveryoneAnInfectionEventuallyTheyWillRecoverOrDie() {
		// create a simulation and start
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/InfectiousBehaviourTestParams.txt", false, false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make sure that people exposed don't revert back to being susceptible
		ForceExposedInfectionsCauseDisease(sim);
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("exposed"));
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getFinalNodesInSim(sim, numDays);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList("recovered", "dead");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	
    // ================================ Helper functions ==================================================
	
	private void HaltDiseaseProgressionAtStage(WorldBankCovid19Sim world, String stage) {
		// You present this function with a stage in the disease which you want to halt the infection, then this
		// function changes the parameters which allows the disease to progress further
		switch (stage) {
		case "Exposed":
			int exp_idx = 0;
			// Make sure there are no transitions from exposed to symptomatic COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_sym_by_age.set(exp_idx, 0.0);
				exp_idx ++;
			}
			break;
		case "Presymptomatic":
			world.params.infectiousToSymptomatic_mean = Integer.MAX_VALUE;
			world.params.infectiousToSymptomatic_std = 0;
			break;
		case "Mild":
			int mild_idx = 0;
			// Make sure there are no transitions from exposed to symptomatic COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_sev_by_age.set(mild_idx, 0.0);
				mild_idx ++;
			}
			break;
		case "Severe":
			int severe_idx = 0;
			// Make sure there are no transitions from exposed to symptomatic COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_cri_by_age.set(severe_idx, 0.0);
				severe_idx ++;
			}
			break;
		case "Critical":
			int critical_idx = 0;
			// Make sure there are no transitions from exposed to symptomatic COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_dea_by_age.set(critical_idx, 0.0);
				critical_idx ++;
			}
			break;
		default:
			System.out.print("No parameters changed");
		}
		
	}
	
	private void ForceExposedInfectionsCauseDisease(WorldBankCovid19Sim world) {
		int idx = 0;
		for (double val: world.params.infection_p_sym_by_age) {
			world.params.infection_r_sus_by_age.set(idx, 1.0);
			idx ++;
		}
	}
	
	private void SetFractionInfectionsWithCertainNode(double fraction, WorldBankCovid19Sim world, BehaviourNode Node) {
		// Make this function assigns an infectious behaviour node of your choice to a certain percentage of the population
		for (Person p: world.agents) {
			double rand = world.random.nextDouble();
			if (!p.hasCovid() && rand <= fraction) {
				CoronavirusInfection inf = new CoronavirusInfection(p, null, world.infectiousFramework.getHomeNode(), world);
				inf.setBehaviourNode(Node);
				// kick off the infectious behaviour framework
				inf.step(world);
			}
		}
	}
	
	private void StopRecoveryHappening(WorldBankCovid19Sim world) {
		// This function sets the recovery time of COVID at various stages of the disease to an very high integer beyond the range
		// of the simulation, thereby stopping recovery from COVID happening
		world.params.asymptomaticToRecovery_mean = Integer.MAX_VALUE;
		world.params.asymptomaticToRecovery_std = 0;
		world.params.symptomaticToRecovery_mean = Integer.MAX_VALUE;
		world.params.symptomaticToRecovery_std = 0;
		world.params.severeToRecovery_mean = Integer.MAX_VALUE;
		world.params.severeToRecovery_std = 0;
		world.params.criticalToRecovery_mean = Integer.MAX_VALUE;
		world.params.criticalToRecovery_std = 0;
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
			for (Infection i: world.infections) {
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
		while(world.schedule.getTime() < Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
			// create a list to store the disease nodes that occur in the simulation
			ArrayList <String> nodesBin = new ArrayList<String>();
			world.schedule.step(world);
		}
		for (Infection i: world.infections) {
			behaviourNodeBin.add(i.getBehaviourName());
		}
		
		List<String> UniqueNodes = new ArrayList<String>(behaviourNodeBin);

		return UniqueNodes;
	}
}
