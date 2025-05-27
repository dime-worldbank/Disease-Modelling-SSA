package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.hosts.Host;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.HOST;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class Cholera extends Disease{
	
	public Cholera(Host myHost, Host mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public Cholera(Host myHost, Host mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		
		source = mySource;
		
		host.addDisease(this);
		
		this.hasAsympt = true;
			
		// store the time when it is infected!
		time_infected = time;		
		infectedAtLocation = myHost.getLocation();
		currentBehaviourNode = initNode;
		myWorld = sim;
		if (myHost.isOfType(HOST.PERSON)){
		myWorld.human_infections.add(this);
		}
	}

	@Override
	public void step(SimState arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInfectious() {

		return false;
	}

	@Override
	public boolean isWaterborne() {

		return true;
	}

	@Override
	public void horizontalTransmission() {
		// N/A
		
	}

	@Override
	public void verticalTransmission(Person baby) {
		// N/A
		
	}

	@Override
	public boolean isOfType(DISEASE disease) {
		return this.getDiseaseType().equals(disease);
	}

	@Override
	public DISEASE getDiseaseType() {

		return DISEASE.CHOLERA;
	}

	@Override
	public String getDiseaseName() {

		return DISEASE.CHOLERA.key;
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