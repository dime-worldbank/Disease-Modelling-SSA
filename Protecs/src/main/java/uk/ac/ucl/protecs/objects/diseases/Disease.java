package uk.ac.ucl.protecs.objects.diseases;

import uk.ac.ucl.protecs.objects.hosts.Host;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.HOST;
import sim.engine.Steppable;
import uk.ac.ucl.swise.behaviours.BehaviourNode;

public abstract class Disease implements Steppable {
	
	public Host host;
	public Host source;
	public Location infectedAtLocation;
	WorldBankCovid19Sim myWorld;

	// behaviours
	BehaviourNode currentBehaviourNode = null;

	// infection timekeeping
	// default these to max value so it's clear when they've been reset
	public double time_infected = Double.MAX_VALUE;
	public double time_contagious = Double.MAX_VALUE;		
	public double time_start_symptomatic = Double.MAX_VALUE;
	public double time_start_severe = Double.MAX_VALUE;
	public double time_start_critical = Double.MAX_VALUE;
	public double time_recovered = 	Double.MAX_VALUE;
	public double time_died = Double.MAX_VALUE;
	public double time_susceptible = Double.MAX_VALUE;
	// clinical care
	double time_start_hospitalised = Double.MAX_VALUE;
	double time_end_hospitalised = Double.MAX_VALUE;
	
	// infection stages
	public enum DISEASESTAGE{
		PRESYMPTOMATIC, ASYMPTOMATIC, MILD, SEVERE, CRITICAL, RECOVERED, SUSCEPTIBLE, CAUSEOFDEATH, NA;
	};
	DISEASESTAGE diseaseStage;
	// symptom manager
	boolean isSymptomatic = false;
	// test manager
	boolean tested = false;
	boolean testedPositive = false;
	boolean testLogged = false;
	boolean eligibleForTesting = false;
	// loggers
	boolean hasStageLogged = false;
	boolean stageLogged = false;
	boolean hasLogged = false;
	boolean isInfectionActive = false;


	// =============================================== relevant information on the host ==============================================================
	public Host getHost() {return this.host;}
	
	public Host getSource() {return this.source;}
	
	public Location infectedAt() {return this.infectedAtLocation;}
	
	public double getStartTime() {return this.time_infected;}
			
	public String getCurrentAdminZone() {return ((Person) this.getHost()).getHomeLocation().getRootSuperLocation().myId;}
	
	public boolean isHostAlive() {return ((Person) this.getHost()).isAlive();}
	
	public int getHostAge() {return ((Person) this.getHost()).getAge();}
	
	public  SEX getHostSex() {return ((Person) this.getHost()).getSex();}
	
	public OCCUPATION getHostEconStatus() {return ((Person) this.getHost()).getEconStatus();}
		
	// =============================================== Disease 'behaviours'================================================================================
	public BehaviourNode getCurrentBehaviourNode() {return this.currentBehaviourNode;}
	
	public void setBehaviourNode(BehaviourNode bn){
		this.currentBehaviourNode = bn;
	}
	
	public String getBehaviourName(){
		if(this.currentBehaviourNode == null) return "";
		return this.currentBehaviourNode.getTitle();
	}
	
	public abstract boolean isInfectious();
	
	public abstract boolean isWaterborne();
	
	public boolean isInHumanHost() {
		return this.getHost().isOfType(HOST.PERSON);
	}
	
	// =============================================== Disease transmission ===========================================================================
	
	public abstract void horizontalTransmission();
	
	public abstract void verticalTransmission(Person baby);
	
	// =============================================== Disease type classification ===========================================================================			
	public abstract boolean isOfType(DISEASE disease);
	
	public abstract DISEASE getDiseaseType();
	
	public abstract String getDiseaseName();

	// =============================================== Disease progression ====================================================================================
	
	public DISEASESTAGE getDiseaseStage() {
		return this.diseaseStage;
	}
	public void setDiseaseStage(DISEASESTAGE stage) {
		this.diseaseStage = stage;
		if (stage == DISEASESTAGE.ASYMPTOMATIC) {
			this.isSymptomatic = false;
		}
		if ((stage == DISEASESTAGE.SUSCEPTIBLE) || (stage == DISEASESTAGE.PRESYMPTOMATIC) || (stage == DISEASESTAGE.NA)) {
			setInfectionActive(false);
		}
		resetStageLogged();
	}
	
	public boolean hasDiseaseStage(DISEASESTAGE stage) {
		
		return stage.equals(this.getDiseaseStage());
	}
	
	public void setSymptomatic() {
		this.isSymptomatic = !this.isSymptomatic;
		
	}
	
	public boolean isSymptomatic() {return this.isSymptomatic;}

	// =============================================== Disease logging ====================================================================================
	public boolean getStageLogged() {
		return hasStageLogged;
	}
	
	public void confirmStageLogged() {
		this.hasStageLogged = true;
	}
	
	public void resetStageLogged() {
		this.hasStageLogged = false;
	}
	
	public boolean getLogged() {
		return hasLogged;
	}
	
	public void confirmLogged() {
		this.hasLogged = true;
	}
	
	public void resetLogged() {
		this.hasLogged = false;
	}
	
	public abstract String writeOut();
	
	
	public boolean isInfectionActive() {
		return isInfectionActive;
	}

	public void setInfectionActive(boolean isActive) {
		this.isInfectionActive = isActive;
	}

	// =============================================== Disease testing ====================================================================================
	public boolean isEligibleForTesting() {
		return this.eligibleForTesting;
	}

	public void setEligibleForTesting() {
		this.eligibleForTesting = true;
	}
	
	public void removeEligibilityForTesting() {
		this.eligibleForTesting = false;
	}
	
	public boolean hasBeenTested() {
		return this.tested;
	}

	public void setTested() {
		this.tested = !this.tested;
	}

	public void setTestedPositive() {
		this.testedPositive = true;
	}

	public boolean hasTestedPositive() {
		return this.testedPositive;
	}
	
	public abstract boolean inATestingAdminZone();

	public boolean getTestLogged() {
		return this.testLogged;
	}

	public void confirmTestLogged() {
		this.testLogged = true;
	}
	public void resetPropertiesPostRecovery() {
		// reset generic disease properties
		this.time_infected = Double.MAX_VALUE;
		this.time_contagious = Double.MAX_VALUE;		
		this.time_start_symptomatic = Double.MAX_VALUE;
		this.time_start_severe = Double.MAX_VALUE;
		this.time_start_critical = Double.MAX_VALUE;
		this.time_recovered = 	Double.MAX_VALUE;
		this.time_died = Double.MAX_VALUE;
		this.time_susceptible = Double.MAX_VALUE;
		// reset clinical care
		this.time_start_hospitalised = Double.MAX_VALUE;
		this.time_end_hospitalised = Double.MAX_VALUE;
		// reset generic infection stages
		this.diseaseStage = DISEASESTAGE.NA;
		this.setInfectionActive(false);
		// reset symptom manager
		this.isSymptomatic = false;
		// reset test manager
		this.tested = false;
		this.testedPositive = false;
		this.testLogged = false;
		this.eligibleForTesting = false;
		// reset loggers
		this.hasStageLogged = false;
		this.hasLogged = false;		
	}
}
