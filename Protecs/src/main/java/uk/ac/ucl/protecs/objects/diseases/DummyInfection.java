package uk.ac.ucl.protecs.objects.diseases;

import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.Person.SEX;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import sim.engine.SimState;
import swise.behaviours.BehaviourNode;

/**
 * The object holds records of individual instances of a 'dummy' disease. It works together with the
 * DummyBehaviourFramework, saving the information about specific instances while
 * using the Framework to drive the characteristic progression.
 *
 */

public class DummyInfection implements Infection {

	// record keeping
	Person host;
	Person source;
	Location infectedAtLocation;
	WorldBankCovid19Sim myWorld;
	
	// behaviours
	BehaviourNode currentBehaviourNode = null;
	
	double infection_rate;
	int infected_symptomatic_status;

	// infection timekeeping
	// default these to max value so it's clear when they've been reset
	public double time_infected = Double.MAX_VALUE;
	public double time_contagious = Double.MAX_VALUE;
	public double time_start_symptomatic = Double.MAX_VALUE;
	public double time_start_severe = Double.MAX_VALUE;
	public double time_start_critical = Double.MAX_VALUE;
	public double time_recovered = 	Double.MAX_VALUE;
	public double time_died = Double.MAX_VALUE;
	
	public boolean tested = false;
	
	/**
	 * 
	 * @param myHost
	 * @param mySource - null implies that they are Patient 0.
	 * @param initNode
	 */
	public DummyInfection(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public DummyInfection(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		
		source = mySource;
		
		host.addInfection(DISEASE.DUMMY, this);
			
		// store the time when it is infected!
		time_infected = time;		
		infectedAtLocation = myHost.getLocation();
		currentBehaviourNode = initNode;
		myWorld = sim;
		myWorld.infections.add(this);
	}

	
	@Override
	public void step(SimState world) {
		double time = world.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		world.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_infecting, this);
	}

	public void setBehaviourNode(BehaviourNode bn){
		this.currentBehaviourNode = bn;
	}
	
	public String getBehaviourName(){
		if(this.currentBehaviourNode == null) return "";
		return this.currentBehaviourNode.getTitle();
	}
	
	
	public Person getHost() { return host; }
	public Person getSource() { return source; }
	public double getStartTime() { return time_infected;}
	public Location getInfectedAtLocation() { return infectedAtLocation;}

	
	public Location infectedAt() { return infectedAtLocation; }
	public BehaviourNode getCurrentBehaviourNode() { return currentBehaviourNode;}

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

	@Override
	public String getDiseaseName() {
		
		return "DUMMY";
	}

	@Override
	public String getCurrentAdminZone() {
		// TODO Auto-generated method stub
		return this.host.getHousehold().getRootSuperLocation().myId;
	}

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return this.getHost().isAlive();
	}

	@Override
	public boolean covidLogCheck() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCovid() {
		return false;
	}

	@Override
	public boolean hasRecovered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasAsympt() {
		// TODO Auto-generated method stub
		return false;
	}

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
	public boolean hasMild() {
		// TODO Auto-generated method stub
		return false;
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
	public boolean hasSevere() {
		// TODO Auto-generated method stub
		return false;
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
	public boolean hasCritical() {
		// TODO Auto-generated method stub
		return false;
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
	public void setAsympt() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getAge() {
		// TODO Auto-generated method stub
		return this.host.getAge();
	}
	@Override
	public SEX getSex() {
		// TODO Auto-generated method stub
		return this.host.getSex();
	}
	@Override
	public OCCUPATION getEconStatus() {
		// TODO Auto-generated method stub
		return this.host.getEconStatus();
	}

	@Override
	public boolean getLogged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLogged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMild() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCovidSpuriousSymptom() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSymptomatic() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSymptomatic() {
		// TODO Auto-generated method stub
		return false;
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
	public boolean isEligibleForTesting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEligibleForTesting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeEligibilityForTesting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTestedPositive() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasTestedPositive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getTestLogged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void confirmTestLogged() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean inATestingAdminZone() {
		String hostLocationId = this.getHost().myHousehold.getRootSuperLocation().myId;
		boolean answer = this.getHost().myWorld.params.admin_zones_to_test_in.contains(hostLocationId);
		return answer;
	}
}
