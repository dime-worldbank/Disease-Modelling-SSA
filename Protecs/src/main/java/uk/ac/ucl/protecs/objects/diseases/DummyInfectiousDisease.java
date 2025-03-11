package uk.ac.ucl.protecs.objects.diseases;

import java.util.HashSet;

import sim.engine.SimState;
import uk.ac.ucl.protecs.behaviours.diseaseSpread.InfectiousSpreadFramework;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.objects.locations.Workplace;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

// A dummy disease framework that can be used as the basis for future diseases. Here we imagine a hypothetical infection 
// with both horizontal and vertical transmission, that has a very small health burden and is asymptomatic. We implement two interfaces to 
// combine the necessary setters and getters for the disease and the transmission from one person to another

public class DummyInfectiousDisease implements Disease{
	
	// record keeping
	Person host;
	Person source;
	Location infectedAtLocation;
	WorldBankCovid19Sim myWorld;
	
	// behaviours
	BehaviourNode currentBehaviourNode = null;
	
	// infection states
	boolean hasAsympt = false;
	boolean hasRecovered = false;
	// loggers
	boolean hasAsymptLogged = false;
	boolean hasLogged = false;
	

	// infection timekeeping
	// default these to max value so it's clear when they've been reset
	public double time_infected = Double.MAX_VALUE;
	public double time_contagious = Double.MAX_VALUE;
	public double time_start_symptomatic = Double.MAX_VALUE;
	public double time_start_severe = Double.MAX_VALUE;
	public double time_start_critical = Double.MAX_VALUE;
	public double time_recovered = 	Double.MAX_VALUE;
	public double time_died = Double.MAX_VALUE;
		
