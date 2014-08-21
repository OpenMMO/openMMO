package opent4c;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import opent4c.utils.AssetsLoader;
import opent4c.utils.LoadingStatus;
import opent4c.utils.PointsManager;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import screens.MapManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;


/**
 * This is a chunk, part of a 9 chunks ChunkMap.
 * @author synoga
 *
 */
public class Chunk{
	private static Logger logger = LogManager.getLogger(Chunk.class.getSimpleName());
	private static int watcher_delay_ms = 32;
	private static ScheduledFuture<?> watcher;
	private Point center = null;
	private LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private Group chunk_sprites = null;
	private Group chunk_tiles = null;
	//private static final Dimension chunk_size = new Dimension(4,4);//pour tester les chunks
	//private static final Dimension chunk_size = new Dimension((Gdx.graphics.getWidth()/96),(Gdx.graphics.getHeight()/48));//On fait des chunks environ de la taille d' 1/9 de fenêtre, en nombre de tuiles, comme ça c'est transparent pour l'utilisateur sans charger trop de tuiles en mémoire.
	public static final Point chunk_size = PointsManager.getPoint(Gdx.graphics.getWidth()/64,Gdx.graphics.getHeight()/32);

	
	/**
	 * Creates a new Chunk form a map name and a point on the map
	 * @param map 
	 * @param point
	 */
	public Chunk(String map, Point point) {
		setCenter(point);
		chunk_sprites = new Group();
		chunk_tiles = new Group();
		loadingStatus.waitUntilTextureAtlasTilesCreated();
		int upLimit = point.y-(MapManager.getChunkSize().y/2)-1;
		int downLimit = point.y+(MapManager.getChunkSize().y/2);
		int leftLimit = point.x-(MapManager.getChunkSize().x/2)-1;
		int rightLimit = point.x+(MapManager.getChunkSize().x/2);
		for(int y = upLimit ; y <= downLimit ; y++){
			for(int x = leftLimit ; x <= rightLimit ; x++){
				int id = MapManager.getIdAtCoordOnMap("v2_worldmap",PointsManager.getPoint(x,y));
				addActeurforId(id,PointsManager.getPoint(x, y));
			}	
		}
	}

	/**
	 * @param id
	 * @param point
	 */
	private void addActeurforId(int id, Point point) {
		if (isTuileId(id)){
			addTileAtCoord(id, point);
		}else if(isSmoothingId(id)){
			addSmoothingAtCoord(id, point);
		}else{
			addSpriteAtCoord(id, point);
		}
	}
	
	private void addSmoothingAtCoord(int id, Point point) {
		String smooth_info = SpriteData.getTexFromId(id);
		String tmpl = smooth_info.substring(0, smooth_info.indexOf(" T1"));
		String t1 = smooth_info.substring(smooth_info.indexOf("T1")+3, smooth_info.indexOf("T2"));
		String t2 = smooth_info.substring(smooth_info.indexOf("T2")+3);
		//logger.info("ID : "+id+" => smooth template : "+tmpl+" ; T1 : "+t1+" ; T2 : "+t2);
		if(tmpl.startsWith("Tmpl")) chunk_tiles.addActor(new Acteur(createSmoothing(tmpl,t1,t2,point),point,PointsManager.getPoint(0, 0)));

	}

	private TextureRegion createSmoothing(String tmpl, String t1, String t2, Point point) {
		TextureRegion result = null;
		TextureRegion template = null;
		TextureAtlas atlas = null;
		if(tmpl.startsWith("Tmpl3")){
			atlas = loadingStatus.getTextureAtlasTile("GenericMerge3");
		}
		if(tmpl.startsWith("Tmpl1")){
			atlas = loadingStatus.getTextureAtlasTile("GenericMerge1");
		}
		if(atlas == null) logger.info("template nul!!!!!!!!");
		template = atlas.findRegion(tmpl);
		if(template == null){
			logger.warn("smooth nul : "+tmpl);
			return getUnknownTile();
		}
		return template;
	}

