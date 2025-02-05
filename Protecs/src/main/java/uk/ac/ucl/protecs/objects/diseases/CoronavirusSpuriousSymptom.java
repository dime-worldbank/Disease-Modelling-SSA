package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import swise.behaviours.BehaviourNode;

public class CoronavirusSpuriousSymptom implements Infection{
	// record keeping
	Person host;
	Person source;
	Location infectedAtLocation;
	WorldBankCovid19Sim myWorld;
	double timeCreated = Double.MAX_VALUE;
	double timeRecovered = Double.MAX_VALUE;
	double timeLastTriggered = Double.MAX_VALUE;
	// default these to max value so it's clear when they've been reset
	public double time_infected = Double.MAX_VALUE;
	public double time_contagious = Double.MAX_VALUE;
	public double time_start_symptomatic = Double.MAX_VALUE;
	public double time_start_severe = Double.MAX_VALUE;
	public double time_start_critical = Double.MAX_VALUE;
	public double time_recovered = 	Double.MAX_VALUE;
	public double time_died = Double.MAX_VALUE;
	
	// behaviours
	BehaviourNode currentBehaviourNode = null;
	
	public CoronavirusSpuriousSymptom(Person p, WorldBankCovid19Sim sim, BehaviourNode initNode, int time) {
		this.host = p;
		this.source = p;
		this.infectedAtLocation = p.getLocation();
		this.currentBehaviourNode = initNode;
		this.timeCreated = time;
		this.myWorld = sim;
		this.myWorld.CovidSpuriousSymptomsList.add(this);
		this.myWorld.infections.add(this);
		this.host.addInfection(DISEASE.COVIDSPURIOUSSYMPTOM, this);


	}

	@Override
	public void step(SimState arg0) {
		double time = myWorld.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		arg0.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_infecting, this);
	}

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
		// TODO Auto-generated method stub
		return "COVID-19_SPURIOUS_SYMPTOM";
	}

}