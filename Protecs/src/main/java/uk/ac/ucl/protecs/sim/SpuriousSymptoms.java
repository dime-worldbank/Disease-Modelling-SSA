package uk.ac.ucl.protecs.sim;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Random;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;

public class SpuriousSymptoms {
	
	public static Steppable spuriosSymptomTrigger(WorldBankCovid19Sim world) {
		
		return new Steppable() {
			
		// ---------------------------------------------- Assigning Spurious symptoms ---------------------------------------------------------------
		// get people who are alive and don't have mild, severe or critical COVID-19 infections which would show symptoms.
		@Override
        public void step(SimState arg0) {
          int time = (int)(arg0.schedule.getTime() / Params.ticks_per_day);
          Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>> has_non_asymptomatic_covid = (Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>>) world.agents.stream().collect(
              Collectors.groupingBy(
                Person::isAlive, 
                Collectors.groupingBy(
                  Person::hasMild, 
                  Collectors.groupingBy(
                    Person::hasSevere, 
                    Collectors.groupingBy(
                      Person::hasCritical, 
                      Collectors.toList())))));
          // We need to work out the number of people who will develop spurious symptoms, calculated are the rate per day, multiplied by number of people who are 
          // 'eligible' for spurious symptoms
          double number_people_with_symptoms_as_double = world.params.rate_of_spurious_symptoms * has_non_asymptomatic_covid.get(true).get(false).get(false).get(false).size();
          int number_people_with_symptoms = (int)number_people_with_symptoms_as_double;
          // We need to generate a random state to use the pickRandom function. Use this simulation seed ((CHECK THAT THIS ISN'T THE SOURCE OF THE ISSUE))
          Random symptom_random = new Random(world.seed());
          // Pick a selection of the people eligible for developing spurious symptoms
          List<Person> people_developing_symptoms = world.pickRandom(has_non_asymptomatic_covid.get(true).get(false).get(false).get(false), number_people_with_symptoms, symptom_random);
          // For each of these people, make them eligible for COVID-19 tests, make them have spurious symptoms, make sure they are removed after 7 days
          for (Person p : people_developing_symptoms) {
            p.elligableForTesting();
            p.setSymptomRemovalDate(time + 7);
            p.setSpuriousSymptoms();
          } 
  		  // ---------------------------------------------- Removing Spurious symptoms ---------------------------------------------------------------
          // We now need to check who will need to have their spurious symptoms removed this day.
          // get everyone who is alive and has spurious symptoms
          Map<Boolean, Map<Boolean, List<Person>>> has_spurios_symptoms = (Map<Boolean, Map<Boolean, List<Person>>>)world.agents.stream().collect(
              Collectors.groupingBy(
                Person::isAlive, 
                Collectors.groupingBy(
                  Person::hasSpuriousSymptoms, 
                  Collectors.toList())));
          List<Person> people_with_symptoms = has_spurios_symptoms.get(true).get(true);
          
          if (people_with_symptoms != null)
        	// If there are people with spurious symptoms...
            for (Person p : people_with_symptoms) {
            	// Go through each individual and then...
              if (p.timeToRemoveSymptoms < time) {
            	// make them unable to go and get a COVID-19 test and then remove their spurious symptoms.
                p.notElligableForTesting();
                p.removeSpuriousSymptoms();
              } 
            }  
        }
      };
}
}