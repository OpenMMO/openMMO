package opent4c;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import opent4c.utils.ThreadsUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;


public class SpriteData {
	private static Logger logger = LogManager.getLogger(SpriteData.class.getSimpleName());
	private static Map<Integer,String> mirrors = null;
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	
	/**
	 * Key : "atlas:aex" , Value : MapPixel
	 */
	private static Map<String,MapPixel> pixel_index = new HashMap<String,MapPixel>();
	private static Map<String, Point> modulos = new HashMap<String, Point>();
	private static Map<Integer, String> idFull = null;
	private static List<MapPixel> origin = new ArrayList<MapPixel>();

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static void loadPixelIndex() {
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
	 * Gets IDfull from idfull.txt file
	 * puts info into a HashMap<Integer,SpriteName>
	 */
	public static void loadIdFullFromFile(){
		setIdfull(new HashMap<Integer,String>());
		File id_file = new File(FilesPath.getIdFullFilePath());
		SpriteUtils.logger.info("Lecture du fichier "+id_file.getName());
		try{
			BufferedReader buff = new BufferedReader(new FileReader(id_file.getPath()));			 
			try {
				String line;
				while ((line = buff.readLine()) != null) {
					readIdFullLine(line);
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Gdx.app.exit();
		}
	}
	
	private static void setIdfull(Map<Integer, String> idfull) {
		idFull  = idfull;
	}


	private static void readIdFullLine(String line) {
		int key = 0;
		String atlas = "";
		String tex = "";
		String[] split = line.split("\\:");
		key = Integer.parseInt(split[0]);
		atlas = split[1];
		tex = split[2];
		getIdfull().put(key, atlas+":"+tex);
	}



	public static Map<Integer, String> getIdfull() {
		return idFull;
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
		if(isTuileId(id)){
			return null;
		}else{
			return getSpriteFromId(id);
		}
	}
	
	/**
	 * Get a MapPixel from an ID and Point
	 */
	public static MapPixel getPixelFromIdAndPoint(int id, Point coord){
		if(isTuileId(id)){
			return getTileFromIdAndPoint(id, coord);
		}else{
			return getSpriteFromId(id);
		}
	}
	
	/**
	 * Get a Tile MapPixel from an ID and a Point.
	 * @param id
	 * @param coord
	 * @return
	 */
	private static MapPixel getTileFromIdAndPoint(int id, Point coord) {
		if (idFull.containsKey(id)){
			String atlas = getAtlasFromId(id);
			String moduledTex = Chunk.getModuledTexNameFromPoint(atlas, id, coord);
			String key = atlas+":"+moduledTex;
			if(pixel_index.containsKey(key)){
				return pixel_index.get(key);
			}else{
				logger.warn("Clé non mappée : "+key);
				return null;
			}
		}else{
			logger.warn("ID non mappée : "+id);
			return null;
		}
	}

	/**
	 * Is there a tile with this id?
	 * @param id
	 * @return
	 */
	public static boolean isTuileId(int id) {
		if (idFull.containsKey(id)){
			if(getTexFromId(id).startsWith("modulos(")){
				return true;
			}
		}
		return false;
	}

	/**
	 * @param id
	 * @return
	 */
	private static MapPixel getSpriteFromId(int id) {
		if (idFull.containsKey(id)){
			String key = idFull.get(id);
			if(pixel_index.containsKey(key)){
				return pixel_index.get(key);
			}else{
				//logger.warn("Clé non mappée : "+key);
				return null;
			}
		}else{
			//logger.warn("ID non mappée : "+id);
			return null;
		}
	}

	/**
	 * 
	 */
	public static void initPixelIndex() {
		pixel_index = new HashMap<String, MapPixel>();
	}





	/**
	 * @param next
	 */
	public static void matchUnknownTileWithId(MapPixel px) {
		Iterator<Integer> iter_id = getIdfull().keySet().iterator();
		while(iter_id.hasNext()){
			int id = iter_id.next();
			if(px.getAtlas().equals(getAtlasFromId(id))){
				px.setId(id);
			}
		}		
	}

	/**
	 * @param next
	 */
	public static void matchUnknownSpriteWithId(MapPixel px) {
		Iterator<Integer> iter_id = getIdfull().keySet().iterator();
		while(iter_id.hasNext()){
			int id = iter_id.next();
			if((px.getAtlas().equals(getAtlasFromId(id))&(px.getTex().equals(getTexFromId(id))))){
				px.setId(id);
			}
		}		
	}

	public static Map<Integer, String> getMirrors(){
		return mirrors;
	}
	
	public static String getAtlasFromId(int id){
		if(idFull.containsKey(id)){
			return idFull.get(id).substring(0, idFull.get(id).indexOf(':'));
		}else{
			return "Atlas non mappé";
		}
	}

	public static String getTexFromId(int id){
		if(idFull.containsKey(id)){
			return idFull.get(id).substring(idFull.get(id).indexOf(':')+1);
		}else{
			return "Texture non mappée";
		}
	}

	public static void writeIdFullToFile() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(FilesPath.getIdFullFilePath(), "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.fatal(e);
			Gdx.app.exit();
		}
		Iterator<Integer> iter = getIdfull().keySet().iterator();
		while(iter.hasNext()){
			int id = iter.next();
			writer.println(id+":"+getAtlasFromId(id)+":"+getTexFromId(id));
		}
		writer.close();
	}

	public static void putOriginPixel(MapPixel pixel) {
		origin.add(pixel);		
	}

	public static List<MapPixel> getOrigin() {
		return origin;
	}

	public static void matchIdWithPixel(MapPixel px) {
		Iterator<Integer> iter_id = getIdfull().keySet().iterator();
		while(iter_id.hasNext()){
			int id = iter_id.next();
			if((px.getAtlas().equals(getAtlasFromId(id)) && (px.getTex().equals(getTexFromId(id))))){
				px.setId(id);
			}else if(px.getTex().equals(getAtlasFromId(id)) && getTexFromId(id).startsWith("modulos(")){
				px.setId(id);
			}
		}	
	}
}
