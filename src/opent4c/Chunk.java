package opent4c;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


/**
 * This is a chunk, part of a 25 chunks ChunkMap.
 * @author synoga
 *
 */
public class Chunk{
	private static Logger logger = LogManager.getLogger(Chunk.class.getSimpleName());
	private static int watcher_delay_ms = 50;
	private static ScheduledFuture<?> watcher;
	private Point center = null;
	private LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private List<Acteur> chunk_sprites = null;
	private List<Acteur> chunk_tiles = null;
	private Map<Integer,List<Point>> unmapped_ids = null;

	
	/**
	 * Creates a new Chunk form a map name and a point on the map
	 * @param map 
	 * @param point
	 */
	public Chunk(String map, Point point) {
		setCenter(point);
		chunk_sprites = new ArrayList<Acteur>(MapManager.getChunkSize().width*MapManager.getChunkSize().height);
		chunk_tiles = new ArrayList<Acteur>(MapManager.getChunkSize().width*MapManager.getChunkSize().height);
		unmapped_ids = new HashMap<Integer,List<Point>>();
		loadingStatus.waitUntilTextureAtlasTilesCreated();
		int upLimit = point.y-(MapManager.getChunkSize().height/2)-1;
		int downLimit = point.y+(MapManager.getChunkSize().height/2);
		int leftLimit = point.x-(MapManager.getChunkSize().width/2)-1;
		int rightLimit = point.x+(MapManager.getChunkSize().width/2);
		for(int y = upLimit ; y <= downLimit ; y++){
			for(int x = leftLimit ; x <= rightLimit ; x++){
				if(!isTileAtCoord(map,PointsManager.getPoint(x,y))){
					chunk_sprites.add(getSpriteAtCoord(map, PointsManager.getPoint(x,y)));
				}else{
					chunk_tiles.add(getTileAtCoord(map, PointsManager.getPoint(x,y)));
				}
			}	
		}
	}

	public Acteur getActeurAtCoord(String map, Point point){
		if(!isTileAtCoord(map,point)){
			return getSpriteAtCoord(map, point);
		}else{
			return getTileAtCoord(map, point);
		}
	}
	
	/**
	 * @param map
	 * @param point
	 * @return the tile at given coordinates on given map
	 */
	public Acteur getTileAtCoord(String map, Point point) {
		Acteur result = null;
		result = new Acteur(getTileTexRegionAtCoord(map, point),point,PointsManager.getPoint(0, 0));
		//result.flip(false, true);
		return result;
	}

	/**
	 * @param map
	 * @param point
	 * @return the TextureRegion for a tile at given coordinates on given map.
	 * If the ID isn't mapped or the atlas is missing or the textureRegion is missing,
	 * returns the "unknown" tile.
	 */
	private TextureRegion getTileTexRegionAtCoord(String map, Point point) {
		TextureRegion result = null;
		String moduloTex = "";
		TextureAtlas texAtlas = null;
		TilePixel px = null;
		px = (TilePixel) getPixelAtCoord(map, point);
		if (px != null){
			texAtlas = getTileTexAtlas(px);
			moduloTex = setModuloFromPoint(px, point);
			result = texAtlas.findRegion(moduloTex);
			if (result == null){
				result = getUnknownTile();
			}
		}else{
			result = getUnknownTile();
		}
		if(result == null){
			logger.fatal("Attention, on charge une texture null : "+result);
			System.exit(1);
		}
		return result;
	}

