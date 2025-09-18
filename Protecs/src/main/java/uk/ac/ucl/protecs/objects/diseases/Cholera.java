package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.hosts.Host;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.locations.Location;
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
	// create cholera-in-water specific time keeping things
	public double time_hyperinfectious_in_water = Double.MAX_VALUE;
	public double time_abnc_in_water = Double.MAX_VALUE;
	public double time_clean_in_water = Double.MAX_VALUE;
	
	
	
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
		else {
		myWorld.other_infections.add(this);
		time_hyperinfectious_in_water = time;
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
		// DALY weights are taken based on the DALY weights associated with diarrhoea, taken from the global burden of disease study: https://pmc.ncbi.nlm.nih.gov/articles/PMC10782811/#S12
		double mild_daly_weight = 0.061;
		double severe_daly_weight = 0.201;
		double critical_daly_weight = 0.281;


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
			yll = lifeExpectancy - ((Person)this.getHost()).getAge();
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
		rec += "\t" + ((Person)this.getHost()).getNumberOfTimesInfected();
		
		rec += "\n";
		return rec;
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