package behaviours;

import objects.Person;
import sim.Params;
import sim.engine.SimState;

/**
 * The MovementBehaviourFramework is an extension on the basis of the BehaviourFramework which
 * encodes the behaviours given in <THAT DOCUMENT SVETA AND SOPHIE HAVE APPROVED :D >
 * 
 * @author swise
 *
 */
public class MovementBehaviourFramework extends BehaviourFramework {
	
	public static enum Activity {
		HOME, AT_WORK, IN_COMMUNITY, AT_HOSPITAL, DEAD // (AKA removed)
	};

	BehaviourNode workNode = null, communityNode = null, homeNode = null;

	public MovementBehaviourFramework(){
		
		homeNode = new BehaviourNode(){

			@Override
			public String getTitle() {return "Home";}

			@Override
			public double next(Person p, double time) {
				
				// extract time info
				int hour = ((int)time) % Params.ticks_per_day;
				int day = (int)(time / Params.ticks_per_day) % 7;
				
				// if it's morning, go out for the day
				if(hour < 2){ 
					
					// pick a target location to move to
					if(day < 5){ // weekdays
						p.goToWork();
						p.setActivityNode(workNode);
						System.out.println("Person " + p.toString() + " going to work!");
						return 2; // 8 hours work
					}
					else{ 		// weekends
						p.goToCommunity();
						p.setActivityNode(communityNode);
						System.out.println("Person " + p.toString() + " going out to the community!");
						return 3; // 12 hours community
					}
				}
				return 1; // otherwise it's not the morning - stay home for now, but check in again later!
			}		
		};
		
		workNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "At work"; }

			@Override
			public double next(Person p, double time) {

				// extract time info
				int hour = ((int)time) % Params.ticks_per_day;
				
				// if it's too late, go straight home
				if(hour > 5){
					p.goHome();
					p.setActivityNode(homeNode);
					System.out.println("Person " + p.toString() + " going home!");
					return 3; // 12 hours at home! These agents are very well-rested
				}
				
				// otherwise, go out into the community!
				p.goToCommunity();
				p.setActivityNode(communityNode);
				System.out.println("Person " + p.toString() + " going out to the community after work!");

				return 1; // for out for a bit!
			}
			
		};
		
		communityNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "In community"; }

			@Override
			public double next(Person p, double time) {
				// extract time info
				int hour = ((int)time) % Params.ticks_per_day;
				int day = (int)(time / Params.ticks_per_day) % 7;

				if(hour >= 5) { // late! Go home!
					p.goHome();
					p.setActivityNode(homeNode);
					System.out.println("Person " + p.toString() + " going home from the community!");

					return 3; // 12 hours at home!
				}
				return 1; // check in again soon, but we have more time!
			}
			
		};
		
		entryPoint = homeNode;
	}
	
/**	public double update(Person p){
		
		// based on current activity, move on to the next one!
		switch(p.getActivity()){
		
			case HOME: // the agent is currently home
				System.out.println("Person " + p.toString() + " is going to work!");
				return goOut(p);
				
			case AT_WORK:
				if(!p.isHome()) {
					p.goHome();
					System.out.println("Person " + p.toString() + " is going home!");
				}
				else
					System.out.println("Person " + p.toString() + " is off work now!");

				p.setActivity(Activity.HOME);
				return 4;
				
			default:
				System.out.println("Activity " + p.getActivity() + " has not yet been implemented!");
				break;
		}
		return -1;
	}
	
	public double goOut(Person p){
		p.setActivity(Activity.AT_WORK);
		return p.goToWork();
	}
	*/
	
	public BehaviourNode getEntryPoint(){
		return entryPoint;
	}
}