package main.java.sim;

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

import main.java.objects.*;


public class Params {
	
	
  public double infection_beta = 0.016D;
  
  public double rate_of_spurious_symptoms = 0.004D;
  
  public int lineListWeightingFactor = 1;
  
  public boolean setting_perfectMixing = false;
  
  public HashMap<String, Double> economic_status_weekday_movement_prob;
  
  public HashMap<String, Double> economic_status_otherday_movement_prob;
  
  public HashMap<String, Double> economic_num_interactions_weekday_perTick;
  
  public static int community_num_interaction_perTick = 3;
  
  public static int community_bubble_size = 30;
  
  double mild_symptom_movement_prob;
  
  String[] exportParams = new String[] { "time", "infected_count", "num_died", 
      "num_recovered", "num_exposed", 
      "num_contagious", "num_severe", "num_critical", "num_symptomatic", "num_asymptomatic" };
  
  HashMap<String, Location> districts;
  
  ArrayList<String> districtNames;
  
  ArrayList<Map<String, List<Double>>> dailyTransitionPrelockdownProbs;
  
  ArrayList<Map<String, List<Double>>> dailyTransitionLockdownProbs;
  
  HashMap<Location, Double> districtLeavingProb;
  
  HashMap<String, Map<String, Double>> economicInteractionDistrib;
  
  HashMap<String, List<Double>> economicInteractionCumulativeDistrib;
  
  HashMap<String, Integer> econBubbleSize;
  
  ArrayList<String> orderedEconStatuses;
  
  HashMap<Location, Integer> lineList;
  
  ArrayList<Double> lockdownChangeList;
  
  public ArrayList<Integer> test_dates;
  
  public ArrayList<Integer> number_of_tests_per_day;
  
  public ArrayList<String> districts_to_test_in;
  
  public ArrayList<Integer> infection_age_params;
  
  public ArrayList<Double> infection_r_sus_by_age;
  
  public ArrayList<Double> infection_p_sym_by_age;
  
  public ArrayList<Double> infection_p_sev_by_age;
  
  public ArrayList<Double> infection_p_cri_by_age;
  
  public ArrayList<Double> infection_p_dea_by_age;
  
  public double exposedToInfectious_mean = 4.5D * ticks_per_day;
  
  public double exposedToInfectious_std = 1.5D * ticks_per_day;
  
  public double infectiousToSymptomatic_mean = 1.1D * ticks_per_day;
  
  public double infectiousToSymptomatic_std = 0.9D * ticks_per_day;
  
  public double symptomaticToSevere_mean = 6.6D * ticks_per_day;
  
  public double symptomaticToSevere_std = 4.9D * ticks_per_day;
  
  public double severeToCritical_mean = 1.5D * ticks_per_day;
  
  public double severeToCritical_std = 2.0D * ticks_per_day;
  
  public double criticalToDeath_mean = 10.7D * ticks_per_day;
  
  public double criticalToDeath_std = 4.8D * ticks_per_day;
  
  public double asymptomaticToRecovery_mean = 8.0D * ticks_per_day;
  
  public double asymptomaticToRecovery_std = 2.0D * ticks_per_day;
  
  public double sympomaticToRecovery_mean = 8.0D * ticks_per_day;
  
  public double sympomaticToRecovery_std = 2.0D * ticks_per_day;
  
  public double severeToRecovery_mean = 18.1D * ticks_per_day;
  
  public double severeToRecovery_std = 6.3D * ticks_per_day;
  
  public double criticalToRecovery_mean = 18.1D * ticks_per_day;
  
  public double criticalToRecovery_std = 6.3D * ticks_per_day;
  
  public String dataDir = "";
  
  public String population_filename = "";
  
  public String district_transition_LOCKDOWN_filename = "";
  
  public String district_transition_PRELOCKDOWN_filename = "";
  
  public String district_leaving_filename = "";
  
  public String economic_status_weekday_movement_prob_filename = "";
  
  public String economic_status_otherday_movement_prob_filename = "";
  
  public String economic_status_num_daily_interacts_filename = "";
  
  public String econ_interaction_distrib_filename = "";
  
