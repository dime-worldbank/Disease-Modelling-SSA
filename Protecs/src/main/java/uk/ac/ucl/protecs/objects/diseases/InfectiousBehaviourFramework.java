package uk.ac.ucl.protecs.objects.diseases;

import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourFramework;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

// TODO: Make this generalised so we can create new disease and extend this framework so you can apply things from here to that disease
public class InfectiousBehaviourFramework extends BehaviourFramework{
//	
//	WorldBankCovid19Sim myWorld;
//	// create an overall list of the behaviour nodes used by the diseases in the model
//
//	BehaviourNode susceptibleNode = null, exposedNode = null, presymptomaticNode= null, asymptomaticNode = null,
//			mildNode = null, severeNode = null, criticalNode = null, recoveredNode = null, deadNode = null;
//	
//	// PARAMS to control development of disease
//	
//	public InfectiousBehaviourFramework(WorldBankCovid19Sim model){
//		myWorld = model;
//		
//		// the default status
//		susceptibleNode = createSusceptibleNode();
//		
//		// the agent has been exposed - determine whether the infection will develop
//		exposedNode = createExposedNode();
//		
//		// the agent is infectious, but not yet showing symptoms
//		presymptomaticNode = createPresymptomaticNode();
//		
//		// the agent is infectious, but will not show symptoms. They will eventually recover.
//		asymptomaticNode = createAsymptomaticNode();
//		
//		// the agent has a mild case and is infectious. They may recover, or else progress to a severe case.
//		mildNode = createMildNode();
//
//		// the agent has a severe case and is infectious. They may recover, or else progress to a critical case.
//		severeNode = createSevereNode();
//
//		// the agent has a critical case and is infectious. They may recover, or else progress to death.
//		criticalNode = createCriticalNode();
//		
//		// the agent has recovered.
//		recoveredNode = createRecoveredNode();
//		
//		deadNode = createDeadNode();
//		
//
//		
//		entryPoint = exposedNode;
//	}
//
//	public BehaviourNode createDeadNode() {
//		return new BehaviourNode(){
//
//			@Override
//			public String getTitle() { return "dead"; }
//
//			@Override
//			public double next(Steppable s, double time) {
//				return Double.MAX_VALUE; // no need to run ever again
//			}
//
//			@Override
//			public boolean isEndpoint() {
//				// TODO Auto-generated method stub
//				return true;
//			}
//			
//		};
//	}
//
//	public BehaviourNode createRecoveredNode() {
//		return new BehaviourNode(){
//
//			@Override
//			public String getTitle() { return "recovered"; }
//
//			@Override
//			public double next(Steppable s, double time) {
//				return Double.MAX_VALUE;
//			}
//
//			@Override
//			public boolean isEndpoint() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		};
//	}
//
//	public BehaviourNode createCriticalNode() {
//		return new BehaviourNode(){
//
//			@Override
//			public String getTitle() { return "critical_case"; }
//
//			@Override
//			public double next(Steppable s, double time) {
//				// update every timestep
//				return Double.MAX_VALUE;
//			}
//
//			@Override
//			public boolean isEndpoint() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		};
//	}
//
//	public BehaviourNode createSevereNode() {
//		return new BehaviourNode(){
//
//			@Override
//			public String getTitle() { return "severe_case"; }
//
//			@Override
//			public double next(Steppable s, double time) {
//				return Double.MAX_VALUE;
//			}
//
//			@Override
//			public boolean isEndpoint() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		};
//	}
//
//	public BehaviourNode createMildNode() {
//		return new BehaviourNode(){
//
//			@Override
//			public String getTitle() { return "mild_case"; }
//
//			@Override
//			public double next(Steppable s, double time) {
//				
//				return Double.MAX_VALUE;
//			}
//
//			@Override
//			public boolean isEndpoint() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		};
//	}
//
//	public BehaviourNode createAsymptomaticNode() {
//		return new BehaviourNode(){
//
//			@Override
//			public String getTitle() { return "asymptomatic"; }
//
//			@Override
//			public double next(Steppable s, double time) {
//				return Double.MAX_VALUE;
//			}
//
//			@Override
//			public boolean isEndpoint() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		};
//	}
//
//	public BehaviourNode createPresymptomaticNode() {
//		return new BehaviourNode(){
//
//			@Override
//			public String getTitle() { return "presymptomatic"; }
//
//			@Override
//			public double next(Steppable s, double time) {
//				return Double.MAX_VALUE;
//			}
//
//			@Override
//			public boolean isEndpoint() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		};
//	}
//
//	public BehaviourNode createExposedNode() {
//		return new BehaviourNode(){
//
//			@Override
//			public String getTitle() { return "exposed"; }
//
//			/**
//			 * After being exposed, the disease may develop in a number of ways.
//			 */
//			@Override
//			public double next(Steppable s, double time) {
//				return Double.MAX_VALUE;
//			}
//
//			@Override
//			public boolean isEndpoint() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		};
//	}
//
//	public BehaviourNode createSusceptibleNode() {
//		return new BehaviourNode(){
//
//			@Override
//			public String getTitle() { return "susceptible"; }
//
//			@Override
//			public double next(Steppable s, double time) {
//				return Double.MAX_VALUE;
//			}
//
//			@Override
//			public boolean isEndpoint() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		};
//	}
//	
//	public BehaviourNode setNodeForTesting(String behaviour) {
//		BehaviourNode toreturn;
//
//		switch (behaviour) {
//		case "susceptible":{
//			toreturn = susceptibleNode;
//			break;
//		}
//		case "exposed":{
//			toreturn = exposedNode;
//			break;
//		}
//		case "presymptomatic":{
//			toreturn = presymptomaticNode;
//			break;
//		}
//		case "asymptomatic":{
//			toreturn = asymptomaticNode;
//			break;
//		}
//		case "mild":{
//			toreturn = mildNode;
//			break;
//		}
//		case "severe":{
//			toreturn = severeNode;
//			break;
//		}
//		case "critical":{
//			toreturn = criticalNode;
//			break;
//		}
//		case "recovered":{
//			toreturn = recoveredNode;
//			break;
//		}
//		case "dead":{
//			toreturn = deadNode;
//			break;
//		}
//		default:
//			toreturn = susceptibleNode;
//			break;
//		}
//			
//		return toreturn;
//	}
//	public BehaviourNode getStandardEntryPoint(){ return susceptibleNode; }
//	public BehaviourNode getInfectedEntryPoint(Location l){
//				
//		if(myWorld.random.nextDouble() < .5){ // TODO make this based on real data
//			l.getRootSuperLocation().metric_new_cases_sympt++;
//			return presymptomaticNode;
//		}
//		else{
//			l.getRootSuperLocation().metric_new_cases_asympt++;
//			return asymptomaticNode;
//		}
//	}
}
