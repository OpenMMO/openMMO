package screens;

import java.awt.Point;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import opent4c.Acteur;
import opent4c.Chunk;
import opent4c.InputManager;
import opent4c.Player;
import opent4c.UpdateDataCheckStatus;
import opent4c.utils.FilesPath;
import opent4c.utils.LoadingStatus;
import opent4c.utils.Place;
import opent4c.utils.PointsManager;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * This class manages the gamescreen.
 * 
 * @author synoga
 *
 */
public class MapManager implements Screen{

	private static Logger logger = LogManager.getLogger(MapManager.class.getSimpleName());
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;

	private static Map<String,ByteBuffer> id_maps = new HashMap<String,ByteBuffer>(5);
	private static MapManager m;
	private static InputManager controller = null;
	private static TextButtonStyle style = new TextButtonStyle();
	private static TextButton OnScreenInfos, playerLocationInfo;
	private static OrthographicCamera camera;
	private static SpriteBatch batch;
	private static ScreenManager screenManager;
	private static Stage tileStage, spriteStage, uiStage, debugStage, smoothStage;
	private static boolean render_ui = true;
	private static boolean render_smoothing = true;
	private static boolean render_sprites = true;
	private static boolean render_tiles = true;
	private static boolean render_debug = false;
	private static boolean idListCreated = false;
	private static boolean highlighted = false;
	private static ScheduledFuture<?> highlight;
	private static Point highlight_point;
	private static Acteur highlight_tile;
	private static Map<Integer, Point> idEditList;
	private static Point playerPosition;
	private static Point cameraPosition;
	public static final float blink_period = 1;
	private static Player player;
	private static IdEditMenu editor;
	private static ScreenViewport viewport;

	/**
	 * Initializes the MapManager and binds the inputManager
	 */
	public static void init() {
		style.font = new BitmapFont();
		tileStage = new Stage();
		spriteStage = new Stage();
		debugStage = new Stage();
		smoothStage = new Stage();
		uiStage = new Stage();
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();
		viewport = new ScreenViewport(camera);
		tileStage.setViewport(viewport);
		spriteStage.setViewport(viewport);
		debugStage.setViewport(viewport);
		smoothStage.setViewport(viewport);
		initInfos();
		controller = new InputManager(getManager());
	}
	
	/**
	 * Sets the onScreen texts
	 */
	private static void initInfos(){
		OnScreenInfos = new TextButton("status", style);
		OnScreenInfos.setPosition(Gdx.graphics.getWidth()/2 + OnScreenInfos.getWidth()/2, Gdx.graphics.getHeight()/2 - OnScreenInfos.getHeight()/2);
		OnScreenInfos.getColor().a = 0f;
		playerLocationInfo = new TextButton("info", style);
		uiStage.addActor(OnScreenInfos);
		uiStage.addActor(playerLocationInfo);
	}
	
