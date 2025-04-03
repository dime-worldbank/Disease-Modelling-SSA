package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.hosts.Host;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class DummyWaterbornDisease extends Disease{
	
	public DummyWaterbornDisease(Host myHost, Host mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public DummyWaterbornDisease(Host myHost, Host mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
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