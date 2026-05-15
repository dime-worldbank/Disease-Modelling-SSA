package uk.ac.ucl.protecs.sim.loggers;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class GeneratePopulationStats {
	WorldBankCovid19Sim myWorld;

	public static void setUpAgeHashMapKeys(WorldBankCovid19Sim world) {
	    initializeMap(world, world.malePopulationSizes);
	    initializeMap(world, world.femalePopulationSizes);
	    initializeMap(world, world.allPopulationSizes);
	}

	private static void initializeMap(WorldBankCovid19Sim world, Map<String, Integer> map) {
	    for (String age : world.params.age_category_list) {
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
	        s.aliveCumulative = new long[max_age + 1];
	        s.deadCumulative = new long[max_age + 1];
	        s.aliveCumulative[0] = s.alive[0];
	        s.deadCumulative[0] = s.dead[0];

	        for (int i = 1; i <= max_age; i++) {
	            s.aliveCumulative[i] = s.aliveCumulative[i - 1] + s.alive[i];
	            s.deadCumulative[i] = s.deadCumulative[i - 1] + s.dead[i];
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
					for (int age: world.params.upper_age_range) {
						// update female age bins
						long femaleAliveBetween = WorldBankCovid19Sim.popStatMap.get(SEX.FEMALE).aliveCumulative[age] 
				                  - WorldBankCovid19Sim.popStatMap.get(SEX.FEMALE).aliveCumulative[world.params.lower_age_range.get(idx)];
						WorldBankCovid19Sim.femalePopulationSizes.put(world.params.age_category_list.get(idx), (int) femaleAliveBetween);
						// update male age bins
						long maleAliveBetween = WorldBankCovid19Sim.popStatMap.get(SEX.MALE).aliveCumulative[age] 
				                  - WorldBankCovid19Sim.popStatMap.get(SEX.MALE).aliveCumulative[world.params.lower_age_range.get(idx)];
						WorldBankCovid19Sim.malePopulationSizes.put(world.params.age_category_list.get(idx), (int) maleAliveBetween);
						
						long totalAliveBetween = femaleAliveBetween + maleAliveBetween;
						WorldBankCovid19Sim.allPopulationSizes.put(world.params.age_category_list.get(idx), (int) totalAliveBetween);
						idx ++;
					}
				}
			};
	}
}
