package screens;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
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
import opent4c.MapPixel;
import opent4c.SpriteData;
import opent4c.TilePixel;
import opent4c.UpdateScreenManagerStatus;
import opent4c.utils.ChunkMovement;
import opent4c.utils.FileLister;
import opent4c.utils.FilesPath;
import opent4c.utils.LoadingStatus;
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

import t4cPlugin.Places;
import tools.DataInputManager;

/**
 * This class manages the chunkMap.
 * On creation, the camera is at the center of chunk 0.
 * 
 * @author synoga
 *
 */
public class MapManager implements Screen{

	private static Logger logger = LogManager.getLogger(MapManager.class.getSimpleName());
	private static Map<String,ByteBuffer> id_maps = new HashMap<String,ByteBuffer>(5);
	private static Map<Integer,Chunk> worldmap = new ConcurrentHashMap<Integer,Chunk>(16);
	private static MapManager m;
	//private static final Dimension chunk_size = new Dimension(4,4);//pour tester les chunks
	//private static final Dimension chunk_size = new Dimension((Gdx.graphics.getWidth()/96),(Gdx.graphics.getHeight()/48));//On fait des chunks environ de la taille d' 1/9 de fenêtre, en nombre de tuiles, comme ça c'est transparent pour l'utilisateur sans charger trop de tuiles en mémoire.
	private static final Dimension chunk_size = new Dimension(4*(Gdx.graphics.getWidth()/32)/5,4*(Gdx.graphics.getHeight()/16)/5);//de la taille d'2/3 d'écran, en nombre de tuiles
	private InputManager controller = null;
	private TextButtonStyle style = new TextButtonStyle();
	private TextButton load;
	private static TextButton status;
	private TextButton fps;
	private TextButton info;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private static Stage stage;
	private Stage ui;
	private Stage highlight_stage;
	private Group menu, infos;
	private boolean render_infos = true;
	private boolean menu_poped = false;
	private static boolean stage_ready = true;
	private ScreenManager sm;
	private static boolean highlighted = false;
	private static ScheduledFuture<?> highlight;
	private Point highlight_point;
	private Acteur highlight_tile;
	private LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private Edit_menu edit_menu = null;
	public static final float blink_period = 3;

	
	public MapManager(ScreenManager screenManager){
		m = this;
		sm = screenManager;
		Gdx.app.postRunnable(new Runnable(){
			public void run(){
				init();
				controller = new InputManager(m);
				Gdx.input.setInputProcessor(controller);
			}
		});
	}
	
	/**
	 * Initializes the MapManager and binds the inputManager
	 */
	private void init() {
		style.font = new BitmapFont();
		stage = new Stage();
		ui = new Stage();
		highlight_stage = new Stage();
		menu = new Group();
		infos = new Group();
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();
		stage.setCamera(camera);
		highlight_stage.setCamera(camera);
		ui.addActor(menu);
		ui.addActor(infos);	
		setLoadInfos();
	}
	
	/**
	 * Sets the on screen texts
	 */
	private void setLoadInfos(){
		load = new TextButton("load", style);
		load.setPosition(Gdx.graphics.getWidth()-200, 15);
		status = new TextButton("status", style);
		status.setPosition(Gdx.graphics.getWidth()/2 + status.getWidth()/2, Gdx.graphics.getHeight()/2 - status.getHeight()/2);
		status.getColor().a = 0f;
		info = new TextButton("info", style);
		fps = new TextButton("fps", style);
		fps.setPosition(Gdx.graphics.getWidth()-50, Gdx.graphics.getHeight()-30);
		infos.addActor(fps);
		infos.addActor(load);
		infos.addActor(status);
		infos.addActor(info);
	}
	
	/**
	 * Loads maps from .decrypt files
	 */
	public void loadMaps() {
		logger.info("Chargement des cartes");
		Places.createDefault();

		List<File> decrypted_maps = FileLister.lister(new File(FilesPath.getDataDirectoryPath()), ".decrypt");
		Iterator<File> iter_decrypted_maps = decrypted_maps.iterator();
		while(iter_decrypted_maps.hasNext()){
			File f = iter_decrypted_maps.next();
			UpdateScreenManagerStatus.setMapsStatus("Chargement carte : "+f.getName());
			ByteBuffer buf = ByteBuffer.allocate((int)f.length());
			try {
				DataInputManager in = new DataInputManager (f);
				while (buf.position() < buf.capacity()){
					buf.put(in.readByte());
				}
				in.close();
			}catch(IOException exc){
				exc.printStackTrace();
				System.exit(1);
			}
			buf.rewind();
			id_maps.put(f.getName().substring(0, f.getName().indexOf('.')),buf);
		}
	}

