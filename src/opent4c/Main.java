package opent4c;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import screens.ScreenManager;
import tools.OSValidator;



public class Main {

	private static Logger logger = LogManager.getLogger(Main.class.getSimpleName());
	private static ScreenManager sm = null;
	private static ShaderProgram shader = null;
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
		Gdx.app.postRunnable(new Runnable(){
			public void run(){
				createShaders();
			}
		});
	}

	private static void createShaders() {
		String vertexShader = "attribute vec4 a_position;    \n" + 
	            "attribute vec4 a_color;\n" +
	            "attribute vec2 a_texCoord0;\n" + 
	            "uniform mat4 u_projTrans;\n" + 
	            "varying vec4 v_color;" + 
	            "varying vec2 v_texCoords;" + 
	            "void main()                  \n" + 
	            "{                            \n" + 
	            "   v_color = vec4(1, 1, 1, 1); \n" + 
	            "   v_texCoords = a_texCoord0; \n" + 
	            "   gl_Position =  u_projTrans * a_position;  \n"      + 
	            "}                            \n" ;
		String fragmentShader = "#ifdef GL_ES\n" +
	              "precision mediump float;\n" + 
	              "#endif\n" + 
	              "varying vec4 v_color;\n" + 
	              "varying vec2 v_texCoords;\n" + 
	              "uniform sampler2D u_texture;\n" + 
	              "void main()                                  \n" + 
	              "{                                            \n" + 
	              "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
	              "}";
		shader = new ShaderProgram(vertexShader, fragmentShader);
		logger.info("Shader compiled : "+shader.isCompiled());
		if(!shader.isCompiled())logger.info(shader.getLog());
	}
	
	public static ShaderProgram getShader(){
		return shader;
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
