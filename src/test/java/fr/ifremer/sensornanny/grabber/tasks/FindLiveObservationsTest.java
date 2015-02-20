package fr.ifremer.sensornanny.grabber.tasks;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.couchbase.client.java.document.json.JsonObject;

import fr.ifremer.sensornanny.grabber.AbstractTest;
import fr.ifremer.sensornanny.grabber.tasks.scheduled.FindLiveObservations;

public class FindLiveObservationsTest extends AbstractTest {

	@Test
	public void test() throws Exception {
		
		createLiveObservation(generatedUUID);
		
		sleep(5);
		
		FindLiveObservations findLiveObservations = new FindLiveObservations();
		findLiveObservations.run();
		
		sleep(5);
		boolean found = false;
		for (Object each : liveObservationsDB.getLiveObservations()) {
			JsonObject observation = (JsonObject) each;
			if (generatedUUID.equals(observation.getString("id"))) {
				found = true;
				break;
			}
		}
		
		assertTrue("Live Observation with id = " + generatedUUID + " not found in live_observation couchbase bucket", found);
	}

}
