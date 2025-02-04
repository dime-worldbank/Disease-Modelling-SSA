package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import swise.behaviours.BehaviourNode;

public class CoronavirusSpuriousSymptom implements Infection{
	// record keeping
	Person host;
	Person source;
	Location infectedAtLocation;
	WorldBankCovid19Sim myWorld;
	double timeCreated = Double.MAX_VALUE;
	double timeRecovered = Double.MAX_VALUE;
	double timeLastTriggered = Double.MAX_VALUE;
	
	
	// behaviours
	BehaviourNode currentBehaviourNode = null;
	
	public CoronavirusSpuriousSymptom(Person p, WorldBankCovid19Sim sim, BehaviourNode initNode, int time) {
		this.host = p;
		this.source = p;
		this.infectedAtLocation = p.getLocation();
		this.currentBehaviourNode = initNode;
		this.timeCreated = time;
		this.myWorld = sim;
		this.myWorld.CovidSpuriousSymptomsList.add(this);
		this.host.addInfection(DISEASE.COVIDSPURIOUSSYMPTOM, this);


	}

	@Override
	public void step(SimState arg0) {
		double time = myWorld.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		arg0.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_infecting, this);
	}

	@Override
	public Person getHost() {
		// TODO Auto-generated method stub
		return this.host;
	}

	@Override
	public Person getSource() {
		// TODO Auto-generated method stub
		return this.source;
	}

	@Override
	public Location infectedAt() {
		// TODO Auto-generated method stub
		return this.infectedAtLocation;
	}

	@Override
	public double getStartTime() {
		// TODO Auto-generated method stub
		return this.timeCreated;
	}

	@Override
	public BehaviourNode getCurrentBehaviourNode() {
		// TODO Auto-generated method stub
		return this.currentBehaviourNode;
	}

	@Override
	public void setBehaviourNode(BehaviourNode bn) {
		// TODO Auto-generated method stub
		this.currentBehaviourNode = bn;
	}

	@Override
	public String getBehaviourName() {
		// TODO Auto-generated method stub
		if(this.currentBehaviourNode == null) return "";
		return this.currentBehaviourNode.getTitle();
	}

	@Override
	public String writeOut() {
		return "P_" + String.valueOf(this.host.getID()) + " Inf at: " + String.valueOf(this.infectedAtLocation) + " created at: " + String.valueOf(this.timeCreated) + " Doing: " + String.valueOf(this.currentBehaviourNode.getTitle());
	}

}