package main.java.sim;

import main.java.behaviours.*;
import ec.util.MersenneTwisterFast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
//import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import main.java.objects.*;

import sim.engine.SimState;
import sim.engine.Steppable;
import main.java.sim.ImportExport;


public class WorldBankCovid19Sim extends SimState {
  // the objects which make up the system
  public ArrayList<Person> agents;
  public ArrayList<Household> households;
  public ArrayList<Infection> infections;
  
  ArrayList<Location> districts;
  
  HashMap<Location, ArrayList<Person>> personsToDistrict;
  
  public MovementBehaviourFramework movementFramework;
  public InfectiousBehaviourFramework infectiousFramework;  
  public Params params;
  
  public boolean lockedDown = false;
  public String outputFilename;
  public String infections_export_filename;
  public String detectedCovidFilename;
  public String spatialdetectedCovidFilename;
  public String sim_info_filename;
  
  int targetDuration = 0;
  public static int param_schedule_lockdown = 0;
  public static int param_schedule_movement = 1;
  public static int param_schedule_updating_locations = 5;
  public static int param_schedule_infecting = 10;
  public static int param_schedule_reporting = 100;
  
  public ArrayList<Integer> testingAgeDist = new ArrayList<>();
  
  ArrayList<HashMap<String, Double>> dailyRecord = new ArrayList<>();
  
  public long timer = -1L;
  
  public WorldBankCovid19Sim(long seed, Params params, String outputFilename) {
    super(seed);
    this.params = params;
    this.outputFilename = String.valueOf(outputFilename) + ".txt";
    this.detectedCovidFilename = String.valueOf(outputFilename) + "_detected_covid_cases.txt";
    this.spatialdetectedCovidFilename = String.valueOf(outputFilename) + "_spatial_detected_covid_cases.txt";
  }
  
