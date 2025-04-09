package uk.ac.ucl.protecs.objects.diseases;

import uk.ac.ucl.protecs.objects.hosts.Host;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

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
	// clinical care
	double time_start_hospitalised;
	double time_end_hospitalised;
	
	// infection stages
	boolean hasAsympt = false;
	boolean hasMild = false;
	boolean hasSevere = false;
	boolean hasCritical = false;
	boolean hasRecovered = false;
	boolean isTheCauseOfDeath = false;
	// symptom manager
	boolean isSymptomatic = false;
	// test manager
	boolean tested = false;
	boolean testedPositive = false;
	boolean testLogged = false;
	boolean eligibleForTesting = false;
	// loggers
	boolean hasDeathLogged = false;
	boolean hasAsymptLogged = false;
	boolean hasMildLogged = false;
	boolean hasSevereLogged = false;
	boolean hasCriticalLogged = false;
	boolean hasLogged = false;
	
	
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
	
	// =============================================== Disease transmission ===========================================================================
	
	public abstract void horizontalTransmission();
	
	public abstract void verticalTransmission(Person baby);
	
	// =============================================== Disease type classification ===========================================================================			
	public abstract boolean isOfType(DISEASE disease);
	
	public abstract DISEASE getDiseaseType();
	
	public abstract String getDiseaseName();

	// =============================================== Disease progression ====================================================================================

	public void setAsympt() {
		this.hasAsympt = true;
		this.hasMild = false;
		this.hasSevere = false;
		this.hasCritical = false;
		this.hasRecovered = false;
		this.isSymptomatic = false;
	}
	
	public boolean hasAsympt() {return this.hasAsympt;}
	
	public void setSymptomatic() {
		this.isSymptomatic = !this.isSymptomatic;
		
	}
	
	public boolean isSymptomatic() {return this.isSymptomatic;}
	
	public void setMild() {
		this.hasAsympt = false;
		this.hasMild = true;
		this.hasSevere = false;
		this.hasCritical = false;
		this.hasRecovered = false;
	}
	
	public boolean hasMild() {
			return this.hasMild;
	}
	
	public void setSevere() {
		this.hasAsympt = false;
		this.hasMild = false;
		this.hasSevere = true;
		this.hasCritical = false;
		this.hasRecovered = false;

	}
	
	public boolean hasSevere() {
		return this.hasSevere;
	}
	
	public void setCritical() {
		this.hasAsympt = false;
		this.hasMild = false;
		this.hasSevere = false;
		this.hasCritical = true;
		this.hasRecovered = false;
	}
	
	public boolean hasCritical() {
		return this.hasCritical;
	}
	
	public void setRecovered() {
		this.hasAsympt = false;
		this.hasMild = false;
		this.hasSevere = false;
		this.hasCritical = false;
		this.hasRecovered = true;
		
	}
	
	public boolean hasRecovered() {
		return this.hasRecovered;
	}
	
	public void setAsCauseOfDeath() {
		this.isTheCauseOfDeath = true;
	};
	
	public boolean isCauseOfDeath() {
		return this.isTheCauseOfDeath;
	};
	// =============================================== Disease logging ====================================================================================
	
	public boolean getAsymptLogged() {
		return this.hasAsymptLogged;
	}
	
	public void confirmAsymptLogged() {
		this.hasAsymptLogged = true; 
	}

	public boolean getMildLogged() {
		return this.hasMildLogged;
	}

	public void confirmMildLogged() {
		this.hasMildLogged = true; 
		
	}

	public boolean getSevereLogged() {
		return this.hasSevereLogged;
	}

	public void confirmSevereLogged() {
		this.hasSevereLogged = true;
	}

	public boolean getCriticalLogged() {
		return this.hasCriticalLogged;
	}

	public void confirmCriticalLogged() {
		this.hasCriticalLogged = true;
	}

	public void confirmDeathLogged() {
		this.hasDeathLogged = true;
	}

	public boolean getDeathLogged() {
		return this.hasDeathLogged ;
	}

	public boolean getLogged() {
		return hasLogged;
	}
	
	public void confirmLogged() {
		this.hasLogged = true;
	}
	
	public abstract String writeOut();

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

}
