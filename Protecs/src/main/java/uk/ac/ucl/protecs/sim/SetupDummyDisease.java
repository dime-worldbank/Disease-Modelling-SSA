package uk.ac.ucl.protecs.sim;

import java.util.Collections;

import uk.ac.ucl.protecs.behaviours.diseaseSpread.DummyNCDOnset;
import uk.ac.ucl.protecs.objects.diseases.DummyInfectiousDisease;
import uk.ac.ucl.protecs.objects.diseases.DummyNonCommunicableDisease;
import uk.ac.ucl.protecs.objects.diseases.DummyWaterborneDisease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Water;
import uk.ac.ucl.protecs.objects.locations.Household;

public class SetupDummyDisease{
	
	public static void SetupDummyDiseases(WorldBankCovid19Sim world) {
		DummyNCDOnset myDummyNCD = new DummyNCDOnset();
		double num_to_seed = world.agents.size() * world.dummyNCDFramework.getDummy_ncd_initial_fraction_with_ncd();
		double i = 0.0;
		for (Person a: world.agents) {
			if (i < num_to_seed) {
			DummyNonCommunicableDisease inf = new DummyNonCommunicableDisease(a, a, world.dummyNCDFramework.getStandardEntryPoint(), world, 0);
			world.schedule.scheduleOnce(1, world.param_schedule_infecting, inf);
			i ++ ;
			}
			else break;
		}
		DummyNCDOnset.causeDummyNCDs dummyNCDtrigger = myDummyNCD.new causeDummyNCDs(world);
		// shuffle the agents so that the first n people won't also get an NCD
		Collections.shuffle(world.agents);
		world.schedule.scheduleRepeating(dummyNCDtrigger, world.param_schedule_infecting, world.params.ticks_per_month);
		i = 0.0;
		for (Person a: world.agents) {
			if (i < num_to_seed) {
			DummyInfectiousDisease inf = new DummyInfectiousDisease(a, null, world.dummyInfectiousFramework.getStandardEntryPoint(), world, 0);
			world.schedule.scheduleOnce(1, world.param_schedule_infecting, inf);
			i ++ ;
			}
			else break;
		}
		double num_hh_to_seed = world.households.size() * world.dummyWaterborneFramework.getDummy_waterborne_initial_fraction_with_inf()
;
		i = 0.0;
		for (Household h : world.households) {
			// for purposes of development we will set every household to be a source of water
			h.setWaterSource(true);
			// create a new water source
			Water householdWater = new Water(h, h.getRootSuperLocation(), world);
			world.waterInSim.add(householdWater);
			h.setWaterHere(householdWater);
			// schedule the water to activate in the simulation
			world.schedule.scheduleOnce(0, world.param_schedule_movement, householdWater);

			// create a new infection in the water for some households
			if (i < num_hh_to_seed) {
				DummyWaterborneDisease diseaseInWater = new DummyWaterborneDisease(householdWater, null, world.dummyWaterborneFramework.getStandardEntryPointForWater(), world, 0);
				world.schedule.scheduleOnce(1, world.param_schedule_infecting, diseaseInWater);
				i ++ ;
			}
		}
		// shuffle the agents so that the first n people won't also get a waterborne infection
		Collections.shuffle(world.agents);
		i = 0.0;
		for (Person a: world.agents) {
			if (i < num_to_seed) {
				DummyWaterborneDisease inf = new DummyWaterborneDisease(a, null, world.dummyWaterborneFramework.getStandardEntryPoint(), world, 0);
				world.schedule.scheduleOnce(1, world.param_schedule_infecting, inf);
			i ++ ;
			}
			else break;
		}
	}
}