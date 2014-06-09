package OpenT4C;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

import t4cPlugin.Acteur;
import t4cPlugin.FileLister;
import t4cPlugin.Lieux;
import t4cPlugin.utils.FilesPath;
import t4cPlugin.utils.PointsManager;
import t4cPlugin.utils.RunnableCreatorUtil;
import t4cPlugin.utils.ThreadsUtil;
import tools.DataInputManager;

/**
 * Classe qui gère les cartes. les chunks sont organisés comme suit :
 * 
 *    6 7 8
 *    5 0 1
 *    4 3 2
 * 
 * la caméra se situe toujours au centre du chunk 0 à la création.
 * 
 * @author synoga
 *
 */
public class MapManager implements Screen{

	private static Logger logger = LogManager.getLogger(MapManager.class.getSimpleName());
	private static Map<String,ByteBuffer> maps = new HashMap<String,ByteBuffer>(5);
	private static final Lieux startPoint = new Lieux("LH TEMPLE", "v2_worldmap",PointsManager.getPoint(2940,1065));
	private static final Lieux mapOrigin = new Lieux("ORIGIN", "v2_worldmap",PointsManager.getPoint(Gdx.graphics.getWidth()/64,Gdx.graphics.getHeight()/32));
	private static Map<Integer,Chunk> worldmap = new ConcurrentHashMap<Integer,Chunk>(9);
	private static MapManager m;
	//private static final Dimension chunk_size = new Dimension(4,4);//pour tester les chunks
	private static final Dimension chunk_size = new Dimension((Gdx.graphics.getWidth()/64)+3,(Gdx.graphics.getHeight()/32)+2);//On fait des chunks environ de la taille d' 1/4 de fenêtre, en nombre de tuiles, comme ça c'est transparent pour l'utilisateur sans charger trop de tuiles en mémoire.
	private InputManager controller = null;
	
	private TextButtonStyle style = new TextButtonStyle();
	private TextButton load;
	private TextButton status;
	private TextButton fps;
	private TextButton info;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Stage stage, ui;
	private Group menu, sprites, infos, tiles;
	private boolean debug = true;
	private static boolean do_render = false;
	
	public MapManager(){
		Gdx.app.postRunnable(new Runnable(){
			public void run(){
				style.font = new BitmapFont();
				stage = new Stage();
				ui = new Stage();
				menu = new Group();
				sprites = new Group();
				tiles = new Group();
				infos = new Group();
				batch = new SpriteBatch();
				camera = new OrthographicCamera();
				camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				//center camera to 0,0
				camera.translate(-Gdx.graphics.getWidth()/2,-Gdx.graphics.getHeight()/2);
				//center camera to start point
				camera.translate(startPoint.getCoord().x*32,startPoint.getCoord().y*16);				
				camera.update();
				stage.setCamera(camera);
				ui.addActor(menu);
				ui.addActor(infos);
				//logger.info("Position camera : "+camera.position);
				setLoadInfos();
				controller = new InputManager(m);
				Gdx.input.setInputProcessor(controller);
			}
		});

	}
	
	private void setLoadInfos(){
		load = new TextButton("load", style);
		load.setPosition(Gdx.graphics.getWidth()-200, 15);
		status = new TextButton("status", style);
		info = new TextButton("info", style);
		fps = new TextButton("fps", style);
		if (debug){
			fps = new TextButton(Gdx.graphics.getFramesPerSecond()+" fps", style);
		}
		fps.setPosition(Gdx.graphics.getWidth()-50, Gdx.graphics.getHeight()-30);
		infos.addActor(fps);
		infos.addActor(load);
		infos.addActor(status);
		infos.addActor(info);
	}
	
	public static void loadMap() {
		logger.info("Chargement des cartes");
		UpdateScreenManagerStatus.loadingMaps();
		List<File> decrypted_maps = FileLister.lister(new File(FilesPath.getDataDirectoryPath()), ".decrypt");
		Iterator<File> iter_decrypted_maps = decrypted_maps.iterator();
		while(iter_decrypted_maps.hasNext()){
			File f = iter_decrypted_maps.next();
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
			maps.put(f.getName().substring(0, f.getName().indexOf('.')),buf);
		}
		UpdateScreenManagerStatus.idle();
	}

