package uk.ac.ucl.protecs.objects.locations;

public class Household extends Location {
		
	public Household(String id, Location l){
		super();
		myId = "HH_" + id;
		mySuperLocation = l;
		setLocationType(LocationCategory.HOME);
	}
}
