package uk.ac.ucl.protecs.objects.diseases;



import uk.ac.ucl.protecs.objects.*;
import uk.ac.ucl.protecs.sim.*;
import sim.engine.Steppable;
import swise.behaviours.BehaviourFramework;
import swise.behaviours.BehaviourNode;

// This file is intended to be the source from which we create disease states for all diseases in the model. 
// Many infections share certain characteristics relevant to modelling, such as infection state, disease progression etc. This is an attempt
// to begin to build a modular framework to house these qualities

public class InfectiousBehaviourFramework extends BehaviourFramework {
	WorldBankCovid19Sim myWorld;
	Infection myInfection;
	BehaviourNode susceptibleNode = null, exposedNode = null, presymptomaticNode= null, asymptomaticNode = null,
			mildNode = null, severeNode = null, criticalNode = null, recoveredNode = null, deadNode = null;

	
	public InfectiousBehaviourFramework(WorldBankCovid19Sim world, Infection infection){
		myWorld = world;
		myInfection = infection;
		
		susceptibleNode = new BehaviourNode(){

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public double next(Steppable s, double time) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		
		
		}
}