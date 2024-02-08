package uk.ac.ucl.protecs.helperFunctions;

import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class helperFunctions {
	
	public static WorldBankCovid19Sim CreateDummySim(long seed, String paramsFilename, boolean demography) {
		Params p = new Params(paramsFilename, false);
		WorldBankCovid19Sim myWorld = new WorldBankCovid19Sim(seed, p, "", demography);
		return myWorld;
	}
}