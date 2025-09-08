package uk.ac.ucl.protecs.objects.diseases;

import java.util.HashSet;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;

import org.junit.Test;

import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.helperFunctions.HelperFunctions.birthsOrDeaths;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.objects.hosts.Water;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;


public class DummyDiseaseTesting{
	
	private final static String paramsDir = "src/test/resources/";

	
	@Test
	public void checkPeopleGetTheDummyDiseases() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params.txt");
		sim.developingModularity = true;
		sim.start();
		int numDays = 8;

		// run the simulation
		HelperFunctions.runSimulation(sim, numDays);
		// Check that the dummy disease is loaded in to the simulation
		HashSet<DISEASE> toCheck = HelperFunctions.InfectionsPresentInSim(sim);

		Assert.assertTrue((toCheck.contains(DISEASE.DUMMY_NCD)) & (toCheck.contains(DISEASE.DUMMY_INFECTIOUS)) & (toCheck.contains(DISEASE.DUMMY_WATERBORNE)));
	}
	
	@Test
	public void checkVerticalTransmissionOfDummyInfectiousWorks() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		// Increase the birth rate to ensure births take place
		HelperFunctions.setParameterListsToValue(sim, sim.params.prob_birth_by_age, 1.0);
		sim.start();
		// turn off deaths to only focus on births.
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
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
		HelperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key)) number_of_new_infections ++;
		}

		Assert.assertTrue(number_of_new_infections > number_of_initial_infections);

	}
	
	@Test
	public void checkHorizontalTransmissionOfDummyInfectiousWorks() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		sim.start();
		// turn off births and deaths.
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.births);

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
		HelperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key)) number_of_new_infections ++;
		}

		Assert.assertTrue(number_of_new_infections > number_of_initial_infections);
	}
	
	@Test
	public void checkNoNewCasesOfDummyNCDBeginWithoutRateOfAcquisition() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		sim.start();
		// turn off the rate of dummy NCD acquisition
		sim.params.dummy_ncd_base_rate = 0.0;
		// turn off deaths to only focus on births.
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		int numDays = 50;
		// get the number of initial dummy infectious diseases
		int number_of_initial_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) number_of_initial_infections ++;
		}
		// run the simulation
		HelperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) number_of_new_infections ++;
		}

		Assert.assertTrue(number_of_new_infections == number_of_initial_infections);
	}
	
	@Test
	public void checkThatNewPeopleDevelopTheDummyNCD() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		sim.start();
		// increase the rate of dummy NCD acquisition
		sim.params.dummy_ncd_base_rate = 0.5;
		// turn off deaths to only focus on births.
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		int numDays = 100;
		// get the number of initial dummy infectious diseases
		int number_of_initial_infections = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) number_of_initial_infections ++;
		}
		// run the simulation
		HelperFunctions.runSimulation(sim, numDays);
		int number_of_infections_at_end = 0;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) number_of_infections_at_end ++;
		}

		Assert.assertTrue(number_of_infections_at_end > number_of_initial_infections);
	}
	
	@Test
	public void checkThatMoreNewDummyNCDsOccurInMen() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
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
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.births);

		int numDays = 100;
		// get the number of initial dummy infectious diseases
		int number_of_initial_male_infections = 0;
		int number_of_initial_female_infections = 0;
		for (Person p: sim.agents) {
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getSex().equals(SEX.MALE))) number_of_initial_male_infections ++;
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getSex().equals(SEX.FEMALE))) number_of_initial_female_infections ++;

		}
		// run the simulation
		HelperFunctions.runSimulation(sim, numDays);
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
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
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
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.births);

		int numDays = 100;
		// get the number of initial dummy infectious diseases
		int number_of_initial_over_50_infections = 0;
		int number_of_initial_under_50_infections = 0;
		for (Person p: sim.agents) {
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getAge() > 50)) number_of_initial_over_50_infections ++;
			if ((p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) & (p.getAge() <= 50)) number_of_initial_under_50_infections ++;

		}
		// run the simulation
		HelperFunctions.runSimulation(sim, numDays);
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
	
	@Test
	public void checkDummyWaterborneDiseaseIsSpreadToWater() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		sim.start();
		int number_of_initial_infections_in_water = 0;
		sim.params.dummy_waterborne_prob_shed_into_water = 1;
		sim.params.dummy_waterborne_initial_fraction_with_inf = 0.5;

		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().containsKey(DISEASE.DUMMY_WATERBORNE.key)) number_of_initial_infections_in_water ++;

		}
		int numDays = 50;
		HelperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections_in_water = 0;

		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().containsKey(DISEASE.DUMMY_WATERBORNE.key)) number_of_new_infections_in_water ++;

		}
		Assert.assertTrue(number_of_new_infections_in_water > number_of_initial_infections_in_water);
		}
	
	@Test
	public void checkDummyWaterborneDiseaseIsSpreadToPeople() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		sim.start();
		int number_of_initial_infections_in_people = 0;
		sim.params.dummy_prob_interact_with_water = 1;
		sim.params.dummy_prob_ingest_dummy_waterborne = 0.5;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_WATERBORNE.key)) number_of_initial_infections_in_people ++;

		}
		int numDays = 50;
		HelperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections_in_people = 0;

		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_WATERBORNE.key)) number_of_new_infections_in_people ++;

		}
		Assert.assertTrue(number_of_new_infections_in_people > number_of_initial_infections_in_people);
		}
	
	@Test
	public void checkNoNewDummyWaterborneCasesHappenIfPeopleDoNotInteractWithWater() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		sim.params.dummy_prob_interact_with_water = 0;
		sim.start();
		int number_of_initial_infections_in_both_hosts = 0;

		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_WATERBORNE.key)) number_of_initial_infections_in_both_hosts ++;

		}
		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().containsKey(DISEASE.DUMMY_WATERBORNE.key)) number_of_initial_infections_in_both_hosts ++;

		}
		int numDays = 50;
		HelperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections_in_both_hosts = 0;

		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_WATERBORNE.key)) number_of_new_infections_in_both_hosts ++;

		}
		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().containsKey(DISEASE.DUMMY_WATERBORNE.key)) number_of_new_infections_in_both_hosts ++;

		}
		Assert.assertTrue(number_of_initial_infections_in_both_hosts == number_of_new_infections_in_both_hosts);
		}
	
	@Test
	public void checkThatDuplicatedInfectionTypesDoNotHappen() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "demography_params.txt");
		sim.developingModularity = true;
		int numberOfDiseasesModelledBySim = DISEASE.values().length;
		sim.start();

		int numDays = 100;
		HelperFunctions.runSimulation(sim, numDays);
		boolean number_of_infections_per_host_is_less_than_total_modelled = true;
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().size() > numberOfDiseasesModelledBySim) number_of_infections_per_host_is_less_than_total_modelled = false;
		}
		
		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().size() > numberOfDiseasesModelledBySim) number_of_infections_per_host_is_less_than_total_modelled = false;
		}
		
		Assert.assertTrue(number_of_infections_per_host_is_less_than_total_modelled);
		}
	
}