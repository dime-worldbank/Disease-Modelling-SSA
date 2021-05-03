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
				// it may be that the individual is exposed but not yet contagious - check if time has been set
				//
				
				if(i.time_contagious < Double.MAX_VALUE){ // if less, it has been changed from default value
					
					// maybe we have been triggered too soon - in that case, don't activate again until it is time!
					if(time < i.time_contagious)
						return i.time_contagious - time;

					// otherwise, the agent is now infected and contagious!
					myWorld.record_numInfected++;
					myWorld.record_numContagious++;
					
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
				
				myWorld.record_numExposed++; // record this exposure event
				
				if(myWorld.random.nextDouble() < i.getHost().getSusceptibility()){
					
					// timekeep this
					i.time_infected = time;
					
					// the agent has been infected - set the time at which it will become infecTIOUS
					double timeUntilInfectious = myWorld.nextRandomLognormal(
							myWorld.params.exposedToInfectious_mean,
							myWorld.params.exposedToInfectious_std);
					i.time_contagious = time + timeUntilInfectious;
					
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
				if(time >= i.time_start_symptomatic){
					i.setBehaviourNode(mildNode);
					myWorld.record_numSymptomatic++;
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
				if(time >= i.time_recovered){
					i.setBehaviourNode(recoveredNode);
					myWorld.record_numAsymptomatic--;
				}
				else if(i.time_recovered == Double.MAX_VALUE){ // has not been set
					
					myWorld.record_numAsymptomatic++;
					
					double time_until_recovered = myWorld.nextRandomLognormal(
							myWorld.params.asymptomaticToRecovery_mean, 
							myWorld.params.asymptomaticToRecovery_std);
					i.time_recovered = time + time_until_recovered;
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
				if(time >= i.time_recovered){
					i.setBehaviourNode(recoveredNode);
					myWorld.record_numSymptomatic--;
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(time >= i.time_start_severe){
					i.setBehaviourNode(severeNode);
					// TODO make agent immobile?
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered == Double.MAX_VALUE && i.time_start_severe == Double.MAX_VALUE){

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
				if(time >= i.time_recovered){
					i.setBehaviourNode(recoveredNode);
					myWorld.record_numSevere--;
					myWorld.record_numSymptomatic--;
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(time >= i.time_start_critical){
					i.setBehaviourNode(criticalNode);
					myWorld.record_numSevere--;
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered == Double.MAX_VALUE && i.time_start_critical == Double.MAX_VALUE){

					myWorld.record_numSevere++;
					
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
				if(time >= i.time_recovered ){
					i.setBehaviourNode(recoveredNode);
					myWorld.record_numCritical--;
					myWorld.record_numSymptomatic--;
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(time >= i.time_died ){
					i.setBehaviourNode(deadNode);
					myWorld.record_numCritical--;
					myWorld.record_numSymptomatic--;
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered == Double.MAX_VALUE && i.time_died == Double.MAX_VALUE ){

					myWorld.record_numCritical++;

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
				myWorld.record_numContagious--;
				myWorld.record_numRecovered++;
				
				((Infection)s).time_recovered = time;

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
				i.time_died = time;
				
				myWorld.record_numDied++;
				myWorld.record_numContagious--;
				
				return Double.MAX_VALUE; // no need to run ever again
			}
			
		};
		

		
		entryPoint = exposedNode;
	}

	public BehaviourNode getStandardEntryPoint(){ return susceptibleNode; }
	public BehaviourNode getInfectedEntryPoint(){
		myWorld.record_numInfected++;
		myWorld.record_numContagious++;
				
		if(myWorld.random.nextDouble() < .5) // TODO make this based on real data
			return presymptomaticNode;
		else
			return asymptomaticNode;
	} 

}