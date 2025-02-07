package uk.ac.ucl.protecs.objects.diseases;

import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.Person.SEX;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import sim.engine.Steppable;
import swise.behaviours.BehaviourNode;

public interface Infection extends Steppable {

	// reporters
	public Person getHost();
	public Person getSource();
	public Location infectedAt();
	public double getStartTime();
	public String getDiseaseName();
	// behaviours
	public BehaviourNode getCurrentBehaviourNode();
	public void setBehaviourNode(BehaviourNode bn);
	public String getBehaviourName();
	
	// output
	public String writeOut();
	
	public String getCurrentAdminZone();
	
	public boolean isAlive();
	
	public int getAge();
	
	public SEX getSex();
	
	public OCCUPATION getEconStatus();
	
	public boolean getDeathLogged();

	public boolean isCovidSpuriousSymptom();
	
	public void confirmDeathLogged();

	public boolean covidLogCheck ();
	
	public boolean isCovid();
	
	public boolean hasRecovered();
	
	public void setAsympt();
	
	public boolean hasAsympt();
	
	public boolean getAsymptLogged();
	
	public void confirmAsymptLogged();
	
	public void setMild();
	
	public boolean hasMild();
	
	public boolean getMildLogged();
	
	public void confirmMildLogged();

	public boolean hasSevere();
	
	public boolean getSevereLogged();
	
	public void confirmSevereLogged();
	
	public boolean hasCritical();
	
	public boolean getCriticalLogged();
	
	public void confirmCriticalLogged();
	
	public boolean getLogged();
	
	public void setLogged();
	
	public void setSymptomatic();
	
	public boolean isSymptomatic();
}
