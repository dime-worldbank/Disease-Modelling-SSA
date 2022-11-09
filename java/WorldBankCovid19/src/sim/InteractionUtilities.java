package sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import objects.Location;
import objects.Person;

public class InteractionUtilities {
	
	/**
	 * Cluster agents into work bubbles.
	 */
	public static void create_work_bubbles(WorldBankCovid19Sim world){
		
		// in perfect mixing, just give a copy of everyone
		if(world.params.setting_perfectMixing) {
			
			HashSet <Person> copyOfAllAgents = new HashSet<Person> (world.agents);
			
			for(Person p: world.agents)
				p.setWorkBubble(copyOfAllAgents);
			
			return;
		}
		
		// OTHERWISE, set this up based on actual subsets
		
		HashMap <Location, Map<String, List<Person>>> peoplePerDistrictPerJob = 
				new HashMap <Location, Map<String, List<Person>>> (); 
		
		// some Persons may not have an economic location - so hold them for easy keeping
		HashMap <Person, Location> holderForEconLocations = new HashMap <Person, Location> ();
		
		
		// position everyone so they can assemble their group of peers
		for(Person p: world.agents){
			
			// extract workplace location
			Location mySuper = p.getHousehold().getRootSuperLocation(); // TODO WE ASSUME THEY WORK IN THEIR OWN DISTRICT - correct???
			
			// store this for later use
			holderForEconLocations.put(p, mySuper);
			
			// get econ status
			String myJob = p.getEconStatus();
			
			// add any necessary structures to support storing this info
			HashMap <String, List<Person>> binsOfWorkers;
			if(peoplePerDistrictPerJob.containsKey(mySuper))
				binsOfWorkers = (HashMap <String, List<Person>>) peoplePerDistrictPerJob.get(mySuper);
			else {
				binsOfWorkers = new HashMap <String, List<Person>> ();
				peoplePerDistrictPerJob.put(mySuper, binsOfWorkers);
			}
			assert (peoplePerDistrictPerJob.size() > 0): "No lists of jobs have been created but should have been";
			// store this record
			if(binsOfWorkers.containsKey(myJob))
				binsOfWorkers.get(myJob).add(p);
			else {
				ArrayList <Person> peepsInJob = new ArrayList <Person> ();
				peepsInJob.add(p);
				binsOfWorkers.put(myJob, peepsInJob);
			assert (peepsInJob.contains(myJob)): "My job hasn't been stored but should have been";
			}			
		}
		
		System.out.print("Attempting to assemble bubbles...");
		
		// for each person, draw their interaction probabilities from the distribution
		for(Person p: world.agents){
			
			String myStatus = p.getEconStatus();
			
			// Person has been moved either to workplace or to household (if no job etc.)
			Location myWorkLocation = holderForEconLocations.get(p);//p.getLocation().getRootSuperLocation();
			
			// pull out the relevant distributions for friend group membership
			ArrayList <Double> interDistrib = (ArrayList <Double>)
					world.params.economicInteractionCumulativeDistrib.get(myStatus);
			int bubbleSize = world.params.econBubbleSize.get(myStatus);
			assert (bubbleSize > 0): "This person's bubble size is zero";
			// pull out the relevant list of potential friends in my district
			HashMap <String, List<Person>> binsOfWorkers = 
					(HashMap <String, List<Person>>) peoplePerDistrictPerJob.get(myWorkLocation);
			
			// combine these into bubble member candidates and add them to the list of friends
			HashSet <Person> candidateBubble = new HashSet <Person> (p.getWorkBubble());
			int emergencyBrake = 100; // it's dangerous to screw with for loops - take this!
			
			for(int i = candidateBubble.size(); i < bubbleSize; i++){ // TODO this should depend on how many friends already exist for this person!
				int indexOfInteract = indexOfCumulativeDist(world.random.nextDouble(), interDistrib);
				String otherStatus = world.params.orderedEconStatuses.get(indexOfInteract);
				
				// pull out the list of potential bubble mates and check that it exists/is populated 
				// (by at least one other person!!)
				ArrayList <Person> potentialBubblemates = (ArrayList <Person>) binsOfWorkers.get(otherStatus);
				if((potentialBubblemates == null || potentialBubblemates.size() <= 1) && emergencyBrake > 0){
					//System.out.print(".");
					i--;
					emergencyBrake--;
					continue;
				}
				
				if(emergencyBrake == 0){
					//System.out.println("\nERROR - cannot assemble full bubble for " + p.toString());
				//	System.out.print(".");
					i = bubbleSize;
					continue;
				}
				
				// select the other Person
				int groupSize = potentialBubblemates.size(); // save for reuse
				Person otherPerson = potentialBubblemates.get(world.random.nextInt(groupSize));
				while(p == otherPerson)
					otherPerson = potentialBubblemates.get(world.random.nextInt(groupSize));

				// save them to the list
				candidateBubble.add(otherPerson);
			}
			
			
			// store the list inside the Person
			p.addToWorkBubble(candidateBubble);
			
			// reset the agent
			//p.goHome();
		}
		System.out.println();
		
		
/*		String makeTerribleGraphFilename = "/Users/swise/Downloads/rawWorkGraph_latest.csv";
		try {
			
			System.out.println("Reading in district transfer information from " + makeTerribleGraphFilename);
			
			// shove it out
			BufferedWriter badGraph = new BufferedWriter(new FileWriter(makeTerribleGraphFilename));

			for(Person p: world.agents){
				String myStr = p.toString();
				for(Person op: p.getWorkBubble()){
					myStr += ";" + op.toString();
				}
				badGraph.write("\n" + myStr);
			}
			
			badGraph.close();
		} catch (Exception e) {
			System.err.println("File input error: " + makeTerribleGraphFilename);
		}
*/
	}

