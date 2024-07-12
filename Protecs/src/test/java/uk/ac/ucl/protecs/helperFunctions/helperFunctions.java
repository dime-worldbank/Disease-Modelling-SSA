package uk.ac.ucl.protecs.helperFunctions;

import java.util.ArrayList;
import java.util.Random;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusInfection;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import swise.behaviours.BehaviourNode;

public class helperFunctions {
	

	public static WorldBankCovid19Sim CreateDummySim(String paramsFilename) {
		Random rand = new Random();
		int seed = rand.nextInt(100000000);
		return CreateDummySimWithSeed(seed, paramsFilename);
	}
	public static WorldBankCovid19Sim CreateDummySimWithSeed(int seed, String paramsFilename) {
		System.out.println("Running with seed = " + String.valueOf(seed));
		Params p = new Params(paramsFilename, false);
		return new WorldBankCovid19Sim(seed, p, "");
	}
	
	public static void setParameterListsToValue(WorldBankCovid19Sim world, ArrayList <Double> list_to_change, double value_to_set) {
		int list_index = 0;
		for (double entry: list_to_change) {
			list_to_change.set(list_index, value_to_set);
			list_index ++;
		}
	}
	public static void runSimulation(WorldBankCovid19Sim sim, int numDays) {
		while(sim.schedule.getTime() < sim.params.ticks_per_day * numDays && !sim.schedule.scheduleComplete()){
			sim.schedule.step(sim);
		}
	}
	public static void runSimulationForTicks(WorldBankCovid19Sim sim, int numTicks) {
		while(sim.schedule.getTime() < numTicks && !sim.schedule.scheduleComplete()){
			sim.schedule.step(sim);
		}
	}	
		
	public static void SetFractionInfectionsWithCertainNode(double fraction, WorldBankCovid19Sim world, BehaviourNode Node) {
		// Make this function assigns an infectious behaviour node of your choice to a certain percentage of the population
		for (Person p: world.agents) {
			double rand = world.random.nextDouble();
			if (!p.hasCovid() && rand <= fraction) {
				CoronavirusInfection inf = new CoronavirusInfection(p, null, world.infectiousFramework.getHomeNode(), world);
				inf.setBehaviourNode(Node);
				world.infections.add(inf);
				// kick off the infectious behaviour framework
				inf.step(world);
			}
		}
	}
	
	public static int GetNumberAlive(WorldBankCovid19Sim world) {
		int counter = 0;
		for (Person p: world.agents) {if (p.isAlive()) {counter++;}}
		return counter;
	}

	public static void HaltDiseaseProgressionAtStage(WorldBankCovid19Sim world, String stage) {
		// You present this function with a stage in the disease which you want to halt the infection, then this
		// function changes the parameters which allows the disease to progress further
		switch (stage) {
		case "Exposed":
			int exp_idx = 0;
			// Make sure there are no transitions from exposed to symptomatic COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_sym_by_age.set(exp_idx, 0.0);
				exp_idx ++;
			}
			break;
		case "Presymptomatic":
			world.params.infectiousToSymptomatic_mean = Integer.MAX_VALUE;
			world.params.infectiousToSymptomatic_std = 0;
			break;
		case "Mild":
			int mild_idx = 0;
			// Make sure there are no transitions from mild to severe COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_sev_by_age.set(mild_idx, 0.0);
				mild_idx ++;
			}
			break;
		case "Severe":
			int severe_idx = 0;
			// Make sure there are no transitions from severe to critical COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_cri_by_age.set(severe_idx, 0.0);
				severe_idx ++;
			}
			break;
		case "Critical":
			int critical_idx = 0;
			// Make sure there are no transitions from critical covid to death
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_dea_by_age.set(critical_idx, 0.0);
				critical_idx ++;
			}
			break;
		default:
			System.out.print("No parameters changed");
		}
		
	}
	
	public static void StopRecoveryHappening(WorldBankCovid19Sim world) {
		// This function sets the recovery time of COVID at various stages of the disease to an very high integer beyond the range
		// of the simulation, thereby stopping recovery from COVID happening
		world.params.asymptomaticToRecovery_mean = Integer.MAX_VALUE;
		world.params.asymptomaticToRecovery_std = 0;
		world.params.symptomaticToRecovery_mean = Integer.MAX_VALUE;
		world.params.symptomaticToRecovery_std = 0;
		world.params.severeToRecovery_mean = Integer.MAX_VALUE;
		world.params.severeToRecovery_std = 0;
		world.params.criticalToRecovery_mean = Integer.MAX_VALUE;
		world.params.criticalToRecovery_std = 0;
	}
}