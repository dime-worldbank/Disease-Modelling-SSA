package main.java.objects;

import main.java.sim.Params;
import main.java.sim.WorldBankCovid19Sim;
import sim.engine.SimState;
import sim.engine.Steppable;
import swise.agents.MobileAgent;
import swise.behaviours.BehaviourNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Stream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.java.behaviours.MovementBehaviourFramework;


public class Person extends MobileAgent {
  int myId;
  
  Household myHousehold;
  
  int age;
  String sex;
  String economic_status;
  Location currentLocation;
  boolean schoolGoer = false;
  Location communityLocation;
  HashSet<Person> workBubble;
  HashSet<Person> communityBubble;
  BehaviourNode currentActivityNode = null;
  
  Infection myInfection = null;
  
  boolean immobilised = false;
  boolean visiting = false;
  boolean atWork = false;
  
  WorldBankCovid19Sim myWorld;
  
  boolean isDead = false;
  boolean hasCovid = false;
  
  boolean asymptomatic = false;
  
  boolean presymptomatic = false;
  
  boolean mild = false;
  
  boolean severe = false;
  
  boolean critical = false;
  
  boolean recovered = false;
  
  boolean hadCovid = false;
  
  public boolean elligableForTesting = false;
  
  boolean hasBeenTested = false;
  
  boolean hasTestedPositive = false;
  
  boolean hasSpuriousSymptoms = false;
  
  public int timeToRemoveSymptoms = 100000000;
//properties that relate to seroprev update
  boolean hasCovidAntibodies = false;
  public int timeToRemoveAntibodies = 100000000;
  boolean hasCovidResistance = false;
  public int timeToRemoveCovidResistance = 100000000;
  
  public Person(int id, int age, String sex, String economic_status, boolean schoolGoer, Household hh, WorldBankCovid19Sim world) {
    this.myId = id;
    this.age = age;
    this.sex = sex;
    this.economic_status = economic_status;
    this.schoolGoer = schoolGoer;
    this.myHousehold = hh;
    this.myWorld = world;
    this.communityLocation = this.myHousehold.getRootSuperLocation();
    this.workBubble = new HashSet<>();
    this.communityBubble = new HashSet<>();
    this.currentLocation = hh;
  }
  
  public void step(SimState world) {
    if (this.isDead)
      return; 
    if (this.immobilised)
      return; 
    double time = world.schedule.getTime();
    double myDelta = this.currentActivityNode.next((Steppable)this, time);
    if (myDelta >= 0.0D) {
      this.myWorld.schedule.scheduleOnce(time + myDelta, WorldBankCovid19Sim.param_schedule_movement, (Steppable)this);
    } else {
      this.myWorld.schedule.scheduleOnce((Steppable)this, WorldBankCovid19Sim.param_schedule_movement);
    } 
  }
  
  Person myWrapper() {
    return this;
  }
  
  public double transferTo(Location l) {
    if (this.currentLocation != null)
      this.currentLocation.removePerson(this); 
    this.currentLocation = l;
    if (l != null)
      l.addPerson(this); 
    return 1.0D;
  }
  
  public void die() {
    this.isDead = true;
    transferTo(null);
    System.out.println(String.valueOf(toString()) + " has DIED :(");
  }
  
  public void infectNeighbours() {
    if (this.isDead)
      return; 
    if (this.currentLocation == null)
      return; 
    if (this.myInfection == null) {
      System.out.println("ERROR: " + this.myId + " asked to infect others, but is not infected!");
      return;
    } 
    if (this.myWorld.params.setting_perfectMixing) {
      Object[] peopleHere = this.currentLocation.getPersonsHere();
      int numPeople = peopleHere.length;
      double someInteractions = Params.community_num_interaction_perTick;
      if (this.atWork)
        someInteractions = ((Double)this.myWorld.params.economic_num_interactions_weekday_perTick.get(this.economic_status)).doubleValue(); 
      double myNumInteractions = Math.min((numPeople - 1), someInteractions);
      double diff = myNumInteractions - Math.floor(myNumInteractions);
      if (this.myWorld.random.nextDouble() < diff)
        myNumInteractions = Math.ceil(myNumInteractions); 
      HashSet<Person> otherPeople = new HashSet<>();
      otherPeople.add(this);
      for (int i = 0; i < myNumInteractions; i++) {
        Person otherPerson = (Person)peopleHere[this.myWorld.random.nextInt(numPeople)];
        if (otherPeople.contains(otherPerson)) {
          i--;
        } else {
          otherPeople.add(otherPerson);
          this.myWorld.testingAgeDist.add(Integer.valueOf(otherPerson.age));
          double myProb = this.myWorld.random.nextDouble();
          if (otherPerson.myInfection == null && 
            myProb < this.myWorld.params.infection_beta) {
            Infection inf = new Infection(otherPerson, this, this.myWorld.infectiousFramework.getHomeNode(), this.myWorld);
            this.myWorld.schedule.scheduleOnce(inf, WorldBankCovid19Sim.param_schedule_infecting);
          } 
        } 
      } 
      return;
    } 
    System.out.println("ERROR: structured mixing under revision");
  }
  
