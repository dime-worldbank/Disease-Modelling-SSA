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

import behaviours.InfectiousBehaviourFramework;
import behaviours.MovementBehaviourFramework;
import objects.Household;
import objects.Infection;
import objects.Location;
import objects.Person;
import sim.engine.SimState;
import sim.engine.Steppable;

public class WorldBankCovid19Sim extends SimState {

	// the objects which make up the system
	ArrayList <Person> agents;
	ArrayList <Household> households;
	
	ArrayList <Location> districts;
	
	HashMap <Location, ArrayList<Person>> personsToDistrict; 
	
	public MovementBehaviourFramework movementFramework;
	public InfectiousBehaviourFramework infectiousFramework;
	public Params params;
	
	// record-keeping
	
	ArrayList <HashMap <String, Double>> dailyRecord = new ArrayList <HashMap <String, Double>> ();
	public double record_numInfected = 0, 
		record_numExposed = 0,
		record_numContagious = 0,
		record_numSevere = 0,
		record_numCritical = 0,
		record_numSymptomatic = 0;
	
	/*
	 * date

infected_count
current_exposed_cases
current_contagious_cases
current_hospitalized_cases
current_critical_cases
asymptomatic_count
symptomatic_count
hospitalized_count
critical_count
died_count
recovered_count
new_cases
new_hospitalized
new_critical
version
days
scenario
	 */
	
	/**
	 * Constructor function
	 * @param seed
	 */
	public WorldBankCovid19Sim(long seed, Params params) {
		super(seed);
		this.params = params;
	}
	
	public void start(){
		
		// copy over the relevant information
		districts = new ArrayList <Location> (params.districts.values());
		
		// set up the behavioural framework
		movementFramework = new MovementBehaviourFramework(this);
		infectiousFramework = new InfectiousBehaviourFramework(this);
		
		// load the population
		load_population(params.population_filename);
		
		// if there are no agents, SOMETHING IS WRONG. Flag this issue!
		if(agents.size() == 0) {
			System.out.println("ERROR *** NO AGENTS LOADED");
			System.exit(0);
		}

		// set up the social networks
		InteractionUtilities.create_work_bubbles(this);
		InteractionUtilities.create_social_bubbles(this);

		// set up the infections
		for(Location l: params.lineList.keySet()){
			
			// number of people to infect
			int countInfections = params.lineList.get(l);
			
			// list of infected people
			HashSet <Person> newlyInfected = new HashSet <Person> ();
			
			// number of people present
			int numPeopleHere = l.getPeople().size();
			if(numPeopleHere == 0){ // if there is no one there, don't continue
				System.out.println("WARNING: attempting to initialise infection in Location " + l.getId() + " but there are no People present. Continuing without successful infection...");
				continue;
			}
			
			int collisions = 100; // to escape while loop in case of troubles

			// infect until you have met the target number of infections
			while(newlyInfected.size() < countInfections && collisions > 0){
				Person p = l.getPeople().get(random.nextInt(numPeopleHere));
				
				// check for duplicates!
				if(newlyInfected.contains(p)){
					collisions--;
					continue;
				}
				else // otherwise record that we're infecting this person
					newlyInfected.add(p);
				
				// create new person
				Infection inf = new Infection(p, null, infectiousFramework.getInfectedEntryPoint(p));
				schedule.scheduleOnce(1, 10, inf);
			}
		}
		/*for(int i = 0; i < 5; i++){
			int personIndex = random.nextInt(agents.size());
			Infection inf = new Infection(agents.get(personIndex), null, infectiousFramework.getInfectedEntryPoint());
			schedule.scheduleOnce(1, 10, inf);
		}*/
		
		// everyone starts from home!
		for(Person p: agents)
			p.goHome();
		
		Steppable reporter = new Steppable(){

			@Override
			public void step(SimState arg0) {
				HashMap <String, Double> myRecord = new HashMap <String, Double> ();
				myRecord.put("date", arg0.schedule.getTime());
				myRecord.put("infected_count", record_numInfected);
				dailyRecord.add(myRecord);
				// TODO Auto-generated method stub
				/*
				 * date

infected_count
current_exposed_cases
current_contagious_cases
current_hospitalized_cases
current_critical_cases
asymptomatic_count
symptomatic_count
hospitalized_count
critical_count
died_count
recovered_count
new_cases
new_hospitalized
new_critical
version
days
scenario
				 */
				
			}
		};
		schedule.scheduleRepeating(reporter, params.ticks_per_day);
	}
	
