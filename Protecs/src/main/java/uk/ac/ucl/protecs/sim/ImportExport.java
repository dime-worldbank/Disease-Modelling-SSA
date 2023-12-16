package uk.ac.ucl.protecs.sim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import uk.ac.ucl.protecs.objects.Infection;
import uk.ac.ucl.protecs.objects.Location;
import uk.ac.ucl.protecs.objects.Person;

public class ImportExport {

	public static void exportSimInformation(WorldBankCovid19Sim world, String sim_info_filename, long seed, int numAgents, int targetDuration) {
		// Write the following information to a .txt file: Seed, number of agents, simulation duration
		// TODO: discuss what else would be useful for the output here
		try {
		System.out.println("Printing out SIMULATION INFORMATION to " + sim_info_filename);
		
		// Create new buffered writer to store this information in
		BufferedWriter exportFile = new BufferedWriter(new FileWriter(sim_info_filename, true));
		// write a new heading 
		exportFile.write("Seed\tNumberOfAgents\tSimuilationDuration"
				+ "\n");
		// Create variable rec to store the information
		String rec = "";
		// get and record the simulation seed
		rec += seed + "\t";
		// get and record the number of agents
		rec += numAgents + "\t";
		// get and record the simulation duration
		rec += targetDuration + "\t";
		
		exportFile.write(rec);
		exportFile.close();
		
		} catch (Exception e) {
			System.err.println("File input error: " + sim_info_filename);
		}

		
	}
	
