package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class CoronavirusSpuriousSymptom implements Infection{
	// record keeping
	Person host;
	Person source;
	Location infectedAtLocation;
	WorldBankCovid19Sim myWorld;
	public double timeCreated = Double.MAX_VALUE;
	public double timeRecovered = Double.MAX_VALUE;
	public double timeLastTriggered = Double.MAX_VALUE;
	// default these to max value so it's clear when they've been reset
	public double time_infected = Double.MAX_VALUE;
	public double time_contagious = Double.MAX_VALUE;
	public double time_start_symptomatic = Double.MAX_VALUE;
	public double time_start_severe = Double.MAX_VALUE;
	public double time_start_critical = Double.MAX_VALUE;
	public double time_recovered = 	Double.MAX_VALUE;
	public double time_died = Double.MAX_VALUE;
	
	// track whether a person is displaying symptoms or not
	public boolean symptomatic = false;
	boolean isTheCauseOfDeath = false;
	public boolean tested = false;
	public boolean eligibleForTesting = false;
	public boolean testedPositive = false;
	public boolean testLogged = false;
	// behaviours
	BehaviourNode currentBehaviourNode = null;
	
	public CoronavirusSpuriousSymptom(Person p, WorldBankCovid19Sim sim, BehaviourNode initNode, int time) {
		this.host = p;
		this.source = p;
		this.infectedAtLocation = p.getLocation();
		this.currentBehaviourNode = initNode;
		this.timeCreated = time;
		this.myWorld = sim;
		this.myWorld.infections.add(this);
		this.host.addInfection(DISEASE.COVIDSPURIOUSSYMPTOM, this);


	}

	@Override
	public void step(SimState arg0) {
		double time = myWorld.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		arg0.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_infecting, this);
	}
	// =============================================== relevant information on the host ==============================================================
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
	public String getCurrentAdminZone() {
		// TODO Auto-generated method stub
		return this.host.getHousehold().getRootSuperLocation().myId;
	}
	
	@Override
	public boolean isHostAlive() {
		// TODO Auto-generated method stub
		return this.getHost().isAlive();
	}
	
	@Override
	public int getHostAge() {
		// TODO Auto-generated method stub
		return this.host.getAge();
	}
	
	@Override
	public SEX getHostSex() {
		// TODO Auto-generated method stub
		return this.host.getSex();
	}
	
	@Override
	public OCCUPATION getHostEconStatus() {
		// TODO Auto-generated method stub
		return this.host.getEconStatus();
	}

	// =============================================== Disease 'behaviours'================================================================================
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


	// =============================================== Disease type classification ===========================================================================
	@Override
	public boolean isCovid() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isCovidSpuriousSymptom() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isDummyInfection() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String getDiseaseName() {
		// TODO Auto-generated method stub
		return "COVID-19_SPURIOUS_SYMPTOM";
	}
	
	// =============================================== Disease progression ====================================================================================
	@Override
	public void setAsympt() {
		// TODO Auto-generated method stub
		this.symptomatic = false;
	}
	
	@Override
	public boolean hasAsympt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSymptomatic() {
		// TODO Auto-generated method stub
		this.symptomatic = true;
	}

	@Override
	public boolean isSymptomatic() {
		// TODO Auto-generated method stub
		return this.symptomatic;
	}
	
	@Override
	public void setMild() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean hasMild() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setSevere() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean hasSevere() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setCritical() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean hasCritical() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setRecovered() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean hasRecovered() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void setAsCauseOfDeath() {};
	
	public boolean isCauseOfDeath() {
		return this.isTheCauseOfDeath;
	};

	// =============================================== Disease logging ====================================================================================
	@Override
	public boolean getAsymptLogged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void confirmAsymptLogged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getMildLogged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void confirmMildLogged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getSevereLogged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void confirmSevereLogged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getCriticalLogged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void confirmCriticalLogged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getDeathLogged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void confirmDeathLogged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getLogged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void confirmLogged() {
		// TODO Auto-generated method stub
		
	}

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
	// filtering and setting who should be tested
	@Override
	public boolean isEligibleForTesting() {
		// TODO Auto-generated method stub
		return this.eligibleForTesting;
	}

	@Override
	public void setEligibleForTesting() {
		// TODO Auto-generated method stub
		this.eligibleForTesting = true;
	}

	@Override
	public void removeEligibilityForTesting() {
		// TODO Auto-generated method stub
		this.eligibleForTesting = false;
	}

	@Override
	public boolean hasBeenTested() {
		// TODO Auto-generated method stub
		return this.tested;
	}

	@Override
	public void setTested() {
		this.tested = !this.tested;
	}

	@Override
	public void setTestedPositive() {
		// TODO Auto-generated method stub
		this.testedPositive = true;
	}

	@Override
	public boolean hasTestedPositive() {
		// TODO Auto-generated method stub
		return this.testedPositive;
	}
	
	@Override
	public boolean inATestingAdminZone() {
		String hostLocationId = this.getHost().myHousehold.getRootSuperLocation().myId;
		boolean answer = this.getHost().myWorld.params.admin_zones_to_test_in.contains(hostLocationId);
		return answer;
	}

	@Override
	public boolean getTestLogged() {
		// TODO Auto-generated method stub
		return this.testLogged;
	}

	@Override
	public void confirmTestLogged() {
		// TODO Auto-generated method stub
		this.testLogged = true;
	}
}