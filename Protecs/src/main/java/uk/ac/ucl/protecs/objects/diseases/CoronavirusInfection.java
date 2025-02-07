package uk.ac.ucl.protecs.objects.diseases;

import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.Person.SEX;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusBehaviourFramework.CoronavirusBehaviourNodeTitle;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import sim.engine.SimState;
import swise.behaviours.BehaviourNode;

/**
 * The object holds records of individual instances of disease. It works together with the
 * CoronavirusBehaviourFramework, saving the information about specific instances while
 * using the Framework to drive the characteristic progression.
 *
 */

public class CoronavirusInfection implements Infection {

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
	
	// clinical care
	double time_start_hospitalised;
	double time_end_hospitalised;
	
	// infection stages
	boolean isSymptomatic = false;
	boolean hasAsympt = false;
	boolean hasMild = false;
	
	// loggers
	boolean hasDeathLogged = false;
	boolean hasCovidLogged = false;
	boolean hasAsymptLogged = false;
	boolean hasMildLogged = false;
	boolean hasSevereLogged = false;
	boolean hasCriticalLogged = false;
	boolean hasLogged = false;
	// counter
	
	/**
	 * 
	 * @param myHost
	 * @param mySource - null implies that they are Patient 0.
	 * @param initNode
	 */
	public CoronavirusInfection(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public CoronavirusInfection(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		myHost.addInfection(DISEASE.COVID, this);
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
		// DALY weights are taken from https://www.ssph-journal.org/articles/10.3389/ijph.2022.1604699/full , exact same DALY weights used 
		// here https://www.ncbi.nlm.nih.gov/pmc/articles/PMC8212397/ and here https://www.ncbi.nlm.nih.gov/pmc/articles/PMC8844028/ , seems like these are common
		// TODO: check if these would be representative internationally
		// TODO: Find DALYs from long COVID
		double critical_daly_weight = 0.655;
		double severe_daly_weight = 0.133;
		double mild_daly_weight = 0.051;

		// calculate DALYs part 1: YLD working from the most serious level of infection
		// YLD = fraction of year with condition * DALY weight
		if (time_start_critical < Double.MAX_VALUE)
			// calculate yld between the onset of critical illness to death or recovery
			if (time_died < Double.MAX_VALUE)
				yld += ((time_died - time_start_critical) / 365) * critical_daly_weight;
			else if (time_recovered < Double.MAX_VALUE)
				yld += ((time_recovered - time_start_critical) / 365) * critical_daly_weight;
		if (time_start_severe < Double.MAX_VALUE)
			// calculate yld between the progression from a severe case to a critical case or recovery
			if (time_start_critical < Double.MAX_VALUE)
				yld += ((time_start_critical - time_start_severe) / 365) * severe_daly_weight;
			else if (time_recovered < Double.MAX_VALUE)
				yld += ((time_recovered - time_start_severe) / 365) * severe_daly_weight;
		if (time_start_symptomatic < Double.MAX_VALUE)
			// calculate yld between the onset of symptoms to progression to severe case or recovery
			if (time_start_severe < Double.MAX_VALUE)
				yld += ((time_start_severe - time_start_symptomatic) / 365) * mild_daly_weight;
			else if (time_recovered < Double.MAX_VALUE)
				yld += ((time_recovered - time_start_symptomatic) / 365) * mild_daly_weight;
		if(yld == 0.0)
			rec += "\t-";
		else
			rec += "\t" + (double) yld;
		// calculate YLL (basic)
		// YLL = Life expectancy in years - age at time of death, if age at death < Life expectancy else 0
		int lifeExpectancy = 62;  // according to world bank estimate https://data.worldbank.org/indicator/SP.DYN.LE00.IN?locations=ZW
		double yll = 0;
		if(time_died == Double.MAX_VALUE)
			rec += "\t-";
		else {
			yll = lifeExpectancy - host.getAge();
			// If this person's age is greater than the life expectancy of Zimbabwe, then assume there are no years of life lost
			if (yll < 0)
				yll = 0;
			rec += "\t" + (double) yll;
		}
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
	
		return "COVID-19";
	}
		
	@Override
	public String getCurrentAdminZone() {return this.host.getHousehold().getRootSuperLocation().myId;}	
	
	@Override
	public boolean isAlive() { return !this.host.isDead; }
	
	@Override
	public boolean covidLogCheck () { return this.hasCovidLogged; }

	@Override
	public boolean isCovid() {
		return true;
	}

	@Override
	public boolean hasRecovered() {
		// TODO Auto-generated method stub
		return this.getCurrentBehaviourNode().getTitle().equals( CoronavirusBehaviourNodeTitle.RECOVERED.key);
	}

	@Override
	public boolean hasAsympt() {
		return this.getCurrentBehaviourNode().getTitle().equals( CoronavirusBehaviourNodeTitle.ASYMPTOMATIC.key);

	}

	@Override
	public boolean getAsymptLogged() {
		// TODO Auto-generated method stub
		return this.hasAsymptLogged;
	}
	
	public void confirmAsymptLogged() {this.hasAsymptLogged = true; }

	@Override
	public boolean hasMild() {
		// TODO Auto-generated method stub
		return this.getCurrentBehaviourNode().getTitle().equals( CoronavirusBehaviourNodeTitle.MILD.key);
	}

	@Override
	public boolean getMildLogged() {
		// TODO Auto-generated method stub
		return this.hasMildLogged;
	}

	@Override
	public void confirmMildLogged() {
		this.hasMildLogged = true; 
		
	}

	@Override
	public boolean hasSevere() {
		// TODO Auto-generated method stub
		return this.getCurrentBehaviourNode().getTitle().equals( CoronavirusBehaviourNodeTitle.SEVERE.key);
	}

	@Override
	public boolean getSevereLogged() {
		// TODO Auto-generated method stub
		return this.hasSevereLogged;
	}

	@Override
	public void confirmSevereLogged() {
		// TODO Auto-generated method stub
		this.hasSevereLogged = true;
	}

	@Override
	public boolean hasCritical() {
		// TODO Auto-generated method stub
		return this.getCurrentBehaviourNode().getTitle().equals( CoronavirusBehaviourNodeTitle.CRITICAL.key);
	}

	@Override
	public boolean getCriticalLogged() {
		// TODO Auto-generated method stub
		return this.hasCriticalLogged;
	}

	@Override
	public void confirmCriticalLogged() {
		// TODO Auto-generated method stub
		this.hasCriticalLogged = true;
	}

	@Override
	public void confirmDeathLogged() {
		// TODO Auto-generated method stub
		this.hasDeathLogged = true;
	}

	@Override
	public boolean getDeathLogged() {
		// TODO Auto-generated method stub
		return this.hasDeathLogged ;
	}

	@Override
	public void setAsympt() {
		// TODO Auto-generated method stub
		this.hasAsympt = true;
		
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
		return hasLogged;
	}
	
	@Override
	public void setLogged() {
		this.hasLogged = true;
	}
	
	@Override
	public void setMild() {
		this.hasMild = true;
		
	}

	@Override
	public boolean isCovidSpuriousSymptom() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSymptomatic() {
		// TODO Auto-generated method stub
		return this.isSymptomatic;
	}

	@Override
	public void setSymptomatic() {
		this.isSymptomatic = !this.isSymptomatic;
		
	}

}
