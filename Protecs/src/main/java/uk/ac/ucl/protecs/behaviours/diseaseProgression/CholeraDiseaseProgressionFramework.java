package uk.ac.ucl.protecs.behaviours.diseaseProgression;


import uk.ac.ucl.protecs.objects.diseases.Cholera;
import uk.ac.ucl.protecs.sim.*;
import sim.engine.Steppable;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class CholeraDiseaseProgressionFramework extends DiseaseProgressionBehaviourFramework {
	
	public enum CholeraBehaviourNodeInHumans{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"), PRESYMPTOMATIC("presymptomatic"), ASYMPTOMATIC("asymptomatic"), 
		MILD("mild"), SEVERE("severe"), CRITICAL("critical"), RECOVERED("recovered"), DEAD("dead");
        String key;
     
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
		CLEAN("clean"), CONTAMINATED("contaminated");

        String key;
     
        CholeraBehaviourNodeInWater(String key) { this.key = key; }
    
        static CholeraBehaviourNodeInWater getValue(String x) {
        	switch (x) {
        	case "clean":
        		return CLEAN;
        	case "contaminated":
        		return CONTAMINATED;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}
	
	public enum nextStepCholera{
		DO_NOTHING("doNothing"), ASYMPTOMATIC("asymptomatic"), MILD("mild"), RECOVER("recover"), HAS_DIED("hasDied");
		public String key;
	     
		nextStepCholera(String key) { this.key = key; }
		
		public static nextStepCholera getValue(String x) {
        	switch (x) {
        	case "doNothing":
        		return DO_NOTHING;
        	case "asymptomatic":
        		return ASYMPTOMATIC;
        	case "symptomatic":
        		return MILD;
        	case "recover":
        		return RECOVER;	
        	case "hasDied":
        		return HAS_DIED;
        	default:
        		throw new IllegalArgumentException();
        	}
		
		}
	} 
	private nextStepCholera nextStep;

	@SuppressWarnings("serial")
	public CholeraDiseaseProgressionFramework(WorldBankCovid19Sim world) {
		super(world);
		// TODO Auto-generated constructor stub
		
		this.susceptibleNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return CholeraBehaviourNodeInHumans.SUSCEPTIBLE.key; }

			@Override
			public double next(Steppable s, double time) {
				// Initially assume no reinfection, so when a person reaches this state, don't do anything
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
	
	this.exposedNode = new BehaviourNode() {
		// TODO: 1) Link the likelihood of exposure developing into an infection or not to the concentration of Cholera bacteria in the water source?
		// 2) Time stamp the next steps in the disease development beyond exposure and infectious
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
			nextStep = nextStepCholera.DO_NOTHING;
			
			// ------------------------------------ DECIDE SYMPT OR ASYMPT CODE BLOCK ------------------------------------------------------------------------------
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
					nextStep = nextStepCholera.ASYMPTOMATIC;
				}
				else {
					nextStep = nextStepCholera.MILD;
				}
			
			}
			// ------------------------------------------------------------------------------------------------------------------------------------------------------------
			// ================================== DECIDE SUCCESSFULL INFECTION OR NOT WITH SCHEDULING CODE BLOCK ==========================================================
			else {
				// Temporarily have a made up whether sufficient number of bacteria was ingested
				double randToRepresentSufficientIngestion = myWorld.random.nextDouble();

				if (randToRepresentSufficientIngestion < 0.95) {
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
					nextStep = nextStepCholera.RECOVER;
				}
			}
			// ===============================================================================================================================================================
			
			
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= MAKE NEXT STEP HAPPEN -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
			switch (nextStep) {
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
				case RECOVER:{
					// This infection did not take, so return to susceptible
					choleraInfection.setBehaviourNode(susceptibleNode);
					choleraInfection.time_recovered = time;
					// For now assume no reinfection
					return Double.MAX_VALUE;
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
			// Asymptomatic people will shed bacteria for about a day (https://pmc.ncbi.nlm.nih.gov/articles/PMC2554681/)
			// Assume this person is asymptomatic and shedding
			nextStep = nextStepCholera.DO_NOTHING;
			// -------------------------- ACT ON SCHEDULED RECOVERY CODE BLOCK ------------------------------------------------------------------------------ 
			if (choleraInfection.time_recovered < Double.MAX_VALUE) {
				// don't trigger recovery too early
				if(time < choleraInfection.time_recovered)
					return choleraInfection.time_recovered - time;
				else {
					nextStep = nextStepCholera.RECOVER;
				}
			}
			// -----------------------------------------------------------------------------------------------------------------------------------------------
			// ================================= SCHEDULE RECOVERY CODE BLOCK ================================================================================
			
			else {
				// If I find any detail to shedding time in asymptomatic infections I will improve
				choleraInfection.time_recovered = time + 1 * Params.ticks_per_day;
			}
			// ================================================================================================================================================
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= MAKE NEXT STEP HAPPEN -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
			switch (nextStep) {
			case DO_NOTHING:{
				// Tick time over until next action
				return 1;
				}
			case RECOVER:{
				// This infection did not take, so return to susceptible
				choleraInfection.setBehaviourNode(susceptibleNode);
				choleraInfection.time_recovered = time;
				// For now assume no reinfection
				return Double.MAX_VALUE;
			}
			default:
				return 1;
			}
			// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
			}
		
	};
	
	this.recoveredNode = new BehaviourNode() {

		@Override
		public String getTitle() {

			return CholeraBehaviourNodeInHumans.RECOVERED.key;
		}

		@Override
		public double next(Steppable s, double time) {
			Cholera d = (Cholera) s;

			return myWorld.params.ticks_per_week;
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}};
	
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
	
	this.contaminatedNode = new BehaviourNode() {

		@Override
		public String getTitle() {
			// TODO Auto-generated method stub
			return CholeraBehaviourNodeInWater.CONTAMINATED.key;
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public double next(Steppable arg0, double arg1) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	};
}
	public BehaviourNode getStandardEntryPoint(){ return this.exposedNode; }
	
	public BehaviourNode getStandardEntryPointForWater(){ return this.contaminatedNode; }


}