	/**
	 * Creates the chunkMap, a group of 25 Chunks
	 */
	public void createChunkMap() {
		teleport(Places.getPlace("startpoint"));
		sm.switchGameScreen(m);
	}

	/**
	 * Creates the 9 Chunks from a starting point
	 * @param point
	 */
	private static void createChunks(Places point) {
		if(point == null){
			logger.warn("On essayer de créer des chunks d'un endroit null");
			return;
		}
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point.getCoord(),chunk_size);
		Iterator<Integer> iter_position = chunk_positions.keySet().iterator();
		while(iter_position.hasNext()){
			int chunkId = iter_position.next();
			//TODO attention plus tard en gérant plusieurs cartes.
			UpdateScreenManagerStatus.setMapsStatus("Création du chunk :"+chunkId);
			getWorldmap().put(chunkId,new Chunk(point.getMap(),chunk_positions.get(chunkId)));
		}
		Chunk.startChunkMapWatcher();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		render_camera();
		stage.act(delta);
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
		load.setText("foo");
		fps.setText(""+Gdx.graphics.getFramesPerSecond()+" fps");
		info.setText("X: " + (((int)camera.position.x/32)) + " Y: " + (((int)camera.position.y/16)) + " Zoom : " + camera.zoom);
		info.setPosition(info.getWidth()*4, info.getHeight());
	}

	/**
	 * Renders chunks
	 */
	public static void renderChunks() {
		Group newChunksTiles = new Group(); 
		Group newChunksSprites = new Group(); 
		Iterator<Integer> iter_chunk = getWorldmap().keySet().iterator();
		while (iter_chunk.hasNext()){
			int key = iter_chunk.next();
			newChunksTiles.addActor(renderChunkTiles(getWorldmap().get(key)));
			if (key < 9)newChunksSprites.addActor(renderChunkSprites(getWorldmap().get(key)));
		}
		Gdx.app.postRunnable(RunnableCreatorUtil.getChunkSwapperRunnable(stage, newChunksTiles, newChunksSprites));
	}

	/**
	 * renders a given Chunk
	 * @param chunk
	 * @return
	 */
	private static Group renderChunkTiles(Chunk chunk) {
		Group result = new Group();
		Iterator<Acteur> iter_tiles = chunk.getTiles().iterator();
		while(iter_tiles.hasNext()){
			Acteur tile = iter_tiles.next();
			result.addActor(tile);
		}
		return result;
	}

	/**
	 * renders a given Chunk
	 * @param chunk
	 * @return
	 */
	private static Group renderChunkSprites(Chunk chunk) {
		Group result = new Group();
		Iterator<Acteur> iter_sprite = chunk.getSprites().iterator();
		while(iter_sprite.hasNext()){
			Acteur pt = iter_sprite.next();
			result.addActor(pt);
		}
		return result;
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

	@Override
	public void show() {
		if (m == null){
			logger.warn("Attention on essaye de renre les chunks d'un MapManager non-instancié.");
		}else{
			MapManager.renderChunks();
		}
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
	 * @return the chunk's size
	 */
	public static Dimension getChunkSize() {
		return chunk_size;
	}

	/**
	 * @param carte
	 * @param point
	 * @return the id at given coordinates on a given map name
	 */
	public static int getIdAtCoordOnMap(String carte, Point point) {
		int result = -1;
		result = getIdAtCoord(id_maps.get(carte), point);
		return result;
	}

	/**
	 * @param buf
	 * @param point
	 * @return the id at given coordinates from a ByteBuffer (map)
	 */
	private static int getIdAtCoord(ByteBuffer buf, Point point) {
		int result = -1;
		if(point.x < 0 || point.x > 3071 || point.y < 0 || point.y > 3071) return -1;
		byte b1=0,b2=0;
		if(buf != null){
			try{
				buf.position((6144*(point.y))+(point.x*2));
			}catch(IllegalArgumentException e){
				logger.fatal(e);
				logger.fatal(point);
				e.printStackTrace();
				System.exit(1);
			}
			b1 = buf.get();
			b2 = buf.get();
		}else{
			logger.fatal("Buffer de carte nul");
			System.exit(1);
		}
		result = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
		return result;
	}

	/**
	 * @return the Screen
	 */
	public static MapManager getScreen() {
		return m;
	}

	/**
	 * Updates Chunks positions
	 */
	public static void updateChunkPositions() {
		Point playerPosition = PointsManager.getPoint(m.camera.position.x/32, m.camera.position.y/16);
		int direction = checkIfChunksNeedsToBeMoved(playerPosition);
		moveChunksIfNeeded(direction);
	}

	/**
	 * Moves ChunkMap in a given direction
	 * @param direction
	 */
	private static void moveChunksIfNeeded(int direction) {
		switch(direction){
			case 0 : /*logger.info("Pas besoin de déplacer les chunks");*/ break;
			case 1 : ChunkMovement.moveChunksRight(getWorldmap()); break;
			case 2 : ChunkMovement.moveChunksDownRight(getWorldmap()); break;
			case 3 : ChunkMovement.moveChunksDown(getWorldmap()); break;
			case 4 : ChunkMovement.moveChunksDownLeft(getWorldmap()); break;
			case 5 : ChunkMovement.moveChunksLeft(getWorldmap()); break;
			case 6 : ChunkMovement.moveChunksUpLeft(getWorldmap()); break;
			case 7 : ChunkMovement.moveChunksUp(getWorldmap()); break;
			case 8 : ChunkMovement.moveChunksUpRight(getWorldmap()); break;
		}
	}



	/**
	 * Checks if ChunkMap needs to be moved. If the camera's position is farther from the center of the chunk map than chunk_size/2.
	 * @param playerPosition
	 * @return direction to move to : 0 not to move, 1 to move right, 2 to move down right, 3 to move down, 4 to move down left, 5 to move left, 6 to move up left, 7 to move up, 8 to move up right.
	 */
	private static int checkIfChunksNeedsToBeMoved(Point playerPosition) {
		int result = 0;
		boolean right = false;
		boolean left = false;
		boolean up = false;
		boolean down = false;
		Point chunkCenter = getWorldmap().get(0).getCenter();
		//logger.info("ChunkWatcher : Player->"+playerPosition.x+";"+playerPosition.y + " Chunk->"+chunkCenter.x+";"+chunkCenter.y);
		if(playerPosition.x > chunkCenter.x+(chunk_size.width/2)) right = true;
		if(playerPosition.x < chunkCenter.x-(chunk_size.width/2)) left = true;
		if(playerPosition.y < chunkCenter.y-(chunk_size.height/2)) up = true;
		if(playerPosition.y > chunkCenter.y+(chunk_size.height/2)) down = true;
		if (right && !left && !down && !up) result = 1;
		if (right && !left && down && !up) result = 2;
		if (!right && !left && down && !up) result = 3;
		if (!right && left && down && !up) result = 4;
		if (!right && left && !down && !up) result = 5;
		if (!right && left && !down && up) result = 6;
		if (!right && !left && !down && up) result = 7;
		if (right && !left && !down && up) result = 8;
		return result;
	}

	/**
	 * @return the camera
	 */
	public static OrthographicCamera getCamera() {
		return m.camera;
	}

	/**
	 * Focuses on the unmapped ids to fix them
	 */
	public void editNextUnmappedID() {
		logger.info("Edition de la prochaine id non mappée.");
		Iterator<Integer> iter_id = getWorldmap().get(0).getUnmappedIds().keySet().iterator();
		while(iter_id.hasNext()){
			logger.info("ID non mappée : "+iter_id.next());
		}
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
		if(!stage_ready)return;
		Chunk.stopChunkMapWatcher();
		createChunks(place);
		renderChunks();
		Gdx.app.postRunnable(new Runnable(){
			@Override
			public void run() {
				getCamera().position.x = place.getCoord().x * 32;
				getCamera().position.y = place.getCoord().y * 16;
				status.clearActions();
				status.setText(place.getNom());
				status.getColor().a = 1f;
				status.addAction(Actions.alpha(0f, 2));				
			}
		});

	}

	/**
	 * Pops up a menu to edit map at given coords 
	 * @param point
	 */
	public void editMapAtCoord(Point point) {
		logger.info("Open menu @ "+point);
		int id = getIdAtCoordOnMap("v2_worldmap", point);
		if(SpriteData.isTileId(id)){
			edit_menu  = new Edit_menu(point, worldmap.get(0).getTileOnMapFromId(id,point.x,point.y), highlight_stage, true);
		}else{
			edit_menu  = new Edit_menu(point, worldmap.get(0).getSpriteOnMapFromId(id,point.x,point.y), highlight_stage, false);
		}
		ui.addActor(edit_menu);
		Gdx.input.setInputProcessor(edit_menu);
	}

	/**
	 * Hihglight a tile n the map
	 * @param point
	 */
	public void highlight(Point point) {
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
		return m.highlight_tile;
	}

	public static void setHighlight_tile(Acteur highlight_tile) {
		m.highlight_tile = highlight_tile;
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

	/**
	 * 
	 */
	public static void close_edit_menu() {
		m.edit_menu = null;
		unHighlight();
		Gdx.input.setInputProcessor(m.controller);
	}
}
