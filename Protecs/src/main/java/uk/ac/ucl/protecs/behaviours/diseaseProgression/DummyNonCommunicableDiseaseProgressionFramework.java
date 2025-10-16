package uk.ac.ucl.protecs.behaviours.diseaseProgression;



import uk.ac.ucl.protecs.objects.diseases.DummyNonCommunicableDisease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.sim.*;
import sim.engine.Steppable;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class DummyNonCommunicableDiseaseProgressionFramework extends DiseaseProgressionBehaviourFramework {
	
	public double dummy_ncd_recovery_rate = 0.5;
	public double dummy_ncd_base_rate = 0.2;
	public double dummy_ncd_rr_male = 1.05;
	public double dummy_ncd_rr_over_50 = 1.2;
	public double dummy_ncd_initial_fraction_with_ncd = 0.1;
	
	public enum DummyNonCommunicableBehaviourNode{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"), RECOVERED("recovered"), DEAD("dead");

        String key;
     
        DummyNonCommunicableBehaviourNode(String key) { this.key = key; }
    
        static DummyNonCommunicableBehaviourNode getValue(String x) {
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
	public DummyNonCommunicableDiseaseProgressionFramework(WorldBankCovid19Sim world) {
		super(world);
		// TODO Auto-generated constructor stub
		
		this.susceptibleNode = new BehaviourNode() {

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return DummyNonCommunicableBehaviourNode.SUSCEPTIBLE.key;
			}

			@Override
			public double next(Steppable s, double time) {
				
				
				// default next step of progression is no symptoms, check if they will develop symptoms this week
				nextStep = nextStepDummy.NO_SYMPTOMS;
				if (myWorld.random.nextDouble() <= 0.5) {
					nextStep = nextStepDummy.CAUSE_SYMPTOMS;
				}
				// check if this person has died
				DummyNonCommunicableDisease d = (DummyNonCommunicableDisease) s;

				if (!((Person) d.getHost()).isAlive()) {
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
			return DummyNonCommunicableBehaviourNode.EXPOSED.key;
		}

		@Override
		public double next(Steppable s, double time) {
			// default next step of progression is no symptoms, check if they will develop symptoms this week
			nextStep = nextStepDummy.DO_NOTHING;
			if (myWorld.random.nextDouble() <= dummy_ncd_recovery_rate) {
				nextStep = nextStepDummy.RECOVER;
			}
			// check if this person has died
			DummyNonCommunicableDisease d = (DummyNonCommunicableDisease) s;
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
			return DummyNonCommunicableBehaviourNode.RECOVERED.key;
		}

		@Override
		public double next(Steppable s, double time) {
			DummyNonCommunicableDisease d = (DummyNonCommunicableDisease) s;
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
			return DummyNonCommunicableBehaviourNode.DEAD.key;
		}

		@Override
		public double next(Steppable s, double time) {
			DummyNonCommunicableDisease d = (DummyNonCommunicableDisease) s;
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
	public double getDummy_ncd_recovery_rate() {
		return dummy_ncd_recovery_rate;
	}
	public void setDummy_ncd_recovery_rate(double dummy_ncd_recovery_rate) {
		this.dummy_ncd_recovery_rate = dummy_ncd_recovery_rate;
	}
	
	public double getDummy_ncd_base_rate() {
		return dummy_ncd_base_rate;
	}
	public void setDummy_ncd_base_rate(double dummy_ncd_base_rate) {
		this.dummy_ncd_base_rate = dummy_ncd_base_rate;
	}
	public double getDummy_ncd_rr_male() {
		return dummy_ncd_rr_male;
	}
	public void setDummy_ncd_rr_male(double dummy_ncd_rr_male) {
		this.dummy_ncd_rr_male = dummy_ncd_rr_male;
	}
	public double getDummy_ncd_rr_over_50() {
		return dummy_ncd_rr_over_50;
	}
	public void setDummy_ncd_rr_over_50(double dummy_ncd_rr_over_50) {
		this.dummy_ncd_rr_over_50 = dummy_ncd_rr_over_50;
	}
	public double getDummy_ncd_initial_fraction_with_ncd() {
		return dummy_ncd_initial_fraction_with_ncd;
	}
	public void setDummy_ncd_initial_fraction_with_ncd(double dummy_ncd_initial_fraction_with_ncd) {
		this.dummy_ncd_initial_fraction_with_ncd = dummy_ncd_initial_fraction_with_ncd;
	}
	
	public BehaviourNode getStandardEntryPoint(){ return this.susceptibleNode; }

}