	public static void reportOnInfected(ArrayList <Person> agents){
		String makeTerribleGraphFilename = "nodes_latest_16.gexf";
		try {
			
			System.out.println("Printing out infects? from " + makeTerribleGraphFilename);
			
			// shove it out
			BufferedWriter badGraph = new BufferedWriter(new FileWriter(makeTerribleGraphFilename));

			//badGraph.write("ID;econ;age;infect;time;source");
			badGraph.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gexf xmlns=\"http://www.gexf.net/1.1draft\" version=\"1.1\">\n" + 
					"<graph mode=\"static\" defaultedgetype=\"directed\">\n" + 
					"<attributes class=\"node\" type=\"static\"> \n" +
				     "<attribute id=\"infected\" title=\"Infected\" type=\"string\"/>\n</attributes>\n");
			badGraph.write("<nodes>\n");
			for(Person p: agents){
				String myStr = p.toString();
				//myStr += ";" + p.getEconStatus() + ";" + p.getAge() + ";" + p.getInfectStatus();
				
				if(p.getInfection() != null){
					Person source = p.getInfection().getSource();
					String sourceName = null;
					if(source != null)
						sourceName = source.toString();
					//myStr += ";" + p.getInfection().getStartTime() + ";" + sourceName;
					myStr = p.getInfection().getBehaviourName();
				}
				else
					//myStr += "Susceptible;;";
					myStr = "Susceptible";
/*				for(Person op: p.getWorkBubble()){
					myStr += ";" + op.toString();
				}
	*/			
				badGraph.write("\t<node id=\"" + p.getID() + "\" label=\"" + p.toString() + 
						"\"> <attvalue for=\"infected\" value=\"" +myStr +  "\"/></node>\n");

				//badGraph.write("\n" + myStr);
			}
			badGraph.write("</nodes>\n");
			badGraph.write("<edges>\n");
			for(Person p: agents){
				int myID = p.getID();
				for(Person op: p.getWorkBubble()){
					badGraph.write("\t<edge source=\"" + myID + "\" target=\"" + op.getID() + "\" weight=\"1\" />\n");
				}
			}
			
			badGraph.write("</edges>\n");
			badGraph.write("</graph>\n</gexf>");
			badGraph.close();
		} catch (Exception e) {
			System.err.println("File input error: " + makeTerribleGraphFilename);
		}
	}
	
	public static void exportMe(String filename, String output, long timer){
		try {
			
			// shove it out
			BufferedWriter exportFile = new BufferedWriter(new FileWriter(filename, true));
			if(timer > 0)
				exportFile.write(timer + "\n");
			exportFile.write(output);
			exportFile.close();
		} catch (Exception e) {
			System.err.println("File input error: " + filename);
		}
	}
	
	public static void exportDailyReports(Params params, String filename, ArrayList <HashMap <String, Double>> dailyRecord, long seed){
		try {
			
			System.out.println("Printing out infects? from " + filename);
			
			// shove it out
			BufferedWriter exportFile = new BufferedWriter(new FileWriter(filename, true));

			String header = "index\t";
			for(int p = 0; p < params.exportParams.length; p++){
				header += params.exportParams[p].toString() + "\t";
			}
			exportFile.write(header);
			
			for(int i = 0; i < dailyRecord.size(); i++){
				HashMap <String, Double> myRecord = dailyRecord.get(i);
				String s = seed + "\t";
				for(String paramName: params.exportParams){
					s += myRecord.get(paramName).toString() + "\t";
				}
				exportFile.write("\n" + s);
			}
			exportFile.close();
		} catch (Exception e) {
			System.err.println("File input error: " + filename);
		}
	}
	
	public static void exportInfections(String infections_export_filename, ArrayList <Infection> infections) {
		try {
			
			System.out.println("Printing out INFECTIONS to " + infections_export_filename);
			
			// shove it out
			BufferedWriter exportFile = new BufferedWriter(new FileWriter(infections_export_filename, true));
			exportFile.write("Host\tSource\tTime\tLocOfTransmission" + 
					"\tContagiousAt\tSymptomaticAt\tSevereAt\tCriticalAt\tRecoveredAt\tDiedAt\tYLD\tYLL\tDALYs\tNTimesInfected"
					+ "\n");
			
			// export infection data
			for(Infection i: infections) {
				
				String rec = i.getHost().getID() + "\t";
				
				// infected by:
				
				Person source = i.getSource();
				if(source == null)
					rec += "null";
				else
					rec += source.getID();
				
				rec += "\t" + i.getStartTime() + "\t";
				
				// infected at:
				
				Location loc = i.getInfectedAtLocation();
				
				if(loc == null)
					rec += "SEEDED";
				else if(loc.getRootSuperLocation() != null)
					rec += loc.getRootSuperLocation().getId();
				else
					rec += loc.getId();
				
				// progress of disease: get rid of max vals
				
				if(i.time_contagious == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_contagious;
				
				if(i.time_start_symptomatic == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_start_symptomatic;
				
				if(i.time_start_severe == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_start_severe;
				
				if(i.time_start_critical == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_start_critical;
				
				if(i.time_recovered == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_recovered;
				
				if(i.time_died == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_died;
				// create variables to calculate DALYs, set to YLD zero as default
				double yld = 0.0;
				// DALY weights are taken from https://www.ssph-journal.org/articles/10.3389/ijph.2022.1604699/full , exact same DALY weights used 
				// here https://www.ncbi.nlm.nih.gov/pmc/articles/PMC8212397/ and here https://www.ncbi.nlm.nih.gov/pmc/articles/PMC8844028/ , seems like these are common
				// TODO: check if these would be representative internationally
				// TODO: Find DALYs from long COVID
				double critical_daly_weight = 0.655;
				double severe_daly_weight = 0.133;
				double mild_daly_weight = 0.051;

				// calculate DALYs part 1: YLD working from the most serious level of infection
				// YLD = fraction of year with condition * DALY weight
				if (i.time_start_critical < Double.MAX_VALUE)
					// calculate yld between the onset of critical illness to death or recovery
					if (i.time_died < Double.MAX_VALUE)
						yld += ((i.time_died - i.time_start_critical) / 365) * critical_daly_weight;
					else if (i.time_recovered < Double.MAX_VALUE)
						yld += ((i.time_recovered - i.time_start_critical) / 365) * critical_daly_weight;
				if (i.time_start_severe < Double.MAX_VALUE)
					// calculate yld between the progression from a severe case to a critical case or recovery
					if (i.time_start_critical < Double.MAX_VALUE)
						yld += ((i.time_start_critical - i.time_start_severe) / 365) * severe_daly_weight;
					else if (i.time_recovered < Double.MAX_VALUE)
						yld += ((i.time_recovered - i.time_start_severe) / 365) * severe_daly_weight;
				if (i.time_start_symptomatic < Double.MAX_VALUE)
					// calculate yld between the onset of symptoms to progression to severe case or recovery
					if (i.time_start_severe < Double.MAX_VALUE)
						yld += ((i.time_start_severe - i.time_start_symptomatic) / 365) * mild_daly_weight;
					else if (i.time_recovered < Double.MAX_VALUE)
						yld += ((i.time_recovered - i.time_start_symptomatic) / 365) * mild_daly_weight;
				if(yld == 0.0)
					rec += "\t-";
				else
					rec += "\t" + (double) yld;
				// calculate YLL (basic)
				// YLL = Life expectancy in years - age at time of death, if age at death < Life expectancy else 0
				int lifeExpectancy = 62;  // according to world bank estimate https://data.worldbank.org/indicator/SP.DYN.LE00.IN?locations=ZW
				double yll = 0;
				if(i.time_died == Double.MAX_VALUE)
					rec += "\t-";
				else {
					yll = lifeExpectancy - i.getHost().getAge();
					// If this person's age is greater than the life expectancy of Zimbabwe, then assume there are no years of life lost
					if (yll < 0)
						yll = 0;
					rec += "\t" + (double) yll;
				}
				// Recored DALYs (YLL + YLD)
				if (yll + yld == 0.0)
					rec += "\t-";
				else
					rec += "\t" + (double) (yll + yld);
				// record number of times with covid
				rec += "\t" + i.getHost().getNumberOfTimesInfected();
				
				rec += "\n";
				
				exportFile.write(rec);
				
			}
			
			exportFile.close();
		} catch (Exception e) {
			System.err.println("File input error: " + infections_export_filename);
		}

	}
	
}