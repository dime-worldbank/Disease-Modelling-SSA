package uk.ac.ucl.protecs.behaviours.diseaseSpread;

import sim.engine.Steppable;


public interface InfectiousSpreadFramework extends Steppable {
	
	void horizontalTransmission(float beta);
	
	void verticalTransmission();

}