package main.java.behaviours;

import main.java.behaviours.*;
import main.java.objects.*;
import main.java.sim.*;
import sim.engine.Steppable;
import swise.behaviours.BehaviourFramework;
import swise.behaviours.BehaviourNode;

public class InfectiousBehaviourFramework extends BehaviourFramework {
	
	WorldBankCovid19Sim myWorld;
	BehaviourNode susceptibleNode = null, exposedNode = null, presymptomaticNode= null, asymptomaticNode = null,
			mildNode = null, severeNode = null, criticalNode = null, recoveredNode = null, deadNode = null;
	
	// PARAMS to control development of disease
	
	public InfectiousBehaviourFramework(WorldBankCovid19Sim model){
		myWorld = model;
		
		// the default status
		susceptibleNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "susceptible"; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		
		// the agent has been exposed - determine whether the infection will develop
		exposedNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "exposed"; }

			/**
			 * After being exposed, the disease may develop in a number of ways.
			 */
			@Override
			public double next(Steppable s, double time) {
				Infection i = (Infection) s;
				if (i.getHost().isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
				//
				// it may be that the individual is exposed but not yet contagious - check if time has been set
				//
				
				if(i.time_contagious < Double.MAX_VALUE){ // if less, it has been changed from default value
					
					// maybe we have been triggered too soon - in that case, don't activate again until it is time!
					if(time < i.time_contagious)
						return i.time_contagious - time;
					// update the person's properties to show they have covid
					i.getHost().storeCovid();
					// The infected agent will either show symptoms or be asymptomatic - choose which at this time

					// moderate this based on the age of the host
					double mySymptLikelihood = myWorld.params.getLikelihoodByAge(
							myWorld.params.infection_p_sym_by_age, i.getHost().getAge());
					// activate the next step probabilistically
					if(myWorld.random.nextDouble() < mySymptLikelihood){
						i.setBehaviourNode(presymptomaticNode);
						i.getHost().getLocation().getRootSuperLocation().metric_new_cases_sympt++;
					}
					else{
						i.setBehaviourNode(asymptomaticNode);
						if(i.getHost() != null && i.getHost().getLocation() != null)
							i.getHost().getLocation().getRootSuperLocation().metric_new_cases_asympt++;
						else
							System.out.println("PROBLEM WITH INFECTION HOST OR LOCATION");
					}
					return 1;
				}
				
				//
				// another case: the agent is newly exposed! Determine whether the infection will take
				//
				
				double mySusceptLikelihood = myWorld.params.getLikelihoodByAge(
						myWorld.params.infection_r_sus_by_age, i.getHost().getAge());
				if(myWorld.random.nextDouble() < mySusceptLikelihood){
					
					// timekeep this
					i.time_infected = time;
					
					// the agent has been infected - set the time at which it will become infecTIOUS
					double timeUntilInfectious = myWorld.nextRandomLognormal(
							myWorld.params.exposedToInfectious_mean,
							myWorld.params.exposedToInfectious_std);
					i.time_contagious = time + timeUntilInfectious;
					// update the person's properties to show they have covid
					i.getHost().storeCovid();
					i.getHost().updateCovidCounter();

					return timeUntilInfectious;
				}
				
				// 
				// the final case is that the agent was exposed, but the infectious has not taken. 
				// In this case the agents just goes back to being susceptible.
				// 
				i.time_recovered = time;
				// update the person's properties to show they don't have covid
				i.getHost().removeCovid();
				i.setBehaviourNode(susceptibleNode);
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		
		// the agent is infectious, but not yet showing symptoms
		presymptomaticNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "presymptomatic"; }

			@Override
			public double next(Steppable s, double time) {
				
				// while presympotmatic, the agent is still infectious
				Infection i = (Infection) s;
				if (i.getHost().isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
				i.getHost().infectNeighbours();

				// determine when the infection will proceed to symptoms - this is
				// only a matter of time in this case
				if(time >= i.time_start_symptomatic){
					i.setBehaviourNode(mildNode);
				}
				else if(i.time_start_symptomatic == Double.MAX_VALUE ){
					double time_until_symptoms = myWorld.nextRandomLognormal(
							myWorld.params.infectiousToSymptomatic_mean, 
							myWorld.params.infectiousToSymptomatic_std);
					i.time_start_symptomatic = time + time_until_symptoms;
				}
				
				// update every timestep
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		
		// the agent is infectious, but will not show symptoms. They will eventually recover.
		asymptomaticNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "asymptomatic"; }

			@Override
			public double next(Steppable s, double time) {
				
				// although asympotmatic, the agent is still infectious
				Infection i = (Infection) s;
				if (i.getHost().isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
				i.getHost().infectNeighbours();
				i.getHost().setAsympt();

				// determine when the agent will recover - this is
				// only a matter of time in this case
				if(time >= i.time_recovered){
					i.setBehaviourNode(recoveredNode);
				}
				else if(i.time_recovered == Double.MAX_VALUE){ // has not been set
					
					double time_until_recovered = myWorld.nextRandomLognormal(
							myWorld.params.asymptomaticToRecovery_mean, 
							myWorld.params.asymptomaticToRecovery_std);
					i.time_recovered = time + time_until_recovered;
				}
				
				// update every timestep
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		
		// the agent has a mild case and is infectious. They may recover, or else progress to a severe case.
		mildNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "mild_case"; }

			@Override
			public double next(Steppable s, double time) {
				
				// the agent is infectious
				Infection i = (Infection) s;
				if (i.getHost().isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
				i.getHost().infectNeighbours();
				if (!i.getHost().hasMild()) {
					i.getHost().setMild();
					}

				// if the agent is scheduled to recover, make sure that it
				// does so
				if(time >= i.time_recovered){
					i.setBehaviourNode(recoveredNode);
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(time >= i.time_start_severe){
					i.setBehaviourNode(severeNode);

					Person p = (Person) i.getHost();
					
					// record this event
					p.getLocation().getRootSuperLocation().metric_new_hospitalized++;
					
					// send them home sick and immobilised
					p.setMobility(false);
					p.sendHome(); 
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered == Double.MAX_VALUE && i.time_start_severe == Double.MAX_VALUE){

					// determine if the patient will become sicker
					double mySevereLikelihood = myWorld.params.getLikelihoodByAge(
							myWorld.params.infection_p_sev_by_age, i.getHost().getAge());
					
					if(myWorld.random.nextDouble() < mySevereLikelihood){
						double time_until_severe = myWorld.nextRandomLognormal(
								myWorld.params.symptomaticToSevere_mean, 
								myWorld.params.symptomaticToSevere_std);
						i.time_start_severe = time + time_until_severe;
					}
					
					// if not, they will recover: schedule this instead
					else {
						double time_until_recovered = myWorld.nextRandomLognormal(
								myWorld.params.sympomaticToRecovery_mean, 
								myWorld.params.sympomaticToRecovery_std);
						i.time_recovered = time + time_until_recovered;
						return 1;
					}
				}
				
				// update every timestep
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};

		// the agent has a severe case and is infectious. They may recover, or else progress to a critical case.
		severeNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "severe_case"; }

			@Override
			public double next(Steppable s, double time) {
				
				// the agent is infectious
				Infection i = (Infection) s;
				if (i.getHost().isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
				i.getHost().infectNeighbours();
				if (i.getHost().hasMild()) {
					i.getHost().removeMild();
					}
				if (!i.getHost().hasSevere()) {
					i.getHost().setSevere();
				}
				// if the agent is scheduled to recover, make sure that it
				// does so
				if(time >= i.time_recovered){
					i.setBehaviourNode(recoveredNode);
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(time >= i.time_start_critical){
					i.setBehaviourNode(criticalNode);
					i.getHost().getLocation().getRootSuperLocation().metric_new_critical++;
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered == Double.MAX_VALUE && i.time_start_critical == Double.MAX_VALUE){

					double myCriticalLikelihood = myWorld.params.getLikelihoodByAge(
							myWorld.params.infection_p_cri_by_age, i.getHost().getAge());
					
					// determine if the patient will become sicker
					if(myWorld.random.nextDouble() < myCriticalLikelihood){
						double time_until_critical = myWorld.nextRandomLognormal(
								myWorld.params.severeToCritical_mean, 
								myWorld.params.severeToCritical_std);
						i.time_start_critical = time + time_until_critical;
					}
					
					// if not, they will recover: schedule this instead
					else {
						double time_until_recovered = myWorld.nextRandomLognormal(
								myWorld.params.severeToRecovery_mean, 
								myWorld.params.severeToRecovery_std);
						i.time_recovered = time + time_until_recovered;
						return 1;
					}
				}
				
				// update every timestep
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};

		// the agent has a critical case and is infectious. They may recover, or else progress to death.
		criticalNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "critical_case"; }

			@Override
			public double next(Steppable s, double time) {
				
				// the agent is infectious
				Infection i = (Infection) s;
				if (i.getHost().isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
				i.getHost().infectNeighbours();
				if (i.getHost().hasSevere()) {
					i.getHost().removeSevere();
				}
				if (!i.getHost().hasCritical()) {
					i.getHost().setCritical();
				}
				// if the agent is scheduled to recover, make sure that it
				// does so
				if(time >= i.time_recovered ){
					i.setBehaviourNode(recoveredNode);
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(time >= i.time_died){
					i.setBehaviourNode(deadNode);
					
					if(!i.getHost().isDeadFromCovid()) {
						Location myDistrict = i.getHost().getLocation().getRootSuperLocation();
						myDistrict.metric_died_count++;
						myDistrict.metric_new_deaths++;
					}
					else
						System.out.println("hmm how did you get here?");
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered == Double.MAX_VALUE && i.time_died == Double.MAX_VALUE ){

					double myDeathLikelihood = myWorld.params.getLikelihoodByAge(
							myWorld.params.infection_p_dea_by_age, i.getHost().getAge());
					
					
					// determine if the patient will die
					if(myWorld.random.nextDouble() < myDeathLikelihood){
						double time_until_death = myWorld.nextRandomLognormal(
								myWorld.params.criticalToDeath_mean, 
								myWorld.params.criticalToDeath_std);
						i.time_died = time + time_until_death;
					}
					
					// if not, they will recover: schedule this instead
					else {
						double time_until_recovered = myWorld.nextRandomLognormal(
								myWorld.params.criticalToRecovery_mean, 
								myWorld.params.criticalToRecovery_std);
						i.time_recovered = time + time_until_recovered;
						return 1;
					}
				}
				
				// update every timestep
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		
		// the agent has recovered.
		recoveredNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "recovered"; }

			@Override
			public double next(Steppable s, double time) {

				Infection i = (Infection) s;
				if (i.getHost().isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
				i.time_recovered = time;
				// update person's properties
				i.getHost().setRecovered();
				i.getHost().removeCovid();
				i.getHost().getLocation().getRootSuperLocation().metric_new_recovered++;

				// the Person may have stopped moving when ill - reactivate!
				if(i.getHost().isImmobilised()){
					i.getHost().setMobility(true);
					myWorld.schedule.scheduleOnce(i.getHost());	// schedule the agent to begin moving again!				
				}
				
				// no need to update again!
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		
		deadNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "dead"; }

			@Override
			public double next(Steppable s, double time) {
				Infection i = (Infection) s;
				// remove covid from person object
				i.getHost().removeCovid();
				i.getHost().die("covid");
				i.time_died = time;
								
				return Double.MAX_VALUE; // no need to run ever again
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return true;
			}
			
		};
		

		
		entryPoint = exposedNode;
	}

	public BehaviourNode getStandardEntryPoint(){ return susceptibleNode; }
	public BehaviourNode getInfectedEntryPoint(Location l){
				
		if(myWorld.random.nextDouble() < .5){ // TODO make this based on real data
			l.getRootSuperLocation().metric_new_cases_sympt++;
			return presymptomaticNode;
		}
		else{
			l.getRootSuperLocation().metric_new_cases_asympt++;
			return asymptomaticNode;
		}
	} 

}
