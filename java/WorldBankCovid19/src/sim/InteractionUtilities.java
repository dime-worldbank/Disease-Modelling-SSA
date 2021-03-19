package sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import objects.Location;
import objects.Person;

public class InteractionUtilities {
	
	/**
	 * Cluster agents into work bubbles.
	 */
	public static void create_work_bubbles(WorldBankCovid19Sim world){
		
		HashMap <Location, Map<String, List<Person>>> peoplePerDistrictPerJob = 
				new HashMap <Location, Map<String, List<Person>>> (); 
		
		// position everyone so they can assemble their group of peers
		for(Person p: world.agents){
			
			// extract workplace location
			p.goToWork(null);
			Location mySuper;
			if(p.getLocation() == null){ // if no workplace, use household instead!
				mySuper = p.getHousehold().getRootSuperLocation();
				p.goHome(); // send them home for easier record keeping in the next bit
			}
			else
				mySuper = p.getLocation().getRootSuperLocation();
			
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

			// store this record
			if(binsOfWorkers.containsKey(myJob))
				binsOfWorkers.get(myJob).add(p);
			else {
				ArrayList <Person> peepsInJob = new ArrayList <Person> ();
				peepsInJob.add(p);
				binsOfWorkers.put(myJob, peepsInJob);
			}			
		}
		
		System.out.print("Attempting to assemble bubbles...");
		
		// for each person, draw their interaction probabilities from the distribution
		for(Person p: world.agents){
			
			String myStatus = p.getEconStatus();
			
			// Person has been moved either to workplace or to household (if no job etc.)
			Location myWorkLocation = p.getLocation().getRootSuperLocation();
			
			// pull out the relevant distributions for friend group membership
			ArrayList <Double> interDistrib = (ArrayList <Double>)
					world.params.economicInteractionCumulativeDistrib.get(myStatus);
			int bubbleSize = world.params.econBubbleSize.get(myStatus);
			
			// pull out the relevant list of potential friends in my district
			HashMap <String, List<Person>> binsOfWorkers = 
					(HashMap <String, List<Person>>) peoplePerDistrictPerJob.get(myWorkLocation);
			
			// combine these into bubble member candidates and add them to the list of friends
			ArrayList <Person> candidateBubble = new ArrayList <Person> ();
			int emergencyBrake = 100; // it's dangerous to screw with for loops - take this!
			for(int i = 0; i < bubbleSize; i++){ // TODO this should depend on how many friends already exist for this person!
				int indexOfInteract = indexOfCumulativeDist(world.random.nextDouble(), interDistrib);
				String otherStatus = world.params.orderedEconStatuses.get(indexOfInteract);
				
				// pull out the list of potential bubble mates and check that it exists/is populated 
				// (by at least one other person!!)
				ArrayList <Person> potentialBubblemates = (ArrayList <Person>) binsOfWorkers.get(otherStatus);
				if((potentialBubblemates == null || potentialBubblemates.size() <= 1) && emergencyBrake > 0){
					System.out.print(".");
					i--;
					emergencyBrake--;
					continue;
				}
				
				if(emergencyBrake == 0){
					System.out.println("\nERROR - cannot assemble full bubble for " + p.toString());
					i = bubbleSize;
					continue;
				}
				
				// select the other Person
				int groupSize = potentialBubblemates.size(); // save for reuse
				Person otherPerson = potentialBubblemates.get(world.random.nextInt(groupSize));
				while(p == otherPerson)
					otherPerson = potentialBubblemates.get(world.random.nextInt(groupSize));

				// save them to the list
				if(!candidateBubble.contains(otherPerson))
					candidateBubble.add(otherPerson);
			}
			
			
			// store the list inside the Person
			p.addToWorkBubble(candidateBubble);
			
			// reset the agent
			p.goHome();
		}
		System.out.println();
	}
	
	public static int indexOfCumulativeDist(double val, ArrayList<Double> dist){
		for(int i = 0; i < dist.size(); i++){
			if(val <= dist.get(i))
				return i;
		}
		System.out.println("\nERROR: no value found");
		return dist.size() - 1;
//		return -1; // TODO REENABLE
	}
}