package uk.ac.ucl.protecs.behaviours.diseaseProgression;

import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusSpuriousSymptom;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class SpuriousSymptomDiseaseProgressionFramework extends DiseaseProgressionBehaviourFramework{

	
	
	// create an enum title for each of the spurious symptom behaviour nodes, susceptible (no symptoms), exposed (has symptoms), 
	// dead (has passed away so can't have symptoms), setup (for initialising) and recover for removing symptoms
	public enum SpuriousSymptomBehaviourNode{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"), DEAD("dead"), SETUP("initialSetUp"), RECOVER("recover");

        String key;
     
        SpuriousSymptomBehaviourNode(String key) { this.key = key; }
    
        static SpuriousSymptomBehaviourNode getValue(String x) {
        	switch (x) {
        	case "susceptible":
        		return SUSCEPTIBLE;
        	case "exposed":
        		return EXPOSED;
        	case "dead":
        		return DEAD;
        	case "initialSetUp":
        		return SETUP;
        	case "recover":
        		return RECOVER;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}
	// create an enum title to progress the onset of symptoms step-wise

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
	
	public SpuriousSymptomDiseaseProgressionFramework(WorldBankCovid19Sim myWorld){
		super(myWorld);
		
		// the default status
		this.susceptibleNode = new BehaviourNode(){

			@Override
			public String getTitle() { return SpuriousSymptomBehaviourNode.SUSCEPTIBLE.key; }

			@Override
			public double next(Steppable s, double time) {
				// regulate the flare up of symptoms here
				CoronavirusSpuriousSymptom symptom = (CoronavirusSpuriousSymptom) s;
				// default next step of progression is no symptoms, check if they will develop symptoms this week
				nextStep = nextStepSpurious.NO_SYMPTOMS;
				if (myWorld.random.nextDouble() <= myWorld.params.rate_of_covid_spurious_symptoms) {
					nextStep = nextStepSpurious.CAUSE_SYMPTOMS;
				}
				// need to check that those who died don't do anything, do this here
				if (!((Person) symptom.getHost()).isAlive()) {
					nextStep = nextStepSpurious.HAS_DIED;
					}
				if (((Person) symptom.getHost()).hasSymptomaticCovid()) {
					nextStep = nextStepSpurious.NO_SYMPTOMS;
				}
				// based on the next step string variable, choose the next thing to do for this person's spurious symptoms.
				switch (nextStep) {
				case CAUSE_SYMPTOMS:{
					symptom.setBehaviourNode(setNode(SpuriousSymptomBehaviourNode.EXPOSED));
					return myWorld.params.ticks_per_week;
				}
				case HAS_DIED:{
					symptom.setBehaviourNode(setNode(SpuriousSymptomBehaviourNode.DEAD));
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
		this.exposedNode = new BehaviourNode(){

			@Override
			public String getTitle() { return SpuriousSymptomBehaviourNode.EXPOSED.key; }

			/**
			 * After being exposed, the disease may develop in a number of ways.
			 */
			@Override
			public double next(Steppable s, double time) {
				CoronavirusSpuriousSymptom symptom = (CoronavirusSpuriousSymptom) s;
				// can't have COVID symptoms if you aren't alive
				if (!((Person) symptom.getHost()).isAlive()) {
					symptom.setBehaviourNode(deadNode);
					return Double.MAX_VALUE;
				}
				// can't have spurious COVID symptoms if actually have covid
				if (((Person) symptom.getHost()).hasSymptomaticCovid()) {
					symptom.setBehaviourNode(setNode(SpuriousSymptomBehaviourNode.SUSCEPTIBLE));
					return 1;
				}
				// Use switch statement to clearly create conditional actions based on the current state of this person's symptoms 
				SpuriousSymptomBehaviourNode action = SpuriousSymptomBehaviourNode.EXPOSED;
				// if this is there first time then they will have a time of creation and no recovery time set
				if (symptom.time_recovered == Double.MAX_VALUE) {
					action = SpuriousSymptomBehaviourNode.SETUP;
				}
				// If the current time is later than the time we need to remove symptoms, remove spurious symptoms
				if (time >= symptom.time_recovered) {
					action = SpuriousSymptomBehaviourNode.RECOVER;
				}
				// If the host has a symptomatic infection, remove spurious symptoms
				if (((Person) symptom.getHost()).hasSymptomaticCovid()) {
					action = SpuriousSymptomBehaviourNode.RECOVER;
				}
				switch (action) {
					case SETUP:{
						symptom.timeLastTriggered = time;
						symptom.setSymptomatic();
						symptom.setEligibleForTesting();
						symptom.time_recovered = symptom.timeLastTriggered + myWorld.params.ticks_per_week;
						return 1;
						}
					case RECOVER:{
						symptom.setAsympt();
						symptom.removeEligibilityForTesting();
						symptom.timeLastTriggered = Double.MAX_VALUE;
						symptom.time_recovered = Double.MAX_VALUE;
						symptom.setBehaviourNode(setNode(SpuriousSymptomBehaviourNode.SUSCEPTIBLE));
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
		this.deadNode = new BehaviourNode(){

			@Override
			public String getTitle() { return SpuriousSymptomBehaviourNode.DEAD.key; }

			@Override
			public double next(Steppable s, double time) {
				CoronavirusSpuriousSymptom symptom = (CoronavirusSpuriousSymptom) s;
				// remove covid from person object
				symptom.setAsympt();
				return Double.MAX_VALUE; // no need to run ever again
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return true;
			}
			
		};
		}
	
		
		public BehaviourNode setNode(SpuriousSymptomBehaviourNode behaviour) {
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
			default:
				System.out.println("No node requested");
				toreturn = this.susceptibleNode;
			}
			return toreturn;

		}
		public BehaviourNode getStandardEntryPoint(){ return this.exposedNode; }

}