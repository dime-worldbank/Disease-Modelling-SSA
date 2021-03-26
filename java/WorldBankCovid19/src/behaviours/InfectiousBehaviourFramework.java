package behaviours;

import objects.Infection;
import objects.Person;
import sim.WorldBankCovid19Sim;
import sim.engine.Steppable;

public class InfectiousBehaviourFramework extends BehaviourFramework {
	
	WorldBankCovid19Sim myWorld;
	BehaviourNode susceptibleNode = null, exposedNode = null, infectedNode = null, recovedNode = null, deadNode = null;

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
				
				if(myWorld.random.nextDouble() < myWorld.params.infection_beta){
					i.setBehaviourNode(infectedNode);
					return 1;
				}
				
				i.setBehaviourNode(susceptibleNode);
				return Double.MAX_VALUE;
			}
			
		};
		
		infectedNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Infected"; }

			@Override
			public double next(Steppable s, double time) {
				((Infection)s).getHost().infectNeighbours();
				return 1;
			}
			
		};
		
		entryPoint = exposedNode;
	}
}