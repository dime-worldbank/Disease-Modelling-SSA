package uk.ac.ucl.protecs.sim;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;

public class CovidSpuriousSymptoms implements SpuriousSymptoms {
	
@SuppressWarnings("serial")
public static Steppable manageSymptoms(WorldBankCovid19Sim world) {
		
		return new Steppable() {
		@Override
		public void step(SimState arg0) {
          int time = (int)(arg0.schedule.getTime() / Params.ticks_per_day);
          // give and remove COVID symptoms
          giveSymptoms(world, time);
          removeSymptoms(world, time);
          }
		};
	}

	public static void giveSymptoms(WorldBankCovid19Sim world, int time) {
		List<Person> people_to_give_symptoms_to = filterForEligiblePeople(world);
		try {
		for (Person p : people_to_give_symptoms_to) {
			setSymptomsInPerson(p, time, world);
          } 
		} catch (NullPointerException e) {
			// No one is eligible for giving symptoms today
		}
	};
	
	public static void removeSymptoms(WorldBankCovid19Sim world, int time) {
		List<Person> people_with_symptoms_to_remove = filterForSymptomsToRemove(world);
		try {
            for (Person p : people_with_symptoms_to_remove) {
            	removeSymptomsInPerson(p);
              } 
		} catch (NullPointerException e) {
			// No one is eligible for symptom removal today
		}
		
	};

	
	public static List<Person>  filterForEligiblePeople(WorldBankCovid19Sim world){
		List<Person> people_developing_symptoms = Collections.emptyList();
		try {

		Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>>> has_non_asymptomatic_covid = (Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>>>) world.agents.stream().collect(
	              Collectors.groupingBy(
	                Person::isAlive, 
	                Collectors.groupingBy(
	                  Person::hasMild, 
	                  Collectors.groupingBy(
	                    Person::hasSevere, 
	                    Collectors.groupingBy(
	                      Person::hasCritical, 
	                      Collectors.groupingBy(
	                      	Person::hasCovidSpuriousSymptoms,
	                      Collectors.toList()
	                      )
	                    )
	                    )
	                  )
	                )
	              );
		
		double number_people_with_symptoms_as_double = world.params.rate_of_spurious_symptoms * has_non_asymptomatic_covid.get(true).get(false).get(false).get(false).get(false).size();
        int number_people_with_symptoms = (int)number_people_with_symptoms_as_double;          
        // Pick a selection of the people eligible for developing spurious symptoms
        people_developing_symptoms = SpuriousSymptoms.pickRandom(world, has_non_asymptomatic_covid.get(true).get(false).get(false).get(false).get(false), number_people_with_symptoms);
		}
		catch (Exception e) {people_developing_symptoms = Collections.emptyList();}
		return people_developing_symptoms;
		};
		
	public static List<Person>  filterForSymptomsToRemove(WorldBankCovid19Sim world){
		List<Person> people_with_symptoms_to_remove = Collections.emptyList();
		try {
			
			Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>> has_spurios_symptoms_due_for_removal = (Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>)  world.agents.stream().collect(
		              Collectors.groupingBy(
		                Person::isAlive, 
		                Collectors.groupingBy(
		                  Person::hasCovidSpuriousSymptoms, 
		                  Collectors.groupingBy(
		    	                  Person::removeCovidSpuriousSymptomsToday,
		                  Collectors.toList()
		                  )
		                  )
		                )
		              );
			people_with_symptoms_to_remove = has_spurios_symptoms_due_for_removal.get(true).get(true).get(false);
	    }
	    catch (Exception e) {people_with_symptoms_to_remove = Collections.emptyList();}

		return people_with_symptoms_to_remove;
		};
	
	
	public static void setSymptomsInPerson(Person p, int time, WorldBankCovid19Sim world) {
		p.setCovidSpuriousSymptoms();
		p.setEligibleForCovidTesting();
		p.setCovidSpuriousSymptomRemovalDate(time + 7);
	};
	public static void removeSymptomsInPerson(Person p) {
		p.removeCovidSpuriousSymptoms();
		p.removeEligibilityForCovidTesting();
		p.setCovidSpuriousSymptomRemovalDate(Integer.MAX_VALUE);
	};
}