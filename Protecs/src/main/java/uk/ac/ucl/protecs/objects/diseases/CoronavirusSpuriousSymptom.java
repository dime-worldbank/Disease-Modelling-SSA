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
		this.myWorld.infections.add(this);
		this.host.addDisease(DISEASE.COVIDSPURIOUSSYMPTOM, this);


	}

	@Override
	public void step(SimState arg0) {
		double time = myWorld.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		arg0.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_infecting, this);
	}
	// =============================================== Disease 'behaviours'================================================================================
	public boolean isInfectious() {
		return false;
	}
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
		String rec = "";
		
		rec += "\t" + time_infected + "\t";
		
		// infected at:
		
		Location loc = infectedAtLocation;
		
		if(loc == null)
			rec += "SEEDED";
		else if(loc.getRootSuperLocation() != null)
			rec += loc.getRootSuperLocation().getId();
		else
			rec += loc.getId();
		
		// progress of disease: get rid of max vals
		
		if(time_contagious == Double.MAX_VALUE)
			rec += "\t-";
		else
			rec += "\t" + (int) time_contagious;
		
		if(time_start_symptomatic == Double.MAX_VALUE)
			rec += "\t-";
		else
			rec += "\t" + (int) time_start_symptomatic;
		
		if(time_start_severe == Double.MAX_VALUE)
			rec += "\t-";
		else
			rec += "\t" + (int) time_start_severe;
		
		if(time_start_critical == Double.MAX_VALUE)
			rec += "\t-";
		else
			rec += "\t" + (int) time_start_critical;
		
		if(time_recovered == Double.MAX_VALUE)
			rec += "\t-";
		else
			rec += "\t" + (int) time_recovered;
		
		if(time_died == Double.MAX_VALUE)
			rec += "\t-";
		else
			rec += "\t" + (int) time_died;
		// create variables to calculate DALYs, set to YLD zero as default
		double yld = 0.0;
		if(yld == 0.0)
			rec += "\t-";
		else
			rec += "\t" + (double) yld;
		// calculate YLL (basic)
		// YLL = Life expectancy in years - age at time of death, if age at death < Life expectancy else 0
		double yll = 0;
		// Recored DALYs (YLL + YLD)
		if (yll + yld == 0.0)
			rec += "\t-";
		else
			rec += "\t" + (double) (yll + yld);
		// record number of times with covid
		rec += "\t" + host.getNumberOfTimesInfected();
		
		rec += "\n";
		return rec;
		
	}

	// =============================================== Disease testing ====================================================================================
	
	
	@Override
	public boolean inATestingAdminZone() {
		String hostLocationId = this.getHost().myHousehold.getRootSuperLocation().myId;
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