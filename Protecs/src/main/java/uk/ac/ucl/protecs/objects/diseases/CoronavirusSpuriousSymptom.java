package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class CoronavirusSpuriousSymptom extends Disease{
	// record keeping
	public double timeLastTriggered = Double.MAX_VALUE;
	
	public CoronavirusSpuriousSymptom(Person p, WorldBankCovid19Sim sim, BehaviourNode initNode, int time) {
		this.host = p;
		this.source = p;
		this.infectedAtLocation = p.getLocation();
		this.currentBehaviourNode = initNode;
		this.time_infected = time;
		this.myWorld = sim;
		this.myWorld.human_infections.add(this);
		this.host.addDisease(this);


	}

	@Override
	public void step(SimState arg0) {
		double time = myWorld.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		arg0.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_infecting, this);
	}
	// =============================================== Disease 'behaviours'================================================================================
	@Override
	public boolean isInfectious() {
		return false;
	}
	@Override
	public boolean isWaterborne() {return false;}

	// =============================================== Disease type classification ===========================================================================
	@Override
	public String getDiseaseName() {
		// TODO Auto-generated method stub
		return "COVID-19_SPURIOUS_SYMPTOM";
	}
	
	@Override
	public DISEASE getDiseaseType() {
		// TODO Auto-generated method stub
		return DISEASE.COVIDSPURIOUSSYMPTOM;
	}
	
	@Override
	public boolean isOfType(DISEASE disease) {
		// TODO Auto-generated method stub
		return this.getDiseaseType().equals(disease);
	}
	
	// =============================================== Disease progression ====================================================================================
	@Override
	public boolean hasAsympt() {
		// TODO Auto-generated method stub
		return false;
	}

	// =============================================== Disease logging ====================================================================================
	@Override
	public String writeOut() {
		
		return null;
		
	}

	// =============================================== Disease testing ====================================================================================
	
	
	@Override
	public boolean inATestingAdminZone() {
		String hostLocationId = ((Person) this.getHost()).getHomeLocation().getRootSuperLocation().myId;
		boolean answer = this.getHost().myWorld.params.admin_zones_to_test_in.contains(hostLocationId);
		return answer;
	}

	@Override
	public void horizontalTransmission() {
		// NA
		
	}

	@Override
	public void verticalTransmission(Person baby) {
		// NA
		
	}
}