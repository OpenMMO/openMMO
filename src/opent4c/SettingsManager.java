package opent4c;

import opent4c.utils.FilesPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL30;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class SettingsManager {
	
	private static LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
	private static Logger logger = LogManager.getLogger(SettingsManager.class.getSimpleName());
	
	private static final String title = "OpenT4C";
	private static final int screen_columns = 32;//32
	private static final int screen_rows = 48;//48
	private static final int background_fps = 24;
	private static final int foreground_fps = 60;
	private static final boolean fullscreen = false;
	private static final boolean use_opengl3_if_possible = false;
	private static final boolean resizable = false;
	private static final boolean vertical_sync = true;
	private static final boolean allow_software_mode_if_hardware_fails = true;
	private static final Color initial_background_color = Color.CLEAR;
	private static final String preferences_directory = ".openT4C/";

	
	
	
	/**
	 * Creates settings for a new LwjglApplication
	 */
	public static void create(){
		//cfg.r;
		//cfg.g;
		//cfg.b;
		//cfg.a;
		//cfg.depth;
		//cfg.samples;
		//cfg.stencil;
		//cfg.overrideDensity;
		
		//cfg.audioDeviceBufferCount;
		//cfg.audioDeviceBufferSize;
		//cfg.audioDeviceSimultaneousSources;
		
		//cfg.forceExit;
		//cfg.setDisplayModeCallback;
		cfg.title = title;
		cfg.preferencesDirectory = preferences_directory;
		cfg.initialBackgroundColor = initial_background_color;
		cfg.useGL30 = use_opengl3_if_possible;
		cfg.width = screen_columns * 32;
		cfg.height = screen_rows * 16;
		cfg.fullscreen = fullscreen;
		cfg.backgroundFPS = background_fps;
		cfg.foregroundFPS = foreground_fps;
		cfg.resizable = resizable;
		cfg.addIcon(FilesPath.get128IconFilePath(), Files.FileType.Internal);
		cfg.addIcon(FilesPath.getIconFilePath(), Files.FileType.Internal);
		cfg.addIcon(FilesPath.get32IconFilePath(), Files.FileType.Internal);
		cfg.addIcon(FilesPath.get16IconFilePath(), Files.FileType.Internal);
		cfg.vSyncEnabled = vertical_sync;
		cfg.allowSoftwareMode = allow_software_mode_if_hardware_fails;
		cfg.x = -1;
		cfg.y = -1;
	}

	/**
	 * prints display capabilities
	 */
	public static void printCapabilities() {
		//TODO faire ça mieux parce que ça peut être vachement long...
		for(int i = 0 ; i < Gdx.graphics.getDisplayModes().length ; i++){
			logger.info("Display Mode : "+Gdx.graphics.getDisplayModes()[i].width+"x"+Gdx.graphics.getDisplayModes()[i].height);
		}
		logger.info("GL30 : "+Gdx.graphics.isGL30Available());
	}

	/**
	 * 
	 * @return settings to apply to the application
	 */
	public static LwjglApplicationConfiguration getSettings() {
		return cfg;
	}
}
