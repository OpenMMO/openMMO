package OpenT4C;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class ScreenManager extends Game{

	private DataCheckerScreen checkScreen = null;
	String map ="";
	OrthographicCamera camera;
	public int status = 0;
	
	public ScreenManager(String f){
		map = f;
		UpdateScreenManagerStatus.setScreenManager(this);
	}

	public ScreenManager(){
		map = "v2_worldmap";
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
		t4cPlugin.AssetsLoader.dispose();
	}
	
	public void initMap(){
		t4cPlugin.AssetsLoader.loadSols();
		MapManager.loadMap();
		MapManager.createChunkMap();
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
}