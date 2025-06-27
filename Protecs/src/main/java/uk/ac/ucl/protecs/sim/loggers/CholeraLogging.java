package uk.ac.ucl.protecs.sim.loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.sim.ImportExport;
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
							if(!i.isHostAlive()) {
								i.confirmDeathLogged();
							}
							if(i.hasAsympt() & !i.getAsymptLogged()) {
								i.confirmAsymptLogged();
							}
							if(i.hasMild() & !i.getMildLogged()) {
								i.confirmMildLogged();
							}
							if(i.hasSevere() & !i.getSevereLogged()) {
								i.confirmSevereLogged();
							}
							if(i.hasCritical() & !i.getCriticalLogged()) {
								i.confirmCriticalLogged();
							}
							if (i.hasRecovered() & !i.getRecoveredLogged()) {
								i.confirmRecoveredLogged();
							}
							if (i.hasSusceptible() & !i.getSusceptibleLogged()) {
								i.confirmSusceptibleLogged();
							}
							i.confirmLogged();
						}
					} 
				}
			};
	}
}