package fr.ifremer.sensornanny.grabber;

import java.util.logging.Logger;

public class MimetypeHelper {

	private static final Logger logger = Logger.getLogger(MimetypeHelper.class.getName());
	
	public static String getExtension(final String mimeType) {
		String extension = "";
		
		switch (mimeType) {
		case "application/data-csv":
			extension = "csv";
			break;

		default:
			logger.severe("Unknown extension for " + mimeType);
			break;
		}
		
		return extension;
	}
	
}
