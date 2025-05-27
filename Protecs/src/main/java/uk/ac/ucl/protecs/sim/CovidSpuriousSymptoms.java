package uk.ac.ucl.protecs.sim;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusSpuriousSymptom;
import uk.ac.ucl.protecs.objects.diseases.Disease;
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
			p.addDisease(new CoronavirusSpuriousSymptom(p, world, world.spuriousFramework.getEntryPoint(), time));
			world.schedule.scheduleOnce(world.schedule.getTime(), world.param_schedule_infecting, p.getDiseaseSet().get(DISEASE.COVIDSPURIOUSSYMPTOM.key));
	
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
			
			Map<DISEASE, List<Disease>> isSpuriousSymptom = (Map<DISEASE, List<Disease>>) world.human_infections.stream().collect(
					Collectors.groupingBy(
							Disease::getDiseaseType, Collectors.toList()
							)
					);
			// get a list of the existing spurious symptoms at this moment
//			Map<Boolean, List<Infection>> isSpuriousSymptom = (Map<Boolean, List<Infection>>) world.infections.stream().collect(
//					Collectors.groupingBy(
//							Infection::isOfType
//							)
//					); 
			List<Disease> spuriousSymptoms = isSpuriousSymptom.get(DISEASE.COVIDSPURIOUSSYMPTOM);
			// remove those with existing spurious symptoms from the potential list of people to give symptoms to
			if (spuriousSymptoms != null) {
			for (Disease existingSymptom: spuriousSymptoms) {
				if (eligiblePersons.contains(existingSymptom.getHost())) {
					eligiblePersons.remove(existingSymptom.getHost());
				}
			}
			}
			// get a list of the current symptomatic covid infections
			Map<Boolean, Map<DISEASE, Map<Boolean, List<Disease>>>> isSymptomaticCovid = (Map<Boolean, Map<DISEASE, Map<Boolean,  List<Disease>>>>) world.human_infections.stream().collect(
		              Collectors.groupingBy(
		            		  Disease::isHostAlive, 
		            		  Collectors.groupingBy(
		            				  Disease::getDiseaseType, 
		            				  Collectors.groupingBy(
		            						  Disease::isSymptomatic, 
		                      Collectors.toList()
		                      )
		                    )
		                   )	                
		              );
			List<Disease> symptomaticCovid = isSymptomaticCovid.get(true).get(DISEASE.COVID).get(true);
			if (symptomaticCovid != null) {
			for (Disease symptomaticInfs: symptomaticCovid) {
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