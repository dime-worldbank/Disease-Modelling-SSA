package uk.ac.ucl.protecs.objects;

import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import sim.engine.SimState;
import swise.agents.MobileAgent;
import swise.behaviours.BehaviourNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import uk.ac.ucl.protecs.objects.Location.LocationCategory;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusInfection;
import uk.ac.ucl.protecs.objects.diseases.Infection;


public class Person extends MobileAgent {
	
	//
	// Personal Attributes
	//
	
	// personal ID to distinguish from other agents
	private final int myId;

	// larger group membership
	Household myHousehold;
	Workplace myWorkplace;
	// personal/demographic attributes
	int age;
	private final int birthday;
	// only two options considered for biological sex, therefore use enum
	public enum SEX {
		MALE("male"), FEMALE("female");
		String key;
	     
		SEX(String key) { this.key = key; }
    
        public static SEX getValue(String x) {
        	switch (x) {
        	case "male":
        		return MALE;
        	case "female":
        		return FEMALE;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}
	
	private final SEX sex;

	// economic attributes. Economic status is read in from census file and can be accessed, but not changed
	public enum OCCUPATION{
		OFFICE_WORKER("office workers"), UNEMPLOYED("not working, inactive, not in universe"), TEACHER("teachers"),
		HOMEMAKER("homemakers/housework"), CURRENT_STUDENTS("current students"), SERVICE_WORKERS("service workers"), AGRICULTURE("agriculture workers"),
		INDUSTRY("industry workers"), ARMY("in the army"), DISABLED_NOT_WORKING("disabled and not working"), SERVICE_RETAIL("service_retail"),
		UNEMPLOYED_NOT_AG("unemployed_not_ag"), OFFICE_WORKERS("office_worker"), INACTIVE("inactive"), STUDENT("student"), 
		INFORMAL_PETTY_TRADE("informal_petty_trade"), OTHER("other"), MANU_MINING_TRADES("manu_mining_trades"), POLICE_ARMY("police_army"),
		HEALTHCARE_SOCIAL_WORK("healthcare_social_work"), EDUCATION("education"), RELIGIOUS("religious"), TRANSPORT_SECTOR("transport_sector"),
		SUBSISTENCE_AG("subsistence_ag"), AG_ESTATES("ag_estates"), STUDENTS_TEACHERS("students_teachers");
		public String key;
	     
		OCCUPATION(String key) { this.key = key; }
		
		public static OCCUPATION getValue(String x) {
        	switch (x) {
        	case "office workers":
        		return OFFICE_WORKER;
        	case "not working, inactive, not in universe":
        		return UNEMPLOYED;
        	case "teachers":
        		return TEACHER;
        	case "homemakers/housework":
        		return HOMEMAKER;
        	case "service workers":
        		return SERVICE_WORKERS;
        	case "student":
        		return STUDENT;
        	case "current students":
        		return CURRENT_STUDENTS;
        	case "agriculture workers":
        		return AGRICULTURE;
        	case "industry workers":
        		return INDUSTRY;
        	case "in the army":
        		return ARMY;
        	case "disabled and not working":
        		return DISABLED_NOT_WORKING;
        	case "service_retail":
        		return SERVICE_RETAIL;
        	case "unemployed_not_ag":
        		return UNEMPLOYED_NOT_AG;
        	case "office_worker":
        		return OFFICE_WORKERS;
        	case "inactive":
        		return INACTIVE;
        	case "informal_petty_trade":
        		return INFORMAL_PETTY_TRADE;
        	case "other":
        		return OTHER;
        	case "manu_mining_trades": 
        		return MANU_MINING_TRADES;
        	case "police_army":
        		return POLICE_ARMY;
        	case "healthcare_social_work":
        		return HEALTHCARE_SOCIAL_WORK;
        	case "education":
        		return EDUCATION;
        	case "religious":
        		return RELIGIOUS;
        	case "transport_sector":
        		return TRANSPORT_SECTOR;
        	case "subsistence_ag":
        		return SUBSISTENCE_AG;
        	case "ag_estates":
        		return AG_ESTATES;
        	case "students_teachers":
        		return STUDENTS_TEACHERS;
        	default:
        		throw new IllegalArgumentException();
        	}
		
		}
	}
	private final OCCUPATION economic_status;
	
	// locational attributes
	Location currentLocation;
	// schoolGoer is read in and never changed, ensure this is private
	private final boolean schoolGoer; // allowed to move between districts?
	
	// social attributes
	Location communityLocation;
	Location workLocation;
	HashSet <Person> workBubble;
	HashSet <Person> communityBubble;
	
	// activity
	BehaviourNode currentActivityNode = null;
	Infection myInfection = null; // TODO make a hashset of different infections! Allow multiple!!
	
	// behaviours
	boolean immobilised = false;
	boolean visiting = false;
	boolean atWork = false;
	boolean isUnemployed = false;
	
	// copy of world
	WorldBankCovid19Sim myWorld;
	
	//
	// Epidemic Attributes
	//
	
	// health
	boolean isDead = false;

	boolean asymptomaticLogged = false;
	boolean mildLogged = false;
	boolean severeLogged = false;
	boolean criticalLogged = false;
	boolean recoveredLogged = false;
	boolean covidLogged = false;
	boolean isDeadFromCovid = false;
	boolean isDeadFromOther = false;
	boolean deathLogged = false;
	boolean gaveBirthLastYear = false;
	boolean birthLogged = false;
	Integer dayGaveBirth = Integer.MAX_VALUE;
	Integer numberOfTimesWithCovid = 0;

	boolean asymptomatic = false;
	boolean presymptomatic = false;
	boolean mild = false;
	boolean severe = false;
	boolean critical = false;
	boolean recovered = false;
	boolean hasCovid = false;
	boolean hadCovid = false;


	// bubble interaction counters
	int number_of_interactions_at_work = Integer.MIN_VALUE;
	
	
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
	public Person(int id, int age, int birthday, SEX sex, OCCUPATION economic_status, boolean schoolGoer, Household hh, Workplace w, WorldBankCovid19Sim world){
		super();

		// demographic characteristics
		
		this.myId = id;
		this.age = age;
		this.birthday = birthday;
		this.sex = sex;
		
		// economic characteristics
		this.economic_status = economic_status;
		
		//this.economic_activity_location = economic_activity_location;
		
		this.schoolGoer = schoolGoer;

		// record-keeping
		myHousehold = hh;
		myWorkplace = w;
		myWorld = world;
		
		// agents are initialised uninfected
		
		communityLocation = myHousehold.getRootSuperLocation();
		communityLocation.setLocationType(LocationCategory.COMMUNITY);

		workLocation = myWorkplace;
		workBubble = new HashSet <Person> ();
		communityBubble = new HashSet <Person> ();
		
		this.currentLocation = hh;
	}
	
	//
	// BEHAVIOURS
	//
	
	@Override
	public void step(SimState world) {
		
		if(isDeadFromCovid | isDeadFromOther) return; // do not run if the Person has already died!
		
		else if(immobilised) return; // do not move while the Person is immobilised!
		
		double time = world.schedule.getTime(); // find the current time
		double myDelta = this.currentActivityNode.next(this, time);
		
		if(myDelta >= 0)
			myWorld.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_movement, this);
		else
			myWorld.schedule.scheduleOnce(this, myWorld.param_schedule_movement);
			
		// HACK TO ENSURE INTERACTION AWAY FROM HOME ADMIN ZONE
		// check if out of home admin zone
/*		if(visiting) {
			
			// if this Person is not infected, check if they catch anything from their neighbours!
			if(this.myInfection == null)
				myWorld.schedule.scheduleOnce(new Steppable() {

					@Override
					public void step(SimState arg0) {
						
						// set up: not at home, so out in the community!
						Object [] checkWithin = currentLocation.personsHere_list;
						int myNumInteractions = myWorld.params.community_interaction_count;
						int sizeOfCommunity = checkWithin.length;
						myNumInteractions = Math.min(myNumInteractions, sizeOfCommunity);
						
						// utilities
						HashSet <Integer> indicesChecked = new HashSet <Integer> ();
						
						// select the interaction partners
						for(int i = 0; i < myNumInteractions; i++){
							
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
							
							// check if they are already infected; if they are are, this Person is infected with with probability BETA
							if(p.myInfection != null 
									&& myWorld.random.nextDouble() < myWorld.params.infection_beta){
								
								Infection inf = new Infection(myWrapper(), p, myWorld.infectiousFramework.getHomeNode(), myWorld);
								myWorld.schedule.scheduleOnce(inf, 10);
							}

						}
												
					}
				
				}, myWorld.param_schedule_infecting);
		}
		//if(this.myId % 10000 == 0) System.out.print(">");
		 * 
		 */
	}	

