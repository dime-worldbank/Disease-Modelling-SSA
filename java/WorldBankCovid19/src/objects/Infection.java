package objects;

import behaviours.BehaviourNode;
import sim.WorldBankCovid19Sim;
import sim.engine.SimState;
import sim.engine.Steppable;

public class Infection implements Steppable {

	// record keeping
	Person host;
	Person source;
	Location infectedAtLocation;
	WorldBankCovid19Sim myWorld;
	
	// behaviours
	BehaviourNode currentBehaviourNode = null;
	
	double infection_rate;
	int infected_symptomatic_status;

	// infection timekeeping
	// default these to -1 so it's clear when they've been reset
	public double time_infected = Double.MAX_VALUE;
	public double time_contagious = Double.MAX_VALUE;
	public double time_start_symptomatic = Double.MAX_VALUE;
	public double time_start_severe = Double.MAX_VALUE;
	public double time_start_critical = Double.MAX_VALUE;
	public double time_recovered = 	Double.MAX_VALUE;
	public double time_died = Double.MAX_VALUE;
	
	// clinical care
	double time_start_hospitalised;
	double time_end_hospitalised;
	
	/**
	 * 
	 * @param myHost
	 * @param mySource - null implies that they are Patient 0.
	 * @param initNode
	 */
	public Infection(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		
		host = myHost;
		myHost.setInfection(this);
		
		source = mySource;
		
		//	epidemic_state = Params.state_susceptible;
		//	infected_symptomatic_status = Params.symptom_none;
		//	clinical_state = Params.clinical_not_hospitalized;
			
		// store the time when it is infected!
		time_infected = host.myWorld.schedule.getTime();		
		infectedAtLocation = host.currentLocation;
		
		time_died = Double.MAX_VALUE;

		currentBehaviourNode = initNode;
		myWorld = sim;
		myWorld.infections.add(this);
	}

	@Override
	public void step(SimState world) {
		double time = world.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		world.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_infecting, this);
		
	//	System.out.println("Infection\t" + host.toString() + "\t" + currentBehaviourNode.getTitle() + "\t" + myDelta);
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
	public Location getInfectedAtLocation() { return infectedAtLocation;}
	
}