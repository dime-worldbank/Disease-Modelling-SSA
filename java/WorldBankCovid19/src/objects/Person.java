package objects;

import sim.Params;
import sim.WorldBankCovid19Sim;
import sim.engine.SimState;
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
	int currentActivity = -1; // 0 is work, 1 is leisure
	
	// copy of world
	WorldBankCovid19Sim myWorld;
	
	//
	// Epidemic Attributes
	//
	
	// health
	int epidemic_state;
	double infection_rate; // TODO what??
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
		int myHour = (int)time % 24; // get the current hour
		int myDay = (int)(time / 24) % 7; // get the day of the week
		
		// TODO PROCESS DEATHS
		
		double delta = 1;
		
		// depending on day of week and time, possibly move
		if(myDay < 5){
			if(myHour == Params.hour_end_day_weekday)
				delta = goHome();
			else if(myHour == Params.hour_start_day_weekday)
				delta = goOut(myDay);
		}
		else {
			if(myHour == Params.hour_end_day_otherday)
				delta = goHome();
			else if (myHour == Params.hour_start_day_otherday)
				delta = goOut(myDay);
		}
		
		if(delta > 0) // delta will be negative if there is a problem and it should no longer run
			world.schedule.scheduleOnce(time + delta, this);
	}
	
	/**
	 * A function which moves the Person from wherever they are to their Household.
	 * @return the amount of time spent travelling to the Household location.
	 */
	double goHome(){
		
		// only move the Person if they are not already in the Household
		if(this.currentLocation != this.myHousehold){
			this.currentLocation = this.myHousehold;
		}
		
		return 1; // TODO make based on distance travelled!
	}
	
	/**
	 * Based on the Person's economic status, attempt to leave the Household. The destination selected will
	 * be drawn from their economic_activity_location.
	 * @param weekday The Person will pick different destinations based on the day of the week.
	 * @return
	 */
	double goOut(int weekday){
		
		// if the Person is not currently in their Household, throw up a warning TODO refine with more nuanced movement model
		if(this.currentLocation != this.myHousehold){
			System.out.println("WARNING: Person " + this.myId + " is not at home.");
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

	//
	// UTILITIES
	//
	
	public void setLocation(Location l){
		this.currentLocation = l;
	}
	

}