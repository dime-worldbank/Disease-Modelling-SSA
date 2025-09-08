package uk.ac.ucl.protecs.behaviours.diseaseProgression;

import sim.engine.Steppable;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class MeaslesDiseaseProgressionFramework extends DiseaseProgressionBehaviourFramework {
	
	public enum MeaslesBehaviourNodeTitle{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"), PRESYMPTOMATIC("presymptomatic"), ASYMPTOMATIC("asymptomatic"), 
		MILD("mild"), COMPLICATIONS("complications"), CRITICAL("critical"), RECOVERED("recovered"), DEAD("dead");
         
        public String key;
     
        MeaslesBehaviourNodeTitle(String key) { this.key = key; }
    
        static MeaslesBehaviourNodeTitle getValue(String x) {
        	switch (x) {
        	case "susceptible":
        		return SUSCEPTIBLE;
        	case "exposed":
        		return EXPOSED;
        	case "presymptomatic":
        		return PRESYMPTOMATIC;
        	case "mild":
        		return MILD;	
        	case "complications":
        		return COMPLICATIONS;	
        	case "critical":
        		return CRITICAL;
        	case "recovered":
        		return RECOVERED;
        	case "dead":
        		return DEAD;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}

	public MeaslesDiseaseProgressionFramework(WorldBankCovid19Sim world) {
		super(world);
		this.susceptibleNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return MeaslesBehaviourNodeTitle.SUSCEPTIBLE.key; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		this.exposedNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return MeaslesBehaviourNodeTitle.EXPOSED.key; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		this.presymptomaticNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return MeaslesBehaviourNodeTitle.PRESYMPTOMATIC.key; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		this.mildNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return MeaslesBehaviourNodeTitle.MILD.key; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		BehaviourNode complicationsNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return MeaslesBehaviourNodeTitle.COMPLICATIONS.key; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		this.criticalNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return MeaslesBehaviourNodeTitle.CRITICAL.key; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		this.recoveredNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return MeaslesBehaviourNodeTitle.RECOVERED.key; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
		
		this.deadNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return MeaslesBehaviourNodeTitle.DEAD.key; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
	}

}