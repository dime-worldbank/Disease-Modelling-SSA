package uk.ac.ucl.protecs.behaviours.diseaseProgression;



import uk.ac.ucl.protecs.objects.diseases.DummyInfectiousDisease;
import uk.ac.ucl.protecs.sim.*;
import sim.engine.Steppable;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class DummyInfectiousDiseaseProgressionFramework extends diseaseProgressionBehaviourFramework {
	
	public enum DummyInfectiousBehaviourNode{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"), RECOVERED("recovered"), DEAD("dead");

        String key;
     
        DummyInfectiousBehaviourNode(String key) { this.key = key; }
    
        static DummyInfectiousBehaviourNode getValue(String x) {
        	switch (x) {
        	case "susceptible":
        		return SUSCEPTIBLE;
        	case "exposed":
        		return EXPOSED;
        	case "recover":
        		return RECOVERED;
        	case "dead":
        		return DEAD;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}
	
	public enum nextStepDummy{
		NO_SYMPTOMS("noSymptoms"), CAUSE_SYMPTOMS("causeSymptoms"), DO_NOTHING("doNothing"), RECOVER("recover"), HAS_DIED("hasDied");
		public String key;
	     
		nextStepDummy(String key) { this.key = key; }
		
		public static nextStepDummy getValue(String x) {
        	switch (x) {
        	case "noSymptoms":
        		return NO_SYMPTOMS;
        	case "causeSymptoms":
        		return CAUSE_SYMPTOMS;
        	case "doNothing":
        		return CAUSE_SYMPTOMS;
        	case "recover":
        		return RECOVER;	
        	case "hasDied":
        		return HAS_DIED;
        	default:
        		throw new IllegalArgumentException();
        	}
		
		}
	} 
	private nextStepDummy nextStep;

	@SuppressWarnings("serial")
	public DummyInfectiousDiseaseProgressionFramework(WorldBankCovid19Sim world) {
		super(world);
		// TODO Auto-generated constructor stub
		
		this.susceptibleNode = new BehaviourNode() {

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return DummyInfectiousBehaviourNode.SUSCEPTIBLE.key;
			}

			@Override
			public double next(Steppable s, double time) {
				
				
				// default next step of progression is no symptoms, check if they will develop symptoms this week
				nextStep = nextStepDummy.NO_SYMPTOMS;
				if (myWorld.random.nextDouble() <= 0.5) {
					nextStep = nextStepDummy.CAUSE_SYMPTOMS;
				}
				// check if this person has died
				DummyInfectiousDisease d = (DummyInfectiousDisease) s;

				if (!d.getHost().isAlive()) {
					nextStep = nextStepDummy.HAS_DIED;
					}
				// choose to progress the disease or not based on value of nextStep
				switch (nextStep) {
				case CAUSE_SYMPTOMS:{
					d.setBehaviourNode(exposedNode);
					return myWorld.params.ticks_per_week;
				}
				case HAS_DIED:{
					d.setBehaviourNode(deadNode);
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
	
	this.exposedNode = new BehaviourNode() {

		@Override
		public String getTitle() {
			// TODO Auto-generated method stub
			return DummyInfectiousBehaviourNode.EXPOSED.key;
		}

		@Override
		public double next(Steppable s, double time) {
			// default next step of progression is no symptoms, check if they will develop symptoms this week
			nextStep = nextStepDummy.DO_NOTHING;
			if (myWorld.random.nextDouble() <= 0.5) {
				nextStep = nextStepDummy.RECOVER;
			}
			// check if this person has died
			DummyInfectiousDisease d = (DummyInfectiousDisease) s;
			d.time_infected = time;
			if (!d.getHost().isAlive()) {
				nextStep = nextStepDummy.HAS_DIED;
				}
			d.getHost().infectNeighbours();

			// choose to progress the disease or not based on value of nextStep
			switch (nextStep) {
			case RECOVER:{
				d.setBehaviourNode(recoveredNode);
				return myWorld.params.ticks_per_week;
			}
			case HAS_DIED:{
				d.setBehaviourNode(deadNode);
				return Double.MAX_VALUE;
			}
			case DO_NOTHING:{
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
	
	this.recoveredNode = new BehaviourNode() {

		@Override
		public String getTitle() {
			// TODO Auto-generated method stub
			return DummyInfectiousBehaviourNode.RECOVERED.key;
		}

		@Override
		public double next(Steppable s, double time) {
			DummyInfectiousDisease d = (DummyInfectiousDisease) s;
			// store the recovery time
			d.time_recovered = time;
			// do nothing by refault
			nextStep = nextStepDummy.DO_NOTHING;
			if (myWorld.random.nextDouble() <= 0.5) {
				// reset infection process
				nextStep = nextStepDummy.NO_SYMPTOMS;
			}
			return myWorld.params.ticks_per_week;
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}};
	
	this.deadNode = new BehaviourNode() {

		@Override
		public String getTitle() {
			// TODO Auto-generated method stub
			return DummyInfectiousBehaviourNode.DEAD.key;
		}

		@Override
		public double next(Steppable s, double time) {
			DummyInfectiousDisease d = (DummyInfectiousDisease) s;
			// Store time of death
			d.time_died = time;
			return Double.MAX_VALUE;
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
		
	};
}
	public BehaviourNode getStandardEntryPoint(){ return this.susceptibleNode; }

}