	public DummyInfectiousDisease(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public DummyInfectiousDisease(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		
		source = mySource;
		
		host.addDisease(DISEASE.DUMMY_INFECTIOUS, this);
		
		this.hasAsympt = true;
			
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
	
	// =============================================== Disease transmission  =====================================================================
	
	@Override
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
				if (!otherPerson.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key) && myProb < myWorld.params.dummy_infectious_beta_horizontal) {
					DummyInfectiousDisease inf = new DummyInfectiousDisease(otherPerson, this.getHost(), myWorld.infectiousFramework.getEntryPoint(), myWorld);
					myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting); 
				}
			}
			return;
		}
		else {
			if(this.getHost().getLocation() instanceof Household){
				assert (!this.getHost().atWorkNow()): "p_" + this.getHost().getID() + "at work but having interactions at home";
				this.getHost().interactWithin(this.getHost().getLocation().personsHere, null, this.getHost().getLocation().personsHere.size(), DISEASE.DUMMY_INFECTIOUS, myWorld.params.dummy_infectious_beta_horizontal);		
			}
			// they may be at their economic activity site!
			else if(this.getHost().getLocation() instanceof Workplace){
				int myNumInteractions;
				if (this.getHost().getNumberOfWorkplaceInteractions() < 0) 
					this.getHost().setNumberOfWorkplaceInteractions(myWorld.params.getWorkplaceContactCount(this.getHost().getEconStatus(), this.myWorld.random.nextDouble()));
				
				myNumInteractions = (int) this.getHost().getNumberOfWorkplaceInteractions() / 2;

				if (myNumInteractions > this.getHost().getLocation().personsHere.size()) myNumInteractions = this.getHost().getLocation().personsHere.size();
				// interact 
				this.getHost().interactWithin(this.getHost().getLocation().personsHere, null, myNumInteractions, DISEASE.DUMMY_INFECTIOUS, myWorld.params.dummy_infectious_beta_horizontal);		

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
						if (!otherPerson.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key) && myProb < myWorld.params.dummy_infectious_beta_horizontal) {
							DummyInfectiousDisease inf = new DummyInfectiousDisease(otherPerson, this.getHost(), myWorld.infectiousFramework.getEntryPoint(), myWorld);
							myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting); 
						}
					}
					return;
				}
		} 
	}
	}
	
	@Override
	public void verticalTransmission(Person baby) {
		double myProb = myWorld.random.nextDouble();
		if (!baby.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key) && myProb < myWorld.params.dummy_infectious_beta_vertical) {
			DummyInfectiousDisease inf = new DummyInfectiousDisease(baby, this.getHost(), myWorld.infectiousFramework.getEntryPoint(), myWorld);
			myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting); 
		}
	}

	// =============================================== relevant information on the host ==============================================================

	@Override
	public Person getHost() {
		return host;
	}

	@Override
	public Person getSource() {
		return source;
	}

	@Override
	public Location infectedAt() {
		return infectedAtLocation;
	}

	@Override
	public double getStartTime() {
		return time_infected;
	}

	@Override
	public String getCurrentAdminZone() {
		return host.getHousehold().getRootSuperLocation().myId;
	}

	@Override
	public boolean isHostAlive() {
		return host.isAlive();
	}

	@Override
	public int getHostAge() {
		return host.getAge();
	}

	@Override
	public SEX getHostSex() {
		return host.getSex();
	}

	@Override
	public OCCUPATION getHostEconStatus() {
		return host.getEconStatus();
	}

	// =============================================== Disease behaviours  ==============================================================================			

	@Override
	public BehaviourNode getCurrentBehaviourNode() {
		return currentBehaviourNode;
	}

	@Override
	public void setBehaviourNode(BehaviourNode bn) {
		this.currentBehaviourNode = bn;
	}

	@Override
	public String getBehaviourName() {
		if(this.currentBehaviourNode == null) return "";
		return this.currentBehaviourNode.getTitle();
	}

	public boolean isInfectious() {
		return true;
	}
	
	// =============================================== Disease type classification ===========================================================================			

	@Override
	public boolean isOfType(DISEASE disease) {
		return this.getDiseaseType().equals(disease);
	}

	@Override
	public DISEASE getDiseaseType() {
		return DISEASE.DUMMY_INFECTIOUS;
	}

	@Override
	public String getDiseaseName() {
		// TODO Auto-generated method stub
		return DISEASE.DUMMY_INFECTIOUS.key;
	}
	// =============================================== Disease progression ====================================================================================

	@Override
	public void setAsympt() {
		this.hasRecovered = false;
		this.hasAsympt = true;
	}

	@Override
	public boolean hasAsympt() {
		// Always true
		return this.hasAsympt;
	}

	@Override
	public void setSymptomatic() {
		// NA
		
	}

	@Override
	public boolean isSymptomatic() {
		// NA
		return false;
	}

	@Override
	public void setMild() {
		// NA
		
	}

	@Override
	public boolean hasMild() {
		// NA
		return false;
	}

	@Override
	public void setSevere() {
		// NA
		
	}

	@Override
	public boolean hasSevere() {
		// NA
		return false;
	}

	@Override
	public void setCritical() {
		// NA
		
	}

	@Override
	public boolean hasCritical() {
		// NA
		return false;
	}

	@Override
	public void setRecovered() {
		this.hasAsympt = false;
		this.hasRecovered = true;
	}

	@Override
	public boolean hasRecovered() {
		return this.hasRecovered;
	}

	@Override
	public void setAsCauseOfDeath() {
		// NA
		
	}

	@Override
	public boolean isCauseOfDeath() {
		// NA	
		return false;
	}
	// =============================================== Disease logging ====================================================================================

	@Override
	public boolean getAsymptLogged() {
		// NA
		return this.hasAsymptLogged;
	}

	@Override
	public void confirmAsymptLogged() {
		this.hasAsymptLogged = true;
		
	}

	@Override
	public boolean getMildLogged() {
		// NA
		return false;
	}

	@Override
	public void confirmMildLogged() {
		// NA
		
	}

	@Override
	public boolean getSevereLogged() {
		// NA
		return false;
	}

	@Override
	public void confirmSevereLogged() {
		// NA
		
	}

	@Override
	public boolean getCriticalLogged() {
		// NA
		return false;
	}

	@Override
	public void confirmCriticalLogged() {
		// NA
		
	}

	@Override
	public boolean getDeathLogged() {
		// NA
		return false;
	}

	@Override
	public void confirmDeathLogged() {
		// NA
		
	}

	@Override
	public boolean getLogged() {
		// TODO Auto-generated method stub
		return this.hasLogged;
	}

	@Override
	public void confirmLogged() {
		this.hasLogged = true;		
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

		// Make up some amount of DALY burden
		double daly_weight = 0.05;

		// calculate DALYs part 1: YLD working from the most serious level of infection
		// YLD = fraction of year with condition * DALY weight
		if (time_infected < Double.MAX_VALUE) {
			// calculate yld between the onset of infection to recovery
			if (time_recovered < Double.MAX_VALUE)
				yld += ((time_recovered - time_infected) / 365) * daly_weight;
			// if they haven't recovered at the end of the simulation record the ongoing health burden
			else yld += ((this.myWorld.schedule.getTime() / this.myWorld.params.ticks_per_day - time_infected) / 365) * daly_weight;
		}
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

	@Override
	public boolean isEligibleForTesting() {
		// NA
		return false;
	}

	@Override
	public void setEligibleForTesting() {
		// NA
		
	}

	@Override
	public void removeEligibilityForTesting() {
		// NA
		
	}

	@Override
	public boolean hasBeenTested() {
		// NA
		return false;
	}

	@Override
	public void setTested() {
		// NA
		
	}

	@Override
	public void setTestedPositive() {
		// NA
		
	}

	@Override
	public boolean hasTestedPositive() {
		// NA
		return false;
	}

	@Override
	public boolean inATestingAdminZone() {
		// NA
		return false;
	}

	@Override
	public boolean getTestLogged() {
		// NA
		return false;
	}

	@Override
	public void confirmTestLogged() {
		// NA
		
	}
	}