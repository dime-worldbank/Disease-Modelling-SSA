package uk.ac.ucl.protecs.sim;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.objects.hosts.Person;

//================================================ Testing =======================================================================
//===== Here we test that the params.java is loading in information needed for the simulation, can run without certain parameter =
//===== files, such as those relating to demography and COVID testing. We also test that the simulation flags up failed parameter=
//===== 'load ins' by raising an assert statement ================================================================================ 
//================================================================================================================================

@RunWith(Parameterized.class)

public class ParamsTesting {
	

	private String params;
	
	public ParamsTesting(String fileName) {
		this.params = fileName;
	}
	private final static String paramsDir = "src/test/resources/";
	
	@Rule
	public TestName testName = new TestName();

	protected int seed;
	protected Random random;
	

	
	@Rule
	public TestWatcher watcher = new TestWatcher() {

	    private String timestamp() {
	        return LocalDateTime.now()
	            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
	    }

	    private void logResult(String result, String extra) {
	        try (FileWriter writer = new FileWriter("params-testing-test-seeds.log", true)) {
	            writer.write(
	                timestamp() +
	                " | Test: " + testName.getMethodName() +
	                " | Params: " + params + ".txt" +
	                " | Seed: " + seed +
	                " | RESULT: " + result +
	                (extra != null ? " | " + extra : "") +
	                "\n"
	            );
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    @Override
	    protected void succeeded(Description description) {
	        logResult("PASSED", null);
	    }

	    @Override
	    protected void failed(Throwable e, Description description) {
	        logResult("FAILED", "Error: " + e.getMessage());
	    }
	};
	
	@Before
	public void setupSeed() throws IOException {
		seed = new java.util.Random().nextInt();;

	    random = new Random(seed);
	}
	@Test
	public void testCommunityLocationWorks() {
		this.params = "params_community_locations";
		// Create the simulation object with the older style census
		int seed = (int) this.seed;		

		// set up the simulation
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_community_locations.txt");
		// attempt to load that object in to the simulation
		sim.start();
		Assert.assertTrue(sim.params.communityLocations.size() > 0);
	}
	
	@Test
	public void testOldStyleCensusStillLoads() {
		this.params = "params_old_census";
		// Create the simulation object with the older style census
		int seed = (int) this.seed;		

		// set up the simulation
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_old_census.txt");
		// attempt to load that object in to the simulation
		try {
			sim.start();
			HelperFunctions.runSimulation(sim, 10);
		}
		catch (Exception e) {
			Assert.fail();
		}
		// Check the default of 'None' is present
		for (Person p: sim.agents) {
			Assert.assertTrue(p.getWorkplace() == null);
		}
	}
	
	@Test
	public void testdailyTransitionWeekdayAndHomeRegionColumnOrderIsMutable() {
		this.params = "params_testing_odm_order";
		// Create the simulation object with column order 'weekday,home_region,...' in the ODMs
		int seed = (int) this.seed;		

		// set up the simulation
		WorldBankCovid19Sim original_order_sim = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_testing_odm_order.txt");
		original_order_sim.start();
		// Create the simulation with column order 'home_region,weekday,...' in the ODMs

		// set up the simulation
		WorldBankCovid19Sim alternative_order_sim = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_testing_odm_order_alt.txt");
		alternative_order_sim.start();
		// check that regardless of column order of the first two columns in the csv file, the ODMs are the same for both lockdown and non-lockdown 
		boolean ld_odms_equal = original_order_sim.params.dailyTransitionLockdownProbs.equals(alternative_order_sim.params.dailyTransitionLockdownProbs);
		boolean nld_odms_equal = original_order_sim.params.dailyTransitionPrelockdownProbs.equals(alternative_order_sim.params.dailyTransitionPrelockdownProbs);
		// test whether both of these conditions are met
		Assert.assertTrue(ld_odms_equal & nld_odms_equal);
	}
	
	@Test
	public void testSimStartsWithoutDemographyFilenames() {
		this.params = "params_testing_no_demography";
		// Create the simulation object without loading in demography related filenames
		int seed = (int) this.seed;		

		// set up the simulation
		WorldBankCovid19Sim sim_no_demog_files = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_testing_no_demography.txt");
		// start the simulation, which triggers the loading in of parameters
		sim_no_demog_files.start();
		// Check that the birth_rate_filename and all_cause_mortality_filename have stayed as their default value.
		Assert.assertTrue((sim_no_demog_files.params.birth_rate_filename == null) & (sim_no_demog_files.params.all_cause_mortality_filename == null));
	}
	
	@Test
	public void testSimRunsWithoutDemographyFilenames() {
		this.params = "params_testing_no_demography";
		// Create the simulation object without loading in demography related filenames
		int seed = (int) this.seed;		

		// set up the simulation
		WorldBankCovid19Sim sim_no_demog_files = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_testing_no_demography.txt");
		// wrap simulation running in a try catch statement
		try {
			// initialise and simulation
			sim_no_demog_files.start();
			HelperFunctions.runSimulation(sim_no_demog_files, 10);
		}
		catch (Exception e) {
			// error has been found, update run_without_issue
			Assert.fail();
		}
		
	}
	
	@Test
	public void testSimStartsWithoutLockdownFilenames() {
		this.params = "params_testing_no_lockdown_filename";
		// Create the simulation object without loading in lockdown triggering related filenames
		int seed = (int) this.seed;		

		// set up the simulation
		WorldBankCovid19Sim sim_no_lockdown_trigger_files = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_testing_no_lockdown_filename.txt");
		// start the simulation, which triggers the loading in of parameters
		sim_no_lockdown_trigger_files.start();
		
		// Check that the lockdown_changeList_filename has stayed as their default value.
		Assert.assertTrue(sim_no_lockdown_trigger_files.params.lockdown_changeList_filename == null);
	}
	
	@Test
	public void testSimRunsWithoutLockdownFilenames() {
		this.params = "params_testing_no_lockdown_filename";
		// Create the simulation object without loading in lockdown triggering related filenames
		int seed = (int) this.seed;		

		// set up the simulation
		WorldBankCovid19Sim sim_no_lockdown_trigger_files = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_testing_no_lockdown_filename.txt");
		// wrap simulation running in a try catch statement
		try {
			sim_no_lockdown_trigger_files.start();
			HelperFunctions.runSimulation(sim_no_lockdown_trigger_files, 10);
		}
		catch (Exception e) {
			Assert.fail();
		}
	}
	

	@Test
	public void testSimStartsWithoutCovidTestingFilenames() {
		this.params = "params_testing_no_covid_testing_file";
		// Create the simulation object without loading in lockdown triggering related filenames
		int seed = (int) this.seed;		

		// set up the simulation
		WorldBankCovid19Sim sim_no_covid_testing_files = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_testing_no_covid_testing_file.txt");
		// start the simulation, which triggers the loading in of parameters
		sim_no_covid_testing_files.start();
		
		// Check that the lockdown_changeList_filename has stayed as their default value.
		Assert.assertTrue(sim_no_covid_testing_files.params.lockdown_changeList_filename == null);
	}
	
	@Test
	public void testSimRunsWithoutCovidTestingFilenames() {
		this.params = "params_testing_no_covid_testing_file";
		// Create the simulation object without loading in lockdown triggering related filenames
		int seed = (int) this.seed;		

		// set up the simulation
		WorldBankCovid19Sim sim_no_covid_testing_files = HelperFunctions.CreateDummySimWithSeed(seed, paramsDir + "params_testing_no_covid_testing_file.txt");
		// wrap simulation running in a try catch statement
		try {
			sim_no_covid_testing_files.start();
			HelperFunctions.runSimulation(sim_no_covid_testing_files, 10);
		}
		catch (Exception e) {
			Assert.fail();
		}
		
	}
	
	// run reject faulty files in bulk
	// params_w_faulty_ODM.txt
	// params_w_faulty_econ_status_movement_prob.txt
	// params_w_faulty_linelist.txt
	// params_w_faulty_inf_transitions.txt
	// params_w_faulty_covid_test_numbers.txt
	// params_w_faulty_covid_test_locations.txt
	// params_w_faulty_all_cause_mortality.txt
	// params_w_faulty_birthrate.txt
	@Test
	public void testParamsWillRejectFaultyInputData() {	
		// create a boolean to indicate if an error was found whilst running the simulation, we expect this to change to false
		boolean ran_without_issue = true;
		// wrap simulation running in a try catch statement
		try {
			// Create the simulation object with one faulty data
			int seed = (int) this.seed;		
			WorldBankCovid19Sim sim_should_raise_exception = HelperFunctions.CreateDummySimWithSeed(seed, params);
			sim_should_raise_exception.start();
			HelperFunctions.runSimulation(sim_should_raise_exception, 10);
		}
		catch (java.lang.AssertionError e) {
			// Assert flag raised, update run_without_issue
			ran_without_issue = false;
		}
		// Check that the simulation ran WITH an issue
		Assert.assertFalse(ran_without_issue);
	}
	
	@Parameterized.Parameters
	public static List<String> params() {
	    return Arrays.asList(
	            new String[]{paramsDir + "params_w_faulty_ODM.txt", paramsDir + "params_w_faulty_econ_status_movement_prob.txt",  
	            		paramsDir + "params_w_faulty_linelist.txt", paramsDir + "params_w_faulty_inf_transitions.txt", 
	            		paramsDir + "params_w_faulty_covid_test_numbers.txt", paramsDir + "params_w_faulty_covid_test_locations.txt",
	            		paramsDir + "params_w_faulty_all_cause_mortality.txt", paramsDir + "params_w_faulty_birthrate.txt"
	            		}
	    
	    );
	}
}
