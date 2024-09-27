package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.Steppable;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import swise.behaviours.BehaviourFramework;
import swise.behaviours.BehaviourNode;

public class SpuriousSymptomBehaviourFramework extends BehaviourFramework{

	
	WorldBankCovid19Sim myWorld;
	BehaviourNode susceptibleNode = null, exposedNode = null, deadNode = null;
	
	public enum SpuriousSymptomNodeTitle{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"),  DEAD("dead");

        String key;
     
        SpuriousSymptomNodeTitle(String key) { this.key = key; }
    
        static SpuriousSymptomNodeTitle getValue(String x) {
        	switch (x) {
        	case "susceptible":
        		return SUSCEPTIBLE;
        	case "exposed":
        		return EXPOSED;
        	case "dead":
        		return DEAD;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}
	public enum nextStepSpurious{
		NO_SYMPTOMS("noSymptoms"), CAUSE_SYMPTOMS("causeSymptoms"), HAS_DIED("hasDied");
		public String key;
	     
		nextStepSpurious(String key) { this.key = key; }
		
		public static nextStepSpurious getValue(String x) {
        	switch (x) {
        	case "noSymptoms":
        		return NO_SYMPTOMS;
        	case "causeSymptoms":
        		return CAUSE_SYMPTOMS;
        	case "hasDied":
        		return HAS_DIED;
        	default:
        		throw new IllegalArgumentException();
        	}
		
		}
	} 
	private nextStepSpurious nextStep;

	// PARAMS to control development of disease
	
	public SpuriousSymptomBehaviourFramework(WorldBankCovid19Sim model){
		myWorld = model;
		
		// the default status
		susceptibleNode = new BehaviourNode(){

			@Override
			public String getTitle() { return SpuriousSymptomNodeTitle.SUSCEPTIBLE.key; }

			@Override
			public double next(Steppable s, double time) {
				// regulate the flare up of symptoms here
				CoronavirusSpuriousSymptom symptom = (CoronavirusSpuriousSymptom) s;
				symptom.getHost().removeCovidSpuriousSymptoms();
				// default next step of progression is no symptoms, check if they will develop symptoms this week
				nextStep = nextStepSpurious.NO_SYMPTOMS;
				if (myWorld.random.nextDouble() <= myWorld.params.rate_of_spurious_symptoms) {
					nextStep = nextStepSpurious.CAUSE_SYMPTOMS;
				}
				// need to check that those who died don't do anything, do this here
				if (!symptom.host.isAlive()) {
					nextStep = nextStepSpurious.HAS_DIED;
					}
				if (symptom.host.hasSymptomaticCovid()) {
					nextStep = nextStepSpurious.NO_SYMPTOMS;
				}
				// based on the next step string variable, choose the next thing to do for this person's spurious symptoms.
				switch (nextStep) {
				case CAUSE_SYMPTOMS:{
					symptom.setBehaviourNode(exposedNode);
					return myWorld.params.ticks_per_week;
				}
				case HAS_DIED:{
					symptom.setBehaviourNode(deadNode);
					return Double.MAX_VALUE;
				}
				case NO_SYMPTOMS:{
					// don't check again for a week
					return myWorld.params.ticks_per_week;
				}
				default:
					return myWorld.params.ticks_per_week;
				}
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		
		// the agent has been exposed, give them the symptoms of COVID for a week
		exposedNode = new BehaviourNode(){

			@Override
			public String getTitle() { return SpuriousSymptomNodeTitle.EXPOSED.key; }

			/**
			 * After being exposed, the disease may develop in a number of ways.
			 */
			@Override
			public double next(Steppable s, double time) {
				CoronavirusSpuriousSymptom symptom = (CoronavirusSpuriousSymptom) s;
				// can't have COVID symptoms if you aren't alive
				if (!symptom.getHost().isAlive()) {
					symptom.setBehaviourNode(deadNode);
					return Double.MAX_VALUE;
				}
				// can't have spurious COVID symptoms if actually have covid
				if (symptom.host.hasSymptomaticCovid()) {
					symptom.setBehaviourNode(susceptibleNode);
					symptom.getHost().removeCovidSpuriousSymptoms();
					return 1;
				}
				// Use switch statement to clearly create conditional actions based on the current state of this person's symptoms 
				String action = "";
				// if this is there first time then they will have a time of creation and no recovery time set
				if (symptom.timeRecovered == Double.MAX_VALUE) {
					action = "initialSetUp";
				}
				if (time >= symptom.timeRecovered) {
					action = "recover";
				}
				switch (action) {
					case "initialSetUp":{
						symptom.getHost().setHasSpuriousObject();
						symptom.timeLastTriggered = time;
						symptom.getHost().setCovidSpuriousSymptoms();
						symptom.getHost().setEligibleForCovidTesting();
						double timeUntilRecovered = symptom.timeLastTriggered + myWorld.params.ticks_per_week;
						symptom.timeRecovered = timeUntilRecovered;
						return 1;
						}
					case "recover":{
						symptom.getHost().removeCovidSpuriousSymptoms();
						symptom.getHost().removeEligibilityForCovidTesting();
						symptom.timeLastTriggered = Double.MAX_VALUE;
						symptom.timeRecovered = Double.MAX_VALUE;
						symptom.setBehaviourNode(susceptibleNode);
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
		deadNode = new BehaviourNode(){

			@Override
			public String getTitle() { return SpuriousSymptomNodeTitle.DEAD.key; }

			@Override
			public double next(Steppable s, double time) {
				CoronavirusSpuriousSymptom symptom = (CoronavirusSpuriousSymptom) s;
				// remove covid from person object
				symptom.getHost().removeCovidSpuriousSymptoms();								
				return Double.MAX_VALUE; // no need to run ever again
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return true;
			}
			
		};
		}
	
		
		public BehaviourNode setNode(SpuriousSymptomNodeTitle behaviour) {
			BehaviourNode toreturn;

			switch (behaviour) {
			case SUSCEPTIBLE:{
				toreturn = susceptibleNode;
				break;
			}
			case EXPOSED:{
				toreturn = exposedNode;
				break;
			}
			default:
				System.out.println("No node requested");
				toreturn = susceptibleNode;
			}
			return toreturn;

		}
		public BehaviourNode getStandardEntryPoint(){ return exposedNode; }

}