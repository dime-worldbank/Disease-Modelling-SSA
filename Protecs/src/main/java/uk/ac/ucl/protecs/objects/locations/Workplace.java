package uk.ac.ucl.protecs.objects.locations;


public class Workplace extends Location {
	
	
	public Workplace(String id, Location l){
		super();
		myId = "wp" + id;
		mySuperLocation = l;
		setLocationType(LocationCategory.WORKPLACE);

	}
}
