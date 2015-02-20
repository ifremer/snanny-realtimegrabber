package fr.ifremer.sensornanny.grabber.functional;

import org.junit.Test;

import fr.ifremer.sensornanny.grabber.AbstractTest;

public class AddNewLiveObservation extends AbstractTest {

	@Test
	public void test() throws Exception {
		
		startLiveRestResultsStub();
		System.out.println("You will have to close rest service stub on port 12345 mannually");
		
		createLiveObservation(generatedUUID);
		System.out.println("You will have to remove snanny_observation and snanny_live_observation with id = " + generatedUUID + " mannually");
		
		sleep(3600);
		
		generatedUUID = null; // No automatic deletion
	}

}
