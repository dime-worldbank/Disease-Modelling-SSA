package sim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import objects.Location;

public class Params {
	
	double r0 = 3.0;
	
	public HashMap <String, Double> economic_status_weekday_movement_prob;
	public HashMap <String, Double> economic_status_otherday_movement_prob;
	double mild_symptom_movement_prob;
	

	// holders for locational data
	HashMap <String, Location> districts;
	ArrayList <Map<String, Map<String, Double>>> dailyTransitionProbs;
	
	// data files
	
	public String population_filename = "/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/preprocessed/census/sample_1500.txt";
	public String district_transition_filename = "/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/preprocessed/mobility/New Files/daily_region_transition_probability-new-district-post-lockdown_i5.csv";	

	public String economic_status_weekday_movement_prob_filename = 
			"/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/configs/ECONOMIC_STATUS_WEEKDAY_MOVEMENT_PROBABILITY.txt";
	public String economic_status_otherday_movement_prob_filename = 
			"/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/configs/ECONOMIC_STATUS_OTHER_DAY_MOVEMENT_PROBABILITY.txt";
	
	
	// params used by other objects
	
	public static int state_susceptible = 0;
	public static int state_infected = 1;
	public static int state_contagious = 2;
	public static int state_recovered = 3;
	public static int state_dead = 4;

	public static int dead_location_id = -1;
	
	public static int clinical_not_hospitalized = 0;
	public static int clinical_hospitalized = 1;
	public static int clinical_critical = 2;
	public static int clinical_released_or_dead = 3;
	
	public static int symptom_none = -1;
	public static int symptom_asymptomatic = 0;
	public static int symptom_symptomatic = 1;

	// time
	public static int hour_start_day_weekday = 8;
	public static int hour_start_day_otherday = 8;
	
	public static int hour_end_day_weekday = 16;
	public static int hour_end_day_otherday = 16;
	
	
	
	public Params(){
		load_district_data(district_transition_filename);
		
		economic_status_weekday_movement_prob = readInEconomicData(economic_status_weekday_movement_prob_filename);
		economic_status_otherday_movement_prob = readInEconomicData(economic_status_otherday_movement_prob_filename);
	}
	
	public void load_district_data(String districtFilename){
		
		// set up structure to hold transition probability
		dailyTransitionProbs = new ArrayList <Map<String, Map<String, Double>>> ();
		for(int i = 0; i < 7; i++){
			dailyTransitionProbs.add(new HashMap <String, Map<String, Double>> ());
		}

		// set up holders
		districts = new HashMap <String, Location> ();
		HashSet <String> districtNames = new HashSet <String> ();
		
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
			
			// set up holders for the information
			
			
			System.out.println("BEGIN READING IN DISTRICTS");
			
			// read in the raw data
			while ((s = districtData.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				
				// extract the day of the week and the district name
				int dayOfWeek = Integer.parseInt(bits[weekdayIndex]);
				String districtName = bits[homeregionIndex];
				
				// save the district name
				districtNames.add(districtName);
				
				// set up a new set of transfers from the given district
				// the key here is the name of the district, and the value is transition probability
				HashMap <String, Double> transferFromDistrict = new HashMap <String, Double> ();
				for(int i = homeregionIndex + 1; i < bits.length; i++)
					transferFromDistrict.put(header[i], Double.parseDouble(bits[i]));

				// save the transitions
				dailyTransitionProbs.get(dayOfWeek).put( districtName, transferFromDistrict);
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

	public HashMap <String, Double> readInEconomicData(String econFilename){
		try {
			
			// set up structure to hold the data
			HashMap <String, Double> econData = new HashMap <String, Double> ();
			
			System.out.println("Reading in econ mobility data from " + econFilename);
			
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
			
			int statusIndex = columnNames.get("economic_status");
			int probIndex = columnNames.get("movement_probability");
			
			// set up holders for the information
			
			// read in the raw data
			while ((s = econDataFile.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				econData.put(bits[statusIndex], Double.parseDouble(bits[probIndex]));
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
}