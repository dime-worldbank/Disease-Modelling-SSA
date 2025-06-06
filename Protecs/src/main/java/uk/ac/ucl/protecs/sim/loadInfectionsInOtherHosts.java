package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.DummyWaterborneDiseaseProgressionFramework;
import uk.ac.ucl.protecs.objects.diseases.DummyWaterborneDisease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Water;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class loadInfectionsInOtherHosts{
	
	
	static void seed_infections_in_others(WorldBankCovid19Sim world) {
		// TODO: discuss with others about how to approach building this, for now I'm just adding water to households

		for (Household h : world.households) {
			// for purposes of development we will set every household to be a source of water
			h.setWaterSource(true);
			// create a new water source
			Water householdWater = new Water(h, h.getRootSuperLocation(), world);
			world.waterInSim.add(householdWater);
			h.setWaterHere(householdWater);
			// schedule the water to activate in the simulation
			world.schedule.scheduleOnce(0, world.param_schedule_movement, householdWater);
		}
			// create a new infection in the water for some households
		for (DISEASE d: DISEASE.values()) {
		HashMap<Location, Integer> diseaseToSeed = world.params.lineListInOther.get(d);
		for(Location l: diseaseToSeed.keySet()){
			// for now just add infections to the water through the person object
			ArrayList <Person> peopleHere = world.personsToAdminBoundary.get(l);
			
			int countInfections = diseaseToSeed.get(l);

			int collisions = 100; // to escape while loop in case of troubles
			// list of infected people
			HashSet <Water> newlyInfected = new HashSet <Water> ();
			
			int numPeopleHere = peopleHere.size();
			// infect until you have met the target number of infections
			while(newlyInfected.size() < countInfections && collisions > 0){
				Person p = peopleHere.get(world.random.nextInt(numPeopleHere));
				
				// check for duplicates!
				if(newlyInfected.contains(p.getHouseholdAsType().getWater())){
					collisions--;
					continue;
				}
				else // otherwise record that we're infecting this person
					newlyInfected.add(p.getHouseholdAsType().getWater());
				
				// create new infection
				switch (d) {
				case COVID:{
					break;
				}
				case DUMMY_NCD:{
					break;
				}
				case DUMMY_INFECTIOUS:{
					break;
				}
				case DUMMY_WATERBORNE:{
					if (world.dummyWaterborneFramework.equals(null)) {
						world.dummyWaterborneFramework = new DummyWaterborneDiseaseProgressionFramework(world);
					}
					DummyWaterborneDisease inf = new DummyWaterborneDisease(p.getHouseholdAsType().getWater(), null, world.dummyWaterborneFramework.getStandardEntryPointForWater(), world, 0);			
					world.schedule.scheduleOnce(1, world.param_schedule_infecting, inf);
					break;
				}
				default:
					break;
				}
				
			}
		}
		}
	}
}