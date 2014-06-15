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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

import t4cPlugin.Acteur;
import t4cPlugin.FileLister;
import t4cPlugin.IG_Menu;
import t4cPlugin.Places;
import t4cPlugin.MapPixel;
import t4cPlugin.utils.FilesPath;
import t4cPlugin.utils.PointsManager;
import tools.DataInputManager;

/**
 * This class manages the chunkMap., chunk are placed as follow :
 * 
 *    6 7 8
 *    5 0 1
 *    4 3 2
 * 
 * At creation, the camera is at the center of chunk 0.
 * 
 * @author synoga
 *
 */
public class MapManager implements Screen{

	private static Logger logger = LogManager.getLogger(MapManager.class.getSimpleName());
	private static Map<String,ByteBuffer> id_maps = new HashMap<String,ByteBuffer>(5);
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
	private boolean render_infos = true;
	private boolean stage_ready = true;
	
	public MapManager(){
		Gdx.app.postRunnable(new Runnable(){
			public void run(){
				init();
				controller = new InputManager(m);
				Gdx.input.setInputProcessor(controller);
			}
		});
	}
	
	/**
	 * Initializes the MapManager and binds the inpuManager
	 */
	private void init() {
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
		camera.translate(-Gdx.graphics.getWidth()/2,-Gdx.graphics.getHeight()/2);
		camera.translate(Places.getPlace("startpoint").getCoord().x*32,Places.getPlace("startpoint").getCoord().y*16);				
		camera.update();
		stage.setCamera(camera);
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
	
	/**
	 * Loads maps from .decrypt files
	 */
	public static void loadMaps() {
		logger.info("Chargement des cartes");
		UpdateScreenManagerStatus.loadingMaps();
		Places.createDefault();

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
			id_maps.put(f.getName().substring(0, f.getName().indexOf('.')),buf);
		}
		UpdateScreenManagerStatus.idle();
	}

	/**
	 * Creates the chunkMap, a group of 9 Chunks
	 */
	public static void createChunkMap() {
		UpdateScreenManagerStatus.creatingChunks();
		m = new MapManager();
		createChunks(Places.getPlace("startpoint"));
		UpdateScreenManagerStatus.idle();
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		logger.info(worldmap.get(0).getIds("v2_worldmap"));
	}

