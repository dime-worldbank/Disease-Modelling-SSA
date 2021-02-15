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
	
	HashMap <String, Double> economic_status_weekday_movement_prob;
	HashMap <String, Double> economic_status_otherday_movement_prob;
	double mild_symptom_movement_prob;
	
	// data files
	
	public String population_filename = "/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/preprocessed/census/sample_1500.txt";
	public String district_transition_filename = "/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/preprocessed/mobility/New Files/daily_region_transition_probability-new-district-post-lockdown_i5.csv";	

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
	
	// holders for locational data
	HashMap <String, Location> districts;
	ArrayList <Map<String, Map<String, Double>>> dailyTransitionProbs;
	
	
	public Params(){
		load_district_data(district_transition_filename);
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
			String [] header = s.split(",");
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
				String [] bits = s.split(",");
				
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
}