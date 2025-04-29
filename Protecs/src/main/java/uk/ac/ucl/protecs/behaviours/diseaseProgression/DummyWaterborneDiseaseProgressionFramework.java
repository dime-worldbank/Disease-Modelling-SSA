package uk.ac.ucl.protecs.behaviours.diseaseProgression;



import uk.ac.ucl.protecs.objects.diseases.DummyWaterborneDisease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.sim.*;
import sim.engine.Steppable;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class DummyWaterborneDiseaseProgressionFramework extends DiseaseProgressionBehaviourFramework {
	
	public enum WaterborneBehaviourNodeInHumans{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"), RECOVERED("recovered"), DEAD("dead");

        String key;
     
        WaterborneBehaviourNodeInHumans(String key) { this.key = key; }
    
        static WaterborneBehaviourNodeInHumans getValue(String x) {
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
	
	public enum WaterborneBehaviourNodeInWater{
		CLEAN("clean"), CONTAMINATED("contaminated");

        String key;
     
        WaterborneBehaviourNodeInWater(String key) { this.key = key; }
    
        static WaterborneBehaviourNodeInWater getValue(String x) {
        	switch (x) {
        	case "clean":
        		return CLEAN;
        	case "contaminated":
        		return CONTAMINATED;
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
	public DummyWaterborneDiseaseProgressionFramework(WorldBankCovid19Sim world) {
		super(world);
		// TODO Auto-generated constructor stub
		
		this.susceptibleNode = new BehaviourNode(){
			
			@Override
			public String getTitle() { return WaterborneBehaviourNodeInHumans.SUSCEPTIBLE.key; }

			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
			}

			@Override
			public boolean isEndpoint() {
				return false;
			}
			
		};
	
	this.exposedNode = new BehaviourNode() {

		@Override
		public String getTitle() {
			// TODO Auto-generated method stub
			return WaterborneBehaviourNodeInHumans.EXPOSED.key;
		}

		@Override
		public double next(Steppable s, double time) {
			// default next step of progression is no symptoms, check if they will develop symptoms this week
			nextStep = nextStepDummy.DO_NOTHING;
			if (myWorld.random.nextDouble() <= world.params.dummy_infectious_recovery_rate) {
				nextStep = nextStepDummy.RECOVER;
			}
			// check if this person has died
			DummyWaterborneDisease d = (DummyWaterborneDisease) s;
			d.time_infected = time;
			if (!((Person) d.getHost()).isAlive()) {
				nextStep = nextStepDummy.HAS_DIED;
				}
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
			return WaterborneBehaviourNodeInHumans.RECOVERED.key;
		}

		@Override
		public double next(Steppable s, double time) {
			DummyWaterborneDisease d = (DummyWaterborneDisease) s;
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
			return WaterborneBehaviourNodeInHumans.DEAD.key;
		}

		@Override
		public double next(Steppable s, double time) {
			DummyWaterborneDisease d = (DummyWaterborneDisease) s;
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
	
	this.infectionInWater = new BehaviourNode() {

		@Override
		public String getTitle() {
			// TODO Auto-generated method stub
			return WaterborneBehaviourNodeInWater.CONTAMINATED.key;
		}

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public double next(Steppable arg0, double arg1) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	};
}
	public BehaviourNode getStandardEntryPoint(){ return this.susceptibleNode; }
	
	public BehaviourNode getStandardEntryPointForWater(){ return this.infectionInWater; }


}