	Person myWrapper() { return this; }
	
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
	
	public void die(String cause){
		if (cause == "covid") {
			isDeadFromCovid = true;
			System.out.println(this.toString() + " has DIED from " + cause + " :( ");
		}
		else {
			isDeadFromOther = true;
			System.out.println(this.toString() + " has DIED :(");

		}
		isDead = true;
		transferTo(null);


	}
	
	public void infectNeighbours(){
		// if this person is dead, do not try and interact
		if (this.isDead) return;
		// if not currently in the space, do not try to interact
		else if(currentLocation == null) return;
		// if they do not have an infection object return out 
		else if(myInfection == null){
			System.out.println("ERROR: " + this.myId + " asked to infect others, but is not infected!");
			return;
		}
		// if there is no one else other than the individual at the location, save computation time and return out
		else if(this.currentLocation.getPersonsHere().length < 2) {
			return; 
			}
		
		if(myWorld.params.setting_perfectMixing) {
			
			perfectMixingInteractions(); 
			return;
		}
		else {
			structuredMixingInteractions();
			return;
		}
	}
		// now apply the rules based on the setting

		// they may be at home
/*		if(currentLocation instanceof Household){

			interactWithin(currentLocation.personsHere, null, currentLocation.personsHere.size());
			
		}
		
		// they may be at their economic activity site!
		else if(atWork){
			
			// set up the stats
			Double d = myWorld.params.economic_num_interactions_weekday.get(this.economic_status);
			int myNumInteractions = (int) Math.round(d);
			
			// interact
			interactWithin(workBubble, currentLocation.personsHere, myNumInteractions);
		}
		
		else {

			// set up the holders
			int myNumInteractions = myWorld.params.community_interaction_count;
			
			if(myWorld.params.setting_perfectMixing) {			
				int numPeople = myWorld.agents.size();
				for(int i = 0; i < myNumInteractions; i++) {
					Person otherPerson = myWorld.agents.get(myWorld.random.nextInt(numPeople));
					if(otherPerson == this) {
						i--;
						continue;
					}
					
					// check if they are already infected; if they are not, infect with with probability BETA
					if(otherPerson.myInfection == null 
							&& myWorld.random.nextDouble() < myWorld.params.infection_beta){
						Infection inf = new Infection(otherPerson, this, myWorld.infectiousFramework.getHomeNode(), myWorld);
						myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting);
					}

				}
				
				return;
			}
			
			Location myHomeCommunity = this.getHousehold().getRootSuperLocation();

			// will need to check if constrained by own local social bubble
			boolean inHomeCommunity = currentLocation == myHomeCommunity;

			if(inHomeCommunity)
				interactWithin(communityBubble, currentLocation.personsHere, myNumInteractions);
			else
				interactWithin(currentLocation.personsHere, null, myNumInteractions);
	
		}
		*/

