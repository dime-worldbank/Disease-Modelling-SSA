package uk.ac.ucl.protecs.sim;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusSpuriousSymptom;
import uk.ac.ucl.protecs.objects.diseases.Infection;
import uk.ac.ucl.protecs.objects.hosts.Person;
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
			p.addInfection(DISEASE.COVIDSPURIOUSSYMPTOM, new CoronavirusSpuriousSymptom(p, world, world.spuriousFramework.getStandardEntryPoint(), time));
			world.schedule.scheduleOnce(world.schedule.getTime(), world.param_schedule_infecting, p.getInfectionSet().get(DISEASE.COVIDSPURIOUSSYMPTOM.key));
	
	      } 
		} catch (NullPointerException e) {
			// No one is eligible for giving symptoms today
		}
	};
	
	public static List<Person>  filterForEligiblePeople(WorldBankCovid19Sim world){
		// We want people without symptomatic covid, and those who don't already have a spurious symptom object already
		
		List<Person> people_developing_symptoms = Collections.emptyList();
		try {
			// get a list of the alive persons at this moment
			Map<Boolean, List<Person>> isAlive = (Map<Boolean, List<Person>>) world.agents.stream().collect(
					Collectors.groupingBy(
							Person::isAlive
							)
					); 
			List<Person> eligiblePersons = isAlive.get(true);
			// get a list of the existing spurious symptoms at this moment
			Map<Boolean, List<Infection>> isSpuriousSymptom = (Map<Boolean, List<Infection>>) world.infections.stream().collect(
					Collectors.groupingBy(
							Infection::isCovidSpuriousSymptom
							)
					); 
			List<Infection> spuriousSymptoms = isSpuriousSymptom.get(true);
			// remove those with existing spurious symptoms from the potential list of people to give symptoms to
			if (spuriousSymptoms != null) {
			for (Infection existingSymptom: spuriousSymptoms) {
				if (eligiblePersons.contains(existingSymptom.getHost())) {
					eligiblePersons.remove(existingSymptom.getHost());
				}
			}
			}
			// get a list of the current symptomatic covid infections
			Map<Boolean, Map<Boolean, Map<Boolean, List<Infection>>>> isSymptomaticCovid = (Map<Boolean, Map<Boolean, Map<Boolean,  List<Infection>>>>) world.infections.stream().collect(
		              Collectors.groupingBy(
		            		  Infection::isHostAlive, 
		            		  Collectors.groupingBy(
		            				  Infection::isCovid, 
		            				  Collectors.groupingBy(
		            						  Infection::isSymptomatic, 
		                      Collectors.toList()
		                      )
		                    )
		                   )	                
		              );
			List<Infection> symptomaticCovid = isSymptomaticCovid.get(true).get(true).get(true);
			if (symptomaticCovid != null) {
			for (Infection symptomaticInfs: symptomaticCovid) {
				if (eligiblePersons.contains(symptomaticInfs.getHost())) {
					eligiblePersons.remove(symptomaticInfs.getHost());
				}
			}
			}
		
		double number_people_with_symptoms_as_double = world.params.rate_of_spurious_symptoms * eligiblePersons.size();
        int number_people_with_symptoms = (int)number_people_with_symptoms_as_double;          
        // Pick a selection of the people eligible for developing spurious symptoms
        people_developing_symptoms = SpuriousSymptoms.pickRandomWithoutReplacement(world, eligiblePersons, number_people_with_symptoms);
		}
		catch (Exception e) {people_developing_symptoms = Collections.emptyList();}
		return people_developing_symptoms;
		};
		

	

}