package opent4c;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import opent4c.utils.AssetsLoader;
import opent4c.utils.LoadingStatus;
import opent4c.utils.Places;
import opent4c.utils.PointsManager;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import screens.MapManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;


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
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private static Map<String, Pixmap> cached_pixmaps = new HashMap<String, Pixmap>();
	private static Group chunk_sprites = new Group();
	private static Group chunk_tiles = new Group();
	private static Group cached_chunk_sprites = new Group();
	private static Group cached_chunk_tiles = new Group();
	//private static final Dimension chunk_size = new Dimension(4,4);//pour tester les chunks
	//private static final Dimension chunk_size = new Dimension((Gdx.graphics.getWidth()/96),(Gdx.graphics.getHeight()/48));//On fait des chunks environ de la taille d' 1/9 de fenêtre, en nombre de tuiles, comme ça c'est transparent pour l'utilisateur sans charger trop de tuiles en mémoire.
	public static final Point chunk_size = PointsManager.getPoint(Gdx.graphics.getWidth()/32,Gdx.graphics.getHeight()/16);
	private static final int SMOOTHING_DELTA = 50000;

	
	/**
	 * Creates a new Chunk form a map name and a point on the map
	 * @param map 
	 * @param point
	 */
	public Chunk(String map, Point point) {
		setCenter(point);
		loadingStatus.waitUntilTextureAtlasTilesCreated();
		int upLimit = point.y-(chunk_size.y/2)-1;
		int downLimit = point.y+(chunk_size.y/2);
		int leftLimit = point.x-(chunk_size.x/2)-1;
		int rightLimit = point.x+(chunk_size.x/2);
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
		String atlas = SpriteData.getAtlasFromId(id);
		String tex = SpriteData.getTexFromId(id);
		if (isTuileId(id)){
			addTileAtCoord(id, point);
			return;
		}else if(isSmoothingId(id)){
			//addSmoothingAtCoord(id, point);
			return;
		}else if(atlas.equals("MIRROR")){
			addMirrorSpriteAtCoord(Integer.parseInt(tex), point);
			return;
		}else{
			addSpriteAtCoord(id, point);
			return;
		}
	}
	
	/**
	 * Adds a smoothing tile on the chunk.
	 * @param id
	 * @param point
	 */
	private void addSmoothingAtCoord(int id, Point point) {
		String smooth_info = SpriteData.getTexFromId(id);
		String tmpl = smooth_info.substring(0, smooth_info.indexOf(" T1"));
		String t1 = smooth_info.substring(smooth_info.indexOf("T1")+3, smooth_info.indexOf(" T2"));
		String t2 = smooth_info.substring(smooth_info.indexOf("T2")+3).trim();
		if (tmpl.startsWith("Tmpl3 ") || tmpl.startsWith("Tmpl1 "))createSmoothing(tmpl,t1,t2,point);
	}

	private void createSmoothing(String tmpl, String t1, String t2, final Point point) {
		addSmoothTile1(Integer.parseInt(t1), point);
		Pixmap template = getCachedPixmap(tmpl);
		int color = detectSmoothingColor(tmpl, template);
		addSmoothingTile2(Integer.parseInt(t2), point, template, color);
	}



	private void addSmoothingTile2(int id, Point point, Pixmap template, int color) {
		Pixmap smoothed = getSmoothedTile(template, id, point, color);
		TextureRegion smooth_tex = new TextureRegion(new Texture(smoothed));
		Acteur act = new Acteur(smooth_tex, point, PointsManager.getPoint(0, 0));
		act.setZIndex(1);
		addSpriteToChunkCache(act);
		}

	private void addSmoothTile1(int id1, Point point) {
		String atl1 = SpriteData.getAtlasFromId(id1);

		TextureAtlas atlas1 = loadingStatus.getTextureAtlasTile(atl1);
		TextureRegion tex1 = atlas1.findRegion(getModuledTexNameFromPoint(atl1, id1, point));
		Acteur act1 = new Acteur(tex1, point, PointsManager.getPoint(0, 0));
		addTileToChunkCache(act1);		
	}

	private int detectSmoothingColor(String tmpl, Pixmap template) {
		int result = 0;
		if (tmpl.startsWith("Tmpl3")){
			result = 11010303;
		}else if (tmpl.startsWith("Tmpl1")){
			result = 255;
		}
		return result;
	}

	private Pixmap getSmoothedTile(Pixmap template, int id, Point point, int color) {
		String atlas = SpriteData.getAtlasFromId(id);
		String texName = getModuledTexNameFromPoint(atlas, id, point);
		TextureAtlas tileAtlas = loadingStatus.getTextureAtlasTile(atlas);
		TextureRegion tile = tileAtlas.findRegion(texName);
		int regionX = tile.getRegionX();
		int regionY = tile.getRegionY();
		//logger.info(atlas+" "+getModuledTexNameFromPoint(atlas, moduloX, moduloY, point)+" "+regionX+";"+regionY);
		int x=0, y=0;
		Texture tileTex = tile.getTexture();
		TextureData txData = tileTex.getTextureData();
		txData.prepare();
		Pixmap px = txData.consumePixmap();
		Pixmap result = new Pixmap(32, 16, Format.RGBA8888);
		while (y < 16){
			while(x < 32){
				int pixel = template.getPixel(x, y);
				if(pixel < color-SMOOTHING_DELTA || pixel > color+SMOOTHING_DELTA)result.drawPixel(x, y, px.getPixel(x+regionX, y+regionY));
				x++;
			}
			y++;
			x = 0;
		}
		return result;
	}

	/**
	 * caches all smooth template pixmaps
	 */
	public static void cacheSmoothingTemplatePixmaps() {
		UpdateDataCheckStatus.setStatus("Mise en cache des templates de smoothing.");
		TextureAtlas tileAtlas = loadingStatus.getTextureAtlasTile("GenericMerge1");
		cacheTemplateAtlas(tileAtlas);
		tileAtlas = loadingStatus.getTextureAtlasTile("GenericMerge3");
		cacheTemplateAtlas(tileAtlas);
		UpdateDataCheckStatus.setStatus("Mise en cache terminée.");
	}

	/**
	 * puts a whole atlas into pixmap cache
	 * @param tileAtlas
	 */
	private static void cacheTemplateAtlas(TextureAtlas tileAtlas) {
		Array<AtlasRegion> tiles = tileAtlas.getRegions();
		Iterator<AtlasRegion> iter = tiles.iterator();
		while(iter.hasNext()){
			cachePixmap(iter.next());
		}		
	}

	/**
	 * puts a Pixmap into cache <name,pixmap>
	 * @param tile
	 */
	private static void cachePixmap(AtlasRegion tile) {
		int regionX = tile.getRegionX();
		int regionY = tile.getRegionY();
		//logger.info("ATLAS : "+atlas+" TEX : "+tmpl+" XY : "+regionX+";"+regionY);
		int x=0, y=0;
		Texture tileTex = tile.getTexture();
		TextureData txData = tileTex.getTextureData();
		txData.prepare();
		Pixmap px = txData.consumePixmap();
		Pixmap result = new Pixmap(32, 16, Format.RGBA8888);
		while (y < 16){
			while(x < 32){
				result.drawPixel(x, y, px.getPixel(x+regionX, y+regionY));
				x++;
			}
			y++;
			x = 0;
		}
		cached_pixmaps.put(tile.name, result);
	}

	/**
	 * Gets a smoothing template pixmap from a name
	 * @param tmpl
	 * @return
	 */
	private Pixmap getCachedPixmap(String tmpl) {
		return cached_pixmaps .get(tmpl);
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
			//logger.warn(id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
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
		
		MapPixel px = SpriteData.getPixelFromId(id);
		if (px == null){
			logger.warn("Not Present in pixel_index : "+id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
			addUnknownTile(point);
			return;
		}
		Acteur act = new Acteur(texRegion,point,px.getOffset());
		if((point.y*16)+px.getOffsetY() > 0){
			act.setZIndex((point.y*16)+px.getOffsetY());
		}else{
			act.setZIndex(0);
		}
		addSpriteToChunkCache(act);
		int id2 = MapManager.getIdAtCoordOnMap("v2_worldmap",PointsManager.getPoint(point.x, point.y+1));
		//if (isTuileId(id2)) addTileAtCoord(id2, point);		
		if (isTuileId(id2)) {
			if(!MapManager.doesRenderSprites()){
				addTileAtCoord(id2, point);
			}else{
				addUnknownTile(point);			
			}
		}
	}

	/**
	 * Adds a sprite to sprite cache.
	 * @param act
	 */
	private synchronized void addSpriteToChunkCache(Acteur act) {
		cached_chunk_sprites.addActor(act);		
	}

	private void addMirrorSpriteAtCoord(int id, final Point point) {
		String atlas = SpriteData.getAtlasFromId(id);
		String tex = SpriteData.getTexFromId(id);
		if (atlas.equals("Atlas non mappé") | tex.equals("Texture non mappée") | atlas.equals("NA")){
			//logger.warn(id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
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
		
		final MapPixel px = SpriteData.getPixelFromId(id);
		if (px == null){
			logger.warn("Not Present in pixel_index : "+id+" => "+atlas+" : "+tex+" @ "+point.x+";"+point.y);
			addUnknownTile(point);
			return;
		}
		Sprite sp = new Sprite(texRegion);
		sp.flip(true, false);
		Acteur mir = new Acteur(sp,point, px.getOffset2());
		if((point.y*16)+px.getOffsetY2() > 0){
			mir.setZIndex((point.y*16)+px.getOffsetY2());
		}else{
			mir.setZIndex(0);
		}
		addSpriteToChunkCache(mir);
		int id2 = MapManager.getIdAtCoordOnMap("v2_worldmap",PointsManager.getPoint(point.x, point.y+1));
		//if (isTuileId(id2)) addTileAtCoord(id2, point);		
		if (isTuileId(id2)) {
			if(!MapManager.doesRenderSprites()){
				addTileAtCoord(id2, point);
			}else{
				addUnknownTile(point);			
			}
		}
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
		if (atlas.equals("Atlas non mappé") | atlas.equals("NA")){
			logger.warn(id+" => "+atlas+" @ "+point.x+";"+point.y);
			addUnknownTile(point);
			return;
		}
		texAtlas = loadingStatus.getTextureAtlasTile(atlas);
		if(texAtlas == null){
			texAtlas = AssetsLoader.load(atlas);
			if(texAtlas == null){
				addUnknownTile(point);
				logger.warn("Atlas missing : "+id+" => "+atlas+" @ "+point.x+";"+point.y);
				return;
			}
		}
		texRegion = texAtlas.findRegion(getModuledTexNameFromPoint(atlas, id, point));
		if(texRegion == null){
			logger.warn("TextureRegion missing : "+id+" => "+atlas+" : "+getModuledTexNameFromPoint(atlas, id, point)+" @ "+point.x+";"+point.y);
			addUnknownTile(point);
			return;
		}
		Acteur act = new Acteur(texRegion,point,PointsManager.getPoint(0, 0));
		addTileToChunkCache(act);
	}

	private boolean isSmoothingId(int id) {
		String tex = SpriteData.getTexFromId(id);
		if(tex.contains("Tmpl") && tex.contains("T1") && tex.contains("T2"))return true;
		return false;
	}

	private boolean isTuileId(int id) {
		if(SpriteData.getTexFromId(id).startsWith("modulos(")) return true;
		return false;
	}

	private void addUnknownTile(Point point) {
		Acteur act = new Acteur(getUnknownTile(),PointsManager.getPoint(point.x, point.y),PointsManager.getPoint(0, 0));
		addTileToChunkCache(act);
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
	public static String getModuledTexNameFromPoint(String atlas, int id, Point point) {
		String tx = SpriteData.getTexFromId(id);
		int moduloX = Integer.parseInt(tx.substring(tx.indexOf('(')+1,tx.indexOf(',')));
		int moduloY = Integer.parseInt(tx.substring(tx.indexOf(',')+1,tx.indexOf(')')));
		return atlas+" ("+((point.x % moduloX)+1)+", "+((point.y % moduloY)+1)+")";	
	}

	/**
	 * Compute Chunks position from the center of the first chunk and the chunks size
	 * @param startpoint
	 * @param chunk_size
	 * @return a Map of chunk positions
	 */
	public static Map<Integer, Point> computeChunkPositions(Point startpoint) {
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
	
	/**
	 * Add a tile to tile cache.
	 * @param act
	 */
	private static void addTileToChunkCache(Acteur act) {
		act.setZIndex(0);
		cached_chunk_tiles.addActor(act);		
	}

	private static void clearCache() {
		cached_chunk_sprites = new Group();
		cached_chunk_tiles = new Group();
	}

	public static void swapChunkCache() {
		chunk_sprites = cached_chunk_sprites;
		chunk_tiles = cached_chunk_tiles;
		clearCache();
	}

	public static Group getChunkSprites() {
		return chunk_sprites;
	}

	public static Group getChunkTiles() {
		return chunk_tiles;
	}
	
	/**
	 * Moves chunks to new coord
	 */
	public static void move(Point coord){
		Places place = new Places("ChunkMove", "v2_worldmap", PointsManager.getPoint(coord.x/32, coord.y/16));
		MapManager.createChunks(place);
		logger.info("Création des chunks terminée.");
		MapManager.renderChunks();
	}
}
