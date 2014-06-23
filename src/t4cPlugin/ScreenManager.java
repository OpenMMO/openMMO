package t4cPlugin;

import opent4c.utils.AssetsLoader;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
@Deprecated
public class ScreenManager extends Game{
	
	public static final int IS_IDLE = 42;
	public static final int IS_CHECKING_SOURCE_DATA = 0;
	public static final int CHECK_ATLAS = 1;
	public static final int CHECK_MAPS = 2;

	
	private boolean ready = false;
	private GdxMap worldmap;
	private LoadScreen loadscreen = null;
	String map ="";
	OrthographicCamera camera;
	public int status = 0;
	
	public ScreenManager(String f){
		map = f;
	}

	public ScreenManager(){
		map = "v2_worldmap";
	}
	
	@Override
	public void create() {
		//switchLoadScreen();
	}
/*----------------------------SWITCHS D'ECRAN CLIENT---------------------*/

	/** Vers l'ecran d'édition de carte */
	public void switchGameScreen(){
		this.setScreen(worldmap);
	}
	
	/** Vers l'ecran de chargement */
	public void switchLoadScreen(){
		loadscreen = new LoadScreen(this);
		this.setScreen(loadscreen);
	}
	
/*-----------------------------RENDU GRAPHIQUE----------------------------*/
	public void render() {
		super.render();
	}
	
	public void dispose() {
		AssetsLoader.dispose();
	}

	public void setReady(boolean b) {
		this.ready = b;
	}
	
	public LoadScreen getLoadScreen(){
		return loadscreen;
	}
	
	public void initMap(){
		setStatus(0);
		Params.STATUS = "Chargement des sprites";
		AssetsLoader.loadSprites();
		Params.STATUS = "Chargement des tuiles.";
		AssetsLoader.loadSols();
		Params.STATUS = "Chargement de la carte.";
		loadMap();
	}
	
	private void loadMap() {
		worldmap = new GdxMap(this);
		worldmap.load();
		switchGameScreen();
		worldmap.readPIXMLMAP(map);
	}
	
	
	public void setStatus(int i) {
		status = i;
	}
	
	public int getStatus(){
		return status;
	}
}