package objects;

import sim.Params;
import sim.WorldBankCovid19Sim;
import sim.engine.SimState;
import behaviours.BehaviourNode;
import behaviours.MovementBehaviourFramework;
import behaviours.MovementBehaviourFramework.Activity;

import swise.agents.MobileAgent;


public class Person extends MobileAgent {
	
	//
	// Personal Attributes
	//
	
	// personal ID to distinguish from other agents
	int myId;

	// larger group membership
	Household myHousehold;

	// personal/demographic attributes
	int age;
	String sex;

	// economic attributes
	String economic_status;
	Location economic_activity_location; // treating districts as ints
	
	// locational attributes
	Location currentLocation;
	boolean district_mover; // allowed to move between districts?
	
	// activity
	Activity currentActivity = Activity.HOME; // 0 is work, 1 is leisure
	BehaviourNode currentActivityNode = null;
	
	// copy of world
	WorldBankCovid19Sim myWorld;
	
	//
	// Epidemic Attributes
	//
	
	// health
	int epidemic_state;
	double infection_rate;
	int infected_symptomatic_status;
	int clinical_state;
	
	double severe_disease_risk;
	
	// infection timekeeping
	double time_infected;
	double time_start_contagious;
	double time_start_symptomatic;
	double time_recovered;
	
	Location infectedAtLocation;
	
	// clinical care
	double time_start_hospitalised;
	double time_end_hospitalised;
	double time_start_critical;
	double time_end_critical;
	double time_died;
	
	/**
	 * Constructor for Person object.
	 * 
	 * Applies all given demographic data and sets up the Person's health properties.
	 * 
	 * @param id Unique identifier for the Person
	 * @param age Age in years
	 * @param sex Assigned sex as String
	 * @param economic_status Linked to economic mobility files in Params
	 * @param economic_activity_location Location for weekday economic activity (workplace, school, etc.)
	 * @param world Copy of the simulation
	 */
	public Person(int id, int age, String sex, String economic_status, Location economic_activity_location, WorldBankCovid19Sim world){
		super();

		// demographic characteristics
		
		this.myId = id;
		this.age = age;
		this.sex = sex;
		
		// economic characteristics
		this.economic_status = economic_status;
		this.economic_activity_location = economic_activity_location;

		// record-keeping
		myWorld = world;
		
		// other characteristics (possibly weighted)
		severe_disease_risk = 1;

		// agents are initialised uninfected
		
		epidemic_state = Params.state_susceptible;
		infected_symptomatic_status = Params.symptom_none;
		clinical_state = Params.clinical_not_hospitalized;
		
		time_infected = Double.MAX_VALUE;
		time_died = Double.MAX_VALUE;
		
		infectedAtLocation = null;
	
	}
	
	//
	// BEHAVIOURS
	//
	
	@Override
	public void step(SimState world) {
		double time = world.schedule.getTime(); // find the current time
//		double myDelta = myWorld.movementFramework.update(this);
		double myDelta = this.currentActivityNode.next(this, time);
		myWorld.schedule.scheduleOnce(time + myDelta, this);
	}	
/*		// get time information		
		double time = world.schedule.getTime(); // find the current time
		int myHour = (int)time % Params.ticks_per_day; // get the current hour
		int myDay = (int)(time / Params.ticks_per_day) % 7; // get the day of the week
		
		double delta = 1;
		
		// DECISION POINT 1: check if the person is severely ill
		// P1 - YES ill
		// Take no action
		if(this.infected_symptomatic_status >= Params.symptom_symptomatic){ // TODO confirm this framing
			world.schedule.scheduleOnce(time + 1, this); // if they are ill, no movement!
			return;
		}
		
		// P1 - NOT ill

		// Go outside!
		
		// DECISION POINT 2: 
		// depending on day of week and time, possibly move. Decide where!
		// DECISION POINT 3: check where to move - within or outside the district
		Location targetLocation = myWorld.params.getTargetMoveDistrict(this, myDay, myWorld.random.nextDouble());
				
		if(Params.isWeekday(myDay)){
			if(myHour >= Params.hour_end_day_weekday)
				delta = goHome();
			else if(myHour == Params.hour_start_day_weekday)
				delta = goOut(myDay);
			else
				delta = goLeisure(targetLocation, Params.time_leisure_weekday);
		}
		else {
			if(myHour >= Params.hour_end_day_otherday)
				delta = goHome();
			else if (myHour == Params.hour_start_day_otherday)
				delta = goOut(myDay);
			else
				delta = goLeisure(targetLocation, Params.time_leisure_weekend);
		}
		
		if(delta > 0) // delta will be negative if there is a problem and it should no longer run
			world.schedule.scheduleOnce(time + delta, this);
	}
	*/
	
	/**
	 * A function which moves the Person from wherever they are to their Household.
	 * @return the amount of time spent travelling to the Household location.
	 */
	public double goHome(){
		
		// only move the Person if they are not already in the Household
		if(this.currentLocation != this.myHousehold){
			this.currentLocation = this.myHousehold;
		}
		
		return 1; // TODO make based on distance travelled!
	}
	
	public double goToWork(){
		if(economic_activity_location != null)
			currentLocation = economic_activity_location;
		return 1;
	}
	
	public double goToCommunity(){
		currentLocation.removePerson(this);
		Location l = myHousehold.getRootSuperLocation();
		currentLocation = l;
		l.addPerson(this);
		return 0;
	}
	
	/**
	 * Based on the Person's economic status, attempt to leave the Household. The destination selected will
	 * be drawn from their economic_activity_location.
	 * @param weekday The Person will pick different destinations based on the day of the week.
	 * @return
	 */
/*	double goOut(int weekday){
	
		
		// First, check that the Person is currently in their Household
		// TODO refine with more nuanced movement model
		if(this.currentLocation != this.myHousehold){
			System.out.println("WARNING: Person " + this.myId + " is not at home.");
		}
		

		
		if(Params.isWeekday(weekday)){
			
		}
		
		// if the Person has a workplace and is not there, consider going!
		if(this.economic_activity_location != null && this.currentLocation != this.economic_activity_location){
			
			// extract the appropriate economic status mobility data given the day
			double myEconStatProb = myWorld.params.getEconProbByDay(weekday, economic_status);
			
			// make sure that the the Parameters has a valid record for movement for this economic_status type
			if(myEconStatProb < 0){
				System.out.println("WARNING: no recorded movement probability for economic_status type \"" + 
						this.economic_status + "\". Person " + myId + " will never move.");
				return -1;
			}

			// TODO add symptomatic aspects (when appropriate)

			// randomly determine whether the Person is going to work today
			double activityProb = myWorld.random.nextDouble();
			if(activityProb < myEconStatProb){
				//System.out.print(myId + " IS GOING OUT\t");
				this.currentLocation = this.economic_activity_location;
				
			}			
		}
		return 1; // TODO base this on distance travelled!!
	}
*/
	//
	// UTILITIES
	//
	
	public void setLocation(Location l){
		this.currentLocation = l;
	}
	
	public Location getLocation(){
		return currentLocation;
	}
	
	public Activity getActivity(){
		return currentActivity;
	}
	
	public void setActivity(Activity a){
		currentActivity = a;
	}
	
	public boolean isHome(){
		return currentLocation == myHousehold;
	}

	public String toString(){
		return "P_" + this.myId;
	}
	
	public void setActivityNode(BehaviourNode bn){
		currentActivityNode = bn;
	}
}