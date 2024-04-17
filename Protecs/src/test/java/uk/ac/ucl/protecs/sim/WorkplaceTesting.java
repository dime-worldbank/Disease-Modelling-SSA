package uk.ac.ucl.protecs.sim;

import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.Workplace;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class WorkplaceTesting{
	// ================================================ Testing =======================================================================
	// ===== Here we test that the model is reading in workplaces from the census csv file and are being stored as workplace objects. =
	// ===== We check that subsequently the workplace bubbles are created and are associated with the workplace location created. =====
	// ===== We check that the bubbles are then created for each person, and that those who share a workplace have everyone associated=
	// ===== with that workplace has been included in the bubble ======================================================================
	// ================================================================================================================================
	
	@Test
	public void checkPopulationWorkplacesAreBeingLoaded() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_params.txt", false, false);
		sim.start();
		Assert.assertTrue(sim.workplaces.size() > 0);

	}
	@Test
	public void checkWorkplaceBubblesAreBeingMade() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_params.txt", false, false);
		sim.start();
		boolean hasBeenGivenABubble = true;
		for (Person p: sim.agents) {
			if (p.getWorkBubble().isEmpty()) {hasBeenGivenABubble = false;}
		}
		Assert.assertTrue(hasBeenGivenABubble);
	}
	@Test
	public void checkWorkplaceBubblesContainEveryoneInWorkplace() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_params.txt", false, false);
		sim.start();
		Map<String, List<Person>> belongingToBubble = sim.agents.stream().collect(
				Collectors.groupingBy(
						Person::checkWorkplaceID
						)
				);
		boolean bubbleContainsEveryoneInWorkplace = true;

		for (Workplace w: sim.workplaces) {
			String workplaceID = w.returnID();
			List<Person> thisBubble = belongingToBubble.get(workplaceID);
			HashSet<Person> bubbleAsHashset = new HashSet<Person>(thisBubble);
			for (Person p: thisBubble) {
				bubbleContainsEveryoneInWorkplace = p.getWorkBubble().containsAll(bubbleAsHashset);
			}
		}
		Assert.assertTrue(bubbleContainsEveryoneInWorkplace);

	}
	
	@Test
	public void dev() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_params.txt", false, false);
		sim.start();
		Assert.assertTrue(sim.workplaces.size() > 0);
		int numDays = 20;
		helperFunctions.runSimulation(sim, numDays);
	}
	
	
}