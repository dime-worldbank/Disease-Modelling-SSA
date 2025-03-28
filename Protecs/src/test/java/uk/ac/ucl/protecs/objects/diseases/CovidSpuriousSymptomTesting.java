package uk.ac.ucl.protecs.objects.diseases;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.helperFunctions.HelperFunctions.NodeOption;
import uk.ac.ucl.protecs.objects.hosts.Person;

import org.junit.Test;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.CoronavirusDiseaseProgressionFramework.CoronavirusBehaviourNodeTitle;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CovidSpuriousSymptomTesting{
	
	private final static String paramsDir = "src/test/resources/";

	
	@Test
	public void CheckPeopleWithSymptomaticCovidDoNotGetSpuriousSymptoms() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "covid_testing_params.txt");
		sim.start();
		int numDays = 8;
		// Give the population mild Covid and spurious symptoms to see if those with mild covid have their spurious symptoms resolved 
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.MILD),
				NodeOption.CoronavirusInfectiousBehaviour);
		// make sure no one recovers or progresses from their mild covid
		HelperFunctions.StopRecoveryHappening(sim);
		HelperFunctions.HaltDiseaseProgressionAtStage(sim, CoronavirusBehaviourNodeTitle.MILD);

		// make sure there are no new Covid infections
		sim.params.infection_beta = 0.0;
		// make sure that after the initial bout of symptoms, no one develops new spurious symptoms
		sim.params.rate_of_spurious_symptoms = 0.0;
		// create a bunch of spurious symptoms for each person
		giveAFractionASpuriousSymptom(1, sim);
		// run the simulation
		HelperFunctions.runSimulation(sim, numDays);
		// In this scenario we would expect that no one has spurious symptoms
		int numberOfPeopleWithSpuriousSymptoms = 0; 
		
		try {
			numberOfPeopleWithSpuriousSymptoms = checkSpuriousSymptomAndTestingEligibilityHasBeenAssigned(sim, true).size();
			}
		catch (Exception e) {}
		Assert.assertTrue(numberOfPeopleWithSpuriousSymptoms == 0);
	}
	
	@Test
	public void CheckPeopleCanHaveAsymptomaticCovidAndSpuriousSymptoms() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "covid_testing_params.txt");
		sim.start();
		int numDays = 1;
		giveAFractionASpuriousSymptom(1, sim);
		// Give everyone asymptomatic Covid
		HelperFunctions.SetFractionObjectsWithCertainBehaviourNode(1, sim, sim.infectiousFramework.setNodeForTesting(CoronavirusBehaviourNodeTitle.ASYMPTOMATIC),
				NodeOption.CoronavirusInfectiousBehaviour);
		// run the simulation
		HelperFunctions.runSimulation(sim, numDays);
		// we need people with symptoms to be alive and not have any Covid infections of any severity. Use streams to search over the population to 
		// find get the number of people with spurious symptoms who match this criteria
		List<Person> peopleWithSpuriousSymptomsAndAsympt = getPopulationWithSpuriousSymptomsAndAsymptomaticCovid(sim);
		Assert.assertTrue(peopleWithSpuriousSymptomsAndAsympt.size() > 0);
	}
	
	@Test
	public void CheckSettingCovidSpuriousSymptomAndTestingEligibilityPropertiesAreBeingRemovedAfterAWeek() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "covid_testing_params.txt");
		sim.start();
		int numDays = 8;
		// Change the rate of setting Covid spurious symptoms so we have control the number of people who get given symptoms
		sim.params.rate_of_spurious_symptoms = 0.0;
		// Stop new Covid infections from developing
		sim.params.infection_beta = 0.0;
		// remove and existing infections from the population and assign half the population spurious symptoms
		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.COVID.key)) {
				p.die("");
				}
		}
		giveAFractionASpuriousSymptom(0.5, sim);
		// run the simulation
		HelperFunctions.runSimulation(sim, numDays);
		// Check that there are no spurious symptoms remaining in the population by checking that all spurious symptoms are asymptomatic
		List<Disease> peopleWithoutSpuriousSymptoms = checkSpuriousSymptomAndTestingEligibilityHasBeenAssigned(sim, true);
		Assert.assertTrue(peopleWithoutSpuriousSymptoms == null);	
	}
	
	@Test
	public void CheckCovidSpuriousSymptomAndTestingEligibilityPropertiesAreBeingSetWhenCreated() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "covid_testing_params.txt");
		sim.start();
		int numDays = 7;	
		// Remove the development of new symptoms
		sim.params.infection_beta = 0.0;
		// remove all people with covid
		for (Person p: sim.agents) { if (p.getDiseaseSet().containsKey(DISEASE.COVID.key)) {p.die("");}}
		// create spurious symptoms
		giveAFractionASpuriousSymptom(1, sim);
		HelperFunctions.runSimulation(sim, numDays);
		int sizeThatShouldHaveBeenGivenSymptoms = HelperFunctions.GetNumberAlive(sim);
		// Check that there are no spurious symptoms remaining in the population
		List<Disease> peopleWithPropertiesAssigned = checkSpuriousSymptomAndTestingEligibilityHasBeenAssigned(sim, true);		
		Assert.assertTrue(peopleWithPropertiesAssigned.size() == sizeThatShouldHaveBeenGivenSymptoms);	
	}
	

	@Test
	public void CheckSpuriousObjectsAreCreated() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "covid_testing_params.txt");
		sim.start();
		int numDays = 8;
		// Change the rate of setting Covid spurious symptoms so we have control the number of people who get given symptoms
		sim.params.rate_of_spurious_symptoms = 0.5;		
		// Remove the development of new symptoms
		sim.params.infection_beta = 0.0;
		// run the simulation
		HelperFunctions.runSimulation(sim, numDays);
		List<Disease> peopleWithPropertiesAssigned = checkSpuriousSymptomAndTestingEligibilityHasBeenAssigned(sim, true);
		Assert.assertTrue((peopleWithPropertiesAssigned.size() > 0) & (peopleWithPropertiesAssigned.size() < sim.agents.size()));	


	}
	public void giveAFractionASpuriousSymptom(double fraction, WorldBankCovid19Sim sim) {
		for (Person p: sim.agents) {
			if (sim.random.nextDouble() <= fraction) {
			p.addDisease(DISEASE.COVIDSPURIOUSSYMPTOM, new CoronavirusSpuriousSymptom(p, sim, sim.spuriousFramework.getStandardEntryPoint(), 0));
			sim.schedule.scheduleOnce(1, sim.param_schedule_infecting, p.getDiseaseSet().get(DISEASE.COVIDSPURIOUSSYMPTOM.key));
		}
		}
	}
	
	public List<Person> getPopulationWithSpuriousSymptomsAndAsymptomaticCovid(WorldBankCovid19Sim world){
		// get a list of spurious symptoms
		Map<DISEASE, List<Disease>> isSpuriousSymptom = (Map<DISEASE, List<Disease>>) world.infections.stream().collect(
				Collectors.groupingBy(
						Disease::getDiseaseType
						)
				);
		List<Disease> spuriousSymptoms = isSpuriousSymptom.get(DISEASE.COVIDSPURIOUSSYMPTOM);
		
		Map<DISEASE, Map<Boolean, List<Disease>>> isAsymptomaticCovid = (Map<DISEASE, Map<Boolean, List<Disease>>>) world.infections.stream().collect(
				Collectors.groupingBy(
						Disease::getDiseaseType,
						Collectors.groupingBy(
								Disease::isSymptomatic
						)
					)
				);
		List<Disease> asymptomaticCovid = isAsymptomaticCovid.get(DISEASE.COVID).get(false);
		
		ArrayList<Person> filteredPopulation = new ArrayList<Person>();
		
		for (Disease spuriousSympt: spuriousSymptoms) filteredPopulation.add(spuriousSympt.getHost());
		for (Disease asymptCovid: asymptomaticCovid) filteredPopulation.add(asymptCovid.getHost());

		
		return filteredPopulation;
		}
	
	public List<Disease> checkSpuriousSymptomAndTestingEligibilityHasBeenAssigned(WorldBankCovid19Sim world, boolean hasBeenAssigned){
		
		Map<DISEASE, Map<Boolean, List<Disease>>> propertiesChecked = (Map<DISEASE, Map<Boolean, List<Disease>>>) world.infections.stream().collect(
	            Collectors.groupingBy(
	            	Disease::getDiseaseType, 
		            Collectors.groupingBy(
		            		Disease::isSymptomatic,
	                    Collectors.toList()
	                    )
		            )
	               );
		
		return propertiesChecked.get(DISEASE.COVIDSPURIOUSSYMPTOM).get(hasBeenAssigned);
		}
}