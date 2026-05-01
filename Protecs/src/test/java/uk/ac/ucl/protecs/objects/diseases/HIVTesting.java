package uk.ac.ucl.protecs.objects.diseases;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Assert;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;


public class HIVTesting {
	// ==================================== Testing ==================================================================
	private final static String paramsDir = "src/test/resources/";
	
	@Test
	public void prevalenceSeedingCreatesCases() {
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_hiv.txt");
		sim.start();
		Assert.assertTrue(sim.human_infections.size() > 0);
	}
	
	@Test
	public void prevalenceWorksAsExpected() {
		// create a simulation and start
		WorldBankCovid19Sim simWithTotalPrevalence = HelperFunctions.CreateDummySim(paramsDir + "params_hiv.txt");
		// set the prevalence of HIV to 1 for all age groups, so everyone should start the sim with HIV
		for (Entry<DISEASE, HashMap<SEX, HashMap<String, Double>>> diseaseEntry : simWithTotalPrevalence.params.prevalenceLineList.entrySet()) {
	        DISEASE disease = diseaseEntry.getKey();
	        HashMap<SEX, HashMap<String, Double>> sexMap = diseaseEntry.getValue();

	        for (Entry<SEX, HashMap<String, Double>> sexEntry : sexMap.entrySet()) {
	            SEX sex = sexEntry.getKey();
	            HashMap<String, Double> ageMap = sexEntry.getValue();

	            for (Entry<String, Double> ageEntry : ageMap.entrySet()) {
	                ageEntry.setValue(1.0);

	            	}
	            }
		}
		simWithTotalPrevalence.start();
		Assert.assertTrue(simWithTotalPrevalence.human_infections.size() == simWithTotalPrevalence.agents.size());
		
		
		// create a simulation and start
		WorldBankCovid19Sim simWithNoPrevalence = HelperFunctions.CreateDummySim(paramsDir + "params_hiv.txt");
		// set the prevalence of HIV to 0 for all age groups, so no one should start the sim with HIV
		for (Entry<DISEASE, HashMap<SEX, HashMap<String, Double>>> diseaseEntry : simWithNoPrevalence.params.prevalenceLineList.entrySet()) {
	        DISEASE disease = diseaseEntry.getKey();
	        HashMap<SEX, HashMap<String, Double>> sexMap = diseaseEntry.getValue();

	        for (Entry<SEX, HashMap<String, Double>> sexEntry : sexMap.entrySet()) {
	            SEX sex = sexEntry.getKey();
	            HashMap<String, Double> ageMap = sexEntry.getValue();

	            for (Entry<String, Double> ageEntry : ageMap.entrySet()) {
	                ageEntry.setValue(0.0);

	            	}
	            }
		}
		simWithNoPrevalence.start();
		Assert.assertTrue(simWithNoPrevalence.human_infections.size() == 0);
		
		// create a simulation and start
		WorldBankCovid19Sim simWithPartialPrevalence = HelperFunctions.CreateDummySim(paramsDir + "params_hiv.txt");
		simWithPartialPrevalence.start();
		Assert.assertTrue((simWithPartialPrevalence.human_infections.size() < simWithTotalPrevalence.human_infections.size()) && 
				(simWithNoPrevalence.human_infections.size() < simWithPartialPrevalence.human_infections.size()));
	}
}