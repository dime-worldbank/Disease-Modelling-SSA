package uk.ac.ucl.protecs.behaviours;

import java.util.Arrays;
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
	
	enum MortalitySteps {
		death,
		noDeath
	}
	
	enum BirthSteps {
		birth,
		noBirth
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
					world.params.prob_death_by_age_male, world.params.all_cause_death_age_params, target.getAge());
				break;
			}
			// ------------------------------------------------------------------------------------------------------------
			// get probability of dying this year if female
			case FEMALE: {
				myMortalityLikelihood = world.params.getLikelihoodByAge(
						world.params.prob_death_by_age_female, world.params.all_cause_death_age_params, target.getAge());	
				break;
			}
			// ------------------------------------------------------------------------------------------------------------
			default: System.out.println("Sex/age based mortality not found");
			}
				
			// check if this person is going to die in the next year. Set up default choice here.
			MortalitySteps nextStep = MortalitySteps.noDeath;
			
			if (arg0.random.nextDouble() <= myMortalityLikelihood){
				nextStep = MortalitySteps.death;
				}
			// act on next step
			switch (nextStep) {
			// ------------------------------------------------------------------------------------------------------------
			case death:{
				// choose a day to die this year, then schedule this death to take place
				this.tickToCauseMortality = arg0.random.nextInt(365) * world.params.ticks_per_day;
				arg0.schedule.scheduleOnce(arg0.schedule.getTime() + this.tickToCauseMortality, this);
				break;
			}
			// ------------------------------------------------------------------------------------------------------------
			case noDeath:{
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
		int daysToRescheduleNextBirth = Integer.MAX_VALUE;
		WorldBankCovid19Sim world;
		public Births( Person p, WorldBankCovid19Sim myWorld ) {
			this.target = p;
			this.ticksUntilNextBirthCheck = myWorld.params.ticks_per_year;
			this.world = myWorld;
		} 
		@Override
		public void step(SimState arg0) {
			// If a due date has been created cause a birth on this day
			if (this.tickToCauseBirth < Integer.MAX_VALUE) {
				createBirth(arg0, target.isAlive());
				postBirthRescheduling(arg0, target.isAlive());
			}
			else {
				determineGivingBirth(arg0, target.isAlive());
				
			}
			
		}
		private void postBirthRescheduling(SimState arg0, boolean isAlive) {
			if (isAlive) {
			// reset tickToCauseBirth so this pathway can be used again
			this.tickToCauseBirth = Integer.MAX_VALUE;
			// reschedule the check to occur next year
			int currentTime = (int) arg0.schedule.getTime();
			int currentYear = (int) Math.floor(currentTime / world.params.ticks_per_year);
			int nextYear = (currentYear + 1);
			arg0.schedule.scheduleOnce(nextYear * world.params.ticks_per_year, this);
		}
		}
		private void determineGivingBirth(SimState arg0, boolean isAlive) {
			if (isAlive) {
			double myPregnancyLikelihood = world.params.getLikelihoodByAge(
					world.params.prob_birth_by_age, world.params.birth_age_params, target.getAge());
			BirthSteps nextStep = BirthSteps.noBirth;
			if (arg0.random.nextDouble() <= myPregnancyLikelihood) {
				nextStep = BirthSteps.birth;
				}
			switch (nextStep) {
			// ------------------------------------------------------------------------------------------------------------
			case birth:{
				// schedule day for the birth
				this.tickToCauseBirth = arg0.random.nextInt(365) * world.params.ticks_per_day;
				arg0.schedule.scheduleOnce(arg0.schedule.getTime() + this.tickToCauseBirth, this);	
				break;
				}
			// ------------------------------------------------------------------------------------------------------------
			case noBirth:{
				// schedule a check for birth next year
				arg0.schedule.scheduleOnce(arg0.schedule.getTime() + this.ticksUntilNextBirthCheck, this);
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
			Location babyAdminZone = target.getLocation();
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
			babyHousehold.addPerson(baby);
			baby.setLocation(babyAdminZone);
			// the baby has decided to go home
			baby.setBehaviourNode(world.movementFramework.getEntryPoint());
			// store the baby in the newBirths array
			world.agents.add(baby);
			// Add the person to the admin zone
			baby.transferTo(babyHousehold);
			// This is a new birth that hasn't been recorded
			target.removeBirthLogged();
			// call on vertical transmission functions for any infections
			for (Disease d: target.getDiseaseSet().values()) {
				d.verticalTransmission(baby);
			}
		}
		}
		
	}
	
}