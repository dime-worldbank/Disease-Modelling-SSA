package uk.ac.ucl.protecs.sim.loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.sim.ImportExport;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class LoggingHelperFunctions{
	// set up commonly used variables to avoid repetition
	// age boundaries to format log files
//	public final static List <Integer> upper_age_range = Arrays.asList(1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 120);
//	public final static List <Integer> lower_age_range = Arrays.asList(0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95);
//
//	public final static String age_categories = "<1" + "\t" + "1_4" + "\t" + "5_9" + "\t" + "10_14" + "\t" + "15_19" + "\t" + "20_24" + "\t" + "25_29" + 
//			"\t" + "30_34" + "\t" + "35_39" + "\t" + "40_44" + "\t" + "45_49" + "\t" + "50_54" + "\t" + "55_59" + "\t" + "60_64" + "\t" + "65_69" + "\t" + 
//			"70_74" + "\t" + "75_79" + "\t" + "80_84" + "\t" + "85_89" + "\t" + "90_94" + "\t" + "95<";
	// tab shortcut
	public final static String tab = "\t";	
	
	public static void setUpAgeBoundaries(WorldBankCovid19Sim world) {
		 world.params.age_sex_categories += tab + "sex" + tab + world.params.age_categories + "\n";
	}
	// get those alive at location
	public static Map<Boolean, Map<String, List<Person>>> get_alive_at_location(WorldBankCovid19Sim world) {
		Map<Boolean, Map<String, List<Person>>> result = new HashMap<>();
		
		for (Person p: world.agents) {
			result
				.computeIfAbsent(p.isAlive(), k -> new HashMap<>())
				.computeIfAbsent(p.getCurrentAdminZone(),  k -> new ArrayList<>())
				.add(p);
		}
		
		return result;
	}
	public static Map<Boolean, Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, List<Disease>>>>>> get_disease_at_location(WorldBankCovid19Sim world) {

	    Map<Boolean, Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, List<Disease>>>>>> result =
	        new HashMap<>();

	    for (Disease d : world.human_infections) {
	        result
	            .computeIfAbsent(d.isHostAlive(), k -> new HashMap<>())
	            .computeIfAbsent(d.getCurrentAdminZone(), k -> new EnumMap<>(DISEASE.class))
	            .computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
	            .computeIfAbsent(d.isInfectionActive(), k-> new HashMap<>())
	            .computeIfAbsent(d.hasRecovered(), k -> new ArrayList<>())
	            .add(d);
	    }

	    return result;
	}

	
	// get those who died from a disease at location
	public static Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, Map<Boolean, List<Disease>>>>>> get_dead_from_disease_at_location(WorldBankCovid19Sim world) {
		
		Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, Map<Boolean, List<Disease>>>>>> result = new HashMap<>();
		
		for (Disease d: world.human_infections) {
			result
				.computeIfAbsent(d.getCurrentAdminZone(), k -> new EnumMap<>(DISEASE.class))
				.computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
				.computeIfAbsent(d.isCauseOfDeath(), k -> new HashMap<>())
	            .computeIfAbsent(d.isInfectionActive(), k-> new HashMap<>())
				.computeIfAbsent(d.getDeathLogged(), k -> new ArrayList<>())
				.add(d);
			
		}
		return result;
	}
	
	
	// get those who died of the disease of age
	public static ArrayList<Integer> get_disease_death_counts_by_age(
	        WorldBankCovid19Sim world, SEX sex, DISEASE disease) {

	    ArrayList<Integer> result = new ArrayList<>();
	    int idx = 0;

		Map<Integer, Map<SEX, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> newdeathCountsByAge = new HashMap<>();
		
		for (Disease d : world.human_infections) {
			newdeathCountsByAge
			.computeIfAbsent(d.getHostAge(), k -> new HashMap<>())
			.computeIfAbsent(d.getHostSex(), k -> new HashMap<>())
			.computeIfAbsent(d.getDiseaseType(),  k -> new HashMap<>())
			.computeIfAbsent(d.isCauseOfDeath(), k -> new HashMap<>())
			.merge(d.getDeathLogged(), 1l, Long::sum);
		}

		 // Now sum by requested age ranges
	    for (int upper : world.params.upper_age_range) {
	        int lower = world.params.lower_age_range.get(idx);

	        int total = 0;
	        for (int age = lower; age < upper; age++) {
	        	try {
	            total += newdeathCountsByAge.get(age).get(sex).get(disease).get(true).get(false);
	            }
	        	catch (Exception e) {
	        		// no corresponding categories, do nothing
	        	}
	        }

	        result.add(total);
	        idx++;
	    }
	   

	    return result;
	}

	
	// get those who alive with a disease of given age and sex
	public static Map<SEX, Map<Integer, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> age_sex_has_disease_map(WorldBankCovid19Sim world) {
		
	    Map<SEX, Map<Integer, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> result =
	            new EnumMap<>(SEX.class);

	    for (Disease d : world.human_infections) {

	        result
	            .computeIfAbsent(d.getHostSex(), k -> new HashMap<>())
	            .computeIfAbsent(d.getHostAge(), k -> new EnumMap<>(DISEASE.class))
	            .computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
	            .computeIfAbsent(d.hasRecovered(), k -> new HashMap<>())
	            .merge(d.getLogged(), 1L, Long::sum);
	    	}

	    return result;
	}
	// get those alive with the disease of a given age of age
	public static ArrayList <Integer> get_disease_counts_by_age(WorldBankCovid19Sim world, SEX sex, DISEASE disease) {
		Integer idx = 0;
		ArrayList <Integer> disease_by_ages = new ArrayList<Integer>();

		// create a function to group the population by sex, age and whether they have the disease
		Map<SEX, Map<Integer, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> age_sex_map_has_disease = age_sex_has_disease_map(world);
				
		//	We now iterate over the age ranges, create a variable to keep track of the iterations
		for (Integer val: world.params.upper_age_range) {
			// for each age group we begin to count the number of people who fall into each category, create variables
			// to store this information in
			Integer disease_count = 0;
			// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
			for (int age = world.params.lower_age_range.get(idx); age < val; age++) {					
				try {
					// try function necessary as some ages won't be present in the population
					disease_count += age_sex_map_has_disease.get(sex).get(age).get(disease).get(false).get(false).intValue();
				}
					catch (Exception e) {
					// age wasn't present in the population, skip
					}
			}
			disease_by_ages.add(disease_count);
			// update the idx variable for the next iteration
			idx++;
			
		}
		return disease_by_ages;
	}
	
	public static Steppable ReportPercentInAdminZoneWithDisease(WorldBankCovid19Sim world, DISEASE disease, String outputFileName) {
		return new Steppable() {
				
			@Override
			public void step(SimState arg0) {

				Map<Boolean, Map<String, List<Person>>> aliveAtLocation = get_alive_at_location(world);

				// create a function to group the population by who is alive in each admin zone and has the disease
				Map<Boolean, Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, List<Disease>>>>>> diseaseAtLocation = get_disease_at_location(world);
				// get a list of admin zone to iterate over
				List <String> adminZones = ((WorldBankCovid19Sim)arg0).params.adminZoneNames;
		
				// create a list to store the number of people and who has covid in each admin zone
				ArrayList <Integer> adminZonePopCounts = new ArrayList<Integer>();
				ArrayList <Integer> adminZoneDiseaseCounts = new ArrayList<Integer>();
				// iterate over each admin zone
				for (String place: adminZones) {
					// get population counts in each admin zone
					try {
						adminZonePopCounts.add(aliveAtLocation.get(true).get(place).size());
						}
					catch (Exception e) {
						// age wasn't present in the population, skip
						adminZonePopCounts.add(0);
						}
					// get disease counts in each admin zone
					try {
						adminZoneDiseaseCounts.add(diseaseAtLocation.get(true).get(place).get(disease).get(true).get(false).size());
						}
					catch (Exception e) {
						// age wasn't present in the population, skip
						adminZoneDiseaseCounts.add(0);
						}
						
				}
				// format the output file for population counts
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

				// format the output for the percent of the admin zone with the disease
				String percent_with_disease_str = "";
				if (time == 0) {
					percent_with_disease_str += "day";
					for (String place: adminZones) {
						percent_with_disease_str += tab + place;
						}
					percent_with_disease_str += "\n" + String.valueOf(time);
				}
				else {
					percent_with_disease_str += "\n" + String.valueOf(time);
				}
				int idx = 0;
				// calculate the percentage in the admin zone with covid
				for (float count: adminZoneDiseaseCounts) {
					float perc_with_disease = count / adminZonePopCounts.get(idx);
					percent_with_disease_str += tab + perc_with_disease;
					idx++;
				}
				// export the file
				ImportExport.exportMe(outputFileName, percent_with_disease_str, world.timer);
				}
			};
		}
	
	public static Steppable ReportPercentOfDiseaseCasesThatAreFatalPerAdminZone(WorldBankCovid19Sim world, DISEASE disease, String outputFileName) {
		return new Steppable() {
				
			@Override
			public void step(SimState arg0) {
				// create a function to group the population by who is alive in each admin zone and has the disease
				Map<Boolean, Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, List<Disease>>>>>> diseaseAtLocation = get_disease_at_location(world);
				Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, Map<Boolean, List<Disease>>>>>> diseaseDeathsAtLocation = get_dead_from_disease_at_location(world);
				// get a list of admin zone to iterate over
				List <String> adminZones = ((WorldBankCovid19Sim)arg0).params.adminZoneNames;
				// format the output file for population counts
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				// create a list to store the number of people and who has covid in each admin zone
				ArrayList <Float> adminZonePercentDiseaseCasesFatal = new ArrayList<Float>();
				// iterate over each admin zone
				for (String place: adminZones) {
					// get population counts in each admin zone
					try {
						// numerator = number of people at location who have died from the disease, but have not had their deaths recorded
						int numerator = diseaseDeathsAtLocation.get(place).get(disease).get(true).get(true).get(false).size();
						// denominator = number of people at location who currently are alive with the disease plus those at location who have died from the disease but not had their deaths recorded
						int denominator = diseaseAtLocation.get(true).get(place).get(disease).get(true).get(false).size() + numerator;
						adminZonePercentDiseaseCasesFatal.add((float) numerator / denominator);
					}
					catch (Exception e) {
						// age wasn't present in the population, skip
						adminZonePercentDiseaseCasesFatal.add(0f);
					}
				}
				// format log file
				String percent_disease_death_per_admin = "";
				if (time == 0) {
					percent_disease_death_per_admin += "day";
					for (String place: adminZones) {
						percent_disease_death_per_admin += tab + place;
					}
					percent_disease_death_per_admin += "\n" + String.valueOf(time);
				}
				else {
					percent_disease_death_per_admin += "\n" + String.valueOf(time);
				}
				// calculate the percentage in the admin zone with covid
				for (float percent: adminZonePercentDiseaseCasesFatal) {
					percent_disease_death_per_admin += tab + percent;
					}
				// export the file
				ImportExport.exportMe(outputFileName, percent_disease_death_per_admin, world.timer);
				}
			};
		}
	public static Steppable adminZonePercentDiedFromDiseaseOutputFilename(WorldBankCovid19Sim world, DISEASE disease, String outputFileName) {
		return new Steppable() {
				
			@Override
			public void step(SimState arg0) {
				// create a function to group the population by who is alive in each admin zone and has the disease
				Map<Boolean, Map<String, List<Person>>> aliveAtLocation = get_alive_at_location(world);
				// create a function to group the population by who died from the disease at each admin zone
				Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, Map<Boolean, List<Disease>>>>>> diseaseDeathsAtLocation = get_dead_from_disease_at_location(world);
				// get a list of admin zone to iterate over
				List <String> adminZones = ((WorldBankCovid19Sim)arg0).params.adminZoneNames;
				// format the output file for population counts
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				// create a list to store the number of people and who has covid in each admin zone
				ArrayList <Float> adminZonePercentDiseaseFatal = new ArrayList<Float>();
				// iterate over each admin zone
				for (String place: adminZones) {
					// get population counts in each admin zone
					try {
						// numerator = number of people at location who have died from the disease, but have not had their deaths recorded
						int numerator = diseaseDeathsAtLocation.get(place).get(disease).get(true).get(true).get(false).size();
						// denominator = number of people at location who currently are alive with the disease plus those at location who have died from the disease but not had their deaths recorded
						int denominator = aliveAtLocation.get(true).get(place).size() + numerator;
						adminZonePercentDiseaseFatal.add((float) numerator / denominator);
					}
					catch (Exception e) {
						// age wasn't present in the population, skip
						adminZonePercentDiseaseFatal.add(0f);
						}
				}
				// format log file
				String percent_disease_death_per_admin = "";

				if (time == 0) {
					percent_disease_death_per_admin += "day";
					for (String place: adminZones) {
						percent_disease_death_per_admin += tab + place;
					}
					percent_disease_death_per_admin += "\n" + String.valueOf(time);
				}
				else {
					percent_disease_death_per_admin += "\n" + String.valueOf(time);
				}
				// calculate the percentage in the admin zone with covid
				for (float percent: adminZonePercentDiseaseFatal) {
					percent_disease_death_per_admin += tab + percent;
				}
				// export the file
				ImportExport.exportMe(outputFileName, percent_disease_death_per_admin, world.timer);
				}
			};
		}
	
	public static Steppable ReportDiseaseIncidenceOfDeath(WorldBankCovid19Sim world, DISEASE disease, String outputFileName) {

		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

				// calculate incidence of death in each age group by cause
				//	covid deaths, incidence in age groups 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges

				// create list to store the counts of the number of males and females alive in each age range, 
				// the number of disease deaths in each age range and the number of 'other' cause deaths in each age range
				ArrayList <Integer> male_disease_deaths_by_ages = get_disease_death_counts_by_age(world, SEX.MALE, disease);
				ArrayList <Integer> female_disease_deaths_by_ages = get_disease_death_counts_by_age(world, SEX.FEMALE, disease);
				ArrayList <Integer> male_alive_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();

				for (String group: world.params.age_category_list) {
					male_alive_ages.add(WorldBankCovid19Sim.malePopulationSizes.get(group));
					female_alive_ages.add(WorldBankCovid19Sim.femalePopulationSizes.get(group));

				}
				// format log file
				String disease_inc_death = "";

				if (time == 0) {
					disease_inc_death += "day" +  world.params.age_sex_categories + String.valueOf(time);
				}
				else {
					disease_inc_death += String.valueOf(time);
				}
				// calculate incidence of disease death in males this day
				disease_inc_death += tab + "m";
				for (int x = 0; x < male_disease_deaths_by_ages.size(); x++){
					double male_disease_deaths_in_age = male_disease_deaths_by_ages.get(x);
					double male_alive_in_age = male_alive_ages.get(x);
					double result =  male_disease_deaths_in_age / male_alive_in_age;
	                result *= 100000;
	                disease_inc_death += tab + String.valueOf(result);
				}
				disease_inc_death += "\n";
				// calculate incidence of disease death in females this day
				disease_inc_death += String.valueOf(time) + tab + "f";
				for (int x =0; x < female_disease_deaths_by_ages.size(); x++){
					double female_disease_deaths_in_age = female_disease_deaths_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = female_disease_deaths_in_age / female_alive_in_age;	                
					result *= 100000;
					disease_inc_death += tab + String.valueOf(result);
				}
				disease_inc_death += "\n";
				

				// export the output files
				ImportExport.exportMe(outputFileName, disease_inc_death, world.timer);
				
				}
			};
		}
	public static Steppable ReportIncidenceOfDisease(WorldBankCovid19Sim world, DISEASE disease, String outputFileName) {

		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				// create list to store the counts of the number of males and females alive in each age range, 
				// the number of disease cases in each age range 
				ArrayList <Integer> male_alive_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();

				for (String group: world.params.age_category_list) {
					male_alive_ages.add(WorldBankCovid19Sim.malePopulationSizes.get(group));
					female_alive_ages.add(WorldBankCovid19Sim.femalePopulationSizes.get(group));

				}
				// calculate incidence of disease in each age group
				//	disease incidence in age groups 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges
				// create list to store the counts of the number of males and females alive in each age range and
				// the number of covid cases in each age range 
				ArrayList <Integer> male_disease_by_ages = get_disease_counts_by_age(world, SEX.MALE, disease);
				ArrayList <Integer> female_disease_by_ages =  get_disease_counts_by_age(world, SEX.FEMALE, disease);
				// create a function to group the population by sex, age and whether they are alive
				
				// format the output file
				String disease_inc = "";
				if (time == 0) {
					disease_inc += "day" +  world.params.age_sex_categories + String.valueOf(time);
				}
				else {
					disease_inc += String.valueOf(time);
				}
				// calculate the incidence of disease in males this day
				disease_inc += tab + "m";
				for (int x = 0; x < male_disease_by_ages.size(); x++){
					double male_disease_cases_in_age = male_disease_by_ages.get(x);
					double male_alive_in_age = male_alive_ages.get(x);
					double result =  male_disease_cases_in_age / male_alive_in_age;
	                result *= 100000;
	                disease_inc += tab + String.valueOf(result);
				}
				disease_inc += "\n";
				// calculate the incidence of disease in females this day
				disease_inc += String.valueOf(time) + tab + "f";

				for (int x =0; x < female_disease_by_ages.size(); x++){
					double female_disease_cases_in_age = female_disease_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = female_disease_cases_in_age / female_alive_in_age;	                
					result *= 100000;
					disease_inc += tab + String.valueOf(result);
				}
				disease_inc += "\n";
				
				// export the output file
				ImportExport.exportMe(outputFileName, disease_inc, world.timer);
				}
			};
		}
	public static Steppable ReportDiseaseCounts(WorldBankCovid19Sim world, DISEASE disease, String outputFileName) {

		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

				ArrayList <Integer> male_cases_by_ages = get_disease_counts_by_age(world, SEX.MALE, disease);
				ArrayList <Integer> male_deaths_by_ages = get_disease_death_counts_by_age(world, SEX.MALE, disease);
				ArrayList <Integer> female_cases_by_ages =  get_disease_counts_by_age(world, SEX.FEMALE, disease);
				ArrayList <Integer> female_deaths_by_ages = get_disease_death_counts_by_age(world, SEX.FEMALE, disease);

				//	calculate the number of counts in each age group	
				String disease_number_and_deaths = "";
				if (time == 0) {
					disease_number_and_deaths += "day" + tab + "metric" +  world.params.age_sex_categories + String.valueOf(time);
				}
				else {
					disease_number_and_deaths += String.valueOf(time);
				}
				disease_number_and_deaths += tab + "cases" + tab + "m";
				for (Integer count: male_cases_by_ages){
					disease_number_and_deaths += tab + count;
				}
				disease_number_and_deaths += "\n";

				disease_number_and_deaths += String.valueOf(time) + tab + "deaths" + tab + "m";
				for (Integer count: male_deaths_by_ages){
					disease_number_and_deaths += tab + count;
				}
				disease_number_and_deaths += "\n";
				disease_number_and_deaths += String.valueOf(time) + tab + "cases" + tab + "f";
				for (Integer count: female_cases_by_ages){
					disease_number_and_deaths += tab + count;
				}
				disease_number_and_deaths += "\n";
				// calculate the incidence of covid in females this day
				disease_number_and_deaths += String.valueOf(time) + tab + "deaths" + tab + "f";
				for (Integer count: female_deaths_by_ages){
					disease_number_and_deaths += tab + count;
				}
				disease_number_and_deaths += "\n";
				ImportExport.exportMe(outputFileName, disease_number_and_deaths, world.timer);


			}
		};
	}
	
	
	public static Steppable ReportDiseaseCountsByOccupation(WorldBankCovid19Sim world, DISEASE disease, String outputFileName) {

		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				ArrayList <Integer> status_counts = new ArrayList<Integer>();
				ArrayList <Integer> status_disease_counts = new ArrayList<Integer>();
				ArrayList <Integer> status_disease_death_counts = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				
				// create a enum maps to group the population by occupation, age and whether they have the disease and also whether they died from the disease
				Map<OCCUPATION, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> economic_alive_has_disease = new EnumMap<>(OCCUPATION.class);
				Map<OCCUPATION, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>> econ_died_from_disease = new EnumMap<>(OCCUPATION.class);
	   
				
				for (Disease d: world.human_infections) {
					economic_alive_has_disease
					.computeIfAbsent(d.getHostEconStatus(), k -> new HashMap<>())
					.computeIfAbsent(d.isHostAlive(), k -> new EnumMap<>(DISEASE.class))
					.computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
					.computeIfAbsent(d.hasRecovered(), k -> new HashMap<>())
					.merge(d.getLogged(), 1l, Long::sum);
					
					econ_died_from_disease
					.computeIfAbsent(d.getHostEconStatus(), k -> new EnumMap<>(DISEASE.class))
					.computeIfAbsent(d.getDiseaseType(), k -> new HashMap<>())
					.computeIfAbsent(d.isCauseOfDeath(), k -> new HashMap<>())
					.merge(d.getDeathLogged(), 1l, Long::sum);
				}
				
				
				// create an enum map to group the population by occupation and whether they are alive
				Map<OCCUPATION, Map<Boolean, Long>> economic_alive = new EnumMap<>(OCCUPATION.class);
				
				for (Person p : world.agents) {
				    economic_alive
				        .computeIfAbsent(p.getEconStatus(), k -> new HashMap<>())
				        .merge(p.isAlive(), 1L, Long::sum);
				}
				

				for (OCCUPATION status: world.occupationsInSim) {
					try {
					status_disease_counts.add(economic_alive_has_disease.get(status).get(true).get(disease).get(false).get(false).intValue());
					}
					catch (Exception e) {
						// no one in population met criteria, skip
						status_disease_counts.add(0);
					}
					try {
						status_disease_death_counts.add(econ_died_from_disease.get(status).get(disease).get(true).get(false).intValue());
						}
					catch (Exception e) {
						// no one in population met criteria, skip
						status_disease_death_counts.add(0);
					}
					try {
					status_counts.add(economic_alive.get(status).get(true).intValue());
					}
					catch (Exception e) {
						// no one in population met criteria, skip
						status_counts.add(0);
					}
				}
				
				String econ_status_categories = "";
				for (OCCUPATION job: world.occupationsInSim) {
					econ_status_categories += tab + job.name();
				}
				econ_status_categories += "\n";
				String econ_status_output = "";
				if (time == 0) {
					econ_status_output += "day" + tab + "metric" + econ_status_categories + String.valueOf(time);
				}
				else {
					econ_status_output += String.valueOf(time);
				}
				econ_status_output += tab + "number_in_occ";
				for (Integer count: status_counts){
					econ_status_output += tab + count;
				}
				econ_status_output += "\n";
				// calculate the number of people in this occumation with the disease
				econ_status_output += String.valueOf(time) + tab + "number_with_" + disease.key;
				for (Integer count: status_disease_counts){
					econ_status_output += tab + count;
				}
				econ_status_output += "\n";
				econ_status_output += String.valueOf(time) + tab + "number_died_from_" + disease.key;
				for (Integer count: status_disease_death_counts){
					econ_status_output += tab + count;
				}
				econ_status_output += "\n";
				
				ImportExport.exportMe(outputFileName, econ_status_output, world.timer);
			}
		};
	}
}

