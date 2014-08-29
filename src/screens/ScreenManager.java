package screens;

import opent4c.Chunk;
import opent4c.UpdateDataCheckStatus;
import opent4c.utils.AssetsLoader;
import opent4c.utils.LoadingStatus;
import opent4c.utils.Place;
import opent4c.utils.RunnableCreatorUtil;

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
	}
	
	@Override
	public void create() {
		checkScreen = new DataCheckerScreen();
		switchCheckDataScreen();
	}
/*----------------------------SWITCHS D'ECRAN CLIENT---------------------*/
	
	public void switchCheckDataScreen() {
		this.setScreen(checkScreen);
	}

	public void switchGameScreen(final MapManager screen) {
		UpdateDataCheckStatus.setStatus("Initialisation.");
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
		Place.createDefault();
		AssetsLoader.loadMappedSprites();
		AssetsLoader.loadSols();
		map = new MapManager(this);
		loadingStatus.waitUntilTileAtlasAreLoaded();
		Gdx.app.postRunnable(RunnableCreatorUtil.getMapInitializerRunnable());
		MapManager.loadMaps();
		MapManager.createIdEditMap();
		loadingStatus.waitIdEditListCreated();
		Chunk.cacheSmoothingTemplatePixmaps();
		Chunk.startChunkEngine();
		switchGameScreen(map);
	}
}