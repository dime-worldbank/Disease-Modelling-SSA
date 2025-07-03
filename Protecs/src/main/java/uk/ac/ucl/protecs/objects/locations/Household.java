package uk.ac.ucl.protecs.objects.locations;
import java.util.HashSet;

import uk.ac.ucl.protecs.objects.hosts.Person;


public class Household extends Location {
	
	HashSet<Person> peopleBelongingToHousehold = new HashSet<Person>();
	
	public Household(String id, Location l){
		super();
		myId = "HH_" + id;
		mySuperLocation = l;
		setLocationType(LocationCategory.HOME);
	}
	
	public void addPersonToHousehold(Person p) {
		peopleBelongingToHousehold.add(p);
	}
	public HashSet<Person> getPeopleBelongingToHousehold(){
		return peopleBelongingToHousehold;
	}
	public void removeDeceasedFromHousehold(Person p) {
		if (!p.isAlive()){
			peopleBelongingToHousehold.remove(p);
		}
	}
}
