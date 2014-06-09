package OpenT4C;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.AssetsLoader;
import t4cPlugin.MapPixel;
import t4cPlugin.utils.LoadingStatus;
import t4cPlugin.utils.PointsManager;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


/**
 * Cette classe est un morceau de la carte à afficher.
 * @author synoga
 *
 */
public class Chunk{
	
	private static Logger logger = LogManager.getLogger(Chunk.class.getSimpleName());
	private Point center = null;
	private LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private Map<Point,Sprite> chunk_sprites = null;
	private Map<Point,Sprite> chunk_tiles = null;
	private Map<Point,MapPixel> chunk_sprite_info = null;

	
	/**
	 * On remplit une liste de Sprite à partir du point central du Chunk
	 * @param carte 
	 * @param point
	 */
	public Chunk(String carte, Point point) {
		setCenter(point);
		chunk_sprite_info = new HashMap<Point,MapPixel>(MapManager.getChunkSize().width*MapManager.getChunkSize().height);
		chunk_sprites = new HashMap<Point,Sprite>(MapManager.getChunkSize().width*MapManager.getChunkSize().height);
		chunk_tiles = new HashMap<Point,Sprite>(MapManager.getChunkSize().width*MapManager.getChunkSize().height);
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

	private Sprite getTileAtCoord(String carte, Point point) {
		Sprite result = null;
		TextureRegion texRegion = null;
		TextureAtlas texAtlas = null;
		MapPixel px = null;
		px = getPixelAtCoord(carte, point);
		if (px != null){
			chunk_sprite_info.put(point, px);
			texAtlas = loadingStatus.getTextureAtlasTile(px.getAtlas());
			if(texAtlas == null){
				texAtlas = loadingStatus.getTextureAtlasSprite("Black");
				texRegion = texAtlas.findRegion("Black Tile");
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
			texAtlas = loadingStatus.getTextureAtlasSprite("Black");
			texRegion = texAtlas.findRegion("Black Tile");
		}
		result = new Sprite(texRegion);
		result.flip(false, true);
		return result;
	}

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
	 * Crée un Sprite à partir de coordonnées et d'une carte.
	 * @param carte
	 * @param point
	 * @return
	 */
	private Sprite getSpriteAtCoord(String carte, Point point) {
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
			texAtlas = loadingStatus.getTextureAtlasSprite("Unknown");
			if (texAtlas == null){
				texAtlas = AssetsLoader.load("Unknown");
			}
			texRegion = texAtlas.findRegion("Unknown Tile");
		}
		result = new Sprite(texRegion);
		result.flip(false, true);
		return result;
	}

	private MapPixel getPixelAtCoord(String carte, Point point) {
		MapPixel result = null;
		int id = MapManager.getIdAtCoordOnMap(carte, point);
		result = SpriteData.getPixelFromId(id);
		if (result == null){
			logger.warn("ID "+id+" non mappée => "+point.x+";"+point.y+"@"+carte);
		}
		return result;
	}

	public Map<Point, Sprite> getSprites() {
		return chunk_sprites;
	}

	public List<Integer> getIds(String carte){
		List<Integer> result = new ArrayList<Integer>();
		Iterator<Point> iter_cells = chunk_sprites.keySet().iterator();
		while (iter_cells.hasNext()){
			Point pt = iter_cells.next();
			result.add(MapManager.getIdAtCoordOnMap(carte, pt));
		}
		return result;
	}
	
	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public Map<Point, Sprite> getTiles() {
		return chunk_tiles;
	}
}
