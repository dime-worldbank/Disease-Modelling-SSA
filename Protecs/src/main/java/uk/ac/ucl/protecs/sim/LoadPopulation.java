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

import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.objects.locations.Workplace;
import uk.ac.ucl.protecs.objects.locations.Location.LocationCategory;

import static org.junit.Assert.fail;

public class LoadPopulation{
	
	
	public static void load_population(String agentsFilename, WorldBankCovid19Sim sim){
		try {			
			// initialise the holder
			for(Location l: sim.adminBoundaries){
				sim.personsToAdminBoundary.put(l, new ArrayList <Person> ());
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
			// map the header into column names relative to location
			String [] header = Params.splitRawCSVString(s);
			HashMap <String, Integer> columnNames = Params.parseHeader(header);
			int personIDIndex = columnNames.get("person_id");
			int ageIndex = columnNames.get("age");
			int sexIndex = columnNames.get("sex");
			int householdIDIndex = columnNames.get("household_id");
			int districtIDIndex = columnNames.get("district_id");
			int economicStatusIndex = columnNames.get("economic_status");
			int schoolGoerIndex = columnNames.get("school_goers");
			int workplaceIDIndex = Integer.MAX_VALUE;
			if (!sim.params.setting_perfectMixing) {
				workplaceIDIndex = columnNames.get("workplace_id");
			}
			boolean usingWorkplaces = (workplaceIDIndex < Integer.MAX_VALUE);
						
			
			System.out.print("BEGIN READING IN PEOPLE...");
			
			// read in the raw data
			//int myIndex = 10;
			while ((s = agentData.readLine()) != null ){//&& myIndex > 0) {
				//myIndex--;
				
				// separate the columns from the raw text
				String[] bits = Params.splitRawCSVString(s);
				
				// make sure the larger units are set up before we create the individual

				// set up the Household for the Person
				String hhName = bits[householdIDIndex];
				Household h = rawHouseholds.get(hhName);
				String wpName = "None";				

				if (usingWorkplaces) {
					wpName = bits[workplaceIDIndex];
				}
				Workplace w = rawWorkplaces.get(wpName);

				// target district
				String myAdminZoneName = bits[districtIDIndex]; 
				Location myAdminZone = sim.params.adminZones.get(myAdminZoneName);

				boolean schoolGoer = bits[schoolGoerIndex].equals("1");
				
				// if the Household doesn't already exist, create it and save it
				if(h == null){
					// set up the Household
					h = new Household(hhName, myAdminZone);
					rawHouseholds.put(hhName, h);
//					sim.households.add(h);
				}
				if (usingWorkplaces && w == null) {
					// if the workplace doesn't already exist, create it and save it
					wpName = bits[workplaceIDIndex];
					// set up the Household
					w = new Workplace(wpName, myAdminZone);
					rawWorkplaces.put(wpName, w);
//					sim.workplaces.add(w);
					
					
				}

				// set up the person
				// create a random birthday
				int birthday = sim.random.nextInt(365);

				// create and save the Person agent
				Person p = new Person(Integer.parseInt(bits[personIDIndex]), // ID 
						Integer.parseInt(bits[ageIndex]), // age
						birthday, // birthday to update population
						SEX.getValue(bits[sexIndex].toLowerCase()), // sex
						OCCUPATION.getValue(bits[economicStatusIndex].toLowerCase()), // lower case all of the job titles
						schoolGoer,
						h,
						w,
						sim
						);

				h.addHost(p);
				h.addPersonToHousehold(p);
//				p.setLocation(myDistrict);
				p.setActivityNode(sim.movementFramework.getEntryPoint());
				sim.agents.add(p);
				sim.personsToAdminBoundary.get(myAdminZone).add(p);
				//	Store the occupations that appear in this census
				sim.occupationsInSim.add(OCCUPATION.getValue(bits[economicStatusIndex].toLowerCase()));
				// schedule the agent to run at the beginning of the simulation
				sim.schedule.scheduleOnce(0, sim.param_schedule_movement, p);
				//this.schedule.scheduleRepeating(p);
				
				// Some occupational constraints will cause people to stay at home, we can initialise this by checking who has the appropriate occupation for this constraint
				// and then immobilising them, causing them to remain at home throughout the simulation
				if (sim.params.OccupationConstraintList.containsKey(OCCUPATION.getValue(bits[economicStatusIndex].toLowerCase()))) {
					// TODO: match this to an enum when everything is merged together
					if (sim.params.OccupationConstraintList.get(OCCUPATION.getValue(bits[economicStatusIndex].toLowerCase())).equals(LocationCategory.HOME)) {
						p.setMobility(false);
					}
						
				}
				if (bits[economicStatusIndex].equals("inactive") || bits[economicStatusIndex].equals("unemployed_not_ag")) p.setUnemployed();
				
			}
			for (Household h: rawHouseholds.values()) {
				sim.households.add(h);
			}
			if (usingWorkplaces) {
				for (Workplace w: rawWorkplaces.values()) {
					sim.workplaces.add(w);
				}
			}
			
			// clean up after ourselves!
			agentData.close();
							
			System.out.println("FINISHED READING PEOPLE");
			if (!sim.params.setting_perfectMixing) {
			System.out.print("CREATING WORKPLACE BUBBLES...");

			Map<String, List<Person>> belongingToBubble = sim.agents.stream().collect(
					Collectors.groupingBy(
							Person::checkWorkplaceID
							)
					);
			for (Workplace w: sim.workplaces) {
				List<Person> peopleInThisBubble = belongingToBubble.get(w.getId());
				// change this list to a hash set so we can store it
				HashSet<Person> bubble = new HashSet<Person>(peopleInThisBubble);
				// existing structure is a 
				for (Person p: peopleInThisBubble) {
					p.setWorkBubble(bubble);
					}
			}
			System.out.println("FINISHED CREATING WORKPLACE BUBBLES");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("File input error: " + agentsFilename);
			fail();
		}
	}
}