	/**
	 * Adds a Sprite on a chunkMap
	 * @param id
	 * @param point
	 */
	private void addSpriteAtCoord(int id, Point point) {
		String atlas = SpriteData.getAtlasFromId(id);
		String tex = SpriteData.getTexFromId(id);
		if (atlas.equals("Atlas non mappé") | tex.equals("Texture non mappée") | atlas.equals("NA")){
			logger.warn(id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
			addUnknownTile(point);
			return;
		}
		TextureAtlas texAtlas = loadingStatus.getTextureAtlasSprite(atlas);			
		if(texAtlas == null){
			texAtlas = AssetsLoader.load(atlas);
			if(texAtlas == null){
				addUnknownTile(point);
				logger.warn("Atlas missing : "+id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
				return;
			}
		}
		TextureRegion texRegion = texAtlas.findRegion(tex);
		if(texRegion == null){
			logger.warn("TextureRegion missing : "+id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
			addUnknownTile(point);
			return;
		}
		
		MapPixel px = SpriteData.getSpriteFromId(id);
		if (px == null){
			logger.warn("Not Present in pixel_index : "+id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
			addUnknownTile(point);
			return;
		}
		chunk_sprites.addActor(new Acteur(texRegion,point,px.getOffset()));
		int id2 = MapManager.getIdAtCoordOnMap("v2_worldmap",PointsManager.getPoint(point.x, point.y+1));
		if (isTuileId(id2)) addTileAtCoord(id2, point);		
	}

	/**
	 * Adds a tile on a chunkMap
	 * @param id
	 * @param point
	 */
	private void addTileAtCoord(int id, Point point) {
		TextureRegion texRegion = null;
		TextureAtlas texAtlas = null;
		String atlas = SpriteData.getAtlasFromId(id);
		String tex = SpriteData.getTexFromId(id);
		int moduloX = Integer.parseInt(tex.substring(tex.indexOf('(')+1,tex.indexOf(',')));
		int moduloY = Integer.parseInt(tex.substring(tex.indexOf(',')+1,tex.indexOf(')')));
		if (atlas.equals("Atlas non mappé") | tex.equals("Texture non mappée") | atlas.equals("NA")){
			logger.warn(id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
			addUnknownTile(point);
			return;
		}
		texAtlas = loadingStatus.getTextureAtlasTile(atlas);
		if(texAtlas == null){
			texAtlas = AssetsLoader.load(atlas);
			if(texAtlas == null){
				addUnknownTile(point);
				logger.warn("Atlas missing : "+id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
				return;
			}
		}
		texRegion = texAtlas.findRegion(getModuledTexNameFromPoint(atlas, moduloX, moduloY, point));
		if(texRegion == null){
			logger.warn("TextureRegion missing : "+id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
			addUnknownTile(point);
			return;
		}
		chunk_tiles.addActor(new Acteur(texRegion,point,PointsManager.getPoint(0, 0)));
	}

	private boolean isSmoothingId(int id) {
		String tex = SpriteData.getTexFromId(id);
		if(tex.contains("Tmpl") && tex.contains("T1") && tex.contains("T2"))return true;
		return false;
	}

	private boolean isTuileId(int id) {
		String tex = SpriteData.getTexFromId(id);
		if(tex.startsWith("modulos(")) return true;
		return false;
	}

	private void addUnknownTile(Point point) {
		chunk_tiles.addActor(new Acteur(getUnknownTile(),PointsManager.getPoint(point.x, point.y),PointsManager.getPoint(0, 0)));
	}

	/**
	 * @return the unknown atlas
	 */
	private TextureAtlas getUnknownAtlas() {
		TextureAtlas texAtlas = null;
		texAtlas = loadingStatus.getTextureAtlasSprite("Unknown");
		if (texAtlas == null){
			texAtlas = AssetsLoader.load("Unknown");
		}
		return texAtlas;
	}

	/**
	 * @param moduloY2 
	 * @param moduloX 
	 * @param px
	 * @param point
	 * @return a textureRegion name with zone effect
	 */
	private String getModuledTexNameFromPoint(String atlas, int moduloX, int moduloY, Point point) {
		return atlas+" ("+((point.x % moduloX)+1)+", "+((point.y % moduloY)+1)+")";	
	}

	/**
	 * Compute Chunks position from the center of the first chunk and the chunks size
	 * @param startpoint
	 * @param chunk_size
	 * @return a Map of chunk positions
	 */
	public static Map<Integer, Point> computeChunkPositions(Point startpoint, Point chunk_size) {
		Map<Integer,Point> result = new HashMap<Integer,Point>(9);
		Point chunk1, chunk2, chunk3, chunk4, chunk5, chunk6, chunk7, chunk8, chunk9;
		chunk5 = startpoint;
		result.put(5, chunk5);
		chunk6 = PointsManager.getPoint(chunk5.x+chunk_size.x, chunk5.y);
		result.put(6, chunk6);
		chunk3 = PointsManager.getPoint(chunk6.x, chunk6.y+chunk_size.y);
		result.put(3, chunk3);
		chunk2 = PointsManager.getPoint(chunk3.x-chunk_size.x, chunk3.y);
		result.put(2, chunk2);
		chunk1 = PointsManager.getPoint(chunk2.x-chunk_size.x, chunk2.y);
		result.put(1, chunk1);
		chunk4 = PointsManager.getPoint(chunk1.x, chunk1.y-chunk_size.y);
		result.put(4, chunk4);
		chunk7 = PointsManager.getPoint(chunk4.x, chunk4.y-chunk_size.y);
		result.put(7, chunk7);
		chunk8 = PointsManager.getPoint(chunk7.x+chunk_size.x, chunk7.y);
		result.put(8, chunk8);
		chunk9 = PointsManager.getPoint(chunk8.x+chunk_size.x, chunk8.y);
		result.put(9, chunk9);
		return result;
	}

	/**
	 * @return the chunk's sprites
	 */
	public Group getSpriteActeur() {
		return chunk_sprites;
	}
	
	/**
	 * @return the chunk's center Point
	 */
	public Point getCenter() {
		return center;
	}

	/**
	 * Sets the Chunk's center Point
	 * @param center
	 */
	public void setCenter(Point center) {
		this.center = center;
	}

	/**
	 * @return the Chunk's tiles
	 */
	public Group getTileActeurs() {
		return chunk_tiles;
	}
	
	/**
	 * @return the "Unknown" tile
	 */
	private TextureRegion getUnknownTile(){
		TextureAtlas texAtlas = getUnknownAtlas();
		return texAtlas.findRegion("Unknown Tile");
	}
	/**
	 * Starts a clock which will check the camera's position on chunk so that it can
	 * move the chunkMap if needed.
	 */
	public static void startChunkMapWatcher() {
		Runnable r = RunnableCreatorUtil.getChunkMapWatcherRunnable();
		watcher = ThreadsUtil.executePeriodicallyInThread(r, watcher_delay_ms, watcher_delay_ms, TimeUnit.MILLISECONDS);
	}
	/**
	 * Stops the clock checking if chunks need to be moved.
	 */
	public static void stopChunkMapWatcher() {
		if (watcher != null) watcher.cancel(true);
	}
}
