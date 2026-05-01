package uk.ac.ucl.protecs.behaviours.diseaseProgression;

import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.HIV;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public class HIVDiseaseProgressionFramework extends DiseaseProgressionBehaviourFramework{
	
	public enum HIVBehaviourNode{
		SUSCEPTIBLE, EXPOSED, MILD, SEVERE, CRITICAL, DEAD, RECOVERED, TREATED
  
	}
	

	public HIVDiseaseProgressionFramework(WorldBankCovid19Sim world) {
		super(world);
		this.susceptibleNode = new BehaviourNode(){

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return HIVBehaviourNode.SUSCEPTIBLE.name();
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
		
		this.exposedNode = new BehaviourNode(){

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return HIVBehaviourNode.EXPOSED.name();
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public double next(Steppable s, double arg1) {
				HIV i = (HIV) s;

				i.setBehaviourNode(mildNode);
				return 1;
			}
			
		};
		
		this.mildNode = new BehaviourNode(){

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return HIVBehaviourNode.MILD.name();
			}

			@Override
			public boolean isEndpoint() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public double next(Steppable arg0, double arg1) {
				// We are just using HIV as a property temporarily return max for now
				return Double.MAX_VALUE;
			}
			
		};
		
		this.severeNode = new BehaviourNode(){

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return HIVBehaviourNode.SEVERE.name();
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
		
		this.criticalNode = new BehaviourNode(){

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return HIVBehaviourNode.CRITICAL.name();
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
		
		this.deadNode = new BehaviourNode(){

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return HIVBehaviourNode.DEAD.name();
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
		
		this.recoveredNode = new BehaviourNode(){

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return HIVBehaviourNode.RECOVERED.name();
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
		
		BehaviourNode treatedNode = new BehaviourNode() {

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return HIVBehaviourNode.TREATED.name();
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

	public BehaviourNode getStandardEntryPoint() {
		return this.exposedNode;
	}

	public double getHIV_vertical_transmission() {
		// this is going to be dependent on if the mother has is being treated for HIV infection, placeholder value is for none, see https://pmc.ncbi.nlm.nih.gov/articles/PMC11643157/
		return 0.334;
	}
	
}