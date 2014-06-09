package OpenT4C;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class SettingsManager {
	
	private static LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
	private static Logger logger = LogManager.getLogger(SettingsManager.class.getSimpleName());

	//TODO Faire un truc plus complet.
	public static void create(){
		cfg.title = "OpenT4C v0.0";
		cfg.useGL20 = true;
		cfg.width = getWidth();
		cfg.height = getHeight();
	}


	private static int getHeight() {
		int result = 800;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		result = getMultiple16Inferieur(screenSize.height);
		logger.info("Height : "+result);
		return result;
	}

	private static int getMultiple16Inferieur(int height) {
		int result = height;
		if (height % 16 == 0){
			result = result - (4*(result/16));
		}else{
			result = result - (2*(result % 16));			
		}
		return result;
	}


	private static int getWidth() {
		int result = 400;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		result = getMultiple32Inferieur(screenSize.width);
		logger.info("Width : "+result);
		return result;
	}
	
	private static int getMultiple32Inferieur(int width) {
		int result = width;
		if (width % 32 == 0){
			result = result - (2*(result/32));
		}else{
			result = result - (2*(result % 32));			
		}		return result;
	}

	public static void printCapabilities() {
		for(int i = 0 ; i < Gdx.graphics.getDisplayModes().length ; i++){
			logger.info("Display Mode : "+Gdx.graphics.getDisplayModes()[i].width+"x"+Gdx.graphics.getDisplayModes()[i].height);
		}
		logger.info("GL1.1 : "+Gdx.graphics.isGL11Available());
		logger.info("GL2.0 : "+Gdx.graphics.isGL20Available());
	}


	public static LwjglApplicationConfiguration get() {
		return cfg;
	}
}