  public void start() {
    districts = new ArrayList<>(this.params.districts.values());
    movementFramework = new MovementBehaviourFramework(this);
    infectiousFramework = new InfectiousBehaviourFramework(this);
    load_population(String.valueOf(this.params.dataDir) + this.params.population_filename);
    if (agents.size() == 0) {
      System.out.println("ERROR *** NO AGENTS LOADED");
      System.exit(0);
    } 
    random = new MersenneTwisterFast(seed());
    infections = new ArrayList<>();
    ArrayList<Location> unactivatedDistricts = new ArrayList<>(this.districts);
    for (Location l : this.params.lineList.keySet()) {
      l.setActive(true);
      unactivatedDistricts.remove(l);
      int countInfections = ((Integer)this.params.lineList.get(l)).intValue() * this.params.lineListWeightingFactor;
      HashSet<Person> newlyInfected = new HashSet<>();
      ArrayList<Person> peopleHere = this.personsToDistrict.get(l);
      int numPeopleHere = peopleHere.size();
      assert numPeopleHere > 0 : "A location has no one in it, this can't happen";
      if (numPeopleHere == 0) {
        System.out.println("WARNING: attempting to initialise infection in Location " + l.getId() + " but there are no People present. Continuing without successful infection...");
        continue;
      } 
      int collisions = 100;
      while (newlyInfected.size() < countInfections && collisions > 0) {
        Person p = peopleHere.get(this.random.nextInt(numPeopleHere));
        if (newlyInfected.contains(p)) {
          collisions--;
          continue;
        } 
        newlyInfected.add(p);
        Infection inf = new Infection(p, null, this.infectiousFramework.getInfectedEntryPoint(l), this);
        inf.time_contagious = 0.0D;
        this.schedule.scheduleOnce(1.0D, param_schedule_infecting, (Steppable)inf);
      } 
    } 
    Steppable updateLocationLists = new Steppable() {
        public void step(SimState arg0) {
          for (Location l : WorldBankCovid19Sim.this.districts)
            l.updatePersonsHere(); 
        }
      };
    schedule.scheduleRepeating(0.0D, param_schedule_updating_locations, updateLocationLists);
    Steppable testing_for_covid = new Steppable() {

		@Override
		public void step(SimState arg0) {
			// get the simulation time
			int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
			// get the index for the test numbers
			int index_for_test_number = params.test_dates.indexOf(time);
			// find the number of tests per 1000 to test today
			int number_of_tests_today = 0;
			try {
				number_of_tests_today = params.number_of_tests_per_day.get(index_for_test_number);
				}
			catch (Exception e) {
			}
			// we only want to test people who are alive and administer the tests per 1000 based on this 
			Map<Boolean, Map<Boolean, List<Person>>> is_elligable_for_testing_map = agents.stream().collect(
									Collectors.groupingBy(
											Person::isDead,
											Collectors.groupingBy(
													Person::isElligableForTesting,
									Collectors.toList()
									)
								)
					);
			WorldBankCovid19Sim myWorld = (WorldBankCovid19Sim) arg0;								// create a random state (I need to link this to the existing random state but don't know how)
			Random testing_random = new Random(myWorld.seed());
			int number_of_positive_tests = 0;
			double percent_positive = 0;
			// generate a list of people to test today
			try {
				List<Person> people_tested = pickRandom(is_elligable_for_testing_map.get(false).get(true), number_of_tests_today, testing_random);
			// create a counter for the number of positive tests
			double test_accuracy = 0.97;
			// iterate over the list of people to test and perform the tests
			for (Person person:people_tested) {
				if(person.hasCovid()) {
					if (random.nextDouble() < test_accuracy) {
						number_of_positive_tests ++;
						person.setTestedPositive();
						// after they have tested positive, they no longer need to be tested again
						person.notElligableForTesting();
					}
				}
			}
			if (number_of_tests_today > 0) {
				percent_positive = (double) number_of_positive_tests / (double) number_of_tests_today; 
			}}
			catch (Exception e) {
			}
			String t = "\t";
			
			String detected_covid_output = "";
			if (time == 0) {
				detected_covid_output += "day" + t + "number_of_detected_cases" + t + "number_of_tests" + t + "fraction_positive" + "\n"+ String.valueOf(time) + t + number_of_positive_tests + t + number_of_tests_today + t + percent_positive+ "\n";
			}
			else {
				detected_covid_output += t + number_of_positive_tests + t + number_of_tests_today + t + percent_positive + "\n";
			}
			ImportExport.exportMe(detectedCovidFilename, detected_covid_output, time);
//			create a list of district names to iterate over for our logging
			List <String> districtList = myWorld.params.districtNames;

			// create list to store the number of cases per district
			ArrayList <Integer> covidTestedPositiveArray = new ArrayList<Integer>();

			// create a function to group the population by location, whether they are alive and if they have covid and if this is a new case
			Map<String, Map<Boolean, Map<Boolean, Long>>> location_alive_tested_pos_for_Covid_map = agents.stream().collect(
					Collectors.groupingBy(
							Person::getCurrentDistrict, 
								Collectors.groupingBy(
											Person::isDead,
											Collectors.groupingBy(
													Person::hasTestedPos,
									Collectors.counting()
									)
							)
					)
					);
//			We now iterate over the districts, to find the current state of the epidemic
			for (String district: districtList) {
				// get the current number of cases in each district
				try {
					covidTestedPositiveArray.add(location_alive_tested_pos_for_Covid_map.get(district).get(false).get(true).intValue());
				} catch (Exception e) {
					// No one in population met criteria
					covidTestedPositiveArray.add(0);
				}
			}
			
			String spatialOutput = "";
			// format the file
			String tabbedDistrictNames = "";
			for (String district: districtList) {tabbedDistrictNames += t + district;}
			if (time == 0) {
				spatialOutput += "day" + tabbedDistrictNames + "\n" + String.valueOf(time);
			}
			// store total number of positive tests in district
			for (int val: covidTestedPositiveArray){
				spatialOutput += t + String.valueOf(val);
			}
			spatialOutput += "\n";
			ImportExport.exportMe(spatialdetectedCovidFilename, spatialOutput, time);
			try {
				List<Person> people_tested = pickRandom(is_elligable_for_testing_map.get(false).get(true), number_of_tests_today, testing_random);
			for (Person person:people_tested) {
				if(person.hasTestedPos()) {
					person.removeTestedPositive();
					}
			}
		} catch (Exception e) {}
	}
		
	};
    schedule.scheduleRepeating(testing_for_covid, param_schedule_reporting, Params.ticks_per_day);
    Steppable lockdownTrigger = new Steppable() {
        public void step(SimState arg0) {
          double currentTime = arg0.schedule.getTime();
          if (params.lockdownChangeList.size() == 0)
            return; 
          double nextChange = params.lockdownChangeList.get(0);
          if (currentTime >= nextChange) {
            params.lockdownChangeList.remove(0);
            lockedDown = !lockedDown;
          } 
        }
      };
    schedule.scheduleRepeating(0.0D, param_schedule_lockdown, lockdownTrigger);
    Steppable spuriosSymptomTrigger = new Steppable() {
        public void step(SimState arg0) {
          int time = (int)(arg0.schedule.getTime() / Params.ticks_per_day);
          Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>> has_non_asymptomatic_covid = (Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>>)WorldBankCovid19Sim.this.agents.stream().collect(
              Collectors.groupingBy(
                Person::isDead, 
                Collectors.groupingBy(
                  Person::hasMild, 
                  Collectors.groupingBy(
                    Person::hasSevere, 
                    Collectors.groupingBy(
                      Person::hasCritical, 
                      Collectors.toList())))));
          double number_people_with_symptoms_as_double = WorldBankCovid19Sim.this.params.rate_of_spurious_symptoms * ((List)((Map)((Map)((Map)has_non_asymptomatic_covid.get(Boolean.valueOf(false))).get(Boolean.valueOf(false))).get(Boolean.valueOf(false))).get(Boolean.valueOf(false))).size();
          int number_people_with_symptoms = (int)number_people_with_symptoms_as_double;
          Random symptom_random = new Random(WorldBankCovid19Sim.this.seed());
          List<Person> people_developing_symptoms = (List)WorldBankCovid19Sim.pickRandom((List)((Map)((Map)((Map)has_non_asymptomatic_covid.get(Boolean.valueOf(false))).get(Boolean.valueOf(false))).get(Boolean.valueOf(false))).get(Boolean.valueOf(false)), number_people_with_symptoms, symptom_random);
          for (Person p : people_developing_symptoms) {
            p.elligableForTesting();
            p.setSymptomRemovalDate(time + 7);
            p.setSpuriousSymptoms();
          } 
          Map<Boolean, Map<Boolean, List<Person>>> has_spurios_symptoms = (Map<Boolean, Map<Boolean, List<Person>>>)WorldBankCovid19Sim.this.agents.stream().collect(
              Collectors.groupingBy(
                Person::isDead, 
                Collectors.groupingBy(
                  Person::hasSpuriousSymptoms, 
                  Collectors.toList())));
          List<Person> people_with_symptoms = (List<Person>)((Map)has_spurios_symptoms.get(Boolean.valueOf(false))).get(Boolean.valueOf(true));
          if (people_with_symptoms != null)
            for (Person p : people_with_symptoms) {
              if (p.timeToRemoveSymptoms < time) {
                p.notElligableForTesting();
                p.removeSpuriousSymptoms();
              } 
            }  
        }
      };
    this.schedule.scheduleRepeating(0.0D, param_schedule_lockdown, spuriosSymptomTrigger);
    String filenameSuffix = String.valueOf(Params.ticks_per_day * this.params.infection_beta) + "_" + 
      this.params.lineListWeightingFactor + "_" + 
      this.targetDuration + "_" + 
      seed() + ".txt";
    this.infections_export_filename = "infections_" + filenameSuffix;
    ImportExport.exportMe(outputFilename, Location.metricNamesToString(), timer);
    Steppable reporter = new Steppable() {
        public void step(SimState arg0) {
          String s = "";
          int time = (int)(arg0.schedule.getTime() / Params.ticks_per_day);
          for (Location l : WorldBankCovid19Sim.this.districts) {
            s = String.valueOf(s) + time + "\t" + l.metricsToString() + "\n";
            l.refreshMetrics();
          } 
          ImportExport.exportMe(outputFilename, s, timer);
          System.out.println("Day " + time + " finished");
        }
      };
    this.schedule.scheduleRepeating(reporter, param_schedule_reporting, Params.ticks_per_day);
    this.random = new MersenneTwisterFast(seed());
  }
  
