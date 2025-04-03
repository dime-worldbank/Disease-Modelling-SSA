package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class DummyWaterbornDisease extends Disease{
	
	public DummyWaterbornDisease(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public DummyWaterbornDisease(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		
		source = mySource;
		
		host.addDisease(DISEASE.DUMMY_WATERBORN, this);
		
		this.hasAsympt = true;
			
		// store the time when it is infected!
		time_infected = time;		
		infectedAtLocation = myHost.getLocation();
		currentBehaviourNode = initNode;
		myWorld = sim;
		myWorld.infections.add(this);
	}

	@Override
	public void step(SimState arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInfectious() {
		return true;
	}
	@Override
	public boolean isWaterborn() {
		return true;
	}
	@Override
	public void horizontalTransmission() {
		// if this infection's host is dead, do not try and interact
		if (!host.isAlive()) return;
		// if not currently in the space, do not try to interact
		else if(host.getLocation() == null) return;
		// check if they are at a source of water (very simple I know but this is a first iteration)
		if (host.getLocation().isWaterSource()) {
			double probShedIntoWater = myWorld.random.nextDouble();
			if (probShedIntoWater < myWorld.params.dummy_waterborn_prob_shed_into_water) {
				
			}

		}
		
		
	}

	@Override
	public void verticalTransmission(Person baby) {
		// NA
		
	}

	@Override
	public boolean isOfType(DISEASE disease) {
		return this.getDiseaseType().equals(disease);
	}

	@Override
	public DISEASE getDiseaseType() {
		// TODO Auto-generated method stub
		return DISEASE.DUMMY_WATERBORN;
	}

	@Override
	public String getDiseaseName() {
		// TODO Auto-generated method stub
		return DISEASE.DUMMY_WATERBORN.key;
	}

	@Override
	public String writeOut() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean inATestingAdminZone() {
		return false;
	}
}