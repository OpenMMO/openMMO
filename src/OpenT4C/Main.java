package OpenT4C;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import t4cPlugin.Params;
import tools.OSValidator;



public class Main {

	private static Logger logger = LogManager.getLogger(Main.class.getSimpleName());
	private static ScreenManager sm = null;
	private static LwjglApplication app = null;
	private static DataChecker checker = null;
	
	/**
	 * Je pense retenter une écriture complète ici, en parallèle
	 * de GDXEditor, simplement pour intégrer de meilleures habitudes de codage.
	 * En définitive, on supprimera pas mal de classes dans d'autres packages pour
	 * regrouper des trucs plus propres ici.
	 * @param args
	 */
	public static void main(String[] args) {
		
		logger.info("Démarrage.");
		Params.SPRITES = "data"+File.separator+"sprites"+File.separator;
		//paramétrage de l'appli en fonction de l'os.
		OSValidator.detect();
		logger.info("Création de l'affichage.");
		afficher();
		logger.info("Vérification des données.");
		verifier();
		logger.info("Chargement des données");
		charger();
	}

	/**
	 * Créé un affichage.
	 */
	private static void afficher() {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "OpenT4C v0.0";
		cfg.useGL20 = true;
		cfg.width = 800;
		cfg.height = 450;
		sm = new ScreenManager();
		app = new LwjglApplication(sm, cfg);		
	}

	/**
	 * Vérifie les données écrites afin de savoir ce qui doit être décrypté.
	 */
	private static void verifier() {
		checker = new DataChecker(sm);
		checker.runCheck();
	}

	/**
	 * Charge une carte dans l'éditeur, une fois qu'on est certains
	 * que tout ce qui est nécessaire est présent.
	 */
	private static void charger() {
		// TODO Auto-generated method stub
		
	}
}
