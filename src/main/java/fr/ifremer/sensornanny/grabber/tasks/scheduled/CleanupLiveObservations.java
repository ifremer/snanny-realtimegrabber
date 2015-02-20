package fr.ifremer.sensornanny.grabber.tasks.scheduled;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.couchbase.client.java.document.json.JsonObject;

import fr.ifremer.sensornanny.grabber.io.couchbase.LiveObservationsDB;
import fr.ifremer.sensornanny.grabber.tasks.parallelized.CleanupLiveObservation;

public class CleanupLiveObservations implements Runnable {

	private static final Logger logger = Logger.getLogger(CleanupLiveObservations.class.getName());
	
	private ExecutorService cleanupLiveObservationExecutor = Executors.newCachedThreadPool();
	
	private LiveObservationsDB liveObservationsDB = new LiveObservationsDB();
	
	private boolean found = false;
	
	@Override
	public void run() {
		logger.info("Cleaning live observations ...");
		
		found = false;
		
		for (Object each : liveObservationsDB.getLiveObservations()) {
			JsonObject liveObservation = (JsonObject) each;
			cleanup(liveObservation);
		}
		
		if (!found) {
			logger.info("No live observation to clean");
		} else {
			try {
				cleanupLiveObservationExecutor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Cleanup Live Observation tasks execution timeout", e);
			}
		}
	}

	private void cleanup(JsonObject liveObservation) {
		found = true;
		cleanupLiveObservationExecutor.submit(new CleanupLiveObservation(liveObservation));
	}
	
}