	private void perfectMixingInteractions() {
		Object [] peopleHere = this.currentLocation.getPersonsHere();
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
		otherPeople.add(this); 
		
		for(int i = 0; i < myNumInteractions; i++) {
			Person otherPerson = (Person) peopleHere[myWorld.random.nextInt(numPeople)]; 
			
			// don't interact with the same person multiple times
			if(otherPeople.contains(otherPerson)) {
				i -= 1;
				continue;
			}
			else
				otherPeople.add(otherPerson); 
			
			myWorld.testingAgeDist.add(otherPerson.age); 
			
			// check if they are already infected; if they are not, infect with with probability BETA
			double myProb = myWorld.random.nextDouble();
			if(otherPerson.myInfection == null 
					&& myProb < myWorld.params.infection_beta){
				CoronavirusInfection inf = new CoronavirusInfection(otherPerson, this, myWorld.infectiousFramework.getHomeNode(), myWorld);
				myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting); 
			} 

		}
	}
	
	private void structuredMixingInteractions() {
		if(currentLocation instanceof Household){
			assert (!this.atWork): "at work but having interactions at home";
			interactWithin(currentLocation.personsHere, null, currentLocation.personsHere.size());		
			myWorld.home_interaction_counter ++;
		}
		// they may be at their economic activity site!
		else if(currentLocation instanceof Workplace){
			int myNumInteractions;
			if (this.number_of_interactions_at_work < 0) 
				this.number_of_interactions_at_work = myWorld.params.getWorkplaceContactCount(this.getEconStatus(), this.myWorld.random.nextDouble());
			
			myNumInteractions = (int) this.number_of_interactions_at_work / 2;

			if (myNumInteractions > currentLocation.personsHere.size()) myNumInteractions = currentLocation.personsHere.size();
			// interact
			interactWithin(workBubble, currentLocation.personsHere, myNumInteractions);
			myWorld.work_interaction_counter ++;

		}
		else {
			myWorld.community_interaction_counter ++;

			perfectMixingInteractions();
		}
	
	}


