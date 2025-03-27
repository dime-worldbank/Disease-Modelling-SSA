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


public class dummyNCDOnset {

	
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
		List<Double> riskFactors = new ArrayList<>(myWorld.agents.size());
		for (int i = 0; i < myWorld.agents.size(); i++) {
			riskFactors.add(1.0); // Add the value 1.0 to the list
		}
		// Don't create new NCD for those who aren't alive
		int aliveCheckIdx = 0;
		for (Person p: myWorld.agents) {
			if (!p.isAlive()) {
				riskFactors.set(aliveCheckIdx, 0.0);
			}
			aliveCheckIdx++;
		}
		// Don't create new NCD for those who already have it
		int alreadyHasDummyNCDIdx = 0;
		for (Person p: myWorld.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.DUMMY_NCD.key)) {
				riskFactors.set(alreadyHasDummyNCDIdx, 0.0);
			}
			alreadyHasDummyNCDIdx++;
		}
		// inflate the risk of developing this NCD if the person is male
		int maleCheckIdx = 0;
		for (Person p: myWorld.agents) {
			if (p.getSex().equals(SEX.MALE)) {
				riskFactors.set(maleCheckIdx, riskFactors.get(maleCheckIdx) * myWorld.params.dummy_ncd_rr_male);
			}
			maleCheckIdx++;
		}
		// inflate the risk of developing this NCD if over 50
		int ageCheckIdx = 0;
		for (Person p: myWorld.agents) {
			if (p.getAge() > 50) {
				riskFactors.set(ageCheckIdx, riskFactors.get(ageCheckIdx) * myWorld.params.dummy_ncd_rr_over_50);
			}
			ageCheckIdx++;
		}
		// get the base rate of developing this NCD for all in population and apply the relevant factors
		riskFactors = riskFactors.stream().map(value -> value * myWorld.params.dummy_ncd_base_rate).collect(Collectors.toList());
		// iterate over the population and create NCDs via random number generation
		int developNCDIndex = 0;
		ArrayList <Person> confirmedDevelopedCases = new ArrayList <Person>();

		for (Person p: myWorld.agents) {
			// determine if they will develop the NCD, do this by REMOVING those who draw a random number associated with them that's bigger
			// than the base rate adjusted by risk factors
			if (myWorld.random.nextDouble() < riskFactors.get(developNCDIndex)) {
				 confirmedDevelopedCases.add(p);
			 }
							 
			developNCDIndex++;
		 }
		// create the NCDs for those who have confirmed their developed case
		createNCD(myWorld, confirmedDevelopedCases);
		
		
		
	}

	private void createNCD(WorldBankCovid19Sim myWorld, ArrayList <Person> personsToDevelopNCD) {
		for (Person target: personsToDevelopNCD) {
		DummyNonCommunicableDisease inf = new DummyNonCommunicableDisease(target, target, myWorld.dummyNCDFramework.getStandardEntryPoint(), myWorld);
		myWorld.schedule.scheduleOnce(inf);
		}
	}	
	

}

	
	
