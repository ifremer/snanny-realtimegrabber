package fr.ifremer.sensornanny.grabber.io.couchbase;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;

import fr.ifremer.sensornanny.grabber.Configuration;

public class ConnectionManager implements ServletContextListener {

	public static Cluster cluster;
	public static Bucket systems;
	public static Bucket observations;
	public static Bucket live_observations;

	private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.log(Level.INFO, "Connecting to Couchbase Cluster");
		cluster = CouchbaseCluster.create(Configuration.getInstance().cluster());
		systems = cluster.openBucket(Configuration.getInstance().systemsBucket(), Configuration.getInstance().systemsBucketPassword());
		observations = cluster.openBucket(Configuration.getInstance().observationsBucket(), Configuration.getInstance().observationsBucketPassword());
		live_observations = cluster.openBucket(Configuration.getInstance().liveObservationsBucket(), Configuration.getInstance().liveObservationsBucketPassword());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.log(Level.INFO, "Disconnecting from Couchbase Cluster");
		cluster.disconnect();
	}

}