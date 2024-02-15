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
			System.out.print("birtday??? " + target.getAge() + ":" + target.toString()  + ":" +  ticksUntilNextBirthday);
			target.updateAge();
			arg0.schedule.scheduleOnce(arg0.schedule.getTime() + ticksUntilNextBirthday, this);
			System.out.println("\tit's my birthday :) " + target.getAge() + "\t" + target.getBirthday());
		}
		
	}
	
}