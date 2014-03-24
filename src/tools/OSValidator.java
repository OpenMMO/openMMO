package tools;

import t4cPlugin.Params;

public class OSValidator {
	 
	private static String OS = System.getProperty("os.name").toLowerCase();
 
	public static void detect() {
		System.out.println("Détection de l'OS :");
 
		if (isWindows()) {
			System.out.println("	- OS : Windows");
			Params.OS = "Windows";
			Params.CHARSET="windows-1258";
			Params.SLASH='\\';
			Params.ANTISLASH='/';
			Params.LINE=System.getProperty("line.separator");
		} else if (isMac()) {
			System.out.println("	- OS : Mac");
			Params.OS = "Mac";
			Params.CHARSET="UTF-8";
			Params.SLASH='/';
			Params.ANTISLASH='\\';
			Params.LINE=System.getProperty("line.separator");
		} else if (isUnix()) {
			System.out.println("	- OS : Unix/Linux");
			Params.OS = "Unix";
			Params.CHARSET="UTF-8";
			Params.SLASH='/';
			Params.ANTISLASH='\\';
			Params.LINE=System.getProperty("line.separator");
		} else if (isSolaris()) {
			System.out.println("	- OS : Solaris");
			Params.OS = "Solaris";
			Params.CHARSET="UTF-8";
			Params.SLASH='/';
			Params.ANTISLASH='\\';
			Params.LINE=System.getProperty("line.separator");
		} else {
			System.out.println("	- OS : Inconnu");
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