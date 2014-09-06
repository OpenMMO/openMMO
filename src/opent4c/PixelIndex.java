package opent4c;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opent4c.utils.FilesPath;
import opent4c.utils.ID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;


public class PixelIndex {
	private static Logger logger = LogManager.getLogger(PixelIndex.class.getSimpleName());
	/**
	 * Key : "atlas:aex" , Value : MapPixel
	 */
	private static Map<String,MapPixel> pixel_index = new HashMap<String,MapPixel>();
	private static List<MapPixel> origin = new ArrayList<MapPixel>();

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static void loadIndexFile() {
		logger.info("Chargement de pixel_index.");
		UpdateDataCheckStatus.setStatus("Chargement de pixel_index.");
		pixel_index.clear();
		
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(FilesPath.getPixelIndexFilePath());
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			logger.fatal(e2);
			Gdx.app.exit();
		}
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(fin);
		} catch (IOException e2) {
			e2.printStackTrace();
			logger.fatal(e2);
			Gdx.app.exit();
		}	
		try {
			pixel_index = (HashMap<String,MapPixel>) ois.readObject();
		} catch (ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
			logger.fatal(e1);
			Gdx.app.exit();
		}
		try {
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			Gdx.app.exit();
		}
		pixel_index.remove(-1);
		logger.info("Pixel_index chargé.");
		UpdateDataCheckStatus.setStatus("Pixel_index chargé.");
	}

	/**
	 * 
	 */
	public static void createPixelIndex() {
		logger.info("Création de pixel_index");
		UpdateDataCheckStatus.setStatus("Création de pixel_index");
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(FilesPath.getPixelIndexFilePath());

		ObjectOutputStream oos = null;

			oos = new ObjectOutputStream(fout);

			oos.writeObject(pixel_index);
			fout.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.fatal(e);
			Gdx.app.exit();
		}
		logger.info("Pixel_index créé.");
	}

	/**
	 * @param pixel
	 */
	public static void putPixel(MapPixel pixel) {
		String key = pixel.getAtlas()+":"+pixel.getTex();
		if(pixel_index.containsKey(key) && pixel.getId() != -1){
			logger.warn("Doublon : "+key+" => "+pixel.getId()+" : "+pixel.getAtlas()+" : "+pixel.getTex());
		}else{
			pixel_index.put(key, pixel);
		}
	}

	/**
	 * @return
	 */
	public static Map<String, MapPixel> getPixelIndex() {
		return pixel_index;
	}

	/**
	 * Get a Sprite MapPixel from an ID
	 */
	public static MapPixel getPixelFromId(int id){
		if(ID.isTileId(id)){
			return null;
		}else{
			return getSpriteFromId(id);
		}
	}
	
	/**
	 * Get a MapPixel from an ID and Point
	 */
	public static MapPixel getPixelFromIdAndPoint(int id, Point coord){
		if(ID.isTileId(id)){
			return getTileFromIdAndPoint(id, coord);
		}else if(ID.isSpriteId(id)){
			return getSpriteFromId(id);
		}
		return null;
	}
	
	/**
	 * Get a Tile MapPixel from an ID and a Point.
	 * @param id
	 * @param coord
	 * @return
	 */
	private static MapPixel getTileFromIdAndPoint(int id, Point coord) {
		if (ID.isTileId(id)){
			String atlas = ID.getAtlasFromId(id);
			String moduledTex = ID.getModuledTexNameFromPoint(id, coord);
			String key = atlas+":"+moduledTex;
			if(pixel_index.containsKey(key)){
				return pixel_index.get(key);
			}else{
				logger.warn("Clé non mappée : "+key);
				return null;
			}
		}else{
			logger.warn("ID n'est pas une tuile : "+id);
			return null;
		}
	}

	/**
	 * Get a Sprite MapPixel from an ID and a Point.
	 * @param id
	 * @return
	 */
	private static MapPixel getSpriteFromId(int id) {
		if (ID.isSpriteId(id)){
			String key = ID.getAtlasFromId(id)+":"+ID.getTexFromId(id);
			if(pixel_index.containsKey(key)){
				return pixel_index.get(key);
			}
		}
		return null;
	}

	/**
	 * 
	 */
	public static void initPixelIndex() {
		pixel_index = new HashMap<String, MapPixel>();
	}

	public static void putOriginPixel(MapPixel pixel) {
		origin.add(pixel);		
	}

	public static List<MapPixel> getOrigin() {
		return origin;
	}
}
