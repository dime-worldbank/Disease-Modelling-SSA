package uk.ac.ucl.protecs.behaviours;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import uk.ac.ucl.protecs.objects.Infection;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class InfectiousBehaviourTesting {
	// ==================================== Testing ==================================================================
	// === These tests are designed to ensure that the transition between different infectious behaviour nodes are ===
	// === happening as they should do. Each of the behaviour nodes are forced into the population and then the ======
	// === infectious behaviour nodes that are meant to be transitioned to via the model's inner workings are checked =
	// === against the infectious behaviour nodes that were activated by the simulation. ==============================
	
	@Test
	public void TestSusceptibleEndpoints() {
		// create a random seed and record in case test fails
		Random random = new Random();
		int seed = random.nextInt(1000000);
		System.out.println("Seed for this run = " + String.valueOf(seed));
		// create a simulation and start
		WorldBankCovid19Sim sim = CreateDummySim(seed, "src/test/resources/InfectiousBehaviourTestParams.txt", false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.susceptibleNode);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the susceptible node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList("susceptible");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	@Test
	public void TestExposedEndpoints() {
		// create a random seed and record in case test fails
		Random random = new Random();
		int seed = random.nextInt(1000000);
		System.out.println("Seed for this run = " + String.valueOf(seed));
		// create a simulation and start
		WorldBankCovid19Sim sim = CreateDummySim(seed, "src/test/resources/InfectiousBehaviourTestParams.txt", false);
		sim.start();
		// Ensure that no one disease progression occurs beyond the exposed stage
		HaltDiseaseProgressionAtStage(sim, "Presymptomatic");
		// make sure no one recovers from their infection
		StopRecoveryHappening(sim);
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.exposedNode);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the exposed, presymptomatic, asymptomatic and susceptible nodes to be present in the run
		List<String> expectedNodes = Arrays.asList("exposed", "susceptible", "presymptomatic", "asymptomatic");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	@Test
	public void TestPresymptomaticEndpoints() {
		// create a random seed and record in case test fails
		Random random = new Random();
		int seed = random.nextInt(1000000);
		System.out.println("Seed for this run = " + String.valueOf(seed));
		// create a simulation and start
		WorldBankCovid19Sim sim = CreateDummySim(seed, "src/test/resources/InfectiousBehaviourTestParams.txt", false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.presymptomaticNode);
		// Make Sure no one's disease progresses beyond a Mild infection
		HaltDiseaseProgressionAtStage(sim, "Mild");
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Make sure no one recovers from COVID
		StopRecoveryHappening(sim);
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the presymptomatic and mild node to show up in this run
		List<String> expectedNodes = Arrays.asList("presymptomatic", "mild_case");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	@Test
	public void TestAsymptomaticEndpoints() {
		// create a random seed and record in case test fails
		Random random = new Random();
		int seed = random.nextInt(1000000);
		System.out.println("Seed for this run = " + String.valueOf(seed));
		// create a simulation and start
		WorldBankCovid19Sim sim = CreateDummySim(seed, "src/test/resources/InfectiousBehaviourTestParams.txt", false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.asymptomaticNode);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the asymptomatic and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("asymptomatic", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	
	@Test
	public void TestMildEndpoints() {
		// create a random seed and record in case test fails
		Random random = new Random();
		int seed = random.nextInt(1000000);
		System.out.println("Seed for this run = " + String.valueOf(seed));
		// create a simulation and start
		WorldBankCovid19Sim sim = CreateDummySim(seed, "src/test/resources/InfectiousBehaviourTestParams.txt", false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.mildNode);
		// Ensure that no one disease progression occurs beyond the severe stage
		HaltDiseaseProgressionAtStage(sim, "Severe");
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the severe, critical and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("mild_case", "severe_case", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	@Test
	public void TestSevereEndpoints() {
		// create a random seed and record in case test fails
		Random random = new Random();
		int seed = random.nextInt(1000000);
		System.out.println("Seed for this run = " + String.valueOf(seed));
		// create a simulation and start
		WorldBankCovid19Sim sim = CreateDummySim(seed, "src/test/resources/InfectiousBehaviourTestParams.txt", false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.severeNode);
		// Ensure that no one disease progression occurs beyond the critical stage
		HaltDiseaseProgressionAtStage(sim, "Critical");
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the severe, critical and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("severe_case", "critical_case", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	@Test
	public void TestCriticalEndpoints() {
		// create a random seed and record in case test fails
		Random random = new Random();
		int seed = random.nextInt(1000000);
		System.out.println("Seed for this run = " + String.valueOf(seed));
		// create a simulation and start
		WorldBankCovid19Sim sim = CreateDummySim(seed, "src/test/resources/InfectiousBehaviourTestParams.txt", false);
		sim.start();
		
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// Make everyone have a critical infection
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.criticalNode);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes activated in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the Critical, dead and recovered nodes to show up in this run
		List<String> expectedNodes = Arrays.asList("critical_case", "dead", "recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		}
	@Test
	public void TestRecoveredEndpoints() {
		// create a random seed and record in case test fails
		Random random = new Random();
		int seed = random.nextInt(1000000);
		System.out.println("Seed for this run = " + String.valueOf(seed));
		// create a simulation and start
		WorldBankCovid19Sim sim = CreateDummySim(seed, "src/test/resources/InfectiousBehaviourTestParams.txt", false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.recoveredNode);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList("recovered");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	@Test
	public void TestDeadEndpoints() {
		// create a random seed and record in case test fails
		Random random = new Random();
		int seed = random.nextInt(1000000);
		System.out.println("Seed for this run = " + String.valueOf(seed));
		// create a simulation and start
		WorldBankCovid19Sim sim = CreateDummySim(seed, "src/test/resources/InfectiousBehaviourTestParams.txt", false);
		sim.start();
		// Make sure there are no new infections
		sim.params.infection_beta = 0;
		// seed a number of the specific node to the run
		SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.deadNode);
		// Set up a duration to run the simulation
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		List<String> uniqueNodesInRun = getUniqueNodesInSim(sim, numDays);
		// we would expect only the recovered node to appear as there is no COVID seeded in this simulation
		List<String> expectedNodes = Arrays.asList("dead");
		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
	}
	
	
    // ================================ Helper functions ==================================================
	private WorldBankCovid19Sim CreateDummySim(long seed, String paramsFilename, boolean demography) {
		// creates the simulation object
		Params p = new Params(paramsFilename, false);
		WorldBankCovid19Sim myWorld = new WorldBankCovid19Sim(seed, p, "", demography);
		return myWorld;
	}
	
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
	
	private void SetFractionInfectionsWithCertainNode(double fraction, WorldBankCovid19Sim world, BehaviourNode Node) {
		// Make this function assigns an infectious behaviour node of your choice to a certain percentage of the population
		for (Person p: world.agents) {
			double rand = world.random.nextDouble();
			if (!p.hasCovid() && rand <= fraction) {
				Infection inf = new Infection(p, null, world.infectiousFramework.getHomeNode(), world);
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
