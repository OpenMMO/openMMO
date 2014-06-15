package OpenT4C;

import t4cPlugin.AssetsLoader;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class ScreenManager extends Game{

	private DataCheckerScreen checkScreen = null;
	OrthographicCamera camera;
	MapManager map;
	public int status = 0;
	private String substatus = "not updated";
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

	public void switchGameScreen() {
		Gdx.app.postRunnable(new Runnable(){

			@Override
			public void run() {
				setScreen(MapManager.getScreen());				
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
		map = new MapManager();
		map.loadMaps();
		map.createChunkMap();
		UpdateScreenManagerStatus.readyToRender();
		MapManager.setReadyToRender();
	}
	
	public void setStatus(int status){
		this.status = status;
		if (status == 42){
			switchGameScreen();
		}
	}
	public int getStatus(){
		return status;
	}

	public String getSubstatus() {
		return substatus;
	}

	public void setSubstatus(String substatus) {
		this.substatus = substatus;
	}
}