	/**
	 * Creates the 9 Chunks from a starting point
	 * @param point
	 */
	private static void createChunks(Places point) {
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point.getCoord(),chunk_size);
		Iterator<Integer> iter_position = chunk_positions.keySet().iterator();
		while(iter_position.hasNext()){
			int chunkId = iter_position.next();
			//TODO attention plus tard en gérant plusieurs cartes.
			worldmap.put(chunkId,new Chunk(point.getMap(),chunk_positions.get(chunkId)));
		}
		Chunk.startChunkMapWatcher();
		m.renderChunks();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		render_camera();
		stage.act(delta);
		ui.act(delta);
		batch.begin();
			if(stage_ready && do_render)stage.draw();
		batch.end();
		batch.begin();
			if(render_infos && do_render){
				render_infos();
				ui.draw();	
			}
		batch.end();
	}

	/**
	 * Sets on screen texts
	 */
	private void render_infos() {
		Gdx.app.getGraphics().setTitle("OpenT4C v0 FPS: " + Gdx.graphics.getFramesPerSecond() + " RAM : " + ((Runtime.getRuntime().totalMemory())/1024/1024) + " Mo");
		load.setText("Load Text : "+UpdateScreenManagerStatus.getReadableStatus());
		status.setText("Status Text : "+UpdateScreenManagerStatus.getReadableStatus());
		status.setPosition(200,20);
		if(debug)fps.setText(""+Gdx.graphics.getFramesPerSecond()+" fps");
		info.setText("X: " + (((int)camera.position.x/32)) + " Y: " + (((int)camera.position.y/16)) + " Zoom : " + camera.zoom);
		info.setPosition(100, camera.viewportHeight - 20);		
	}

	/**
	 * Renders chunks
	 */
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

	/**
	 * renders a given Chunk
	 * @param chunk
	 */
	private void renderChunk(Chunk chunk) {
		Map<Point, Sprite> tile_list = chunk.getTiles();
		Iterator<Point> iter_tiles = tile_list.keySet().iterator();
		while(iter_tiles.hasNext()){
			Point pt = iter_tiles.next();
			Sprite sp = tile_list.get(pt);
			Point offset = PointsManager.getPoint(0,0);
			float spx = /*-(sp.getScaleX()*offset.x)+*/(pt.x*32);
			float spy = /*-(sp.getScaleY()*offset.y)+*/(pt.y*16);
			sp.setPosition(spx, spy);
			//logger.info(pt.x+";"+pt.y);
			tiles.addActor(new Acteur(sp));
		}		
		Map<Point, Sprite> sprite_list = chunk.getSprites();
		Iterator<Point> iter_sprite = sprite_list.keySet().iterator();
		while(iter_sprite.hasNext()){
			Point pt = iter_sprite.next();
			Sprite sp = sprite_list.get(pt);
			Point offset = chunk.getOffsetFromPoint(pt);
			float spx = (sp.getScaleX()*offset.x)+(pt.x*32);
			float spy = (sp.getScaleY()*offset.y)+(pt.y*16);
			sp.setPosition(spx, spy);
			//logger.info(pt.x+";"+pt.y);
			sprites.addActor(new Acteur(sp));
		}
	}

	/**
	 * Clears the on screen menu
	 */
	public void clearMenu(){
		menu.clear();
	}
	
	/**
	 * @param point
	 * @param pixel
	 * @return informations from a MapPixel and a point
	 */
	private String getInfoPixel(Point point, MapPixel pixel) {
		return point.x +","+ point.y +" "+pixel.getAtlas()+" "+pixel.getTex()+" id : "+pixel.getId()+" Modulo : "+pixel.getModulo().x+","+pixel.getModulo().y;
	}
	
	/**
	 * Pops up a menu to display informations at given coordinates
	 * @param screenX
	 * @param screenY
	 */
	public void pop_menu(int screenX, int screenY) {
		Point p = PointsManager.getPoint((int)((screenX+camera.position.x-camera.viewportWidth/2)/(32/camera.zoom)),(int)((screenY+camera.position.y-camera.viewportHeight/2)/(16/camera.zoom)));
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		if (worldmap.get(0).getPixelAtCoord("v2_worldmap", p) != null){
			MapPixel px = worldmap.get(0).getPixelAtCoord("v2_worldmap", p);
			String status = getInfoPixel(p, px);
			logger.info(status);
			if(px.isTuile()){

				TextButton pixel_info0 = new TextButton(p.x +","+ p.y+" : "+px.getAtlas()+" "+px.getTex(),style);
				TextButton pixel_info1 = new TextButton("id : "+getIdAtCoordOnMap("v2_worldmap", PointsManager.getPoint(p.x, p.y))+" Modulo : "+px.getModulo().x+","+px.getModulo().y,style);
				TextButton pixel_info2 = new TextButton("ettre ici les infos concernant l'id",style);
				pixel_info0.setPosition(screenX+10,(int)(camera.viewportHeight-screenY+5));
				pixel_info1.setPosition(screenX+10,(int)(camera.viewportHeight-screenY+25));
				pixel_info2.setPosition(screenX+10,(int)(camera.viewportHeight-screenY+45));

				Sprite sp = worldmap.get(0).getTileAtCoord("v2_worldmap", p);
				sp.setPosition(screenX-16+pixel_info1.getWidth()/2,(int)(camera.viewportHeight-screenY+73));
				
				menu.addActor(new IG_Menu(screenX,(int) camera.viewportHeight-screenY,(int) (pixel_info1.getWidth()+20),100));
				menu.addActor(pixel_info0);
				menu.addActor(pixel_info1);
				menu.addActor(pixel_info2);
				menu.addActor(new Acteur(sp));
				
			}else{
				if(px.getAtlas() != null){
					
					//TODO Attention lorsqu'on gèrera plusieurs cartes
					Sprite sp = worldmap.get(0).getSpriteAtCoord("v2_worldmap", p);
					sp.setPosition(screenX,(int)(camera.viewportHeight-screenY));
					sp.flip(false, true);
					menu.addActor(new IG_Menu(screenX,(int)camera.viewportHeight-screenY,(int) sp.getWidth(), (int) sp.getHeight()));
					menu.addActor(new Acteur(sp));
					
				}else{
					TextButton pixel_info0 = new TextButton(p.x +","+ p.y+" : ID "+px.getId()+" inconnu",style);
					pixel_info0.setPosition(screenX+10,(int)(camera.viewportHeight-screenY+5));
					menu.addActor(new IG_Menu(screenX,(int)camera.viewportHeight-screenY,(int) (pixel_info0.getWidth()+20),(int) (pixel_info0.getHeight()+10)));
					menu.addActor(pixel_info0);
					
				}
			}
		}

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
			case 1 : moveChunksRight(); break;
			case 2 : moveChunksDownRight(); break;
			case 3 : moveChunksDown(); break;
			case 4 : moveChunksDownLeft(); break;
			case 5 : moveChunksLeft(); break;
			case 6 : moveChunksUpLeft(); break;
			case 7 : moveChunksUp(); break;
			case 8 : moveChunksUpRight(); break;
		}
	}

	/**
	 * Moves ChunkMap up and right
	 */
	private static void moveChunksUpRight() {
		logger.info("Déplacement à droite et en haut");
		Point point = worldmap.get(8).getCenter();
		worldmap.put(4, worldmap.get(0));
		worldmap.put(5, worldmap.get(7));
		worldmap.put(3, worldmap.get(1));
		worldmap.put(0, worldmap.get(8));
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, chunk_size);
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		worldmap.put(6,new Chunk("v2_worldmap",chunk_positions.get(6)));
		worldmap.put(7,new Chunk("v2_worldmap",chunk_positions.get(7)));
		worldmap.put(8,new Chunk("v2_worldmap",chunk_positions.get(8)));
		worldmap.put(1,new Chunk("v2_worldmap",chunk_positions.get(1)));
		worldmap.put(2,new Chunk("v2_worldmap",chunk_positions.get(2)));
		m.renderChunks();
	}

	/**
	 * Moves ChunkMap up
	 */
	private static void moveChunksUp() {
		logger.info("Déplacement en haut"); 
		Point point = worldmap.get(7).getCenter();
		worldmap.put(2, worldmap.get(1));
		worldmap.put(3, worldmap.get(0));
		worldmap.put(4, worldmap.get(5));
		worldmap.put(5, worldmap.get(6));
		worldmap.put(0, worldmap.get(7));
		worldmap.put(1, worldmap.get(8));
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, chunk_size);
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		worldmap.put(6,new Chunk("v2_worldmap",chunk_positions.get(6)));
		worldmap.put(7,new Chunk("v2_worldmap",chunk_positions.get(7)));
		worldmap.put(8,new Chunk("v2_worldmap",chunk_positions.get(8)));
		m.renderChunks();
	}

	/**
	 * Moves ChunkMap up and left
	 */
	private static void moveChunksUpLeft() {
		logger.info("Déplacement à gauche et en haut");
		Point point = worldmap.get(6).getCenter();
		worldmap.put(2, worldmap.get(0));
		worldmap.put(3, worldmap.get(5));
		worldmap.put(1, worldmap.get(7));
		worldmap.put(0, worldmap.get(6));
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, chunk_size);
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		worldmap.put(4,new Chunk("v2_worldmap",chunk_positions.get(4)));
		worldmap.put(5,new Chunk("v2_worldmap",chunk_positions.get(5)));
		worldmap.put(6,new Chunk("v2_worldmap",chunk_positions.get(6)));
		worldmap.put(7,new Chunk("v2_worldmap",chunk_positions.get(7)));
		worldmap.put(8,new Chunk("v2_worldmap",chunk_positions.get(8)));
		m.renderChunks();
	}

	/**
	 * Moves ChunkMap left
	 */
	private static void moveChunksLeft() {
		logger.info("Déplacement à gauche");
		Point point = worldmap.get(5).getCenter();
		worldmap.put(8, worldmap.get(7));
		worldmap.put(1, worldmap.get(0));
		worldmap.put(2, worldmap.get(3));
		worldmap.put(7, worldmap.get(6));
		worldmap.put(0, worldmap.get(5));
		worldmap.put(3, worldmap.get(4));
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, chunk_size);
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		worldmap.put(4,new Chunk("v2_worldmap",chunk_positions.get(4)));
		worldmap.put(5,new Chunk("v2_worldmap",chunk_positions.get(5)));
		worldmap.put(6,new Chunk("v2_worldmap",chunk_positions.get(6)));
		m.renderChunks();
	}

	/**
	 * Moves ChunkMap down and left
	 */
	private static void moveChunksDownLeft() {
		logger.info("Déplacement à gauche et en bas");
		Point point = worldmap.get(4).getCenter();
		worldmap.put(8, worldmap.get(0));
		worldmap.put(7, worldmap.get(5));
		worldmap.put(1, worldmap.get(3));
		worldmap.put(0, worldmap.get(4));
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, chunk_size);
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		worldmap.put(2,new Chunk("v2_worldmap",chunk_positions.get(2)));
		worldmap.put(3,new Chunk("v2_worldmap",chunk_positions.get(3)));
		worldmap.put(4,new Chunk("v2_worldmap",chunk_positions.get(4)));
		worldmap.put(5,new Chunk("v2_worldmap",chunk_positions.get(5)));
		worldmap.put(6,new Chunk("v2_worldmap",chunk_positions.get(6)));
		m.renderChunks();
	}

	/**
	 * Moves ChunkMap down
	 */
	private static void moveChunksDown() {
		logger.info("Déplacement en bas");
		Point point = worldmap.get(3).getCenter();
		worldmap.put(6, worldmap.get(5));
		worldmap.put(7, worldmap.get(0));
		worldmap.put(8, worldmap.get(1));
		worldmap.put(5, worldmap.get(4));
		worldmap.put(0, worldmap.get(3));
		worldmap.put(1, worldmap.get(2));
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, chunk_size);
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		worldmap.put(4,new Chunk("v2_worldmap",chunk_positions.get(4)));
		worldmap.put(3,new Chunk("v2_worldmap",chunk_positions.get(3)));
		worldmap.put(2,new Chunk("v2_worldmap",chunk_positions.get(2)));
		m.renderChunks();
	}

	/**
	 * Moves ChunkMap down and right
	 */
	private static void moveChunksDownRight() {
		logger.info("Déplacement à droite et en bas");
		Point point = worldmap.get(2).getCenter();
		worldmap.put(6, worldmap.get(0));
		worldmap.put(5, worldmap.get(3));
		worldmap.put(7, worldmap.get(1));
		worldmap.put(0, worldmap.get(2));
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, chunk_size);
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		worldmap.put(4,new Chunk("v2_worldmap",chunk_positions.get(4)));
		worldmap.put(3,new Chunk("v2_worldmap",chunk_positions.get(3)));
		worldmap.put(2,new Chunk("v2_worldmap",chunk_positions.get(2)));
		worldmap.put(1,new Chunk("v2_worldmap",chunk_positions.get(1)));
		worldmap.put(8,new Chunk("v2_worldmap",chunk_positions.get(8)));
		m.renderChunks();
	}

	/**
	 * Moves ChunkMap right
	 */
	private static void moveChunksRight() {
		logger.info("Déplacement à droite");	
		Point point = worldmap.get(1).getCenter();
		worldmap.put(6, worldmap.get(7));
		worldmap.put(5, worldmap.get(0));
		worldmap.put(4, worldmap.get(3));
		worldmap.put(7, worldmap.get(8));
		worldmap.put(0, worldmap.get(1));
		worldmap.put(3, worldmap.get(2));
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, chunk_size);
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		worldmap.put(8,new Chunk("v2_worldmap",chunk_positions.get(8)));
		worldmap.put(1,new Chunk("v2_worldmap",chunk_positions.get(1)));
		worldmap.put(2,new Chunk("v2_worldmap",chunk_positions.get(2)));
		m.renderChunks();
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
		Point chunkCenter = worldmap.get(0).getCenter();
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
	 * Tells the renderer the stage is ready to be rendered
	 */
	public static void setReadyToRender() {
		if(m == null){
			logger.fatal("On essaye d'activer le rendu avant d'avoir instancié correctement MapManager.");
			System.exit(1);
		}
		do_render = true;
	}

	/**
	 * @return the camera
	 */
	public OrthographicCamera getCamera() {
		return m.camera;
	}

	/**
	 * Focuses on the unmapped ids to fix them
	 */
	public void editNextUnmappedID() {
		logger.info("Edition de la prochaine id non mappée.");
		Iterator<Integer> iter_id = worldmap.get(0).getUnmappedIds().keySet().iterator();
		while(iter_id.hasNext()){
			logger.info("ID non mappée : "+iter_id.next());
		}
	}

}