	public static void createChunkMap() {
		UpdateScreenManagerStatus.creatingChunks();
		m = new MapManager();
		createChunks(startPoint);
		UpdateScreenManagerStatus.idle();
		logger.info(worldmap.get(0).getIds("v2_worldmap"));
	}

	private static void createChunks(Lieux point) {
		Map<Integer,Point> chunk_positions = computeChunkPositions(point.getCoord());
		Iterator<Integer> iter_position = chunk_positions.keySet().iterator();
		while(iter_position.hasNext()){
			int chunkId = iter_position.next();
			//TODO attention plus tard en gérant plusieurs cartes.
			worldmap.put(chunkId,new Chunk(point.getMap(),chunk_positions.get(chunkId)));
		}
		startChunkMapWatcher();
		m.renderChunks();
	}

	private static void startChunkMapWatcher() {
		Runnable r = RunnableCreatorUtil.getChunkMapWatcherRunnable();
		ThreadsUtil.executePeriodicallyInThread(r, 1, 50, TimeUnit.MILLISECONDS);
	}

	private static Map<Integer, Point> computeChunkPositions(Point startpoint) {
		Map<Integer,Point> result = new HashMap<Integer,Point>(9);
		Point chunk0, chunk1, chunk2, chunk3, chunk4, chunk5, chunk6, chunk7, chunk8;
		chunk0 = startpoint;
		result.put(0, chunk0);
		chunk1 = PointsManager.getPoint(chunk0.x+chunk_size.width-1, chunk0.y);
		result.put(1, chunk1);
		chunk2 = PointsManager.getPoint(chunk1.x, chunk1.y+chunk_size.height-1);
		result.put(2, chunk2);
		chunk3 = PointsManager.getPoint(chunk2.x-chunk_size.width+1, chunk2.y);
		result.put(3, chunk3);
		chunk4 = PointsManager.getPoint(chunk3.x-chunk_size.width+1, chunk3.y);
		result.put(4, chunk4);
		chunk5 = PointsManager.getPoint(chunk4.x, chunk4.y-chunk_size.height+1);
		result.put(5, chunk5);
		chunk6 = PointsManager.getPoint(chunk5.x, chunk5.y-chunk_size.height+1);
		result.put(6, chunk6);
		chunk7 = PointsManager.getPoint(chunk6.x+chunk_size.width-1, chunk6.y);
		result.put(7, chunk7);
		chunk8 = PointsManager.getPoint(chunk7.x+chunk_size.width-1, chunk7.y);
		result.put(8, chunk8);
		return result;
	}

	private boolean render_infos = true;


