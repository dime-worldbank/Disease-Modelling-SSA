package uk.ac.ucl.protecs.sim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import uk.ac.ucl.protecs.objects.*;
import uk.ac.ucl.protecs.objects.Location.LocationCategory;
import uk.ac.ucl.protecs.objects.Person.OCCUPATION;

public class Params {
	

	public boolean verbose = true;
	
	public double infection_beta = 0.016;
	public double rate_of_spurious_symptoms = 0.004;
	public int lineListWeightingFactor = 1; // the line list contains only detected instances, which can be biased 
											// - weight this if we suspect it's undercounting
	public boolean setting_perfectMixing = false; // if TRUE: there are no work or social bubbles; individuals have
	// equal chance of interacting with anyone else in the simulation
	public double prob_go_to_work = 0.8;

	public boolean demography = false;

	
	public HashMap <String, Double> economic_status_weekday_movement_prob;
	public HashMap <String, Double> economic_status_otherday_movement_prob;
	
	public HashMap <String, List<Double>> workplaceContactProbability;
	public ArrayList <String> occupationNames;
	public ArrayList <Integer> workplaceContactCounts;
	public static int community_num_interaction_perTick = 5;

	public static int community_bubble_size = 30;
	
	
	double mild_symptom_movement_prob;
	
	// export parameters
	String [] exportParams = new String [] {"time", "infected_count", "num_died",
			"num_recovered", "num_exposed", 
			"num_contagious", "num_severe", "num_critical", "num_symptomatic", "num_asymptomatic"};

	// holders for locational data
	
	HashMap <String, Location> adminZones;
	ArrayList <String> adminZoneNames;
	ArrayList <Map<String, List<Double>>> dailyTransitionPrelockdownProbs;
	ArrayList <Map<String, List<Double>>> dailyTransitionLockdownProbs;
	
	// holders for economic-related data
	
	HashMap <String, Map<String, Double>> economicInteractionDistrib;
	HashMap <String, List<Double>> economicInteractionCumulativeDistrib;
	HashMap <String, Integer> econBubbleSize;
	ArrayList <String> orderedEconStatuses;
	
	// holders for epidemic-related data
	
	HashMap <Location, Integer> lineList;
	ArrayList <Double> lockdownChangeList;
	
	// holders for testing
	public ArrayList <Integer> test_dates;
	public ArrayList <Integer> number_of_tests_per_day;
	public ArrayList <String> admin_zones_to_test_in;
	
	// holders for workplace bubble constraints
	public HashMap <OCCUPATION, LOCATIONTYPE> OccupationConstraintList;
	
	// parameters drawn from Kerr et al 2020 - https://www.medrxiv.org/content/10.1101/2020.05.10.20097469v3.full.pdf
	public ArrayList <Integer> infection_age_params;
	public ArrayList <Double> infection_r_sus_by_age;
	public ArrayList <Double> infection_p_sym_by_age;
	public ArrayList <Double> infection_p_sev_by_age;
	public ArrayList <Double> infection_p_cri_by_age;
	public ArrayList <Double> infection_p_dea_by_age;

	// also from Kerr et al 2020, translated from days into ticks 
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
	public double symptomaticToRecovery_mean =	8.0 * ticks_per_day;
	public double symptomaticToRecovery_std =	2.0 * ticks_per_day;
	public double severeToRecovery_mean =		18.1 * ticks_per_day;
	public double severeToRecovery_std =		6.3 * ticks_per_day;
	public double criticalToRecovery_mean =		18.1 * ticks_per_day;
	public double criticalToRecovery_std =		6.3 * ticks_per_day;
	
	// all cause mortality parameters, currently pulled out my arse
	public ArrayList <Integer> all_cause_death_age_params;
	public ArrayList <Double> prob_death_by_age_male;
	public ArrayList <Double> prob_death_by_age_female;
	public ArrayList <Integer> birth_age_params;
	public ArrayList <Double> prob_birth_by_age;
	// data files
	
	public String dataDir = "";
	
	
	public String population_filename = "";
	public String admin_zone_transition_LOCKDOWN_filename = "";
	public String admin_zone_transition_PRELOCKDOWN_filename = "";
	public String admin_zone_leaving_filename = "";
	
	public String economic_status_weekday_movement_prob_filename = "";
	public String economic_status_otherday_movement_prob_filename = "";
	public String economic_status_num_daily_interacts_filename = "";
	
	public String econ_interaction_distrib_filename = "";
	
