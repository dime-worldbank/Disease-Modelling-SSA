package uk.ac.ucl.protecs.sim.loggers;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class CholeraLogging {
	
	// place holder for the disease logging that is non-generic
	public static Steppable ResetCholeraLoggedProperties(WorldBankCovid19Sim world) {
		return new Steppable() {			
			@Override
			public void step(SimState arg0) {
					// to make sure deaths and cases aren't counted multiple times, update this person's properties
					for (Disease i: world.human_infections) {
							if (i.isOfType(DISEASE.CHOLERA)) {
							i.confirmLogged();
						}
					} 
				}
			};
	}
}