	/**
	 * Loads maps from .decrypt files
	 */
	public static void loadMaps() {
		logger.info("Chargement des cartes");
		UpdateDataCheckStatus.setStatus("Chargement des cartes");
		List<File> decrypted_maps = FilesPath.getMapFilePathsPaths();
		Iterator<File> iter_decrypted_maps = decrypted_maps.iterator();
		while(iter_decrypted_maps.hasNext()){
			File f = iter_decrypted_maps.next();
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getMapLoaderRunnable(f));
		}
		loadingStatus.waitForAllMapsLoaded();
		logger.info("Cartes chargées");
		UpdateDataCheckStatus.setStatus("Cartes chargées");
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		render_camera();
		tileStage.act(delta);
		smoothStage.act(delta);
		spriteStage.act(delta);
		debugStage.act(delta);
		uiStage.act(delta);
		if(render_ui)updateInfos();
		batch.begin();
			if(render_tiles)tileStage.draw();
			if(render_smoothing)smoothStage.draw();
			if(render_sprites)spriteStage.draw();
			if(render_debug)debugStage.draw();
			if(render_ui)uiStage.draw();
		batch.end();

	}

	/**
	 * Sets on screen texts
	 */
	private void updateInfos() {
		Gdx.app.getGraphics().setTitle("OpenT4C v0 FPS: " + Gdx.graphics.getFramesPerSecond() + " RAM : " + ((Runtime.getRuntime().totalMemory())/1024/1024) + " Mo" + Chunk.getEngineInfos()+" Actors in stages : "+getActorsInMemory());
		playerLocationInfo.setText("X: " + (((int)camera.position.x/32)) + " Y: " + (((int)camera.position.y/16)) + " Zoom : " + camera.zoom);
		playerLocationInfo.setPosition(playerLocationInfo.getWidth()*4, playerLocationInfo.getHeight());
	}

	private int getActorsInMemory() {
		return tileStage.getActors().size+spriteStage.getActors().size+smoothStage.getActors().size+debugStage.getActors().size+uiStage.getActors().size;
	}

	@Override
	public void show() {
		tileStage.addAction(Actions.alpha(1f, 2f));
		spriteStage.addAction(Actions.alpha(1f, 2f));
		debugStage.addAction(Actions.alpha(1f, 2f));
		smoothStage.addAction(Actions.alpha(1f, 2f));
		uiStage.addAction(Actions.alpha(1f, 2f));
		Gdx.input.setInputProcessor(controller);
	}

	/**
	 * Gets an ID from map coordinates on a specific map.
	 * @param carte
	 * @param point
	 * @return the id at given coordinates on a given map name
	 */
	public static int getIdAtCoordOnMap(String carte, Point point) {
		int result = -1;
		result = getIdAtCoord(getIdMaps().get(carte), point);
		return result;
	}

	/**
	 * @param buf
	 * @param point
	 * @return the id at given coordinates from a ByteBuffer (map)
	 */
	private static int getIdAtCoord(ByteBuffer buf, Point point) {
		int result = -1;
		if(point.x < 0 || point.x > 3071 || point.y < 0 || point.y > 3071){
			return result;
		}
		byte b1=0,b2=0;
		try{
			buf.position((6144*(point.y))+(point.x*2));
		}catch(Exception e){
			logger.fatal(point+" : "+e);
			e.printStackTrace();
			Gdx.app.exit();
		}
		b1 = buf.get();
		b2 = buf.get();
		result = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
		return result;
	}

	/**
	 * Teleports player to a given place and generates chunk.
	 * @param place
	 */
	public static void teleport(Place place){
		if(place == null){
			logger.warn("On tente de se téléporter dans un endroit null");
			return;
		}
		Gdx.input.setInputProcessor(null);
		//logger.info("Téléportation : Camera("+place.getCoord().x+";"+place.getCoord().y+") Player("+place.getMapCoord().x+";"+place.getMapCoord().y+")");
		getCamera().position.x = place.getCoord().x;
		getCamera().position.y = place.getCoord().y;
		OnScreenInfos.clearActions();
		OnScreenInfos.setText(place.getNom());
		OnScreenInfos.getColor().a = 1f;
		OnScreenInfos.addAction(Actions.alpha(0f, 1));
		ThreadsUtil.executeInGraphicalThread(RunnableCreatorUtil.getMapCleanerRunnable(place));
	}

	/**
	 * clears all actors from all stages (but not ui).
	 */
	public static void clearStages(){
		tileStage.clear();
		spriteStage.clear();
		debugStage.clear();
		smoothStage.clear();		
	}
	
	/**
	 * Creates a chunk at the given place.
	 * @param place
	 */
	public static void createChunk(Place place) {
		Chunk.newChunk(place.getMapName(),place.getMapCoord());
		Gdx.input.setInputProcessor(controller);
	}

	/**
	 * Pops up a menu to edit map at given coords 
	 * @param point
	 */
	public void editMapAtCoord(Point point) {
		int id = getIdAtCoordOnMap("v2_worldmap", point);
		logger.info("Open menu ID "+id+"@ "+point.x+";"+point.y);
		editor = new IdEditMenu(point);
		Gdx.input.setInputProcessor(editor);
		screenManager.setScreen(editor);
	}


	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////UTILS/////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static Map<Integer, Point> getIdEditList() {
		return idEditList;
	}

	public static void setIdEditList(Map<Integer, Point> idEditList) {
		MapManager.idEditList = idEditList;
	}
	
	public MapManager(ScreenManager sm){
		screenManager = sm;
		setManager(this);
	}
	
	public static void createIdEditMap() {
		ThreadsUtil.executeInThread(RunnableCreatorUtil.getIdEditListCreatorRunnable());
	}
	
	/**
	 * @return the Screen
	 */
	public static MapManager getScreen() {
		return getManager();
	}
	
	/**
	 * Checks if chunk needs to be moved. If the camera's position is farther from the center of the chunk map than one tile.
	 * @param playerPosition
	 * @return direction to move to : 0 not to move, 1 to move right, 2 to move down right, 3 to move down, 4 to move down left, 5 to move left, 6 to move up left, 7 to move up, 8 to move up right.
	 */
	private static int checkIfChunksNeedsToBeMoved() {
		updateCameraPosition();
		updatePlayerPosition();
		Point chunkCenter = Chunk.getCenter();
		boolean left = false;
		boolean right = false;
		boolean up = false;
		boolean down = false;
		if((cameraPosition.x > chunkCenter.x*32+32)) right = true;
		if((cameraPosition.x < chunkCenter.x*32-32)) left = true;
		if((cameraPosition.y < chunkCenter.y*16-16)) up = true;
		if((cameraPosition.y > chunkCenter.y*16+16)) down = true;
		if (right && !left && !up && !down) return 6;
		if (!right && left && !up && !down) return 4;
		if (!right && !left && up && !down) return 8;
		if (!right && !left && !up && down) return 2;
		if (right && !left && up && !down) return 9;
		if (right && !left && !up && down) return 3;
		if (!right && left && up && !down) return 7;
		if (!right && left && !up && down) return 1;
		return 5;
	}

	/**
	 * Updates last known player position
	 */
	public static void updatePlayerPosition(){
		playerPosition = PointsManager.getPoint(cameraPosition.x/32, cameraPosition.y/16);
	}
	
	/**
	 * Updates Chunk position
	 */
	public static void updateChunkPosition() {
		updateCameraPosition();
		updatePlayerPosition();
		Chunk.move(checkIfChunksNeedsToBeMoved());
	}
	
	/**
	 * Updates last known camera position.
	 */
	private static void updateCameraPosition() {
		cameraPosition = PointsManager.getPoint(getCamera().position.x, getCamera().position.y);
	}

	/**
	 * @return the camera
	 */
	public static OrthographicCamera getCamera() {
		return MapManager.camera;
	}
	
	/**
	 * Hihglight a tile on the map
	 * @param point
	 */
	public static void highlight(Point point) {
		//logger.info("HIGHLIGHT");
		highlight_point = point;
		TextureRegion tex = getHighlightTile();
		highlight_tile = new Acteur(tex,highlight_point, PointsManager.getPoint(0, 0));
		highlight_tile.getColor().a = 0f;
		spriteStage.addActor(highlight_tile);
		setHighlighted(true);
		highlight = ThreadsUtil.executePeriodicallyInThread(RunnableCreatorUtil.getHighlighterRunnable(), 0, (int)blink_period, TimeUnit.SECONDS);
	}
	
	/**
	 * @return the "Highlight" tile
	 */
	private static TextureRegion getHighlightTile(){
		TextureAtlas texAtlas = Chunk.getUtilsAtlas();
		return texAtlas.findRegion("Highlight");
	}
	
	/**
	 * 
	 */
	public static void tileFadeIn() {
		Gdx.app.postRunnable(new Runnable(){
			@Override
			public void run() {
				getHighlight_tile().addAction(Actions.alpha(0.3f, blink_period/2));
			}
		});
	}

	/**
	 * 
	 */
	public static void tileFadeOut() {
		Gdx.app.postRunnable(new Runnable(){
			@Override
			public void run() {
				getHighlight_tile().addAction(Actions.alpha(0f, blink_period/2));
			}
		});
	}

	public static Acteur getHighlight_tile() {
		return highlight_tile;
	}

	public static boolean isHighlighted() {
		return highlighted;
	}

	public static void setHighlighted(boolean highlighted) {
		MapManager.highlighted = highlighted;
	}

	/**
	 * 
	 */
	public static void unHighlight() {
		highlight.cancel(false);
		setHighlighted(false);
	}

	/**
	 * Closes edit menu and gives back control.
	 */
	public static void close_edit_menu() {
		screenManager.setScreen(m);
		Gdx.input.setInputProcessor(controller);
		unHighlight();
		editor.dispose();
	}

	/**
	 * toggles a boolean
	 * @param b
	 */
	private static boolean toggle(boolean b){
		return !b;
	}
	
	/**
	 * toggles sprite rendering
	 */
	public static void toggleRenderSprites() {
		render_sprites = toggle(render_sprites);
	}
	
	/**
	 * toggles sprite rendering
	 */
	public static void toggleRenderTiles() {
		render_tiles = toggle(render_tiles);
	}
	
	/**
	 * toggles sprite rendering
	 */
	public static void toggleRenderUI() {
		render_ui = toggle(render_ui);
	}
	
	/**
	 * toggles sprite rendering
	 */
	public static void toggleRenderDebug() {
		render_debug = toggle(render_debug);
	}

	public static Map<String,ByteBuffer> getIdMaps() {
		return id_maps;
	}

	public static void setId_maps(Map<String,ByteBuffer> id_maps) {
		MapManager.id_maps = id_maps;
	}

	public static boolean isIdEditListCreated() {
		return idListCreated ;
	}

	public static void setIdEditListCreated(boolean b) {
		idListCreated = b;
	}
	
	@Override
	public void hide() {
		tileStage.addAction(Actions.alpha(0f, 1f));
		spriteStage.addAction(Actions.alpha(0f, 1f));
		debugStage.addAction(Actions.alpha(0f, 1f));
		smoothStage.addAction(Actions.alpha(0f, 1f));
		uiStage.addAction(Actions.alpha(0f, 1f));
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		batch.dispose();
		uiStage.dispose();
		debugStage.dispose();
		spriteStage.dispose();
		tileStage.dispose();
		smoothStage.dispose();
	}
	
	/**
	 * Updates the camera
	 */
	private void render_camera() {
		camera.update();		
	}

	@Override
	public void resize(int width, int height) {
	}

	public static MapManager getManager() {
		return m;
	}

	public static void setManager(MapManager m) {
		MapManager.m = m;
	}

	public static void toggleRenderSmootinhg() {
		render_smoothing = toggle(render_smoothing);
	}

	public static void addActorToTiles(Acteur acteur) {
		tileStage.addActor(acteur);
	}

	public static void addActorToSprites(Acteur acteur) {
		spriteStage.addActor(acteur);
	}

	public static void addActorToDebug(Acteur acteur) {
		debugStage.addActor(acteur);
	}

	public static void addActorToSmooth(Acteur acteur) {
		smoothStage.addActor(acteur);		
	}

	/**
	 * Removes actors that are not in chunk's limits
	 */
	public static void cleanMap() {
		cleanStage(tileStage);
		cleanStage(spriteStage);
		cleanStage(debugStage);
		cleanStage(smoothStage);
	}
	
	private static void cleanStage(Stage stage){
		//TODO voir pourquoi l'itération se passe un peu mal, on n'a que la moitié des tuiles à chaque fois...
		Iterator<Actor> iter_actors = stage.getActors().iterator();
		while(iter_actors.hasNext()){
			Actor act = iter_actors.next();
			Point point = PointsManager.getPoint(act.getX()/32, act.getY()/16);
			if(!Chunk.isPointInChunk(point)){
				act.remove();
			}
		}
	}
}
