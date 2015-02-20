package fr.ifremer.sensornanny.grabber.tasks.scheduled;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.couchbase.client.java.document.json.JsonObject;

import fr.ifremer.sensornanny.grabber.io.couchbase.ObservationsDB;
import fr.ifremer.sensornanny.grabber.tasks.parallelized.FindLiveObservation;

public class FindLiveObservations implements Runnable {

	private static final Logger logger = Logger.getLogger(FindLiveObservations.class.getName());
	
	private ExecutorService findLiveObservationExecutor = Executors.newCachedThreadPool();
	
	private ObservationsDB observationsDB = new ObservationsDB();
	
	private boolean found = false;
	
	@Override
	public void run() {
		logger.info("Finding live observations ...");
		
		found = false;
		
		for (Object each : observationsDB.getLiveObservations()) {
			JsonObject liveObservation = (JsonObject) each;
			find(liveObservation);
		}
		
		if (!found) {
			logger.info("No live observation found");
		} else {
			try {
				findLiveObservationExecutor.awaitTermination(20, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Find Live Observation tasks execution timeout", e);
			}
		}
		
	}

	private void find(JsonObject liveObservation) {
		found = true;
		findLiveObservationExecutor.submit(new FindLiveObservation(liveObservation));
	}
			
}
