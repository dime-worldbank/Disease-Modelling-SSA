package uk.ac.ucl.protecs.objects.hosts;

import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.HOST;

import sim.engine.SimState;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.CholeraDiseaseProgressionFramework.CholeraBehaviourNodeInWater;
import uk.ac.ucl.protecs.objects.diseases.Cholera;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusInfection;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.objects.diseases.DummyInfectiousDisease;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.objects.locations.Workplace;
import uk.ac.ucl.protecs.objects.locations.Location.LocationCategory;


public class Person extends Host {
	
	//
	// Personal Attributes
	//
	
	// personal ID to distinguish from other agents
	private final int myId;

	// larger group membership
	public Location homeLocation;
	public Workplace myWorkplace;
	// personal/demographic attributes	
	private final int birthday;
	
	private final OCCUPATION economic_status;
	
	// schoolGoer is read in and never changed, ensure this is private
	private final boolean schoolGoer; // allowed to move between districts?
	
	// social attributes
	Location communityLocation;
	Location workLocation;
	HashSet <Person> workBubble;
	HashSet <Person> communityBubble;
	
	// activity
	BehaviourNode currentActivityNode = null;
	
	
	// behaviours
	boolean immobilised = false;
	boolean visiting = false;
	boolean atWork = false;
	boolean wentToWorkToday = false;
	boolean wentToCommunityToday = false;
	boolean isUnemployed = false;
	boolean isWaterGatherer = false;
	
	//
	// Epidemic Attributes
	//
	
	// health

	boolean isDeadFromOther = false;
	boolean deathLogged = false;
	boolean isPregnant = false;
	boolean gaveBirthLastYear = false;
	boolean birthLogged = false;
	Integer dayGaveBirth = Integer.MAX_VALUE;
	Integer numberOfTimesWithCovid = 0;

	// bubble interaction counters
	int number_of_interactions_at_work = Integer.MIN_VALUE;
	int number_of_interactions_at_work_happened = 0;
	int number_of_interactions_in_community_per_day = Integer.MIN_VALUE;
	int number_of_interactions_at_community_happened = 0;

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

		boolean isDead;
		
		int age;
		
		SEX sex;
			
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
		homeLocation = hh;
		myWorkplace = w;
		myWorld = world;
		
		// agents are initialised uninfected
		
		communityLocation = homeLocation.getRootSuperLocation();
		communityLocation.setLocationType(LocationCategory.COMMUNITY);

		workLocation = myWorkplace;
		workBubble = new HashSet <Person> ();
		communityBubble = new HashSet <Person> ();
		myDiseaseSet = new HashMap <String, Disease>();
		setLocation(hh, this);
	}
	
	//
	// BEHAVIOURS
	//
	
	@Override
	public void step(SimState world) {
		
		if(isDead) return; // do not run if the Person has already died!
		
		else if(immobilised) return; // do not move while the Person is immobilised!
		
		double time = world.schedule.getTime(); // find the current time
		double myDelta = this.currentActivityNode.next(this, time);
		
		if(myDelta >= 0)
			myWorld.schedule.scheduleOnce(time + myDelta, myWorld.param_schedule_movement, this);
		else
			myWorld.schedule.scheduleOnce(this, myWorld.param_schedule_movement);
		
		
		
	}

	Person myWrapper() { return this; }
	
	/**
	 * A function which moves the Person from wherever they are to the given Location.
	 * Also updates the various Locations as appropriate (given possible nulls).
	 * @return the amount of time spent travelling to the given location.
	 */	
	
	public void die(String cause){
		if (cause == "COVID-19") {
			isDead = true;
			System.out.println(this.toString() + " has DIED from " + cause + " :( ");
		}
		if (cause == "Cholera") {
			isDead = true;
			System.out.println(this.toString() + " has DIED from " + cause + " :( ");
		}
		else {
			isDeadFromOther = true;
			System.out.println(this.toString() + " has DIED :(");

		}
		isDead = true;
		transferTo(null);
		// remove this person from the household storage list
		getHouseholdAsType().removeDeceasedFromHousehold(this);
		// if they are responsible for gathering water, reassign the responsibility
		if (getWaterGatherer()) {
			this.getHouseholdAsType().determineWaterGathererInHousehold();
		}


	}
	
	public void infectNeighbours(){
		for (Disease d: this.getDiseaseSet().values()) {
			d.horizontalTransmission();
		}
//		// if this person is dead, do not try and interact
//		if (this.isDead) return;
//		// if not currently in the space, do not try to interact
//		else if(currentLocation == null) return;
//		// if they do not have an infection object return out 
////		else if(myInfection == null){
////			System.out.println("ERROR: " + this.myId + " asked to infect others, but is not infected!");
////			return;
////		}
//		else if(!myDiseaseSet.containsKey(DISEASE.COVID.key)){
//			System.out.println("ERROR: " + this.myId + " asked to infect others, but is not infected!");
//			return;
//		}
//		// if there is no one else other than the individual at the location, save computation time and return out
//		else if(this.currentLocation.getPersonsHere().length < 2) {
//			return; 
//			} 
//		
//		if(myWorld.params.setting_perfectMixing) {
//			
//			perfectMixingInteractions(); 
//			return;
//		}
//		else {
//			structuredMixingInteractions();
//			return;
//		} 
	}
		
 
