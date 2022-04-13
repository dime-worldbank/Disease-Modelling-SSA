package sim;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class CrummyTest {
	
	public static void main(String [] args){

		try {
			String filename = args[0];
			BufferedWriter testFriend = new BufferedWriter(new FileWriter(filename));
			testFriend.write(filename);
			testFriend.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}