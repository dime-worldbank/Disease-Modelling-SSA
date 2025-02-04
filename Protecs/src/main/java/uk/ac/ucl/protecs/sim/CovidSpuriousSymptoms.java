package uk.ac.ucl.protecs.sim;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusSpuriousSymptom;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class CovidSpuriousSymptoms implements SpuriousSymptoms {
	
	public static Steppable createSymptomObject(WorldBankCovid19Sim world) {
			
			return new Steppable() {
			@Override
			public void step(SimState arg0) {
	          int time = (int)(arg0.schedule.getTime() / Params.ticks_per_day);
	          // give and remove COVID symptoms
	          createSymptoms(world, time);
	          }
			};
		}
		
	public static void createSymptoms(WorldBankCovid19Sim world, int time) {
		List<Person> people_to_give_symptoms_to = filterForEligiblePeople(world);
		try {
		for (Person p : people_to_give_symptoms_to) {
			CoronavirusSpuriousSymptom CovSpuriousSymptoms = new CoronavirusSpuriousSymptom(p, world, world.spuriousFramework.getStandardEntryPoint(), time);
			p.setHasSpuriousObject();
			world.CovidSpuriousSymptomsList.add(CovSpuriousSymptoms);
			world.schedule.scheduleOnce(world.schedule.getTime(), world.param_schedule_infecting, CovSpuriousSymptoms);
	
	      } 
		} catch (NullPointerException e) {
			// No one is eligible for giving symptoms today
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
	    	                      	Person::hasSpuriousObject,
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
        people_developing_symptoms = SpuriousSymptoms.pickRandomWithoutReplacement(world, has_non_asymptomatic_covid.get(true).get(false).get(false).get(false).get(false), number_people_with_symptoms);
		}
		catch (Exception e) {people_developing_symptoms = Collections.emptyList();}
		return people_developing_symptoms;
		};
		

	

}