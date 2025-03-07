package uk.ac.ucl.protecs.objects.diseases;

import java.util.HashSet;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;

import org.junit.Test;

import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;


public class DummyDiseaseTesting{
	
	private final static String paramsDir = "src/test/resources/";

	
	@Test
	public void CheckPeopleGetTheDummyInfections() {
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
	public void CheckAllInfectionTypesAppearInWorldInfections() {
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
	
	
}