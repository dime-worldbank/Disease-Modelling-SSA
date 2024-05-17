package uk.ac.ucl.protecs.helperFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusInfection;
import uk.ac.ucl.protecs.objects.diseases.Infection;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

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
		Params p = new Params(paramsFilename, true);
		return new WorldBankCovid19Sim(seed, p, "", demography);
	}
	
	public static void setParameterListsToValue(WorldBankCovid19Sim world, ArrayList <Double> list_to_change, double value_to_set) {
		int list_index = 0;
		for (double entry: list_to_change) {
			list_to_change.set(list_index, value_to_set);
			list_index ++;
		}
	}
	public static HashSet<String> getUniqueNodesOverCourseofSim(WorldBankCovid19Sim world, double numDaysToRun, NodeOption option, double sample_regularity){
		
		
		// Create a list to store the unique node stages that occur in each step
		HashSet <String> behaviourNodeBin = new HashSet<String>();
		
		
		switch (option) {
		case CoronavirusInfectiousBehaviour:{
		// Simulate over the time period and get the disease stages present in the simulation
		while(world.schedule.getTime() < (double) Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
			// create a list to store the disease nodes that occur in the simulation

			world.schedule.step(world);
			if (world.schedule.getTime() % (int) Params.ticks_per_day == sample_regularity) {
			for (Infection i: world.infections) {
				behaviourNodeBin.add(i.getBehaviourName());
				}
			}
		}
		return behaviourNodeBin;
		
		}
		case MovementBehaviour:{
		// Simulate over the time period and get the movement behaviours present in the simulation
		while(world.schedule.getTime() < (double) Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
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
	public static void SetFractionObjectsWithCertainBehaviourNode(double fraction, WorldBankCovid19Sim world, BehaviourNode Node, NodeOption option) {
		
		switch (option) {
		case CoronavirusInfectiousBehaviour:{
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
		break;
		}
		case MovementBehaviour:{
		// Make this function assigns an infectious behaviour node of your choice to a certain percentage of the population
		for (Person p: world.agents) {
			double rand = world.random.nextDouble();
			if (rand <= fraction) {
				p.setActivityNode(Node);
				// kick off the infectious behaviour framework
				p.step(world);
			}
		}
		break;
		}
		default:{
			System.out.println("No option recognised");
		}
	}
	}
	
	public static List<String> getFinalBehaviourNodesInSim(WorldBankCovid19Sim world, double numDaysToRun, NodeOption option){
		// This function runs the simulation for a predetermined number of days.
		// At the end of the simulation, the function returns a list of the behaviour nodes being 'performed' by the object.
		// Create a list to store the unique node stages that occur in each step

		HashSet <String> behaviourNodeBin = new HashSet<String>();

		switch (option) {
		case CoronavirusInfectiousBehaviour:{
		
		// Simulate over the time period and get the disease stages present in the simulation
		while(world.schedule.getTime() < (double) Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
			// create a list to store the disease nodes that occur in the simulation
			world.schedule.step(world);
		}
		for (Infection i: world.infections) {
			behaviourNodeBin.add(i.getBehaviourName());
		}
		
		List<String> UniqueNodes = new ArrayList<String>(behaviourNodeBin);
		return UniqueNodes;
		}
		case MovementBehaviour:{
			
			// Simulate over the time period and get the disease stages present in the simulation
			while(world.schedule.getTime() < Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
				// create a list to store the disease nodes that occur in the simulation
				world.schedule.step(world);
			}
			for (Person p: world.agents) {
				behaviourNodeBin.add(p.getActivityNode().getTitle());
			}
			
			List<String> UniqueNodes = new ArrayList<String>(behaviourNodeBin);
			return UniqueNodes;
			}
		default:
			return null;
		}
		
	}
	
	public static void makePeopleAlwaysLeaveHome(WorldBankCovid19Sim world) {				
		for (Map.Entry<String, Double> entry : world.params.economic_status_weekday_movement_prob.entrySet()) {
			entry.setValue(1.0);
		}
		for (Map.Entry<String, Double> entry : world.params.economic_status_otherday_movement_prob.entrySet()) {
			entry.setValue(1.0);
		}
	}
	
	public static HashSet<String> getUniqueLocationsOverCourseOfSimulation(WorldBankCovid19Sim world, double numDaysToRun){
		HashSet <String> locationBin = new HashSet<String>();
		while(world.schedule.getTime() < (double) Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
			// create a list to store the disease nodes that occur in the simulation
			world.schedule.step(world);
		
		for (Person p: world.agents) {
			locationBin.add(p.getLocation().getLocationType().key);
		}
		}
		return locationBin;
	}
}