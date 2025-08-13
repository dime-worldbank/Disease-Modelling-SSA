package uk.ac.ucl.protecs.behaviours;

import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.*;
import sim.engine.Steppable;
import uk.ac.ucl.swise.behaviours.BehaviourFramework;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

/**
 * The MovementBehaviourFramework is an extension on the basis of the BehaviourFramework which
 * encodes the behaviours given in <THAT DOCUMENT SVETA AND SOPHIE HAVE APPROVED :D >
 * 
 * @author swise
 *
 */
public class MovementBehaviourFramework implements BehaviourFramework {
	
	WorldBankCovid19Sim myWorld;
	BehaviourNode workNode = null, communityNode = null, homeNode = null;
	
	public enum mobilityNodeTitle{
        HOME("home"), WORK("work"), COMMUNITY("community");
         
        public String key; 
     
        mobilityNodeTitle(String key) { this.key = key; }
    
        static mobilityNodeTitle getValue(String x) {
        	
        	switch (x) {
        	case "home":
        		return HOME;
        	case "work":
        		return WORK;
        	case "community":
        		return COMMUNITY;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
   }
	public MovementBehaviourFramework(WorldBankCovid19Sim model){
		myWorld = model;
		
		homeNode = new BehaviourNode(){
			
			@Override
			public String getTitle() {return mobilityNodeTitle.HOME.key;}

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
				assert (hour >= 0) : "Hour of the day not valid, somehow has become negative " + hour;
				assert (day >= 0) : "Day not valid, somehow has become negative " + day;
				// determine likelihood of leaving the home today
				double myEconStatProb = myWorld.params.getEconProbByDay(day, p.getEconStatus());
				assert (myEconStatProb >= 0.0) & (myEconStatProb <= 1.0) : "Probability not valid " + myEconStatProb;
				if(myWorld.random.nextDouble() > myEconStatProb)
					return myWorld.params.ticks_per_day; // rest until tomorrow

				// if it's morning, go out for the day, reset the number of contacts they will have
				if(hour >= myWorld.params.hour_start_day_weekday){ 
					// reset occurs at hour 2
					p.resetWorkplaceContacts();
					p.resetCommunityContacts();

					return determineDailyRoutine(p, hour, day);
				}
				
				return 1; // otherwise it's not the morning - stay home for now, but check in again later!
			}
			
			private double determineDailyRoutine(Person p, int hour, int day) {
				Location target;
				target = myWorld.params.getTargetMoveAdminZone(p, day, myWorld.random.nextDouble(), myWorld.lockedDown);
				// then check if they are supposed to leave the admin zone they are currently in. If so, then they cannot go to work.
				boolean stayingInHomeDistrict = target.getId().equals(p.getHomeLocation().getRootSuperLocation().getId());
				// First check if they are visiting another district
				if (!stayingInHomeDistrict) p.setVisiting(true);
				if (p.visitingNow() & !stayingInHomeDistrict) {
					// travelling to another district!
					p.transferTo(target);
					p.setActivityNode(communityNode);
					p.setAtWork(false);
					assert ! p.getHomeLocation().getSuper().equals(target) : 
						"set to travel to a different district but didn't, home/target " + p.getHomeLocation().getSuper().getId() + " " + target.getId();
					 // stay out until time to go home!
					return myWorld.params.hour_end_day_otherday - hour;
				}
				// if they aren't visiting, are they at work or the community?
				else {
					// Check they are going to work
					boolean goToWork = myWorld.random.nextDouble() < myWorld.params.prob_go_to_work;

					// if unemployed or homemaker don't go to work
					if (p.isUnemployed()) {
						goToWork = false;
					}
					// first check if there is any constraints to this occupations movements. Note that if the model is using this code block then 
					// this person has not been immobilised and if their movement is constrained it will mean that they only go to the community and not to work
					boolean movementConstrained = myWorld.params.OccupationConstraintList.containsKey(p.getEconStatus());
						
					if(myWorld.params.setting_perfectMixing) // in perfect mixing, just go to the community!
						goToWork = false;
					
					if (goToWork & !movementConstrained) target = p.getWorkLocation();

					
					p.transferTo(target);
					assert (p.getLocation().equals(target)) : "Transfer to target didn't work, meant to be at " + target.getId() + " but is instead at " + p.getLocation().getId();
					// update appropriately
					if(goToWork){ // working
						p.setActivityNode(workNode);
						p.setAtWork(true);
						p.setVisiting(false);
						return myWorld.params.hours_at_work_weekday;
					}					
					else { // in home district, not working
						p.setActivityNode(communityNode);
						p.setAtWork(false);	
						p.setVisiting(false);
						assert p.getHomeLocation().getSuper().getId().equals(target.getId()) : 
							"set to travel to a within admin zone but didn't, home/target is " + p.getHomeLocation().getSuper().getId() + " " + target.getId();
						return myWorld.params.hour_end_day_otherday - hour; // stay out until time to go home!
					}
			}
			}
			
//			private double oldDetermineDailyRoutine(Person p, int hour, int day) {
//				Location target;
//				target = myWorld.params.getTargetMoveDistrict(p, day, myWorld.random.nextDouble(), myWorld.lockedDown);
//				assert target.getId().startsWith("d_"): "target is a null location";
//				// define workday
//				boolean goToWork = (p.isSchoolGoer() || target == p.getCommunityLocation()) // schoolgoer or going to own district
//						&& myWorld.params.isWeekday(day);
//
//				if(myWorld.params.setting_perfectMixing) // in perfect mixing, just go to the community!
//					goToWork = false;
//				
//				p.transferTo(target);
//				assert (p.getLocation().equals(target)) : "Transfer to target didn't work";
//
//				// update appropriately
//				if(goToWork){// working
//					p.setActivityNode(workNode);
//					p.setAtWork(true);
//					p.setVisiting(false);
//					return myWorld.params.hours_at_work_weekday;
//				}
//				
//				else if(target == p.getCommunityLocation()) { // in home district, not working
//					p.setActivityNode(communityNode);
//					p.setAtWork(false);	
//					assert p.getHousehold().getSuper().getId().equals(target.getId()) : 
//						"set to travel to a within district but didn't, home/target " + p.getHousehold().getSuper().getId() + " " + target.getId();
//					return myWorld.params.hour_end_day_otherday - hour; // stay out until time to go home!
//				}
//				
//				else{ // travelling to another district!
//					p.setActivityNode(communityNode);
//					p.setAtWork(false);
//					p.setVisiting(true);
//					assert ! p.getHousehold().getSuper().equals(target) : 
//						"set to travel to a different district but didn't, home/target " + p.getHousehold().getSuper().getId() + " " + target.getId();
//					return myWorld.params.hour_end_day_otherday - hour; // stay out until time to go home!
//				}
//			}

			@Override
			public boolean isEndpoint() {
				return false;
			}		
		};
		
		workNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return mobilityNodeTitle.WORK.key; }

