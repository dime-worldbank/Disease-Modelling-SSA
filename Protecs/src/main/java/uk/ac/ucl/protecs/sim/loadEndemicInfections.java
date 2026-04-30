package uk.ac.ucl.protecs.sim;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import uk.ac.ucl.protecs.behaviours.diseaseProgression.HIVDiseaseProgressionFramework;
import uk.ac.ucl.protecs.objects.diseases.HIV;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

import java.util.stream.Collectors;


public class loadEndemicInfections{
	

	public static List<Person> get_demographic(WorldBankCovid19Sim world, int[] age_range, SEX sex) {
	    return world.agents.stream()
	            .filter(p -> p.inAgeRange(age_range))
	            .filter(p -> p.isOfSex(sex))
	            .collect(Collectors.toList());
	}
	
	static void seed_endemic_infections(WorldBankCovid19Sim world) {

	    for (Entry<DISEASE, HashMap<SEX, HashMap<String, Double>>> diseaseEntry : world.params.prevalenceLineList.entrySet()) {
	        DISEASE disease = diseaseEntry.getKey();
	        HashMap<SEX, HashMap<String, Double>> sexMap = diseaseEntry.getValue();

	        for (Entry<SEX, HashMap<String, Double>> sexEntry : sexMap.entrySet()) {
	            SEX sex = sexEntry.getKey();
	            HashMap<String, Double> ageMap = sexEntry.getValue();

	            for (Entry<String, Double> ageEntry : ageMap.entrySet()) {
	                String age_range = ageEntry.getKey();
	                double prevalence = ageEntry.getValue();

	                int[] bounds = convert_GBD_boundary_to_int(age_range);
	                List<Person> eligible = get_demographic(world, bounds, sex);

	                for (Person p : eligible) {
	                    if (world.random.nextDouble() < prevalence) {

	                        switch (disease) {
	                            case HIV: {
	        						if (world.hivFramework == null) {
	        							world.hivFramework = new HIVDiseaseProgressionFramework(world);
	        							}
	                                HIV inf = new HIV(p, null, world.hivFramework.getEntryPoint(), world, 0);
	                                world.schedule.scheduleOnce(inf, world.param_schedule_infecting);
	                            }
	                            default: {
	                                // no-op for now
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    }
	}

	private static int[] convert_GBD_boundary_to_int(String ageRange) {
	    ageRange = ageRange.trim();

	    // Case: "<5 years"
	    if (ageRange.startsWith("<")) {
	        int upper = Integer.parseInt(ageRange.replaceAll("[^0-9]", ""));
	        return new int[]{0, upper - 1};
	    }

	    // Case: "95+ years"
	    if (ageRange.contains("+")) {
	        int lower = Integer.parseInt(ageRange.replaceAll("[^0-9]", ""));
	        return new int[]{lower, 120}; // or Integer.MAX_VALUE if you prefer
	    }

	    // Case: "X-Y years"
	    String[] parts = ageRange.replace(" years", "").split("-");
	    int lower = Integer.parseInt(parts[0]);
	    int upper = Integer.parseInt(parts[1]);

	    return new int[]{lower, upper};
	}
}