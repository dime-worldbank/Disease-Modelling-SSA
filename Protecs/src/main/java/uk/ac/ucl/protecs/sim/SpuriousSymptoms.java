package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
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
          double number_people_with_symptoms_as_double = world.params.rate_of_spurious_symptoms * has_non_asymptomatic_covid.get(true).get(false).get(false).get(false).size();
          int number_people_with_symptoms = (int)number_people_with_symptoms_as_double;
          Random symptom_random = new Random(world.seed());
          List<Person> people_developing_symptoms = world.pickRandom(has_non_asymptomatic_covid.get(true).get(false).get(false).get(false), number_people_with_symptoms, symptom_random);
          for (Person p : people_developing_symptoms) {
            p.elligableForTesting();
            p.setSymptomRemovalDate(time + 7);
            p.setSpuriousSymptoms();
          } 
          Map<Boolean, Map<Boolean, List<Person>>> has_spurios_symptoms = (Map<Boolean, Map<Boolean, List<Person>>>)world.agents.stream().collect(
              Collectors.groupingBy(
                Person::isAlive, 
                Collectors.groupingBy(
                  Person::hasSpuriousSymptoms, 
                  Collectors.toList())));
          List<Person> people_with_symptoms = has_spurios_symptoms.get(true).get(true);
          if (people_with_symptoms != null)
            for (Person p : people_with_symptoms) {
              if (p.timeToRemoveSymptoms < time) {
                p.notElligableForTesting();
                p.removeSpuriousSymptoms();
              } 
            }  
        }
      };
}
}