	public String line_list_filename = "";
	public String infection_transition_params_filename = "";
	public String lockdown_changeList_filename = "";
	public String all_cause_mortality_filename = "";
	public String birth_rate_filename = "";
	
	public String testDataFilename = "";
	public String testLocationFilename = "";
	
	public String workplaceContactsFilename = "";
	public String workplaceConstraintsFilename= "";
		
	
	// time
	public static int hours_per_tick = 4; // the number of hours each tick represents
	public static int ticks_per_day = 24 / hours_per_tick;
	public static int ticks_per_week = ticks_per_day * 7;
	public static int ticks_per_year = ticks_per_day * 365;
	
	public static int hour_start_day_weekday = 8 / hours_per_tick;
	public static int hour_start_day_otherday = 8 / hours_per_tick;
	
	public static int hour_end_day_weekday = 16 / hours_per_tick;
	public static int hour_end_day_otherday = 16 / hours_per_tick;
	
	public static int hours_at_work_weekday = 8 / hours_per_tick;
	public static int hours_sleeping = 8 / hours_per_tick;
	
	
	public Params(String paramsFilename, boolean isVerbose){
		
		this.verbose = isVerbose;
		readInParamFile(paramsFilename);
		
		dailyTransitionLockdownProbs = load_admin_zone_data(dataDir + admin_zone_transition_LOCKDOWN_filename);
		dailyTransitionPrelockdownProbs = load_admin_zone_data(dataDir + admin_zone_transition_PRELOCKDOWN_filename);
		
		economic_status_weekday_movement_prob = readInEconomicData(dataDir + economic_status_weekday_movement_prob_filename, "economic_status", "movement_probability");
		economic_status_otherday_movement_prob = readInEconomicData(dataDir + economic_status_otherday_movement_prob_filename, "economic_status", "movement_probability");
				
		load_line_list(dataDir  + line_list_filename);
		load_lockdown_changelist(dataDir +  lockdown_changeList_filename);
		load_infection_params(dataDir  + infection_transition_params_filename);

		load_all_cause_mortality_params(dataDir + all_cause_mortality_filename);
		load_all_birthrate_params(dataDir + birth_rate_filename);
		
		// load the workplace contacts data
		load_workplace_contacts(dataDir + workplaceContactsFilename);
		// load in the file to determine which occupations will have reduced mobility
		load_occupational_constraints(dataDir + workplaceConstraintsFilename);
		
	}
	//
	// DATA IMPORT UTILITIES
	//
	
