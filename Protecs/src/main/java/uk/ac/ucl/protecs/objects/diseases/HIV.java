package uk.ac.ucl.protecs.objects.diseases;

import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

import sim.engine.SimState;
import uk.ac.ucl.swise.behaviours.BehaviourNode;
public class HIV extends Disease {

	public HIV(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public HIV(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		myHost.addDisease(this);
		source = mySource;
		
		//	epidemic_state = Params.state_susceptible;
		//	infected_symptomatic_status = Params.symptom_none;
		//	clinical_state = Params.clinical_not_hospitalized;
			
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
		double myProb = myWorld.random.nextDouble();
		if (!baby.getDiseaseSet().containsKey(DISEASE.HIV.key) && myProb < myWorld.hivFramework.getHIV_vertical_transmission()) {
			HIV inf = new HIV(baby, ((Person) this.getHost()), myWorld.hivFramework.getEntryPoint(), myWorld);
			myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting); 
		}
		
	}

	@Override
	public boolean isOfType(DISEASE disease) {
		// TODO Auto-generated method stub
		return this.getDiseaseType().equals(disease);
	}

	@Override
	public DISEASE getDiseaseType() {
		return DISEASE.HIV;
	}

	@Override
	public String getDiseaseName() {
		return DISEASE.HIV.key;
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