	/**
	 * Cluster agents into work bubbles.
	 */
	public static void create_community_bubbles(WorldBankCovid19Sim world){
		
		// in perfect mixing, just give a copy of everyone
		if(world.params.setting_perfectMixing) {
			
			HashSet <Person> copyOfAllAgents = new HashSet<Person> (world.agents);
			
			for(Person p: world.agents)
				p.setCommunityBubble(copyOfAllAgents);
			
			return;
		}
		
		// OTHERWISE, set this up based on actual subsets
		
		HashMap <String, List<Person>> peoplePerDistrict = 
				new HashMap <String, List<Person>> (); 
		
		// position everyone so they can assemble their group of peers
		for(Person p: world.agents){

			// extract this Person's location
			String agentLocation = p.getHousehold().getRootSuperLocation().getId();
			
			// assemble an arraylist of Persons assocaited with each district 
			if(peoplePerDistrict.containsKey(agentLocation))
				peoplePerDistrict.get(agentLocation).add(p);
			else {
				ArrayList <Person> peopleInDistrict = new ArrayList <Person> ();
				peopleInDistrict.add(p);
				peoplePerDistrict.put(agentLocation, peopleInDistrict);
			}
		}
		
		System.out.print("Attempting to assemble community bubbles...");
		
		// for each person, draw their interaction probabilities from the distribution
		for(Person p: world.agents){
			
			// Person has been moved either to workplace or to household (if no job etc.)
			Location myLocation = p.getLocation();
			
			int bubbleSize = world.params.community_bubble_size;
			
			// combine these into bubble member candidates and add them to the list of friends
			ArrayList <Person> candidateBubble = (ArrayList <Person>) peoplePerDistrict.get(p.getHousehold().getRootSuperLocation().getId());
			HashSet <Person> myBubble = new HashSet <Person> ();
			
			int emergencyBrake = 100; // it's dangerous to screw with for loops - take this!
			
			for(int i = myBubble.size(); i < bubbleSize; i++){ // TODO this should depend on how many friends already exist for this person!

				if(emergencyBrake == 0){
					//System.out.println("\nERROR - cannot assemble full bubble for " + p.toString());
					System.out.print(".");
					i = bubbleSize;
					continue;
				}

				// select the other Person
				int groupSize = candidateBubble.size(); // save for reuse
				Person otherPerson = candidateBubble.get(world.random.nextInt(groupSize));
				while(p == otherPerson)
					otherPerson = candidateBubble.get(world.random.nextInt(groupSize));

				// save them to the list
				myBubble.add(otherPerson);
			}
			
			
			// store the list inside the Person
			p.addToCommunityBubble(myBubble);			
		}
		System.out.println();

		// finally, reset the agent
//		for(Person p: world.agents)
//			p.goHome();
// TODO sort this out better
		
/*		String makeTerribleGraphFilename = "/Users/swise/Downloads/rawSocialGraph_latest.csv";
		try {
			
			System.out.println("Reading in district transfer information from " + makeTerribleGraphFilename);
			
			// shove it out
			BufferedWriter badGraph = new BufferedWriter(new FileWriter(makeTerribleGraphFilename));

			for(Person p: world.agents){
				String myStr = p.toString();
				for(Person op: p.getCommunityBubble()){
					myStr += ";" + op.toString();
				}
				badGraph.write("\n" + myStr);
			}
			
			badGraph.close();
		} catch (Exception e) {
			System.err.println("File input error: " + makeTerribleGraphFilename);
		}
*/
	}
	
	public static int indexOfCumulativeDist(double val, ArrayList<Double> dist){
		for(int i = 0; i < dist.size(); i++){
			if(val <= dist.get(i))
				return i;
		}
//		System.out.println("\nERROR: no value found");
		return dist.size() - 1;
//		return -1; // TODO REENABLE
	}
}