package uk.ac.ucl.protecs.objects.locations;


public class CommunityLocation extends Location {
	
	public enum LocationCategoryCategory{
		CHURCH("church"), MARKET("market"), BOREHOLE("borehole"), WELL("well");
		
		public String key;

		LocationCategoryCategory(String key) { this.key = key; }

        public static LocationCategoryCategory getValue(String x) {

        	switch (x) {
        	case "church":
        		return CHURCH;
        	case "borehole":
        		return BOREHOLE;
        	case "well":
        		return WELL;
        	case "market":
        		return MARKET;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}
	LocationCategoryCategory locationType;
	
	public CommunityLocation(String id, Location l, String locationTypeToSet, boolean isAWaterSource) {
		super();
		myId = id;
		mySuperLocation = l;
		locationType = LocationCategoryCategory.getValue(locationTypeToSet);
		setWaterSource(isAWaterSource);
		setLocationType(LocationCategory.COMMUNITY);
	}

}
