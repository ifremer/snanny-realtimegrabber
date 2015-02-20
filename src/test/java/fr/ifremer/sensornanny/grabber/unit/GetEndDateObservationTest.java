package fr.ifremer.sensornanny.grabber.unit;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import fr.ifremer.sensornanny.grabber.AbstractTest;
import fr.ifremer.sensornanny.grabber.io.couchbase.ConnectionManager;

public class GetEndDateObservationTest extends AbstractTest {

	@Test
	public void test() throws Exception {
		
		createLiveObservation(generatedUUID);
		
		sleep(3);
		
		assertEquals("endPostion of phenomenonTime should not be set for observation with id = " + generatedUUID, 0l, observationsDB.getEndDate(generatedUUID));
		
		final long now = System.currentTimeMillis();
		
		final String endDate = new SimpleDateFormat(DATE_FORMAT).format(new Date(now));
		final long endDateMillis = new SimpleDateFormat(DATE_FORMAT).parse(endDate).getTime();
		
		final JsonObject observation = observationsDB.getObservation(generatedUUID);
		if (observation.containsKey(ATTRIBUTE_FILE_JSON)) {
			JsonObject fileJSON = observation.getObject(ATTRIBUTE_FILE_JSON);
			if (fileJSON.containsKey(ATTRIBUTE_CHILD_NODES)) {
				for (Object each : fileJSON.getArray(ATTRIBUTE_CHILD_NODES)) {
					JsonObject element = (JsonObject) each;
					if (ATTRIBUTE_OM_PHENOMENON_TIME.equals(element.getString(ATTRIBUTE_TAG_NAME))) {
						JsonArray timePeriodArray = ((JsonObject) element.getArray(ATTRIBUTE_CHILD_NODES).get(0)).getArray(ATTRIBUTE_CHILD_NODES);
						for (Object each2 : timePeriodArray) {
							JsonObject element2 = (JsonObject) each2;
							if (ATTRIBUTE_GML_END_POSITION.equals(element2.getString(ATTRIBUTE_TAG_NAME))) {
								element2.put(ATTRIBUTE_CHILD_NODES, JsonArray.empty().add(endDate));
							}
						}
					}
				}
			}
		}
		JsonDocument document = JsonDocument.create(generatedUUID, observation);
		ConnectionManager.observations.upsert(document);
		
		sleep(3);
		
		assertEquals("endPostion of phenomenonTime should be set to " + endDate + " for observation with id = " + generatedUUID, endDateMillis, observationsDB.getEndDate(generatedUUID));
	}

}
