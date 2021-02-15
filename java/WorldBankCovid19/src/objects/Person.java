package objects;

import sim.Params;
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
	
	public Person(int id, int age, String sex, String economic_status, Location economic_activity_location){
		super();

		// demographic characteristics
		
		this.myId = id;
		this.age = age;
		this.sex = sex;
		
		// economic characteristics
		this.economic_status = economic_status;
		this.economic_activity_location = economic_activity_location;
		
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
	
	void goHome(){
		if(this.currentLocation != this.myHousehold){
			System.out.println(myId + " IS GOING HOME");
			this.currentLocation = this.myHousehold;
		}
	}
	
	void goOut(){
		
		if(this.economic_activity_location != null && this.currentLocation != this.economic_activity_location){
			System.out.println(myId + " IS GOING OUT");
			this.currentLocation = this.economic_activity_location;
		}
	}
	
	@Override
	public void step(SimState arg0) {
		
		double time = arg0.schedule.getTime(); // find the current time
		int myHour = (int)time % 24; // get the current hour
		
		// TODO PROCESS DEATHS
		
		if(myHour == Params.hour_end_day_weekday || myHour == Params.hour_end_day_otherday)
			goHome();
		
		else if(myHour == Params.hour_start_day_weekday || myHour == Params.hour_start_day_otherday)
			goOut();
	}
	
	public void setLocation(Location l){
		this.currentLocation = l;
	}
}