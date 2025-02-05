package uk.ac.ucl.protecs.objects.diseases;



import uk.ac.ucl.protecs.objects.*;
import uk.ac.ucl.protecs.objects.diseases.SpuriousSymptomBehaviourFramework.nextStepSpurious;
import uk.ac.ucl.protecs.sim.*;
import sim.engine.Steppable;
import swise.behaviours.BehaviourFramework;
import swise.behaviours.BehaviourNode;

public class DummyBehaviourFramework extends InfectiousBehaviourFramework {
	
	public enum DummyBehaviourNode{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"), RECOVERED("recovered");

        String key;
     
        DummyBehaviourNode(String key) { this.key = key; }
    
        static DummyBehaviourNode getValue(String x) {
        	switch (x) {
        	case "susceptible":
        		return SUSCEPTIBLE;
        	case "exposed":
        		return EXPOSED;
        	case "recover":
        		return RECOVERED;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}
	
	public enum nextStepDummy{
		NO_SYMPTOMS("noSymptoms"), CAUSE_SYMPTOMS("causeSymptoms"), HAS_DIED("hasDied");
		public String key;
	     
		nextStepDummy(String key) { this.key = key; }
		
		public static nextStepDummy getValue(String x) {
        	switch (x) {
        	case "noSymptoms":
        		return NO_SYMPTOMS;
        	case "causeSymptoms":
        		return CAUSE_SYMPTOMS;
        	case "hasDied":
        		return HAS_DIED;
        	default:
        		throw new IllegalArgumentException();
        	}
		
		}
	} 
	private nextStepDummy nextStep;

	@SuppressWarnings("serial")
	public DummyBehaviourFramework(WorldBankCovid19Sim world) {
		super(world);
		// TODO Auto-generated constructor stub
		
		this.susceptibleNode = new BehaviourNode() {

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return DummyBehaviourNode.SUSCEPTIBLE.key;
			}

			@Override
			public double next(Steppable s, double time) {
				
				// default next step of progression is no symptoms, check if they will develop symptoms this week
				nextStep = nextStepDummy.NO_SYMPTOMS;
				if (myWorld.random.nextDouble() <= 0.5) {
					nextStep = nextStepDummy.CAUSE_SYMPTOMS;
				}
				
				if (myWorld.random.nextDouble() <= 0.5) {
					nextStep = nextStepDummy.CAUSE_SYMPTOMS;
				}
				// TODO Auto-generated method stub
				return 0;
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
			return DummyBehaviourNode.EXPOSED.key;
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
	
	this.recoveredNode = new BehaviourNode() {

		@Override
		public String getTitle() {
			// TODO Auto-generated method stub
			return DummyBehaviourNode.RECOVERED.key;
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
		}};
	

}
}
