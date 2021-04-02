package behaviours;

import objects.Infection;
import objects.Person;
import sim.WorldBankCovid19Sim;
import sim.engine.Steppable;

public class InfectiousBehaviourFramework extends BehaviourFramework {
	
	WorldBankCovid19Sim myWorld;
	BehaviourNode susceptibleNode = null, exposedNode = null, infectedNode = null,
			hospitalNode = null, icuNode = null, recovedNode = null, deadNode = null;
	
	double severity_param_DEATH = 80;
	double severity_param_ICU = 5;
	double severity_param_HOSPITAL = 2;
	double severity_param_RECOVERED = .01;

	// PARAMS to control development of disease
	
	public InfectiousBehaviourFramework(WorldBankCovid19Sim model){
		myWorld = model;
		
		// doesn't do anything - just sits in neutral
		susceptibleNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Susceptible"; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}
			
		};
		
		exposedNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Exposed"; }

			/**
			 * After being exposed, the disease may develop in a number of ways.
			 */
			@Override
			public double next(Steppable s, double time) {
				Infection i = (Infection) s;
				
				// the infection may take - in which case the agent becomes INFECTED
				if(myWorld.random.nextDouble() < myWorld.params.infection_beta){
					i.setBehaviourNode(infectedNode);
					return 1;
				}
				
				// the infection may also NOT take - in which case the agents just goes back
				// to being susceptible
				i.setBehaviourNode(susceptibleNode);
				return Double.MAX_VALUE;
			}
			
		};
		
		infectedNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Infected"; }

			@Override
			public double next(Steppable s, double time) {
				Infection i = (Infection) s;
				i.updateSeverity();
				i.getHost().infectNeighbours();
				if(i.getSeverity() > severity_param_DEATH){
					i.setBehaviourNode(deadNode);
					i.getHost().die();
					return Double.MAX_VALUE;
				} else if (i.getSeverity() > severity_param_ICU){
				
				}
				return 1;
			}
			
		};
		
		deadNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Dead"; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE; // no need to run ever again
			}
			
		};
		
		entryPoint = exposedNode;
	}
}