	public void load_population(String agentsFilename){
		try {
			
			// holders for construction
			agents = new ArrayList <Person> ();
			households = new ArrayList <Household> ();
			
			// initialise the holder
			personsToDistrict = new HashMap <Location, ArrayList<Person>>();
			for(Location l: districts){
				personsToDistrict.put(l, new ArrayList <Person> ());
			}

			
			// use a helpful holder to find households by their names
			HashMap <String, Household> rawHouseholds = new HashMap <String, Household> ();
			
			System.out.println("Reading in agents from " + agentsFilename);
			
			// Open the file
			FileInputStream fstream = new FileInputStream(agentsFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader agentData = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// get rid of the header
			s = agentData.readLine(); // TODO use header to specify where everything is, just in case!!!
			
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

				// target district
				String myDistrictName = bits[5];
				Location myDistrict = params.districts.get(myDistrictName);

				// if the Household doesn't already exist, create it and save it
				if(h == null){
					
					// set up the Household
					h = new Household(hhName, myDistrict);
					rawHouseholds.put(hhName, h);
					households.add(h);
				}
				
				// identify the location in which the person, possibly, works
				
				String economicActivityLocationName = bits[7];
				Location econLocation = params.districts.get(economicActivityLocationName);
				// TODO: they might not work anywhere! Further, they might work in a particular subset of the location!
				
				// set up the person

				// create and save the Person agent
				Person p = new Person(Integer.parseInt(bits[1]), // ID 
						Integer.parseInt(bits[2]), // age
						bits[3], // sex
						bits[6],
						econLocation,
						this
						);
				h.addPerson(p);
				//p.setLocation(myDistrict);
				p.setActivityNode(movementFramework.getEntryPoint());
				agents.add(p);
				personsToDistrict.get(myDistrict).add(p);
				
				// schedule the agent to run at the beginning of the simulation
				this.schedule.scheduleOnce(0, p);
				//this.schedule.scheduleRepeating(p);
			}
			
			// clean up after ourselves!
			agentData.close();
							
			System.out.println("FINISHED READING PEOPLE");
		} catch (Exception e) {
			System.err.println("File input error: " + agentsFilename);
		}
	}
	

	void reportOnInfected(){
		String makeTerribleGraphFilename = "/Users/swise/Downloads/nodes_latest_16.gexf";
		try {
			
			System.out.println("Printing out infects? from " + makeTerribleGraphFilename);
			
			// shove it out
			BufferedWriter badGraph = new BufferedWriter(new FileWriter(makeTerribleGraphFilename));

			//badGraph.write("ID;econ;age;infect;time;source");
			badGraph.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gexf xmlns=\"http://www.gexf.net/1.1draft\" version=\"1.1\">\n" + 
					"<graph mode=\"static\" defaultedgetype=\"directed\">\n" + 
					"<attributes class=\"node\" type=\"static\"> \n" +
				     "<attribute id=\"infected\" title=\"Infected\" type=\"string\"/>\n</attributes>\n");
			badGraph.write("<nodes>\n");
			for(Person p: agents){
				String myStr = p.toString();
				//myStr += ";" + p.getEconStatus() + ";" + p.getAge() + ";" + p.getInfectStatus();
				
				if(p.getInfection() != null){
					Person source = p.getInfection().getSource();
					String sourceName = null;
					if(source != null)
						sourceName = source.toString();
					//myStr += ";" + p.getInfection().getStartTime() + ";" + sourceName;
					myStr = p.getInfection().getBehaviourName();
				}
				else
					//myStr += "Susceptible;;";
					myStr = "Susceptible";
/*				for(Person op: p.getWorkBubble()){
					myStr += ";" + op.toString();
				}
	*/			
				badGraph.write("\t<node id=\"" + p.getID() + "\" label=\"" + p.toString() + 
						"\"> <attvalue for=\"infected\" value=\"" +myStr +  "\"/></node>\n");

				//badGraph.write("\n" + myStr);
			}
			badGraph.write("</nodes>\n");
			badGraph.write("<edges>\n");
			for(Person p: agents){
				int myID = p.getID();
				for(Person op: p.getWorkBubble()){
					badGraph.write("\t<edge source=\"" + myID + "\" target=\"" + op.getID() + "\" weight=\"1\" />\n");
				}
			}
			
			badGraph.write("</edges>\n");
			badGraph.write("</graph>\n</gexf>");
			badGraph.close();
		} catch (Exception e) {
			System.err.println("File input error: " + makeTerribleGraphFilename);
		}
	}
	
	void exportDailyReports(String filename){
		try {
			
			System.out.println("Printing out infects? from " + filename);
			
			// shove it out
			BufferedWriter exportFile = new BufferedWriter(new FileWriter(filename, true));

			for(int i = 0; i < dailyRecord.size(); i++){
				HashMap <String, Double> myRecord = dailyRecord.get(i);
				String s = "";
				for(String paramName: params.exportParams){
					s += myRecord.get(paramName).toString() + "\t";
				}
				exportFile.write("\n" + s);
			}
			exportFile.close();
		} catch (Exception e) {
			System.err.println("File input error: " + filename);
		}
	}
	
	public double nextRandomLognormal(double mean, double std){
		return Math.exp(mean + random.nextGaussian() * std);
	}
	
	public static void main(String [] args){
		if(args.length < 0){
			System.out.println("usage error");
			System.exit(0);
		}
		
		WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(System.currentTimeMillis(), new Params());
		
		System.out.println("Loading...");

		mySim.start();

		System.out.println("Running...");

		while(mySim.schedule.getTime() < 6 * 60 && !mySim.schedule.scheduleComplete()){
			mySim.schedule.step(mySim);
			double myTime = mySim.schedule.getTime();
			System.out.println("*****END TIME: DAY " + (int)(myTime / 6) + " HOUR " + (int)((myTime % 6) * 4) + " RAWTIME: " + myTime);
		}
		
		mySim.reportOnInfected();
		mySim.exportDailyReports("dailyReport.tsv");
		
		mySim.finish();
		
		System.out.println("...run finished");
		//System.exit(0);
	}
}