			@Override
			public double next(Steppable s, double time) {

				Person p = (Person) s;

				// extract time info
				int hour = ((int)time) % Params.ticks_per_day;
				
				// if it's too late, go straight home
				if(hour > myWorld.params.hour_end_day_weekday){
					p.transferTo(p.getHomeLocation());
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

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		communityNode = new BehaviourNode(){
						
			@Override
			public String getTitle() { return mobilityNodeTitle.COMMUNITY.key; }

			@Override
			public double next(Steppable s, double time) {
				
				Person p = (Person) s;

				// extract time info
				int hour = ((int)time) % Params.ticks_per_day;

				if(hour >= myWorld.params.hour_end_day_otherday) { // late! Go home!
					p.transferTo(p.getHomeLocation());
					p.setActivityNode(homeNode);
					p.setVisiting(false);
					assert p.getLocation().getId().equals(p.getHomeLocation().getId()) : "person isn't home but should be " + p.getLocation().getId();
					return myWorld.params.hours_sleeping;
				}
				return 1; // check in again soon, but we have more time!
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};

	}
	
	public BehaviourNode setMobilityNodeForTesting(mobilityNodeTitle behaviour) {
		BehaviourNode toreturn;

		switch (behaviour) {
		case HOME:{
			toreturn = homeNode;
			break;
		}
		case WORK:{
			toreturn = workNode;
			break;
		}
		case COMMUNITY:{
			toreturn = communityNode;
			break;
		}
		default:
			toreturn = homeNode;
			break;
		}
			
		return toreturn;
	}

	@Override
	public BehaviourNode getEntryPoint() {
		return this.homeNode;
	}

	@Override
	public BehaviourNode getHomeNode() {
		// TODO Auto-generated method stub
		return null;
	}
}
