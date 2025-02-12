package uk.ac.ucl.protecs.behaviours;



import uk.ac.ucl.protecs.objects.diseases.Infection;
import uk.ac.ucl.protecs.sim.*;
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

	BehaviourNode standardEntryPoint;
	public InfectiousBehaviourFramework(WorldBankCovid19Sim world){
		myWorld = world;
		
		}
	public void setStandardEntryPoint() {};	
	
}