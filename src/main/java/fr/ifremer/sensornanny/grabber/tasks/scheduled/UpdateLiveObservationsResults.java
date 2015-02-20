package fr.ifremer.sensornanny.grabber.tasks.scheduled;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.couchbase.client.java.document.json.JsonObject;

import fr.ifremer.sensornanny.grabber.io.couchbase.LiveObservationsDB;
import fr.ifremer.sensornanny.grabber.tasks.parallelized.UpdateLiveObservationResults;

public class UpdateLiveObservationsResults implements Runnable {

	private static final Logger logger = Logger.getLogger(UpdateLiveObservationsResults.class.getName());
	
	private ExecutorService updateLiveObservationResultsExecutor = Executors.newCachedThreadPool();
	
	private LiveObservationsDB liveObservationsDB = new LiveObservationsDB();
	
	private boolean found = false;
	
	@Override
	public void run() {
		logger.info("Updating live observations results ...");
		
		found = false;
		
		for (Object each : liveObservationsDB.getLiveObservations()) {
			JsonObject liveObservation = (JsonObject) each;
			update(liveObservation);
		}
		
		if (!found) {
			logger.info("No live observation to update");
		} else {
			try {
				updateLiveObservationResultsExecutor.awaitTermination(20, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Update Live Observation Results tasks execution timeout", e);
			}
		}
		
	}

	private void update(JsonObject liveObservation) {
		found = true;
		updateLiveObservationResultsExecutor.submit(new UpdateLiveObservationResults(liveObservation));
	}
	
}
