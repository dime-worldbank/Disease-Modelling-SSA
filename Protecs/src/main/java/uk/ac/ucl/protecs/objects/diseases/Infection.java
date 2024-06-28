package uk.ac.ucl.protecs.objects.diseases;

import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import sim.engine.SimState;
import sim.engine.Steppable;
import swise.behaviours.BehaviourFramework;
import swise.behaviours.BehaviourNode;

public interface Infection extends Steppable {

	// reporters
	public Person getHost();
	public Person getSource();
	public Location infectedAt();
	public double getStartTime();

	// behaviours
	public BehaviourNode getCurrentBehaviourNode();
	public void setBehaviourNode(BehaviourNode bn);
	public String getBehaviourName();
	
	// output
	public String writeOut();
	
}
