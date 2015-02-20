package fr.ifremer.sensornanny.grabber.io.couchbase;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.Stale;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;

import fr.ifremer.sensornanny.grabber.Configuration;
import fr.ifremer.sensornanny.grabber.OMAttributes;

public class LiveObservationsDB implements OMAttributes {
	
	private static final Logger logger = Logger.getLogger(LiveObservationsDB.class.getName());

	public JsonArray getLiveObservations() {
		JsonArray result = JsonArray.empty();
		
		ViewQuery viewQuery = ViewQuery.from(Configuration.getInstance().liveObservationsViewDesign(), Configuration.getInstance().liveObservationsViewName()).stale(Stale.FALSE);
		
		ViewResult viewResponse = ConnectionManager.live_observations.query(viewQuery);
		for (ViewRow row : viewResponse.allRows()) {
			
			JsonObject liveObservation = (JsonObject) row.value();
			liveObservation.put("id", row.id());
			
			result.add(liveObservation);
		}
		
		return result;
	}
	
	public void createLiveObservation(String id, JsonObject liveObservation) {
		JsonDocument liveObservationDocument = JsonDocument.create(id, liveObservation);
		ConnectionManager.live_observations.insert(liveObservationDocument);
		logger.info("Created liveObsevation with id = " + id);
	}
	
	public String[] getCSVHeader(String id) {
		List<String> columns = new ArrayList<String>();
		JsonObject liveObservation = getLiveObservation(id);
		
		JsonArray resultArray = liveObservation.getArray("result");
		for (Object each : resultArray) {
			JsonObject sweDataArrayNode = (JsonObject) each;
			if (ATTRIBUTE_SWE_DATA_ARRAY.equals(sweDataArrayNode.getString(ATTRIBUTE_TAG_NAME))) {
				for (Object each2 : sweDataArrayNode.getArray(ATTRIBUTE_CHILD_NODES)) {
					JsonObject sweResultChildNode = (JsonObject) each2;
					if (ATTRIBUTE_SWE_ELEMENT_TYPE.equals(sweResultChildNode.getString(ATTRIBUTE_TAG_NAME))) {
						for (Object each3 : sweResultChildNode.getArray(ATTRIBUTE_CHILD_NODES)) {
							JsonObject sweDataRecordNode = (JsonObject) each3;
							if (ATTRIBUTE_SWE_DATA_RECORD.equals(sweDataRecordNode.getString(ATTRIBUTE_TAG_NAME))) {
								for (Object each4 : sweDataRecordNode.getArray(ATTRIBUTE_CHILD_NODES)) {
									JsonObject sweFieldNode = (JsonObject) each4;
									String sweLabel = getSWELabel(sweFieldNode);
									if (sweLabel != null) {
										columns.add(sweLabel);
									}
								}
							}
						}
					}
				}
			}
		}
		
		return columns.toArray(new String[columns.size()]);
	}
	
	private String getSWELabel(JsonObject sweField) {
		String ret = null;
		
		for (Object each : sweField.getArray(ATTRIBUTE_CHILD_NODES)) {
			JsonObject sweQuantity = (JsonObject) each;
			if (ATTRIBUTE_SWE_QUANTITY.equals(sweQuantity.getString(ATTRIBUTE_TAG_NAME))) {
				for (Object each2 : sweQuantity.getArray(ATTRIBUTE_CHILD_NODES)) {
					JsonObject sweLabel = (JsonObject) each2;
					if (ATTRIBUTE_SWE_LABEL.equals(sweLabel.getString(ATTRIBUTE_TAG_NAME))) {
						ret = sweLabel.getArray(ATTRIBUTE_CHILD_NODES).getString(0);
						break;
					}
				}
			}
		}
		
		return ret;
	}
	
	public long getLastRetrieved(String id) {
		JsonObject liveObservation = getLiveObservation(id);
		
		Long lastRetrieved = liveObservation.getLong("last_retrieved");
		if (lastRetrieved == null) {
			lastRetrieved = 0l;
		}
		
		return lastRetrieved;
	}
	
	public void updateLastRetrieved(String id, long last_retrieved) {
		JsonObject liveObservation = getLiveObservation(id);
		liveObservation.put("last_retrieved", last_retrieved);
		ConnectionManager.live_observations.upsert(JsonDocument.create(id, liveObservation));
	}
	
	public void delete(String id) {
		ConnectionManager.live_observations.remove(id);
	}

	public JsonObject getLiveObservation(String id) {
		JsonDocument ret = ConnectionManager.live_observations.get(id);
		return ret.content();
	}

}
