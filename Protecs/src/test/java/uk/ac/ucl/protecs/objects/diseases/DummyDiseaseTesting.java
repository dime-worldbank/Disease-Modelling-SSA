package uk.ac.ucl.protecs.objects.diseases;

import java.util.HashSet;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;

import org.junit.Test;

import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.helperFunctions.helperFunctions.birthsOrDeaths;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
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
		sim.start();
		// turn off the rate of dummy NCD acquisition
		sim.params.dummy_ncd_base_rate = 0.0;
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
	
	@Test
	public void checkThatNewPeopleDevelopTheDummyNCD() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		sim.start();
		// increase the rate of dummy NCD acquisition
		sim.params.dummy_ncd_base_rate = 0.5;
		// turn off deaths to only focus on births.
		helperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		int numDays = 100;
		// get the number of initial dummy infectious diseases
		int number_of_initial_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) number_of_initial_infections ++;
		}
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		int number_of_infections_at_end = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) number_of_infections_at_end ++;
		}

		Assert.assertTrue(number_of_infections_at_end > number_of_initial_infections);
	}
	
	@Test
	public void checkThatMoreNewDummyNCDsOccurInMen() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		sim.params.dummy_ncd_initial_fraction_with_ncd = 0;
		sim.start();
		
		// equalise the number of men and women in the simulation
		int initial_number_of_women = 0;
		int initial_number_of_men = 0;

		for (Person p: sim.agents) {
			if (p.getSex().equals(SEX.MALE)) initial_number_of_men ++;
			if (p.getSex().equals(SEX.FEMALE)) initial_number_of_women ++;

		}
		for (Person p: sim.agents) {
			if (p.getSex().equals(SEX.FEMALE)) {
				p.die("");
				initial_number_of_women --;
				if (initial_number_of_women < initial_number_of_men) break;
			}

		}
		
		// increase the rate of dummy NCD acquisition
		sim.params.dummy_ncd_rr_over_50 = 1.0;
		// increase the relative risk of developing this condition if male
		sim.params.dummy_ncd_rr_male = 2;
		// turn off deaths to only focus on births.
		helperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		helperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.births);

		int numDays = 100;
		// get the number of initial dummy infectious diseases
		int number_of_initial_male_infections = 0;
		int number_of_initial_female_infections = 0;
		for (Person p: sim.agents) {
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getSex().equals(SEX.MALE))) number_of_initial_male_infections ++;
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getSex().equals(SEX.FEMALE))) number_of_initial_female_infections ++;

		}
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		int number_of_male_infections_at_end = 0;
		int number_of_female_infections_at_end = 0;

		for (Person p: sim.agents) {
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getSex().equals(SEX.MALE))) number_of_male_infections_at_end ++;
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getSex().equals(SEX.FEMALE))) number_of_female_infections_at_end ++;

		}
		int new_male_cases = number_of_male_infections_at_end - number_of_initial_male_infections;
		int new_female_cases = number_of_female_infections_at_end - number_of_initial_female_infections;

		Assert.assertTrue(new_male_cases > new_female_cases);
	}
	@Test
	public void checkThatMoreNewDummyNCDsInOver50s() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		sim.params.dummy_ncd_initial_fraction_with_ncd = 0;
		sim.start();
		
		// equalise the number of over and under 50s in the simulation
		int initial_number_over_50 = 0;
		int initial_number_under_50 = 0;

		for (Person p: sim.agents) {
			if (p.getAge() > 50) initial_number_over_50 ++;
			if (p.getAge() <= 50) initial_number_under_50 ++;

		}
		
		for (Person p: sim.agents) {
			if (p.getAge() <= 50) {
				p.die("");
				initial_number_under_50 --;
				if (initial_number_under_50 < initial_number_over_50) break;
			}

		}
		
		// increase the rate of dummy NCD acquisition
		sim.params.dummy_ncd_rr_over_50 = 2.0;
		// increase the relative risk of developing this condition if male
		sim.params.dummy_ncd_rr_male = 1.0;
		// turn off deaths to only focus on births.
		helperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		helperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.births);

		int numDays = 100;
		// get the number of initial dummy infectious diseases
		int number_of_initial_over_50_infections = 0;
		int number_of_initial_under_50_infections = 0;
		for (Person p: sim.agents) {
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getAge() > 50)) number_of_initial_over_50_infections ++;
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getAge() <= 50)) number_of_initial_under_50_infections ++;

		}
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		int number_of_over_50_infections_at_end = 0;
		int number_of_under_50_infections_at_end = 0;

		for (Person p: sim.agents) {
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getAge() > 50)) number_of_over_50_infections_at_end ++;
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getAge() <= 50)) number_of_under_50_infections_at_end ++;

		}
		int new_under_50_cases = number_of_under_50_infections_at_end - number_of_initial_under_50_infections;
		int new_over_50_cases = number_of_over_50_infections_at_end - number_of_initial_over_50_infections;

		Assert.assertTrue(new_over_50_cases > new_under_50_cases);
	}
}