//	private void perfectMixingInteractions() {
//		Object [] peopleHere = this.currentLocation.getPersonsHere();
//		int numPeople = peopleHere.length;
//		
//		double someInteractions = myWorld.params.community_num_interaction_perTick;
//		
//		double myNumInteractions = Math.min(numPeople - 1, someInteractions);
//		
//		// this number may be probabilistic - e.g. 3.5. In this case, in 50% of ticks they should
//		// interact with 4 people, and in 50% of ticks they should interact with only 3.
//		
//		// Thus, we calculate the probability of the extra person
//		double diff = myNumInteractions - Math.floor(myNumInteractions); // number between 0 and 1
//		
//		// if the random number is less than this, we bump the number up to the higher number this tick
//		if(myWorld.random.nextDouble() < diff)
//				myNumInteractions = Math.ceil(myNumInteractions);
//		
//		// don't interact with the same person twice
//		HashSet <Person> otherPeople = new HashSet <Person> ();
//		otherPeople.add(this);  
//		
//		for(int i = 0; i < myNumInteractions; i++) {
//			Person otherPerson = (Person) peopleHere[myWorld.random.nextInt(numPeople)]; 
//			
//			// don't interact with the same person multiple times
//			if(otherPeople.contains(otherPerson)) {
//				i -= 1;
//				continue; 
//			}
//			else
//				otherPeople.add(otherPerson); 
//			
//			myWorld.testingAgeDist.add(otherPerson.age); 
//			
//			// check if they are already infected; if they are not, infect with with probability BETA
//			double myProb = myWorld.random.nextDouble();
//			if (!otherPerson.myDiseaseSet.containsKey(DISEASE.COVID.key) && myProb < myWorld.params.infection_beta) {
//				otherPerson.myDiseaseSet.put(DISEASE.COVID.key, 
//						new CoronavirusInfection(otherPerson, this, myWorld.infectiousFramework.getEntryPoint(), myWorld));
//				myWorld.schedule.scheduleOnce(otherPerson.myDiseaseSet.get(DISEASE.COVID.key), myWorld.param_schedule_infecting); 
//			}
//		}
//	}
//	
//	private void structuredMixingInteractions() {
//		if(currentLocation instanceof Household){
//			assert (!this.atWork): "p_" + this.getID() + "at work but having interactions at home";
//			interactWithin(currentLocation.personsHere, null, currentLocation.personsHere.size());		
//		}
//		// they may be at their economic activity site!
//		else if(currentLocation instanceof Workplace){
//			int myNumInteractions;
//			if (this.number_of_interactions_at_work < 0) 
//				this.number_of_interactions_at_work = myWorld.params.getWorkplaceContactCount(this.getEconStatus(), this.myWorld.random.nextDouble());
//			
//			myNumInteractions = (int) this.number_of_interactions_at_work / 2;
//
//			if (myNumInteractions > currentLocation.personsHere.size()) myNumInteractions = currentLocation.personsHere.size();
//			// interact 
//			interactWithin(workBubble, currentLocation.personsHere, myNumInteractions);
//
//		}
//		else {
//
//			perfectMixingInteractions(); 
//		}
//	
//	}
	
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
	public void interactWithin(HashSet <Person> group, HashSet <Person> largerCommunity, int interactNumber, DISEASE inf, double beta) {
	
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
				switch (inf) {
				case COVID:{
					if(!p.getDiseaseSet().containsKey(inf.key) && myWorld.random.nextDouble() < beta){
						p.getDiseaseSet().put(inf.key, new CoronavirusInfection(p, this, myWorld.covidInfectiousFramework.getEntryPoint(), myWorld));
						myWorld.schedule.scheduleOnce(p.getDiseaseSet().get(inf.key), myWorld.param_schedule_infecting);
					}
					// reinfection
					// check if they are already infected; if they are not, infect with with probability BETA

//					else {
//						p.getDiseaseSet().get(inf.key).setBehaviourNode(myWorld.infectiousFramework.getEntryPoint());
//						myWorld.schedule.scheduleOnce(p.getDiseaseSet().get(inf.key), myWorld.param_schedule_infecting);
//					}
					break;
				}
				case DUMMY_INFECTIOUS:{
					if(!p.getDiseaseSet().containsKey(inf.key) && myWorld.random.nextDouble() < beta){
						p.getDiseaseSet().put(inf.key, new DummyInfectiousDisease(p, this, myWorld.dummyInfectiousFramework.getEntryPoint(), myWorld));
						myWorld.schedule.scheduleOnce(p.getDiseaseSet().get(inf.key), myWorld.param_schedule_infecting);
					}
				break;
				}
				default:
					break;
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
	public boolean isHome(){ return currentLocation == homeLocation;}
	
	public void setAtWork(boolean isAtWork) {
		this.atWork = isAtWork;
	}
	public boolean atWorkNow() {
		return this.atWork;
	}

	public Location getCommunityLocation(){ return communityLocation;}
	
	public Location getHomeLocation() {return homeLocation;}

	public Location getWorkLocation() { 
		return workLocation; 
	}
	
	public boolean visitingNow() { return this.visiting; }
	public void setVisiting(boolean visiting) { this.visiting = visiting; }

	public void sendHome() {
		this.transferTo(getHomeLocation());
		this.setActivityNode(myWorld.movementFramework.getEntryPoint());
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
	
	public int getBirthday() {return birthday; }
	public OCCUPATION getEconStatus(){ return economic_status;}
	public Location getWorkplace(){ return myWorkplace; }

		
	public void setMobility(boolean mobile){ this.immobilised = !mobile; }
	public boolean isImmobilised(){ return this.immobilised; }


	public boolean isDeadFromOther() { return this.isDeadFromOther; }
	
	public boolean getDeathLogged () { return this.deathLogged; }
	public boolean gaveBirthLastYear() { return this.gaveBirthLastYear; }
	public int getDateGaveBirth() { return this.dayGaveBirth; }
	public boolean getBirthLogged() { return this.birthLogged; }
	public int getNumberOfTimesInfected() { return this.numberOfTimesWithCovid; }

	public boolean isAlive() { return !this.isDead; }

	
	public boolean isSchoolGoer() { return this.schoolGoer; }

	public String getCurrentAdminZone() {return this.getHomeLocation().getRootSuperLocation().myId;}
	public void setUnemployed() {this.isUnemployed = true;}
	public boolean isUnemployed() {return this.isUnemployed;}  
	
	public void setNumberOfWorkplaceInteractions(int n) {this.number_of_interactions_at_work = n;}
	public int getNumberOfWorkplaceInteractions() {return this.number_of_interactions_at_work;}
	
	public int getNumberOfWorkplaceInteractionsHappened() {return this.number_of_interactions_at_work_happened;}
	
	public void setNumberOfCommunityInteractions(int n) {this.number_of_interactions_in_community_per_day = n;}
	public int getNumberOfCommunityInteractions() {return this.number_of_interactions_in_community_per_day;}
	public int getNumberOfCommunityInteractionsHappened() {return this.number_of_interactions_at_community_happened;}

	
	public void addCommunityContact() {
		this.number_of_interactions_at_community_happened += 1;
	}
	public void addWorkplaceContact() {
		this.number_of_interactions_at_work_happened += 1;
	}
	
	public void resetCommunityContacts() { 
		this.number_of_interactions_in_community_per_day = Integer.MIN_VALUE;
		this.number_of_interactions_at_community_happened = 0;
		setWentToCommunityToday(false);

		}


	public void resetWorkplaceContacts() { 
		this.number_of_interactions_at_work = Integer.MIN_VALUE;
		this.number_of_interactions_at_work_happened = 0;
		setWentToWorkToday(false);
		}
	
	public void setWentToWorkToday(boolean val) {
		this.wentToWorkToday = val;
	}
	public boolean getWentToWorkToday() {
		return this.wentToWorkToday;
	}
	public void setWentToCommunityToday(boolean val) {
		this.wentToCommunityToday = val;
	}
	public boolean getWentToCommunityToday() {
		return this.wentToCommunityToday;
	}
	// UTILS
	
	public String toString(){ return "P_" + this.myId;}
	// for use in the HashCode!!
	public int hashCode(){ return myId; }
	
	public boolean equals(Object o){
		if(! (o instanceof Person)) return false;
		return ((Person) o).myId == this.myId;
	}
	public void confirmDeathLogged() { this.deathLogged = true; }
	public boolean isPregnant() {
		if (getSex().equals(SEX.MALE)) {
			return false;
		}
		else {
			return isPregnant;
		}
	}

	public void setPregnant(boolean isPregnant) {
		if (getSex().equals(SEX.FEMALE)) {
			this.isPregnant = isPregnant;
		}
	}
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
	
	
	public Household getHouseholdAsType() {
		
		return (Household) this.homeLocation;
	}
	
	public boolean hasSymptomaticCovid() {
		if (this.getDiseaseSet().containsKey(DISEASE.COVID.key)) {
			if (!this.getDiseaseSet().get(DISEASE.COVID.key).getBehaviourName().equals("asymptomatic"))
			return true;
		}
		return false;
	}

	@Override
	public String getHostType() {
		return HOST.PERSON.key;
	}

	public SEX getSex() {
		return sex;
	}

	public int getAge() {
		return age;
	}

	public int getID(){ return this.myId; }
	
	@Override
	public boolean isOfType(HOST host) {
		if (host.equals(HOST.PERSON)) return true;
		
		return false;
	};
	
	public void setWaterGatherer() {
		isWaterGatherer = true;
	}
	
	public boolean getWaterGatherer() {
		return isWaterGatherer;
	}
	
	public boolean isAdult() {
		return this.age > 18;
	}

	public void fetchWater(Water waterFrom, Water waterTo) {
		// fetch the water, assume that this is only collection of water and not consuming it.
		if (waterFrom.getDiseaseSet().size() > 0) {
			for (String diseaseName: waterFrom.getDiseaseSet().keySet()) {
				// check if this water is clean:
				boolean cleanWater = waterFrom.getDiseaseSet().get(diseaseName).getCurrentBehaviourNode().getTitle().equals(CholeraBehaviourNodeInWater.CLEAN.key);
				if (!cleanWater) {
					// if a cholera infection exists in the other watersource, transfer over the behaviour node to represent transferring water over
					if (waterTo.getDiseaseSet().containsKey(diseaseName)) {
						waterTo.getDiseaseSet().get(diseaseName).setBehaviourNode(waterFrom.getDiseaseSet().get(diseaseName).getCurrentBehaviourNode());
					}
					// if there is no cholera present in the other water source, create a new instance of cholera in that water source, scheduling it in the next
					// tick
					else {					
						double time = myWorld.schedule.getTime(); 
						Cholera inf = new Cholera(waterTo, waterFrom, waterFrom.getDiseaseSet().get(diseaseName).getCurrentBehaviourNode(), myWorld);
						myWorld.schedule.scheduleOnce(time + 1, myWorld.param_schedule_infecting, inf);
					}
				}
			}
		}
		// potentially contaminate the water source
		if (this.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)) {
			double rand_to_shed = myWorld.random.nextDouble();
			if (rand_to_shed < myWorld.params.cholera_prob_shed) {
				// check if the water source already has cholera
				if (waterFrom.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)){
					// already cholera object here, change it to the entry point for water from humans
					waterFrom.getDiseaseSet().get(DISEASE.CHOLERA.key).setBehaviourNode(myWorld.choleraFramework.getStandardEntryPointForWater());
				}
				else {
					double time = myWorld.schedule.getTime(); 
					// create a new cholera object in the water source
					Cholera inf = new Cholera(waterFrom, this, myWorld.choleraFramework.getStandardEntryPointForWater(), myWorld);
					myWorld.schedule.scheduleOnce(time, myWorld.param_schedule_infecting, inf);
				}
			}
		}
		
	}
}
