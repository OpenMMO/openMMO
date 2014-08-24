package screens;

import java.awt.Point;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import opent4c.Acteur;
import opent4c.Chunk;
import opent4c.InputManager;
import opent4c.UpdateDataCheckStatus;
import opent4c.utils.FilesPath;
import opent4c.utils.LoadingStatus;
import opent4c.utils.Places;
import opent4c.utils.PointsManager;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

/**
 * This class manages the chunkMap.
 * On creation, the camera is at the center of chunk 5.
 * 
 * @author synoga
 *
 */
public class MapManager implements Screen{

	private static Logger logger = LogManager.getLogger(MapManager.class.getSimpleName());
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;

	private static Map<String,ByteBuffer> id_maps = new HashMap<String,ByteBuffer>(5);
	private static Map<Integer,Chunk> chunkMap = new ConcurrentHashMap<Integer,Chunk>(9);
	private static MapManager m;
	private static InputManager controller = null;
	private static TextButtonStyle style = new TextButtonStyle();
	private static TextButton OnScreenInfos, playerLocationInfo;
	private static OrthographicCamera camera;
	private static SpriteBatch batch;
	private static Stage tileStage, spriteStage, uiStage, sfxStage;
	private static boolean render_ui = true, render_sprites = true, render_tiles = true, render_sfx = true, idListCreated = false, highlighted = false;
	private static ScheduledFuture<?> highlight;
	private static Point highlight_point;
	private static Acteur highlight_tile;
	private static Map<Integer, Point> idEditList;
	private static Point playerPosition;
	public static final float blink_period = 1;

	/**
	 * Initializes the MapManager and binds the inputManager
	 */
	public static void init() {
		style.font = new BitmapFont();
		tileStage = new Stage();
		spriteStage = new Stage();
		sfxStage = new Stage();
		uiStage = new Stage();
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();
		tileStage.setCamera(camera);
		spriteStage.setCamera(camera);
		sfxStage.setCamera(camera);
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

	/**
	 * Creates the 9 Chunks from a starting point
	 * @param point
	 */
	public static void createChunks(Places point) {
		if(point == null){
			logger.warn("On essayer de créer des chunks d'un endroit null");
			return;
		}
		Chunk.stopChunkMapWatcher();
		resetWorldMap();
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point.getCoord());
		Iterator<Integer> iter_position = chunk_positions.keySet().iterator();
		while(iter_position.hasNext()){
			int chunkId = iter_position.next();
			//TODO attention plus tard en gérant plusieurs cartes.
			Chunk chunk = new Chunk(point.getMapName(),chunk_positions.get(chunkId));
			getWorldmap().put(chunkId, chunk);
			logger.info("Chunk : "+chunkId+" créé.");
		}
		Chunk.swapChunkCache();
		resetWorldMap();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		render_camera();
		//tileStage.act(delta);
		//spriteStage.act(delta);
		sfxStage.act(delta);
		uiStage.act(delta);
		if(render_ui)updateInfos();
		batch.begin();
			if(render_tiles)tileStage.draw();
			if(render_sprites)spriteStage.draw();
			if(render_sfx)sfxStage.draw();
			if(render_ui)uiStage.draw();
		batch.end();

	}

	/**
	 * Sets on screen texts
	 */
	private void updateInfos() {
		Gdx.app.getGraphics().setTitle("OpenT4C v0 FPS: " + Gdx.graphics.getFramesPerSecond() + " RAM : " + ((Runtime.getRuntime().totalMemory())/1024/1024) + " Mo");
		playerLocationInfo.setText("X: " + (((int)camera.position.x/32)) + " Y: " + (((int)camera.position.y/16)) + " Zoom : " + camera.zoom);
		playerLocationInfo.setPosition(playerLocationInfo.getWidth()*4, playerLocationInfo.getHeight());
	}

	/**
	 * Renders chunks
	 */
	public static void renderChunks() {
		tileStage.clear();
		if(render_tiles)tileStage.addActor(Chunk.getChunkTiles());
		spriteStage.clear();
		if(render_sprites)spriteStage.addActor(Chunk.getChunkSprites());
		Chunk.startChunkMapWatcher();
	}



