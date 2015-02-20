package fr.ifremer.sensornanny.grabber.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.ifremer.sensornanny.grabber.AbstractTest;

public class UpdateResultPathTest extends AbstractTest {

	@Test
	public void test() throws Exception {
		
		createLiveObservation(generatedUUID);
		
		sleep(3);
		
		final String originalResultPath = observationsDB.getResultPath(generatedUUID);

		sleep(3);
		
		observationsDB.updateResultPath(generatedUUID, "test");
		assertEquals("ResultPath incorrect for id = " + generatedUUID, "test", observationsDB.getResultPath(generatedUUID));

		sleep(3);
		
		observationsDB.updateResultPath(generatedUUID, originalResultPath);
		assertEquals("ResultPath incorrect for id = " + generatedUUID, originalResultPath, observationsDB.getResultPath(generatedUUID));
		
	}

}