	void OLDinteractWithin(HashSet <Person> group, HashSet <Person> largerCommunity, int interactNumber) {
		
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
				CoronavirusInfection inf = new CoronavirusInfection(p, this, myWorld.infectiousFramework.getHomeNode(), myWorld); 
				myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting);
			}

		}	
	}
	
	/**
	 * A helper function for infectNeighbours().
	 *
	 * Utility function to help the individual interact with up to interactNumber individuals, as constrained by
	 * both the group and the largerCommunity. If largerCommunity is non-null, the Person will only interact with 
	 * members of "group" who are also present in largerCommunity.
	 * 
	 * @param group - the small group to check within
	 * @param largerCommunity - if this is null, the parameter __group__ represents the set of Persons present. 
	 * 		If __largerCommunity__ is not null, it represents the Persons who are physically present and __group__ 
	 * 		represents the Persons with whom this Person might actually interact.
	 * @param interactNumber - the number of interactions to make
	 */
	void interactWithin(HashSet <Person> group, HashSet <Person> largerCommunity, int interactNumber) {
	
		// set up parameters
		boolean largerCommunityContext = largerCommunity != null;

		// set up the probabilities
		double groupSize = group.size();
		double numberOfInteractions = interactNumber; 
		double probabilityOfInteractingWithAnyGivenGroupMember = numberOfInteractions / groupSize;

		// create the iterator and iterate over the set elements
		// TODO: look at selection without replacement 
		Iterator myIt = group.iterator();
		while(myIt.hasNext() && numberOfInteractions > 0) { 
			
			// generate the likelihood of selecting this particular element
			double prob = myWorld.random.nextDouble();
			
			if(prob <= probabilityOfInteractingWithAnyGivenGroupMember) { // INTERACT WITH THE PERSON 
				
				// pull them out!
				Person p = (Person) myIt.next();
				
				if(p == this) // oops! It might be this person - if so, continue!
					continue;
				
				// if it's someone else, make sure they're here!
				else if (largerCommunityContext && !largerCommunity.contains(p))
					continue;
				
				// if neither of the above are true, the interaction can take place!
				numberOfInteractions -= 1; 
				
				// check if they are already infected; if they are not, infect with with probability BETA
				if(p.myInfection == null 
						&& myWorld.random.nextDouble() < myWorld.params.infection_beta){
					Infection inf = new CoronavirusInfection(p, this, myWorld.infectiousFramework.getHomeNode(), myWorld);
					myWorld.schedule.scheduleOnce(inf, myWorld.param_schedule_infecting);
				}

			}
			else // just pass over it
				myIt.next();
			groupSize -= 1; 
			probabilityOfInteractingWithAnyGivenGroupMember = numberOfInteractions / groupSize;
		}
	}
	
	//
	// GETTERS AND SETTERS
	//

	// LOCATIONAL 
	
	public void setLocation(Location l){
		if(this.currentLocation != null)
			currentLocation.removePerson(this);
		this.currentLocation = l;
		l.addPerson(this);
	}
	
	public Location getLocation(){ return currentLocation;}
	public boolean isHome(){ return currentLocation == myHousehold;}

	public Location getCommunityLocation(){ return communityLocation;}

	public Location getWorkLocation() { 
		return workLocation;
	}
	public boolean atWorkNow(){ return this.atWork; }
	public void setAtWork(boolean atWork) { this.atWork = atWork; }
	
	public boolean visitingNow() { return this.visiting; }
	public void setVisiting(boolean visiting) { this.visiting = visiting; }

	public void sendHome() {
		this.transferTo(myHousehold);
		this.setActivityNode(myWorld.movementFramework.getHomeNode());
		this.setAtWork(false); 
	}
	
	// BUBBLE MANAGEMENT
	
	public void addToWorkBubble(Collection <Person> newPeople){ workBubble.addAll(newPeople);}	
	public HashSet <Person> getWorkBubble(){ return workBubble; }
	public String checkWorkplaceID() { return myWorkplace.getId(); } 
	public void setWorkBubble(HashSet <Person> newBubble) { workBubble = newBubble; }

	public void addToCommunityBubble(Collection <Person> newPeople){ communityBubble.addAll(newPeople);}
	public HashSet <Person> getCommunityBubble(){ return communityBubble; }
	public void setCommunityBubble(HashSet <Person> newBubble) { communityBubble = newBubble; }

	// ATTRIBUTES

	public double getSusceptibility(){ return myWorld.params.getSuspectabilityByAge(age); } // TODO make more nuanced
	
	public void setActivityNode(BehaviourNode bn){ currentActivityNode = bn; }
	public BehaviourNode getActivityNode(){ return currentActivityNode; }
	
	public int getAge(){ return age;}
	public int getBirthday() {return birthday; }
	public SEX getSex() {return this.sex;};
	public OCCUPATION getEconStatus(){ return economic_status;}
	public Location getHousehold(){ return myHousehold; }

	public boolean hasPresymptCovid() { return this.presymptomatic; }

		
	public void setInfection(Infection i){ myInfection = i; }
	public boolean hadCovid() { return this.hadCovid; }
	public boolean hasAsymptCovid() { return this.asymptomatic; }
	public boolean hasMild() { return this.mild; }
	public boolean hasSevere() { return this.severe; }
	public boolean hasCritical() { return this.critical; }
	public boolean hasRecovered() { return this.recovered; }


	public Infection getInfection(){ return myInfection; }
	
	public void setMobility(boolean mobile){ this.immobilised = !mobile; }
	public boolean isImmobilised(){ return this.immobilised; }

	public boolean getCovidLogged() { return this.covidLogged; }
	public boolean getAsymptCovidLogged() { return this.asymptomaticLogged; }
	public boolean getMildCovidLogged() { return this.mildLogged; }
	public boolean getSevereCovidLogged() { return this.severeLogged; }
	public boolean getCriticalCovidLogged() { return this.criticalLogged; }
	public boolean isDeadFromCovid() { return this.isDeadFromCovid; }
	public boolean isDeadFromOther() { return this.isDeadFromOther; }
	
	public boolean covidLogCheck () { return this.covidLogged; }
	public boolean getDeathLogged () { return this.deathLogged; }
	public boolean gaveBirthLastYear() { return this.gaveBirthLastYear; }
	public int getDateGaveBirth() { return this.dayGaveBirth; }
	public boolean getBirthLogged() { return this.birthLogged; }
	public int getNumberOfTimesInfected() { return this.numberOfTimesWithCovid; }

	public boolean isAlive() { return !this.isDead; }

	public boolean hasCovid() { return this.hasCovid; }
	
	public boolean isSchoolGoer() { return this.schoolGoer; }

	public void storeCovid() { this.hasCovid = true; this.hadCovid = true;}
	public void setAsympt() { this.asymptomatic = true; }
	public void setPresympt() { this.presymptomatic = true; }
	public void removePresympt() { this.presymptomatic = false; }
	public void setMild() { this.mild = true; }
	public void removeMild() { this.mild = false; }
	public void setSevere() { this.severe = true; }
	public void removeSevere() { this.severe = false; }
	public void setCritical() { this.critical = true; }
	public void setRecovered() { this.recovered = true; }

	public void removeCovid() { 
		this.asymptomatic = false;
		this.mild = false;
		this.severe = false;
		this.critical = false;
		this.hasCovid = false; 
		}

	public String getCurrentAdminZone() {return this.getHousehold().getRootSuperLocation().myId;}
	public void setUnemployed() {this.isUnemployed = true;}
	public boolean isUnemployed() {return this.isUnemployed;} 
	public void resetWorkplaceContacts() { this.number_of_interactions_at_work = Integer.MIN_VALUE;}
	// UTILS
	
	public String toString(){ return "P_" + this.myId;}
	public int getID(){ return this.myId; }

	// for use in the HashCode!!
	public int hashCode(){ return myId; }
	
	public boolean equals(Object o){
		if(! (o instanceof Person)) return false;
		return ((Person) o).myId == this.myId;
	}
	public void confirmDeathLogged() { this.deathLogged = true; }
	public void gaveBirth(int time) { 
		this.gaveBirthLastYear = true; 
		this.dayGaveBirth = time;
	}
	public void updateAge(){
		this.age += 1;
	}
	public void ableToGiveBirth() { 
		this.gaveBirthLastYear = false; 
		this.dayGaveBirth = Integer.MAX_VALUE;
	}
	public void confirmBirthlogged() { this.birthLogged = true; }
	public void removeBirthLogged() { this.birthLogged = false; }
	public void confirmCovidLogged() { this.covidLogged = true; }
	public void confirmAsymptLogged() {this.asymptomaticLogged = true; }
	public void confirmMildLogged() {this.mildLogged = true; }
	public void confirmSevereLogged() {this.severeLogged = true; }
	public void confirmCriticalLogged() {this.criticalLogged = true; }
	public void updateCovidCounter () { this.numberOfTimesWithCovid++; }
	
	public void resetCovidLog() { this.covidLogged = false; this.asymptomaticLogged = false; this.mildLogged = false; this.severeLogged = false; this.criticalLogged = false;}


	public Household getHouseholdAsType() {
		
		return this.myHousehold;
	}
	
}