	@Override
	public void show() {
		Places origin = Places.getPlace("startpoint");
		teleport(origin);
		Gdx.input.setInputProcessor(controller);
	}



	/**
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
	 * Teleports player to a given place and generates chunks.
	 * @param place
	 */
	public static void teleport(final Places place){
		if(place == null){
			logger.warn("On tente de se téléporter dans un endroit null");
			return;
		}
		createChunks(place);
		renderChunks();
		getCamera().position.x = place.getCoord().x * 32;
		getCamera().position.y = place.getCoord().y * 16;
		OnScreenInfos.clearActions();
		OnScreenInfos.setText(place.getNom());
		OnScreenInfos.getColor().a = 1f;
		OnScreenInfos.addAction(Actions.alpha(0f, 2));	
	}

	/**
	 * Pops up a menu to edit map at given coords 
	 * @param point
	 */
	public void editMapAtCoord(Point point) {
		int id = getIdAtCoordOnMap("v2_worldmap", point);
		logger.info("Open menu ID "+id+"@ "+point);
		new IdEditMenu(point);
		Gdx.input.setInputProcessor(null);
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


	
	public MapManager(ScreenManager screenManager){
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
	 * Checks if ChunkMap needs to be moved. If the camera's position is farther from the center of the chunk map than chunk_size/2.
	 * @param playerPosition
	 * @return direction to move to : 0 not to move, 1 to move right, 2 to move down right, 3 to move down, 4 to move down left, 5 to move left, 6 to move up left, 7 to move up, 8 to move up right.
	 */
	private static Point checkIfChunksNeedsToBeMoved() {
		Point chunkCenter = Chunk.getCenter();
		if((playerPosition.x*32 > chunkCenter.x*32+(Gdx.graphics.getWidth()/2)) ||
		(playerPosition.x*32 < chunkCenter.x*32-(Gdx.graphics.getWidth()/2)) ||
		(playerPosition.y*16 < chunkCenter.y*16-(Gdx.graphics.getHeight()/2)) ||
		(playerPosition.y*16 > chunkCenter.y*16+(Gdx.graphics.getHeight()/2))) return playerPosition;
		return null;
	}

	/**
	 * Updates last known player position
	 */
	public static void updatePlayerPosition(){
		playerPosition = PointsManager.getPoint(getCamera().position.x/32, getCamera().position.y/16);
	}
	
	/**
	 * Updates Chunks positions
	 */
	public static void updateChunkPositions() {
		updatePlayerPosition();
		Point newCoord = checkIfChunksNeedsToBeMoved();
		if (newCoord != null){
			Chunk.move(newCoord);
		}
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
		sfxStage.addActor(highlight_tile);
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
	
	public static Map<Integer,Chunk> getWorldmap() {
		return chunkMap;
	}

	public static void setWorldmap(Map<Integer,Chunk> worldmap) {
		MapManager.chunkMap = worldmap;
	}

	public static void resetWorldMap(){
		MapManager.chunkMap = new ConcurrentHashMap<Integer,Chunk>();
	}
	
	/**
	 * 
	 */
	public static void close_edit_menu() {
		Gdx.input.setInputProcessor(controller);
		unHighlight();
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
		Places place = new Places("Toggle Render Sprites : "+render_sprites, "v2_worldmap", playerPosition);
		teleport(place);
	}

	/**
	 * toggles sprite rendering
	 */
	public static void toggleRenderTiles() {
		render_tiles = toggle(render_tiles);
		Places place = new Places("Toggle Render Tiles : "+render_tiles, "v2_worldmap", playerPosition);
		teleport(place);
	}
	
	/**
	 * toggles sprite rendering
	 */
	public static void toggleRenderUI() {
		render_ui = toggle(render_ui);
		Places place = new Places("Toggle Render UI : "+render_ui, "v2_worldmap", playerPosition);
		teleport(place);
	}
	
	/**
	 * toggles sprite rendering
	 */
	public static void toggleRenderSfx() {
		render_sfx = toggle(render_sfx);
		Places place = new Places("Toggle Render  SFX : "+render_sfx, "v2_worldmap", playerPosition);
		teleport(place);
	}
	
	public static boolean doesRenderSprites() {
		return render_sprites;
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
		spriteStage.dispose();
		uiStage.dispose();
		sfxStage.dispose();
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
}
