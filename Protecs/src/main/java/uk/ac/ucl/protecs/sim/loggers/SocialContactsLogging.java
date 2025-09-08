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
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class SocialContactsLogging{
	
				
		
		public static Steppable WorkplaceContactsReporter(WorldBankCovid19Sim myWorld){
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
					String outputColumnNames = t + "lockdown_applied"  + t + "occupation"  + t + "av_contacts_work_happened"+ t + "std_contacts_work_happened" + 
					t + "av_contacts_work_scheduled"+ t + "std_contacts_work_scheduled" + "\n";
					if (firstTimeReporting) {
						socialContactsOutput += "day" + outputColumnNames;
						firstTimeReporting = false;
					}
				
					for (OCCUPATION status: world.occupationsInSim) {
						double av_workplace_contacts_happened = 0;
						double std_workplace_contacts_happened = 0;

						
						double av_workplace_contacts_scheduled = 0;
						double std_workplace_contacts_scheduled = 0;

						
						try {
							List<Person> eligiblePersons = economic_alive_with_disease_at_location.get(status).get(true).get(true);
							ArrayList<Integer> workplaceContactCountsHappened = new ArrayList<Integer>();
							ArrayList<Integer> workplaceContactCountsScheduled = new ArrayList<Integer>();
							workplaceContactCountsHappened.add(0);
							workplaceContactCountsScheduled.add(0);
							for (Person p: eligiblePersons) {
								if (p.getNumberOfWorkplaceInteractionsHappened() > 0) {
									workplaceContactCountsHappened.add(p.getNumberOfWorkplaceInteractionsHappened());
									}
								if (p.getNumberOfWorkplaceInteractions() > 0) {
									workplaceContactCountsScheduled.add(p.getNumberOfWorkplaceInteractions());
									}


							}
							if (workplaceContactCountsHappened.size() > 1) {
								workplaceContactCountsHappened.remove(0);
							}
							if (workplaceContactCountsScheduled.size() > 1) {
								workplaceContactCountsScheduled.remove(0);
							}
							av_workplace_contacts_happened = workplaceContactCountsHappened.stream().mapToDouble(a -> a).average().getAsDouble();
							std_workplace_contacts_happened = Math.sqrt(workplaceContactCountsHappened.stream().map(i -> i - workplaceContactCountsHappened.stream().mapToDouble(a -> a).average().getAsDouble()).map(i -> i*i).mapToDouble(i -> i).average().getAsDouble());

							av_workplace_contacts_scheduled = workplaceContactCountsScheduled.stream().mapToDouble(a -> a).average().getAsDouble();
							std_workplace_contacts_scheduled = Math.sqrt(workplaceContactCountsScheduled.stream().map(i -> i - workplaceContactCountsScheduled.stream().mapToDouble(a -> a).average().getAsDouble()).map(i -> i*i).mapToDouble(i -> i).average().getAsDouble());

							} 
							catch (Exception e) {
									// no one matching criteria in sim
							}
							
						int lockedDown = world.lockedDown? 1 : 0;
							
						socialContactsOutput += dayOfSimulation + t + lockedDown + t + status  + t + av_workplace_contacts_happened + t + std_workplace_contacts_happened  + 
								t + av_workplace_contacts_scheduled + t + std_workplace_contacts_scheduled + "\n";
						}
					
		
					ImportExport.exportMe(world.workplaceContactsOutputFilename, socialContactsOutput, world.timer);
		
				}
			};
		}
		
		public static Steppable CommunityContactsReporter(WorldBankCovid19Sim myWorld){
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
					String outputColumnNames = t + "lockdown_applied"  + t + "occupation"  + t + "av_contacts_community_happened" + t + "std_contacts_community_happened" + t + 
					"av_contacts_community_scheduled" + t + "std_contacts_community_scheduled" + "\n";
					if (firstTimeReporting) {
						socialContactsOutput += "day" + outputColumnNames;
						firstTimeReporting = false;
					}
				
					for (OCCUPATION status: world.occupationsInSim) {
						double av_community_contacts_happened = 0;
						double std_community_contacts_happened = 0;

						double av_community_contacts_scheduled = 0;
						double std_community_contacts_scheduled = 0;
						
						try {
							List<Person> eligiblePersons = economic_alive_with_disease_at_location.get(status).get(true).get(true);

							ArrayList<Integer> communityContactCountsHappened = new ArrayList<Integer>();
							ArrayList<Integer> communityContactCountsScheduled = new ArrayList<Integer>();
							communityContactCountsHappened.add(0);
							communityContactCountsScheduled.add(0);
							for (Person p: eligiblePersons) {
								if (p.getNumberOfCommunityInteractionsHappened() > 0) {
									communityContactCountsHappened.add(p.getNumberOfCommunityInteractionsHappened());
									}
								if (p.getNumberOfCommunityInteractions() > 0) {
									communityContactCountsScheduled.add(p.getNumberOfCommunityInteractions());
									}

							}
							if (communityContactCountsHappened.size() > 1) {
								communityContactCountsHappened.remove(0);
							}
							if (communityContactCountsScheduled.size() > 1) {
								communityContactCountsScheduled.remove(0);
							}
							av_community_contacts_happened = communityContactCountsHappened.stream().mapToDouble(a -> a).average().getAsDouble();
							std_community_contacts_happened = Math.sqrt(communityContactCountsHappened.stream().map(i -> i - communityContactCountsHappened.stream().mapToDouble(a -> a).average().getAsDouble()).map(i -> i*i).mapToDouble(i -> i).average().getAsDouble());

							av_community_contacts_scheduled = communityContactCountsScheduled.stream().mapToDouble(a -> a).average().getAsDouble();
							std_community_contacts_scheduled = Math.sqrt(communityContactCountsScheduled.stream().map(i -> i - communityContactCountsScheduled.stream().mapToDouble(a -> a).average().getAsDouble()).map(i -> i*i).mapToDouble(i -> i).average().getAsDouble());

							} 
							catch (Exception e) {
									// no one matching criteria in sim
							}
							
						int lockedDown = world.lockedDown? 1 : 0;
							
						socialContactsOutput += dayOfSimulation + t + lockedDown + t + status  + t + av_community_contacts_happened + t + std_community_contacts_happened +
								t + av_community_contacts_scheduled + t + std_community_contacts_scheduled + "\n";
						}
					
		
					ImportExport.exportMe(world.communityContactsOutputFilename, socialContactsOutput, world.timer);
		
				}
			};
		}

}