package uk.ac.ucl.protecs.helperFunctions;

import java.util.ArrayList;
import java.util.Random;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusInfection;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class helperFunctions {
	
	public static WorldBankCovid19Sim CreateDummySimWithRandomSeed(String paramsFilename, boolean demography, boolean testing) {
		Random rand = new Random();
		int seed = rand.nextInt(100000000);
		System.out.println("Running with seed = " + String.valueOf(seed));
		Params p = new Params(paramsFilename, false);
		WorldBankCovid19Sim myWorld = new WorldBankCovid19Sim(seed, p, "", demography, testing);
		return myWorld;
	}
	public static WorldBankCovid19Sim CreateDummySimWithChosenSeed(int seed, String paramsFilename, boolean demography, boolean testing) {
		System.out.println("Running with seed = " + String.valueOf(seed));
		Params p = new Params(paramsFilename, false);
		WorldBankCovid19Sim myWorld = new WorldBankCovid19Sim(seed, p, "", demography, testing);
		return myWorld;
	}
	
	public static void setParameterListsToValue(WorldBankCovid19Sim world, ArrayList <Double> list_to_change, double value_to_set) {
		int list_index = 0;
		for (double entry: list_to_change) {
			list_to_change.set(list_index, value_to_set);
			list_index ++;
		}
	}
	public static void runSimulation(WorldBankCovid19Sim sim, int numDays) {
		while(sim.schedule.getTime() < Params.ticks_per_day * numDays && !sim.schedule.scheduleComplete()){
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
	
}