  public String line_list_filename = "";
  
  public String infection_transition_params_filename = "";
  
  public String lockdown_changeList_filename = "";
  
  public String testDataFilename = "";
  
  public String testLocationFilename = "";
  
  public static int hours_per_tick = 4;
  
  public static int ticks_per_day = 24 / hours_per_tick;
  
  public static int hour_start_day_weekday = 8 / hours_per_tick;
  
  public static int hour_start_day_otherday = 8 / hours_per_tick;
  
  public static int hour_end_day_weekday = 16 / hours_per_tick;
  
  public static int hour_end_day_otherday = 16 / hours_per_tick;
  
  public static int hours_at_work_weekday = 8 / hours_per_tick;
  
  public static int hours_sleeping = 8 / hours_per_tick;
  
  public Params(String paramsFilename) {
    readInParamFile(paramsFilename);
    this.dailyTransitionLockdownProbs = load_district_data(String.valueOf(this.dataDir) + this.district_transition_LOCKDOWN_filename);
    this.dailyTransitionPrelockdownProbs = load_district_data(String.valueOf(this.dataDir) + this.district_transition_PRELOCKDOWN_filename);
    load_district_leaving_data(String.valueOf(this.dataDir) + this.district_leaving_filename);
    this.economic_status_weekday_movement_prob = readInEconomicData(String.valueOf(this.dataDir) + this.economic_status_weekday_movement_prob_filename, "economic_status", "movement_probability");
    this.economic_status_otherday_movement_prob = readInEconomicData(String.valueOf(this.dataDir) + this.economic_status_otherday_movement_prob_filename, "economic_status", "movement_probability");
    this.economic_num_interactions_weekday_perTick = readInEconomicData(String.valueOf(this.dataDir) + this.economic_status_num_daily_interacts_filename, "economic_status", "interactions");
    load_econStatus_distrib(String.valueOf(this.dataDir) + this.econ_interaction_distrib_filename);
    load_line_list(String.valueOf(this.dataDir) + this.line_list_filename);
    load_lockdown_changelist(String.valueOf(this.dataDir) + this.lockdown_changeList_filename);
    load_infection_params(String.valueOf(this.dataDir) + this.infection_transition_params_filename);
    load_testing(String.valueOf(this.dataDir) + this.testDataFilename);
    load_testing_locations(String.valueOf(this.dataDir) + this.testLocationFilename);
  }
  
