package fr.ifremer.sensornanny.grabber.tasks.parallelized;

import java.util.logging.Logger;

import com.couchbase.client.java.document.json.JsonObject;

import fr.ifremer.sensornanny.grabber.io.couchbase.LiveObservationsDB;
import fr.ifremer.sensornanny.grabber.io.couchbase.ObservationsDB;

public class CleanupLiveObservation implements Runnable {

	private static final Logger logger = Logger.getLogger(CleanupLiveObservation.class.getName());
	
	private ObservationsDB observationsDB = new ObservationsDB();
	
	private LiveObservationsDB liveObservationsDB = new LiveObservationsDB();
	
	private JsonObject liveObservation = null;
	
	public CleanupLiveObservation(JsonObject liveObservation) {
		this.liveObservation = liveObservation;
	}
	
	@Override
	public void run() {
		String id = liveObservation.getString("id");
		Long lastRetrieved = liveObservation.getLong("last_retrieved");
		
		long endDate = observationsDB.getEndDate(id);
		
		if (endDate != 0l && lastRetrieved != null && endDate < lastRetrieved) {
			liveObservationsDB.delete(id);
			logger.info("Cleaned live observation with id = " + id);
		}
		
	}

}
