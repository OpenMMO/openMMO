package t4cPlugin;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class ScreenManager extends Game{
	private boolean ready = false;
	private GdxMap worldmap;
	private LoadScreen loadscreen = null;
	String map ="";
	OrthographicCamera camera;
	public int status = 0;
	
	public ScreenManager(String f){
		map = f;
	}
	
	@Override
	public void create() {
		switchLoadScreen();
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