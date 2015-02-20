package fr.ifremer.sensornanny.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONML;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.SocketConnection;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.transcoder.JsonTranscoder;

import fr.ifremer.sensornanny.grabber.io.couchbase.ConnectionManager;
import fr.ifremer.sensornanny.grabber.io.couchbase.LiveObservationsDB;
import fr.ifremer.sensornanny.grabber.io.couchbase.ObservationsDB;
import fr.ifremer.sensornanny.grabber.tasks.FindLiveObservationsTest;
import fr.ifremer.sensornanny.grabber.tasks.scheduled.CleanupLiveObservations;

public abstract class AbstractTest implements OMAttributes {

	protected static final Logger logger = Logger.getLogger(CleanupLiveObservations.class.getName());
	
	protected String generatedUUID;
	protected JsonTranscoder jsonTranscoder = new JsonTranscoder();
	protected ObservationsDB observationsDB = new ObservationsDB();
	protected LiveObservationsDB liveObservationsDB = new LiveObservationsDB();

	protected LiveRestResultsStub liveRestResultsStub;
	private ContainerSocketProcessor liveRestResultsStubProcessor;
	private SocketConnection liveRestResultsStubConnection;
	private InetSocketAddress liveRestResultsStubAddress;
	
	@BeforeClass
	public static void initialize() {
		new ConnectionManager().contextInitialized(null);
		sleep(3);
	}

	@AfterClass
	public static void destroy() {
		new ConnectionManager().contextDestroyed(null);
	}
	
	@Before
	public void generateUUID() {
		generatedUUID = UUID.randomUUID().toString();
	}

	@After
	public void cleanupObservation() {
		if (generatedUUID != null) {
			ConnectionManager.observations.remove(generatedUUID);
			ConnectionManager.live_observations.remove(generatedUUID);
		}
		generatedUUID = null;
	}
	
	protected class LiveRestResultsStub implements Container {
		private boolean connectionProblem = false;
		
		public void handle(Request request, Response response) {
			
			if (connectionProblem) {
				throw new RuntimeException("connection problem");
			}
			
			try {
				
				if (!"/appli/tsg001".equals(request.getPath().getPath())) {
					response.close();
					return;
				}
				
				// From query parameter (optional)
				final String from = request.getQuery().get("from");
				long fromMillis = 0l;
				if (from != null) {
					fromMillis = new SimpleDateFormat(Configuration.getInstance().restServiceQueryTimeFormat()).parse(from).getTime();
				}

				// To query parameter (optional)
				final String to = request.getQuery().get("to");
				long toMillis = 0l;
				if (to != null) {
					toMillis = new SimpleDateFormat(Configuration.getInstance().restServiceQueryTimeFormat()).parse(to).getTime();
				}

				final long now = System.currentTimeMillis();
				if (toMillis == 0l) {
					toMillis = now;
				}

				Map<Long, List<String>> results = new TreeMap<Long, List<String>>();

				final int resultCount = (int) (5 + (Math.random() * 10));
				for (int i = 0; i < resultCount; i++) {
					long eventMillis = fromMillis + (long) (Math.random() * (toMillis - fromMillis));
					double temperature = (Math.random() * 5) + 16;
					double conductivity = (Math.random() * 50) + 50;

					List<String> row = new ArrayList<String>();
					row.add(new SimpleDateFormat(Configuration.getInstance().restServiceTimeFormat()).format(new Date(eventMillis)));
					row.add(String.valueOf(temperature));
					row.add(String.valueOf(conductivity));

					results.put(eventMillis, row);
				}

				response.setValue("Content-Type", "application/data-csv");
				PrintStream stream = response.getPrintStream();

				for (List<String> each : results.values()) {
					stream.println(String.join(",", each));
				}

				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void simulateInternetConnectionProblem() {
			this.connectionProblem = true;
		}

		public void noMoreInternetConnectionProblem() {
			this.connectionProblem = false;
		}
	}
	
	protected void startLiveRestResultsStub() {
		// Launch rest service stub on port 12345
		liveRestResultsStub = new LiveRestResultsStub();
		try {
			liveRestResultsStubProcessor = new ContainerSocketProcessor(liveRestResultsStub);
			liveRestResultsStubConnection = new SocketConnection(liveRestResultsStubProcessor);
			liveRestResultsStubAddress = new InetSocketAddress(12345);
			liveRestResultsStubConnection.connect(liveRestResultsStubAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void stopLiveRestResultsStub() {
		try {
			if (liveRestResultsStubConnection != null) {
				liveRestResultsStubConnection.close();
				liveRestResultsStubConnection = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void createLiveObservation(String id) {
		final String omXMLFile = "om_tsg.xml";
		
		InputStream omXMLStream = FindLiveObservationsTest.class.getClassLoader().getResourceAsStream(omXMLFile);
		Scanner omXMLScanner = null;
		
		try {
			omXMLScanner = new Scanner(omXMLStream, "UTF-8");
			String omXML = omXMLScanner.useDelimiter("\\A").next();
			String omJSON = JSONML.toJSONObject(omXML).toString();

			omJSON.replace("d8326d7e-815b-11e4-a0e2-5c260a184584", id);

			JsonObject fileJSON = null;
			try {
				fileJSON = jsonTranscoder.stringToJsonObject(omJSON);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to parse JSON generated from " + omXMLFile, e);
			}

			if (fileJSON != null) {
				JsonObject content = JsonObject.empty();
				content.put("Filejson", fileJSON);
				content.put("Authorname", "test");

				JsonDocument document = JsonDocument.create(id, content);
				ConnectionManager.observations.insert(document);
			}
		} finally {
			if (omXMLScanner != null) {
				omXMLScanner.close();
			}
		}
	}
	
	protected static void sleep(int seconds) {
		try { Thread.sleep(seconds * 1000); } catch (InterruptedException ignored) { }
	}
	
}
