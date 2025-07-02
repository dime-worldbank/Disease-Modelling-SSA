package uk.ac.ucl.protecs.behaviours.diseaseProgression;


import uk.ac.ucl.protecs.objects.diseases.Cholera;
import uk.ac.ucl.protecs.sim.*;
import sim.engine.Steppable;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class CholeraDiseaseProgressionFramework extends DiseaseProgressionBehaviourFramework {
	
	// set up custom behaviour nodes for choler in water
	BehaviourNode cleanNode;
	
	BehaviourNode acitveButNonCulturableNode;
	
	BehaviourNode hyperinfectiousNode;


	
	public enum CholeraBehaviourNodeInHumans{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"), PRESYMPTOMATIC("presymptomatic"), ASYMPTOMATIC("asymptomatic"), 
		MILD("mild"), SEVERE("severe"), CRITICAL("critical"), RECOVERED("recovered"), DEAD("dead");
        public String key;
     
        CholeraBehaviourNodeInHumans(String key) { this.key = key; }
    
        static CholeraBehaviourNodeInHumans getValue(String x) {
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
	
	public enum CholeraBehaviourNodeInWater{
		// Cholera in water is only infectious in the time period immediately after initial exposure, it rapidly deteriorates from a hyperinfectious state into an 
		// 'active but non-culturable' (ABNC) state within 5 hours (https://pubmed.ncbi.nlm.nih.gov/12050664/)
		CLEAN("clean"), ABNC("ABMC"), HYPERINFECTIOUS("hyperinfectious");

        public String key;
     
        CholeraBehaviourNodeInWater(String key) { this.key = key; }
    
        static CholeraBehaviourNodeInWater getValue(String x) {
        	switch (x) {
        	case "clean":
        		return CLEAN;
        	case "ABNC":
        		return ABNC;
        	case "hyperinfectious":
        		return HYPERINFECTIOUS;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}
	
	public enum nextStepCholeraInHumans{
		DO_NOTHING("doNothing"), ASYMPTOMATIC("asymptomatic"), MILD("mild"), SEVERE("severe"), TREATMENT("treatment"), CRITICAL("critical"),
		RECOVER("recover"), HAS_DIED("hasDied"), SUSCEPTIBLE("susceptible");
		public String key;
	     
		nextStepCholeraInHumans(String key) { this.key = key; }
		
		public static nextStepCholeraInHumans getValue(String x) {
        	switch (x) {
        	case "doNothing":
        		return DO_NOTHING;
        	case "asymptomatic":
        		return ASYMPTOMATIC;
        	case "mild":
        		return MILD;
        	case "severe":
        		return SEVERE;
        	case "treatment":
        		return TREATMENT;
        	case "critical":
        		return CRITICAL;
        	case "recover":
        		return RECOVER;	
        	case "hasDied":
        		return HAS_DIED;
        	case "susceptible":
        		return SUSCEPTIBLE;
        	default:
        		throw new IllegalArgumentException();
        	}
		
		}
	} 
	private nextStepCholeraInHumans nextStepInHumans;
	
	public enum nextStepCholeraInWater{
		DO_NOTHING("doNothing"), HYPERINFECTIOUS("hyperinfectious"), ABNC("abnc"), CLEAN("clean");
		public String key;
	     
		nextStepCholeraInWater(String key) { this.key = key; }
		
		public static nextStepCholeraInWater getValue(String x) {
        	switch (x) {
        	case "doNothing":
        		return DO_NOTHING;
        	case "hyperinfectious":
        		return HYPERINFECTIOUS;
        	case "abnc":
        		return ABNC;
        	case "clean":
        		return CLEAN;
        	default:
        		throw new IllegalArgumentException();
        	}
		
		}
	} 
	private nextStepCholeraInWater nextStepInWater;

	@SuppressWarnings("serial")
	public CholeraDiseaseProgressionFramework(WorldBankCovid19Sim world) {
		super(world);
		// TODO Auto-generated constructor stub
		
		this.susceptibleNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return CholeraBehaviourNodeInHumans.SUSCEPTIBLE.key; }

			@Override
			public double next(Steppable s, double time) {
				Cholera choleraInfection = (Cholera) s;
				// Any infection here has recovered, we need to reset some of the infections properties
				if (choleraInfection.time_infected < Double.MAX_VALUE) {
					choleraInfection.resetPropertiesPostRecovery();
				}
				// handle any time dependent protection here, first see if they had a prior asymptomatic infection
				if (choleraInfection.time_protection_from_asymptomatic_ends < Double.MAX_VALUE) {
					// If they have a time scheduled, tick over using the below return
					if(time > choleraInfection.time_protection_from_asymptomatic_ends) {
						// if we are passed the time where they have protections from an asymptomatic infection, reset this value
						choleraInfection.time_protection_from_asymptomatic_ends = Double.MAX_VALUE;
						choleraInfection.setHadAsymptCholera(false);
						}
					return 1;
				}
				if (choleraInfection.time_protection_from_symptomatic_ends < Double.MAX_VALUE) {
					// If they have a time scheduled, tick over using the below return
					if(time > choleraInfection.time_protection_from_symptomatic_ends) {
						// if we are passed the time where they have protections from a symptomatic infection, reset this value
						choleraInfection.time_protection_from_symptomatic_ends = Double.MAX_VALUE;
						choleraInfection.setHadSymptCholera(false);
						}
					return 1;
				}
				// Allow for reinfection, keep this ticking over
				return 1;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
	
	this.exposedNode = new BehaviourNode() {
		// TODO: 1) Link the likelihood of exposure developing into an infection or not to the concentration of Cholera bacteria in the water source?
		// 2) Look into other factors influencing the likelihood of developing cholera
		@Override
		public String getTitle() {
			// TODO Auto-generated method stub
			return CholeraBehaviourNodeInHumans.EXPOSED.key;
		}

		@Override
		public double next(Steppable s, double time) {
			// person is newly exposed to cholera, need to determine if they will go on to develop symptoms of Cholera, 
			// will become asymptomatic or if the infection will not take.
			
			// There is a known quantity of Cholera required to cause Cholera in an adult (100 million Cholera bacteria specifically) https://www.thelancet.com/journals/lancet/article/PIIS0140-6736(03)15328-7/abstract
			// It would be good to include this in the epi model if possible, we'll probably be able to find the amount shed as well.
			Cholera choleraInfection = (Cholera) s;

			// default to doing nothing and the infection not taking for now.
			nextStepInHumans = nextStepCholeraInHumans.DO_NOTHING;
			
			// ------------------------------------ DECIDE ASYMPT, MILD OR SEVERE CODE BLOCK ------------------------------------------------------------------------------
			// If we have scheduled action for this cholera infection i.e. progression or determined recovery check if it's time for that to happen
			if (choleraInfection.time_contagious < Double.MAX_VALUE) {
				
				// maybe we have been triggered too soon - in that case, don't activate again until it is time!
				if(time < choleraInfection.time_contagious)
					return choleraInfection.time_contagious - time;
				
				// now the time to be contagious has arrived, determine what type of infection this instance of cholera will become
				double randToDecideAsymptOrSympt = myWorld.random.nextDouble();
				// The ratio of asymptomatic to symptomatic cases has been estimated to be 1 : 3 to 1 : 100 (https://ui.adsabs.harvard.edu/abs/2008Natur.454..877K/abstract)
				// for now assume that 3 out of 4 Cholera cases are asymptomatic
				if (randToDecideAsymptOrSympt <= myWorld.params.cholera_prob_asymptomatic) {
					nextStepInHumans = nextStepCholeraInHumans.ASYMPTOMATIC;
				}
				else {
					// Here we need to develop a means to assign the severity of the initial infection depending on the amount of bacteria this person 
					// ingests, if a high dose is ingested (10^8 bacteria) then this will cause a severe infection in an adult. For now just use a probability 
					// to simulate
					
					double rand_for_ingesting_large_dose = myWorld.random.nextDouble();
					// assume that 3% of exposed infections that take will result in a severe infection
					if (rand_for_ingesting_large_dose < myWorld.params.cholera_prob_severe) { // needs to be calibrated
						nextStepInHumans = nextStepCholeraInHumans.SEVERE;
					}
					else {
						nextStepInHumans = nextStepCholeraInHumans.MILD;
					}
				}
			
			}
			// ------------------------------------------------------------------------------------------------------------------------------------------------------------
			// ================================== DECIDE SUCCESSFULL INFECTION OR NOT WITH SCHEDULING CODE BLOCK ==========================================================
			else {
				// Temporarily have a made up whether sufficient number of bacteria was ingested for any infection to take hold
				double randToRepresentSufficientIngestion = myWorld.random.nextDouble();
				// Need to account for any prior infections and determine if they were asymptomatic or symptomatic
				boolean prior_asymptomatic_infection = choleraInfection.getHadAsymptCholera();
				boolean prior_symptomatic_infection = choleraInfection.getHadSymptCholera();
				
				if (prior_asymptomatic_infection) {
					// if they had a previous asymptomatic infection and this is still offering protection, apply a reductive factor
					randToRepresentSufficientIngestion *= myWorld.params.cholera_prior_asympt_protection_factor;
				}
				else if (prior_symptomatic_infection) {
					// if they had a previous symptomatic infection and this is still offering protection, apply a reductive factor
					randToRepresentSufficientIngestion *= myWorld.params.cholera_prior_sympt_protection_factor;
				}

				if (randToRepresentSufficientIngestion < myWorld.params.cholera_sufficient_ingestion) {
					// Note that this is an established Cholera infection
					choleraInfection.time_infected = time;
					// Schedule a time in the future where this person will start shedding the Cholera infection, mean time is 1.4 days (https://www.sciencedirect.com/science/article/pii/S0163445312003477)
					// Standard deviation is 0.0917.
					choleraInfection.time_contagious = time + myWorld.nextRandomLognormal(
							myWorld.params.cholera_exposed_to_infectious_mean,
							myWorld.params.cholera_exposed_to_infectious_std);
					// scheduled the next step to occur, do nothing until then
				}
				else {
					nextStepInHumans = nextStepCholeraInHumans.SUSCEPTIBLE;
				}
			}
			// ===============================================================================================================================================================
			
			
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= MAKE NEXT STEP HAPPEN -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
			switch (nextStepInHumans) {
				case DO_NOTHING:{
					// Tick time over until next action
					return 1;
					}
				case ASYMPTOMATIC:{
					choleraInfection.setBehaviourNode(asymptomaticNode);
					return 1;
				}
				case MILD:{
					choleraInfection.setBehaviourNode(mildNode);
					return 1;
				}
				case SEVERE:{
					choleraInfection.setBehaviourNode(severeNode);
					return 1;
				}
				case SUSCEPTIBLE:{
					// This infection did not take, so return to susceptible
					choleraInfection.setBehaviourNode(susceptibleNode);
					return 1;
				}
				default:
					return 1;
			}
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
		
	};
	
	this.asymptomaticNode = new BehaviourNode() {
		// TODO: improve stochasticity in shedding time if possible
		@Override
		public String getTitle() {

			return CholeraBehaviourNodeInHumans.ASYMPTOMATIC.key;
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

		@Override
		public double next(Steppable s, double time) {
			Cholera choleraInfection = (Cholera) s;
			choleraInfection.setHadAsymptCholera(true);
			// track for logging
			choleraInfection.setAsympt();
			// Asymptomatic people will shed bacteria for about a day (https://pmc.ncbi.nlm.nih.gov/articles/PMC2554681/)
			// Assume this person is asymptomatic and shedding
			nextStepInHumans = nextStepCholeraInHumans.DO_NOTHING;
			// -------------------------- ACT ON SCHEDULED RECOVERY CODE BLOCK ------------------------------------------------------------------------------ 
			if (choleraInfection.time_recovered < Double.MAX_VALUE) {
				// don't trigger recovery too early
				if(time < choleraInfection.time_recovered)
					return choleraInfection.time_recovered - time;
				else {
					nextStepInHumans = nextStepCholeraInHumans.RECOVER;
				}
			}
			// -----------------------------------------------------------------------------------------------------------------------------------------------
			// ================================= SCHEDULE RECOVERY CODE BLOCK ================================================================================
			
			else {
				// If I find any detail to shedding time in asymptomatic infections I will improve
				choleraInfection.time_recovered = time + myWorld.params.cholera_mean_time_recovery_asympt * myWorld.params.ticks_per_day;
			}
			// ================================================================================================================================================
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= MAKE NEXT STEP HAPPEN -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
			switch (nextStepInHumans) {
			case DO_NOTHING:{
				// Tick time over until next action
				return 1;
				}
			case RECOVER:{
				// This infection did not take, so return to susceptible
				choleraInfection.setBehaviourNode(recoveredNode);
				return 1;
			}
			default:
				return 1;
			}
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
			}
		
	};
	this.mildNode = new BehaviourNode() {

		@Override
		public String getTitle() {
			return CholeraBehaviourNodeInHumans.MILD.key;
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public double next(Steppable s, double time) {
			Cholera choleraInfection = (Cholera) s;
			choleraInfection.setHadSymptCholera(true);
			choleraInfection.setMild();

			// mild cases will recover in around 4-5 days (https://www.thelancet.com/journals/lancet/article/PIIS0140-6736(03)15328-7/fulltext)
			// Assume this person is mildly infected and shedding
			nextStepInHumans = nextStepCholeraInHumans.DO_NOTHING;
			// -------------------------- ACT ON SCHEDULED RECOVERY CODE BLOCK ------------------------------------------------------------------------------ 
			if (choleraInfection.time_recovered < Double.MAX_VALUE) {
				// don't trigger recovery too early
				if(time < choleraInfection.time_recovered)
					return choleraInfection.time_recovered - time;
				else {
					nextStepInHumans = nextStepCholeraInHumans.RECOVER;
				}
			}
			// -----------------------------------------------------------------------------------------------------------------------------------------------
			// ================================= SCHEDULE RECOVERY CODE BLOCK ================================================================================
						
			else {
				// If I find any detail to shedding time in mild infections I will improve
				choleraInfection.time_recovered = time + myWorld.params.cholera_mean_time_recovery_mild * myWorld.params.ticks_per_day; // with treatment this will go down to 2-3 days
			}
			// ================================================================================================================================================
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= MAKE NEXT STEP HAPPEN -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
			switch (nextStepInHumans) {
				case DO_NOTHING:{
					// Tick time over until next action
					return 1;
					}
				case RECOVER:{
					// This infection did not take, so return to recovered
					choleraInfection.setBehaviourNode(recoveredNode);
					return 1;
					}
				default:
					return 1;
			}
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		}
	};
	
	this.severeNode = new BehaviourNode() {
		// TODO: Make the distinction between seeking treatment and not seeking treatment based off something
		@Override
		public String getTitle() {
			return CholeraBehaviourNodeInHumans.SEVERE.key;
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public double next(Steppable s, double time) {
			Cholera choleraInfection = (Cholera) s;
			choleraInfection.setHadSymptCholera(true);
			choleraInfection.setSevere();
			nextStepInHumans = nextStepCholeraInHumans.DO_NOTHING;
			
			// -------------------------- ACT ON SCHEDULED NEXT STEP CODE BLOCK ------------------------------------------------------------------------------ 
			if (choleraInfection.time_recovered < Double.MAX_VALUE) {
				// don't trigger recovery too early
				if(time < choleraInfection.time_recovered)
					return choleraInfection.time_recovered - time;
				else {
					nextStepInHumans = nextStepCholeraInHumans.RECOVER;
				}
			}
			else if (choleraInfection.time_died < Double.MAX_VALUE) {
				// don't trigger death too early
				if(time < choleraInfection.time_died)
					return choleraInfection.time_died - time;
				else {
					nextStepInHumans = nextStepCholeraInHumans.HAS_DIED;
				}
			}
			else if (choleraInfection.time_start_critical < Double.MAX_VALUE) {
				// don't trigger critical too early
				if(time < choleraInfection.time_start_critical)
					return choleraInfection.time_start_critical - time;
				else {
					nextStepInHumans = nextStepCholeraInHumans.CRITICAL;
				}
			}
			// ========================================== DECIDE NEXT STEP CODE BLOCK ========================================================================
			else {
				// Assume this person is severely infected and shedding
				// Does this person seek treatment? Use a flat probability for now
				double rand_for_seeking_treatment = myWorld.random.nextDouble();
				// Assume that 80% of people seek treatment
				if (rand_for_seeking_treatment < myWorld.params.cholera_prob_seek_treatment) {
					nextStepInHumans = nextStepCholeraInHumans.TREATMENT;
					// even with treatment a small percentage of people will still die
					double rand_for_determining_mortality = myWorld.random.nextDouble();
					if (rand_for_determining_mortality < myWorld.params.cholera_prob_mortality_with_treatment) { // 1% CFR for those in treatment, assume mortality occurs within a day
						choleraInfection.time_died = time + myWorld.params.cholera_mean_time_death_with_treatment *  myWorld.params.ticks_per_day;
					}
					// schedule a recovery time
					else {
						choleraInfection.time_recovered = time + myWorld.params.cholera_mean_time_recovery_severe * myWorld.params.ticks_per_day; // made up
					}
					
				}
				else {
					// schedule the progression to critical Cholera
					choleraInfection.time_start_critical = time; 
					// Cholera progression can be rapid, if people have severe cholera they need to get help 
					// fast so assume that progression to the next stage happens almost immediately
				}
			}
						
			
			// ================================================================================================================================================
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= MAKE NEXT STEP HAPPEN -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
			switch (nextStepInHumans) {
				case DO_NOTHING:{
					// Tick time over until next action
					return 1;
					}
				case TREATMENT:{
					// Tick time over until next action
					return 1;
				}
				case CRITICAL:{
					choleraInfection.setBehaviourNode(criticalNode);
					return 1;
				}
				case HAS_DIED:{
					choleraInfection.setBehaviourNode(deadNode);
					return 1;
				}
				case RECOVER:{
					choleraInfection.setBehaviourNode(recoveredNode);
					return 1;
					}
				default:
					return 1;
			}
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		}
	};
	
	this.criticalNode = new BehaviourNode() {

		@Override
		public String getTitle() {
			return CholeraBehaviourNodeInHumans.CRITICAL.key;
		}

		@Override
		public boolean isEndpoint() {

			return false;
		}

		@Override
		public double next(Steppable s, double time) {
			Cholera choleraInfection = (Cholera) s;
			choleraInfection.setCritical();
			// -------------------------- ACT ON SCHEDULED NEXT STEP CODE BLOCK ------------------------------------------------------------------------------ 
			if (choleraInfection.time_recovered < Double.MAX_VALUE) {
				// don't trigger recovery too early
				if(time < choleraInfection.time_recovered)
					return choleraInfection.time_recovered - time;
				else {
					nextStepInHumans = nextStepCholeraInHumans.RECOVER;
				}
			}
			else if (choleraInfection.time_died < Double.MAX_VALUE) {
				// don't trigger death too early
				if(time < choleraInfection.time_died)
					return choleraInfection.time_died - time;
				else {
					nextStepInHumans = nextStepCholeraInHumans.HAS_DIED;
					}
			}
			// ========================================== DECIDE NEXT STEP CODE BLOCK ========================================================================
			else {
				// Assume this person is critically infected and shedding
				nextStepInHumans = nextStepCholeraInHumans.DO_NOTHING;

				// without treatment, a large portion of those infected will die
				double rand_for_determining_mortality = myWorld.random.nextDouble();
				if (rand_for_determining_mortality <= myWorld.params.cholera_prob_mortality_without_treatment) { // 50% CFR for those untreated
						choleraInfection.time_died = time + myWorld.params.cholera_mean_time_death_without_treatment * myWorld.params.ticks_per_day;
					}
					// schedule a recovery time
					else {
					choleraInfection.time_recovered = time + myWorld.params.cholera_mean_time_recovery_critical * myWorld.params.ticks_per_day; // made up
					}
					
			}

			// ================================================================================================================================================
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= MAKE NEXT STEP HAPPEN -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
			switch (nextStepInHumans) {
				case DO_NOTHING:{
					// Tick time over until next action
					return 1;
					}
				case HAS_DIED:{
					choleraInfection.setBehaviourNode(deadNode);
					return 1;
				}
				case RECOVER:{
					choleraInfection.setBehaviourNode(recoveredNode);
					return 1;
					}
				default:
					return 1;
			}
		}
	};
	
	this.recoveredNode = new BehaviourNode() {

		@Override
		public String getTitle() {

			return CholeraBehaviourNodeInHumans.RECOVERED.key;
		}

		@Override
		public double next(Steppable s, double time) {
			Cholera choleraInfection = (Cholera) s;
			choleraInfection.setRecovered();
			// Let's assume that we give people who had cholera a month of absolute immunity, with partial immunity being scheduled back in the 
			// susceptible step if applicable.
			nextStepInHumans = nextStepCholeraInHumans.DO_NOTHING;
			
			// check if the next step has been scheduled
			if (choleraInfection.time_susceptible < Double.MAX_VALUE) {
				// tick over until it is time to act
				if(time < choleraInfection.time_susceptible)
					return choleraInfection.time_susceptible - time;
				else {
					// it's time to act, make the next step be susceptible
					nextStepInHumans = nextStepCholeraInHumans.SUSCEPTIBLE;
					// We've given those who have recovered a months grace period from infection, now apply a date for partial protection from subsequent infections based on
					// if this infection was symptomatic or not
					if (choleraInfection.getHadSymptCholera()) {
						// protection from cholera for the next three years based on persistence studies (https://pmc.ncbi.nlm.nih.gov/articles/PMC8136710/pdf/pntd.0009383.pdf)
						choleraInfection.time_protection_from_symptomatic_ends = time + myWorld.params.cholera_recovered_from_sympt_partial_protection_years * myWorld.params.ticks_per_year;
					}
					if (choleraInfection.getHadAsymptCholera()) {
						// evidence to suggest that reinfection from asymptomatic infection can occur from as little as three months from initial infection
						choleraInfection.time_protection_from_asymptomatic_ends = time + myWorld.params.cholera_recovered_from_asympt_partial_protection_months * myWorld.params.ticks_per_month;
					}
				}
			}
			else {
				choleraInfection.time_susceptible = time + myWorld.params.cholera_natural_immunity_days_post_infection * myWorld.params.ticks_per_day; 
				return 1;
				
			}
			switch (nextStepInHumans) {
			case DO_NOTHING:{
				// Tick time over until next action
				return 1;
				}
			case SUSCEPTIBLE:{
				choleraInfection.setBehaviourNode(susceptibleNode);
				return 1;
			}
			default:
				return 1;
		}
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
	};
	
	this.deadNode = new BehaviourNode() {

		@Override
		public String getTitle() {
			// TODO Auto-generated method stub
			return CholeraBehaviourNodeInHumans.DEAD.key;
		}

		@Override
		public double next(Steppable s, double time) {
			Cholera d = (Cholera) s;
			// Store time of death
			d.time_died = time;
			return Double.MAX_VALUE;
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
		
	};
	
	this.cleanNode = new BehaviourNode() {

		@Override
		public String getTitle() {
			return CholeraBehaviourNodeInWater.CLEAN.key;
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

		@Override
		public double next(Steppable s, double time) {
			// No cholera is present in this water source so don't do anything until reactivated
			return Double.MAX_VALUE;
		}
		
	};

	this.hyperinfectiousNode = new BehaviourNode() {
		// has consequences on the transmission of disease: https://journals.plos.org/plosmedicine/article?id=10.1371/journal.pmed.0030007
		@Override
		public String getTitle() {
			return CholeraBehaviourNodeInWater.HYPERINFECTIOUS.key;
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public double next(Steppable s, double time) {
			Cholera choleraInfection = (Cholera) s;
			nextStepInWater = nextStepInWater.DO_NOTHING;
			// check if progression into ABNC has been triggered, don't trigger transition too early
			if (choleraInfection.time_abnc_in_water < Double.MAX_VALUE) {
				if (choleraInfection.time_abnc_in_water < time) {
					nextStepInWater = nextStepInWater.ABNC;
				}
			}
			else {
				// hyperinfectious state is very short, around 5 hours, 1 tick is 4 hours therefore 5/4 ticks is 5 hours (https://pubmed.ncbi.nlm.nih.gov/12050664/)
				choleraInfection.time_abnc_in_water = choleraInfection.time_hyperinfectious_in_water + myWorld.params.cholera_time_hyperinfectious_in_water;
			}
			switch (nextStepInWater) {
			case DO_NOTHING:{
				// Tick time over until next action
				return 1;
				}
			case ABNC:{
				choleraInfection.setBehaviourNode(acitveButNonCulturableNode);
				return 1;
			}
			default:
				return 1;
		}
		}
		
	};
	
	this.acitveButNonCulturableNode = new BehaviourNode() {
		@Override
		public String getTitle() {
			return CholeraBehaviourNodeInWater.ABNC.key;
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

		@Override
		public double next(Steppable s, double time) {
			Cholera choleraInfection = (Cholera) s;
			nextStepInWater = nextStepInWater.DO_NOTHING;
			if (choleraInfection.time_clean_in_water < Double.MAX_VALUE) {
				if (choleraInfection.time_clean_in_water < time) {
				nextStepInWater = nextStepInWater.CLEAN;
				}
			}
			else {
				// Little information on how long cholera can remain in non-oceanic water... assume this is a month
				choleraInfection.time_clean_in_water = choleraInfection.time_abnc_in_water +  myWorld.params.cholera_time_abnc_in_water;
			}
			switch (nextStepInWater) {
			case DO_NOTHING:{
				// Tick time over until next action
				return 1;
				}
			case CLEAN:{
				choleraInfection.setBehaviourNode(cleanNode);
				return 1;
			}
			default:
				return 1;
		}
		}
	};
}
	public BehaviourNode getStandardEntryPoint(){ return this.exposedNode; }
	
	public BehaviourNode getStandardEntryPointForWater(){ return this.hyperinfectiousNode; }

	public BehaviourNode setNodeForTesting(CholeraBehaviourNodeInHumans behaviour) {
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

}
