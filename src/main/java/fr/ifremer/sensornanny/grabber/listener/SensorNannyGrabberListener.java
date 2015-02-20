package fr.ifremer.sensornanny.grabber.listener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import fr.ifremer.sensornanny.grabber.tasks.scheduled.FindLiveObservations;
import fr.ifremer.sensornanny.grabber.tasks.scheduled.UpdateLiveObservationsResults;

public class SensorNannyGrabberListener implements ServletContextListener {
	
	private static final Logger logger = Logger.getLogger(SensorNannyGrabberListener.class.getName());

	private ScheduledExecutorService findLiveObservationsExecutor = Executors.newSingleThreadScheduledExecutor();
	private ScheduledExecutorService updateLiveObservationsResultsExecutor = Executors.newSingleThreadScheduledExecutor();
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("Starting tasks");
		
		FindLiveObservations findLiveObservations = new FindLiveObservations();
		findLiveObservationsExecutor.scheduleAtFixedRate(findLiveObservations, 5, 30, TimeUnit.SECONDS);
		
		UpdateLiveObservationsResults updateLiveObservationsResults = new UpdateLiveObservationsResults();
		updateLiveObservationsResultsExecutor.scheduleAtFixedRate(updateLiveObservationsResults, 5, 30, TimeUnit.SECONDS);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("Stopping tasks");
		findLiveObservationsExecutor.shutdown();
		updateLiveObservationsResultsExecutor.shutdown();
	}

}
