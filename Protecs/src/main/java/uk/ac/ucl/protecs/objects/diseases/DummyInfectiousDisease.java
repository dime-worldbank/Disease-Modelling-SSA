package uk.ac.ucl.protecs.objects.diseases;

import java.util.HashSet;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.objects.locations.Workplace;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

// A dummy disease framework that can be used as the basis for future diseases. Here we imagine a hypothetical infection 
// with both horizontal and vertical transmission, that has a very small health burden and is asymptomatic. We implement two interfaces to 
// combine the necessary setters and getters for the disease and the transmission from one person to another

public class DummyInfectiousDisease extends Disease{
	
	public DummyInfectiousDisease(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim){
		this(myHost, mySource, initNode, sim, (int) sim.schedule.getTime());
	}

	public DummyInfectiousDisease(Person myHost, Person mySource, BehaviourNode initNode, WorldBankCovid19Sim sim, int time){
		
		host = myHost;
		
		source = mySource;
		
		host.addDisease(this);
		
		this.hasAsympt = true;
			
		// store the time when it is infected!
		time_infected = time;		
		infectedAtLocation = myHost.getLocation();
		currentBehaviourNode = initNode;
		myWorld = sim;
		myWorld.human_infections.add(this);
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
		if (!((Person) this.getHost()).isAlive()) return;
		// if not currently in the space, do not try to interact
		else if(((Person) this.getHost()).getLocation() == null) return;
		// if there is no one else other than the individual at the location, save computation time and return out
		else if(((Person) this.getHost()).getLocation().getPersonsHere().length < 2) {
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
			otherPeople.add(((Person) this.getHost()));  
			
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
				if (!otherPerson.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key) && myProb < myWorld.dummyInfectiousFramework.getDummy_infectious_beta_horizontal()) {
					DummyInfectiousDisease inf = new DummyInfectiousDisease(otherPerson, ((Person) this.getHost()), myWorld.dummyInfectiousFramework.getEntryPoint(), myWorld);
					myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting); 
				}
			}
			return;
		}
	}
	
	@Override
	public void verticalTransmission(Person baby) {
		double myProb = myWorld.random.nextDouble();
		if (!baby.getDiseaseSet().containsKey(DISEASE.DUMMY_INFECTIOUS.key) && myProb < myWorld.dummyInfectiousFramework.getDummy_infectious_beta_vertical()) {
			DummyInfectiousDisease inf = new DummyInfectiousDisease(baby, ((Person) this.getHost()), myWorld.dummyInfectiousFramework.getEntryPoint(), myWorld);
			myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting); 
		}
	}
	@Override
	public boolean isInfectious() {
		return true;
	}
	
	@Override
	public boolean isWaterborne() {return false;}

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
	public void setRecovered() {
		this.hasAsympt = false;
		this.hasRecovered = true;
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
	public String writeOut() {
		return null;
	}

	// =============================================== Disease testing ====================================================================================

	@Override
	public boolean inATestingAdminZone() {
		// NA
		return false;
	}

	}