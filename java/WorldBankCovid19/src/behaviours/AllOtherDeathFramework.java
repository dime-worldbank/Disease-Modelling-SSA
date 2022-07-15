package behaviours;

import objects.Person;
import sim.WorldBankCovid19Sim;
import sim.engine.Steppable;


public class AllOtherDeathFramework extends BehaviourFramework {
	WorldBankCovid19Sim myWorld;
	BehaviourNode survivingNode = null, otherCauseDeathNode = null;
	public AllOtherDeathFramework(WorldBankCovid19Sim model){
		myWorld = model;
		
		// the default status
		survivingNode = new BehaviourNode(){

			@Override
			public String getTitle() { return "Alive"; }

			@Override
			public double next(Steppable s, double time) {
				Person p = (Person) s;
				// decide whether this person will die of a non-covid cause by age and gender
				if(p.getSex() == "male") {
					double myMortalityLikelihood = myWorld.params.getAllCauseLikelihoodByAge(
							myWorld.params.prob_death_by_age_male, p.getAge());
				}
				else {
					double myMortalityLikelihood = myWorld.params.getAllCauseLikelihoodByAge(
							myWorld.params.prob_death_by_age_female, p.getAge());
				}
				
				return Double.MAX_VALUE;

			}
		};
			
		// the agent has been exposed - determine whether the infection will develop
		otherCauseDeathNode = new BehaviourNode(){
			@Override
			public String getTitle() { return "Alive"; }
			@Override
			public double next(Steppable s, double time) {
				return Double.MAX_VALUE;
				
			}
		};
	}
		
}