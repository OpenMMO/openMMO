package tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.Params;

public class OSValidator {
	
	private static Logger logger = LogManager.getLogger(OSValidator.class.getSimpleName());
	 
	private static String OS = System.getProperty("os.name").toLowerCase();
 
	public static void detect() {
		logger.info("Détection de l'OS :");
 
		if (isWindows()) {
			logger.info("	- OS : Windows");
			Params.OS = "Windows";
			Params.CHARSET="windows-1258";
			Params.SLASH='\\';
			Params.ANTISLASH='/';
			Params.LINE=System.getProperty("line.separator");
		} else if (isMac()) {
			logger.info("	- OS : Mac");
			Params.OS = "Mac";
			Params.CHARSET="UTF-8";
			Params.SLASH='/';
			Params.ANTISLASH='\\';
			Params.LINE=System.getProperty("line.separator");
		} else if (isUnix()) {
			logger.info("	- OS : Unix/Linux");
			Params.OS = "Unix";
			Params.CHARSET="UTF-8";
			Params.SLASH='/';
			Params.ANTISLASH='\\';
			Params.LINE=System.getProperty("line.separator");
		} else if (isSolaris()) {
			logger.info("	- OS : Solaris");
			Params.OS = "Solaris";
			Params.CHARSET="UTF-8";
			Params.SLASH='/';
			Params.ANTISLASH='\\';
			Params.LINE=System.getProperty("line.separator");
		} else {
			logger.fatal("	- OS : Inconnu");
			System.exit(1);
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