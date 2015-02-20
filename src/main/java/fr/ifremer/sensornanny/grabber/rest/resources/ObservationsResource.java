package fr.ifremer.sensornanny.grabber.rest.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import fr.ifremer.sensornanny.grabber.io.couchbase.ObservationsDB;

@Path(ObservationsResource.PATH)
public class ObservationsResource {

	private static final Logger logger = Logger.getLogger(ObservationsResource.class.getName());

	private ObservationsDB db = new ObservationsDB();

	public static final String PATH = "/observations";

	/**
	 * Get observation by id
	 * 
	 * @param id
	 *            id of the observation
	 * @return an observation in JSON format
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Object getObservationById(@PathParam("id") String id) {
		try {
			return db.getObservation(id);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while retrieving observation with id = " + id, e);
		}

		return "Error while retrieving observation with id = " + id;
	}

}
