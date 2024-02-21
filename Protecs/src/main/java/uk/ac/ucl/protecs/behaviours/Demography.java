package uk.ac.ucl.protecs.behaviours;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class Demography {
	
	public class Aging implements Steppable {

		Person target;
		int ticksUntilNextBirthday = 365;
		
		public Aging(Person p, int ticksPerDay) {
			this.target = p;
			this.ticksUntilNextBirthday = 365 * ticksPerDay;
		}
		
		@Override
		public void step(SimState arg0) {
			target.updateAge();
			arg0.schedule.scheduleOnce(arg0.schedule.getTime() + ticksUntilNextBirthday, this);
		}
		
	}
	
	public class Mortality implements Steppable{
		
		Person target;
		int ticksUntilNextMortalityCheck = 365;
		int tickToCauseMortality = Integer.MAX_VALUE;
		int storedTicksPerDay = 6; // hard coded
		WorldBankCovid19Sim world;
		public Mortality( Person p, int ticksPerDay, WorldBankCovid19Sim myWorld ) {
			this.target = p;
			this.ticksUntilNextMortalityCheck = 365 * ticksPerDay;
			this.world = myWorld;
		} 

		@Override
		public void step(SimState arg0) {
			if (this.tickToCauseMortality < Integer.MAX_VALUE) {
				if (target.isAlive()) {target.die("");}
			}
			else {
			}
				double myMortalityLikelihood;
				if(target.getSex().equals("male")) {
					myMortalityLikelihood = world.params.getLikelihoodByAge(
							world.params.prob_death_by_age_male, world.params.all_cause_death_age_params, target.getAge());
				}
				else {
					myMortalityLikelihood = world.params.getLikelihoodByAge(
							world.params.prob_death_by_age_female, world.params.all_cause_death_age_params, target.getAge());

				if (arg0.random.nextDouble() < myMortalityLikelihood) {
				int rand_day_of_death = arg0.random.nextInt(365);
				
				this.tickToCauseMortality = rand_day_of_death * this.storedTicksPerDay;
				arg0.schedule.scheduleOnce(arg0.schedule.getTime() + this.tickToCauseMortality, this);
				}
				else {
					arg0.schedule.scheduleOnce(arg0.schedule.getTime() + this.ticksUntilNextMortalityCheck, this);

				}


				
			}
			// TODO Auto-generated method stub
			
		}
		
	}
	
}