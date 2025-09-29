package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.CholeraDiseaseProgressionFramework;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.DummyWaterborneDiseaseProgressionFramework;
import uk.ac.ucl.protecs.objects.diseases.Cholera;
import uk.ac.ucl.protecs.objects.diseases.DummyWaterborneDisease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Water;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class loadInfectionsInOtherHosts{
	
	
	static void seed_infections_in_others(WorldBankCovid19Sim world) {
		// TODO: discuss with others about how to approach building this, for now I'm just adding water to households
		// create a new infection in the water for some households
		for (DISEASE d: waterborneDiseaseInSim(world)) {
			HashMap<Location, Integer> diseaseToSeed = world.params.lineListInOther.get(d);
			for(Location l: diseaseToSeed.keySet()){
				// get the water at this location
				ArrayList<Water> waterHere = world.waterSourcesToAdminBoundary.get(l);
				
				int countInfections = diseaseToSeed.get(l);
	
				int collisions = countInfections + 10; // to escape while loop in case of troubles
				// list of infected people
				HashSet <Water> newlyInfected = new HashSet <Water> ();
				
				int numWaterHere = waterHere.size();
				// infect until you have met the target number of infections
				while(newlyInfected.size() < countInfections && collisions > 0){
					Water w = waterHere.get(world.random.nextInt(numWaterHere));
					// check for duplicates!
					if(newlyInfected.contains(w)){
						collisions--;
						if (collisions < 0) break;
						continue;
					}
					else // otherwise record that we're infecting this person
						newlyInfected.add(w);
					
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
							DummyWaterborneDisease inf = new DummyWaterborneDisease(w, null, world.dummyWaterborneFramework.getStandardEntryPointForWater(), world, 0);			
							world.schedule.scheduleOnce(1, world.param_schedule_infecting, inf);
							break;
						}
						case CHOLERA:{
							if (world.choleraFramework.equals(null)) {
								world.choleraFramework = new CholeraDiseaseProgressionFramework(world);
							}
							Cholera inf = new Cholera(w, null, world.choleraFramework.getStandardEntryPointForWater(), world, 0);
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
	
	private static ArrayList<DISEASE> waterborneDiseaseInSim(WorldBankCovid19Sim world){
			
		ArrayList<DISEASE> toReturn = new ArrayList<DISEASE>();
		if (world.choleraFramework != null) {toReturn.add(DISEASE.CHOLERA);}
		if (world.dummyWaterborneFramework != null) {toReturn.add(DISEASE.DUMMY_WATERBORNE);}

		return toReturn;
	}
}