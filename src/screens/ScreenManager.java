package screens;

import opent4c.Chunk;
import opent4c.UpdateDataCheckStatus;
import opent4c.utils.AssetsLoader;
import opent4c.utils.LoadingStatus;
import opent4c.utils.Place;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;


public class ScreenManager extends Game{

	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private DataCheckerScreen checkScreen = null;
	OrthographicCamera camera;
	GameScreen map;
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
		setScreen(checkScreen);
	}

	public void switchGameScreen(final GameScreen screen) {
		UpdateDataCheckStatus.setStatus("Initialisation.");
		GameScreen.teleport(Place.getPlace("lh_temple"));
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
		Place.createDefaultPlaces();
		AssetsLoader.loadMappedSprites();
		AssetsLoader.loadSols();
		map = new GameScreen(this);
		loadingStatus.waitUntilTileAtlasAreLoaded();
		Gdx.app.postRunnable(RunnableCreatorUtil.getMapInitializerRunnable());
		GameScreen.loadMaps();
		//GameScreen.createIdEditMap();
		//loadingStatus.waitIdEditListCreated();
		Chunk.cacheSmoothingTemplatePixmaps();
		Chunk.startChunkEngine();
		switchGameScreen(map);
	}
}