  public void load_population(String agentsFilename) {
    try {
      this.agents = new ArrayList<>();
      this.households = new ArrayList<>();
      this.personsToDistrict = new HashMap<>();
      for (Location l : this.districts)
        this.personsToDistrict.put(l, new ArrayList<>()); 
      HashMap<String, Household> rawHouseholds = new HashMap<>();
      System.out.println("Reading in agents from " + agentsFilename);
      FileInputStream fstream = new FileInputStream(agentsFilename);
      BufferedReader agentData = new BufferedReader(new InputStreamReader(fstream));
      String s = agentData.readLine();
      System.out.print("BEGIN READING IN PEOPLE...");
      while ((s = agentData.readLine()) != null) {
        String[] bits = Params.splitRawCSVString(s);
        String hhName = bits[4];
        Household h = rawHouseholds.get(hhName);
        String myDistrictName = "d_" + bits[5];
        Location myDistrict = this.params.districts.get(myDistrictName);
        boolean schoolGoer = bits[8].equals("1");
        if (h == null) {
          h = new Household(hhName, myDistrict);
          rawHouseholds.put(hhName, h);
          this.households.add(h);
        } 
        Person p = new Person(Integer.parseInt(bits[1]), 
            Integer.parseInt(bits[2]), 
            bits[3], 
            bits[6].toLowerCase(), 
            schoolGoer, 
            h, 
            this);
        h.addPerson(p);
        p.setActivityNode(this.movementFramework.getHomeNode());
        this.agents.add(p);
        ((ArrayList<Person>)this.personsToDistrict.get(myDistrict)).add(p);
        this.schedule.scheduleOnce(0.0D, param_schedule_movement, (Steppable)p);
      } 
      agentData.close();
      System.out.println("FINISHED READING PEOPLE");
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("File input error: " + agentsFilename);
    } 
  }

  
  public double nextRandomLognormal(double mean, double std) {
    double m2 = mean * mean;
    double phi = Math.sqrt(m2 + std);
    double mu = Math.log(m2 / phi);
    double sigma = Math.sqrt(Math.log(phi * phi / m2));
    double x = this.random.nextDouble() * sigma + mu;
    return Math.exp(x);
  }
  
