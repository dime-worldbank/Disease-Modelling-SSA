package uk.ac.ucl.protecs.behaviours.diseaseSpread;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.DummyNonCommunicableDisease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;


public class DummyNCDOnset {

	
	public class causeDummyNCDs implements Steppable{
		int ticksUntilNextNCDOnsetCheck = 0;
		WorldBankCovid19Sim world;
		ArrayList <Person> personsToDevelopNCD = null;

		public causeDummyNCDs(WorldBankCovid19Sim myWorld) {
			// link this NCD disease onset development to simulation
			this.world = myWorld;
			// we'll check every month for this development of new dummy NCD conditions
			this.ticksUntilNextNCDOnsetCheck = world.params.ticks_per_day * 30;
			}
		 
		
		@Override
		public void step(SimState arg0) {
			// determine if anyone will develop a dummy NCD this month
			determineDevelopyNCD(arg0, world);		

			
		}
	}

	private void determineDevelopyNCD(SimState arg0, WorldBankCovid19Sim myWorld) {
		// Create a list to account for risk factors and protective factors against developing the dummy NCD

		for (Person p: myWorld.agents) {
			// create a risk factor for this individual
			double riskFactor = 1;
			// if they aren't alive they can't develop the NCD
			if (!p.isAlive()) {
				riskFactor = 0;
				continue;
			}
			// if they already have the NCD they can't develop the NCD
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) {
				riskFactor = 0;
				continue;
			}
			// if they are male increase the likelihood of developing the NCD 
			if (p.getSex().equals(SEX.MALE)) {
				riskFactor *= myWorld.params.dummy_ncd_rr_male;
			}
			// if they are over 50 increase the likelihood of developing the NCD 
			if (p.getAge() > 50) {
				riskFactor *= myWorld.params.dummy_ncd_rr_over_50;
			}
			// Check if they develop the NCD
			if (myWorld.random.nextDouble() < riskFactor * myWorld.params.dummy_ncd_base_rate) {
				createNCD(myWorld, p);
			 }
		}
	
		
		
	}

	private void createNCD(WorldBankCovid19Sim myWorld, Person personToDevelopNCD) {
		DummyNonCommunicableDisease inf = new DummyNonCommunicableDisease(personToDevelopNCD, personToDevelopNCD, myWorld.dummyNCDFramework.getStandardEntryPoint(), myWorld);
		myWorld.schedule.scheduleOnce(inf);
		
	}	
	

}

	
	
