package objects;

import java.util.ArrayList;

import swise.agents.SpatialAgent;

public class Household extends Location {
	
	String myId;
	
	public Household(String id, Location l){
		super();
		myId = "HH_" + id;
		mySuperLocation = l;
	}
	
	/**
	 * Add a person to the Household, and in turn set this household as the Person's household.
	 * Returns false if the Person is already a member of the Household.
	 * @param p The Person to add to the Household.
	 * @return whether addition was successful.
	 */
	public boolean addPerson(Person p){
		boolean result = super.addPerson(p);
		p.myHousehold = this;
		updatePersonsHere();
		return result;
	}
	
}