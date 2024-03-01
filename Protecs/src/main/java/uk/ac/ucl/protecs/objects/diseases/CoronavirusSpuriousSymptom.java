package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class CoronavirusSpuriousSymptom implements Infection{
	// record keeping
	Person host;
	Person source;
	Location infectedAtLocation;
	WorldBankCovid19Sim myWorld;
	double timeCreated = Double.MAX_VALUE;
	double timeRecovered = Double.MAX_VALUE;
	
	// behaviours
	BehaviourNode currentBehaviourNode = null;
	
	public CoronavirusSpuriousSymptom(Person p, BehaviourNode initNode, WorldBankCovid19Sim sim) {
		this.host = p;
		this.source = p;
		this.infectedAtLocation = p.getLocation();
		this.timeCreated = sim.schedule.getTime();
	}

	@Override
	public void step(SimState arg0) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return null;
	}
	
}