	private boolean stage_ready = true;

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		render_camera();
		stage.act(delta);
		ui.act(delta);
		batch.begin();
			if(stage_ready)stage.draw();
		batch.end();
		batch.begin();
			if(render_infos && do_render){
				render_infos();
				ui.draw();	
			}
		batch.end();
	}

	private void render_infos() {
		Gdx.app.getGraphics().setTitle("OpenT4C v0 FPS: " + Gdx.graphics.getFramesPerSecond() + " RAM : " + ((Runtime.getRuntime().totalMemory())/1024/1024) + " Mo");
		load.setText("Load Text : "+UpdateScreenManagerStatus.getReadableStatus());
		status.setText("Status Text : "+UpdateScreenManagerStatus.getReadableStatus());
		status.setPosition(200,20);
		if(debug)fps.setText(""+Gdx.graphics.getFramesPerSecond()+" fps");
		info.setText("X: " + (((int)camera.position.x/32)) + " Y: " + (((int)camera.position.y/16)) + " Zoom : " + camera.zoom);
		info.setPosition(100, camera.viewportHeight - 20);		
	}

	private void renderChunks() {
		stage_ready = false;
		sprites.clear();
		tiles.clear();
		Iterator<Integer> iter_chunk = worldmap.keySet().iterator();
		while (iter_chunk.hasNext()){
			int key = iter_chunk.next();
			renderChunk(worldmap.get(key));
		}
		stage.addActor(tiles);
		stage.addActor(sprites);
		stage_ready = true;
	}

	private void renderChunk(Chunk chunk) {
		Map<Point, Sprite> tile_list = chunk.getTiles();
		Iterator<Point> iter_tiles = tile_list.keySet().iterator();
		while(iter_tiles.hasNext()){
			Point pt = iter_tiles.next();
			Sprite sp = tile_list.get(pt);
			Point offset = PointsManager.getPoint(0,0);
			float spx = (sp.getScaleX()*offset.x)+(pt.x*32);
			float spy = (sp.getScaleY()*offset.y)+(pt.y*16);
			sp.setPosition(spx, spy);
			//logger.info(pt.x+";"+pt.y);
			tiles.addActor(new Acteur(sp));
		}		
		Map<Point, Sprite> sprite_list = chunk.getSprites();
		Iterator<Point> iter_sprite = sprite_list.keySet().iterator();
		while(iter_sprite.hasNext()){
			Point pt = iter_sprite.next();
			Sprite sp = sprite_list.get(pt);
			Point offset = PointsManager.getPoint(0,0);
			float spx = (sp.getScaleX()*offset.x)+(pt.x*32);
			float spy = (sp.getScaleY()*offset.y)+(pt.y*16);
			sp.setPosition(spx, spy);
			//logger.info(pt.x+";"+pt.y);
			sprites.addActor(new Acteur(sp));
		}
	}

	private void render_camera() {
		camera.update();		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		batch.dispose();
		stage.dispose();
		ui.dispose();		
	}

	public static Lieux getStartpoint() {
		return startPoint;
	}

	public static Dimension getChunkSize() {
		return chunk_size;
	}

	public static int getIdAtCoordOnMap(String carte, Point point) {
		int result = -1;
		result = getIdAtCoord(maps.get(carte), point);
		return result;
	}

	private static int getIdAtCoord(ByteBuffer buf, Point point) {
		int result = -1;
		byte b1=0,b2=0;
		if(buf != null){
			try{
				buf.position((6144*point.y)+(point.x*2));
			}catch(IllegalArgumentException e){
				logger.fatal(e);
				logger.fatal(point);
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

	public static MapManager getScreen() {
		return m;
	}

	public static void updateChunkPositions() {
		Point playerPosition = PointsManager.getPoint(m.camera.position.x/32, m.camera.position.y/16);
		int direction = checkIfChunksNeedsToBeMoved(playerPosition);
		moveChunksIfNeeded(direction);
	}

	private static void moveChunksIfNeeded(int direction) {
		switch(direction){
			case 0 : /*logger.info("Pas besoin de déplacer les chunks");*/ break;
			case 1 :  moveChunksRight(); break;
			case 2 : moveChunksDownRight(); break;
			case 3 : moveChunksDown(); break;
			case 4 : moveChunksDownLeft(); break;
			case 5 : moveChunksLeft(); break;
			case 6 : moveChunksUpLeft(); break;
			case 7 : moveChunksUp(); break;
			case 8 : moveChunksUpRight(); break;
		}
	}

	private static void moveChunksUpRight() {
		logger.info("Déplacement à droite et en haut");
		Point point = worldmap.get(8).getCenter();
		worldmap.put(4, worldmap.get(0));
		worldmap.put(5, worldmap.get(7));
		worldmap.put(3, worldmap.get(1));
		worldmap.put(0, worldmap.get(8));
		Map<Integer,Point> chunk_positions = computeChunkPositions(point);
		worldmap.put(6,new Chunk("v2_worldmap",chunk_positions.get(6)));
		worldmap.put(7,new Chunk("v2_worldmap",chunk_positions.get(7)));
		worldmap.put(8,new Chunk("v2_worldmap",chunk_positions.get(8)));
		worldmap.put(1,new Chunk("v2_worldmap",chunk_positions.get(1)));
		worldmap.put(2,new Chunk("v2_worldmap",chunk_positions.get(2)));
		m.renderChunks();
	}

	private static void moveChunksUp() {
		logger.info("Déplacement en haut"); 
		Point point = worldmap.get(7).getCenter();
		worldmap.put(2, worldmap.get(1));
		worldmap.put(3, worldmap.get(0));
		worldmap.put(4, worldmap.get(5));
		worldmap.put(5, worldmap.get(6));
		worldmap.put(0, worldmap.get(7));
		worldmap.put(1, worldmap.get(8));
		Map<Integer,Point> chunk_positions = computeChunkPositions(point);
		worldmap.put(6,new Chunk("v2_worldmap",chunk_positions.get(6)));
		worldmap.put(7,new Chunk("v2_worldmap",chunk_positions.get(7)));
		worldmap.put(8,new Chunk("v2_worldmap",chunk_positions.get(8)));
		m.renderChunks();
	}

	private static void moveChunksUpLeft() {
		logger.info("Déplacement à gauche et en haut");
		Point point = worldmap.get(6).getCenter();
		worldmap.put(2, worldmap.get(0));
		worldmap.put(3, worldmap.get(5));
		worldmap.put(1, worldmap.get(7));
		worldmap.put(0, worldmap.get(6));
		Map<Integer,Point> chunk_positions = computeChunkPositions(point);
		worldmap.put(4,new Chunk("v2_worldmap",chunk_positions.get(4)));
		worldmap.put(5,new Chunk("v2_worldmap",chunk_positions.get(5)));
		worldmap.put(6,new Chunk("v2_worldmap",chunk_positions.get(6)));
		worldmap.put(7,new Chunk("v2_worldmap",chunk_positions.get(7)));
		worldmap.put(8,new Chunk("v2_worldmap",chunk_positions.get(8)));
		m.renderChunks();
	}

	private static void moveChunksLeft() {
		logger.info("Déplacement à gauche");
		Point point = worldmap.get(5).getCenter();
		worldmap.put(8, worldmap.get(7));
		worldmap.put(1, worldmap.get(0));
		worldmap.put(2, worldmap.get(3));
		worldmap.put(7, worldmap.get(6));
		worldmap.put(0, worldmap.get(5));
		worldmap.put(3, worldmap.get(4));
		Map<Integer,Point> chunk_positions = computeChunkPositions(point);
		worldmap.put(4,new Chunk("v2_worldmap",chunk_positions.get(4)));
		worldmap.put(5,new Chunk("v2_worldmap",chunk_positions.get(5)));
		worldmap.put(6,new Chunk("v2_worldmap",chunk_positions.get(6)));
		m.renderChunks();
	}

	private static void moveChunksDownLeft() {
		logger.info("Déplacement à gauche et en bas");
		Point point = worldmap.get(4).getCenter();
		worldmap.put(8, worldmap.get(0));
		worldmap.put(7, worldmap.get(5));
		worldmap.put(1, worldmap.get(3));
		worldmap.put(0, worldmap.get(4));
		Map<Integer,Point> chunk_positions = computeChunkPositions(point);
		worldmap.put(2,new Chunk("v2_worldmap",chunk_positions.get(2)));
		worldmap.put(3,new Chunk("v2_worldmap",chunk_positions.get(3)));
		worldmap.put(4,new Chunk("v2_worldmap",chunk_positions.get(4)));
		worldmap.put(5,new Chunk("v2_worldmap",chunk_positions.get(5)));
		worldmap.put(6,new Chunk("v2_worldmap",chunk_positions.get(6)));
		m.renderChunks();
	}

	private static void moveChunksDown() {
		logger.info("Déplacement en bas");
		Point point = worldmap.get(3).getCenter();
		worldmap.put(6, worldmap.get(5));
		worldmap.put(7, worldmap.get(0));
		worldmap.put(8, worldmap.get(1));
		worldmap.put(5, worldmap.get(4));
		worldmap.put(0, worldmap.get(3));
		worldmap.put(1, worldmap.get(2));
		Map<Integer,Point> chunk_positions = computeChunkPositions(point);
		worldmap.put(4,new Chunk("v2_worldmap",chunk_positions.get(4)));
		worldmap.put(3,new Chunk("v2_worldmap",chunk_positions.get(3)));
		worldmap.put(2,new Chunk("v2_worldmap",chunk_positions.get(2)));
		m.renderChunks();
	}

	private static void moveChunksDownRight() {
		logger.info("Déplacement à droite et en bas");
		Point point = worldmap.get(2).getCenter();
		worldmap.put(6, worldmap.get(0));
		worldmap.put(5, worldmap.get(3));
		worldmap.put(7, worldmap.get(1));
		worldmap.put(0, worldmap.get(2));
		Map<Integer,Point> chunk_positions = computeChunkPositions(point);
		worldmap.put(4,new Chunk("v2_worldmap",chunk_positions.get(4)));
		worldmap.put(3,new Chunk("v2_worldmap",chunk_positions.get(3)));
		worldmap.put(2,new Chunk("v2_worldmap",chunk_positions.get(2)));
		worldmap.put(1,new Chunk("v2_worldmap",chunk_positions.get(1)));
		worldmap.put(8,new Chunk("v2_worldmap",chunk_positions.get(8)));
		m.renderChunks();
	}

	private static void moveChunksRight() {
		logger.info("Déplacement à droite");	
		Point point = worldmap.get(1).getCenter();
		worldmap.put(6, worldmap.get(7));
		worldmap.put(5, worldmap.get(0));
		worldmap.put(4, worldmap.get(3));
		worldmap.put(7, worldmap.get(8));
		worldmap.put(0, worldmap.get(1));
		worldmap.put(3, worldmap.get(2));
		Map<Integer,Point> chunk_positions = computeChunkPositions(point);
		worldmap.put(8,new Chunk("v2_worldmap",chunk_positions.get(8)));
		worldmap.put(1,new Chunk("v2_worldmap",chunk_positions.get(1)));
		worldmap.put(2,new Chunk("v2_worldmap",chunk_positions.get(2)));
		m.renderChunks();
	}

	/**
	 * compare la position du jouer par rapport à la position du chunk central.
	 * Si on déborde du chunk dans ue direction ou une autre, on donne la direction
	 * on doit déplacer les chunks.
	 * @param playerPosition
	 * @return la direction dans laquelle on se déplace : 0 pour ne pas déplacer, 1 à droite, 2 en bas à droite, 3 en bas, 4 en bas à gauche, 5 à gauche, 6 en haut à gauche, 7 en haut, 8 en haut à droite.
	 */
	private static int checkIfChunksNeedsToBeMoved(Point playerPosition) {
		int result = 0;
		boolean right = false;
		boolean left = false;
		boolean up = false;
		boolean down = false;
		Point chunkCenter = worldmap.get(0).getCenter();
		//logger.info("ChunkWatcher : Player->"+playerPosition.x+";"+playerPosition.y + " Chunk->"+chunkCenter.x+";"+chunkCenter.y);
		//doit-on bouger à droite?
		if(playerPosition.x > chunkCenter.x+(chunk_size.width/2)) right = true;
		//doit-on bouger à gauche?
		if(playerPosition.x < chunkCenter.x-(chunk_size.width/2)) left = true;
		//doit-on bouger en haut?
		if(playerPosition.y < chunkCenter.y-(chunk_size.height/2)) up = true;
		//doit-on bouger en bas?
		if(playerPosition.y > chunkCenter.y+(chunk_size.height/2)) down = true;
		//On déduit la direction.
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

	public static void setReadyToRender() {
		if(m == null){
			logger.fatal("On essaye d'activer le rendu avant d'avoir instancié correctement MapManager.");
			System.exit(1);
		}
		do_render = true;
	}

	public OrthographicCamera getCamera() {
		return m.camera;
	}

}
