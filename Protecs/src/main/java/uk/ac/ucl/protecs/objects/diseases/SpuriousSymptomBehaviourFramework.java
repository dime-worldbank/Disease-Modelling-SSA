package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.Steppable;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourFramework;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class SpuriousSymptomBehaviourFramework extends BehaviourFramework{

	
	WorldBankCovid19Sim myWorld;
	BehaviourNode susceptibleNode = null, exposedNode = null, deadNode = null;
	
	// PARAMS to control development of disease
	
	public SpuriousSymptomBehaviourFramework(WorldBankCovid19Sim model){
		myWorld = model;
		
		// the default status
		susceptibleNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "susceptible"; }

			@Override
			public double next(Steppable s, double time) {
				// regulate the flare up of symptoms here
				CoronavirusSpuriousSymptom symptom = (CoronavirusSpuriousSymptom) s;
				// default next step of progression is no symptoms, check if they will develop symptoms this week
				String nextStep = "noSymptoms";
				if (myWorld.random.nextDouble() <= myWorld.params.rate_of_spurious_symptoms) {
					nextStep = "causeSymptoms";
				}
				// need to check that those who died don't do anything, do this here
				if (!symptom.host.isAlive()) {
					nextStep = "hasDied";
					}
				// based on the next step string variable, choose the next thing to do for this person's spurious symptoms.
				switch (nextStep) {
				case "causeSymptoms":{
					symptom.setBehaviourNode(exposedNode);
					return myWorld.params.ticks_per_week;
				}
				case "hasDied":{
					symptom.setBehaviourNode(deadNode);
					return Double.MAX_VALUE;
				}
				case "noSymptoms":{
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
			public String getTitle() { return "exposed"; }

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
						System.out.println("setting");
						symptom.timeLastTriggered = time;
						symptom.getHost().setCovidSpuriousSymptoms();
						double timeUntilRecovered = symptom.timeLastTriggered + myWorld.params.ticks_per_week;
						symptom.timeRecovered = timeUntilRecovered;
						return 1;
						}
					case "recover":{
						System.out.println("recovering");
						symptom.setBehaviourNode(susceptibleNode);
						symptom.getHost().removeCovidSpuriousSymptoms();
						symptom.timeLastTriggered = Double.MAX_VALUE;
						symptom.timeRecovered = Double.MAX_VALUE;
						return Double.MAX_VALUE;
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
			public String getTitle() { return "dead"; }

			@Override
			public double next(Steppable s, double time) {
				CoronavirusSpuriousSymptom symptom = (CoronavirusSpuriousSymptom) s;
				// remove covid from person object
				symptom.getHost().removeCovidSpuriousSymptoms();;								
				return Double.MAX_VALUE; // no need to run ever again
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return true;
			}
			
		};
		}
	
		
		public BehaviourNode setNode(String behaviour) {
			BehaviourNode toreturn;

			switch (behaviour) {
			case "susceptible":{
				toreturn = susceptibleNode;
				break;
			}
			case "exposed":{
				toreturn = exposedNode;
				break;
			}
			default:
				System.out.println("No node requested");
				toreturn = susceptibleNode;
			}
			return toreturn;

		}
		public BehaviourNode getStandardEntryPoint(){ return susceptibleNode; }

}