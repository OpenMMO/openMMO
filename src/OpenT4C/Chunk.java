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
 * Cette classe est un morceau de la carte à afficher. Chaque carte affichée contient 9 chunks
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
	 * On remplit une liste de Sprites à partir du point central du Chunk
	 * @param carte 
	 * @param point
	 */
	public Chunk(String carte, Point point) {
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
				if(!isTileAtCoord(carte,PointsManager.getPoint(x,y))){
					chunk_sprites.put(PointsManager.getPoint(x,y),getSpriteAtCoord(carte, PointsManager.getPoint(x,y)));
				}else{
					chunk_tiles.put(PointsManager.getPoint(x,y),getTileAtCoord(carte, PointsManager.getPoint(x,y)));
				}
			}	
		}
	}

	/**
	 * Retourne la tuile présente au point donné de la carte donnée.
	 * @param carte
	 * @param point
	 * @return
	 */
	public Sprite getTileAtCoord(String carte, Point point) {
		Sprite result = null;
		TextureRegion texRegion = null;
		TextureAtlas texAtlas = null;
		MapPixel px = null;
		px = getPixelAtCoord(carte, point);
		if (px != null){
			chunk_sprite_info.put(point, px);
			texAtlas = loadingStatus.getTextureAtlasTile(px.getAtlas());
			if(texAtlas == null){
				texRegion = getUnknownTile();
			}else{
				String tex = px.getTex();
				int tileModuloX = px.getModulo().x;
				int tileModuloY = px.getModulo().y;
				int moduloX = (point.x % tileModuloX)+1;
				int moduloY = (point.y % tileModuloY)+1;
				String moduloTex = tex.substring(0,tex.indexOf('(')+1)+moduloX+", "+moduloY+")";
				texRegion = texAtlas.findRegion(moduloTex);
				if(texRegion == null){
					logger.fatal("Attention, on charge une texture null : "+moduloTex);
					System.exit(1);
				}
			}
		}else{
			texRegion = getUnknownTile();
		}
		result = new Sprite(texRegion);
		result.flip(false, true);
		return result;
	}

	/**
	 * Détermine si le point donné de la carte donné est une tuile ou un sprite
	 * @param carte
	 * @param point
	 * @return
	 */
	private boolean isTileAtCoord(String carte, Point point) {
		MapPixel px = null;
		px = getPixelAtCoord(carte, point);
		if(px == null) return false;
		if (px.isTuile()){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Calcule la position des chunks en fonction du point central et de la taille des chunks
	 * @param startpoint
	 * @param chunk_size
	 * @return
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
	 * Retourne un Sprite à partir du point donné de la carte donnée.
	 * @param carte
	 * @param point
	 * @return
	 */
	public Sprite getSpriteAtCoord(String carte, Point point) {
		Sprite result = null;
		TextureRegion texRegion = null;
		TextureAtlas texAtlas = null;
		MapPixel px = null;
		px = getPixelAtCoord(carte, point);
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
	 * retourne les informations concernant le point donné de la carte donnée
	 * @param carte
	 * @param point
	 * @return
	 */
	public MapPixel getPixelAtCoord(String carte, Point point) {
		MapPixel result = null;
		int id = MapManager.getIdAtCoordOnMap(carte, point);
		result = SpriteData.getPixelFromId(id);
		if (result == null){
			if (unmapped_ids.get(id)==null){
				unmapped_ids.put(id,new ArrayList<Point>());
				unmapped_ids.get(id).add(point);
			}else{
				unmapped_ids.get(id).add(point);
			}
			logger.warn("ID "+id+" non mappée => "+point.x+";"+point.y+"@"+carte);
		}
		return result;
	}

	/**
	 * Retourne les sprites du chunk
	 * @return
	 */
	public Map<Point, Sprite> getSprites() {
		return chunk_sprites;
	}

	/**
	 * Returne les ID des cases du chunk
	 * @param carte
	 * @return
	 */
	public List<Integer> getIds(String carte){
		List<Integer> result = new ArrayList<Integer>();
		Iterator<Point> iter_cells = chunk_sprites.keySet().iterator();
		while (iter_cells.hasNext()){
			Point pt = iter_cells.next();
			result.add(MapManager.getIdAtCoordOnMap(carte, pt));
		}
		return result;
	}
	
	/**
	 * Retourne le point central du chunk
	 * @return
	 */
	public Point getCenter() {
		return center;
	}

	/**
	 * Fixe le point central du chunk
	 * @param center
	 */
	public void setCenter(Point center) {
		this.center = center;
	}

	/**
	 * Retourne les tuiles du chunk
	 * @return
	 */
	public Map<Point, Sprite> getTiles() {
		return chunk_tiles;
	}
	
	
	private TextureRegion getUnknownTile(){
		TextureAtlas texAtlas = loadingStatus.getTextureAtlasSprite("Unknown");
		if (texAtlas == null){
			texAtlas = AssetsLoader.load("Unknown");
		}
		return texAtlas.findRegion("Unknown Tile");
	}
	/**
	 * Démarre une horloge qui va vérifier périodiquement la position de la camera sur les chunks
	 * afin de déclenché le déplacement de la chunkMap au besoin.
	 */
	public static void startChunkMapWatcher() {
		Runnable r = RunnableCreatorUtil.getChunkMapWatcherRunnable();
		ThreadsUtil.executePeriodicallyInThread(r, watcher_delay, watcher_delay, TimeUnit.MILLISECONDS);
	}
	
	public Map<Integer,List<Point>> getUnmappedIds(){
		return unmapped_ids;
	}
}
