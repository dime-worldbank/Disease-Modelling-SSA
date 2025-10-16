package uk.ac.ucl.protecs.behaviours;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.objects.locations.Workplace;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class Demography {
	WorldBankCovid19Sim myWorld;
	// all cause mortality parameters
	public ArrayList <Integer> all_cause_death_age_params;
	public ArrayList <Double> prob_death_by_age_male;
	public ArrayList <Double> prob_death_by_age_female;
	public ArrayList <Integer> birth_age_params;
	public ArrayList <Double> prob_birth_by_age;
	
	enum MortalitySteps {
		DEATH,
		NO_DEATH
	}
	
	enum BirthSteps {
		INITIALISED_PREGNANT,
		BIRTH,
		PREGNANCY,
		SCHEDULE_PREGNANCY,
		NO_PREGNANCY
	}
	public Demography(WorldBankCovid19Sim myWorld) {
		this.myWorld = myWorld;
		load_all_cause_mortality_params(myWorld.params.dataDir + myWorld.params.all_cause_mortality_filename);
		load_all_birthrate_params(myWorld.params.dataDir + myWorld.params.birth_rate_filename);
	}
	
	public class Aging implements Steppable {

		Person target;
		int ticksUntilNextBirthday = 365;
		
		public Aging(Person p, WorldBankCovid19Sim myWorld) {
			this.target = p;
			this.ticksUntilNextBirthday = myWorld.params.ticks_per_year;
		}
		
		@Override
		public void step(SimState arg0) {
			target.updateAge();
			arg0.schedule.scheduleOnce(arg0.schedule.getTime() + ticksUntilNextBirthday, this);
		}
		
	}
	
	public class Mortality implements Steppable{
		
		Person target;
		int ticksUntilNextMortalityCheck = 0;
		int tickToCauseMortality = Integer.MAX_VALUE;
		WorldBankCovid19Sim world;
		public Mortality( Person p, WorldBankCovid19Sim myWorld ) {
			this.target = p;
			this.ticksUntilNextMortalityCheck = myWorld.params.ticks_per_year;
			this.world = myWorld;
		} 
		// steps taken
		@Override
		public void step(SimState arg0) {
			// This step performs the determining/causing mortality. Each year, a person will have a chance to have a date of
			// death selected. Initially everyone goes through the determineMortality function. Which creates the date of death/reschedules
			// itself for the start of next year if no date is selected.
			if(this.target.isAlive()) {
			if (this.tickToCauseMortality < Integer.MAX_VALUE) {
				causeDeath();
			}
			else {
				
				determineMortality(arg0);
			}		
			}
		}
		// functions used
		private void causeDeath() {
			if (target.isAlive()) {
				target.die("<default>");
				}
		}

		private void determineMortality(SimState arg0) {
			double myMortalityLikelihood = 0.0;
			// do switch operation on the person's sex to determine likelihood of mortality
			switch (target.getSex()) {
			// ------------------------------------------------------------------------------------------------------------
			// get probability of dying this year if male
			case MALE: {
				myMortalityLikelihood = world.params.getLikelihoodByAge(
					prob_death_by_age_male, all_cause_death_age_params, target.getAge());
				break;
			}
			// ------------------------------------------------------------------------------------------------------------
			// get probability of dying this year if female
			case FEMALE: {
				myMortalityLikelihood = world.params.getLikelihoodByAge(
						prob_death_by_age_female, all_cause_death_age_params, target.getAge());	
				break;
			}
			// ------------------------------------------------------------------------------------------------------------
			default: System.out.println("Sex/age based mortality not found");
			}
				
			// check if this person is going to die in the next year. Set up default choice here.
			MortalitySteps nextStep = MortalitySteps.NO_DEATH;
			
			if (arg0.random.nextDouble() <= myMortalityLikelihood){
				nextStep = MortalitySteps.DEATH;
				}
			// act on next step
			switch (nextStep) {
			// ------------------------------------------------------------------------------------------------------------
			case DEATH:{
				// choose a day to die this year, then schedule this death to take place
				this.tickToCauseMortality = arg0.random.nextInt(365) * world.params.ticks_per_day;
				arg0.schedule.scheduleOnce(arg0.schedule.getTime() + this.tickToCauseMortality, this);
				break;
			}
			// ------------------------------------------------------------------------------------------------------------
			case NO_DEATH:{
				// reschedule this whole mortality deciding process to begin next year.
				arg0.schedule.scheduleOnce(arg0.schedule.getTime() + this.ticksUntilNextMortalityCheck, this);
				break;
			}
			// ------------------------------------------------------------------------------------------------------------
			default: {System.out.println("Mortality not determined");
			}
			}
			
		}
		
	}
	
	public class Births implements Steppable{
		
		Person target;
		int ticksUntilNextBirthCheck = 0;
		int tickToCauseBirth = Integer.MAX_VALUE;
		int ticksToUpdatePregnancy = Integer.MAX_VALUE;
		int daysToRescheduleNextBirth = Integer.MAX_VALUE;
		boolean initialSetUp = true;
		WorldBankCovid19Sim world;
		public Births( Person p, WorldBankCovid19Sim myWorld ) {
			this.target = p;
			this.ticksUntilNextBirthCheck = myWorld.params.ticks_per_month;
			this.world = myWorld;
		} 
		@Override
		public void step(SimState arg0) {
			
			determineGivingBirth(arg0, target, initialSetUp);
		}
		
		private void postBirthRescheduling(SimState arg0, boolean isAlive) {
			if (isAlive) {
				// reset tickToCauseBirth so this pathway can be used again
				this.tickToCauseBirth = Integer.MAX_VALUE;
				this.ticksToUpdatePregnancy = Integer.MAX_VALUE;

				// reschedule the check to occur next year
				int currentTime = (int) arg0.schedule.getTime();
				int currentDay = (int) currentTime / world.params.ticks_per_day;
				int currentYear = (int) Math.floor(currentTime / world.params.ticks_per_year);
				int nextYear = (currentYear + 1);
				this.ticksUntilNextBirthCheck = (nextYear * 365 + currentDay) * world.params.ticks_per_day;
				arg0.schedule.scheduleOnce((nextYear * 365 + currentDay) * world.params.ticks_per_day, this);
			}
		}
		private void determineGivingBirth(SimState arg0, Person target, Boolean initialSetUp) {
			// We first need to determine if this person will give birth this year. As some people will have become pregnant before the start of the simulation, we will need to
			// update pregnancy properties when births are scheduled in the first nine months of simulation time
			boolean isAlive = target.isAlive();
			int currentTime = (int) arg0.schedule.getTime();
			int currentDay = (int) arg0.schedule.getTime() / world.params.ticks_per_day;
			
			
			// handle the first time this is set up here (we want to start with some people being pregnant, therefore we need to set up the pregnancies that would
			// have happened before the simulation started.
			if (isAlive) {
				if (initialSetUp) {
					
					// assuming that in the last nine months on average 9/12 of the population would be a year younger we may need to adjust this person's age
					// to accurately represent their likelihood of being pregnant in the last 9 months
					boolean needToAdjustAge = world.random.nextDouble() < (9 / 12);
					int ageForCheck = target.getAge();
					if ((needToAdjustAge) && (target.getAge() > 1)) {
						ageForCheck -= 1;
					}
					double myPregnancyLikelihood = world.params.getLikelihoodByAge(
							prob_birth_by_age, birth_age_params, ageForCheck);
					// increase this likelihood nine-fold to determine if they got pregnant in the last nine months
					double adjustedPregnancyLikelihood = myPregnancyLikelihood * 9;
					BirthSteps nextStep = BirthSteps.NO_PREGNANCY;
					if (arg0.random.nextDouble() <= adjustedPregnancyLikelihood) {
						// this person is pregnant
						nextStep = BirthSteps.INITIALISED_PREGNANT;
					}
					// If they aren't initialised as pregnant do a check again to see if they will be born at some point in the tenth month
					if ((nextStep != BirthSteps.INITIALISED_PREGNANT) && (arg0.random.nextDouble() <= myPregnancyLikelihood)) {
						nextStep = BirthSteps.SCHEDULE_PREGNANCY;
					}
					switch (nextStep) {
					case INITIALISED_PREGNANT:{
						target.setPregnant(true);
						int dayToCauseBirth = arg0.random.nextInt(9 * 30);
						this.tickToCauseBirth = (currentDay + dayToCauseBirth) * world.params.ticks_per_day;
//						System.out.println("First nine months scheduled to give birth on " + (currentDay + dayToCauseBirth));
						// schedule this to rerun on the birth date
						arg0.schedule.scheduleOnce(this.tickToCauseBirth, this);	
						break;
						
					}
					// ------------------------------------------------------------------------------------------------------------
					case SCHEDULE_PREGNANCY:{
						// schedule day for the pregnancy
						int dayToCausePregnancy = arg0.random.nextInt(30);
						// create a corresponding start of pregnancy
						this.ticksToUpdatePregnancy = (currentDay + dayToCausePregnancy) * world.params.ticks_per_day;
//						System.out.println("Starting pregnancy on " + (currentDay + dayToCausePregnancy));

						// schedule this event again on the day to cause pregnancy
						arg0.schedule.scheduleOnce(currentTime + this.ticksToUpdatePregnancy, this);
						break;
					}
					// ------------------------------------------------------------------------------------------------------------
					case NO_PREGNANCY:{
						// schedule a check for birth next month
						arg0.schedule.scheduleOnce(arg0.schedule.getTime() + this.ticksUntilNextBirthCheck, this);
						this.ticksUntilNextBirthCheck += world.params.ticks_per_month;
						break;
						}
					// ------------------------------------------------------------------------------------------------------------
					default: {
						System.out.println("Giving birth not determined");
						}
					}
					
					// after initial set up is done, make sure this pathway isn't followed again
					this.initialSetUp = false;
					return;
				}
			
				// handle this months checks of starting pregnancy in this section
				double myPregnancyLikelihood = world.params.getLikelihoodByAge(
						prob_birth_by_age, birth_age_params, target.getAge());
				BirthSteps nextStep = BirthSteps.NO_PREGNANCY;
				if (arg0.random.nextDouble() <= myPregnancyLikelihood) {
					nextStep = BirthSteps.SCHEDULE_PREGNANCY;
				}
				if (this.ticksToUpdatePregnancy < Integer.MAX_VALUE) {
					nextStep = BirthSteps.PREGNANCY;
				}
				if (this.tickToCauseBirth < Integer.MAX_VALUE) {
					nextStep = BirthSteps.BIRTH;
				}
				
			
				switch (nextStep) {
					case BIRTH:{
						createBirth(arg0, target.isAlive());
						postBirthRescheduling(arg0, target.isAlive());
						break;
					}
					
					case PREGNANCY:{
						target.setPregnant(true);
						// set a date for the birth
						this.tickToCauseBirth =  9 * 30 * world.params.ticks_per_day + ticksToUpdatePregnancy;
						// schedule this to rerun on the birth date
						arg0.schedule.scheduleOnce(this.tickToCauseBirth, this);	
						break;
					
					}
					// ------------------------------------------------------------------------------------------------------------
					case SCHEDULE_PREGNANCY:{
						// schedule day for the pregnancy
						int dayToCausePregnancy = arg0.random.nextInt(30);
						this.ticksToUpdatePregnancy = (currentDay + dayToCausePregnancy) * world.params.ticks_per_day;
//						System.out.println("Starting pregnancy on " + (currentDay + dayToCausePregnancy));

						// schedule this event again on the day to cause pregnancy
						arg0.schedule.scheduleOnce(currentTime + this.ticksToUpdatePregnancy, this);
						break;
								
					}
					// ------------------------------------------------------------------------------------------------------------
					case NO_PREGNANCY:{
						// schedule a check for birth next month
						arg0.schedule.scheduleOnce(this.ticksUntilNextBirthCheck, this);
						this.ticksUntilNextBirthCheck += world.params.ticks_per_month;
						break;
						}
					// ------------------------------------------------------------------------------------------------------------
					default: {
						System.out.println("Giving birth not determined");
						}
				}
			}
		}
		
		private void createBirth(SimState arg0, boolean isAlive) {
			if (isAlive) {
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
//				System.out.println(target.getID() + " giving birth on " + (time));

				target.gaveBirth(time);
				// create attributed for the newborn, id, age, sex, occupation status (lol), their 
				// household (assume this is the mothers), where the baby is, that it's not going to school
				// and a copy of the simulation, then create the person
				int new_id = world.agents.size() + 1;
				int baby_age = 0;
				// although we use an enum for biological sex, upon creation of a person a string is passed to choose sex. This is because
				List<SEX> sexList = Arrays.asList(SEX.MALE, SEX.FEMALE);
				SEX sexAssigned = sexList.get(world.random.nextInt(sexList.size()));
				OCCUPATION babiesJob = OCCUPATION.UNEMPLOYED;
				Household babyHousehold = target.getHouseholdAsType();
				Workplace babyWorkplace = null;
				boolean babySchooling = false;
				int birthday = time;
				Person baby = new Person(new_id, // ID 
						baby_age, // age
						birthday, // date of birth
						sexAssigned, // sex
						babiesJob, // lower case all of the job titles
						babySchooling,
						babyHousehold, // household
						babyWorkplace,
						world
						);				
				// update the household and location to include the baby
				babyHousehold.addHost(baby);
				// the baby has decided to go home
				baby.setActivityNode(world.movementFramework.getEntryPoint());
				// store the baby in the newBirths array
				world.agents.add(baby);
				// Add the person to the admin zone
				baby.transferTo((Household) babyHousehold);
				// This is a new birth that hasn't been recorded
				target.removeBirthLogged();
				// call on vertical transmission functions for any infections
				for (Disease d: target.getDiseaseSet().values()) {
					d.verticalTransmission(baby);
				}
			}
		// reset if they are pregnant or not
		target.setPregnant(false);
		}
		
	}
	public ArrayList<Integer> getAll_cause_death_age_params() {
		return all_cause_death_age_params;
	}

	public void setAll_cause_death_age_params(ArrayList<Integer> all_cause_death_age_params) {
		this.all_cause_death_age_params = all_cause_death_age_params;
	}

	public ArrayList<Double> getProb_death_by_age_male() {
		return prob_death_by_age_male;
	}

	public void setProb_death_by_age_male(ArrayList<Double> prob_death_by_age_male) {
		this.prob_death_by_age_male = prob_death_by_age_male;
	}

	public ArrayList<Double> getProb_death_by_age_female() {
		return prob_death_by_age_female;
	}

	public void setProb_death_by_age_female(ArrayList<Double> prob_death_by_age_female) {
		this.prob_death_by_age_female = prob_death_by_age_female;
	}

	public ArrayList<Integer> getBirth_age_params() {
		return birth_age_params;
	}

	public void setBirth_age_params(ArrayList<Integer> birth_age_params) {
		this.birth_age_params = birth_age_params;
	}

	public ArrayList<Double> getProb_birth_by_age() {
		return prob_birth_by_age;
	}

	public void setProb_birth_by_age(ArrayList<Double> prob_birth_by_age) {
		this.prob_birth_by_age = prob_birth_by_age;
	}
	public void load_all_cause_mortality_params(String filename) {
		try {
			
			if(myWorld.params.verbose)
				System.out.println("Reading in all cause mortality data from " + filename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(filename);

			// Convert our input stream to a BufferedReader
			BufferedReader lineListDataFile = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = lineListDataFile.readLine();

			// map the header into column names relative to location
			String [] header = myWorld.params.splitRawCSVString(s);
			HashMap <String, Integer> columnNames = myWorld.params.parseHeader(header);
			
			// set up data container
			
			all_cause_death_age_params = new ArrayList<Integer> ();
			prob_death_by_age_male = new ArrayList <Double> ();
			prob_death_by_age_female = new ArrayList <Double> ();

			
			// read in the raw data
			while ((s = lineListDataFile.readLine()) != null) {
				String [] bits = myWorld.params.splitRawCSVString(s);
				
				// assemble the age data
				String [] ageRange = bits[0].split("-");
				int maxAge = Integer.MAX_VALUE;
				if(ageRange.length > 1){
					maxAge = Integer.parseInt(ageRange[1]); // take the maximum
				}
				all_cause_death_age_params.add(maxAge);
				
				double male_prob_death  = Double.parseDouble(bits[1]),
						female_prob_death = Double.parseDouble(bits[2]);
				
				// store the values
				prob_death_by_age_male.add(male_prob_death);
				prob_death_by_age_female.add(female_prob_death);

			}
			lineListDataFile.close();
			} catch (Exception e) {
				System.err.println("File input error: " + filename);
				fail();
			}
	}
	
	public void load_all_birthrate_params(String filename) {
		try {
			
			if(myWorld.params.verbose)
				System.out.println("Reading in birth rate data from " + filename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(filename);

			// Convert our input stream to a BufferedReader
			BufferedReader lineListDataFile = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = lineListDataFile.readLine();

			// map the header into column names relative to location
			String [] header = myWorld.params.splitRawCSVString(s);
			HashMap <String, Integer> columnNames = myWorld.params.parseHeader(header);
			
			// set up data container
			
			birth_age_params = new ArrayList<Integer> ();
			prob_birth_by_age = new ArrayList <Double> ();

			
			// read in the raw data
			while ((s = lineListDataFile.readLine()) != null) {
				String [] bits = myWorld.params.splitRawCSVString(s);
				
				// assemble the age data
				String [] ageRange = bits[0].split("-");
				int maxAge = Integer.MAX_VALUE;
				if(ageRange.length > 1){
					maxAge = Integer.parseInt(ageRange[1]); // take the maximum
				}
				birth_age_params.add(maxAge);
				
				double female_prob_birth = Double.parseDouble(bits[1]);
				
				// store the values
				prob_birth_by_age.add(female_prob_birth);

			}
			lineListDataFile.close();
			} catch (Exception e) {
				System.err.println("File input error: " + filename);
				fail();
			}
	}
	
}