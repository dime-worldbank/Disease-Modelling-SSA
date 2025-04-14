package uk.ac.ucl.protecs.behaviours.diseaseProgression;



import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.sim.*;
import uk.ac.ucl.swise.behaviours.BehaviourFramework;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

// This file is intended to be the source from which we create disease states for all diseases in the model. 
// Many infections share certain characteristics relevant to modelling, such as infection state, disease progression etc. This is an attempt
// to begin to build a modular framework to house these qualities

public class DiseaseProgressionBehaviourFramework implements BehaviourFramework {
	WorldBankCovid19Sim myWorld;
	Disease myInfection;
	BehaviourNode susceptibleNode = null, exposedNode = null, presymptomaticNode= null, asymptomaticNode = null,
			mildNode = null, severeNode = null, criticalNode = null, recoveredNode = null, deadNode = null, infectionInWater = null;

	BehaviourNode standardEntryPoint;
	public DiseaseProgressionBehaviourFramework(WorldBankCovid19Sim world){
		myWorld = world;
		
		}
	
	@Override
	public BehaviourNode getEntryPoint() {
		return this.exposedNode;
	}

	@Override
	public BehaviourNode getHomeNode() {
		// TODO Auto-generated method stub
		return null;
	};	
	
}