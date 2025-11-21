package uk.ac.ucl.protecs.sim.loggers;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class GeneratePopulationStats {
	WorldBankCovid19Sim myWorld;

	public static final String[] AGE_GROUPS = {
		    "<1", "1_4", "5-9", "10-14", "15-19", "20-24", "25-29",
		    "30-44", "35-49", "40-44", "45-49", "50-54", "55-59",
		    "60-64", "65-69", "70-74", "75-79", "80-84",
		    "85-89", "90-94", "95+"
		};
	public final static List <Integer> upper_age_range = Arrays.asList(1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 120);
	public final static List <Integer> lower_age_range = Arrays.asList(0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95);

	
	public static void setUpAgeHashMapKeys(WorldBankCovid19Sim world) {
	    initializeMap(world.malePopulationSizes);
	    initializeMap(world.femalePopulationSizes);
	    initializeMap(world.allPopulationSizes);
	}

	private static void initializeMap(Map<String, Integer> map) {
	    for (String age : AGE_GROUPS) {
	        map.put(age, 0);
	    }
	}
	
	
	public static Map<SEX, SharedLoggingBins> buildStats(WorldBankCovid19Sim world) {
	    Map<SEX, SharedLoggingBins> result = new EnumMap<>(SEX.class);
		int max_age = 120;

	    for (SEX sex : SEX.values()) {
	    	SharedLoggingBins s = new SharedLoggingBins();
	        s.alive = new long[max_age + 1];
	        s.dead = new long[max_age + 1];
	        result.put(sex, s);
	    }

	    for (Person p : world.agents) {
	    	SharedLoggingBins stat = result.get(p.getSex());
	        if (p.isAlive()) stat.alive[p.getAge()]++;
	        else stat.dead[p.getAge()]++;
	    }

	    // Build prefix sums
	    for (SharedLoggingBins s : result.values()) {
	        s.alivePrefix = new long[max_age + 1];
	        s.deadPrefix = new long[max_age + 1];
	        for (int i = 1; i <= max_age; i++) {
	            s.alivePrefix[i] = s.alivePrefix[i - 1] + s.alive[i];
	            s.deadPrefix[i] = s.deadPrefix[i - 1] + s.dead[i];
	        }
	    }

	    return result;
	}
	
	GeneratePopulationStats(WorldBankCovid19Sim world){
		myWorld = world;

	}
	
	public static Steppable updateAgeSexMaps (WorldBankCovid19Sim world) {
			return new Steppable() {
					
				@Override
				public void step(SimState arg0) {
					WorldBankCovid19Sim.popStatMap = GeneratePopulationStats.buildStats(world);

					int idx = 0;
					for (int age: upper_age_range) {
						// update female age bins
						long femaleAliveBetween = WorldBankCovid19Sim.popStatMap.get(SEX.FEMALE).alivePrefix[age] 
				                  - WorldBankCovid19Sim.popStatMap.get(SEX.FEMALE).alivePrefix[lower_age_range.get(idx)];
						WorldBankCovid19Sim.femalePopulationSizes.put(AGE_GROUPS[idx], (int) femaleAliveBetween);
						// update male age bins
						long maleAliveBetween = WorldBankCovid19Sim.popStatMap.get(SEX.MALE).alivePrefix[age] 
				                  - WorldBankCovid19Sim.popStatMap.get(SEX.MALE).alivePrefix[lower_age_range.get(idx)];
						WorldBankCovid19Sim.malePopulationSizes.put(AGE_GROUPS[idx], (int) maleAliveBetween);
						
						long totalAliveBetween = femaleAliveBetween + maleAliveBetween;
						WorldBankCovid19Sim.allPopulationSizes.put(AGE_GROUPS[idx], (int) totalAliveBetween);
						idx ++;
					}
				}
			};
	}
}
