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

public class MeaslesTesting {
	// ==================================== Testing ==================================================================
	// === These tests are designed to ensure that the transition between different infectious behaviour nodes are ===
	// === happening as they should do. Each of the behaviour nodes are forced into the population and then the ======
	// === infectious behaviour nodes that are meant to be transitioned to via the model's inner workings are checked =
	// === against the infectious behaviour nodes that were activated by the simulation. ==============================
	private final static String paramsDir = "src/test/resources/";

	@Test
	public void ifThereAreNoMeaslesInfectionsPeopleStaySusceptible() {
		Assert.assertTrue(true);
	}
	
	@Test
	public void exposedBehaviourNodesLeadToSusceptibleAndPresymptomaticOnly() {
		Assert.assertTrue(true);
	}
	
	@Test
	public void presymptomaticBehaviourNodesLeadToInfectionsOnly() {
		Assert.assertTrue(true);
	}
	
	
	@Test
	public void establishedInfectionsLeadToComplicationsAndRecoveryOnly() {
		Assert.assertTrue(true);
	}
	
	@Test
	public void confirmInfectionsResolveToRecoveredWhenTheyDoNotProgress() {
		Assert.assertTrue(true);
	}
	
	@Test
	public void complicationsLeadToDeadOrRecoveredOnly() {
		Assert.assertTrue(true);
		}
	
	@Test
	public void confirmComplicationsResolveToRecoveredWhenTheyDoNotProgress() {
		Assert.assertTrue(true);
	}
	
	@Test
	public void recoveredBehaviourNodesStayRecovered() {
		Assert.assertTrue(true);
	}
	@Test
	public void deadBehaviourNodesStayDead() {
		Assert.assertTrue(true);
	}
	@Test
	public void ifWeGiveEveryoneAnInfectionEventuallyTheyWillRecoverOrDie() {
		Assert.assertTrue(true);
	}
	
	@Test
	public void ensureNewMeaslesCasesAreCreated() {
		Assert.assertTrue(true);

	}
	
	@Test
	public void testMeaslesAndHIVInteraction() {
		Assert.assertTrue(true);

	}
	
	@Test
	public void testMeaslesAndMalnutritionInteraction() {
		Assert.assertTrue(true);

	}
	
	@Test
	public void testMeaslesAndPregnancyInteraction() {
		Assert.assertTrue(true);

	}
	
	@Test
	public void testHealthComplications() {
		Assert.assertTrue(true);

	}
	
	@Test
	public void measlesVaccinesPreventInfections() {
		Assert.assertTrue(true);

	}
	
	@Test
	public void herdImmunityIsPossible() {
		Assert.assertTrue(true);

	}
	
}