	public void readInParamFile(String paramFilename) {
		if(verbose)
			System.out.println("Reading in data from " + paramFilename);
		
		// Open the tracts file
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(paramFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader paramFile = new BufferedReader(new InputStreamReader(fstream));
			String s;

			while ((s = paramFile.readLine()) != null) {
				
				// skip comments
				if(s.length() == 0 || s.charAt(0) == '#')
					continue;
				
				// extract all other parameters
				String [] bits = s.split(":");
				Field f = this.getClass().getDeclaredField(bits[0].trim());
				f.setAccessible(true);
				String myVal = bits[1].trim();
				try {
					f.set(this, Integer.parseInt(myVal));
				} catch (Exception e){
					if(myVal.equals("true") || myVal.equals("false"))
						f.set(this, Boolean.parseBoolean(myVal));
					else
						f.set(this, myVal);	
				}
			}			

		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	
	// Epidemic
	
	public void load_line_list(String lineListFilename){
		try {
			
			if(verbose)
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
			int adminZoneNameIndex = columnNames.get("admin_zone");
			int countIndex = columnNames.get("count");
			
			// set up data container
			lineList = new HashMap <Location, Integer> ();
			
			// read in the raw data
			while ((s = lineListDataFile.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				Location myAdminZone = adminZones.get(bits[adminZoneNameIndex]);
				Integer myCount = Integer.parseInt(bits[countIndex]);
				lineList.put(myAdminZone, myCount);
			}
			assert (lineList.size() > 0): "lineList not loaded";

		} catch (Exception e) {
			System.err.println("File input error: " + lineListFilename);
		}
	}
	
	public void load_lockdown_changelist(String lockdownChangelistFilename) {
		try {
			
			if(verbose)
				System.out.println("Reading in data from " + lockdownChangelistFilename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(lockdownChangelistFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader lineListDataFile = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = lineListDataFile.readLine();

			// map the header into column names relative to location
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> columnNames = parseHeader(header);
			int dayIndex = columnNames.get("day");
			int levelIndex = columnNames.get("level");
			
			// set up data container
			lockdownChangeList = new ArrayList <Double> ();
			
			// read in the raw data
			boolean started = false;
			while ((s = lineListDataFile.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				int dayVal = Integer.parseInt(bits[dayIndex]);
				Integer myLevel = Integer.parseInt(bits[levelIndex]);
				if(!started && myLevel > 0) {
					lockdownChangeList.add((double)dayVal);
					started = true;
				}
				else if(started) {
					lockdownChangeList.add((double)dayVal);
					started = false;
				}
			}

		} catch (Exception e) {
			System.err.println("File input error: " + lockdownChangelistFilename);
		}
	}
	
	
	public void load_workplace_contacts(String workplaceFilename){

		// set up structure to hold transition probability
		workplaceContactProbability = new HashMap <String, List<Double>> ();

		// set up structure to hold transition probability
		occupationNames = new ArrayList <String> ();
		workplaceContactCounts = new ArrayList <Integer>();

		
		try {
			
			if(verbose)
				System.out.println("Reading in workplace contact data from " + workplaceFilename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(workplaceFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader workplaceData = new BufferedReader(new InputStreamReader(fstream));
			
			String s;

			// extract the header
			s = workplaceData.readLine();
			
			// map the header into column names
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> rawColumnNames = new HashMap <String, Integer> ();
			for(int i = 0; i < header.length; i++){
				rawColumnNames.put(header[i], new Integer(i));
			}
			int occupationIndex = rawColumnNames.get("Work contacts yesterday");
			
			// assemble use of district names for other purposes
			for(int i = occupationIndex + 1; i < header.length; i++){
				workplaceContactCounts.add(Integer.parseInt(header[i]));
			}
			// set up holders for the information
			
			
			if(verbose)
				System.out.println("BEGIN READING IN WORKPLACE CONTACTS");
			
			
			// read in the raw data
			while ((s = workplaceData.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				
				// extract the occupation
				String occupationName = bits[occupationIndex];
				
				// set up a new set workplace contact count probabilities
				ArrayList <Double> cumulativeProbCount = new ArrayList <Double> ();
				for(int i = occupationIndex + 1; i < bits.length; i++){
					cumulativeProbCount.add(Double.parseDouble(bits[i]));
				}

				// save the transitions
//				dailyTransitionProbs.get(dayOfWeek).put( districtName, transferFromDistrict);
				workplaceContactProbability.put(occupationName, cumulativeProbCount);
			}
			// clean up after ourselves
			workplaceData.close();
		} catch (Exception e) {
			System.err.println("File input error: " + workplaceFilename);
		}
		
	}
	
	private void load_occupational_constraints(String workplaceConstraints) {

		// set up structure to hold transition probability
		OccupationConstraintList = new HashMap<OCCUPATION, LOCATIONTYPE>();
		
		try {
			
			if(verbose)
				System.out.println("Reading in workplace constraints from " + workplaceConstraints);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(workplaceConstraints);

			// Convert our input stream to a BufferedReader
			BufferedReader workplaceData = new BufferedReader(new InputStreamReader(fstream));
			
			String s;

			// extract the header
			s = workplaceData.readLine();
			
			// map the header into column names
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> rawColumnNames = new HashMap <String, Integer> ();
			for(int i = 0; i < header.length; i++){
				rawColumnNames.put(header[i], new Integer(i));
			}
			int occupationIndex = rawColumnNames.get("Occupation");			
			int constraintIndex = rawColumnNames.get("Constraint");			
			if(verbose)
				System.out.println("BEGIN READING IN OCCUPATION CONSTRAINTS");
			
			
			// read in the raw data
			while ((s = workplaceData.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				
				// extract the occupation
				OCCUPATION occupationName = OCCUPATION.getValue(bits[occupationIndex].toLowerCase());
				LOCATIONTYPE locationName = LOCATIONTYPE.getValue(bits[constraintIndex]);

				// save the transitions
				OccupationConstraintList.put(occupationName, locationName);
			}
			// clean up after ourselves
			workplaceData.close();
		} catch (Exception e) {
			System.err.println("File input error: " + workplaceConstraints);
		}
		
	}

	
	public void load_infection_params(String filename){
		try {
			
			if(verbose)
				System.out.println("Reading in data from " + filename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(filename);

			// Convert our input stream to a BufferedReader
			BufferedReader lineListDataFile = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = lineListDataFile.readLine();

			// map the header into column names relative to location
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> columnNames = parseHeader(header);
			
			// set up data container
			infection_age_params = new ArrayList <Integer> ();
			infection_r_sus_by_age = new ArrayList <Double> ();
			infection_p_sym_by_age = new ArrayList <Double> ();
			infection_p_sev_by_age = new ArrayList <Double> ();
			infection_p_cri_by_age = new ArrayList <Double> ();
			infection_p_dea_by_age = new ArrayList <Double> ();

			
			// read in the raw data
			while ((s = lineListDataFile.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				
				// assemble the age data
				String [] ageRange = bits[0].split("-");
				int maxAge = Integer.MAX_VALUE;
				if(ageRange.length > 1){
					maxAge = Integer.parseInt(ageRange[1]); // take the maximum
				}
				infection_age_params.add(maxAge);
				
				double r_sus  = Double.parseDouble(bits[1]),
						p_sym = Double.parseDouble(bits[2]),
						p_sev = Double.parseDouble(bits[3]),
						p_cri = Double.parseDouble(bits[4]),
						p_dea = Double.parseDouble(bits[5]);
				
				// they are read in as ABSOLUTE values - convert to relative values!
				p_dea /= p_cri;
				p_cri /= p_sev;
				p_sev /= p_sym;
				
				// store the values
				infection_r_sus_by_age.add(r_sus);
				infection_p_sym_by_age.add(p_sym);
				infection_p_sev_by_age.add(p_sev);
				infection_p_cri_by_age.add(p_cri);
				infection_p_dea_by_age.add(p_dea);

			}
			assert (infection_r_sus_by_age.size() > 0): "infection_r_sus_by_age not loaded";
			assert (infection_p_sym_by_age.size() > 0): "infection_p_sym_by_age not loaded";
			assert (infection_p_sev_by_age.size() > 0): "infection_p_sev_by_age not loaded";
			assert (infection_p_cri_by_age.size() > 0): "infection_p_cri_by_age not loaded";
			assert (infection_p_dea_by_age.size() > 0): "infection_p_dea_by_age not loaded";


			} catch (Exception e) {
				System.err.println("File input error: " + filename);
			}
		}
	
	public void load_all_cause_mortality_params(String filename) {
		try {
			
			if(verbose)
				System.out.println("Reading in all cause mortality data from " + filename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(filename);

			// Convert our input stream to a BufferedReader
			BufferedReader lineListDataFile = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = lineListDataFile.readLine();

			// map the header into column names relative to location
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> columnNames = parseHeader(header);
			
			// set up data container
			
			all_cause_death_age_params = new ArrayList<Integer> ();
			prob_death_by_age_male = new ArrayList <Double> ();
			prob_death_by_age_female = new ArrayList <Double> ();

			
			// read in the raw data
			while ((s = lineListDataFile.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				
				// assemble the age data
				String [] ageRange = bits[0].split("-");
				int maxAge = Integer.MAX_VALUE;
				if(ageRange.length > 1){
					maxAge = Integer.parseInt(ageRange[1]); // take the maximum
				}
				all_cause_death_age_params.add(maxAge);
				
				double male_prob_death  = Double.parseDouble(bits[1]),
						female_prob_death = Double.parseDouble(bits[2]);
				
				// store the values
				prob_death_by_age_male.add(male_prob_death);
				prob_death_by_age_female.add(female_prob_death);

			}
			} catch (Exception e) {
				System.err.println("File input error: " + filename);
			}
	}
	
	public void load_all_birthrate_params(String filename) {
		try {
			
			if(verbose)
				System.out.println("Reading in birth rate data from " + filename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(filename);

			// Convert our input stream to a BufferedReader
			BufferedReader lineListDataFile = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = lineListDataFile.readLine();

			// map the header into column names relative to location
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> columnNames = parseHeader(header);
			
			// set up data container
			
			birth_age_params = new ArrayList<Integer> ();
			prob_birth_by_age = new ArrayList <Double> ();

			
			// read in the raw data
			while ((s = lineListDataFile.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				
				// assemble the age data
				String [] ageRange = bits[0].split("-");
				int maxAge = Integer.MAX_VALUE;
				if(ageRange.length > 1){
					maxAge = Integer.parseInt(ageRange[1]); // take the maximum
				}
				birth_age_params.add(maxAge);
				
				double female_prob_birth = Double.parseDouble(bits[1]);
				
				// store the values
				prob_birth_by_age.add(female_prob_birth);

			}
			} catch (Exception e) {
				System.err.println("File input error: " + filename);
			}
	}

	// Economic
	// ------------------- This form of including workplace bubbles in the model has been replaced ------------------------------
//	public void load_econStatus_distrib(String filename){
//		economicInteractionDistrib = new HashMap <String, Map<String, Double>> ();
//		economicInteractionCumulativeDistrib = new HashMap <String, List<Double>> ();
//		econBubbleSize = new HashMap <String, Integer> ();
//		orderedEconStatuses = new ArrayList <String> ();
//		
//		try {
//			
//			if(verbose)
//				System.out.println("Reading in econ interaction data from " + filename);
//			
//			// Open the tracts file
//			FileInputStream fstream = new FileInputStream(filename);
//
//			// Convert our input stream to a BufferedReader
//			BufferedReader econDistribData = new BufferedReader(new InputStreamReader(fstream));
//			String s;
//
//			// extract the header
//			s = econDistribData.readLine();
//			
//			// map the header into column names relative to location
//			String [] header = splitRawCSVString(s);
//			HashMap <String, Integer> rawColumnNames = new HashMap <String, Integer> ();
//			for(int i = 0; i < header.length; i++){
//				rawColumnNames.put(header[i], new Integer(i));
//			}
//			//int bubbleIndex = rawColumnNames.get("Bubble");
//			
//			while ((s = econDistribData.readLine()) != null) {
//				String [] bits = splitRawCSVString(s);
//				String myTitle = bits[0].toLowerCase();
//				if(verbose)
//					System.out.println(bits);
//				
//				// save bubble info
//				//econBubbleSize.put(myTitle, Integer.parseInt(bits[bubbleIndex]));
//				
//				// save interaction info
//				HashMap <String, Double> interacts = new HashMap <String, Double> ();
//				ArrayList <Double> interactsCum = new ArrayList <Double> ();
//				double cumTotal = 0;
//				for(int i = 1;//bubbleIndex + 1; 
//						i < bits.length; i++){
//					Double val = Double.parseDouble(bits[i]);
//					interacts.put(header[i], val);
//					
//					cumTotal += val;
//					interactsCum.add(cumTotal);
//				}
//				economicInteractionDistrib.put(myTitle, interacts);
//				economicInteractionCumulativeDistrib.put(myTitle, interactsCum);
//				
//				// save ordering info
//				orderedEconStatuses.add(bits[0].toLowerCase());
//			}
//			assert (economicInteractionDistrib.size() > 0): "economicInteractionDistrib not loaded";
//			assert (economicInteractionCumulativeDistrib.size() > 0): "economicInteractionCumulativeDistrib not loaded";
//			assert (orderedEconStatuses.size() > 0): "orderedEconStatuses not loaded";			
//			econDistribData.close();
//		} catch (Exception e) {
//			System.err.println("File input error: " + econ_interaction_distrib_filename);
//		}
//	}
	
	public ArrayList <Map<String, List<Double>>> load_admin_zone_data(String adminZoneFilename){
		
		// set up structure to hold transition probability
		ArrayList <Map<String, List<Double>>> probHolder = new ArrayList <Map<String, List<Double>>> ();
		for(int i = 0; i < 7; i++){
			probHolder.add(new HashMap <String, List<Double>> ());
		}
		adminZoneNames = new ArrayList <String> ();


		// set up holders
		adminZones = new HashMap <String, Location> ();
		adminZoneNames = new ArrayList <String> ();
		
		try {
			
			if(verbose)
				System.out.println("Reading in admin zone transfer information from " + adminZoneFilename);
			
			// Open the tracts file
			FileInputStream fstream = new FileInputStream(adminZoneFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader adminZoneData = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// extract the header
			s = adminZoneData.readLine();
			
			// map the header into column names relative to location
			String [] header = splitRawCSVString(s);
			HashMap <String, Integer> rawColumnNames = new HashMap <String, Integer> ();
			for(int i = 0; i < header.length; i++){
				rawColumnNames.put(header[i], new Integer(i));
			}
			int weekdayIndex = rawColumnNames.get("weekday");
			int homeregionIndex = rawColumnNames.get("home_region");
			
			// assemble use of admin zone names for other purposes
			for(int i = homeregionIndex + 1; i < header.length; i++){
				adminZoneNames.add(header[i]);
			}
			// set up holders for the information
			
			
			if(verbose)
				System.out.println("BEGIN READING IN ADMIN ZONES");
			
			// read in the raw data
			while ((s = adminZoneData.readLine()) != null) {
				String [] bits = splitRawCSVString(s);
				
				// extract the day of the week and the admin zone name
				int dayOfWeek = Integer.parseInt(bits[weekdayIndex]);
				String adminZoneName = bits[homeregionIndex];
				
				// set up a new set of transfers from the given admin zone
				// the key here is the name of the admin zone, and the value is transition probability
				HashMap <String, Double> transferFromAdminZone = new HashMap <String, Double> ();
				ArrayList <Double> cumulativeProbTransfer = new ArrayList <Double> ();
				for(int i = homeregionIndex + 1; i < bits.length; i++){
					transferFromAdminZone.put(header[i], Double.parseDouble(bits[i]));
					cumulativeProbTransfer.add(Double.parseDouble(bits[i])/100.);
				}

				// save the transitions
				probHolder.get(dayOfWeek).put( adminZoneName, cumulativeProbTransfer);
			}
			
			// create Locations for each admin zone
			for(String d: adminZoneNames){
				Location l = new Location(d);
				l.setType(LOCATIONCATEGORY.COMMUNITY);
				districts.put(d, l);
			}
			
			// clean up after ourselves
			adminZoneData.close();
			assert (adminZones.size() > 0): "Admin zone not loaded";
			assert (probHolder.size() > 0): "Probability of transition between admin zones not loaded";
			return probHolder;
		} catch (Exception e) {
			System.err.println("File input error: " + adminZoneFilename);
			return null;
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
			
			if(verbose)
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
			if(verbose)
				System.out.println("...Finished reading in from " + econFilename);
			
			return econData;
		} catch (Exception e) {
			System.err.println("File input error: " + econFilename);
		}
		return null;
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
	public double getSuspectabilityByAge(int age){
		return infection_beta * getLikelihoodByAge(infection_r_sus_by_age, infection_age_params, age);
	}
	
	// Mobility data access
	
	/**
	 * Public function to allow Persons to query economic mobility data based on day of week.
	 * @param day Day of the week as an integer (0-4 are weekdays, 5-6 are weekends)
	 * @param econ_status Name of economic_status
	 * @return
	 */
	public double getEconProbByDay(int day, OCCUPATION econ_status){
		String occ_as_string = econ_status.key;
		if(day < 5){
			if(!economic_status_weekday_movement_prob.containsKey(occ_as_string))
				return -1;
			else return economic_status_weekday_movement_prob.get(occ_as_string);
		}
		else {
			if(!economic_status_otherday_movement_prob.containsKey(occ_as_string))
				return -1;
			else return economic_status_otherday_movement_prob.get(occ_as_string);
		}
	}
	
	public int getWorkplaceContactCount(OCCUPATION occupation, double random) {
		List <Double> probabilityOfCount = workplaceContactProbability.get(occupation.key);
		int indexOfCount = 0;
		for (double probability: probabilityOfCount) {
			if (random < probability) break;
			indexOfCount ++;
		}
		return workplaceContactCounts.get(indexOfCount);
	}

	/**
	 * Get the probability of leaving a admin zone.
	 * @param l A location, which may be a sub-location of the admin zone. In this case, the module
	 * finds the "admin zone" super-Location of the Location and returns the associated chance of leaving.
	 * @return
	 */

	
	public Location getTargetMoveAdminZone(Person p, int day, double rand, boolean lockedDown){
		// extract current admin zone from the location
		
		Location l = p.getLocation();
		// If the person is at home or at work then we cannot use their current location to predict movement between admin zones. 
		// If this happens, use the admin zone they are currently in to do so instead
		if (l.getLocationType() != LocationCategory.COMMUNITY) {
			l = l.getSuper();
		}

		// get the transition probability for the given admin zone on the given day
		ArrayList <Double> myTransitionProbs;
		if(lockedDown)
			myTransitionProbs = (ArrayList <Double>) dailyTransitionLockdownProbs.get(day).get(l.getId());
		else
			myTransitionProbs = (ArrayList <Double>) dailyTransitionPrelockdownProbs.get(day).get(l.getId());
		// now compare the random roll to the probability distribution.
		for(int i = 0; i < myTransitionProbs.size(); i++){
			if(rand <= myTransitionProbs.get(i)) {// hooray! We've found the right bucket!
				Location resultLoc = adminZones.get(adminZoneNames.get(i));
				return resultLoc; // return the location associated with this position
			}
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

	public double getLikelihoodByAge(ArrayList <Double> distrib, ArrayList<Integer> compareToDistrib, int age){
		for(int i = 0; i < compareToDistrib.size(); i++){
			if(age < compareToDistrib.get(i))
				return distrib.get(i);
		}
		return -1; // somehow poorly formatted?
	}

}
