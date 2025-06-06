package uk.ac.ucl.protecs.objects.diseases;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.CholeraDiseaseProgressionFramework.CholeraBehaviourNodeInHumans;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.CoronavirusDiseaseProgressionFramework.CoronavirusBehaviourNodeTitle;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.helperFunctions.HelperFunctions.NodeOption;
import uk.ac.ucl.protecs.objects.hosts.Person;

public class CholeraInHumansTesting {
	// ============================================== Cholera in humans testing suit ==============================================================================
	// Here we aim to test the instantiation of Cholera in the Person object and the disease progression behaviour following subsequent infection.
	// We test that: 
	// 1) cholera cases cases are loaded in from the line list
	// 2) the entry point is always the 'exposed' behaviour node
	// 3) the exposed node will lead to susceptible, asymptomatic, mild and severe cholera only
	// 4) asymptomatic cholera will lead to recovery only
	// 5) mild cholera will lead to recovery only
	// 6) severe cholera will lead to critical, death or recovery only
	// 7) critical cholera will lead to death or recovery only
	// 8) death leads only to death
	// 9) recovery leads to susceptibility only
	// ============================================================================================================================================================
	private final static String paramsDir = "src/test/resources/";
	
	@Test
	public void choleraCasesAreLoadedInViaLineList() {
		// Test that cholera infections are created and loaded in via the line list
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera.txt");
		sim.start();
		// assume no cases have been loaded in the the person objects
		boolean choleraLoadedIn = false;
		// iterate over the population to try and find a cholera infection via their disease set
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)) {
				// if we found a cholera case, alter our assumption that none have been loaded in and stop the search
				choleraLoadedIn = true;
				break;
			}
		}
		// test whether infections have been loaded in
		Assert.assertTrue(choleraLoadedIn);
	}
	
	@Test
	public void choleraCasesAreInitiallySetToExposedNode() {
		// Test that cholera infections are initially load in with the exposed behaviour node
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera.txt");
		sim.start();
		// assume every cholera case starts with the exposed behaviour node
		boolean startsAsExposed = true;
		// iterate over the population to try and find a cholera infection via their disease set
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)) {
				// get this infection's current behaviour
				String currentBehaviourName = sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.EXPOSED).getTitle();
				// check this is the exposed node behaviour, break for loop if not
				if (!p.getDiseaseSet().get(DISEASE.CHOLERA.key).getBehaviourName().equals(currentBehaviourName)) {
					startsAsExposed = false;
					break;
				}
			}
		}
		// test whether infections are doing the exposed behaviour
		Assert.assertTrue(startsAsExposed);
	}
	
	@Test
	public void exposedNodeLeadsToSusceptibleAsymptomaticMildAndSevere() {
		// Test that the exposed node leads to susceptible, asymptomatic, mild and critical states only
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera.txt");
		sim.start();
		int num_days = 7;
		// adjust probability of outcomes to (hopefully) make sure that all options are explored from the exposed node
		// balance out the likelihood of the next states from the exposed node
		sim.params.cholera_sufficient_ingestion = 0.5;
		sim.params.cholera_prob_asymptomatic = 0.5;
		sim.params.cholera_prob_severe = 0.5;
		// remove any chance of severe infections becoming critical
		sim.params.cholera_prob_seek_treatment = 1.01;
		// remove any chance of death
		sim.params.cholera_prob_mortality_with_treatment = 0;
		sim.params.cholera_prob_mortality_without_treatment = 0;
		// remove any chance of recovery in the simulation time
		sim.params.cholera_mean_time_recovery_asympt = num_days + 2; 
		sim.params.cholera_mean_time_recovery_mild = num_days + 2; 
		sim.params.cholera_mean_time_recovery_severe = num_days + 2; 
		sim.params.cholera_mean_time_recovery_critical = num_days + 2; 
		sim.params.cholera_mean_time_death_with_treatment = num_days + 2; 
		sim.params.cholera_mean_time_death_without_treatment = num_days + 2; 
		
		// run the simulation for a few day and track the unique behaviour nodes of cholera infections
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, num_days, NodeOption.Cholera, 1);
		
		// list the expected nodes
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.EXPOSED.key, CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.key, CoronavirusBehaviourNodeTitle.ASYMPTOMATIC.key, 
				CoronavirusBehaviourNodeTitle.MILD.key, CoronavirusBehaviourNodeTitle.SEVERE.key);

		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
	}
	@Test
	public void asymptomaticLeadsToRecoveredOnly() {
		// Test that the asymptomatic node leads to the recovered state only
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera.txt");
		sim.start();
		int num_days = 7;
		for (Disease d: sim.human_infections) {
			d.setBehaviourNode(sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.ASYMPTOMATIC));
		}

		// make everyone have asymptomatic cholera
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.ASYMPTOMATIC), 
				NodeOption.Cholera);

		// remove any chance of recovery in the simulation time
		sim.params.cholera_mean_time_recovery_asympt = num_days - 3; 

		
		// run the simulation for a few day and track the unique behaviour nodes of cholera infections
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, num_days, NodeOption.Cholera, 1);
		
		// list the expected nodes
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.ASYMPTOMATIC.key, CoronavirusBehaviourNodeTitle.RECOVERED.key);

		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
	}
	@Test
	public void mildLeadsToRecoveredOnly() {
		// Test that the mild node leads to the recovered state only
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera.txt");
		sim.start();
		int num_days = 7;
		for (Disease d: sim.human_infections) {
			d.setBehaviourNode(sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.MILD));
		}

		// make everyone have mild cholera
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.MILD), 
				NodeOption.Cholera);

		// ensure that recovery time happens within the simulation time
		sim.params.cholera_mean_time_recovery_mild = num_days - 3; 

		
		// run the simulation for a few day and track the unique behaviour nodes of cholera infections
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, num_days, NodeOption.Cholera, 1);
		
		// list the expected nodes
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.MILD.key, CoronavirusBehaviourNodeTitle.RECOVERED.key);

		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
	}
	@Test
	public void severeLeadsToCriticalDeadAndRecoveredOnly() {
		// Test that the severe node leads to the critical, dead and recovered states only
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera.txt");
		sim.start();
		int num_days = 7;
		for (Disease d: sim.human_infections) {
			d.setBehaviourNode(sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.SEVERE));
		}

		// make everyone have severe cholera
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.SEVERE), 
				NodeOption.Cholera);

		// ensure that recovery time happens within the simulation time
		sim.params.cholera_mean_time_recovery_severe = num_days - 3; 
		// make half the people seek treatment and those who don't will develop critical cholera
		sim.params.cholera_prob_mortality_with_treatment = 0.5;

		
		// run the simulation for a few day and track the unique behaviour nodes of cholera infections
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, num_days, NodeOption.Cholera, 1);
		
		// list the expected nodes
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.SEVERE.key, CoronavirusBehaviourNodeTitle.CRITICAL.key, CoronavirusBehaviourNodeTitle.DEAD.key, CoronavirusBehaviourNodeTitle.RECOVERED.key);

		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
	}
	
	@Test
	public void criticalLeadsToDeadAndRecoveredOnly() {
		// Test that the critical node leads to the dead and recovered states only
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera.txt");
		sim.start();
		int num_days = 7;
		for (Disease d: sim.human_infections) {
			d.setBehaviourNode(sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.CRITICAL));
		}

		// make everyone have asymptomatic cholera
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.CRITICAL), 
				NodeOption.Cholera);

		// ensure that recovery time happens within the simulation time
		sim.params.cholera_mean_time_recovery_critical = num_days - 3; 

		
		// run the simulation for a few day and track the unique behaviour nodes of cholera infections
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, num_days, NodeOption.Cholera, 1);
		
		// list the expected nodes
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.CRITICAL.key, CoronavirusBehaviourNodeTitle.DEAD.key, CoronavirusBehaviourNodeTitle.RECOVERED.key);

		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
	}
	
	@Test
	public void deadLeadsToDeadOnly() {
		// Test that the dead node does not change
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera.txt");
		sim.start();
		int num_days = 7;
		for (Disease d: sim.human_infections) {
			d.setBehaviourNode(sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.DEAD));
		}

		// make everyone dead
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.DEAD), 
				NodeOption.Cholera);

		// ensure that recovery time happens within the simulation time
		sim.params.cholera_mean_time_recovery_critical = num_days - 3; 

		
		// run the simulation for a few day and track the unique behaviour nodes of cholera infections
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, num_days, NodeOption.Cholera, 1);
		
		// list the expected nodes
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.DEAD.key);

		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
	}
	
	@Test
	public void recoveredLeadsToSusceptible() {
		// Test that the recovered node goes to susceptible
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera.txt");
		sim.start();
		int num_days = 7;
		for (Disease d: sim.human_infections) {
			d.setBehaviourNode(sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.RECOVERED));
		}

		// make everyone have recovered from cholera
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.choleraFramework.setNodeForTesting(CholeraBehaviourNodeInHumans.RECOVERED), 
				NodeOption.Cholera);

		// ensure that immunity from cholera wears out over the course of the simulation
		sim.params.cholera_natural_immunity_days_post_infection = num_days - 3; 

		
		// run the simulation for a few day and track the unique behaviour nodes of cholera infections
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, num_days, NodeOption.Cholera, 1);
		
		// list the expected nodes
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.RECOVERED.key, CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.key);

		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
	}
}