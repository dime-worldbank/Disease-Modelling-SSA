package uk.ac.ucl.protecs.behaviours;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;

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
		
		public Mortality( Person p, int ticksPerDay ) {
			this.target = p;
			this.ticksUntilNextMortalityCheck = 365 * ticksPerDay;
		} 

		@Override
		public void step(SimState arg0) {
			if (this.tickToCauseMortality < Integer.MAX_VALUE) {
				System.out.println("Person P_" + String.valueOf(target.getID()) +  " has been scheduled a day to die");
				if (target.isAlive()) {target.die("");}
			}
			else {
				if (arg0.random.nextDouble() < 0.5) {
				int rand_day_of_death = arg0.random.nextInt(365);
				
				this.tickToCauseMortality = rand_day_of_death * this.storedTicksPerDay;
				System.out.println("Scheduling a day to die, day P_" + String.valueOf(target.getID()) + " on " + String.valueOf(rand_day_of_death));
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