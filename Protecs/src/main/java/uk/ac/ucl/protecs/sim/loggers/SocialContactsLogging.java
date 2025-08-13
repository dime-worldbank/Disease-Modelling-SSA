package uk.ac.ucl.protecs.sim.loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.sim.ImportExport;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class SocialContactsLogging{
	
				
		
		public static Steppable UniqueContactsReporter(WorldBankCovid19Sim myWorld){
			return new Steppable() {
				WorldBankCovid19Sim world = myWorld;
				boolean firstTimeReporting = true;
	
				
				// get those alive at location with that occupation
				Map<OCCUPATION,  Map<Boolean, Map<Boolean, List<Person>>>> economic_alive_with_disease_at_location = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getEconStatus,
										Collectors.groupingBy(
												Person::isAlive,
												Collectors.groupingBy(
														Person::hasADisease
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
					String outputColumnNames = t + "lockdown_applied"  + t + "occupation"  + t + "av_contacts_work"+ t + "av_contacts_community" + t + "total_average_contacts"+ "\n";
					if (firstTimeReporting) {
						socialContactsOutput += "day" + outputColumnNames;
						firstTimeReporting = false;
					}
				
					for (OCCUPATION status: world.occupationsInSim) {
						double av_home_contacts = 0;
						double av_workplace_contacts = 0;
						double av_community_contacts = 0;
						double av_total_contacts = 0;
						try {
							List<Person> eligiblePersons = economic_alive_with_disease_at_location.get(status).get(true).get(true);
							ArrayList<Integer> workplaceContactCounts = new ArrayList<Integer>();
							ArrayList<Integer> communityContactCounts = new ArrayList<Integer>();
							for (Person p: eligiblePersons) {
								workplaceContactCounts.add(p.getNumberOfWorkplaceInteractionsHappened());
								communityContactCounts.add(p.getNumberOfCommunityInteractionsHappened());
							}
							av_workplace_contacts += workplaceContactCounts.stream().mapToDouble(a -> a).average().getAsDouble();
							av_community_contacts += communityContactCounts.stream().mapToDouble(a -> a).average().getAsDouble();
							av_total_contacts += av_home_contacts + av_workplace_contacts + av_community_contacts;								} 
							catch (Exception e) {
									// no one matching criteria in sim
							}
							
						int lockedDown = world.lockedDown? 1 : 0;
							
						socialContactsOutput += dayOfSimulation + t + lockedDown + t + status  + t + av_workplace_contacts + t + av_community_contacts  + t + av_total_contacts + "\n";
						}
					
		
					ImportExport.exportMe(world.socialContactsOutputFilename, socialContactsOutput, world.timer);
		
				}
			};
		}

}
