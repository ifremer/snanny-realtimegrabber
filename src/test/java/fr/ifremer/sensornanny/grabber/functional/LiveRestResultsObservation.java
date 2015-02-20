package fr.ifremer.sensornanny.grabber.functional;

import org.junit.Test;

import fr.ifremer.sensornanny.grabber.AbstractTest;

public class LiveRestResultsObservation extends AbstractTest {

	@Test
	public void test() throws Exception {
		
		startLiveRestResultsStub();
		System.out.println("You will have to close rest service stub on port 12345 mannually");
		
		sleep(24 * 3600);
		
		generatedUUID = null; // No automatic deletion
	}

}
