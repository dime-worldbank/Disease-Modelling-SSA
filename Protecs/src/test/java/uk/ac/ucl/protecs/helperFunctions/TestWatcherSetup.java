package uk.ac.ucl.protecs.helperFunctions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public abstract class TestWatcherSetup {

    protected static final String PARAMS_DIR = "src/test/resources/";

    protected int seed;
    protected Random random;

    @Rule
    public TestName testName = new TestName();

    /**
     * Each test provides its own params file name that is overwritten in the test files
     */
    protected abstract String getParams();
    
    // Similarly each test file will specify the output file name
    
    protected abstract String getOutputFileName();


    @Rule
    public TestWatcher watcher = new TestWatcher() {

        private String timestamp() {
            return LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        }

        private void logResult(String result, String extra) {
            try (FileWriter writer =
                         new FileWriter(getOutputFileName(), true)) {

                writer.write(
                        timestamp()
                                + " | Test: " + testName.getMethodName()
                                + " | Params: " + getParams() + ".txt"
                                + " | Seed: " + seed
                                + " | RESULT: " + result
                                + (extra != null ? " | " + extra : "")
                                + "\n"
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
    public void setupSeed() {
        seed = new Random().nextInt();
        random = new Random(seed);
    }
}