package uk.ac.ucl.protecs.helperFunctions;

import java.util.ArrayList;

import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class helperFunctions {
	
	public static WorldBankCovid19Sim CreateDummySim(long seed, String paramsFilename, boolean demography) {
		Params p = new Params(paramsFilename, false);
		WorldBankCovid19Sim myWorld = new WorldBankCovid19Sim(seed, p, "", demography);
		return myWorld;
	}
	
	public static void setParameterListsToValue(WorldBankCovid19Sim world, ArrayList <Double> list_to_change, double value_to_set) {
		int list_index = 0;
		for (double entry: list_to_change) {
			list_to_change.set(list_index, value_to_set);
			list_index ++;
		}
	}
	
}