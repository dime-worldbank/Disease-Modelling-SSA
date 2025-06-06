package uk.ac.ucl.protecs.objects.diseases;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.CholeraDiseaseProgressionFramework.CholeraBehaviourNodeInHumans;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.CoronavirusDiseaseProgressionFramework.CoronavirusBehaviourNodeTitle;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.helperFunctions.HelperFunctions.NodeOption;
import uk.ac.ucl.protecs.objects.hosts.Person;

public class CholeraInHumansTesting {
	// ============================================== Cholera in humans testing suit ==============================================================================
	// Here we aim to test the instantiation of Cholera in the Person object and the disease progression behaviour following subsequent infection
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
		HashSet<String> uniqueNodesInRun = HelperFunctions.getUniqueNodesOverCourseofSim(sim, 7, NodeOption.Cholera, 1);
		
		// list the expected nodes
		List<String> expectedNodes = Arrays.asList(CoronavirusBehaviourNodeTitle.EXPOSED.key, CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.key, CoronavirusBehaviourNodeTitle.ASYMPTOMATIC.key, 
				CoronavirusBehaviourNodeTitle.MILD.key, CoronavirusBehaviourNodeTitle.SEVERE.key);

		// Make sure than no other nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun));
		
	}
}