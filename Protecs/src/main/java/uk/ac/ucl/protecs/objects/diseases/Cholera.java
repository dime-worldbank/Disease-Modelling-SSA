package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.hosts.Host;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.HOST;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class Cholera extends Disease{
	
	// Zimbabwe's 2018 outbreak was V. cholerae O1 serotype Ogawa
	
	// Whether this infection was asymptomatic or not seems to have consequences on reinfection later on. As such track whether this infection was asymptomatic
	// at any point
	boolean hadAsymptCholera = false;
	// Persistence studies show a reduced risk of subsequent cholera infection if this person previously had an infection
	boolean hadSymptCholera = false;
	// assign values to time-dependent protection from prior infections 
	public double time_protection_from_asymptomatic_ends = Double.MAX_VALUE;
	public double time_protection_from_symptomatic_ends = Double.MAX_VALUE;

	
	
	public Cholera(Host myHost, Host mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public Cholera(Host myHost, Host mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		
		source = mySource;
		
		host.addDisease(this);
					
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
	public void step(SimState world) {
		double time = world.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		world.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_infecting, this);
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
	
	public void setHadAsymptCholera(boolean hadAsympt) {
		this.hadAsymptCholera = hadAsympt;

	}
	
	public void setHadSymptCholera(boolean hadSympt) {
		this.hadSymptCholera = hadSympt;
	}
	
	public boolean getHadAsymptCholera() {
		return this.hadAsymptCholera;
	}
	
	public boolean getHadSymptCholera() {
		return this.hadSymptCholera;
	}

}