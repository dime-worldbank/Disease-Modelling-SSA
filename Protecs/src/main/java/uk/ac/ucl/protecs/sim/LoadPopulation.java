package uk.ac.ucl.protecs.sim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.ac.ucl.protecs.objects.Household;
import uk.ac.ucl.protecs.objects.Workplace;
import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.objects.Person;

public class LoadPopulation{
	
	
	public static void load_population(String agentsFilename, WorldBankCovid19Sim sim){
		try {			
			// initialise the holder
			for(Location l: sim.districts){
				sim.personsToDistrict.put(l, new ArrayList <Person> ());
			}

			
			// use a helpful holder to find households by their names
			HashMap <String, Household> rawHouseholds = new HashMap <String, Household> ();
			HashMap <String, Workplace> rawWorkplaces = new HashMap <String, Workplace> ();

			System.out.println("Reading in agents from " + agentsFilename);
			
			// Open the file
			FileInputStream fstream = new FileInputStream(agentsFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader agentData = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// get rid of the header
			s = agentData.readLine();
			//Params.parseHeader(s.split(',')); TODO fix meeee
			
			System.out.print("BEGIN READING IN PEOPLE...");
			
			// read in the raw data
			//int myIndex = 10;
			while ((s = agentData.readLine()) != null ){//&& myIndex > 0) {
				//myIndex--;
				
				// separate the columns from the raw text
				String[] bits = Params.splitRawCSVString(s);
				
				// make sure the larger units are set up before we create the individual

				// set up the Household for the Person
				String hhName = bits[4];
				Household h = rawHouseholds.get(hhName);
				String wpName = bits[5];
				Workplace w = rawWorkplaces.get(wpName);

				// target district
				String myDistrictName = "d_" + bits[6]; // TODO AN ABOMINATION, STANDARDISE IT
				Location myDistrict = sim.params.districts.get(myDistrictName);

				boolean schoolGoer = bits[8].equals("1");
				
				// if the Household doesn't already exist, create it and save it
				if(h == null){
					// set up the Household
					h = new Household(hhName, myDistrict);
					rawHouseholds.put(hhName, h);
					sim.households.add(h);
				}
				// if the workplace doesn't already exist, create it and save it
				if(w == null){
					
					// set up the Household
					w = new Workplace(wpName, myDistrict);
					rawWorkplaces.put(wpName, w);
					sim.workplaces.add(w);
				}

				// set up the person
				// create a random birthday
				int birthday = sim.random.nextInt(365);

				// create and save the Person agent
				Person p = new Person(Integer.parseInt(bits[1]), // ID 
						Integer.parseInt(bits[2]), // age
						birthday, // birthday to update population
						bits[3], // sex
						bits[7].toLowerCase(), // lower case all of the job titles
						schoolGoer,
						h,
						w,
						sim
						);
				h.addPerson(p);
				w.addPerson(p);
				//p.setLocation(myDistrict);
				p.setActivityNode(sim.movementFramework.getHomeNode());
				sim.agents.add(p);
				sim.personsToDistrict.get(myDistrict).add(p);
				
				// schedule the agent to run at the beginning of the simulation
				sim.schedule.scheduleOnce(0, sim.param_schedule_movement, p);
				//this.schedule.scheduleRepeating(p);
			}
			
			// clean up after ourselves!
			agentData.close();
							
			System.out.println("FINISHED READING PEOPLE, now make workplace bubbles");
			Map<String, List<Person>> belongingToBubble = sim.agents.stream().collect(
					Collectors.groupingBy(
							Person::checkWorkplaceID
							)
					);
			for (Workplace w: sim.workplaces) {
				List<Person> peopleInThisBubble = belongingToBubble.get(w.returnID());
				// change this list to a hash set so we can store it
				HashSet<Person> bubble = new HashSet<Person>(peopleInThisBubble);
				// existing structure is a 
				for (Person p: peopleInThisBubble) {
					p.setWorkBubble(bubble);
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("File input error: " + agentsFilename);
		}
	}
}
