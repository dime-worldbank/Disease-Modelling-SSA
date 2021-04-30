package behaviours;

import objects.Infection;
import objects.Person;
import sim.WorldBankCovid19Sim;
import sim.engine.Steppable;

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
			public String getTitle() { return "Susceptible"; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}
			
		};
		
		// the agent has been exposed - determine whether the infection will develop
		exposedNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Exposed"; }

			/**
			 * After being exposed, the disease may develop in a number of ways.
			 */
			@Override
			public double next(Steppable s, double time) {
				Infection i = (Infection) s;
				
				//
				// it may be that the individual is exposed but not yet infectious - if so, check if it is time yet
				//
				
				if(i.time_infectious > 0){
					
					// maybe we have been triggered too soon - in that case, don't activate again until it is time!
					if(i.time_infectious < time)
						return i.time_infectious - time;

					// otherwise, the agent is now infectious!
					myWorld.record_numInfected++;
					
					// The infected agent will either show symptoms or be asymptomatic - choose which at this time
					
					if(myWorld.random.nextDouble() < .5) // TODO make this based on real data
						i.setBehaviourNode(presymptomaticNode);
					else
						i.setBehaviourNode(asymptomaticNode);
					return 1;
				}
				
				//
				// another case: the agent is newly exposed! Determine whether the infection will take
				//
				
				if(myWorld.random.nextDouble() < i.getHost().getSusceptibility()){
					
					// the agent has been infected - set the time at which it will become infecTIOUS
					double timeUntilInfectious = myWorld.nextRandomLognormal(
							myWorld.params.exposedToInfectious_mean,
							myWorld.params.exposedToInfectious_std);
					i.time_infectious = time + timeUntilInfectious;
					
					return timeUntilInfectious;
				}
				
				// 
				// the final case is that the agent was exposed, but the infectious has not taken. 
				// In this case the agents just goes back to being susceptible.
				// 
				i.setBehaviourNode(susceptibleNode);
				return Double.MAX_VALUE;
			}
			
		};
		
		// the agent is infectious, but not yet showing symptoms
		presymptomaticNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Presymptomatic"; }

			@Override
			public double next(Steppable s, double time) {
				
				// while presympotmatic, the agent is still infectious
				Infection i = (Infection) s;
				i.getHost().infectNeighbours();

				// determine when the infection will proceed to symptoms - this is
				// only a matter of time in this case
				if(i.time_start_symptomatic >= time){
					i.setBehaviourNode(mildNode);
				}
				else if(i.time_start_symptomatic < 0){
					double time_until_symptoms = myWorld.nextRandomLognormal(
							myWorld.params.infectiousToSymptomatic_mean, 
							myWorld.params.infectiousToSymptomatic_std);
					i.time_start_symptomatic = time + time_until_symptoms;
				}
				
				// update every timestep
				return 1;
			}
			
		};
		
		// the agent is infectious, but will not show symptoms. They will eventually recover.
		asymptomaticNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Asymptomatic"; }

			@Override
			public double next(Steppable s, double time) {
				
				// although asympotmatic, the agent is still infectious
				Infection i = (Infection) s;
				i.getHost().infectNeighbours();

				// determine when the agent will recover - this is
				// only a matter of time in this case
				if(i.time_recovered >= time){
					i.setBehaviourNode(recoveredNode);
				}
				else if(i.time_recovered < 0){ // may not yet have been set
					double time_until_recovered = myWorld.nextRandomLognormal(
							myWorld.params.asymptomaticToRecovery_mean, 
							myWorld.params.asymptomaticToRecovery_std);
					i.time_start_symptomatic = time + time_until_recovered;
				}
				
				// update every timestep
				return 1;
			}
			
		};
		
		// the agent has a mild case and is infectious. They may recover, or else progress to a severe case.
		mildNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "MildCase"; }

			@Override
			public double next(Steppable s, double time) {
				
				// the agent is infectious
				Infection i = (Infection) s;
				i.getHost().infectNeighbours();

				// if the agent is scheduled to recover, make sure that it
				// does so
				if(i.time_recovered >= time){
					i.setBehaviourNode(recoveredNode);
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(i.time_start_severe >= time){
					i.setBehaviourNode(severeNode);
					// TODO make agent immobile?
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered < 0 && i.time_start_severe < 0){

					// determine if the patient will become sicker
					if(myWorld.random.nextDouble() < .5){
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
			
		};

		// the agent has a severe case and is infectious. They may recover, or else progress to a critical case.
		severeNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "SevereCase"; }

			@Override
			public double next(Steppable s, double time) {
				
				// the agent is infectious
				Infection i = (Infection) s;
				i.getHost().infectNeighbours();

				// if the agent is scheduled to recover, make sure that it
				// does so
				if(i.time_recovered >= time){
					i.setBehaviourNode(recoveredNode);
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(i.time_start_critical >= time){
					i.setBehaviourNode(criticalNode);
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered < 0 && i.time_start_critical < 0){

					// determine if the patient will become sicker
					if(myWorld.random.nextDouble() < .5){
						double time_until_critical = myWorld.nextRandomLognormal(
								myWorld.params.severeToCritical_mean, 
								myWorld.params.severeToCritical_std);
						i.time_start_severe = time + time_until_critical;
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
			
		};

		// the agent has a critical case and is infectious. They may recover, or else progress to death.
		criticalNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "CriticalCase"; }

			@Override
			public double next(Steppable s, double time) {
				
				// the agent is infectious
				Infection i = (Infection) s;
				i.getHost().infectNeighbours();

				// if the agent is scheduled to recover, make sure that it
				// does so
				if(i.time_recovered >= time){
					i.setBehaviourNode(recoveredNode);
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(i.time_died >= time){
					i.setBehaviourNode(deadNode);
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered < 0 && i.time_died < 0){

					// determine if the patient will die
					if(myWorld.random.nextDouble() < .5){
						double time_until_death = myWorld.nextRandomLognormal(
								myWorld.params.criticalToDeath_mean, 
								myWorld.params.criticalToDeath_std);
						i.time_start_severe = time + time_until_death;
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
			
		};
		
		// the agent has recovered.
		recoveredNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Recovered"; }

			@Override
			public double next(Steppable s, double time) {

				myWorld.record_numInfected--;

				// no need to update again!
				return Double.MAX_VALUE;
			}
			
		};
		deadNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Dead"; }

			@Override
			public double next(Steppable s, double time) {
				Infection i = (Infection) s;
				i.getHost().die();
				return Double.MAX_VALUE; // no need to run ever again
			}
			
		};
		

		
		entryPoint = exposedNode;
	}

	public BehaviourNode getStandardEntryPoint(){ return susceptibleNode; }
	public BehaviourNode getInfectedEntryPoint(Person host){
		myWorld.record_numInfected++;
		if(myWorld.random.nextDouble() < .5) // TODO make this based on real data
			return presymptomaticNode;
		else
			return asymptomaticNode;
	} 

}