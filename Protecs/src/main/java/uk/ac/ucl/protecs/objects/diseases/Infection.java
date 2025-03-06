package uk.ac.ucl.protecs.objects.diseases;

import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import sim.engine.Steppable;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public interface Infection extends Steppable {

	// =============================================== relevant information on the host ==============================================================
	public Person getHost();
	
	public Person getSource();
	
	public Location infectedAt();
	
	public double getStartTime();
			
	public String getCurrentAdminZone();
	
	public boolean isHostAlive();
	
	public int getHostAge();
	
	public SEX getHostSex();
	
	public OCCUPATION getHostEconStatus();
		
	// =============================================== Disease 'behaviours'================================================================================
	public BehaviourNode getCurrentBehaviourNode();
	
	public void setBehaviourNode(BehaviourNode bn);
	
	public String getBehaviourName();
	
	// =============================================== Disease type classification ===========================================================================			
	public boolean isOfType(DISEASE disease);
	
	public DISEASE getDiseaseType();
	
	public String getDiseaseName();

	// =============================================== Disease progression ====================================================================================

	public void setAsympt();
	
	public boolean hasAsympt();
	
	public void setSymptomatic();
	
	public boolean isSymptomatic();
	
	public void setMild();
	
	public boolean hasMild();
	
	public void setSevere();
	
	public boolean hasSevere();
	
	public void setCritical();
	
	public boolean hasCritical();
	
	public void setRecovered();
	
	public boolean hasRecovered();
	
	public void setAsCauseOfDeath();
	
	public boolean isCauseOfDeath();
	// =============================================== Disease logging ====================================================================================
	
	public boolean getAsymptLogged();
	
	public void confirmAsymptLogged();
		
	public boolean getMildLogged();
	
	public void confirmMildLogged();
	
	public boolean getSevereLogged();
	
	public void confirmSevereLogged();
	
	public boolean getCriticalLogged();
	
	public void confirmCriticalLogged();
	
	public boolean getDeathLogged();
	
	public void confirmDeathLogged();
		
	public boolean getLogged();
	
	public void confirmLogged();
	
	public String writeOut();

	// =============================================== Disease testing ====================================================================================
	// filtering and setting who should be tested
	public boolean isEligibleForTesting();
	
	public void setEligibleForTesting();

	public void removeEligibilityForTesting();
	
	public boolean hasBeenTested();
	
	public void setTested();
	
	public void setTestedPositive();
	
	public boolean hasTestedPositive();
	
	public boolean inATestingAdminZone();

	// testing logging
	public boolean getTestLogged();
	
	public void confirmTestLogged();

}
