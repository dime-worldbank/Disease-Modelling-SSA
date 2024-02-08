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

public class Demography {
	
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
						myPregnancyLikelihood = params.getBirthLikelihoodByAge(
								params.prob_birth_by_age, age);
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
	
	public static Steppable ReportBirthRates(WorldBankCovid19Sim world) {
		// create a function to report on birth rates
		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				Params params = world.params;

				//	calculate the birth rate in age groups 15-19, 10-14, ..., 45-49
				//	create a list to define our age group search ranges
				List <Integer> upper_age_range = Arrays.asList(20, 25, 30, 35, 40, 45, 50);
				List <Integer> lower_age_range = Arrays.asList(15, 20, 25, 30, 35, 40, 45);
				// create list to store the counts of the number of females alive in each age range and the 
				// number of births in each age range.
				ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_pregnancy_ages = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				Map<String, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getSex, 
								Collectors.groupingBy(
										Person::getAge, 
										Collectors.groupingBy(
												Person::isAlive,
										Collectors.counting()
										)
								)
						)
						);
				// create a function to group the population by sex, age and whether they gave birth
				Map<String, Map<Integer, Map<Boolean, Map<Boolean, Long>>>> age_sex_map_gave_birth = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getSex, 
								Collectors.groupingBy(
										Person::getAge, 
										Collectors.groupingBy(
												Person::gaveBirthLastYear,
												Collectors.groupingBy(
														Person::getBirthLogged,
														Collectors.counting()
										)
								)
						)
						)
						);
				
				//	We now iterate over the age ranges, create a variable to keep track of the iterations
				Integer idx = 0;
				for (Integer val: upper_age_range) {
					// for each age group we begin to count the number of people who fall into each category, create variables
					// to store this information in
					Integer female_count = 0;
					Integer female_gave_birth_count = 0;
					// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
					for (int age = lower_age_range.get(idx); age < val; age++) {
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (female, alive)
							female_count += age_sex_alive_map.get("female").get(age).get(true).intValue();
						}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (female, alive and gave birth)
							female_gave_birth_count += age_sex_map_gave_birth.get("female").get(age).get(true).get(false).intValue();
						}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
					}
					// store what we have found in the lists we created
					female_alive_ages.add(female_count);
					female_pregnancy_ages.add(female_gave_birth_count);
					// update the idx variable for the next iteration
					idx++;
				}
				// calculate the birth rate per 1000 this day
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				String age_dependent_birth_rate = "";

				String t = "\t";
				String age_categories = t + "15_19" + t + "20_24" + t + "25_29" + t + "30_34" + t + "35_39" + t + "40_44" + t + "45_49" + "\n";
				if (time == 0) {
					age_dependent_birth_rate += "day" + age_categories + String.valueOf(time);
				}
				else {
					age_dependent_birth_rate += String.valueOf(time);
				}
				age_dependent_birth_rate += t;
				for (int x = 0; x <female_pregnancy_ages.size(); x++){
					double births_in_age = female_pregnancy_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = births_in_age / female_alive_in_age;
	                result *= 1000;
	                age_dependent_birth_rate += t + String.valueOf(result);
				}
				age_dependent_birth_rate += "\n";


				// create a string to store this information in
				// get the day
				
				ImportExport.exportMe(world.birthRateOutputFilename, age_dependent_birth_rate, world.timer);
				// to make sure that births aren't counted more than once, update this person's properties
				for (Person p: world.agents) {
					if(p.gaveBirthLastYear()) {
						p.confirmBirthlogged();
						}
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
							myMortalityLikelihood = params.getAllCauseLikelihoodByAge(
									params.prob_death_by_age_male, age);
						}
						else {
							myMortalityLikelihood = params.getAllCauseLikelihoodByAge(
									params.prob_death_by_age_female, age);
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