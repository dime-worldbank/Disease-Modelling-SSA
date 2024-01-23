package main.java.behaviours;

import main.java.behaviours.*;
import main.java.objects.*;
import main.java.sim.*;
import sim.engine.Steppable;
import swise.behaviours.BehaviourFramework;
import swise.behaviours.BehaviourNode;

public class InfectiousBehaviourFramework extends BehaviourFramework {
  WorldBankCovid19Sim myWorld;
  
  BehaviourNode susceptibleNode = null, exposedNode = null, presymptomaticNode = null, asymptomaticNode = null;
  
  BehaviourNode mildNode = null;
  
  BehaviourNode severeNode = null;
  
  BehaviourNode criticalNode = null;
  
  BehaviourNode recoveredNode = null;
  
  BehaviourNode deadNode = null;
  
  public InfectiousBehaviourFramework(WorldBankCovid19Sim model) {
    myWorld = model;
    susceptibleNode = new BehaviourNode() {
        public String getTitle() {
          return "susceptible";
        }
        
        public double next(Steppable s, double time) {
          return Double.MAX_VALUE;
        }

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
      };
    exposedNode = new BehaviourNode() {
        public String getTitle() {
          return "exposed";
        }
        
        public double next(Steppable s, double time) {
          Infection i = (Infection)s;
          if (i.time_contagious < Double.MAX_VALUE) {
            if (time < i.time_contagious)
              return i.time_contagious - time; 
            double mySymptLikelihood = myWorld.params.getLikelihoodByAge(
                myWorld.params.infection_p_sym_by_age, i.getHost().getAge());

            if (myWorld.random.nextDouble() < mySymptLikelihood) {
              i.setBehaviourNode(presymptomaticNode);
              (i.getHost().getLocation().getRootSuperLocation()).metric_new_cases_sympt++;
              i.getHost().storeCovid();
            } else {
              i.setBehaviourNode(asymptomaticNode);
              if (i.getHost() != null && i.getHost().getLocation() != null) {
                (i.getHost().getLocation().getRootSuperLocation()).metric_new_cases_asympt++;
                i.getHost().storeCovid();
              } else {
                System.out.println("PROBLEM WITH INFECTION HOST OR LOCATION");
              } 
            } 
            return 1.0D;
          } 
          double mySusceptLikelihood = myWorld.params.getLikelihoodByAge(
              myWorld.params.infection_r_sus_by_age, i.getHost().getAge());
          if (myWorld.random.nextDouble() < mySusceptLikelihood) {
            i.time_infected = time;
            
            double timeUntilInfectious = myWorld.nextRandomLognormal(
                myWorld.params.exposedToInfectious_mean, 
                myWorld.params.exposedToInfectious_std);
            i.time_contagious = time + timeUntilInfectious;
            return timeUntilInfectious;
          } 
          i.time_recovered = time;
          i.setBehaviourNode(susceptibleNode);
          return Double.MAX_VALUE;
        }

		@Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
      };
    presymptomaticNode = new BehaviourNode() {
        public String getTitle() {
          return "presymptomatic";
        }
        
        public double next(Steppable s, double time) {
          Infection i = (Infection)s;
          i.getHost().infectNeighbours();
          if (!i.getHost().hasPresymptCovid())
            i.getHost().setPresympt(); 
          if (time >= i.time_start_symptomatic) {
            i.setBehaviourNode(mildNode);
          } else if (i.time_start_symptomatic == Double.MAX_VALUE) {
            double time_until_symptoms = myWorld.nextRandomLognormal(
                myWorld.params.infectiousToSymptomatic_mean, 
                myWorld.params.infectiousToSymptomatic_std);
            i.time_start_symptomatic = time + time_until_symptoms;
          } 
          return 1.0D;
        }
        @Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
      };
    asymptomaticNode = new BehaviourNode() {
        public String getTitle() {
          return "asymptomatic";
        }
        
        public double next(Steppable s, double time) {
          Infection i = (Infection)s;
          i.getHost().infectNeighbours();
          if (!i.getHost().hasAsymptCovid())
            i.getHost().setAsympt(); 
          if (time >= i.time_recovered) {
            i.setBehaviourNode(recoveredNode);
          } else if (i.time_recovered == Double.MAX_VALUE) {
            double time_until_recovered = myWorld.nextRandomLognormal(
                myWorld.params.asymptomaticToRecovery_mean, 
                myWorld.params.asymptomaticToRecovery_std);
            i.time_recovered = time + time_until_recovered;
          } 
          return 1.0D;
        }
        @Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
      };
    mildNode = new BehaviourNode() {
        public String getTitle() {
          return "mild_case";
        }
        
        public double next(Steppable s, double time) {
          Infection i = (Infection)s;
          i.getHost().infectNeighbours();
          if (i.getHost().hasPresymptCovid())
            i.getHost().removePresympt(); 
          if (!i.getHost().hasMild()) {
            i.getHost().setMild();
            i.getHost().elligableForTesting();
          } 
          if (time >= i.time_recovered) {
            i.setBehaviourNode(recoveredNode);
          } else {
            if (time >= i.time_start_severe) {
              i.setBehaviourNode(severeNode);
              Person p = i.getHost();
              (p.getLocation().getRootSuperLocation()).metric_new_hospitalized++;
              p.setMobility(false);
              p.sendHome();
              return 1.0D;
            } 
            if (i.time_recovered == Double.MAX_VALUE && i.time_start_severe == Double.MAX_VALUE) {
              double mySevereLikelihood = myWorld.params.getLikelihoodByAge(
                  myWorld.params.infection_p_sev_by_age, i.getHost().getAge());
              if (myWorld.random.nextDouble() < mySevereLikelihood) {
                double time_until_severe = myWorld.nextRandomLognormal(
                    myWorld.params.symptomaticToSevere_mean, 
                    myWorld.params.symptomaticToSevere_std);
                i.time_start_severe = time + time_until_severe;
              } else {
                double time_until_recovered = myWorld.nextRandomLognormal(
                    myWorld.params.sympomaticToRecovery_mean, 
                    myWorld.params.sympomaticToRecovery_std);
                i.time_recovered = time + time_until_recovered;
                return 1.0D;
              } 
            } 
          } 
          return 1.0D;
        }
        @Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
      };
    severeNode = new BehaviourNode() {
        public String getTitle() {
          return "severe_case";
        }
        
        public double next(Steppable s, double time) {
          Infection i = (Infection)s;
          i.getHost().infectNeighbours();
          if (i.getHost().hasMild())
            i.getHost().removeMild(); 
          if (!i.getHost().hasSevere())
            i.getHost().setSevere(); 
          if (time >= i.time_recovered) {
            i.setBehaviourNode(recoveredNode);
          } else {
            if (time >= i.time_start_critical) {
              i.setBehaviourNode(criticalNode);
              (i.getHost().getLocation().getRootSuperLocation()).metric_new_critical++;
              return 1.0D;
            } 
            if (i.time_recovered == Double.MAX_VALUE && i.time_start_critical == Double.MAX_VALUE) {
              double myCriticalLikelihood = myWorld.params.getLikelihoodByAge(
                  myWorld.params.infection_p_cri_by_age, i.getHost().getAge());
 
              if (myWorld.random.nextDouble() < myCriticalLikelihood) {
                double time_until_critical = myWorld.nextRandomLognormal(
                    myWorld.params.severeToCritical_mean, 
                    myWorld.params.severeToCritical_std);

                i.time_start_critical = time + time_until_critical;
              } else {
                double time_until_recovered = myWorld.nextRandomLognormal(
                    myWorld.params.severeToRecovery_mean, 
                    myWorld.params.severeToRecovery_std);
                i.time_recovered = time + time_until_recovered;
                return 1.0D;
              } 
            } 
          } 
          return 1.0D;
        }
        @Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
      };
    criticalNode = new BehaviourNode() {
        public String getTitle() {
          return "critical_case";
        }
        
        public double next(Steppable s, double time) {
          Infection i = (Infection)s;
          i.getHost().infectNeighbours();
          if (i.getHost().hasSevere())
            i.getHost().removeSevere(); 
          if (!i.getHost().hasCritical())
            i.getHost().setCritical(); 
          if (time >= i.time_recovered) {
            i.setBehaviourNode(recoveredNode);
          } else {
            if (time >= i.time_died) {
              i.setBehaviourNode(deadNode);
              if (!i.getHost().isDead()) {
                Location myDistrict = i.getHost().getLocation().getRootSuperLocation();
                myDistrict.metric_died_count++;
                myDistrict.metric_new_deaths++;
              } else {
                System.out.println("hmm how did you get here?");
              } 
              return 1.0D;
            } 
            if (i.time_recovered == Double.MAX_VALUE && i.time_died == Double.MAX_VALUE) {
              double myDeathLikelihood = myWorld.params.getLikelihoodByAge(
                  myWorld.params.infection_p_dea_by_age, i.getHost().getAge());
     
              if (myWorld.random.nextDouble() < myDeathLikelihood) {
                double time_until_death = myWorld.nextRandomLognormal(
                    myWorld.params.criticalToDeath_mean, 
                    myWorld.params.criticalToDeath_std);
                i.time_died = time + time_until_death;
              } else {
                double time_until_recovered = myWorld.nextRandomLognormal(
                    myWorld.params.criticalToRecovery_mean, 
                    myWorld.params.criticalToRecovery_std);
                i.time_recovered = time + time_until_recovered;
                return 1.0D;
              } 
            } 
          } 
          return 1.0D;
        }
        @Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
      };
    recoveredNode = new BehaviourNode() {
        public String getTitle() {
          return "recovered";
        }
        
        public double next(Steppable s, double time) {
          Infection i = (Infection)s;
          i.time_recovered = time;
          (i.getHost().getLocation().getRootSuperLocation()).metric_new_recovered++;
          i.getHost().setRecovered();
          i.getHost().removeCovid();
          i.getHost().notElligableForTesting();
          if (i.getHost().isImmobilised()) {
            i.getHost().setMobility(true);
            myWorld.schedule.scheduleOnce((Steppable)i.getHost());
          } 
          return Double.MAX_VALUE;
        }
        @Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
      };
    deadNode = new BehaviourNode() {
        public String getTitle() {
          return "dead";
        }
        
        public double next(Steppable s, double time) {
          Infection i = (Infection)s;
          i.getHost().die();
          i.time_died = time;
          return Double.MAX_VALUE;
        }
        @Override
		public boolean isEndpoint() {
			// TODO Auto-generated method stub
			return false;
		}
      };
    this.entryPoint = this.exposedNode;
  }
  
  public BehaviourNode getStandardEntryPoint() {
    return this.susceptibleNode;
  }
  
  public BehaviourNode getInfectedEntryPoint(Location l) {
    if (this.myWorld.random.nextDouble() < 0.5D) {
      (l.getRootSuperLocation()).metric_new_cases_sympt++;
      return this.presymptomaticNode;
    } 
    (l.getRootSuperLocation()).metric_new_cases_asympt++;
    return this.asymptomaticNode;
  }
}
