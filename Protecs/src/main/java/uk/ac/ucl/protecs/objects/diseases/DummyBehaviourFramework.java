package uk.ac.ucl.protecs.objects.diseases;



import uk.ac.ucl.protecs.objects.*;
import uk.ac.ucl.protecs.sim.*;
import sim.engine.Steppable;
import swise.behaviours.BehaviourFramework;
import swise.behaviours.BehaviourNode;

public class DummyBehaviourFramework extends InfectiousBehaviourFramework {
	
	public enum DummyBehaviourNode{
		SUSCEPTIBLE("susceptible"), EXPOSED("exposed"), RECOVER("recover");

        String key;
     
        DummyBehaviourNode(String key) { this.key = key; }
    
        static DummyBehaviourNode getValue(String x) {
        	switch (x) {
        	case "susceptible":
        		return SUSCEPTIBLE;
        	case "exposed":
        		return EXPOSED;
        	case "recover":
        		return RECOVER;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}

	@SuppressWarnings("serial")
	public DummyBehaviourFramework(WorldBankCovid19Sim world, Infection infection) {
		super(world, infection);
		// TODO Auto-generated constructor stub
		
		this.susceptibleNode = new BehaviourNode() {

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return DummyBehaviourNode.SUSCEPTIBLE.key;
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
