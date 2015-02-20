package fr.ifremer.sensornanny.grabber.tasks.parallelized;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.couchbase.client.java.document.json.JsonObject;

import fr.ifremer.sensornanny.grabber.Configuration;
import fr.ifremer.sensornanny.grabber.io.couchbase.LiveObservationsDB;

public class UpdateLiveObservationResults implements Runnable {

	private static final Logger logger = Logger.getLogger(UpdateLiveObservationResults.class.getName());
	
	private LiveObservationsDB liveObservationsDB = new LiveObservationsDB();
	
	private JsonObject liveObservation = null;
	
	public UpdateLiveObservationResults(JsonObject liveObservation) {
		this.liveObservation = liveObservation;
	}
	
	@Override
	public void run() {
		String id = liveObservation.getString("id");
		
		long to = System.currentTimeMillis();
		long from = liveObservationsDB.getLastRetrieved(id);
		
		if (from == 0l) {
			Long begin = liveObservation.getLong("begin");
			if (begin != null) {
				from = begin;
			}
		}
		
		try {
			long lastRetrieved = getResultsSinceLastRetrieved(liveObservation, from, to);
			if (lastRetrieved != 0l) {
				liveObservationsDB.updateLastRetrieved(id, lastRetrieved);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to get results for live observation with id = " + id, e);
		}
		
	}
	
	private String retrieve(String serviceURL, String targetResultPath) throws Exception {
		String lastRetrieved = null;

		HttpURLConnection connection = null;
		try {
			// Connection to rest service
			URL url = new URL(serviceURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));

			// File in append mode
			File resultFile = new File(targetResultPath);
			resultFile.getParentFile().mkdirs(); // create parent directories if necessary
			boolean newFile = !resultFile.exists();
			FileOutputStream fos = new FileOutputStream(resultFile, true); // append
			BufferedWriter wd = new BufferedWriter(new OutputStreamWriter(fos));
			
			// Put header
			if (newFile && "application/data-csv".equals(liveObservation.getString("result_type"))) {
				wd.append(String.join(",", liveObservationsDB.getCSVHeader(liveObservation.getString("id"))));
				wd.newLine();
			}

			String line;
			while ((line = rd.readLine()) != null) {
				wd.append(line);
				wd.newLine();

				// FIXME: configure how to get time value in stream (for CSV which index ? which token ?)
				lastRetrieved = line.split(",")[0];
			}

			wd.close();
			rd.close();

		} finally {
			if(connection != null) {
				connection.disconnect(); 
			}
		}

		return lastRetrieved;
	}

	private long getResultsSinceLastRetrieved(JsonObject liveObservation, long from, long to) {
		long lastRetrieved = 0l;
		final String resultPath = liveObservation.getString("result_path");
		final String targetResultPath = liveObservation.getString("target_result_path");
		
		String fromQueryParam = null;
		if (from != 0l) {
			fromQueryParam = new SimpleDateFormat(Configuration.getInstance().restServiceQueryTimeFormat()).format(new Date(from));
		}

		String toQueryParam = null;
		if (to != 0l) {
			toQueryParam = new SimpleDateFormat(Configuration.getInstance().restServiceQueryTimeFormat()).format(new Date(to));
		}
		
		StringBuilder serviceURL = new StringBuilder();
		serviceURL.append(resultPath);
		if (fromQueryParam != null || toQueryParam != null) {
			serviceURL.append("?");
		}
		if (fromQueryParam != null && toQueryParam != null) {
			serviceURL.append("from=");
			serviceURL.append(fromQueryParam);
			serviceURL.append("&to=");
			serviceURL.append(toQueryParam);
		} else if (fromQueryParam != null) {
			serviceURL.append("from=");
			serviceURL.append(fromQueryParam);
		} else if (toQueryParam != null) {
			serviceURL.append("to=");
			serviceURL.append(toQueryParam);
		}
		
		try {
			String lastRetrievedStr = retrieve(serviceURL.toString(), targetResultPath);
			if (lastRetrievedStr != null) {
				lastRetrieved = new SimpleDateFormat(Configuration.getInstance().restServiceTimeFormat()).parse(lastRetrievedStr).getTime();
			}
			logger.info("Successfully retrieved results calling " + serviceURL + " in order to append in " + targetResultPath + " for observation with id = " + liveObservation.getString("id"));
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to retrieve results calling " + serviceURL + " in order to append in " + targetResultPath + " for observation with id = " + liveObservation.getString("id"), e);
		}
		
		return lastRetrieved;
	}
	
}
