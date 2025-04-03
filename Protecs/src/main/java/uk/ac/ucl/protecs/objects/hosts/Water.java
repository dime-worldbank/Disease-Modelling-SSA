package uk.ac.ucl.protecs.objects.hosts;

import java.util.HashSet;

import sim.engine.SimState;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.HOST;

public class Water extends Host {
	
	Location currentLocation;
	Location source;
	public Water(Location myLocation, Location mySource){
		currentLocation = myLocation;
		source = mySource;
	}
	
	

	@Override
	public void step(SimState arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getHostType() {	
			return HOST.WATER.key;
		}


}