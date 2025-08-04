package uk.ac.ucl.protecs.sim.loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.locations.Location.LocationCategory;
import uk.ac.ucl.protecs.sim.ImportExport;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class SocialContactsLogging{
	
				
		
		public static Steppable UniqueContactsReporter(WorldBankCovid19Sim myWorld){
			return new Steppable() {
				WorldBankCovid19Sim world = myWorld;
				boolean firstTimeReporting = true;
	
				
				// get those alive at location with that occupation
				Map<OCCUPATION, Map<String, Map<Boolean, List<Person>>>> economic_alive_at_location = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getEconStatus,
								Collectors.groupingBy(
										Person::getCurrentAdminZone,
										Collectors.groupingBy(
												Person::isAlive
												)
										)
								)
						);
				@Override
				public void step(SimState arg0) {
					// get day
					int dayOfSimulation = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
					String socialContactsOutput = "";
		
					String t = "\t";
					String outputColumnNames = t + "lockdown_applied" + t + "district_id" + t + "occupation" + t + "av_contacts_home" + t + "av_contacts_work"+ t + "av_contacts_community" + t + "total_average_contacts"+ "\n";
					if (firstTimeReporting) {
						socialContactsOutput += "day" + outputColumnNames;
						firstTimeReporting = false;
					}
					for (String zone: world.params.adminZoneNames) {
						for (OCCUPATION status: world.occupationsInSim) {
							double av_home_contacts = 0;
							double av_workplace_contacts = 0;
							double av_community_contacts = 0;
							double av_total_contacts = 0;
							try {
								List<Person> eligiblePersons = economic_alive_at_location.get(status).get(zone).get(true);
								ArrayList<Integer> homeContactCounts = new ArrayList<Integer>();
								ArrayList<Integer> workplaceContactCounts = new ArrayList<Integer>();
								ArrayList<Integer> communityContactCounts = new ArrayList<Integer>();
								for (Person p: eligiblePersons) {
									homeContactCounts.add(p.getListInteractionsByLocation().get(LocationCategory.HOME).size());
									workplaceContactCounts.add(p.getListInteractionsByLocation().get(LocationCategory.WORKPLACE).size());
									communityContactCounts.add(p.getListInteractionsByLocation().get(LocationCategory.COMMUNITY).size());
								}
								av_home_contacts += homeContactCounts.stream().mapToDouble(a -> a).average().getAsDouble();
								av_workplace_contacts += workplaceContactCounts.stream().mapToDouble(a -> a).average().getAsDouble();
								av_community_contacts += communityContactCounts.stream().mapToDouble(a -> a).average().getAsDouble();
								av_total_contacts += av_home_contacts + av_workplace_contacts + av_community_contacts;								} 
								catch (Exception e) {
									// no one matching criteria in sim
								}
							
							int lockedDown = world.lockedDown? 1 : 0;
							
							socialContactsOutput += dayOfSimulation + t + lockedDown + t + zone + t + status + t + av_home_contacts + t + av_workplace_contacts + t + av_community_contacts  + t + av_total_contacts + "\n";
						}
					}
		
					ImportExport.exportMe(world.socialContactsOutputFilename, socialContactsOutput, world.timer);
		
				}
			};
		}

}
