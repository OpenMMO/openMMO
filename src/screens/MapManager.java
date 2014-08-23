package screens;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import opent4c.Acteur;
import opent4c.Chunk;
import opent4c.InputManager;
import opent4c.UpdateDataCheckStatus;
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
import com.badlogic.gdx.scenes.scene2d.Group;
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
	private static Map<String,ByteBuffer> id_maps = new HashMap<String,ByteBuffer>(5);
	private static Map<Integer,Chunk> worldmap = new ConcurrentHashMap<Integer,Chunk>(9);
	private static MapManager m;
	private static InputManager controller = null;
	private static TextButtonStyle style = new TextButtonStyle();
	private static TextButton status;
	private static TextButton info;
	private static OrthographicCamera camera;
	private static SpriteBatch batch;
	private static Stage stage;
	private static Stage ui;
	private static Stage highlight_stage;
	private static Group infos;
	private static boolean render_infos = true;
	private static boolean highlighted = false;
	private static ScheduledFuture<?> highlight;
	private static boolean renderSprites = true;
	private static Point highlight_point;
	private static Acteur highlight_tile;
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private static Map<Integer, Point> idEditList;
	private static boolean idListCreated = false;
	private static Point playerPosition;
	
	public static Map<Integer, Point> getIdEditList() {
		return idEditList;
	}

	public static void setIdEditList(Map<Integer, Point> idEditList) {
		MapManager.idEditList = idEditList;
	}

	public static final float blink_period = 1;

	
	public MapManager(ScreenManager screenManager){
		setM(this);
	}
	
	public static void createIdEditMap() {
		ThreadsUtil.executeInThread(RunnableCreatorUtil.getIdEditListCreatorRunnable());
	}

	/**
	 * Initializes the MapManager and binds the inputManager
	 */
	public static void init() {
		style.font = new BitmapFont();
		stage = new Stage();
		ui = new Stage();
		highlight_stage = new Stage();
		infos = new Group();
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();
		stage.setCamera(camera);
		highlight_stage.setCamera(camera);
		ui.addActor(infos);	
		setLoadInfos();
		controller = new InputManager(getM());
	}
	
	/**
	 * Sets the on screen texts
	 */
	private static void setLoadInfos(){
		status = new TextButton("status", style);
		status.setPosition(Gdx.graphics.getWidth()/2 + status.getWidth()/2, Gdx.graphics.getHeight()/2 - status.getHeight()/2);
		status.getColor().a = 0f;
		info = new TextButton("info", style);
		infos.addActor(status);
		infos.addActor(info);
	}
	
	/**
	 * Loads maps from .decrypt files
	 */
	public static void loadMaps() {
		logger.info("Chargement des cartes");
		UpdateDataCheckStatus.setStatus("Chargement des cartes");
		Places.createDefault();
		ThreadsUtil.executeInThread(RunnableCreatorUtil.getMapLoaderRunnable());
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
			//UpdateDataCheckStatus.setMapsStatus("Création du chunk :"+chunkId);
			Chunk chunk = new Chunk(point.getMap(),chunk_positions.get(chunkId));
			getWorldmap().put(chunkId, chunk);
			logger.info("Chunk : "+chunkId+" créé.");
		}
		Chunk.swapChunkCache();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		render_camera();
		//stage.act(delta);
		ui.act(delta);
		highlight_stage.act(delta);
		if(render_infos){
			render_infos();
		}		
		batch.begin();
			stage.draw();
			ui.draw();	
			highlight_stage.draw();
		batch.end();

	}

	/**
	 * Sets on screen texts
	 */
	private void render_infos() {
		Gdx.app.getGraphics().setTitle("OpenT4C v0 FPS: " + Gdx.graphics.getFramesPerSecond() + " RAM : " + ((Runtime.getRuntime().totalMemory())/1024/1024) + " Mo");
		info.setText("X: " + (((int)camera.position.x/32)) + " Y: " + (((int)camera.position.y/16)) + " Zoom : " + camera.zoom);
		info.setPosition(info.getWidth()*4, info.getHeight());
	}

	/**
	 * Renders chunks
	 */
	public static void renderChunks() {
		Group newChunksSprites = null;
		Group newChunksTiles = Chunk.getChunkTiles();
		if(renderSprites)newChunksSprites = Chunk.getChunkSprites();
		stage.clear();
		if(newChunksTiles != null)stage.addActor(newChunksTiles);
		if(newChunksSprites != null)stage.addActor(newChunksSprites);
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
		result = getIdAtCoord(getId_maps().get(carte), point);
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
		if(buf != null){
			try{
				buf.position((6144*(point.y))+(point.x*2));
			}catch(IllegalArgumentException e){
				logger.fatal(e);
				logger.fatal(point);
				e.printStackTrace();
				Gdx.app.exit();
			}
			b1 = buf.get();
			b2 = buf.get();
		}else{
			logger.fatal("Buffer de carte nul");
			Gdx.app.exit();
		}
		result = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
		return result;
	}









	/**
	 * Translates camera to a given place and generates chunks.
	 * @param place
	 */
	public static void teleport(final Places place){
		if(place == null){
			logger.warn("On tente de se téléporter dans un endroit null");
			return;
		}
		createChunks(place);
		renderChunks();
		//Gdx.app.postRunnable(RunnableCreatorUtil.getChunkRendererRunnable());
		getCamera().position.x = place.getCoord().x * 32;
		getCamera().position.y = place.getCoord().y * 16;
		status.clearActions();
		status.setText(place.getNom());
		status.getColor().a = 1f;
		status.addAction(Actions.alpha(0f, 2));	
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
	
	/**
	 * @return the Screen
	 */
	public static MapManager getScreen() {
		return getM();
	}
	
	/**
	 * Checks if ChunkMap needs to be moved. If the camera's position is farther from the center of the chunk map than chunk_size/2.
	 * @param playerPosition
	 * @return direction to move to : 0 not to move, 1 to move right, 2 to move down right, 3 to move down, 4 to move down left, 5 to move left, 6 to move up left, 7 to move up, 8 to move up right.
	 */
	private static Point checkIfChunksNeedsToBeMoved(Point playerPosition) {
		boolean right = false;
		boolean left = false;
		boolean up = false;
		boolean down = false;
		Point chunkCenter = getWorldmap().get(5).getCenter();
		//logger.info("ChunkWatcher : Player->"+playerPosition.x+";"+playerPosition.y + " Chunk->"+chunkCenter.x+";"+chunkCenter.y);
		if(playerPosition.x > chunkCenter.x*32+(Gdx.graphics.getWidth()/2)) right = true;
		if(playerPosition.x < chunkCenter.x*32-(Gdx.graphics.getWidth()/2)) left = true;
		if(playerPosition.y < chunkCenter.y*16-(Gdx.graphics.getHeight()/2)) up = true;
		if(playerPosition.y > chunkCenter.y*16+(Gdx.graphics.getHeight()/2)) down = true;
		if (right && !left && !down && !up) return playerPosition;
		if (right && !left && down && !up) return playerPosition;
		if (!right && !left && down && !up) return playerPosition;
		if (!right && left && down && !up) return playerPosition;
		if (!right && left && !down && !up) return playerPosition;
		if (!right && left && !down && up) return playerPosition;
		if (!right && !left && !down && up) return playerPosition;
		if (right && !left && !down && up) return playerPosition;
		return null;
	}
	
	/**
	 * Updates Chunks positions
	 */
	public static void updateChunkPositions() {
		playerPosition = PointsManager.getPoint(getCamera().position.x, getCamera().position.y);
		Point newCoord = checkIfChunksNeedsToBeMoved(playerPosition);
		if (newCoord != null){
			logger.info("Camera Move");
			moveChunksIfNeeded(newCoord);
		}
		//logger.info("Not moving");
	}

	/**
	 * Moves ChunkMap in a given direction
	 * @param direction
	 */
	private static void moveChunksIfNeeded(Point coord) {
		Chunk.move(coord);
	}
	
	/**
	 * @return the camera
	 */
	public static OrthographicCamera getCamera() {
		return getM().camera;
	}
	
	/**
	 * Hihglight a tile n the map
	 * @param point
	 */
	public static void highlight(Point point) {
		//logger.info("HIGHLIGHT");
		highlight_point = point;
		TextureAtlas atlas = loadingStatus.getTextureAtlasSprite("Highlight");
		TextureRegion tex = atlas.findRegion("Highlight Tile");
		highlight_tile = new Acteur(tex,highlight_point, PointsManager.getPoint(0, 0));
		highlight_tile.getColor().a = 0f;
		stage.addActor(highlight_tile);
		setHighlighted(true);
		highlight = ThreadsUtil.executePeriodicallyInThread(RunnableCreatorUtil.getHighlighterRunnable(), 0, (int)blink_period, TimeUnit.SECONDS);
	}

	/**
	 * 
	 */
	public static void tileFadeIn() {
		getHighlight_tile().getColor().a = 0.5f;
		/*Gdx.app.postRunnable(new Runnable(){
			@Override
			public void run() {
				getHighlight_tile().addAction(Actions.alpha(0.3f, blink_period/2));
			}
		});*/
	}

	/**
	 * 
	 */
	public static void tileFadeOut() {
		getHighlight_tile().getColor().a = 0f;
		/*Gdx.app.postRunnable(new Runnable(){
			@Override
			public void run() {
				getHighlight_tile().addAction(Actions.alpha(0f, blink_period/2));
			}
		})*/;
	}

	public static Acteur getHighlight_tile() {
		return highlight_tile;
	}

	public static void setHighlight_tile(Acteur highlight) {
		highlight_tile = highlight;
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
		return worldmap;
	}

	public static void setWorldmap(Map<Integer,Chunk> worldmap) {
		MapManager.worldmap = worldmap;
	}

	public static void resetWorldMap(){
		MapManager.worldmap = new ConcurrentHashMap<Integer,Chunk>();
	}
	
	/**
	 * 
	 */
	public static void close_edit_menu() {
		Gdx.input.setInputProcessor(controller);
		unHighlight();
	}

	/**
	 * 
	 */
	public static void toggleRenderSprites() {
		if(renderSprites){
			renderSprites = false;
			Point point = PointsManager.getPoint((int)camera.position.x/32,(int)camera.position.y/16);
			createChunks(new Places("ToggleRenderSprites", "v2_worldmap", point));
			renderChunks();
			return;
		}else{
			renderSprites  = true;
			Point point = PointsManager.getPoint((int)camera.position.x/32,(int)camera.position.y/16);
			createChunks(new Places("ToggleRenderSprites", "v2_worldmap", point));
			renderChunks();
		}
	}

	public static boolean doesRenderSprites() {
		return renderSprites;
	}

	public static Map<String,ByteBuffer> getId_maps() {
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
		stage.dispose();
		ui.dispose();
		highlight_stage.dispose();
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

	public static MapManager getM() {
		return m;
	}

	public static void setM(MapManager m) {
		MapManager.m = m;
	}
}
