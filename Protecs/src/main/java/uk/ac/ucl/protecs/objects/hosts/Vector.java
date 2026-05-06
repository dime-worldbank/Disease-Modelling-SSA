package uk.ac.ucl.protecs.objects.hosts;

import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.HOST;

public class Vector extends Host {

	@Override
	public String getHostType() {
		return HOST.VECTOR.key;
	}

	@Override
	public boolean isOfType(HOST host) {
		
		return (host.equals(HOST.VECTOR));
	}
	
}