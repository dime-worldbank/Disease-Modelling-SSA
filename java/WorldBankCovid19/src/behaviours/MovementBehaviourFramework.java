package behaviours;

import objects.Location;
import objects.Person;
import sim.Params;
import sim.WorldBankCovid19Sim;
import sim.engine.SimState;
import sim.engine.Steppable;

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
				
				// extract time info
				int hour = ((int)time) % Params.ticks_per_day;
				int day = (int)(time / Params.ticks_per_day) % 7; // because 7 days in a week
				
				// TODO do they even go out at all? Ref to ECONOMIC_STATUS_WEEKDAY_MOVEMENT_PROBABILITY.txt
				// determine likelihood of leaving the home today
				//double myEconStatProb = myWorld.params.getEconProbByDay(day, p.getEconStatus());
				//if(myWorld.random.nextDouble() > myEconStatProb)
				//	return 6; // rest until the same time tomorrow

				
				// if it's morning, go out for the day
				if(hour > 1 && hour <= 3){ 

					// define workday
					boolean goToWork = p.getEconomicLocation() != null && myWorld.params.isWeekday(day);

					// TODO students/teachers just don't move
					
					// pick a target location to move to
					if(goToWork){ // weekdays
						p.transferTo(p.getEconomicLocation());
						p.setActivityNode(workNode);
//						System.out.println("Person " + p.toString() + " going to work!");
						return 8 / Params.hours_per_tick; // 8 hours work
					}
					else{ 		// weekends
						Location target = myWorld.params.getTargetMoveDistrict(p, day, myWorld.random.nextDouble());
						p.transferTo(target);
						p.setActivityNode(communityNode);
//						System.out.println("Person " + p.toString() + " going out to community " + target.toString());
						return 12 / Params.hours_per_tick; // 12 hours community
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
				if(hour > 5){
					p.transferTo(p.getHousehold());
					p.setActivityNode(homeNode);
//					System.out.println("Person " + p.toString() + " going home!");
					return 12 / Params.hours_per_tick; // 12 hours at home! These agents are very well-rested
				}
				
				// if there is some time before going home, go out into the community!
				else if(hour > 3) {
					p.transferTo(p.getCommunityLocation());
					p.setActivityNode(communityNode);
//					System.out.println("Person " + p.toString() + " going out to the community after work!");
					return 1; // 4 hours in the community
				}

				return 1; // otherwise, another 4 hours at work
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

				if(hour >= 5) { // late! Go home!
					p.transferTo(p.getHousehold());
					p.setActivityNode(homeNode);
//					System.out.println("Person " + p.toString() + " going home from the community!");

					return 12 / Params.hours_per_tick; // 12 hours at home!
				}
				return 1; // check in again soon, but we have more time!
			}
			
		};
		
		entryPoint = homeNode;
	}
	
	public BehaviourNode getEntryPoint(){
		return entryPoint;
	}
}