  private static <E> List<E> pickRandom(List<E> list, int n, Random rand) {
    return (List<E>)(new Random()).ints(n, 0, list.size()).mapToObj(list::get).collect(Collectors.toList());
  }
  public WorldBankCovid19Sim returnSim() {return this;}
  
  public static void main(String[] args) {
    int numDays = 7;
    double myBeta = 0.016D;
    double mySpurious = 0.016D;
    long seed = 12345L;
    String outputFilename = "dailyReport_" + myBeta + "_" + numDays + "_" + seed + ".txt";
    String infectionsOutputFilename = "";
    String paramsFilename = "data/configs/params.txt";
    if (args.length < 0) {
      System.out.println("usage error");
      System.exit(0);
    } else if (args.length > 0) {
      numDays = Integer.parseInt(args[0]);
      myBeta = Double.parseDouble(args[2]);
      mySpurious = Double.parseDouble(args[3]);
      if (args.length > 3) {
        seed = Long.parseLong(args[4]);
        outputFilename = "dailyReport_" + myBeta + "_" + numDays + "_" + seed + ".tsv";
      } 
      if (args.length > 4)
        outputFilename = args[5]; 
      if (args.length > 5)
        paramsFilename = args[6]; 
      if (args.length > 6)
        infectionsOutputFilename = args[7]; 
    } 
    long startTime = System.currentTimeMillis();
    WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(seed, new Params(paramsFilename), outputFilename);
    System.out.println("Loading...");
    mySim.params.infection_beta = myBeta / Params.ticks_per_day;
    mySim.params.rate_of_spurious_symptoms = mySpurious;
    mySim.targetDuration = numDays;
    mySim.start();
    mySim.infections_export_filename = infectionsOutputFilename;
    System.out.println("Running...");
    while (mySim.schedule.getTime() < (Params.ticks_per_day * numDays) && !mySim.schedule.scheduleComplete()) {
      mySim.schedule.step(mySim);
      double d = mySim.schedule.getTime();
    } 
	ImportExport.exportInfections(infectionsOutputFilename, mySim.infections);
    long endTime = System.currentTimeMillis();
    mySim.timer = endTime - startTime;
    System.out.println("...run finished after " + mySim.timer + " ms");
  }
  
  void exportSimInformation() {
    try {
      System.out.println("Printing out SIMULATION INFORMATION to " + this.sim_info_filename);
      BufferedWriter exportFile = new BufferedWriter(new FileWriter(this.sim_info_filename, true));
      exportFile.write("Seed\tNumberOfAgents\tSimuilationDuration\n");
      String rec = "";
      rec = String.valueOf(rec) + seed() + "\t";
      rec = String.valueOf(rec) + this.agents.size() + "\t";
      rec = String.valueOf(rec) + this.targetDuration + "\t";
      exportFile.write(rec);
      exportFile.close();
    } catch (Exception e) {
      System.err.println("File input error: " + this.sim_info_filename);
    } 
  }
}