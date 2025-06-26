package uk.ac.ucl.protecs.sim.loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class LoggingHelperFunctions{
	// set up commonly used variables to avoid repetition
	// age boundaries to format log files
	public final static List <Integer> upper_age_range = Arrays.asList(1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 120);
	public final static List <Integer> lower_age_range = Arrays.asList(0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95);

	public final static String age_categories = "<1" + "\t" + "1_4" + "\t" + "5_9" + "\t" + "10_14" + "\t" + "15_19" + "\t" + "20_24" + "\t" + "25_29" + 
			"\t" + "30_34" + "\t" + "35_39" + "\t" + "40_44" + "\t" + "45_49" + "\t" + "50_54" + "\t" + "55_59" + "\t" + "60_64" + "\t" + "65_69" + "\t" + 
			"70_74" + "\t" + "75_79" + "\t" + "80_84" + "\t" + "85_89" + "\t" + "90_94" + "\t" + "95<";
	// tab shortcut
	public final static String tab = "\t";
	
	public static Map<SEX, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map(WorldBankCovid19Sim world) {
		Map<SEX, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map = world.agents.stream().collect(
				Collectors.groupingBy(
						Person::getSex, 
						Collectors.groupingBy(
								Person::getAge, 
								Collectors.groupingBy(
										Person::isAlive,
								Collectors.counting()
								)
						)
				)
				);
		return age_sex_alive_map;
	}
	
	// get number of those alive of age and sex
	public static ArrayList <Integer> get_number_of_alive(WorldBankCovid19Sim world, SEX sex) {
		ArrayList <Integer> alive_ages = new ArrayList<Integer>();
		Map<SEX, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map_copy = age_sex_alive_map(world);
		// We now iterate over the age ranges, create a variable to keep track of the iterations
		Integer idx = 0;
		for (Integer val: upper_age_range) {
			// for each age group we begin to count the number of people who fall into each category, create variables
			// to store this information in
			Integer count = 0;
			// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
			for (int age = lower_age_range.get(idx); age < val; age++) {
				
				try {
					// try function necessary as some ages won't be present in the population
					// use the functions created earlier to calculate the number of people of each age group who fall
					// into the categories we are interested in (alive, died from covid, died from other)
					count += age_sex_alive_map_copy.get(sex).get(age).get(true).intValue();
				}
					catch (Exception e) {
						// age wasn't present in the population, skip
					}	
			}
			alive_ages.add(count);

		}
		return alive_ages;
	}
	
	
	
	// get those alive at location
	public static Map<Boolean, Map<String, List<Person>>> get_alive_at_location(WorldBankCovid19Sim world) {
		// create a function to group the population by who is alive at this admin zone
		Map<Boolean, Map<String, List<Person>>> aliveAtLocation = world.agents.stream().collect(
				Collectors.groupingBy(
						Person::isAlive,
						Collectors.groupingBy(
								Person::getCurrentAdminZone
								)
				)
			);
		return aliveAtLocation;
	}
	
}

