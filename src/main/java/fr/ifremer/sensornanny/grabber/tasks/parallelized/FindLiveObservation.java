package fr.ifremer.sensornanny.grabber.tasks.parallelized;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.couchbase.client.java.document.json.JsonObject;

import fr.ifremer.sensornanny.grabber.Configuration;
import fr.ifremer.sensornanny.grabber.MimetypeHelper;
import fr.ifremer.sensornanny.grabber.io.couchbase.LiveObservationsDB;
import fr.ifremer.sensornanny.grabber.io.couchbase.ObservationsDB;

public class FindLiveObservation implements Runnable {

	private static final Logger logger = Logger.getLogger(FindLiveObservation.class.getName());

	private ObservationsDB observationsDB = new ObservationsDB();
	private LiveObservationsDB liveObservationsDB = new LiveObservationsDB();

	private JsonObject liveObservation = null;

	public FindLiveObservation(JsonObject liveObservation) {
		this.liveObservation = liveObservation;
	}

	@Override
	public void run() {
		String id = liveObservation.getString("id");
		String resultPath = liveObservation.getString("result_path");
		String resultType = liveObservation.getString("result_type");
		String targetResultPath = computeTargetResultPath(id, resultPath, resultType);

		liveObservation.put("target_result_path", targetResultPath);

		observationsDB.updateResultPath(id, targetResultPath);

		liveObservationsDB.createLiveObservation(id, liveObservation);
		
		logger.info("Found live observation with id = " + id + " | result_path = " + resultPath + " | target_result_path = " + targetResultPath);
	}

	private String computeTargetResultPath(final String id, final String resultPath, final String resultType) {
		final String targetResultPathPattern = Configuration.getInstance().targetResultPathPattern();
		String targetResultPath = targetResultPathPattern;
		
		if (targetResultPath.contains("{resultsDirectory}")) {
			String resultsDirectory = FindLiveObservation.class.getClassLoader().getResource("results").getPath();
			if (resultsDirectory.substring(2, 3).equals(":") && resultsDirectory.startsWith("/")) {
				resultsDirectory = resultsDirectory.substring(1);
			}
			targetResultPath = targetResultPath.replace("{resultsDirectory}", resultsDirectory);
		}
		
		targetResultPath = targetResultPath.replace("{id}", id);
		
		if (targetResultPath.contains("{device}")) {
			String device = "";
			try {
				device = new URL(resultPath).getPath();
				device = resultPath.substring(resultPath.lastIndexOf('/') + 1, resultPath.length());
			} catch (MalformedURLException e) {
				logger.log(Level.SEVERE, "URL for observation with id = " + id + " is malformed", e);
			}
			targetResultPath = targetResultPath.replace("{device}", device);
		}
		
		if (targetResultPath.contains("{extension}")) {
			final String extension = MimetypeHelper.getExtension(resultType);
			targetResultPath = targetResultPath.replace("{extension}", extension);
		}
		
		return targetResultPath;
	}

}
