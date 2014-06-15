package OpenT4C;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.AssetsLoader;
import t4cPlugin.MapPixel;
import t4cPlugin.utils.LoadingStatus;
import t4cPlugin.utils.PointsManager;
import t4cPlugin.utils.RunnableCreatorUtil;
import t4cPlugin.utils.ThreadsUtil;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


/**
 * This is a chunk, part of a 9 chunks ChunkMap.
 * @author synoga
 *
 */
public class Chunk{
	
	private static Logger logger = LogManager.getLogger(Chunk.class.getSimpleName());
	private static int watcher_delay = 500;
	private Point center = null;
	private LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private Map<Point,Sprite> chunk_sprites = null;
	private Map<Point,Sprite> chunk_tiles = null;
	private Map<Point,MapPixel> chunk_sprite_info = null;
	private Map<Integer,List<Point>> unmapped_ids = null;

	
	/**
	 * Creates a new Chunk form a map name and a point on the map
	 * @param map 
	 * @param point
	 */
	public Chunk(String map, Point point) {
		setCenter(point);
		chunk_sprite_info = new HashMap<Point,MapPixel>(MapManager.getChunkSize().width*MapManager.getChunkSize().height);
		chunk_sprites = new HashMap<Point,Sprite>(MapManager.getChunkSize().width*MapManager.getChunkSize().height);
		chunk_tiles = new HashMap<Point,Sprite>(MapManager.getChunkSize().width*MapManager.getChunkSize().height);
		unmapped_ids = new HashMap<Integer,List<Point>>();
		loadingStatus.waitUntilTextureAtlasTilesCreated();
		int upLimit = point.y-(MapManager.getChunkSize().height/2);
		int downLimit = point.y+(MapManager.getChunkSize().height/2);
		int leftLimit = point.x-(MapManager.getChunkSize().width/2);
		int rightLimit = point.x+(MapManager.getChunkSize().width/2);
		for(int y = upLimit ; y < downLimit ; y++){
			for(int x = leftLimit ; x < rightLimit ; x++){
				if(!isTileAtCoord(map,PointsManager.getPoint(x,y))){
					chunk_sprites.put(PointsManager.getPoint(x,y),getSpriteAtCoord(map, PointsManager.getPoint(x,y)));
				}else{
					chunk_tiles.put(PointsManager.getPoint(x,y),getTileAtCoord(map, PointsManager.getPoint(x,y)));
				}
			}	
		}
	}

	/**
	 * @param map
	 * @param point
	 * @return the tile at given coordinates on given map
	 */
	public Sprite getTileAtCoord(String map, Point point) {
		Sprite result = null;
		result = new Sprite(getTileTexRegion(map, point));
		result.flip(false, true);
		return result;
	}

	/**
	 * @param map
	 * @param point
	 * @return the TextureRegion for a tile at given coordinates on given map.
	 * If the ID isn't mapped or the atlas is missing or the textureRegion is missing,
	 * returns the "unknown" tile.
	 */
	private TextureRegion getTileTexRegion(String map, Point point) {
		TextureRegion result = null;
		String moduloTex = "";
		TextureAtlas texAtlas = null;
		MapPixel px = null;
		px = getPixelAtCoord(map, point);
		if (px != null){
			chunk_sprite_info.put(point, px);
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
		return AssetsLoader.load("Unknown");
	}

	/**
	 * @param px
	 * @param point
	 * @return a textureRegion name with zone effect
	 */
	private String setModuloFromPoint(MapPixel px, Point point) {
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
	private boolean isTileAtCoord(String map, Point point) {
		MapPixel px = null;
		px = getPixelAtCoord(map, point);
		if(px == null) return false;
		if (px.isTuile()){
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
	
	/**
	 * @param map
	 * @param point
	 * @return a sprite from a given point on a given map
	 */
	public Sprite getSpriteAtCoord(String map, Point point) {
		Sprite result = null;
		TextureRegion texRegion = null;
		TextureAtlas texAtlas = null;
		MapPixel px = null;
		px = getPixelAtCoord(map, point);
		if (px != null){
			chunk_sprite_info.put(point, px);
			texAtlas = loadingStatus.getTextureAtlasSprite(px.getAtlas());
			if (texAtlas == null){
				texAtlas = AssetsLoader.load(px.getAtlas());
			}
			texRegion = texAtlas.findRegion(px.getTex());
		}else{
			texRegion = getUnknownTile();
		}
		result = new Sprite(texRegion);
		result.flip(false, true);
		return result;
	}

	/**
	 * @param map
	 * @param point
	 * @return a MapPxiel from Point on a given map name
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
	public Map<Point, Sprite> getSprites() {
		return chunk_sprites;
	}

	/**
	 * @param map
	 * @return the chunk's ids from a map
	 */
	public List<Integer> getIds(String map){
		List<Integer> result = new ArrayList<Integer>();
		Iterator<Point> iter_cells = chunk_sprites.keySet().iterator();
		while (iter_cells.hasNext()){
			Point pt = iter_cells.next();
			result.add(MapManager.getIdAtCoordOnMap(map, pt));
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
	public Map<Point, Sprite> getTiles() {
		return chunk_tiles;
	}
	
	/**
	 * @return the "Unknown" tile
	 */
	private TextureRegion getUnknownTile(){
		TextureAtlas texAtlas = loadingStatus.getTextureAtlasSprite("Unknown");
		if (texAtlas == null){
			texAtlas = AssetsLoader.load("Unknown");
		}
		return texAtlas.findRegion("Unknown Tile");
	}
	/**
	 * Starts a clock which will check the camera's position on chunk so that it can
	 * move the chunkMap if needed.
	 */
	public static void startChunkMapWatcher() {
		Runnable r = RunnableCreatorUtil.getChunkMapWatcherRunnable();
		ThreadsUtil.executePeriodicallyInThread(r, watcher_delay, watcher_delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * @return a list of unmapped ids on the Chunk
	 */
	public Map<Integer,List<Point>> getUnmappedIds(){
		return unmapped_ids;
	}

	public Point getOffsetFromPoint(Point point) {
		return chunk_sprite_info.get(point).getOffset();
	}
}
