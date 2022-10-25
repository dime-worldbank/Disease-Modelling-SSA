package behaviours;

import objects.Location;
import objects.Person;
import sim.Params;
import sim.WorldBankCovid19Sim;
import sim.engine.Steppable;
import swise.behaviours.BehaviourFramework;
import swise.behaviours.BehaviourNode;

/**
 * The MovementBehaviourFramework is an extension on the basis of the BehaviourFramework which
 * encodes the behaviours given in <THAT DOCUMENT SVETA AND SOPHIE HAVE APPROVED :D >
 * 
 * @author swise
 *
 */
public class MovementBehaviourFramework extends BehaviourFramework {
	
	WorldBankCovid19Sim myWorld;
	BehaviourNode workNode = null, communityNode = null, homeNode = null;

	public MovementBehaviourFramework(WorldBankCovid19Sim model){
		myWorld = model;
		
		homeNode = new BehaviourNode(){

			@Override
			public String getTitle() {return "Home";}

			@Override
			public double next(Steppable s, double time) {
				
				Person p = (Person) s;
				
				// the Person may have been sent home immobilised: update everything and don't schedule
				// to run again until it has been un-immobilised!
				if(p.isImmobilised()) {
					p.setVisiting(false);
					p.setAtWork(false);
					return Double.MAX_VALUE; 
				}
				
				// extract time info
				int hour = ((int)time) % Params.ticks_per_day;
				int day = (int)(time / Params.ticks_per_day) % 7; // because 7 days in a week
				
				// determine likelihood of leaving the home today
				double myEconStatProb = myWorld.params.getEconProbByDay(day, p.getEconStatus());
				if(myWorld.random.nextDouble() > myEconStatProb)
					return myWorld.params.ticks_per_day; // rest until tomorrow

				
				// if it's morning, go out for the day
				if(hour >= myWorld.params.hour_start_day_weekday){ 

					Location target;
					target = myWorld.params.getTargetMoveDistrict(p, day, myWorld.random.nextDouble(), myWorld.lockedDown);
					
					// define workday
					boolean goToWork = (p.isSchoolGoer() || target == p.getCommunityLocation()) // schoolgoer or going to own district
							&& myWorld.params.isWeekday(day);				// it's a weekday

					if(myWorld.params.setting_perfectMixing) // in perfect mixing, just go to the community!
						goToWork = false;
					
					p.transferTo(target);
					
					// update appropriately
					if(goToWork){ // working
						p.setActivityNode(workNode);
						p.setAtWork(true);
						p.setVisiting(false);
						return myWorld.params.hours_at_work_weekday;
					}
					
					else if(target == p.getCommunityLocation()) { // in home district, not working
						p.setActivityNode(communityNode);
						p.setAtWork(false);						
						return myWorld.params.hour_end_day_otherday - hour; // stay out until time to go home!
					}
					
					else{ // travelling to another district!
						p.setActivityNode(communityNode);
						p.setAtWork(false);
						p.setVisiting(true);
						return myWorld.params.hour_end_day_otherday - hour; // stay out until time to go home!
					}
				}
				return 1; // otherwise it's not the morning - stay home for now, but check in again later!
			}		
		};
		
		workNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "At work"; }

			@Override
			public double next(Steppable s, double time) {

				Person p = (Person) s;
				
				// extract time info
				int hour = ((int)time) % Params.ticks_per_day;
				
				// if it's too late, go straight home
				if(hour > myWorld.params.hour_end_day_weekday){
					p.transferTo(p.getHousehold());
					p.setActivityNode(homeNode);
					p.setAtWork(false);
					return myWorld.params.hours_sleeping;
				}
				
				// if there is some time before going home, go out into the community!
				else if(hour <= myWorld.params.hour_end_day_weekday) {
					p.transferTo(p.getCommunityLocation());
					p.setActivityNode(communityNode);
					p.setAtWork(false);
					return 1; // 4 hours in the community
				}

				return 1; // otherwise, stay at work
			}
			
		};
		
		communityNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "In community"; }

			@Override
			public double next(Steppable s, double time) {
				
				Person p = (Person) s;
				
				// extract time info
				int hour = ((int)time) % Params.ticks_per_day;

				if(hour >= myWorld.params.hour_end_day_otherday) { // late! Go home!
					p.transferTo(p.getHousehold());
					p.setActivityNode(homeNode);
					p.setVisiting(false);

					return myWorld.params.hours_sleeping;
				}
				return 1; // check in again soon, but we have more time!
			}
			
		};
		
		entryPoint = homeNode;
	}
	
	public BehaviourNode getHomeNode(){
		return entryPoint;
	}
}