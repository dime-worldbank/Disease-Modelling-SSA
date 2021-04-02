package objects;

import behaviours.BehaviourNode;
import sim.engine.SimState;
import sim.engine.Steppable;

public class Infection implements Steppable {
	
	Person host;
	Person source;
	BehaviourNode currentBehaviourNode = null;
	
	double severity; // 0-100 degree to which person is being impacted by disease and can't move/work/etc.
	
	int epidemic_state;
	double infection_rate;
	int infected_symptomatic_status;

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
	 * 
	 * @param myHost
	 * @param mySource - null implies that they are Patient 0.
	 * @param initNode
	 */
	public Infection(Person myHost, Person mySource, BehaviourNode initNode){
		
		host = myHost;
		myHost.setInfection(this);
		
		severity = host.myWorld.random.nextDouble() * 100;
		
		source = mySource;
		
		//	epidemic_state = Params.state_susceptible;
		//	infected_symptomatic_status = Params.symptom_none;
		//	clinical_state = Params.clinical_not_hospitalized;
			
		// store the time when it is infected!
		time_infected = host.myWorld.schedule.getTime();		
		infectedAtLocation = host.currentLocation;
		
		time_died = Double.MAX_VALUE;

		currentBehaviourNode = initNode;
	}

	@Override
	public void step(SimState world) {
		double time = world.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		world.schedule.scheduleOnce(time + myDelta, this);
	}

	public void updateSeverity(){
		// TODO age factor
		double impactFactor = 1 + (.5 - host.myWorld.random.nextDouble())/ 100.;
		severity *= impactFactor;
	}
	
	public void setBehaviourNode(BehaviourNode bn){
		this.currentBehaviourNode = bn;
	}
	
	public String getBehaviourName(){
		if(this.currentBehaviourNode == null) return "";
		return this.currentBehaviourNode.getTitle();
	}
	
	public Person getHost() { return host; }
	public Person getSource() { return source; }
	public double getStartTime() { return time_infected;}
	
	public double getSeverity(){ return severity;}
}