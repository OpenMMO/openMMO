package opent4c;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import opent4c.utils.FileLister;
import opent4c.utils.FilesPath;
import opent4c.utils.LoadingStatus;
import opent4c.utils.PointsManager;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.SpriteName;
import opent4c.utils.ThreadsUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpriteData {
	private static Logger logger = LogManager.getLogger(SpriteData.class.getSimpleName());
	public static Map<Integer,SpriteName> ids = null;
	private static Map<String,Point> modulos;
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private static List<SpritePixel> unknownSprites = new ArrayList<SpritePixel>();
	private static List<TilePixel> unknownTiles = new ArrayList<TilePixel>();
	private static Map<Integer, List<SpritePixel>> sprites = new HashMap<Integer,List<SpritePixel>>();
	private static Map<Integer, List<TilePixel>> tiles = new HashMap<Integer, List<TilePixel>>();
	private static Map<Integer, List<MapPixel>> pixels = new HashMap<Integer, List<MapPixel>>();

	
	/**
	 * Loads sprite data from file
	 */
	public static void load(){
		loadSpriteData();
		loadTileData();
		readModuloFile();
		SpriteData.writeIdsToFile();
	}

	/**
	 * Read tile_data file
	 */
	private static void loadTileData() {
		BufferedReader buf = null;
		try {
			buf  = new BufferedReader(new FileReader(FilesPath.getTileDataFilePath()));
		} catch (IOException e1) {
			logger.fatal(e1);
			System.exit(1);
		}
		String line = "";
		try {
			while((line = buf.readLine()) != null){
				readTileDataLine(line);
			}
		} catch (NumberFormatException | IOException | ArrayIndexOutOfBoundsException e) {
			logger.fatal(line);
			logger.fatal(e);
			e.printStackTrace();
			System.exit(1);
		}
		try {
			buf.close();
		} catch (IOException e) {
			logger.fatal(e);
			System.exit(1);
		}				
	}

	/**
	 * reads sprite_data file
	 */
	private static void loadSpriteData() {
		BufferedReader buf = null;
		try {
			buf  = new BufferedReader(new FileReader(FilesPath.getSpriteDataFilePath()));
		} catch (IOException e1) {
			logger.fatal(e1);
			System.exit(1);
		}
		String line = "";
		try {
			while((line = buf.readLine()) != null){
				readSpriteDataLine(line);
			}
		} catch (NumberFormatException | IOException | ArrayIndexOutOfBoundsException e) {
			logger.fatal(line);
			logger.fatal(e);
			e.printStackTrace();
			System.exit(1);
		}
		try {
			buf.close();
		} catch (IOException e) {
			logger.fatal(e);
			System.exit(1);
		}		
	}

	/**
	 * Reads a line from the tile_data file
	 * @param line
	 */
	private static void readTileDataLine(String line) {
		String[] index = line.split("\\;");//On lit chaque ligne du fichier, et pour chaque ligne :
		int id = Integer.parseInt(index[0]);
		String atlas = index[1];
		String tex = index[2];
		int type = Integer.parseInt(index[3]);
		int ombre = Integer.parseInt(index[4]);
		int transColor = Integer.parseInt(index[7]);
		int offsetX = Integer.parseInt(index[8]);
		int offsetY = Integer.parseInt(index[9]);
		int offsetX2 = Integer.parseInt(index[10]);
		int offsetY2 = Integer.parseInt(index[11]);
		int numDDA = Integer.parseInt(index[12]);
		String palette = index[13];
		TilePixel px = new TilePixel(id, atlas, tex, type, ombre, transColor, PointsManager.getPoint(offsetX, offsetY), PointsManager.getPoint(offsetX2, offsetY2), numDDA, palette, false);
		UpdateDataCheckStatus.setSpriteDataStatus("Tuile lue => "+tex);
		putTile(px);
	}
	
	/**
	 * Reads a line from the sprite_data file
	 * @param line
	 */
	private static void readSpriteDataLine(String line) {
		String[] index = line.split("\\;");//On lit chaque ligne du fichier, et pour chaque ligne :
		int id = Integer.parseInt(index[0]);
		String atlas = index[1];
		String tex = index[2];
		int type = Integer.parseInt(index[3]);
		int ombre = Integer.parseInt(index[4]);
		int largeur = Integer.parseInt(index[5]);
		int hauteur = Integer.parseInt(index[6]);
		int transColor = Integer.parseInt(index[7]);
		int offsetX = Integer.parseInt(index[8]);
		int offsetY = Integer.parseInt(index[9]);
		int offsetX2 = Integer.parseInt(index[10]);
		int offsetY2 = Integer.parseInt(index[11]);
		int numDDA = Integer.parseInt(index[12]);
		String palette = index[13];
		boolean perfectMatch = readMatch(index[14]);
		SpritePixel px = new SpritePixel(id, atlas, tex, type, ombre, PointsManager.getPoint(largeur, hauteur), transColor, PointsManager.getPoint(offsetX, offsetY), PointsManager.getPoint(offsetX2, offsetY2), numDDA, palette, perfectMatch);
		UpdateDataCheckStatus.setSpriteDataStatus("Sprite lu => "+tex);
		putSprite(px);
	}

	/**
	 * Read modulo_data file
	 */
	private static void readModuloFile() {
		BufferedReader buf = null;
		try {
			buf  = new BufferedReader(new FileReader(FilesPath.getModuloFilePath()));
		} catch (IOException e1) {
			logger.fatal(e1);
			System.exit(1);
		}
		String line = "";
		try {
			while((line = buf.readLine()) != null){
				readModuloDataLine(line);
			}
		} catch (NumberFormatException | IOException | ArrayIndexOutOfBoundsException e) {
			logger.fatal(line);
			logger.fatal(e);
			e.printStackTrace();
			System.exit(1);
		}
		try {
			buf.close();
		} catch (IOException e) {
			logger.fatal(e);
			System.exit(1);
		}
	}

	/**
	 * @param line
	 */
	private static void readModuloDataLine(String line) {
		String[] index = line.split("\\ ");
		String atlas = index[0];
		String modulo = index[1];
		String[] modulo_index = modulo.split("\\;");
		int moduloX = Integer.parseInt(modulo_index[0]);
		int moduloY = Integer.parseInt(modulo_index[1]);
		Iterator<Integer> iter = tiles.keySet().iterator();
		while(iter.hasNext()){
			int key = iter.next();
			Iterator<TilePixel> iter_px = tiles.get(key).iterator();
			while(iter_px.hasNext()){
				TilePixel px = (TilePixel) iter_px.next();
				if(px.getAtlas().equals(atlas))px.setModulo(PointsManager.getPoint(moduloX, moduloY));
			}
		}
	}

	/**
	 * @param string
	 * @return
	 */
	private static boolean readMatch(String string) {
		if (string.equals("0")) return false;
		return true;
	}
	
	/**
	 * 
	 */
	public static void createTileData() {
		OutputStreamWriter dat_file = null;
		try {
			dat_file = new OutputStreamWriter(new FileOutputStream(FilesPath.getTileDataFilePath()));
		} catch (FileNotFoundException e) {
			logger.fatal(e);
			System.exit(1);
		}
		Iterator<Integer> iter_sprite_ids = getTiles().keySet().iterator();
		while (iter_sprite_ids.hasNext()){
			int key = iter_sprite_ids.next();
			List<TilePixel> px_list = getTiles().get(key);
			Iterator<TilePixel> iter_px = px_list.iterator();
			while(iter_px.hasNext()){
				TilePixel px = iter_px.next();
				writeTileDataLine(px, dat_file);
			}
		}
		try {
			dat_file.close();
		} catch (IOException e) {
			logger.fatal(e);
			e.printStackTrace();
			System.exit(1);
		}				
	}

	/**
	 * @param px
	 * @param dat_file
	 */
	private static void writeTileDataLine(TilePixel px, OutputStreamWriter dat_file) {
		String line = buildTileDataLine(px);
		try {
			dat_file.write(line);
		} catch (IOException e) {
			logger.fatal(e);
			System.exit(1);
		}	
	}

	/**
	 * 
	 */
	public static void createSpriteData() {
		OutputStreamWriter dat_file = null;
		try {
			dat_file = new OutputStreamWriter(new FileOutputStream(FilesPath.getSpriteDataFilePath()));
		} catch (FileNotFoundException e) {
			logger.fatal(e);
			System.exit(1);
		}
		Iterator<Integer> iter_sprite_ids = getSprites().keySet().iterator();
		while (iter_sprite_ids.hasNext()){
			int key = iter_sprite_ids.next();
			List<SpritePixel> px_list = getSprites().get(key);
			Iterator<SpritePixel> iter_px = px_list.iterator();
			while(iter_px.hasNext()){
				SpritePixel px = iter_px.next();
				writeSpriteDataLine(px, dat_file);
			}
		}
		try {
			dat_file.close();
		} catch (IOException e) {
			logger.fatal(e);
			e.printStackTrace();
			System.exit(1);
		}		
	}

	/**
	 * Writes a line into the sprite_data file
	 * @param key
	 * @param dat_file
	 */
	private static void writeSpriteDataLine(SpritePixel px, OutputStreamWriter dat_file) {
		String line = buildSpriteDataLine(px);
		try {
			dat_file.write(line);
		} catch (IOException e) {
			logger.fatal(e);
			System.exit(1);
		}		
	}

	/**
	 * @param px
	 * @return
	 */
	private static String buildSpriteDataLine(SpritePixel px) {
		int match = 0;
		if (px.isPerfectMatch()) match = 1;
		StringBuilder sb = new StringBuilder();
		sb.append(px.getId());
		sb.append(";");
		sb.append(px.getAtlas());
		sb.append(";");
		sb.append(px.getTex());
		sb.append(";");
		sb.append(px.getType());
		sb.append(";");
		sb.append(px.getOmbre());
		sb.append(";");
		sb.append(px.getLargeur());
		sb.append(";");
		sb.append(px.getHauteur());
		sb.append(";");
		sb.append(px.getCouleurTrans());
		sb.append(";");
		sb.append(px.getOffset().x);
		sb.append(";");
		sb.append(px.getOffset().y);
		sb.append(";");
		sb.append(px.getOffset2().x);
		sb.append(";");
		sb.append(px.getOffset2().y);
		sb.append(";");
		sb.append(px.getNumDDA());
		sb.append(";");
		sb.append(px.getPaletteName());
		sb.append(";");
		sb.append(match);
		sb.append(System.lineSeparator());
		
		return sb.toString();
	}

	/**
	 * @param px
	 * @return
	 */
	private static String buildTileDataLine(TilePixel px) {
		StringBuilder sb = new StringBuilder();
		sb.append(px.getId());
		sb.append(";");
		sb.append(px.getAtlas());
		sb.append(";");
		sb.append(px.getTex());
		sb.append(";");
		sb.append(px.getType());
		sb.append(";");
		sb.append(px.getOmbre());
		sb.append(";");
		sb.append(px.getLargeur());
		sb.append(";");
		sb.append(px.getHauteur());
		sb.append(";");
		sb.append(px.getCouleurTrans());
		sb.append(";");
		sb.append(px.getOffset().x);
		sb.append(";");
		sb.append(px.getOffset().y);
		sb.append(";");
		sb.append(px.getOffset2().x);
		sb.append(";");
		sb.append(px.getOffset2().y);
		sb.append(";");
		sb.append(px.getNumDDA());
		sb.append(";");
		sb.append(px.getPaletteName());
		sb.append(System.lineSeparator());
		
		return sb.toString();
	}
	
	/**
	 * Computes all tiles modulo
	 */
	public static void computeModulos(){
		modulos = new HashMap<String, Point>();
		ArrayList<File> tileDirs = new ArrayList<File>();
		tileDirs.addAll(FileLister.listerDir(new File(FilesPath.getTuileDirectoryPath())));
		logger.info("Nombre de modulos à calculer : "+tileDirs.size());
		loadingStatus.setNbModulosToBeComputed(tileDirs.size());
		Iterator<File> iter_tiledirs = tileDirs.iterator();
		while (iter_tiledirs.hasNext()){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getModuloComputerRunnable(iter_tiledirs.next()));
		}
		loadingStatus.waitUntilModulosAreComputed();
	}
	
	/**
	 * writes modulo_data file
	 */
	private static void writeModulos() {
		Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter(FilesPath.getModuloFilePath(), true));
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		
		Iterator<String> iter_tiles = modulos.keySet().iterator();
		while(iter_tiles.hasNext()){
			String atlas = iter_tiles.next();
			try {
				output.append(atlas+" "+modulos.get(atlas).x+";"+modulos.get(atlas).y+System.lineSeparator());
			} catch (IOException e) {
				e.printStackTrace();
				logger.fatal(e);
				System.exit(1);
			}	
		}	
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}		
	}

	/**
	 * Computes one tile directory modulos
	 * @param tileDir
	 */
	public static void computeModulo(File tileDir) {
		int moduloX=1, moduloY=1;
		ArrayList<File> tiles = new ArrayList<File>();
		tiles.addAll(FileLister.lister(tileDir.getAbsoluteFile(),".png"));
		Iterator<File> iter_tiles = tiles.iterator();
		while(iter_tiles.hasNext()){
			File tile = iter_tiles.next();
			try{
				int tmpX=1,tmpY=1;
				String firstPart, secondPart;
				firstPart = tile.getName().substring(tile.getName().indexOf('(')+1, tile.getName().indexOf(','));
				secondPart = tile.getName().substring(tile.getName().indexOf(',')+2, tile.getName().indexOf(')'));
				//logger.warn("Modulo "+tile.getName()+" : "+firstPart+" | "+secondPart);
				tmpX = Integer.parseInt(firstPart);
				tmpY = Integer.parseInt(secondPart);
				if (tmpX>moduloX)moduloX = tmpX;
				if (tmpY>moduloY)moduloY = tmpY;
			}catch(StringIndexOutOfBoundsException exc){
				logger.fatal("Erreur dans le calcul du modulo : "+tile.getName());
				exc.printStackTrace();
				System.exit(1);
			}
			modulos.put(tileDir.getName(),PointsManager.getPoint(moduloX,moduloY));
		}
		//logger.info("Modulo : "+tileDir.getName()+" => "+moduloX+";"+moduloY);		
	}

	/**
	 * 
	 * @param id
	 * @return a SpritePixel list from an ID
	 */
	public static List<SpritePixel> getAllSpriteswithId(int id) {
		if (sprites.containsKey(id)){
			return sprites.get(id);
		}
		return null;
	}
	/**
	 * 
	 * @param id
	 * @return a TilePixel list from an ID
	 */
	public static List<TilePixel> getAllTileswithId(int id) {
		if (tiles.containsKey(id)){
			return tiles.get(id);
		}
		return null;
	}

	/**
	 * 
	 */
	public static void deleteSpriteDataFileOnExit() {
		File f = new File(FilesPath.getSpriteDataFilePath());
		f.deleteOnExit();
	}


	/**
	 * @return
	 */
	public static Map<Integer, List<SpritePixel>> getSprites() {
		return sprites;
	}

	/**
	 * @return
	 */
	public static Map<Integer, List<TilePixel>> getTiles() {
		return tiles;
	}
	
	/**
	 * @param pixel
	 */
	public static SpritePixel putSprite(MapPixel pixel) {
		SpritePixel sprite = new SpritePixel(pixel);
		if(sprite.getId() == -1){
			unknownSprites.add(sprite);
			return sprite;
		}
		if(sprites.containsKey(sprite.getId())){
			sprites.get(sprite.getId()).add(sprite);
		}else{
			List<SpritePixel> lst = new ArrayList<SpritePixel>();
			lst.add(sprite);
			sprites.put(sprite.getId(), lst);
		}
		return sprite;
	}

	/**
	 * @param pixel
	 */
	public static TilePixel putTile(MapPixel pixel) {
		TilePixel tile = new TilePixel(pixel);
		if(tile.getId() == -1){
			unknownTiles.add(tile);
			logger.warn("unknownTiles size should ideally be 0 (when all ids will be mapped");
			return tile;
		}
		if(tiles.containsKey(tile.getId())){
			tiles.get(tile.getId()).add(tile);
		}else{
			List<TilePixel> lst = new ArrayList<TilePixel>();
			lst.add(tile);
			tiles.put(tile.getId(), lst);
		}
		return tile;
	}

	/**
	 * @param pixel
	 */
	public static void removePixels() {
		logger.info("Cleared pixel list");
		pixels.clear();
	}

	/**
	 * @param pixel
	 */
	public static void putPixel(MapPixel pixel) {
		if(!pixels.containsKey(pixel.getId())){
			List<MapPixel> lst = new ArrayList<MapPixel>();
			lst.add(pixel);
			pixels.put(pixel.getId(), lst);
		}else{
			pixels.get(pixel.getId()).add(pixel);
		}		
	}

	/**
	 * @return
	 */
	public static Map<Integer, List<MapPixel>> getPixels() {
		return pixels;
	}

	/**
	 * @param id
	 * @return
	 */
	public static SpritePixel getSpriteFromId(int id) {
		if (sprites.containsKey(id))return sprites.get(id).get(0);
		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public static boolean isTileId(int id) {
		if(tiles.containsKey(id))return true;
		return false;
	}

	/**
	 * @param id
	 * @return
	 */
	public static TilePixel getTileFromId(int id) {
		if(tiles.containsKey(id))return tiles.get(id).get(0);
		return null;
	}

	/**
	 * 
	 */
	public static void createModuloData() {
		computeModulos();
		writeModulos();
	}

	/**
	 * Gets IDs from id.txt file
	 * puts info into a HashMap<Integer,SpriteName>
	 */
	public static void loadIdsFromFile(){
		ids = new HashMap<Integer,SpriteName>();
		File id_file = new File(FilesPath.getIdFilePath());
		SpriteUtils.logger.info("Lecture du fichier "+id_file.getName());
		try{
			BufferedReader buff = new BufferedReader(new FileReader(id_file.getPath()));			 
			try {
				String line;
				while ((line = buff.readLine()) != null) {
					SpriteUtils.readIdLine(line);
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * writes ids into id.txt
	 */
	public static void writeIdsToFile(){
		OutputStreamWriter dat_file = null;
		try {
			dat_file = new OutputStreamWriter(new FileOutputStream(FilesPath.getIdFilePath()));
		} catch (FileNotFoundException e) {
			SpriteUtils.logger.fatal(e);
			System.exit(1);
		}
		Iterator<Integer> iter_sn = ids.keySet().iterator();
		while (iter_sn.hasNext()){
			int id = iter_sn.next();
			SpriteName sn = ids.get(id);
			try {
				dat_file.write(id+" "+sn.getName()+System.lineSeparator());
			} catch (IOException e) {
				SpriteUtils.logger.fatal(e);
				System.exit(1);
			}	
		}
	
		try {
			dat_file.close();
		} catch (IOException e) {
			SpriteUtils.logger.fatal(e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public static boolean isSpriteId(int id) {
		if(sprites.containsKey(id))return true;
		return false;
	}

	/**
	 * @param id
	 * @return
	 */
	public static boolean isUnknownId(int id) {
		if(!isTileId(id) & !isSpriteId(id)) return true;
		return false;
	}
}
