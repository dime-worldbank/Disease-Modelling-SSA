package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.Steppable;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourFramework;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class SpuriousSymptomBehaviourFramework extends BehaviourFramework{

	
	WorldBankCovid19Sim myWorld;
	BehaviourNode susceptibleNode = null, exposedNode = null;
	
	// PARAMS to control development of disease
	
	public SpuriousSymptomBehaviourFramework(WorldBankCovid19Sim model){
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
				if (symptom.getHost().isDeadFromOther()) {
					return Double.MAX_VALUE;
				}
				// Use switch statement to clearly create conditional actions based on the current state of this person's symptoms 
				String action = "";
				// if this is there first time then they will have a time of creation and no recovery time set
				if (symptom.timeRecovered == Double.MAX_VALUE) {
					action = "initialSetUp";
				}
				if (time < symptom.timeRecovered) {
					action = "maintainSymptoms";
				}
				if (time >= symptom.timeRecovered) {
					action = "recover";
				}
				switch (action) {
					case "initialSetUp":{
						symptom.getHost().setCovidSpuriousSymptoms();
						double timeUntilRecovered = symptom.timeCreated + 7.0;
						symptom.timeRecovered = timeUntilRecovered;
						return timeUntilRecovered;
						}
					case "maintainSymptoms":{
						return 1;
					}
					case "recover":{
						symptom.setBehaviourNode(susceptibleNode);
						symptom.getHost().removeCovidSpuriousSymptoms();
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
		
	} 
}