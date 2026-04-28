package uk.ac.ucl.protecs.objects.locations;

import uk.ac.ucl.protecs.objects.hosts.Person;

public class Workplace extends Location {
	
	
	public Workplace(String id, Location l){
		super();
		myId = "wp" + id;
		mySuperLocation = l;
		setLocationType(LocationCategory.WORKPLACE);

	}
	
	/**
	 * Add a person to the Household, and in turn set this household as the Person's household.
	 * Returns false if the Person is already a member of the Household.
	 * @param p The Person to add to the Household.
	 * @return whether addition was successful.
	 */
	public boolean addPerson(Person p){
		boolean result = super.addHost(p);
		p.myWorkplace = this;
		return result;
	}

}
