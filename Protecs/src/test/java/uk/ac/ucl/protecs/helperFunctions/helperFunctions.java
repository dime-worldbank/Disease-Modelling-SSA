package uk.ac.ucl.protecs.helperFunctions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.ac.ucl.protecs.objects.diseases.CoronavirusInfection;
import uk.ac.ucl.protecs.objects.diseases.Infection;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.behaviours.CoronavirusBehaviourFramework.CoronavirusBehaviourNodeTitle;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import swise.behaviours.BehaviourNode;

public class helperFunctions {
	

	public enum NodeOption{
		CoronavirusInfectiousBehaviour,
		MovementBehaviour
	}
	

	public static WorldBankCovid19Sim CreateDummySim(String paramsFilename) {
		Random rand = new Random();
		int seed = rand.nextInt(100000000);
		return CreateDummySimWithSeed(seed, paramsFilename);
	}
	public static WorldBankCovid19Sim CreateDummySimWithSeed(int seed, String paramsFilename) {
		System.out.println("Running with seed = " + String.valueOf(seed));
		Params p = new Params(paramsFilename, true);
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
			if (!p.getInfectionSet().containsKey(DISEASE.COVID.key) && rand <= fraction) {			
				CoronavirusInfection inf = new CoronavirusInfection(p, null, world.infectiousFramework.getHomeNode(), world);
				inf.setBehaviourNode(Node);
				world.infections.add(inf);
				// kick off the infectious behaviour framework
				inf.step(world);
			}
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
			if (!p.getInfectionSet().containsKey(DISEASE.COVID.key) && rand <= fraction) {
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
			if (p.isAlive()) locationBin.add(p.getLocation().getLocationType().key);
		}
		}
		return locationBin;
	}
	
	public static void HaltDiseaseProgressionAtStage(WorldBankCovid19Sim world, CoronavirusBehaviourNodeTitle stage) {
		// You present this function with a stage in the disease which you want to halt the infection, then this
		// function changes the parameters which allows the disease to progress further
		switch (stage) {
		case EXPOSED:
			int exp_idx = 0;
			// Make sure there are no transitions from exposed to symptomatic COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_sym_by_age.set(exp_idx, 0.0);
				exp_idx ++;
			}
			break;
		case PRESYMPTOMATIC:
			world.params.infectiousToSymptomatic_mean = Integer.MAX_VALUE;
			world.params.infectiousToSymptomatic_std = 0;
			break;
		case MILD:
			int mild_idx = 0;
			// Make sure there are no transitions from exposed to symptomatic COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_sev_by_age.set(mild_idx, 0.0);
				mild_idx ++;
			}
			break;
		case SEVERE:
			int severe_idx = 0;
			// Make sure there are no transitions from exposed to symptomatic COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_cri_by_age.set(severe_idx, 0.0);
				severe_idx ++;
			}
			break;
		case CRITICAL:
			int critical_idx = 0;
			// Make sure there are no transitions from exposed to symptomatic COVID
			for (double val: world.params.infection_p_sym_by_age) {
				world.params.infection_p_dea_by_age.set(critical_idx, 0.0);
				critical_idx ++;
			}
			break;
		default:
			System.out.print("No parameters changed");
		}
		
	}

	
	public static void StopCovidFromSpreading(WorldBankCovid19Sim world) {
		world.params.infection_beta = 0.0;
	}
	
	public static HashSet<DISEASE> InfectionsPresentInSim(WorldBankCovid19Sim world) {
		HashSet<DISEASE> toReturn = new HashSet<DISEASE>();
		for (Infection i: world.infections) {
			toReturn.add(DISEASE.getValue(i.getDiseaseName()));
		}
		return toReturn;
	}
}