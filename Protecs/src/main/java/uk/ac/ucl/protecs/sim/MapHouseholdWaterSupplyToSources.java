package uk.ac.ucl.protecs.sim;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import uk.ac.ucl.protecs.objects.hosts.Water;
import uk.ac.ucl.protecs.objects.locations.CommunityLocation;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;

public class MapHouseholdWaterSupplyToSources{
	
	static void mapWaterToSource(WorldBankCovid19Sim world) {
		// print out logging messages
		System.out.println("Beginning to map household water supply to their sources...");
		// iterate over the admin zones in the simulation
		
		for (Location adminZone: world.adminBoundaries) {
			// create a way to map the locations to the cumulative percentage of the amount of water they supply
			// create a holder
			HashMap<CommunityLocation, Double> sourceToPercent = new HashMap <CommunityLocation, Double>();
			double percentServedIter = 0.0;
			for (CommunityLocation loc: world.communityLocations) {
				if (loc.isWaterSource()){
					// only work on locations in this zone
					if (loc.getRootSuperLocation().equals(adminZone)) {
						percentServedIter += loc.getPercentServed();
						sourceToPercent.put(loc, percentServedIter);
					}
				}
			}
			// sort the values of this hashmap
			Map<CommunityLocation, Double> sortedSourceToPercent = sourceToPercent.entrySet().stream()
			        .sorted(Map.Entry.comparingByValue())
			        .collect(Collectors.toMap(
			                Map.Entry::getKey,
			                Map.Entry::getValue,
			                (a, b) -> { throw new AssertionError(); },
			                LinkedHashMap::new
			        ));
			// now create water for each home and then map it to a communal source
			for (Household home: world.households) {
				// only work on locations in this zone
				if (home.getRootSuperLocation().equals(adminZone)) {
					// create a random number to choose a source
					double rand_for_choosing = world.random.nextDouble();
					// iterate over the water sources
					for (CommunityLocation source: sortedSourceToPercent.keySet()) {
						// randomly select a community water source to provide water to this household
						if (rand_for_choosing < sourceToPercent.get(source)) {
							// Access the household water object
							Water householdWater = home.getWater();
							// set the source
							householdWater.setSource(source);
							// schedule the water to activate in the simulation
							world.schedule.scheduleOnce(0, world.param_schedule_movement, householdWater);
						}
					}
				}
			}
			
		}
		}
	}