  void OLDinteractWithin(HashSet<Person> group, HashSet<Person> largerCommunity, int interactNumber) {
    Object[] checkWithin = group.toArray();
    int sizeOfCommunity = group.size();
    boolean largerCommunityContext = (largerCommunity != null);
    HashSet<Integer> indicesChecked = new HashSet<>();
    for (int i = 0; i < interactNumber; i++) {
      if (indicesChecked.size() >= sizeOfCommunity)
        return; 
      int j = this.myWorld.random.nextInt(sizeOfCommunity);
      if (indicesChecked.contains(Integer.valueOf(j))) {
        i--;
        continue;
      } 
      indicesChecked.add(Integer.valueOf(j));
      Person p = (Person)checkWithin[j];
      if (p == this) {
        i--;
        continue;
      } 
      if (largerCommunityContext)
        if (!largerCommunity.contains(p))
          continue;  
      if (p.myInfection == null && 
        this.myWorld.random.nextDouble() < this.myWorld.params.infection_beta) {
        Infection inf = new Infection(p, this, this.myWorld.infectiousFramework.getHomeNode(), this.myWorld);
        this.myWorld.schedule.scheduleOnce(inf, WorldBankCovid19Sim.param_schedule_infecting);
      } 
      continue;
    } 
  }
  
  void interactWithin(HashSet<Person> group, HashSet<Person> largerCommunity, int interactNumber) {
    boolean largerCommunityContext = (largerCommunity != null);
    double d = group.size();
    double n = interactNumber;
    double cutOff = n / d;
    Iterator<Person> myIt = group.iterator();
    while (myIt.hasNext() && n > 0.0D) {
      double prob = this.myWorld.random.nextDouble();
      if (prob <= cutOff) {
        Person p = myIt.next();
        if (p == this)
          continue; 
        if (largerCommunityContext && !largerCommunity.contains(p))
          continue; 
        n--;
        if (p.myInfection == null && 
          this.myWorld.random.nextDouble() < this.myWorld.params.infection_beta) {
          Infection inf = new Infection(p, this, this.myWorld.infectiousFramework.getHomeNode(), this.myWorld);
          this.myWorld.schedule.scheduleOnce(inf, WorldBankCovid19Sim.param_schedule_infecting);
        } 
      } else {
        myIt.next();
      } 
      d--;
      cutOff = n / d;
    } 
  }
  
  public void setLocation(Location l) {
    if (this.currentLocation != null)
      this.currentLocation.removePerson(this); 
    this.currentLocation = l;
    l.addPerson(this);
  }
  
  public Location getLocation() {
    return this.currentLocation;
  }
  
  public boolean isHome() {
    return (this.currentLocation == this.myHousehold);
  }
  
  public Location getCommunityLocation() {
    return this.communityLocation;
  }
  
  public boolean atWorkNow() {
    return this.atWork;
  }
  
  public void setAtWork(boolean atWork) {
    this.atWork = atWork;
  }
  
  public boolean visitingNow() {
    return this.visiting;
  }
  
  public void setVisiting(boolean visiting) {
    this.visiting = visiting;
  }
  
  public void sendHome() {
    transferTo(this.myHousehold);
    setActivityNode(this.myWorld.movementFramework.getHomeNode());
  }
  
  public void addToWorkBubble(Collection<Person> newPeople) {
    this.workBubble.addAll(newPeople);
  }
  
  public HashSet<Person> getWorkBubble() {
    return this.workBubble;
  }
  
  public void setWorkBubble(HashSet<Person> newBubble) {
    this.workBubble = newBubble;
  }
  
  public void addToCommunityBubble(Collection<Person> newPeople) {
    this.communityBubble.addAll(newPeople);
  }
  
  public HashSet<Person> getCommunityBubble() {
    return this.communityBubble;
  }
  
  public void setCommunityBubble(HashSet<Person> newBubble) {
    this.communityBubble = newBubble;
  }
  
  public double getSusceptibility() {
    return this.myWorld.params.getSuspectabilityByAge(this.age);
  }
  
  public void setActivityNode(BehaviourNode bn) {
    this.currentActivityNode = bn;
  }
  
