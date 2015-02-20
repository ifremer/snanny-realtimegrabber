package fr.ifremer.sensornanny.grabber.io.couchbase;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;

import fr.ifremer.sensornanny.grabber.Configuration;

public class SystemsDB {

	public JsonArray getSystems() {
		JsonArray result = JsonArray.empty();

		ViewQuery viewQuery = ViewQuery.from(Configuration.getInstance().systemsViewDesign(), Configuration.getInstance().systemsViewName());

		ViewResult viewResponse = ConnectionManager.systems.query(viewQuery);
		for (ViewRow row : viewResponse.allRows()) {

			Object value = row.value();
			
			/*
			if (!(value instanceof JsonObject)) {
				System.err.println(row.id() + " Value not a JSON Object: " + value);
				ConnectionManager.systems.remove(row.id());
				continue;
			}
			*/
			
			JsonObject parsedDoc = (JsonObject) value;

//			JsonObject system = JsonObject.empty().put("author", parsedDoc.getString("author")).put("date", row.key())
//					.put("description", parsedDoc.getString("description")).put("boundedBox", parsedDoc.getObject("boundedBox"));

			parsedDoc.put("id", row.id());
			
			result.add(parsedDoc);
		}

		return result;
	}

	public JsonObject getSystem(String id) {
		JsonDocument ret = ConnectionManager.systems.get(id);
		return ret.content();
	}

}
