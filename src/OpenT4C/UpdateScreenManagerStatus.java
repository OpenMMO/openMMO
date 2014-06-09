package OpenT4C;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateScreenManagerStatus {
	
	private static Logger logger = LogManager.getLogger(UpdateScreenManagerStatus.class.getSimpleName());
	private static ScreenManager sm = null;
	
	public static void setScreenManager(ScreenManager manager){
		sm = manager;
	}
	private static void setStatus(final int status){
		if (sm == null){
			logger.fatal("ScreenManager not set");
			System.exit(1);
		}
		logger.info("Set ScreenManager Status : "+getReadableStatus());
		sm.setStatus(status);
	}
	
	public static String getReadableStatus() {
		switch (sm.status){
		case 0 : return "0 : Idle";
		case 1 : return "1 : Populating Source Data";
		case 2 : return "2 : Checking Source Data";
		case 3 : return "3 : Checking Atlas";
		case 4 : return "4 : Checking Sprite Data";
		case 5 : return "5 : Checking Maps";
		case 6 : return "6 : Loading Tiles";
		case 7 : return "7 : Loading Maps";
		case 8 : return "8 : Creating Chunks";
		case 42 : return "42 : Ready To Render";
		}
		
		return "Inconnu";
	}
	
	public static void idle() {
		setStatus(0);		
	}
	public static void populatingSourceData() {
		setStatus(1);		
	}
	public static void checkingSourceData() {
		setStatus(2);		
	}
	public static void checkingAtlas() {
		setStatus(3);		
	}
	public static void checkingSpriteData() {
		setStatus(4);			
	}
	public static void checkingMaps() {
		setStatus(5);
	}
	public static void loadingTiles() {
		setStatus(6);		
	}
	public static void loadingMaps() {
		setStatus(7);				
	}
	public static void creatingChunks() {
		setStatus(8);
	}
	public static void readyToRender() {
		setStatus(42);		
	}
}
