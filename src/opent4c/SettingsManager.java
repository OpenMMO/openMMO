package opent4c;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class SettingsManager {
	
	private static LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
	private static Logger logger = LogManager.getLogger(SettingsManager.class.getSimpleName());
	private static int nb_tile_dim = 40;
	private static String CONSOLE_INPUT = "";
	
	//TODO Faire un truc plus complet.
	/**
	 * Creates settings to apply to a new LwjglApplication
	 */
	public static void create(){
		cfg.title = "OpenT4C v0.0";
		cfg.useGL20 = true;
		//cfg.width = getWidth();
		//cfg.height = getHeight();
		cfg.fullscreen = false;
		cfg.width = 1200;
		cfg.height = 600;
		//cfg.fullscreen = true;
		cfg.backgroundFPS = 24;
		cfg.foregroundFPS = 60;
		cfg.resizable = false;
		cfg.vSyncEnabled = true;
		cfg.allowSoftwareMode = true;
		cfg.x = -1;
		cfg.y = -1;
	}

	/**
	 * sets windows height to the biggest 16 multiple below screen resolution
	 * @return
	 */
	private static int getHeight() {
		int result = 800;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		result = getMultiple16Inferieur(screenSize.height);
		logger.info("Height : "+result);
		return result;
	}

	/**
	 * gives a 16 multiple below a given value
	 * @param height
	 * @return
	 */
	private static int getMultiple16Inferieur(int height) {
		int result = height;
		if (height % 16 == 0){
			result = result - (4*(result/16));
		}else{
			result = result - (2*(result % 16));			
		}
		//return result;
		return (nb_tile_dim*16);
	}

	/**
	 * sets windows width to the biggest 32 multiple below screen resolution
	 * @return
	 */
	private static int getWidth() {
		int result = 400;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		result = getMultiple32Inferieur(screenSize.width);
		logger.info("Width : "+result);
		//return result;
		return (nb_tile_dim*32);
	}
	
	/**
	 * gives a 32 multiple below a given value
	 * @param width
	 * @return
	 */
	private static int getMultiple32Inferieur(int width) {
		int result = width;
		if (width % 32 == 0){
			result = result - (2*(result/32));
		}else{
			result = result - (2*(result % 32));			
		}		return result;
	}

	/**
	 * prints display capabilities
	 */
	public static void printCapabilities() {
		//TODO faire ça mieux parce que ça peut être vachement long...
		for(int i = 0 ; i < Gdx.graphics.getDisplayModes().length ; i++){
			logger.info("Display Mode : "+Gdx.graphics.getDisplayModes()[i].width+"x"+Gdx.graphics.getDisplayModes()[i].height);
		}
		logger.info("GL1.1 : "+Gdx.graphics.isGL11Available());
		logger.info("GL2.0 : "+Gdx.graphics.isGL20Available());
	}

	/**
	 * 
	 * @return settings to apply to the application
	 */
	public static LwjglApplicationConfiguration getSettings() {
		return cfg;
	}

	/**
	 * @param result
	 */
	public static void setCONSOLE_INPUT(String input) {
		CONSOLE_INPUT  = input;
	}
}
