package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class MeaslesInfection extends Disease {
	
	public MeaslesInfection(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public MeaslesInfection(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		myHost.addDisease(this);
		source = mySource;
			
		// store the time when it is infected!
		time_infected = time;		
		infectedAtLocation = myHost.getLocation();
		
		time_died = Double.MAX_VALUE;
		currentBehaviourNode = initNode;
		myWorld = sim;
		myWorld.human_infections.add(this);
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
	public boolean isWaterborne() {
		
		return false;
	}

	@Override
	public void horizontalTransmission() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verticalTransmission(Person baby) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOfType(DISEASE disease) {

		return this.getDiseaseType().equals(disease);
	}

	@Override
	public DISEASE getDiseaseType() {
		
		return DISEASE.MEASLES;
	}

	@Override
	public String getDiseaseName() {
		// TODO Auto-generated method stub
		return "MEASLES";
	}

	@Override
	public String writeOut() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean inATestingAdminZone() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
