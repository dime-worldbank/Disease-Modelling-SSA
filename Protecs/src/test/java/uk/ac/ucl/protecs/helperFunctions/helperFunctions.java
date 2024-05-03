package uk.ac.ucl.protecs.helperFunctions;

import java.util.ArrayList;
import java.util.Random;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class helperFunctions {
	
	public static WorldBankCovid19Sim CreateDummySim(String paramsFilename, boolean demography) {
		Random rand = new Random();
		int seed = rand.nextInt(100000000);
		return CreateDummySimWithSeed(seed, paramsFilename, demography);
	}
	public static WorldBankCovid19Sim CreateDummySimWithSeed(int seed, String paramsFilename, boolean demography) {
		System.out.println("Running with seed = " + String.valueOf(seed));
		Params p = new Params(paramsFilename, false);
		return new WorldBankCovid19Sim(seed, p, "", demography);
	}
	
	public static void setParameterListsToValue(WorldBankCovid19Sim world, ArrayList <Double> list_to_change, double value_to_set) {
		int list_index = 0;
		for (double entry: list_to_change) {
			list_to_change.set(list_index, value_to_set);
			list_index ++;
		}
	}
	
}