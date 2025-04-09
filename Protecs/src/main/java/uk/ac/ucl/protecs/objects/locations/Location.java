package uk.ac.ucl.protecs.objects.locations;

import java.util.HashSet;

import uk.ac.ucl.protecs.objects.hosts.Host;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Water;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.HOST;


/**
 * A generic holder for spatial data. Locations keep track of the spatial location within which they exist.
 * TODO add relationships between/among locations.
 * 
 * @author swise
 */
public class Location {
	
	public String myId;
	Location mySuperLocation; // the Location within which this Location exists
	public HashSet <Person> personsHere;
	public HashSet <Water> allWaterHere;
	public Water waterHere;
	Object [] personsHere_list;
	Object [] waterHere_list;

	// waterborn disease related things
	private boolean isWaterSource;

	LocationCategory myType;
	boolean active = false;
	
	public int metric_died_count;
	public int metric_new_hospitalized;
	public int metric_new_critical;
	public int metric_new_cases_asympt;
	public int metric_new_cases_sympt;
	public int metric_new_deaths;
	public int metric_new_recovered;
	public int metric_currently_infected;
	public enum LocationCategory{
		HOME("home"), WORKPLACE("workplace"), COMMUNITY("community");
		
		public String key;

		LocationCategory(String key) { this.key = key; }

        public static LocationCategory getValue(String x) {

        	switch (x) {
        	case "home":
        		return HOME;
        	case "work":
        		return WORKPLACE;
        	case "community":
        		return COMMUNITY;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}
	
	// CONSTRUCTORS
	
	public Location(String id, Location mySuper){
		myId = id;
		mySuperLocation = mySuper;
		personsHere = new HashSet <Person> ();
		allWaterHere = new HashSet <Water> ();
		waterHere = null;

		
		metric_died_count = 0; 
		metric_new_hospitalized = 0;
		metric_new_critical = 0;
		metric_new_cases_asympt = 0;
		metric_new_cases_sympt = 0;
		metric_new_deaths = 0;
		metric_new_recovered = 0;
		metric_currently_infected = 0;
	}
	
	public Location(Location mySuper){
		this("", mySuper);
	}

	public Location(String id){
		this(id, null);
	}
	
	public Location(){
		this("", null);
	}

	/**
	 * Add a person to the Location. Returns false if the Person is already there.
	 * @param p The Person to add to the Location.
	 * @return whether addition was successful.
	 */
	public boolean addHost(Host h){
		if (h.getHostType().equals(HOST.PERSON.key)){
			if(personsHere.contains(h))
				return false;
			return personsHere.add((Person) h);
		}
		if (h.getHostType().equals(HOST.WATER.key)){
			if(allWaterHere.contains(h))
				return false;
			return allWaterHere.add((Water) h);
		}
		return false;
	}
	
	public boolean removeHost(Host h){
		if (h.getHostType().equals(HOST.PERSON.key)){
			if(personsHere.contains(h))
				return personsHere.remove(h);
		}
		else if (h.getHostType().equals(HOST.WATER.key)){
			if(allWaterHere.contains(h))
				return allWaterHere.remove(h);
		}
		return false;
		
	}
	
	public Object [] getPersonsHere() {
		return personsHere.toArray();
	}
	
	public Location getSuper(){
		return mySuperLocation;
	}
	
	public String getId(){
		return myId;
	}
	
	public Location getRootSuperLocation(){
		Location l = this;
		while(l.getSuper() != null)
			l = l.getSuper();
		return l;
	}
	
	public HashSet <Person> getPeople(){
		return personsHere;
	}

	public void refreshMetrics(){
		metric_currently_infected = metric_currently_infected + metric_new_cases_asympt + metric_new_cases_sympt 
				- metric_new_deaths - metric_new_recovered;
		
		metric_new_hospitalized = 0;
		metric_new_critical = 0;
		metric_new_cases_asympt = 0;
		metric_new_cases_sympt = 0;
		metric_new_deaths = 0;
		metric_new_recovered = 0;
	}
	
	public String metricsToString(){
		String s = myId;
		s += "\t";
		s += metric_died_count + "\t";
		s += metric_new_hospitalized + "\t";
		s += metric_new_critical + "\t";
		s += metric_new_cases_asympt + "\t";
		s += metric_new_cases_sympt + "\t";
		s += metric_new_deaths + "\t";
		s += metric_new_recovered + "\t";
		s += metric_currently_infected + "\t";
		return s;
	}
	
	public static String metricNamesToString(){
		String s = "time" + "\t" + "myId" + "\t" + "metric_died_count" + "\t" + "metric_new_hospitalized" + "\t" + 
				"metric_new_critical" + "\t" + "metric_new_cases_asympt" + "\t" + "metric_new_cases_sympt" + "\t" + 
				"metric_new_deaths" + "\t" + "metric_new_recovered"  + "\t" + "metric_currently_infected" + "\t\n";
		return s;
	}
	
	public void setActive(boolean b) {
		active = b;
	}
	public boolean getActive() { return active; }

	public void setLocationType(LocationCategory type) {
		myType = type;		
	}
	public LocationCategory getLocationType() {return this.myType;}
	

	public boolean isWaterSource() {
		return isWaterSource;
	}

	public void setWaterSource(boolean isWaterSource) {
		this.isWaterSource = isWaterSource;
	}
	
	public HashSet <Water> getWaterHere(){
		return allWaterHere;
	}
	
	public Water getWater() {
		return waterHere;
	}
	
	public void setWaterHere(Water Water) {
		waterHere = Water;
		
	}
}