  public int getAge() {
    return this.age;
  }
  
  public String getEconStatus() {
    return this.economic_status;
  }
  
  public Location getHousehold() {
    return this.myHousehold;
  }
  
  public boolean hasAsymptCovid() {
    return this.asymptomatic;
  }
  
  public boolean hasPresymptCovid() {
    return this.presymptomatic;
  }
  
  public boolean hasCovid() {
    return this.hasCovid;
  }
  
  public boolean hasMild() {
    return this.mild;
  }
  
  public boolean hasSevere() {
    return this.severe;
  }
  
  public boolean hasCritical() {
    return this.critical;
  }
  
  public boolean hasRecovered() {
    return this.recovered;
  }
  
  public boolean hasBeenTested() {
    return this.hasBeenTested;
  }
  
  public boolean hasTestedPos() {
    return this.hasTestedPositive;
  }
  
  public boolean isElligableForTesting() {
    return this.elligableForTesting;
  }
  
  public boolean hasSpuriousSymptoms() {
    return this.hasSpuriousSymptoms;
  }
  
  public boolean inADistrictTesting() {
    boolean answer = this.myWorld.params.districts_to_test_in.stream().anyMatch(x -> x.equals((this.myHousehold.getRootSuperLocation()).myId));
    return answer;
  }
  
  public boolean inASamplingDistrict() {
		boolean answer = this.myWorld.params.sero_districts_to_test_in.stream().anyMatch(x -> x.equals(this.myHousehold.getRootSuperLocation().myId));
		return answer;
	}	
  
  public void setInfection(Infection i) {
    this.myInfection = i;
  }
  
  public Infection getInfection() {
    return this.myInfection;
  }
  
  public void setMobility(boolean mobile) {
    this.immobilised = !mobile;
  }
  
  public boolean isImmobilised() {
    return this.immobilised;
  }
  
  public boolean isDead() {
    return this.isDead;
  }
  
  public boolean isSchoolGoer() {
    return this.schoolGoer;
  }
  
  public void storeCovid() {
    this.hasCovid = true;
    this.hadCovid = true;
  }
  
  public void setAsympt() {
    this.asymptomatic = true;
  }
  
  public void setPresympt() {
    this.presymptomatic = true;
  }
  
  public void removePresympt() {
    this.presymptomatic = false;
  }
  
  public void setMild() {
    this.mild = true;
  }
  
  public void removeMild() {
    this.mild = false;
  }
  
  public void setSevere() {
    this.severe = true;
  }
  
  public void removeSevere() {
    this.severe = false;
  }
  
  public void setCritical() {
    this.critical = true;
  }
  
  public void setRecovered() {
    this.recovered = true;
  }
  
  public void elligableForTesting() {
    this.elligableForTesting = true;
  }
  
  public void notElligableForTesting() {
    this.elligableForTesting = false;
  }
  
  public void setTested() {
    this.hasBeenTested = true;
  }
  
  public void setTestedPositive() {
    this.hasTestedPositive = true;
  }
  
  public void setSymptomRemovalDate(int time) {
    this.timeToRemoveSymptoms = time;
  }
  
  public void removeTestedPositive() {
    this.hasTestedPositive = false;
  }
  
  public void removeCovid() {
    this.asymptomatic = false;
    this.mild = false;
    this.severe = false;
    this.critical = false;
    this.hasCovid = false;
  }
  
  public void removeSpuriousSymptoms() {
    this.hasSpuriousSymptoms = false;
  }
  
  public void setSpuriousSymptoms() {
    this.hasSpuriousSymptoms = true;
  }
  
  public void removeCovidAntibodies() { 
		this.hasCovidAntibodies = false;
	}
	public void setCovidAntibodies() {
		this.hasCovidAntibodies = true;
	}
	public void removeCovidResistance() { 
		this.hasCovidResistance = false;
	}
	public void setCovidResistance() {
		this.hasCovidResistance = true;
	}
  public void setCovidAntibodyRemovalDate(int time) { this.timeToRemoveAntibodies = time; }

  public void setCovidResistanceRemovalDate(int time) { this.timeToRemoveCovidResistance = time; }

  public String getCurrentDistrict() {
    return (getHousehold().getRootSuperLocation()).myId;
  }
  
  public String toString() {
    return "P_" + this.myId;
  }
  
  public int getID() {
    return this.myId;
  }
  
  public int hashCode() {
    return this.myId;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof Person))
      return false; 
    return (((Person)o).myId == this.myId);
  }

	public Household getHouseholdAsType() {
		
		return this.myHousehold;
	}
}
