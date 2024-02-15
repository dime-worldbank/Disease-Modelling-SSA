package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Household;
import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.objects.Person;

public class DemographyORIGINAL {
	
	
	
	public static Steppable CreateBirths(WorldBankCovid19Sim world) {
		
		// Functions that control the background demography of the model
		// Create a function to create births in the population
		
		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				
				// create a list of babies
				Params params = world.params;
				ArrayList<Person> newBirths = new ArrayList <Person> ();
				
				//	get a reference to the current simulation day		
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				
				// create an id_offset variable to assign the babies a unique id
				int id_offset = 0;
				
				// iterate over all people (would make sense to do this for woman only)
				for(Person p:world.agents) {
				
					// create a variable to predict the likelihood of pregnancy
					double myPregnancyLikelihood = 0.0;
					// get this person's sex
					String sex = p.getSex();
					// if this person is female and gave birth over a year ago the 9 months pregnancy period reset their 
					//ability to give birth
					if (sex.equals("female") & (p.getDateGaveBirth() < time - 365 - 9 * 30)) {
						p.ableToGiveBirth();
					}
					// get a reference to their age
					int age = p.getAge();
					// if they are a woman, are alive and didn't give birth within the last year consider whether they
					// will give birth today
					if(sex.equals("female") & p.isAlive() & !(p.gaveBirthLastYear())){
						
						// get the probability of giving birth at this age
						myPregnancyLikelihood = params.getLikelihoodByAge(
								params.prob_birth_by_age, params.birth_age_params, age);
						
						if(world.random.nextDouble() < myPregnancyLikelihood) {
							// this woman has given birth, update their birth status and note the time of birth
							p.gaveBirth(time);
							// create attributed for the newborn, id, age, sex, occupation status (lol), their 
							// household (assume this is the mothers), where the baby is, that it's not going to school
							// and a copy of the simulation, then create the person
							int new_id = world.agents.size() + 1 + id_offset;
							int baby_age = 0;
							List<String> sexList = Arrays.asList("male", "female");
							String sexAssigned = sexList.get(world.random.nextInt(sexList.size()));
							String babiesJob = "Not working, inactive, not in universe".toLowerCase();
							Household babyHousehold = p.getHouseholdAsType();
							Location babyDistrict = p.getLocation();
							boolean babySchooling = false;
							int birthday = time;
							Person baby = new Person(new_id, // ID 
									baby_age, // age
									birthday, // date of birth
									sexAssigned, // sex
									babiesJob, // lower case all of the job titles
									babySchooling,
									babyHousehold, // household
									world
									);				
							// update the household and location to include the baby
							babyHousehold.addPerson(baby);
							baby.setLocation(babyDistrict);
							// the baby has decided to go home
							baby.setActivityNode(world.movementFramework.getHomeNode());
							// store the baby in the newBirths array
							newBirths.add(baby);
							// Add the person to the district
							world.personsToDistrict.get(babyDistrict.getRootSuperLocation()).add(baby);
							// ++ the id_offset so the next baby will have an id
							id_offset ++;
						}
					}
				}
				// store the newborns in the population
				for (Person baby:newBirths) {
					world.agents.add(baby);
//					System.out.print("baby " + baby.getID() + " has been born \n");
					}
				
			}
		};
	}
	
	
	
	public static Steppable CheckMortality (WorldBankCovid19Sim world) {
		return new Steppable() {
			@Override
			public void step(SimState arg0) {
				Params params = world.params;

				// create a temp variable to be updated when the likelihood of mortality has been determined
				double myMortalityLikelihood = 0.0;
				
				// iterate over the population
				for(Person p: world.agents){
					// only determine mortality if this person is alive
					if (p.isAlive()){
						// get the person's age and sex
						String sex = p.getSex();
						int age = p.getAge();
						// based on their properties determine if they die today
						if(sex.equals("male")) {
							myMortalityLikelihood = params.getLikelihoodByAge(
									params.prob_death_by_age_male, params.all_cause_death_age_params, age);
						}
						else {
							myMortalityLikelihood = params.getLikelihoodByAge(
									params.prob_death_by_age_female, params.all_cause_death_age_params, age);
						}
						if(world.random.nextDouble() < myMortalityLikelihood) {
							p.die("other");
						}
					}
				}				
			}
		};
	}
	
	public static Steppable UpdateAges(WorldBankCovid19Sim world) {
		return new Steppable() {
			@Override
			public void step(SimState arg0) {
				// create a function to group the population by those who are alive and those who's birthday it is
				Map<Boolean, Map<Integer, List<Person>>> birthday_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::isAlive,
								Collectors.groupingBy(
										Person::getBirthday
						)
						)
						);
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				
				List<Person> birthdays = birthday_map.get(true).get(time + 1 % 365);
				try {
					for(Person p: birthdays){
						// update the person's age
						p.updateAge();
					}
				}
				catch (Exception e) {
						// No one had a birthday today, skip.
				}
			}
		};
	}
}