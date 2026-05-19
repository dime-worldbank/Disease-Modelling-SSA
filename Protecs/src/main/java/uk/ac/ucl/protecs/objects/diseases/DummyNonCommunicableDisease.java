package uk.ac.ucl.protecs.objects.diseases;

import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import sim.engine.SimState;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

/**
 * The object holds records of individual instances of a 'dummy' disease. It works together with the
 * DummyBehaviourFramework, saving the information about specific instances while
 * using the Framework to drive the characteristic progression.
 *
 */

public class DummyNonCommunicableDisease extends Disease {

	public DummyNonCommunicableDisease(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public DummyNonCommunicableDisease(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		
		source = mySource;
		
		host.addDisease(this);
			
		// store the time when it is infected!
		time_infected = time;		
		infectedAtLocation = myHost.getLocation();
		currentBehaviourNode = initNode;
		myWorld = sim;
		myWorld.human_infections.add(this);
	}

	
	@Override
	public void step(SimState world) {
		double time = world.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		world.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_infecting, this);
	}
	// =============================================== Disease type classification ===========================================================================	
	@Override
	public String getDiseaseName() {
		
		return DISEASE.DUMMY_NCD.key;
	}
	
	@Override
	public DISEASE getDiseaseType() {
		// TODO Auto-generated method stub
		return DISEASE.DUMMY_NCD;
	}
	
	@Override
	public boolean isOfType(DISEASE disease) {
		// TODO Auto-generated method stub
		return this.getDiseaseType().equals(disease);
	}
	
	// =============================================== Disease logging ====================================================================================
	
	@Override
	public String writeOut() {
		return null;
	}
	
	// =============================================== Disease testing ====================================================================================
	
	@Override
	public boolean inATestingAdminZone() {
		return false;
	}

	@Override
	public boolean isInfectious() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isWaterborne() {return false;}

	@Override
	public void horizontalTransmission() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verticalTransmission(Person baby) {
		// TODO Auto-generated method stub
		
	}
}
