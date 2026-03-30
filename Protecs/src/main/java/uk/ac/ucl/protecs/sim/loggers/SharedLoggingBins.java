package uk.ac.ucl.protecs.sim.loggers;


public class SharedLoggingBins {
	
    long[] alive;      // indexed by age
    long[] dead;       // indexed by age
    long[] aliveCumulative;
    long[] deadCumulative;
}