package sim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import objects.Location;
import objects.Person;

public class Params {
	
	public double r0 = 3.0;
	public double infection_beta = 0.116;
	
	public HashMap <String, Double> economic_status_weekday_movement_prob;
	public HashMap <String, Double> economic_status_otherday_movement_prob;
	public HashMap <String, Double> economic_num_interactions_weekday;
	double mild_symptom_movement_prob;
	
	// export parameters
	String [] exportParams = new String [] {"time", "infected_count", "num_died",
			"num_recovered", "num_exposed", 
			"num_contagious", "num_severe", "num_critical", "num_symptomatic", "num_asymptomatic"};

	// holders for locational data
	HashMap <String, Location> districts;
	ArrayList <String> districtNames;
	ArrayList <Map<String, List<Double>>> dailyTransitionProbs;

	HashMap <Location, Double> districtLeavingProb;
	
	// holders for economic-related data
	
	HashMap <String, Map<String, Double>> economicInteractionDistrib;
	HashMap <String, List<Double>> economicInteractionCumulativeDistrib;
	HashMap <String, Integer> econBubbleSize;
	ArrayList <String> orderedEconStatuses;
	
	// holders for epidemic-related data
	
	HashMap <Location, Integer> lineList;
	
	// see Kerr et al 2020 - https://www.medrxiv.org/content/10.1101/2020.05.10.20097469v3.full.pdf
	public double exposedToInfectious_mean =	4.5 * ticks_per_day;
	public double exposedToInfectious_std =		1.5 * ticks_per_day;
	public double infectiousToSymptomatic_mean =1.1 * ticks_per_day;
	public double infectiousToSymptomatic_std = 0.9 * ticks_per_day;
	public double symptomaticToSevere_mean = 	6.6 * ticks_per_day;
	public double symptomaticToSevere_std = 	4.9 * ticks_per_day;
	public double severeToCritical_mean =		1.5 * ticks_per_day;
	public double severeToCritical_std =		2.0 * ticks_per_day;
	public double criticalToDeath_mean =		10.7 * ticks_per_day;
	public double criticalToDeath_std =			4.8 * ticks_per_day;
	public double asymptomaticToRecovery_mean =	8.0 * ticks_per_day;
	public double asymptomaticToRecovery_std =	2.0 * ticks_per_day;
	public double sympomaticToRecovery_mean =	8.0 * ticks_per_day;
	public double sympomaticToRecovery_std =	2.0 * ticks_per_day;
	public double severeToRecovery_mean =		18.1 * ticks_per_day;
	public double severeToRecovery_std =		6.3 * ticks_per_day;
	public double criticalToRecovery_mean =		18.1 * ticks_per_day;
	public double criticalToRecovery_std =		6.3 * ticks_per_day;
	
	
	// data files
	
	public String dataDir = "";
	
	
	public String population_filename = "preprocessed/census/census_sample_5perc_040521.csv";//census_sample_5perc_042221.csv";//sample_1500.txt";
	public String district_transition_filename = "preprocessed/mobility/New Files/daily_region_transition_probability-new-district-post-lockdown_i5.csv";	
	public String district_leaving_filename = "preprocessed/mobility/intra_district_decreased_mobility_rates.csv";
	
	public String economic_status_weekday_movement_prob_filename = "configs/ECONOMIC_STATUS_WEEKDAY_MOVEMENT_PROBABILITY.txt";
	public String economic_status_otherday_movement_prob_filename = "configs/ECONOMIC_STATUS_OTHER_DAY_MOVEMENT_PROBABILITY.txt";
	public String economic_status_num_daily_interacts_filename = "configs/no_interactions_wk_econ.txt";
	
	public String econ_interaction_distrib_filename = "configs/interaction_matrix_nld.csv";
	
	public String line_list_filename = "preprocessed/line_list/line_list_5perc.txt";
	
	// social qualities
	public static int social_bubble_size = 30;
	public static int community_interaction_count = 5;
	
	// time
	public static int hours_per_tick = 4; // the number of hours each tick represents
	public static int ticks_per_day = 24 / hours_per_tick;
	
	public static int hour_start_day_weekday = 8;
	public static int hour_start_day_otherday = 8;
	
