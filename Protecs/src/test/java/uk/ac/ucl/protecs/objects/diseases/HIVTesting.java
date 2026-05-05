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
		// using GBD South Sudan data as an example, increase prevalence of HIV as only 1% in total population, random chance may have this test fail
		for (Entry<DISEASE, HashMap<String, HashMap<String, Double>>> diseaseEntry : sim.params.prevalenceLineList.entrySet()) {
	        DISEASE disease = diseaseEntry.getKey();
	        HashMap<String, HashMap<String, Double>> sexMap = diseaseEntry.getValue();

	        for (Entry<String, HashMap<String, Double>> sexEntry : sexMap.entrySet()) {
	        	String sex = sexEntry.getKey();
	            HashMap<String, Double> ageMap = sexEntry.getValue();

	            for (Entry<String, Double> ageEntry : ageMap.entrySet()) {
	                double prevalence = ageEntry.getValue();
	                prevalence *= 100;
	                ageEntry.setValue(prevalence);

	            	}
	            }
		}
		sim.start();
		Assert.assertTrue(sim.human_infections.size() > 0);
	}
	
	@Test
	public void prevalenceWorksAsExpected() {
		// create a simulation and start
		WorldBankCovid19Sim simWithTotalPrevalence = HelperFunctions.CreateDummySim(paramsDir + "params_hiv.txt");
		// set the prevalence of HIV to 1 for all age groups, so everyone should start the sim with HIV
		for (Entry<DISEASE, HashMap<String, HashMap<String, Double>>> diseaseEntry : simWithTotalPrevalence.params.prevalenceLineList.entrySet()) {
	        DISEASE disease = diseaseEntry.getKey();
	        HashMap<String, HashMap<String, Double>> sexMap = diseaseEntry.getValue();

	        for (Entry<String, HashMap<String, Double>> sexEntry : sexMap.entrySet()) {
	        	String sex = sexEntry.getKey();
	            HashMap<String, Double> ageMap = sexEntry.getValue();

	            for (Entry<String, Double> ageEntry : ageMap.entrySet()) {
	                ageEntry.setValue(100.0);

	            	}
	            }
		}
		simWithTotalPrevalence.start();
		Assert.assertTrue(simWithTotalPrevalence.human_infections.size() == simWithTotalPrevalence.agents.size());
		
		
		// create a simulation and start
		WorldBankCovid19Sim simWithNoPrevalence = HelperFunctions.CreateDummySim(paramsDir + "params_hiv.txt");
		// set the prevalence of HIV to 0 for all age groups, so no one should start the sim with HIV
		for (Entry<DISEASE, HashMap<String, HashMap<String, Double>>> diseaseEntry : simWithNoPrevalence.params.prevalenceLineList.entrySet()) {
	        DISEASE disease = diseaseEntry.getKey();
	        HashMap<String, HashMap<String, Double>> sexMap = diseaseEntry.getValue();

	        for (Entry<String, HashMap<String, Double>> sexEntry : sexMap.entrySet()) {
	        	String sex = sexEntry.getKey();
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
		for (Entry<DISEASE, HashMap<String, HashMap<String, Double>>> diseaseEntry : simWithPartialPrevalence.params.prevalenceLineList.entrySet()) {
	        DISEASE disease = diseaseEntry.getKey();
	        HashMap<String, HashMap<String, Double>> sexMap = diseaseEntry.getValue();

	        for (Entry<String, HashMap<String, Double>> sexEntry : sexMap.entrySet()) {
	            String sex = sexEntry.getKey();
	            HashMap<String, Double> ageMap = sexEntry.getValue();

	            for (Entry<String, Double> ageEntry : ageMap.entrySet()) {
	                double prevalence = ageEntry.getValue();
	                prevalence *= 100;
	                ageEntry.setValue(prevalence);

	            	}
	            }
		}
		simWithPartialPrevalence.start();
		Assert.assertTrue((simWithPartialPrevalence.human_infections.size() < simWithTotalPrevalence.human_infections.size()) && 
				(simWithNoPrevalence.human_infections.size() < simWithPartialPrevalence.human_infections.size()));
	}
	
	
	
	// Future tests --------- these are commented out as the actual epidemiology of HIV is yet to be developed -------------------------------------
	
//	@Test
//	public void verticalTransmissionWorks() {
//		// TODO: When horizontal transmission is created, adjust this test to include a way to block horizontal transmission from occurring
//		
//		// create a simulation and start
//		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_hiv.txt");
//		// using GBD South Sudan data as an example, increase prevalence of HIV as only 1% in total population, random chance may have this test fail
//		for (Entry<DISEASE, HashMap<String, HashMap<String, Double>>> diseaseEntry : sim.params.prevalenceLineList.entrySet()) {
//	        DISEASE disease = diseaseEntry.getKey();
//	        HashMap<String, HashMap<String, Double>> sexMap = diseaseEntry.getValue();
//
//	        for (Entry<String, HashMap<String, Double>> sexEntry : sexMap.entrySet()) {
//	        	String sex = sexEntry.getKey();
//	            HashMap<String, Double> ageMap = sexEntry.getValue();
//
//	            for (Entry<String, Double> ageEntry : ageMap.entrySet()) {
//	                double prevalence = ageEntry.getValue();
//	                prevalence *= 1000;
//	                ageEntry.setValue(prevalence);
//
//	            	}
//	            }
//		}
//		sim.start();
//		int initial_number_of_cases = sim.human_infections.size();
//		// run the simulation for 150 days
//		HelperFunctions.runSimulation(sim, 150);
//		int final_number_of_cases = sim.human_infections.size();
//		Assert.assertTrue(initial_number_of_cases < final_number_of_cases);
//	}
	
//	@Test
//	public void horizontalTransmissionWorks() {}
	
}