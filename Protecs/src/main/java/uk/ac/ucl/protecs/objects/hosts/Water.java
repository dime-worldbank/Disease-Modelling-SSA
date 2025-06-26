package uk.ac.ucl.protecs.objects.hosts;

import java.util.HashMap;
import java.util.HashSet;

import sim.engine.SimState;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.CholeraDiseaseProgressionFramework.CholeraBehaviourNodeInWater;
import uk.ac.ucl.protecs.objects.diseases.Cholera;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.objects.diseases.DummyWaterborneDisease;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.HOST;

public class Water extends Host {
	
	double volume;
	
	Location source;
	public Water(Location myLocation, Location mySource, WorldBankCovid19Sim world){
		currentLocation = myLocation;
		myDiseaseSet = new HashMap <String, Disease>();
		source = mySource;
		myWorld = world;

	}
	
	

	@Override
	public void step(SimState arg0) {
		// Interact with water here
		interactWithWater();
	}
	
	private void interactWithWater() {
		// find the current time
		double time = myWorld.schedule.getTime(); 
		for (Person p: this.getLocation().getPeople()) {
			double randomToInteractWithWater = myWorld.random.nextDouble();
			if (randomToInteractWithWater < myWorld.params.dummy_prob_interact_with_water) {
				// skip over step if no infection is present in water/person
				boolean thisPersonHasDummyWaterborne = p.myDiseaseSet.containsKey(DISEASE.DUMMY_WATERBORNE.key);
				
				boolean thisPersonHasCholera = p.myDiseaseSet.containsKey(DISEASE.CHOLERA.key);
				
				boolean waterIsContaminatedByDummy = (this.getDiseaseSet().containsKey(DISEASE.DUMMY_WATERBORNE.key));
				
				boolean waterIsContaminatedByCholera = (this.getDiseaseSet().containsKey(DISEASE.CHOLERA.key));

				// if this person has the dummy water born infection and the location doesn't have the dummy water born infection, 
				// potentially cause a new infection in the water
				if (thisPersonHasDummyWaterborne & !waterIsContaminatedByDummy) {
					double randomToShedIntoWater = myWorld.random.nextDouble();
					// check if the person interacts and sheds into water
					if (randomToShedIntoWater < myWorld.params.dummy_waterborne_prob_shed_into_water) {
						
						DummyWaterborneDisease inf = new DummyWaterborneDisease(this, p, myWorld.dummyWaterborneFramework.getStandardEntryPointForWater(), myWorld);
						myWorld.schedule.scheduleOnce(time, myWorld.param_schedule_infecting, inf);
						
					}
				}
				
				if (!thisPersonHasDummyWaterborne & waterIsContaminatedByDummy) {
					double randomToIngestInfection = myWorld.random.nextDouble();
					// check if the person interacts and ingests sufficient amounts of cholera to get an infection
					if (randomToIngestInfection < myWorld.params.dummy_prob_ingest_dummy_waterborne) {
						DummyWaterborneDisease inf = new DummyWaterborneDisease(p, this, myWorld.dummyWaterborneFramework.getStandardEntryPoint(), myWorld);
						myWorld.schedule.scheduleOnce(time, myWorld.param_schedule_infecting, inf);
						
					}
				}
				// if this person has cholera  and the location doesn't have cholera, 
				// potentially cause a new infection in the water
				if (thisPersonHasCholera & !waterIsContaminatedByCholera) {
					double randomToShedIntoWater = myWorld.random.nextDouble();
					// check if the person interacts and sheds into water
					if (randomToShedIntoWater < myWorld.params.cholera_prob_shed) {
						
						Cholera inf = new Cholera(this, p, myWorld.choleraFramework.getStandardEntryPointForWater(), myWorld);
						myWorld.schedule.scheduleOnce(time, myWorld.param_schedule_infecting, inf);
						
					}
				}
				
				if (!thisPersonHasCholera & waterIsContaminatedByCholera) {
					double randomToIngestInfection = myWorld.random.nextDouble();
					// HERE WE NEED TO ACCOUNT FOR THE HYPERINFECTIOUS STATE OF CHOLERA, DOSE DEPENDENCE ETC..
					// 10^8 needed for severe cholera (https://www.thelancet.com/journals/lancet/article/PIIS0140-6736(03)15328-7/abstract), 
					// in animal studies this is reduced by two orders of magnitude in animal studies (https://wwwnc.cdc.gov/eid/article/17/11/11-1109_article)
					
					// check if the person interacts and ingests sufficient amounts of cholera to get an infection
					if (randomToIngestInfection < myWorld.params.cholera_prob_ingest) {
						Cholera inf = new Cholera(p, this, myWorld.choleraFramework.getStandardEntryPoint(), myWorld);
						myWorld.schedule.scheduleOnce(time, myWorld.param_schedule_infecting, inf);
						
					}
				}
			}
		}
		
	}	
	@Override
	public String getHostType() {	
			return HOST.WATER.key;
		}

	@Override
	public boolean isOfType(HOST host) {
		if (host.equals(HOST.WATER)) return true;
		
		return false;
	};
	
	public Location getSource() {
		return this.source;
	}
	
	public void setSource(Location l) {
		this.source = l;
	}
	
}