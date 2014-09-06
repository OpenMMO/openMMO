package opent4c;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

import screens.ScreenManager;
import tools.OSValidator;



public class Main {

	private static Logger logger = LogManager.getLogger(Main.class.getSimpleName());
	private static ScreenManager sm = null;
	private static final boolean debug_GDX = false;
	
	public static void main(String[] args) {
		
		logger.info("Démarrage.");
		OSValidator.detect();
		afficher();
		verifier();
		charger();
	}

	/**
	 * Sets up a display
	 */
	private static void afficher() {
		logger.info("Création de l'affichage.");
		sm = new ScreenManager();
		SettingsManager.create();
		new LwjglApplication(sm, SettingsManager.getSettings());
		if(debug_GDX)SettingsManager.printCapabilities();
	}

	/**
	 * Creates a DataChecker 
	 */
	private static void verifier() {
		UpdateDataCheckStatus.setStatus("Vérification des données.");
		DataChecker.runCheck();
	}

	/**
	 * Loads Data
	 */
	public static void charger() {
		UpdateDataCheckStatus.setStatus("Chargement des données.");
		PixelIndex.loadIndexFile();
		sm.initMap();
	}
}
