package objects;

import sim.Params;
import sim.WorldBankCovid19Sim;
import sim.engine.SimState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import behaviours.BehaviourNode;
import behaviours.MovementBehaviourFramework;

import swise.agents.MobileAgent;


public class Person extends MobileAgent {
	
	//
	// Personal Attributes
	//
	
	// personal ID to distinguish from other agents
	int myId;

	// larger group membership
	Household myHousehold;

	// personal/demographic attributes
	int age;
	String sex;

	// economic attributes
	String economic_status;
	Location economic_activity_location; // treating districts as ints
	
	// locational attributes
	Location currentLocation;
	boolean district_mover; // allowed to move between districts?
	
	// social attributes
	Location communityLocation;
	HashSet <Person> workBubble;
	HashSet <Person> communityBubble;
	
	// activity
	BehaviourNode currentActivityNode = null;
	Infection myInfection = null; // TODO make a hashset of different infections! Allow multiple!!
	boolean immobilised = false;
	
	// copy of world
	WorldBankCovid19Sim myWorld;
	
	//
	// Epidemic Attributes
	//
	
	// health
	boolean isDead = false;
	int clinical_state;
	
	double severe_disease_risk;
	

	
	/**
	 * Constructor for Person object.
	 * 
	 * Applies all given demographic data and sets up the Person's health properties.
	 * 
	 * @param id Unique identifier for the Person
	 * @param age Age in years
	 * @param sex Assigned sex as String
	 * @param economic_status Linked to economic mobility files in Params
	 * @param economic_activity_location Location for weekday economic activity (workplace, school, etc.)
	 * @param world Copy of the simulation
	 */
	public Person(int id, int age, String sex, String economic_status, Location economic_activity_location, 
			Household hh, WorldBankCovid19Sim world){
		super();

		// demographic characteristics
		
		this.myId = id;
		this.age = age;
		this.sex = sex;
		
		// economic characteristics
		this.economic_status = economic_status;
		this.economic_activity_location = economic_activity_location;

		// record-keeping
		myHousehold = hh;
		myWorld = world;
		
		// other characteristics (possibly weighted)
		severe_disease_risk = 1;

		// agents are initialised uninfected
		
		communityLocation = myHousehold.getRootSuperLocation();
		workBubble = new HashSet <Person> ();
		communityBubble = new HashSet <Person> ();
		
		this.currentLocation = hh;
	}
	
	//
	// BEHAVIOURS
	//
	
	@Override
	public void step(SimState world) {
		if(isDead) return; // do not run if the Person has already died!
		else if(immobilised) return; // do not run until the Person is better!
		
		double time = world.schedule.getTime(); // find the current time
		double myDelta = this.currentActivityNode.next(this, time);
		myWorld.schedule.scheduleOnce(time + myDelta, this);
		//if(this.myId % 10000 == 0) System.out.print(">");
	}	

	/**
	 * A function which moves the Person from wherever they are to the given Location.
	 * Also updates the various Locations as appropriate (given possible nulls).
	 * @return the amount of time spent travelling to the given location.
	 */	
	public double transferTo(Location l){
		if(currentLocation != null)
			currentLocation.removePerson(this);
		currentLocation = l;
		if(l != null)
			l.addPerson(this);
		return 1; // TODO make based on distance travelled!
	}
	
	public void die(){
		isDead = true;
		transferTo(null);
		System.out.println(this.toString() + " has DIED :(");
	}
	
	public void infectNeighbours(){
		
		// if not currently in the space, do not try to interact
		if(currentLocation == null) return;
		else if(myInfection == null){
			System.out.println("ERROR: " + this.myId + " asked to infect others, but is not infected!");
			return;
		}
		
		// now apply the rules based on the setting

		// they may be at home
		if(currentLocation instanceof Household){

			interactWithin(currentLocation.personsHere, null, currentLocation.personsHere.size());
			
		}
		
		// they may be at their economic activity site!
		else if(currentLocation == economic_activity_location){
			
			// set up the stats
			Double d = myWorld.params.economic_num_interactions_weekday.get(this.economic_status);
			int myNumInteractions = (int) Math.round(d);
			
			// interact
			interactWithin(workBubble, currentLocation.personsHere, myNumInteractions);
		}
		
		else {

			// set up the holders
			int myNumInteractions = myWorld.params.community_interaction_count;
			Location myHomeCommunity = this.getHousehold().getRootSuperLocation();

			// will need to check if constrained by own local social bubble
			boolean inHomeCommunity = currentLocation == myHomeCommunity;

			if(inHomeCommunity)
				interactWithin(communityBubble, currentLocation.personsHere, myNumInteractions);
			else
				interactWithin(currentLocation.personsHere, null, myNumInteractions);
	
		}
	}

