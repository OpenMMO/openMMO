package screens;

import opent4c.UpdateScreenManagerStatus;
import opent4c.utils.AssetsLoader;
import opent4c.utils.LoadingStatus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;


public class ScreenManager extends Game{

	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private DataCheckerScreen checkScreen = null;
	OrthographicCamera camera;
	MapManager map;
	public int status = 0;

	/**
	 * default map is v2_worldmap
	 */
	public ScreenManager(){
		UpdateScreenManagerStatus.setScreenManager(this);
	}
	
	@Override
	public void create() {
		checkScreen = new DataCheckerScreen(this);
		switchCheckDataScreen();
	}
/*----------------------------SWITCHS D'ECRAN CLIENT---------------------*/
	
	public void switchCheckDataScreen() {
		this.setScreen(checkScreen);
	}

	public void switchGameScreen(final MapManager screen) {
		Gdx.app.postRunnable(new Runnable(){

			@Override
			public void run() {
				setScreen(screen);				
			}
		});
	}
	
/*-----------------------------RENDU GRAPHIQUE----------------------------*/
	public void render() {
		super.render();
	}
	
	public void dispose() {
		AssetsLoader.dispose();
	}
	
	public void initMap(){
		AssetsLoader.loadSols();
		AssetsLoader.load("Unknown");
		map = new MapManager(this);
		loadingStatus.waitUntilSpritesPackaged();
		map.loadMaps();
		map.createChunkMap();
	}
}