	/**
	 * @param px
	 * @return the TextureAtlas for a tile from a MapPixel
	 * if the atlas is missing, returns the unknown atlas
	 */
	private TextureAtlas getTileTexAtlas(MapPixel px) {
		TextureAtlas texAtlas = null;
		texAtlas = loadingStatus.getTextureAtlasTile(px.getAtlas());
		if(texAtlas == null){
			return getUnknownAtlas();
		}else{
			return texAtlas;
		}
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
	 * @param px
	 * @param point
	 * @return a textureRegion name with zone effect
	 */
	private String setModuloFromPoint(TilePixel px, Point point) {
		String tex = px.getTex();
		int tileModuloX = px.getModulo().x;
		int tileModuloY = px.getModulo().y;
		int moduloX = (point.x % tileModuloX)+1;
		int moduloY = (point.y % tileModuloY)+1;
		return tex.substring(0,tex.indexOf('(')+1)+moduloX+", "+moduloY+")";		
	}

	/**
	 * Tells if the given coordinate on given map is a tile or a sprite
	 * @param map
	 * @param point
	 * @return
	 */
	public boolean isTileAtCoord(String map, Point point) {
		MapPixel px = null;
		px = getPixelAtCoord(map, point);
		if(px == null) return false;
		if (px instanceof TilePixel){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Compute Chunks position from the center of the first chunk and the chunks size
	 * @param startpoint
	 * @param chunk_size
	 * @return a Map of chunk positions
	 */
	public static Map<Integer, Point> computeChunkPositions(Point startpoint, Dimension chunk_size) {
		Map<Integer,Point> result = new HashMap<Integer,Point>(9);
		Point chunk0, chunk1, chunk2, chunk3, chunk4, chunk5, chunk6, chunk7, chunk8, chunk9, chunk10, chunk11, chunk12, chunk13, chunk14, chunk15, chunk16, chunk17, chunk18, chunk19, chunk20, chunk21, chunk22, chunk23, chunk24;
		chunk0 = startpoint;
		result.put(0, chunk0);
		chunk1 = PointsManager.getPoint(chunk0.x+chunk_size.width, chunk0.y);
		result.put(1, chunk1);
		chunk2 = PointsManager.getPoint(chunk1.x, chunk1.y+chunk_size.height);
		result.put(2, chunk2);
		chunk3 = PointsManager.getPoint(chunk2.x-chunk_size.width, chunk2.y);
		result.put(3, chunk3);
		chunk4 = PointsManager.getPoint(chunk3.x-chunk_size.width, chunk3.y);
		result.put(4, chunk4);
		chunk5 = PointsManager.getPoint(chunk4.x, chunk4.y-chunk_size.height);
		result.put(5, chunk5);
		chunk6 = PointsManager.getPoint(chunk5.x, chunk5.y-chunk_size.height);
		result.put(6, chunk6);
		chunk7 = PointsManager.getPoint(chunk6.x+chunk_size.width, chunk6.y);
		result.put(7, chunk7);
		chunk8 = PointsManager.getPoint(chunk7.x+chunk_size.width, chunk7.y);
		result.put(8, chunk8);
		chunk9 = PointsManager.getPoint(chunk8.x+chunk_size.width, chunk8.y);
		result.put(9, chunk9);
		chunk10 = PointsManager.getPoint(chunk9.x, chunk9.y+chunk_size.height);
		result.put(10, chunk10);
		chunk11 = PointsManager.getPoint(chunk10.x, chunk10.y+chunk_size.height);
		result.put(11, chunk11);
		chunk12 = PointsManager.getPoint(chunk11.x, chunk11.y+chunk_size.height);
		result.put(12, chunk12);
		chunk13 = PointsManager.getPoint(chunk12.x-chunk_size.width, chunk12.y);
		result.put(13, chunk13);
		chunk14 = PointsManager.getPoint(chunk13.x-chunk_size.width, chunk13.y);
		result.put(14, chunk14);
		chunk15 = PointsManager.getPoint(chunk14.x-chunk_size.width, chunk14.y);
		result.put(15, chunk15);
		chunk16 = PointsManager.getPoint(chunk15.x-chunk_size.width, chunk15.y);
		result.put(16, chunk16);
		chunk17 = PointsManager.getPoint(chunk16.x, chunk16.y-chunk_size.height);
		result.put(17, chunk17);
		chunk18 = PointsManager.getPoint(chunk17.x, chunk17.y-chunk_size.height);
		result.put(18, chunk18);
		chunk19 = PointsManager.getPoint(chunk18.x, chunk18.y-chunk_size.height);
		result.put(19, chunk19);
		chunk20 = PointsManager.getPoint(chunk19.x, chunk19.y-chunk_size.height);
		result.put(20, chunk20);
		chunk21 = PointsManager.getPoint(chunk20.x+chunk_size.width, chunk20.y);
		result.put(21, chunk21);
		chunk22 = PointsManager.getPoint(chunk21.x+chunk_size.width, chunk21.y);
		result.put(22, chunk22);
		chunk23 = PointsManager.getPoint(chunk22.x+chunk_size.width, chunk22.y);
		result.put(23, chunk23);
		chunk24 = PointsManager.getPoint(chunk23.x+chunk_size.width, chunk23.y);
		result.put(24, chunk24);
		return result;
	}
	
	/**
	 * @param map
	 * @param point
	 * @return a sprite from a given point on a given map
	 */
	public Acteur getSpriteAtCoord(String map, Point point) {
		Acteur result = null;
		TextureRegion texRegion = null;
		TextureAtlas texAtlas = null;
		MapPixel px = null;
		px = getPixelAtCoord(map, point);
		if (px == null){
			//logger.warn("On tente de charger un MapPixel null");
			result = new Acteur(getUnknownTile(),point,PointsManager.getPoint(0, 0));
			return result;
		}
		texAtlas = loadingStatus.getTextureAtlasSprite(px.getAtlas());
		if (texAtlas == null){
			texAtlas = AssetsLoader.load(px.getAtlas());
		}
		texRegion = texAtlas.findRegion(px.getTex());
		if(texRegion == null){
			logger.warn("On tente de charger une TextureRegion null");
			texRegion = getUnknownTile();
		}
		result = new Acteur(texRegion,point,px.getOffset());
		return result;
	}

	/**
	 * @param map
	 * @param point
	 * @return a MapPixel from Point on a given map name
	 */
	public MapPixel getPixelAtCoord(String map, Point point) {
		MapPixel result = null;
		int id = MapManager.getIdAtCoordOnMap(map, point);
		result = SpriteData.getPixelFromId(id);
		if (result == null){
			if (unmapped_ids.get(id)==null){
				unmapped_ids.put(id,new ArrayList<Point>());
				unmapped_ids.get(id).add(point);
			}else{
				unmapped_ids.get(id).add(point);
			}
			//logger.warn("ID "+id+" non mappée => "+point.x+";"+point.y+"@"+carte);
		}
		return result;
	}

	/**
	 * @return the chunk's sprites
	 */
	public List<Acteur> getSprites() {
		return chunk_sprites;
	}

	/**
	 * @param map
	 * @return the chunk's ids from a map
	 */
	public List<Integer> getIds(String map){
		List<Integer> result = new ArrayList<Integer>();
		Iterator<Acteur> iter_cells = chunk_sprites.iterator();
		while (iter_cells.hasNext()){
			Acteur pt = iter_cells.next();
			result.add(MapManager.getIdAtCoordOnMap(map, PointsManager.getPoint(pt.getX(), pt.getY())));
		}
		return result;
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
	public List<Acteur> getTiles() {
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
	 * @return a list of unmapped ids on the Chunk
	 */
	public Map<Integer,List<Point>> getUnmappedIds(){
		return unmapped_ids;
	}

	public static void stopChunkMapWatcher() {
		if (watcher != null) watcher.cancel(true);
	}
}