	/**
	 * 
	 * @param group - the small group to check within
	 * @param largerCommunity - if this is null, the parameter __group__ represents the set of Persons present. If __largerCommunity__
	 * 		is not null, it represents the Persons who are physically present and __group__ represents the Persons with whom this Person
	 * 		might actually interact.
	 * @param interactNumber - the number of interactions to make
	 */
	void interactWithin(HashSet <Person> group, HashSet <Person> largerCommunity, int interactNumber) {
		
		// setup
		Object [] checkWithin = group.toArray();
		int sizeOfCommunity = group.size();
		boolean largerCommunityContext = largerCommunity != null;
		
		// utilities
		HashSet <Integer> indicesChecked = new HashSet <Integer> ();
		
		// select the interaction partners
		for(int i = 0; i < interactNumber; i++){
			
			// make sure there are people left to add 
			if(indicesChecked.size() >= sizeOfCommunity) {
				return; // everyone available has been checked! No need to look any more!
			}

			// choose a random Person who is also here
			int j = myWorld.random.nextInt(sizeOfCommunity);
			
			// is this someone who hasn't already been checked?
			if(indicesChecked.contains(j)) { // already checked
				i--;
				continue;
			}
			else // they're being checked now!
				indicesChecked.add(j);
			
			// pull this Person out
			Person p = (Person) checkWithin[j];
			
			if(p == this) { // make sure it's not us!
				i--;
				continue;
			}
			
			// if we are within a larger community, we have to make sure our target interaction is present
			if(largerCommunityContext) {
				
				// make sure that Person is actually here right now!
				if(!largerCommunity.contains(p))
					continue;
			}
			
			// check if they are already infected; if they are not, infect with with probability BETA
			if(p.myInfection == null 
					&& myWorld.random.nextDouble() < myWorld.params.infection_beta){
				Infection inf = new Infection(p, this, myWorld.infectiousFramework.getEntryPoint(), myWorld);
				myWorld.schedule.scheduleOnce(inf, 10);
			}

		}	
	}
	
	/**
	 * Based on the Person's economic status, attempt to leave the Household. The destination selected will
	 * be drawn from their economic_activity_location.
	 * @param weekday The Person will pick different destinations based on the day of the week.
	 * @return
	 */
/*	double goOut(int weekday){
	
		
		// First, check that the Person is currently in their Household
		// TODO refine with more nuanced movement model
		if(this.currentLocation != this.myHousehold){
			System.out.println("WARNING: Person " + this.myId + " is not at home.");
		}
		

		
		if(Params.isWeekday(weekday)){
			
		}
		
		// if the Person has a workplace and is not there, consider going!
		if(this.economic_activity_location != null && this.currentLocation != this.economic_activity_location){
			
			// extract the appropriate economic status mobility data given the day
			double myEconStatProb = myWorld.params.getEconProbByDay(weekday, economic_status);
			
			// make sure that the the Parameters has a valid record for movement for this economic_status type
			if(myEconStatProb < 0){
				System.out.println("WARNING: no recorded movement probability for economic_status type \"" + 
						this.economic_status + "\". Person " + myId + " will never move.");
				return -1;
			}

			// TODO add symptomatic aspects (when appropriate)

			// randomly determine whether the Person is going to work today
			double activityProb = myWorld.random.nextDouble();
			if(activityProb < myEconStatProb){
				//System.out.print(myId + " IS GOING OUT\t");
				this.currentLocation = this.economic_activity_location;
				
			}			
		}
		return 1; // TODO base this on distance travelled!!
	}
*/
	//
	// UTILITIES
	//
	
	public void setLocation(Location l){
		if(this.currentLocation != null)
			currentLocation.removePerson(this);
		this.currentLocation = l;
		l.addPerson(this);
	}
	
	public Location getLocation(){
		return currentLocation;
	}
	
	public void addToWorkBubble(Collection <Person> newPeople){
		workBubble.addAll(newPeople);
	}
	
	public HashSet <Person> getWorkBubble(){ return workBubble; }

	public void addToCommunityBubble(Collection <Person> newPeople){
		communityBubble.addAll(newPeople);
	}
	
	public HashSet <Person> getCommunityBubble(){ return communityBubble; }
	public boolean isHome(){
		return currentLocation == myHousehold;
	}

	public String toString(){
		return "P_" + this.myId;
	}
	
	public void setActivityNode(BehaviourNode bn){
		currentActivityNode = bn;
	}
	
	public String getEconStatus(){ return economic_status;}
	
	public Location getHousehold(){ return myHousehold; }
	
	public boolean equals(Object o){
		if(! (o instanceof Person)) return false;
		return ((Person) o).myId == this.myId;
	}
	
	/** HashCode */
	public int hashCode(){ return myId; }
	
	public void setInfection(Infection i){
		myInfection = i;
	}
	
	public Infection getInfection(){
		return myInfection;
	}
	
	public String getInfectStatus(){
		if(myInfection == null)
			return "";
		return myInfection.currentBehaviourNode.getTitle();
	}
	
	public int getAge(){
		return age;
	}
	
	public Location getCommunityLocation(){ return communityLocation;}
	public Location getEconomicLocation(){ return this.economic_activity_location; }
	
	public int getID(){ return this.myId; }
	
	public double getSusceptibility(){
		return myWorld.params.getSuspectabilityByAge(age);
	}
	
	public void setMobility(boolean mobile){ this.immobilised = !mobile; }
	public boolean isImmobilised(){ return this.immobilised; }
}