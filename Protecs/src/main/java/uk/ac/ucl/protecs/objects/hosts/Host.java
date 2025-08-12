package uk.ac.ucl.protecs.objects.hosts;

import java.util.HashMap;

import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.HOST;
import uk.ac.ucl.swise.agents.MobileAgent;



public abstract class Host extends MobileAgent implements Steppable  {
	
	// personal ID to distinguish from other agents
	Location currentLocation;
	HashMap <String, Disease> myDiseaseSet;
	
	// copy of world
	public WorldBankCovid19Sim myWorld;
	
	// spatial functions
	public void setLocation(Location l, Host h) {
		if(this.currentLocation != null)
			currentLocation.removeHost(this);
		this.currentLocation = l;
		l.addHost(this);
	};
	
	public double transferTo(Location l){
		if(currentLocation != null) {
			currentLocation.removeHost(this);
			}
		currentLocation = l;
		if(l != null)
			l.addHost(this);
		return 1; // TODO make based on distance travelled!
	}
	
	public Location getLocation(){ return currentLocation;}
		
	public abstract String getHostType();
	
	
	public void addDisease(Disease i) {
		
		this.myDiseaseSet.put(i.getDiseaseType().key, i);
	};
	public HashMap<String, Disease> getDiseaseSet() {return this.myDiseaseSet; }
	
	public abstract boolean isOfType(HOST host);
	
	public boolean hasADisease() {
		return this.myDiseaseSet.size() > 0;
	}

}
