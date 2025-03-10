package uk.ac.ucl.protecs.objects.diseases;

import java.util.HashSet;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;

import org.junit.Test;

import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.helperFunctions.helperFunctions.birthsOrDeaths;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;


public class DummyDiseaseTesting{
	
	private final static String paramsDir = "src/test/resources/";

	
	@Test
	public void checkPeopleGetTheDummyDiseases() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(paramsDir + "params.txt");
		sim.developingModularity = true;
		sim.start();
		int numDays = 8;

		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		// Check that the dummy disase is loaded in to the simulation
		HashSet<DISEASE> toCheck = helperFunctions.InfectionsPresentInSim(sim);

		Assert.assertTrue((toCheck.contains(DISEASE.DUMMY_NCD)) & (toCheck.contains(DISEASE.DUMMY_INFECTIOUS)));
	}
	
	@Test
	public void checkAllDiseasesAppearInWorldDiseaseList() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(paramsDir + "covid_testing_params.txt");
		sim.developingModularity = true;
		sim.start();
		int numDays = 8;

		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		// Check that the dummy disase is loaded in to the simulation
		HashSet<DISEASE> toCheck = helperFunctions.InfectionsPresentInSim(sim);
		boolean dummyNCDPresent = toCheck.contains(DISEASE.DUMMY_NCD);
		boolean dummyInfectiousPresent = toCheck.contains(DISEASE.DUMMY_INFECTIOUS);
		boolean covidPresent = toCheck.contains(DISEASE.COVID);
		boolean covidSymptomsPresent = toCheck.contains(DISEASE.COVIDSPURIOUSSYMPTOM);


		Assert.assertTrue(dummyNCDPresent & dummyInfectiousPresent & covidPresent & covidSymptomsPresent);
	}
	
	@Test
	public void checkVerticalTransmissionOfDummyInfectiousWorks() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		// Increase the birth rate to ensure births take place
		helperFunctions.setParameterListsToValue(sim, sim.params.prob_birth_by_age, 1.0);
		sim.start();
		// turn off deaths to only focus on births.
		helperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		// stop horizontal transmission
		sim.params.dummy_infectious_beta_horizontal = 0.0;
		sim.params.dummy_infectious_beta_vertical = 1.0;
		sim.params.dummy_infectious_recovery_rate = 0.0;
		int numDays = 50;
		// get the number of initial dummy infectious diseases
		int number_of_initial_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key)) number_of_initial_infections ++;
		}
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key)) number_of_new_infections ++;
		}

		Assert.assertTrue(number_of_new_infections > number_of_initial_infections);

	}
	
	@Test
	public void checkHorizontalTransmissionOfDummyInfectiousWorks() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		// Increase the birth rate to ensure births take place
		helperFunctions.setParameterListsToValue(sim, sim.params.prob_birth_by_age, 1.0);
		sim.start();
		// turn off deaths to only focus on births.
		helperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		// stop vertical transmission
		sim.params.dummy_infectious_beta_horizontal = 1.0;
		sim.params.dummy_infectious_beta_vertical = 0.0;
		sim.params.dummy_infectious_recovery_rate = 0.0;
		int numDays = 50;
		// get the number of initial dummy infectious diseases
		int number_of_initial_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key)) number_of_initial_infections ++;
		}
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key)) number_of_new_infections ++;
		}

		Assert.assertTrue(number_of_new_infections > number_of_initial_infections);
	}
	
	@Test
	public void checkNoTransmissionOfDummyNCDHappensWithoutRateOfAcquisition() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		// Increase the birth rate to ensure births take place
		helperFunctions.setParameterListsToValue(sim, sim.params.prob_birth_by_age, 1.0);
		sim.start();
		// turn off deaths to only focus on births.
		helperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		int numDays = 50;
		// get the number of initial dummy infectious diseases
		int number_of_initial_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) number_of_initial_infections ++;
		}
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) number_of_new_infections ++;
		}

		Assert.assertTrue(number_of_new_infections == number_of_initial_infections);
	}
}