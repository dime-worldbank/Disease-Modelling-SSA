package uk.ac.ucl.protecs.behaviours.diseaseProgression;



import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusInfection;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.objects.locations.Location.LocationCategory;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class CoronavirusDiseaseProgressionFramework extends DiseaseProgressionBehaviourFramework {
	
	
	public enum CoronavirusBehaviourNodeTitle{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"), PRESYMPTOMATIC("presymptomatic"), ASYMPTOMATIC("asymptomatic"), 
		MILD("mild"), SEVERE("severe"), CRITICAL("critical"), RECOVERED("recovered"), DEAD("dead");
         
        public String key;
     
        CoronavirusBehaviourNodeTitle(String key) { this.key = key; }
    
        static CoronavirusBehaviourNodeTitle getValue(String x) {
        	switch (x) {
        	case "susceptible":
        		return SUSCEPTIBLE;
        	case "exposed":
        		return EXPOSED;
        	case "presymptomatic":
        		return PRESYMPTOMATIC;
        	case "asymptomatic":
        		return ASYMPTOMATIC;
        	case "mild":
        		return MILD;	
        	case "severe":
        		return SEVERE;	
        	case "critical":
        		return CRITICAL;
        	case "recovered":
        		return RECOVERED;
        	case "dead":
        		return DEAD;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}
	
	// PARAMS to control development of disease
	
	public CoronavirusDiseaseProgressionFramework(WorldBankCovid19Sim myWorld){
		super(myWorld);
		
		// the default status
		this.susceptibleNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return CoronavirusBehaviourNodeTitle.SUSCEPTIBLE.key; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		// the agent has been exposed - determine whether the infection will develop
		this.exposedNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return CoronavirusBehaviourNodeTitle.EXPOSED.key; }

			/**
			 * After being exposed, the disease may develop in a number of ways.
			 */
			@Override
			public double next(Steppable s, double time) {
				CoronavirusInfection i = (CoronavirusInfection) s;
				if (((Person) i.getHost()).isDeadFromOther()) { 
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
					// The infected agent will either show symptoms or be asymptomatic - choose which at this time

					// moderate this based on the age of the host
					double mySymptLikelihood = myWorld.params.getLikelihoodByAge(
							myWorld.params.infection_p_sym_by_age, myWorld.params.infection_age_params, ((Person) i.getHost()).getAge());
					assert (mySymptLikelihood >= 0.0) & (mySymptLikelihood <= 1.0) : "probability out of bounds " + mySymptLikelihood;
					assert i.getHost() != null : "PROBLEM WITH INFECTION IN PERSON. INFECTION IS NULL " + ((Person) i.getHost()).getID();
					assert i.getHost().getLocation() != null : "PROBLEM WITH LOCATION, LOCATION IS NULL" + i.getHost().getLocation().getId();

					// activate the next step probabilistically
					if(myWorld.random.nextDouble() < mySymptLikelihood){
						i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.PRESYMPTOMATIC));
						i.getHost().getLocation().getRootSuperLocation().metric_new_cases_sympt++;
						// Store this person's covid
					}
					else{
						i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.ASYMPTOMATIC));
						if(i.getHost() != null && i.getHost().getLocation() != null) {
							i.getHost().getLocation().getRootSuperLocation().metric_new_cases_asympt++;
							// Store this person's covid
							}
						else
							System.out.println("PROBLEM WITH INFECTION HOST OR LOCATION");
					}
					return 1;
				}
				
				//
				// another case: the agent is newly exposed! Determine whether the infection will take
				//
				
				double mySusceptLikelihood = myWorld.params.getLikelihoodByAge(
						myWorld.params.infection_r_sus_by_age, myWorld.params.infection_age_params, ((Person) i.getHost()).getAge());
				if(myWorld.random.nextDouble() < mySusceptLikelihood){
					
					// timekeep this
					i.time_infected = time;
//					---------------- mySusceptLikelihood is sometimes greater than 1, is this correct -------------------------------------
//					assert (mySusceptLikelihood >= 0.0) & (mySusceptLikelihood <= 1.0): "probability out of bounds: " + mySusceptLikelihood;
					assert i.getHost() != null : "PROBLEM WITH INFECTION IN PERSON. INFECTION IS NULL " + ((Person) i.getHost()).getID();
					assert i.getHost().getLocation() != null : "PROBLEM WITH LOCATION, LOCATION IS NULL" + i.getHost().getLocation().getId();
					// the agent has been infected - set the time at which it will become infecTIOUS
					double timeUntilInfectious = myWorld.nextRandomLognormal(
							myWorld.params.exposedToInfectious_mean,
							myWorld.params.exposedToInfectious_std);
					assert (timeUntilInfectious > 0): "Something has gone wrong in deciding when a person will become infectious, time is not in future: " + timeUntilInfectious;
					i.time_contagious = time + timeUntilInfectious;
					// update the person's properties to show they have covid

					return timeUntilInfectious;
				}
				
				// 
				// the final case is that the agent was exposed, but the infectious has not taken. 
				// In this case the agents just goes back to being susceptible.
				// 
				i.time_recovered = time;
				// update the person's properties to show they don't have covid
				i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.SUSCEPTIBLE));
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		// the agent is infectious, but not yet showing symptoms
		this.presymptomaticNode = new BehaviourNode(){
			
			
			@Override
			public String getTitle() { return CoronavirusBehaviourNodeTitle.PRESYMPTOMATIC.key; }

			@Override
			public double next(Steppable s, double time) {
				
				// while presympotmatic, the agent is still infectious
				CoronavirusInfection i = (CoronavirusInfection) s;
				if (((Person) i.getHost()).isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
//				((Person) i.getHost()).infectNeighbours();
				// determine when the infection will proceed to symptoms - this is
				// only a matter of time in this case
				if(time >= i.time_start_symptomatic){
					i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.MILD));
				}
				else if(i.time_start_symptomatic == Double.MAX_VALUE ){
					double time_until_symptoms = myWorld.nextRandomLognormal(
							myWorld.params.infectiousToSymptomatic_mean, 
							myWorld.params.infectiousToSymptomatic_std);
					assert (time_until_symptoms >= 0.0) : "sheduled time not in future: " + time_until_symptoms;
					i.time_start_symptomatic = time + time_until_symptoms;
				}
				
				// update every timestep
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		// the agent is infectious, but will not show symptoms. They will eventually recover.
		this.asymptomaticNode = new BehaviourNode(){
			

			@Override
			public String getTitle() { return CoronavirusBehaviourNodeTitle.ASYMPTOMATIC.key; }

			@Override
			public double next(Steppable s, double time) {
				
				// although asympotmatic, the agent is still infectious
				CoronavirusInfection i = (CoronavirusInfection) s;
				if (((Person) i.getHost()).isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
//				((Person) i.getHost()).infectNeighbours();

				// determine when the agent will recover - this is
				// only a matter of time in this case
				if(time >= i.time_recovered){
					i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.RECOVERED));
				}
				else if(i.time_recovered == Double.MAX_VALUE){ // has not been set
					
					double time_until_recovered = myWorld.nextRandomLognormal(
							myWorld.params.asymptomaticToRecovery_mean, 
							myWorld.params.asymptomaticToRecovery_std);
					assert (time_until_recovered > 0) : "Time until recovered is not set to the future " + time_until_recovered;
					i.time_recovered = time + time_until_recovered;
				}
				
				// update every timestep
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		// the agent has a mild case and is infectious. They may recover, or else progress to a severe case.
		this.mildNode = new BehaviourNode(){

			@Override
			public String getTitle() { return CoronavirusBehaviourNodeTitle.MILD.key; }

			@Override
			public double next(Steppable s, double time) {
				
				// the agent is infectious
				CoronavirusInfection i = (CoronavirusInfection) s;
				Person p = (Person) i.getHost();
				if (((Person) i.getHost()).isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
//				((Person) i.getHost()).infectNeighbours();
				i.setSymptomatic();
				i.setMild();
				i.setEligibleForTesting();
				if (i.getHost().getDiseaseSet().containsKey(DISEASE.COVIDSPURIOUSSYMPTOM.key)) {
					i.getHost().getDiseaseSet().get(DISEASE.COVIDSPURIOUSSYMPTOM.key).setAsympt();
				}
				
				// if the agent is scheduled to recover, make sure that it
				// does so
				if(time >= i.time_recovered){
					i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.RECOVERED));
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(time >= i.time_start_severe){
					i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.SEVERE));
					// record this event
					p.getLocation().getRootSuperLocation().metric_new_hospitalized++;
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered == Double.MAX_VALUE && i.time_start_severe == Double.MAX_VALUE){
					// determine if they will be immobilised with mild covid
					double myImmobilisedLikelihood = myWorld.random.nextDouble();
					if (myImmobilisedLikelihood < myWorld.params.covid_prob_stay_at_home_mild) {
						p.setMobility(false);
						p.sendHome(); 
					}

					// determine if the patient will become sicker
					double mySevereLikelihood = myWorld.params.getLikelihoodByAge(
							myWorld.params.infection_p_sev_by_age, myWorld.params.infection_age_params, ((Person) i.getHost()).getAge());
					assert (mySevereLikelihood >= 0.0) & (mySevereLikelihood <= 1.0) : "probablilty not valid: " + mySevereLikelihood;
					if(myWorld.random.nextDouble() < mySevereLikelihood){
						double time_until_severe = myWorld.nextRandomLognormal(
								myWorld.params.symptomaticToSevere_mean, 
								myWorld.params.symptomaticToSevere_std);
						assert time_until_severe > 0 : "time until disease progression not scheduled in future";
						i.time_start_severe = time + time_until_severe;
					}
					
					// if not, they will recover: schedule this instead
					else {
						double time_until_recovered = myWorld.nextRandomLognormal(
								myWorld.params.symptomaticToRecovery_mean, 
								myWorld.params.symptomaticToRecovery_std);
						assert time_until_recovered > 0 : "time until recovery not scheduled in future: " + time_until_recovered;

						i.time_recovered = time + time_until_recovered;
						return 1;
					}
				}
				
				// update every timestep
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};

		// the agent has a severe case and is infectious. They may recover, or else progress to a critical case.
		this.severeNode = new BehaviourNode(){

			@Override
			public String getTitle() { return CoronavirusBehaviourNodeTitle.SEVERE.key; }

			@Override
			public double next(Steppable s, double time) {
				
				// the agent is infectious
				CoronavirusInfection i = (CoronavirusInfection) s;
				Person p = (Person) i.getHost();
				// check if they are mobile, if they are send them home sick and make them immobilised
				if (!p.isImmobilised()) {
					p.setMobility(false);
					p.sendHome(); 
				}
				if (((Person) i.getHost()).isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
//				((Person) i.getHost()).infectNeighbours();
				i.setSevere();
				// if the agent is scheduled to recover, make sure that it
				// does so
				if(time >= i.time_recovered){
					i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.RECOVERED));
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(time >= i.time_start_critical){
					i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.CRITICAL));
					i.getHost().getLocation().getRootSuperLocation().metric_new_critical++;
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered == Double.MAX_VALUE && i.time_start_critical == Double.MAX_VALUE){

					double myCriticalLikelihood = myWorld.params.getLikelihoodByAge(
							myWorld.params.infection_p_cri_by_age, myWorld.params.infection_age_params, ((Person) i.getHost()).getAge());
					assert (myCriticalLikelihood >= 0.0) & (myCriticalLikelihood <= 1.0) : "probablilty not valid " + myCriticalLikelihood;

					// determine if the patient will become sicker
					if(myWorld.random.nextDouble() < myCriticalLikelihood){
						double time_until_critical = myWorld.nextRandomLognormal(
								myWorld.params.severeToCritical_mean, 
								myWorld.params.severeToCritical_std);
						assert time_until_critical > 0.0 : "time until critical not in future";

						i.time_start_critical = time + time_until_critical;
					}
					
					// if not, they will recover: schedule this instead
					else {
						double time_until_recovered = myWorld.nextRandomLognormal(
								myWorld.params.severeToRecovery_mean, 
								myWorld.params.severeToRecovery_std);
						assert time_until_recovered > 0.0 : "time until recovered not in future " + time_until_recovered;

						i.time_recovered = time + time_until_recovered;
						return 1;
					}
				}
				
				// update every timestep
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};

		// the agent has a critical case and is infectious. They may recover, or else progress to death.
		this.criticalNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return CoronavirusBehaviourNodeTitle.CRITICAL.key; }

			@Override
			public double next(Steppable s, double time) {
				
				// the agent is infectious
				CoronavirusInfection i = (CoronavirusInfection) s;
				if (((Person) i.getHost()).isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
//				((Person) i.getHost()).infectNeighbours();
				i.setCritical();
				
				// if the agent is scheduled to recover, make sure that it
				// does so
				if(time >= i.time_recovered ){
					i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.RECOVERED));
				}
				
				// otherwise, if it is scheduled to worsen, progress it
				else if(time >= i.time_died){
					i.setBehaviourNode(setNodeForTesting(CoronavirusBehaviourNodeTitle.DEAD));
					
					Location myAdminZone = i.getHost().getLocation().getRootSuperLocation();
					myAdminZone.metric_died_count++;
					myAdminZone.metric_new_deaths++;
					return 1;
				}
				
				// finally, if the next step has not yet been decided, schedule it
				else if(i.time_recovered == Double.MAX_VALUE && i.time_died == Double.MAX_VALUE ){

					double myDeathLikelihood = myWorld.params.getLikelihoodByAge(
							myWorld.params.infection_p_dea_by_age, myWorld.params.infection_age_params, ((Person) i.getHost()).getAge());
					
					assert (myDeathLikelihood >= 0.0) & (myDeathLikelihood <= 1.0) : "probablilty not valid " + myDeathLikelihood;

					// determine if the patient will die
					if(myWorld.random.nextDouble() < myDeathLikelihood){
						double time_until_death = myWorld.nextRandomLognormal(
								myWorld.params.criticalToDeath_mean, 
								myWorld.params.criticalToDeath_std);
						assert time_until_death > 0.0 : "time until died not in future " + time_until_death;

						i.time_died = time + time_until_death;
					}
					
					// if not, they will recover: schedule this instead
					else {
						double time_until_recovered = myWorld.nextRandomLognormal(
								myWorld.params.criticalToRecovery_mean, 
								myWorld.params.criticalToRecovery_std);
						assert time_until_recovered > 0.0 : "time until recovered not in future " + time_until_recovered;

						i.time_recovered = time + time_until_recovered;
						return 1;
					}
				}
				
				// update every timestep
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		// the agent has recovered.
		this.recoveredNode = new BehaviourNode(){

			@Override
			public String getTitle() { return CoronavirusBehaviourNodeTitle.RECOVERED.key; }

			@Override
			public double next(Steppable s, double time) {

				CoronavirusInfection i = (CoronavirusInfection) s;
				if (((Person) i.getHost()).isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
				i.time_recovered = time;
				// update person's properties
				i.getHost().getLocation().getRootSuperLocation().metric_new_recovered++;				
				i.setEligibleForTesting();
				// the Person may have stopped moving when ill - reactivate!
				if(((Person) i.getHost()).isImmobilised()){
					// remobilise this person if they aren't being held at home by occupational constraint
					// first check if there is any constraint to this persons movement by checking their econ status
					if (!myWorld.params.OccupationConstraintList.containsKey(((Person) i.getHost()).getEconStatus())){
						((Person) i.getHost()).setMobility(true);
						myWorld.schedule.scheduleOnce(i.getHost()); // schedule the agent to begin moving again!	
					}
					else {
						// their occupation has some constraint, check that their constraint is set to home or not, if it's set to home nothing changes, otherwise set them moving again
						if (!myWorld.params.OccupationConstraintList.get(((Person) i.getHost()).getEconStatus()).equals(LocationCategory.HOME)) {
							((Person) i.getHost()).setMobility(true);
							myWorld.schedule.scheduleOnce(i.getHost()); // schedule the agent to begin moving again!	
						}
					}
				}
				// if they have had symptomatic covid, make them no longer have symptoms of covid
				if (i.isSymptomatic()) i.setSymptomatic();
				i.setRecovered();
				// no need to update again!
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		this.deadNode = new BehaviourNode(){

			@Override
			public String getTitle() { return CoronavirusBehaviourNodeTitle.DEAD.key; }

			@Override
			public double next(Steppable s, double time) {
				CoronavirusInfection i = (CoronavirusInfection) s;
				// remove covid from person object
				((Person) i.getHost()).die("COVID-19");
				i.setAsCauseOfDeath();
				i.time_died = time;
								
				return Double.MAX_VALUE; // no need to run ever again
			}

			@Override
			public boolean isEndpoint() {
				return true;
			}
			
		};
		

		
	}
	
	public BehaviourNode setNodeForTesting(CoronavirusBehaviourNodeTitle behaviour) {
		BehaviourNode toreturn;

		switch (behaviour) {
		case SUSCEPTIBLE:{
			toreturn = this.susceptibleNode;
			break;
		}
		case EXPOSED:{
			toreturn = this.exposedNode;
			break;
		}
		case PRESYMPTOMATIC:{
			toreturn = this.presymptomaticNode;
			break;
		}
		case ASYMPTOMATIC:{
			toreturn = this.asymptomaticNode;
			break;
		}
		case MILD:{
			toreturn = this.mildNode;
			break;
		}
		case SEVERE:{
			toreturn = this.severeNode;
			break;
		}
		case CRITICAL:{
			toreturn = this.criticalNode;
			break;
		}
		case RECOVERED:{
			toreturn = this.recoveredNode;
			break;
		}
		case DEAD:{
			toreturn = this.deadNode;
			break;
		}
		default:
			toreturn = this.susceptibleNode;
			break;
		}
			
		return toreturn;
	}
	
	public BehaviourNode getStandardEntryPoint(){ return this.exposedNode; }
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
