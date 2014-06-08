package OpenT4C;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import tools.OSValidator;



public class Main {

	private static Logger logger = LogManager.getLogger(Main.class.getSimpleName());
	private static ScreenManager sm = null;
	private static boolean debug_GDX = true;
	
	/**
	 * Je pense retenter une écriture complète ici, en parallèle
	 * de GDXEditor, simplement pour intégrer de meilleures habitudes de codage.
	 * En définitive, on supprimera pas mal de classes dans d'autres packages pour
	 * regrouper des trucs plus propres ici.
	 * @param args
	 */
	public static void main(String[] args) {
		
		logger.info("Démarrage.");
		OSValidator.detect();
		afficher();
		verifier();
		charger();
	}

	/**
	 * Créé un affichage.
	 */
	private static void afficher() {
		logger.info("Création de l'affichage.");
		sm = new ScreenManager();
		SettingsManager.create();
		new LwjglApplication(sm, SettingsManager.get());
		if(debug_GDX)SettingsManager.printCapabilities();
	}

	/**
	 * Vérifie les données écrites afin de savoir ce qui doit être décrypté.
	 */
	private static void verifier() {
		logger.info("Vérification des données.");
		DataChecker.runCheck();
	}

	/**
	 * Charge une carte dans l'éditeur, une fois qu'on est certains
	 * que tout ce qui est nécessaire est présent.
	 */
	private static void charger() {
		logger.info("Chargement des données");
		SpriteData.load();
		sm.initMap();
	}
	
	/**
	 * On donne l'accès au ScreenManager
	 */
	public ScreenManager getScreenManager(){
		return sm;
	}
}
