package uk.ac.ucl.protecs.objects.diseases;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.CoronavirusDiseaseProgressionFramework.CoronavirusBehaviourNodeTitle;
import uk.ac.ucl.protecs.objects.hosts.Person;

import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.objects.locations.Workplace;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

import java.util.HashSet;

import sim.engine.SimState;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

/**
 * The object holds records of individual instances of disease. It works together with the
 * CoronavirusBehaviourFramework, saving the information about specific instances while
 * using the Framework to drive the characteristic progression.
 *
 */

public class CoronavirusInfection extends Disease {

	public CoronavirusInfection(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public CoronavirusInfection(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		myHost.addDisease(DISEASE.COVID, this);
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

	
	public void step(SimState world) {
		double time = world.schedule.getTime(); // find the current time
		double myDelta = this.currentBehaviourNode.next(this, time);
		world.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_infecting, this);
	}
	// =============================================== Disease transmission  =====================================================================
	
		public void horizontalTransmission() {
			// if this infection's host is dead, do not try and interact
			if (!host.isAlive()) return;
			// if not currently in the space, do not try to interact
			else if(host.getLocation() == null) return;
			// if there is no one else other than the individual at the location, save computation time and return out
			else if(host.getLocation().getPersonsHere().length < 2) {
				return; 
				} 
			if(myWorld.params.setting_perfectMixing) {
				Object [] peopleHere = host.getLocation().getPersonsHere();
				int numPeople = peopleHere.length;
				
				double someInteractions = myWorld.params.community_num_interaction_perTick;
				
				double myNumInteractions = Math.min(numPeople - 1, someInteractions);
				
				// this number may be probabilistic - e.g. 3.5. In this case, in 50% of ticks they should
				// interact with 4 people, and in 50% of ticks they should interact with only 3.
				
				// Thus, we calculate the probability of the extra person
				double diff = myNumInteractions - Math.floor(myNumInteractions); // number between 0 and 1
				
				// if the random number is less than this, we bump the number up to the higher number this tick
				if(myWorld.random.nextDouble() < diff)
						myNumInteractions = Math.ceil(myNumInteractions);
				
				// don't interact with the same person twice
				HashSet <Person> otherPeople = new HashSet <Person> ();
				otherPeople.add(host);  
				
				for(int i = 0; i < myNumInteractions; i++) {
					Person otherPerson = (Person) peopleHere[myWorld.random.nextInt(numPeople)]; 
					
					// don't interact with the same person multiple times
					if(otherPeople.contains(otherPerson)) {
						i -= 1;
						continue; 
					}
					else
						otherPeople.add(otherPerson); 
									
					// check if they are already infected; if they are not, infect with with probability BETA
					double myProb = myWorld.random.nextDouble();
					// Eligibility for Covid infection: They haven't already got COVID, and if they have it they are susceptible to reinfection
					boolean has_recovered_from_prior_covid_infection = false;
					try {
						has_recovered_from_prior_covid_infection = (otherPerson.getDiseaseSet().get(DISEASE.COVID.key).getBehaviourName().equals(CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.key));
						}
					catch (Exception e) {}
					
					boolean eligible_for_infection = (!otherPerson.getDiseaseSet().containsKey(DISEASE.COVID.key)) | has_recovered_from_prior_covid_infection;
					
					if ((eligible_for_infection) && (myProb < myWorld.params.infection_beta)) {
						CoronavirusInfection inf = new CoronavirusInfection(otherPerson, this.getHost(), myWorld.infectiousFramework.getEntryPoint(), myWorld);
						myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting);
					}
				}
				return;
			}
			else {
				if(this.getHost().getLocation() instanceof Household){
					assert (!this.getHost().atWorkNow()): "p_" + this.getHost().getID() + "at work but having interactions at home";
					this.getHost().interactWithin(this.getHost().getLocation().personsHere, null, this.getHost().getLocation().personsHere.size(), DISEASE.COVID, myWorld.params.infection_beta);		
				}
				// they may be at their economic activity site!
				else if(this.getHost().getLocation() instanceof Workplace){
					int myNumInteractions;
					if (this.getHost().getNumberOfWorkplaceInteractions() < 0) 
						this.getHost().setNumberOfWorkplaceInteractions(myWorld.params.getWorkplaceContactCount(this.getHost().getEconStatus(), this.myWorld.random.nextDouble()));
					
					myNumInteractions = (int) this.getHost().getNumberOfWorkplaceInteractions() / 2;

					if (myNumInteractions > this.getHost().getLocation().personsHere.size()) myNumInteractions = this.getHost().getLocation().personsHere.size();
					// interact 
					this.getHost().interactWithin(this.getHost().getLocation().personsHere, null, myNumInteractions, DISEASE.COVID, myWorld.params.infection_beta);		

				}
				else {
					// if this infection's host is dead, do not try and interact
					if (!host.isAlive()) return;
					// if not currently in the space, do not try to interact
					else if(host.getLocation() == null) return;
					// if there is no one else other than the individual at the location, save computation time and return out
					else if(host.getLocation().getPersonsHere().length < 2) {
						return; 
						} 
					if(myWorld.params.setting_perfectMixing) {
						Object [] peopleHere = host.getLocation().getPersonsHere();
						int numPeople = peopleHere.length;
						
						double someInteractions = myWorld.params.community_num_interaction_perTick;
						
						double myNumInteractions = Math.min(numPeople - 1, someInteractions);
						
						// this number may be probabilistic - e.g. 3.5. In this case, in 50% of ticks they should
						// interact with 4 people, and in 50% of ticks they should interact with only 3.
						
						// Thus, we calculate the probability of the extra person
						double diff = myNumInteractions - Math.floor(myNumInteractions); // number between 0 and 1
						
						// if the random number is less than this, we bump the number up to the higher number this tick
						if(myWorld.random.nextDouble() < diff)
								myNumInteractions = Math.ceil(myNumInteractions);
						
						// don't interact with the same person twice
						HashSet <Person> otherPeople = new HashSet <Person> ();
						otherPeople.add(host);  
						
						for(int i = 0; i < myNumInteractions; i++) {
							Person otherPerson = (Person) peopleHere[myWorld.random.nextInt(numPeople)]; 
							
							// don't interact with the same person multiple times
							if(otherPeople.contains(otherPerson)) {
								i -= 1;
								continue; 
							}
							else
								otherPeople.add(otherPerson); 
											
							// check if they are already infected; if they are not, infect with with probability BETA
							double myProb = myWorld.random.nextDouble();
							// Eligibility for Covid infection: They haven't already got COVID, and if they have it they are susceptible to reinfection
							boolean has_recovered_from_prior_covid_infection = false;
							try {
								has_recovered_from_prior_covid_infection = (otherPerson.getDiseaseSet().get(DISEASE.COVID.key).getBehaviourName().equals(CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.key));
								}
							catch (Exception e) {}
							
							boolean eligible_for_infection = (!otherPerson.getDiseaseSet().containsKey(DISEASE.COVID.key)) | has_recovered_from_prior_covid_infection;
							if ((eligible_for_infection) && (myProb < myWorld.params.infection_beta)) {
								CoronavirusInfection inf = new CoronavirusInfection(otherPerson, this.getHost(), myWorld.infectiousFramework.getEntryPoint(), myWorld);
								myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting);
							}
						}
						return;
					}
			} 
		}
		}
		
		public void verticalTransmission(Person baby) {
			// TODO Auto-generated method stub
			
		}		
	
	// =============================================== Disease 'behaviours'================================================================================
	public boolean isInfectious() {
		return true;
	}

	
	// =============================================== Disease type classification ===========================================================================
	@Override
	public String getDiseaseName() {
	
		return "COVID-19";
	}
	@Override
	public DISEASE getDiseaseType() {
		// TODO Auto-generated method stub
		return DISEASE.COVID;
	}
	@Override
	public boolean isOfType(DISEASE disease) {
		// TODO Auto-generated method stub
		return this.getDiseaseType().equals(disease);
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
	
	// =============================================== Disease testing ====================================================================================
	// filtering and setting who should be tested
	@Override
	public boolean inATestingAdminZone() {
		String hostLocationId = this.getHost().myHousehold.getRootSuperLocation().myId;
		boolean answer = this.getHost().myWorld.params.admin_zones_to_test_in.contains(hostLocationId);
		return answer;
	}

}
