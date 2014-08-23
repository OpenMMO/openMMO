package tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;

public class OSValidator {
	
	private static Logger logger = LogManager.getLogger(OSValidator.class.getSimpleName());
	 
	private static String OS = System.getProperty("os.name").toLowerCase();
 
	public static void detect() {
		logger.info("Détection de l'OS :");
 
		if (isWindows()) {
			logger.info("Windows");
		} else if (isMac()) {
			logger.info("Mac");
		} else if (isUnix()) {
			logger.info("Unix/Linux");
		} else if (isSolaris()) {
			logger.info("Solaris");
		} else {
			logger.fatal("Inconnu");
			Gdx.app.exit();
		}
	}
 
	public static boolean isWindows() {
 
		return (OS.indexOf("win") >= 0);
 
	}
 
	public static boolean isMac() {
 
		return (OS.indexOf("mac") >= 0);
 
	}
 
	public static boolean isUnix() {
 
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
 
	}
 
	public static boolean isSolaris() {
 
		return (OS.indexOf("sunos") >= 0);
 
	}
 
}