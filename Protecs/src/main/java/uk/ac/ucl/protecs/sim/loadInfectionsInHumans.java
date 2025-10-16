package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.CholeraDiseaseProgressionFramework;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.CoronavirusDiseaseProgressionFramework;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.DummyInfectiousDiseaseProgressionFramework;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.DummyNonCommunicableDiseaseProgressionFramework;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.DummyWaterborneDiseaseProgressionFramework;
import uk.ac.ucl.protecs.behaviours.diseaseSpread.DummyNCDOnset;
import uk.ac.ucl.protecs.behaviours.diseaseSpread.DummyNCDOnset.causeDummyNCDs;
import uk.ac.ucl.protecs.objects.diseases.Cholera;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusInfection;
import uk.ac.ucl.protecs.objects.diseases.DummyInfectiousDisease;
import uk.ac.ucl.protecs.objects.diseases.DummyNonCommunicableDisease;
import uk.ac.ucl.protecs.objects.diseases.DummyWaterborneDisease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Water;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class loadInfectionsInHumans{
	
	
	static void seed_infections_in_humans(WorldBankCovid19Sim world) {
		boolean usingDummyNCD = false;
			
		for (DISEASE d: DISEASE.values()) {
			HashMap<Location, Integer> diseaseToSeed = world.params.lineList.get(d);
			for(Location l: diseaseToSeed.keySet()){
				
				// activate this location
				l.setActive(true);
				
				// number of people to infect
				int countInfections = diseaseToSeed.get(l) * world.params.lineListWeightingFactor;
				
				// list of infected people
				HashSet <Person> newlyInfected = new HashSet <Person> ();
				
				// number of people present
				ArrayList <Person> peopleHere = world.personsToAdminBoundary.get(l);
				int numPeopleHere = peopleHere.size();//l.getPeople().size();
				if(numPeopleHere == 0){ // if there is no one there, don't continue
					System.out.println("WARNING: attempting to initialise infection in Location " + l.getId() + " but there are no People present. Continuing without successful infection...");
					continue;
				}
		
				// schedule people here
				//for(Person p: peopleHere)
				//	schedule.scheduleRepeating(0, p);
					
				int collisions = 100; // to escape while loop in case of troubles
	
				// infect until you have met the target number of infections
				while(newlyInfected.size() < countInfections && collisions > 0){
					Person p = peopleHere.get(world.random.nextInt(numPeopleHere));
						
					// check for duplicates!
					if(newlyInfected.contains(p)){
						collisions--;
						continue;
					}
					else // otherwise record that we're infecting this person
						newlyInfected.add(p);
						
					// create new infection
					switch (d) {
					case COVID:{
						if (world.covidInfectiousFramework == null) {
							world.covidInfectiousFramework = new CoronavirusDiseaseProgressionFramework(world);
						}
						if (world.covidInfectiousFramework.covid_infection_age_params == null) {
							world.covidInfectiousFramework.load_infection_params(world.params.dataDir  + world.params.infection_transition_params_filename);
						}
						CoronavirusInfection inf = new CoronavirusInfection(p, null, world.covidInfectiousFramework.getInfectedEntryPoint(l), world, 0);						
						if (inf.getBehaviourName().equals("asymptomatic")) {
							inf.setAsympt();
						}
						else {
							inf.setMild();
						}
						world.schedule.scheduleOnce(1, world.param_schedule_infecting, inf);
						break;
					}
					case DUMMY_NCD:{
						if (world.dummyNCDFramework == null) {
							world.dummyNCDFramework = new DummyNonCommunicableDiseaseProgressionFramework(world);
							}
						DummyNonCommunicableDisease inf = new DummyNonCommunicableDisease(p, p, world.dummyNCDFramework.getStandardEntryPoint(), world, 0);			
						world.schedule.scheduleOnce(1, world.param_schedule_infecting, inf);
						usingDummyNCD = true;
						break;
					}
					case DUMMY_INFECTIOUS:{
						if (world.dummyInfectiousFramework == null) {
							world.dummyInfectiousFramework = new DummyInfectiousDiseaseProgressionFramework(world);
						}
						DummyInfectiousDisease inf = new DummyInfectiousDisease(p, null, world.dummyInfectiousFramework.getStandardEntryPoint(), world, 0);			
						world.schedule.scheduleOnce(1, world.param_schedule_infecting, inf);
						break;
					}
					case DUMMY_WATERBORNE:{
						if (world.dummyWaterborneFramework == null) {
							world.dummyWaterborneFramework = new DummyWaterborneDiseaseProgressionFramework(world);
							}
						DummyWaterborneDisease inf = new DummyWaterborneDisease(p, null, world.dummyWaterborneFramework.getStandardEntryPoint(), world, 0);			
						world.schedule.scheduleOnce(1, world.param_schedule_infecting, inf);
						break;
					}
					case CHOLERA:{
						if (world.choleraFramework == null) {
							world.choleraFramework = new CholeraDiseaseProgressionFramework(world);
							}
						Cholera inf = new Cholera(p, null, world.choleraFramework.getStandardEntryPoint(), world, 0);
						world.schedule.scheduleOnce(1, world.param_schedule_infecting, inf);
						}
						default:
							break;
						}
						
					}
							
			}
			}
			if (usingDummyNCD) {
				DummyNCDOnset myDummyNCD = new DummyNCDOnset();
				DummyNCDOnset.causeDummyNCDs dummyNCDtrigger = myDummyNCD.new causeDummyNCDs(world);
				world.schedule.scheduleRepeating(dummyNCDtrigger, world.param_schedule_infecting, world.params.ticks_per_month);
			}
			
		}
}