	public static int hour_end_day_weekday = 16;
	public static int hour_end_day_otherday = 16;
	
	public static int time_leisure_weekday = 4;
	public static int time_leisure_weekend = 12;
	
	
	public Params(String dirname){
		
		dataDir = dirname;
		
		load_district_data(dirname + district_transition_filename);
		load_district_leaving_data(dirname + district_leaving_filename);
		
		economic_status_weekday_movement_prob = readInEconomicData(dirname + economic_status_weekday_movement_prob_filename, "economic_status", "movement_probability");
		economic_status_otherday_movement_prob = readInEconomicData(dirname + economic_status_otherday_movement_prob_filename, "economic_status", "movement_probability");
		economic_num_interactions_weekday = readInEconomicData(dirname + economic_status_num_daily_interacts_filename, "economic_status", "interactions");
		
		load_econ_distrib(dirname + econ_interaction_distrib_filename);
		
		load_line_list(dirname + line_list_filename);
	}
	
	//
	// DATA IMPORT UTILITIES
	//
	
	// Epidemic
	
	public void load_line_list(String lineListFilename){
		try {
			
			System.out.println("Reading in data from " + lineListFilename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(lineListFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader lineListDataFile = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = lineListDataFile.readLine();

			// map the header into column names relative to location
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> columnNames = parseHeader(header);
			int districtNameIndex = columnNames.get("district");
			int countIndex = columnNames.get("count");
			
			// set up data container
			lineList = new HashMap <Location, Integer> ();
			
			// read in the raw data
			while ((s = lineListDataFile.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				Location myDistrict = districts.get(bits[districtNameIndex]);
				Integer myCount = Integer.parseInt(bits[countIndex]);
				lineList.put(myDistrict, myCount);
			}

		} catch (Exception e) {
			System.err.println("File input error: " + lineListFilename);
		}
	}
	
	
	
	// Economic
	
	public void load_econ_distrib(String filename){
		economicInteractionDistrib = new HashMap <String, Map<String, Double>> ();
		economicInteractionCumulativeDistrib = new HashMap <String, List<Double>> ();
		econBubbleSize = new HashMap <String, Integer> ();
		orderedEconStatuses = new ArrayList <String> ();
		
		try {
			
			System.out.println("Reading in econ interaction data from " + filename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(filename);

			// Convert our input stream to a BufferedReader
			BufferedReader econDistribData = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = econDistribData.readLine();
			
			// map the header into column names relative to location
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> rawColumnNames = new HashMap <String, Integer> ();
			for(int i = 0; i < header.length; i++){
				rawColumnNames.put(header[i], new Integer(i));
			}
			int bubbleIndex = rawColumnNames.get("Bubble");
			
			while ((s = econDistribData.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				String myTitle = bits[0].toLowerCase();
				System.out.println(bits);
				
				// save bubble info
				econBubbleSize.put(myTitle, Integer.parseInt(bits[bubbleIndex]));
				
				// save interaction info
				HashMap <String, Double> interacts = new HashMap <String, Double> ();
				ArrayList <Double> interactsCum = new ArrayList <Double> ();
				double cumTotal = 0;
				for(int i = bubbleIndex + 1; i < bits.length; i++){
					Double val = Double.parseDouble(bits[i]);
					interacts.put(header[i], val);
					
					cumTotal += val;
					interactsCum.add(cumTotal);
				}
				economicInteractionDistrib.put(myTitle, interacts);
				economicInteractionCumulativeDistrib.put(myTitle, interactsCum);
				
				// save ordering info
				orderedEconStatuses.add(bits[0].toLowerCase());
			}
			
			econDistribData.close();
		} catch (Exception e) {
			System.err.println("File input error: " + econ_interaction_distrib_filename);
		}
	}
	
	public void load_district_data(String districtFilename){
		
		// set up structure to hold transition probability
		dailyTransitionProbs = new ArrayList <Map<String, List<Double>>> ();
		for(int i = 0; i < 7; i++){
			dailyTransitionProbs.add(new HashMap <String, List<Double>> ());
		}
		districtNames = new ArrayList <String> ();


		// set up holders
		districts = new HashMap <String, Location> ();
		//HashSet <String> districtNames = new HashSet <String> ();
		districtNames = new ArrayList <String> ();
		
		try {
			
			System.out.println("Reading in district transfer information from " + districtFilename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(districtFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader districtData = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = districtData.readLine();
			
			// map the header into column names relative to location
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> rawColumnNames = new HashMap <String, Integer> ();
			for(int i = 0; i < header.length; i++){
				rawColumnNames.put(header[i], new Integer(i));
			}
			int weekdayIndex = rawColumnNames.get("weekday");
			int homeregionIndex = rawColumnNames.get("home_region");
			
			// assemble use of district names for other purposes
			for(int i = homeregionIndex + 1; i < header.length; i++){
				districtNames.add(header[i]);
			}
			// set up holders for the information
			
			
			System.out.println("BEGIN READING IN DISTRICTS");
			
			// read in the raw data
			while ((s = districtData.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				
				// extract the day of the week and the district name
				int dayOfWeek = Integer.parseInt(bits[weekdayIndex]);
				String districtName = bits[homeregionIndex];
				
				// save the district name
				//districtNames.add(districtName);
				
				// set up a new set of transfers from the given district
				// the key here is the name of the district, and the value is transition probability
				HashMap <String, Double> transferFromDistrict = new HashMap <String, Double> ();
				ArrayList <Double> cumulativeProbTransfer = new ArrayList <Double> ();
				for(int i = homeregionIndex + 1; i < bits.length; i++){
					transferFromDistrict.put(header[i], Double.parseDouble(bits[i]));
					cumulativeProbTransfer.add(Double.parseDouble(bits[i])/100.);
				}

				// save the transitions
//				dailyTransitionProbs.get(dayOfWeek).put( districtName, transferFromDistrict);
				dailyTransitionProbs.get(dayOfWeek).put( districtName, cumulativeProbTransfer);
			}
			
			// create Locations for each district
			for(String d: districtNames){
				Location l = new Location(d);
				districts.put(d, l);
			}
			
			// clean up after ourselves
			districtData.close();
		} catch (Exception e) {
			System.err.println("File input error: " + districtFilename);
		}
	}

	/**
	 * 
	 * @param econFilename
	 * @return
	 */
	public HashMap <String, Double> readInEconomicData(String econFilename, String statusColName, String probColName){
		try {
			
			// set up structure to hold the data
			HashMap <String, Double> econData = new HashMap <String, Double> ();
			
			System.out.println("Reading in data from " + econFilename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(econFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader econDataFile = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = econDataFile.readLine();
			
			// map the header into column names relative to location
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> columnNames = parseHeader(header);
			
			int statusIndex = columnNames.get(statusColName);
			int probIndex = columnNames.get(probColName);
			
			// set up holders for the information
			
			// read in the raw data
			while ((s = econDataFile.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				econData.put(bits[statusIndex].toLowerCase(), Double.parseDouble(bits[probIndex]));
			}
			
			// cleanup
			econDataFile.close();
			
			// report success
			System.out.println("...Finished reading in from " + econFilename);
			
			return econData;
		} catch (Exception e) {
			System.err.println("File input error: " + econFilename);
		}
		return null;
	}

	public void load_district_leaving_data(String districtFilename){
		
		// set up structure to hold transition probability
		districtLeavingProb = new HashMap <Location, Double> ();
		
		try {
			
			System.out.println("Reading in district transfer information from " + districtFilename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(districtFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader districtData = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = districtData.readLine();
			
			// map the header into column names relative to location
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> rawColumnNames = new HashMap <String, Integer> ();
			for(int i = 0; i < header.length; i++){
				rawColumnNames.put(header[i], new Integer(i));
			}
			int locationIndex = rawColumnNames.get("district_id");
			int probIndex = rawColumnNames.get("pctdif_distance");
			

			System.out.println("BEGIN READING IN LEAVING PROBABILITIES");
			
			// read in the raw data
			while ((s = districtData.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				
				// extract the day of the week and the district name
				String dId = bits[locationIndex];
				Double prob = Double.parseDouble(bits[probIndex]);
				
				// extract the associated Location and check for problems
				Location myLocation = districts.get(dId);
				if(myLocation == null){
					System.out.println("WARNING: no districted named " + dId + " as requested in district leaving file. Skipping!");
					continue;
				}
				
				districtLeavingProb.put(myLocation, prob);
			}

			// clean up after ourselves
			districtData.close();
		} catch (Exception e) {
			System.err.println("File input error: " + districtFilename);
		}
	}

	// file import helper utilities
	
	/**
	 * Helper function to allow user to read in comma separated values, respecting double quotes.
	 * @param s the raw String
	 * @return An array of Strings, stripped of quotation marks and whitespace
	 */
	public static String [] splitRawCSVString(String s){
		String [] myString =s.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		for(int i = 0; i < myString.length; i++){
			String transfer = myString[i];
			myString[i] = transfer.replaceAll("\"", "").trim();
		}
		return myString;
	}
	
	/**
	 * 
	 * @param rawHeader
	 * @return
	 */
	public static HashMap <String, Integer> parseHeader(String [] rawHeader){
		HashMap <String, Integer> rawColumnNames = new HashMap <String, Integer> ();
		for(int i = 0; i < rawHeader.length; i++){
			String colName = rawHeader[i];
			rawColumnNames.put(colName, new Integer(i));
		}
		return rawColumnNames;
	}
	
	// END file import helper utilities
	
	//
	// END DATA IMPORT UTILITIES
	//

	//
	// DATA ACCESS UTILITIES
	//
	
	// Epidemic data access
	public double getSuspectabilityByAge(double age){
		// TODO make specific
		return infection_beta;
	}
	
	// Mobility data access
	
	/**
	 * Public function to allow Persons to query economic mobility data based on day of week.
	 * @param day Day of the week as an integer (0-4 are weekdays, 5-6 are weekends)
	 * @param econ_status Name of economic_status
	 * @return
	 */
	public double getEconProbByDay(int day, String econ_status){
		if(day < 5){
			if(!economic_status_weekday_movement_prob.containsKey(econ_status))
				return -1;
			else return economic_status_weekday_movement_prob.get(econ_status);
		}
		else {
			if(!economic_status_otherday_movement_prob.containsKey(econ_status))
				return -1;
			else return economic_status_otherday_movement_prob.get(econ_status);
		}
	}

	/**
	 * Get the probability of leaving a district.
	 * @param l A location, which may be a sub-location of the District. In this case, the module
	 * finds the "District" super-Location of the Location and returns the associated chance of leaving.
	 * @return
	 */
	public double getProbToLeaveDistrict(Location l){
		Location dummy = l;
		while(districtLeavingProb.get(dummy) == null && dummy.getSuper() != null)
			dummy = dummy.getSuper();
		return districtLeavingProb.get(dummy);
	}
	
	public Location getTargetMoveDistrict(Person p, int day, double rand){
		
		// extract current District from the location
		Location l = p.getLocation();
		Location dummy = l;
		while(districtLeavingProb.get(dummy) == null && dummy.getSuper() != null)
			dummy = dummy.getSuper();

		// get the transition probability for the given district on the given day
		ArrayList <Double> myTransitionProbs = (ArrayList <Double>) dailyTransitionProbs.get(day).get(dummy.getId());
		
		// now compare the random roll to the probability distribution.
		for(int i = 0; i < myTransitionProbs.size(); i++){
			if(rand <= myTransitionProbs.get(i)) // hooray! We've found the right bucket!
				return districts.get(districtNames.get(i)); // return the location associated with this position
		}
		
		return null; // there has been an error!
	}
	
	/**
	 * Generalised check for the weekdays. Here, we assume the week begins with, for example, Monday and continues
	 * into Friday, i.e. days 0-4 of the week with Sat-Sun as 5 and 6. This can be changed if, for example, Friday
	 * is a rest day without needing to reformat other datasets.
	 * @param day - day of the week (here, 0 = Monday, 6 = Sunday)
	 * @return whether the day is a weekday
	 */
	public static boolean isWeekday(int day){
		if(day < 5)
			return true;
		else return false;
	}
	
}