package uk.ac.ucl.protecs.objects.locations;

import java.util.ArrayList;

import uk.ac.ucl.protecs.objects.hosts.Water;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

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
	double percentAdminZoneServed;
	
	public CommunityLocation(String id, Location l, String locationTypeToSet, boolean isAWaterSource, double percentServed) {
		super();
		myId = id;
		mySuperLocation = l;
		locationType = LocationCategoryCategory.getValue(locationTypeToSet);
		percentAdminZoneServed = percentServed;
		setWaterSource(isAWaterSource);
		setLocationType(LocationCategory.COMMUNITY);
	}
	
	public double getPercentServed() {
		return this.percentAdminZoneServed;
	}
	
	public void createWaterAtThisSource(WorldBankCovid19Sim world) {
		// create a new water source
		Water communityWater = new Water(this, this, world);
		// update the household to show that people can interact with water here
		this.setWaterSource(true);
		// link the house to the water object
		this.setWaterHere(communityWater);
		try {
			world.waterSourcesToAdminBoundary.get(this.getRootSuperLocation()).add(communityWater);
		}
		catch (NullPointerException e) {
			world.waterSourcesToAdminBoundary.put(this.getRootSuperLocation(), new ArrayList<Water>());
			world.waterSourcesToAdminBoundary.get(this.getRootSuperLocation()).add(communityWater);
		}
		// update the water in the simulation
		world.waterInSim.add(communityWater);
		// schedule the water to activate in the simulation
		world.schedule.scheduleOnce(0, world.param_schedule_movement, communityWater);
	}

}
