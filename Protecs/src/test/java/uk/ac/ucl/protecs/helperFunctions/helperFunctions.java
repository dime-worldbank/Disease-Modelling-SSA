package uk.ac.ucl.protecs.helperFunctions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.diseases.Infection;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class helperFunctions {
	
	public enum NodeOption{
		CoronavirusInfectiousBehaviour,
		MovementBehaviour
	}
	
	public static WorldBankCovid19Sim CreateDummySim(String paramsFilename, boolean demography) {
		Random rand = new Random();
		int seed = rand.nextInt(100000000);
		return CreateDummySimWithSeed(seed, paramsFilename, demography);
	}
	public static WorldBankCovid19Sim CreateDummySimWithSeed(int seed, String paramsFilename, boolean demography) {
		System.out.println("Running with seed = " + String.valueOf(seed));
		Params p = new Params(paramsFilename, false);
		return new WorldBankCovid19Sim(seed, p, "", demography);
	}
	
	public static void setParameterListsToValue(WorldBankCovid19Sim world, ArrayList <Double> list_to_change, double value_to_set) {
		int list_index = 0;
		for (double entry: list_to_change) {
			list_to_change.set(list_index, value_to_set);
			list_index ++;
		}
	}
	public static HashSet<String> getUniqueNodesOverCourseofSim(WorldBankCovid19Sim world, int numDaysToRun, NodeOption option, double sample_regularity){
		
		
		// Create a list to store the unique node stages that occur in each step
		HashSet <String> behaviourNodeBin = new HashSet<String>();
		
		
		switch (option) {
		case CoronavirusInfectiousBehaviour:{
		// Simulate over the time period and get the disease stages present in the simulation
		while(world.schedule.getTime() < Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
			// create a list to store the disease nodes that occur in the simulation

			world.schedule.step(world);
			if (world.schedule.getTime() % Params.ticks_per_day == sample_regularity) {
			for (Infection i: world.infections) {
				behaviourNodeBin.add(i.getBehaviourName());
				}
			}
		}
		return behaviourNodeBin;
		
		}
		case MovementBehaviour:{
		// Simulate over the time period and get the movement behaviours present in the simulation
		while(world.schedule.getTime() < Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
			// create a list to store the mobility nodes that occur in the simulation
			world.schedule.step(world);
			for (Person p: world.agents) {
				behaviourNodeBin.add(p.getActivityNode().getTitle());
			}
			
		}
			return behaviourNodeBin;
		}
		
		default:{
			System.out.println("No option recognised");
			return null;
		}
		}
		
	}
}