  public void readInParamFile(String paramFilename) {
    System.out.println("Reading in data from " + paramFilename);
    try {
      FileInputStream fstream = new FileInputStream(paramFilename);
      BufferedReader paramFile = new BufferedReader(new InputStreamReader(fstream));
      String s;
      while ((s = paramFile.readLine()) != null) {
        if (s.length() == 0 || s.charAt(0) == '#')
          continue; 
        String[] bits = s.split(":");
        Field f = getClass().getDeclaredField(bits[0].trim());
        f.setAccessible(true);
        String myVal = bits[1].trim();
        try {
          f.set(this, Integer.valueOf(Integer.parseInt(myVal)));
        } catch (Exception e) {
          if (myVal.equals("true") || myVal.equals("false")) {
            f.set(this, Boolean.valueOf(Boolean.parseBoolean(myVal)));
            continue;
          } 
          f.set(this, myVal);
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public void load_line_list(String lineListFilename) {
    try {
      System.out.println("Reading in data from " + lineListFilename);
      FileInputStream fstream = new FileInputStream(lineListFilename);
      BufferedReader lineListDataFile = new BufferedReader(new InputStreamReader(fstream));
      String s = lineListDataFile.readLine();
      String[] header = splitRawCSVString(s);
      HashMap<String, Integer> columnNames = parseHeader(header);
      int districtNameIndex = ((Integer)columnNames.get("district")).intValue();
      int countIndex = ((Integer)columnNames.get("count")).intValue();
      this.lineList = new HashMap<>();
      while ((s = lineListDataFile.readLine()) != null) {
        String[] bits = splitRawCSVString(s);
        Location myDistrict = this.districts.get(bits[districtNameIndex]);
        Integer myCount = Integer.valueOf(Integer.parseInt(bits[countIndex]));
        this.lineList.put(myDistrict, myCount);
      } 
      assert this.lineList.size() > 0 : "lineList not loaded";
    } catch (Exception e) {
      System.err.println("File input error: " + lineListFilename);
    } 
  }
  
  public void load_lockdown_changelist(String lockdownChangelistFilename) {
    try {
      System.out.println("Reading in data from " + lockdownChangelistFilename);
      FileInputStream fstream = new FileInputStream(lockdownChangelistFilename);
      BufferedReader lineListDataFile = new BufferedReader(new InputStreamReader(fstream));
      String s = lineListDataFile.readLine();
      String[] header = splitRawCSVString(s);
      HashMap<String, Integer> columnNames = parseHeader(header);
      int dayIndex = ((Integer)columnNames.get("day")).intValue();
      int levelIndex = ((Integer)columnNames.get("level")).intValue();
      this.lockdownChangeList = new ArrayList<>();
      boolean started = false;
      while ((s = lineListDataFile.readLine()) != null) {
        String[] bits = splitRawCSVString(s);
        int dayVal = Integer.parseInt(bits[dayIndex]);
        Integer myLevel = Integer.valueOf(Integer.parseInt(bits[levelIndex]));
        if (!started && myLevel.intValue() > 0) {
          this.lockdownChangeList.add(Double.valueOf(dayVal));
          started = true;
          continue;
        } 
        if (started) {
          this.lockdownChangeList.add(Double.valueOf(dayVal));
          started = false;
        } 
      } 
    } catch (Exception e) {
      System.err.println("File input error: " + lockdownChangelistFilename);
    } 
  }
  
  public void load_testing(String testDataFilename) {
    try {
      System.out.println("Reading in testing data from " + testDataFilename);
      FileInputStream fstream = new FileInputStream(testDataFilename);
      BufferedReader testingDataFile = new BufferedReader(new InputStreamReader(fstream));
      String s = testingDataFile.readLine();
      String[] header = splitRawCSVString(s);
      HashMap<String, Integer> columnNames = parseHeader(header);
      int dayIndex = ((Integer)columnNames.get("date")).intValue();
      int number_of_tests = ((Integer)columnNames.get("number_of_tests")).intValue();
      this.test_dates = new ArrayList<>();
      this.number_of_tests_per_day = new ArrayList<>();
      while ((s = testingDataFile.readLine()) != null) {
        String[] bits = splitRawCSVString(s);
        int dayVal = Integer.parseInt(bits[dayIndex]);
        Integer tests_on_day = Integer.valueOf(Integer.parseInt(bits[number_of_tests]));
        this.test_dates.add(Integer.valueOf(dayVal));
        this.number_of_tests_per_day.add(tests_on_day);
      } 
      assert this.number_of_tests_per_day.size() > 0 : "Number of tests per day not loaded";
    } catch (Exception e) {
      System.err.println("File input error: " + testDataFilename);
    } 
  }
  
  public void load_testing_locations(String testLocationsFilename) {
    try {
      System.out.println("Reading in testing locations from " + testLocationsFilename);
      FileInputStream fstream = new FileInputStream(testLocationsFilename);
      BufferedReader testingDataFile = new BufferedReader(new InputStreamReader(fstream));
      String s = testingDataFile.readLine();
      String[] header = splitRawCSVString(s);
      HashMap<String, Integer> columnNames = parseHeader(header);
      int district_numbers = ((Integer)columnNames.get("number")).intValue();
      this.districts_to_test_in = new ArrayList<>();
      while ((s = testingDataFile.readLine()) != null) {
        String[] bits = splitRawCSVString(s);
        String district_to_test_in = "d_" + bits[district_numbers];
        this.districts_to_test_in.add(district_to_test_in);
      } 
      assert this.districts_to_test_in.size() > 0 : "Number of districts to test in not loaded";
    } catch (Exception e) {
      System.err.println("File input error: " + testLocationsFilename);
    } 
  }
  
  public void load_infection_params(String filename) {
    try {
      System.out.println("Reading in data from " + filename);
      FileInputStream fstream = new FileInputStream(filename);
      BufferedReader lineListDataFile = new BufferedReader(new InputStreamReader(fstream));
      String s = lineListDataFile.readLine();
      String[] header = splitRawCSVString(s);
      HashMap<String, Integer> columnNames = parseHeader(header);
      this.infection_age_params = new ArrayList<>();
      this.infection_r_sus_by_age = new ArrayList<>();
      this.infection_p_sym_by_age = new ArrayList<>();
      this.infection_p_sev_by_age = new ArrayList<>();
      this.infection_p_cri_by_age = new ArrayList<>();
      this.infection_p_dea_by_age = new ArrayList<>();
      while ((s = lineListDataFile.readLine()) != null) {
        String[] bits = splitRawCSVString(s);
        String[] ageRange = bits[0].split("-");
        int maxAge = Integer.MAX_VALUE;
        if (ageRange.length > 1)
          maxAge = Integer.parseInt(ageRange[1]); 
        this.infection_age_params.add(Integer.valueOf(maxAge));
        double r_sus = Double.parseDouble(bits[1]);
        double p_sym = Double.parseDouble(bits[2]);
        double p_sev = Double.parseDouble(bits[3]);
        double p_cri = Double.parseDouble(bits[4]);
        double p_dea = Double.parseDouble(bits[5]);
        p_dea /= p_cri;
        p_cri /= p_sev;
        p_sev /= p_sym;
        this.infection_r_sus_by_age.add(Double.valueOf(r_sus));
        this.infection_p_sym_by_age.add(Double.valueOf(p_sym));
        this.infection_p_sev_by_age.add(Double.valueOf(p_sev));
        this.infection_p_cri_by_age.add(Double.valueOf(p_cri));
        this.infection_p_dea_by_age.add(Double.valueOf(p_dea));
      } 
      assert this.infection_r_sus_by_age.size() > 0 : "infection_r_sus_by_age not loaded";
      assert this.infection_p_sym_by_age.size() > 0 : "infection_p_sym_by_age not loaded";
      assert this.infection_p_sev_by_age.size() > 0 : "infection_p_sev_by_age not loaded";
      assert this.infection_p_cri_by_age.size() > 0 : "infection_p_cri_by_age not loaded";
      assert this.infection_p_dea_by_age.size() > 0 : "infection_p_dea_by_age not loaded";
    } catch (Exception e) {
      System.err.println("File input error: " + filename);
    } 
  }
  
  public void load_econStatus_distrib(String filename) {
    this.economicInteractionDistrib = new HashMap<>();
    this.economicInteractionCumulativeDistrib = new HashMap<>();
    this.econBubbleSize = new HashMap<>();
    this.orderedEconStatuses = new ArrayList<>();
    try {
      System.out.println("Reading in econ interaction data from " + filename);
      FileInputStream fstream = new FileInputStream(filename);
      BufferedReader econDistribData = new BufferedReader(new InputStreamReader(fstream));
      String s = econDistribData.readLine();
      String[] header = splitRawCSVString(s);
      HashMap<String, Integer> rawColumnNames = new HashMap<>();
      for (int i = 0; i < header.length; i++)
        rawColumnNames.put(header[i], new Integer(i)); 
      while ((s = econDistribData.readLine()) != null) {
        String[] bits = splitRawCSVString(s);
        String myTitle = bits[0].toLowerCase();
        System.out.println(bits);
        HashMap<String, Double> interacts = new HashMap<>();
        ArrayList<Double> interactsCum = new ArrayList<>();
        double cumTotal = 0.0D;
        for (int j = 1; 
          j < bits.length; j++) {
          Double val = Double.valueOf(Double.parseDouble(bits[j]));
          interacts.put(header[j], val);
          cumTotal += val.doubleValue();
          interactsCum.add(Double.valueOf(cumTotal));
        } 
        this.economicInteractionDistrib.put(myTitle, interacts);
        this.economicInteractionCumulativeDistrib.put(myTitle, interactsCum);
        this.orderedEconStatuses.add(bits[0].toLowerCase());
      } 
      assert this.economicInteractionDistrib.size() > 0 : "economicInteractionDistrib not loaded";
      assert this.economicInteractionCumulativeDistrib.size() > 0 : "economicInteractionCumulativeDistrib not loaded";
      assert this.orderedEconStatuses.size() > 0 : "orderedEconStatuses not loaded";
      econDistribData.close();
    } catch (Exception e) {
      System.err.println("File input error: " + this.econ_interaction_distrib_filename);
    } 
  }
  
  public ArrayList<Map<String, List<Double>>> load_district_data(String districtFilename) {
    ArrayList<Map<String, List<Double>>> probHolder = new ArrayList<>();
    for (int i = 0; i < 7; i++)
      probHolder.add(new HashMap<>()); 
    this.districtNames = new ArrayList<>();
    this.districts = new HashMap<>();
    this.districtNames = new ArrayList<>();
    try {
      System.out.println("Reading in district transfer information from " + districtFilename);
      FileInputStream fstream = new FileInputStream(districtFilename);
      BufferedReader districtData = new BufferedReader(new InputStreamReader(fstream));
      String s = districtData.readLine();
      String[] header = splitRawCSVString(s);
      HashMap<String, Integer> rawColumnNames = new HashMap<>();
      for (int j = 0; j < header.length; j++)
        rawColumnNames.put(header[j], new Integer(j)); 
      int weekdayIndex = ((Integer)rawColumnNames.get("weekday")).intValue();
      int homeregionIndex = ((Integer)rawColumnNames.get("home_region")).intValue();
      for (int k = homeregionIndex + 1; k < header.length; k++)
        this.districtNames.add(header[k]); 
      System.out.println("BEGIN READING IN DISTRICTS");
      while ((s = districtData.readLine()) != null) {
        String[] bits = splitRawCSVString(s);
        int dayOfWeek = Integer.parseInt(bits[weekdayIndex]);
        String districtName = bits[homeregionIndex];
        HashMap<String, Double> transferFromDistrict = new HashMap<>();
        ArrayList<Double> cumulativeProbTransfer = new ArrayList<>();
        for (int m = homeregionIndex + 1; m < bits.length; m++) {
          transferFromDistrict.put(header[m], Double.valueOf(Double.parseDouble(bits[m])));
          cumulativeProbTransfer.add(Double.valueOf(Double.parseDouble(bits[m]) / 100.0D));
        } 
       probHolder.get(dayOfWeek).put(districtName, cumulativeProbTransfer);
      } 
      for (String d : this.districtNames) {
        Location l = new Location(d);
        this.districts.put(d, l);
      } 
      districtData.close();
      assert this.districts.size() > 0 : "Districts not loaded";
      assert probHolder.size() > 0 : "Probability of transition between districts not loaded";
      return probHolder;
    } catch (Exception e) {
      System.err.println("File input error: " + districtFilename);
      return null;
    } 
  }
  
  public HashMap<String, Double> readInEconomicData(String econFilename, String statusColName, String probColName) {
    try {
      HashMap<String, Double> econData = new HashMap<>();
      System.out.println("Reading in data from " + econFilename);
      FileInputStream fstream = new FileInputStream(econFilename);
      BufferedReader econDataFile = new BufferedReader(new InputStreamReader(fstream));
      String s = econDataFile.readLine();
      String[] header = splitRawCSVString(s);
      HashMap<String, Integer> columnNames = parseHeader(header);
      int statusIndex = ((Integer)columnNames.get(statusColName)).intValue();
      int probIndex = ((Integer)columnNames.get(probColName)).intValue();
      while ((s = econDataFile.readLine()) != null) {
        String[] bits = splitRawCSVString(s);
        econData.put(bits[statusIndex].toLowerCase(), Double.valueOf(Double.parseDouble(bits[probIndex])));
      } 
      econDataFile.close();
      System.out.println("...Finished reading in from " + econFilename);
      return econData;
    } catch (Exception e) {
      System.err.println("File input error: " + econFilename);
      return null;
    } 
  }
  
  public void load_district_leaving_data(String districtFilename) {
    this.districtLeavingProb = new HashMap<>();
    try {
      System.out.println("Reading in district transfer information from " + districtFilename);
      FileInputStream fstream = new FileInputStream(districtFilename);
      BufferedReader districtData = new BufferedReader(new InputStreamReader(fstream));
      String s = districtData.readLine();
      String[] header = splitRawCSVString(s);
      HashMap<String, Integer> rawColumnNames = new HashMap<>();
      for (int i = 0; i < header.length; i++)
        rawColumnNames.put(header[i], new Integer(i)); 
      int locationIndex = ((Integer)rawColumnNames.get("district_id")).intValue();
      int probIndex = ((Integer)rawColumnNames.get("pctdif_distance")).intValue();
      System.out.println("BEGIN READING IN LEAVING PROBABILITIES");
      while ((s = districtData.readLine()) != null) {
        String[] bits = splitRawCSVString(s);
        String dId = bits[locationIndex];
        Double prob = Double.valueOf(Double.parseDouble(bits[probIndex]));
        Location myLocation = this.districts.get(dId);
        if (myLocation == null) {
          System.out.println("WARNING: no districted named " + dId + " as requested in district leaving file. Skipping!");
          continue;
        } 
        this.districtLeavingProb.put(myLocation, prob);
      } 
      assert this.districtLeavingProb.size() > 0 : "District leaving probability not loaded";
      districtData.close();
    } catch (Exception e) {
      System.err.println("File input error: " + districtFilename);
    } 
  }
  
  public static String[] splitRawCSVString(String s) {
    String[] myString = s.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    for (int i = 0; i < myString.length; i++) {
      String transfer = myString[i];
      myString[i] = transfer.replaceAll("\"", "").trim();
    } 
    return myString;
  }
  
  public static HashMap<String, Integer> parseHeader(String[] rawHeader) {
    HashMap<String, Integer> rawColumnNames = new HashMap<>();
    for (int i = 0; i < rawHeader.length; i++) {
      String colName = rawHeader[i];
      rawColumnNames.put(colName, new Integer(i));
    } 
    return rawColumnNames;
  }
  
  public double getSuspectabilityByAge(int age) {
    return this.infection_beta * getLikelihoodByAge(this.infection_r_sus_by_age, age);
  }
  
  public double getEconProbByDay(int day, String econ_status) {
    if (day < 5) {
      if (!this.economic_status_weekday_movement_prob.containsKey(econ_status))
        return -1.0D; 
      return ((Double)this.economic_status_weekday_movement_prob.get(econ_status)).doubleValue();
    } 
    if (!this.economic_status_otherday_movement_prob.containsKey(econ_status))
      return -1.0D; 
    return ((Double)this.economic_status_otherday_movement_prob.get(econ_status)).doubleValue();
  }
  
  public Location getTargetMoveDistrict(Person p, int day, double rand, boolean lockedDown) {
    ArrayList<Double> myTransitionProbs;
    Location l = p.getLocation();
    Location dummy = l;
    while (this.districtLeavingProb.get(dummy) == null && dummy.getSuper() != null)
      dummy = dummy.getSuper(); 
    if (lockedDown) {
      myTransitionProbs = (ArrayList<Double>)((Map)this.dailyTransitionLockdownProbs.get(day)).get(dummy.getId());
    } else {
      myTransitionProbs = (ArrayList<Double>)((Map)this.dailyTransitionPrelockdownProbs.get(day)).get(dummy.getId());
    } 
    for (int i = 0; i < myTransitionProbs.size(); i++) {
      if (rand <= ((Double)myTransitionProbs.get(i)).doubleValue()) {
        Location resultLoc = this.districts.get(this.districtNames.get(i));
        return resultLoc;
      } 
    } 
    return null;
  }
  
  public static boolean isWeekday(int day) {
    if (day < 5)
      return true; 
    return false;
  }
  
  public double getLikelihoodByAge(ArrayList<Double> distrib, int age) {
    for (int i = 0; i < this.infection_age_params.size(); i++) {
      if (age < ((Integer)this.infection_age_params.get(i)).intValue())
        return ((Double)distrib.get(i)).doubleValue(); 
    } 
    return -1.0D;
  }
}