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
import java.util.Set;

import opent4c.utils.FileLister;
import opent4c.utils.FilesPath;
import opent4c.utils.LoadingStatus;
import opent4c.utils.PointsManager;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
		pixel_index.clear();
		
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(FilesPath.getPixelIndexFilePath());
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			logger.fatal(e2);
			System.exit(1);
		}
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(fin);
		} catch (IOException e2) {
			e2.printStackTrace();
			logger.fatal(e2);
			System.exit(1);
		}	
		try {
			pixel_index = (HashMap<String,MapPixel>) ois.readObject();
		} catch (ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
			logger.fatal(e1);
			System.exit(1);
		}
		try {
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		pixel_index.remove(-1);
	}

	/**
	 * 
	 */
	public static void createPixelIndex() {
		logger.info("Création de pixel_index");
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(FilesPath.getPixelIndexFilePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(fout);
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		try {
			oos.writeObject(pixel_index);
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		try {
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		logger.info("Pixel_index créé.");
	}
	
	/**
	 * Computes all tiles modulo
	 */
	public static void computeModulos(){
		ArrayList<File> tileDirs = new ArrayList<File>();
		tileDirs.addAll(FileLister.listerDir(new File(FilesPath.getTuileDirectoryPath())));
		logger.info("Nombre de modulos à calculer : "+tileDirs.size());
		loadingStatus.setNbModulosToBeComputed(tileDirs.size());
		Iterator<File> iter_tiledirs = tileDirs.iterator();
		while (iter_tiledirs.hasNext()){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getModuloComputerRunnable(iter_tiledirs.next()));
		}
		loadingStatus.waitUntilModulosAreComputed();
		logger.info("Application des modulos aux tuiles.");
		applyModulos();
		logger.info("Modulos appliqués");

	}

	/**
	 * Computes one tile directory modulos
	 * @param tileDir
	 */
	public static void computeModulo(File tileDir) {
		//escape smoothing tiles
		if(tileDir.getName().equals("GenericMerge1")|tileDir.getName().equals("GenericMerge3")|tileDir.getName().equals("GenericMerge2Wooden")|tileDir.getName().equals("WoodenSmooth")){
			return;
		}
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
				tmpX = Integer.parseInt(firstPart);
				tmpY = Integer.parseInt(secondPart);
				if (tmpX>moduloX)moduloX = tmpX;
				if (tmpY>moduloY)moduloY = tmpY;
			}catch(StringIndexOutOfBoundsException exc){
				logger.fatal("Erreur dans le calcul du modulo : "+tile.getName());
				exc.printStackTrace();
				System.exit(1);
			}
		}
		//logger.info("Computed modulo : "+tileDir.getName()+"=>"+PointsManager.getPoint(moduloX, moduloY));
		modulos.put(tileDir.getName(), PointsManager.getPoint(moduloX, moduloY));
	}

	
	public static void applyModulos(){
		Iterator<String> iter_id = pixel_index.keySet().iterator();
		while(iter_id.hasNext()){
			String key = iter_id.next();
			MapPixel px = pixel_index.get(key);
			Point modulo = modulos.get(px.getAtlas());
			if(modulo != null){
				px.setModulo(modulo);
				//logger.info("Set modulo : "+px.getAtlas()+"=>"+modulo);
			}
		}
	}
	
	/**
	 * @param pixel
	 */
	public static void putPixel(MapPixel pixel) {
		String key = pixel.getAtlas()+":"+pixel.getTex();
		if(pixel_index.containsKey(key)){
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
			System.exit(1);
		}
	}
	
	private static void setIdfull(Map<Integer, String> idfull) {
		idFull  = idfull;
	}


	//TODO ici on différencie les tuiles des sprites
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
	 * Gets mirrors from mirrors file
	 * puts info into a HashMap<Integer,SpriteName>
	 */
	public static void loadMirrorsFromFile(){
		File mirror_file = new File(FilesPath.getMirrorFilePath());
		SpriteUtils.logger.info("Lecture du fichier "+mirror_file.getName());
		try{
			BufferedReader buff = new BufferedReader(new FileReader(mirror_file.getPath()));			 
			try {
				String line;
				while ((line = buff.readLine()) != null) {
					readMirrorLine(line);
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}
	
	private static void readMirrorLine(String line) {
		int key = 0;
		String value = "";
		key = Integer.parseInt(line.substring(0, line.indexOf(' ')));
		value = line.substring(line.indexOf(' ')+3);
		getMirrors().put(key,value);		
	}

	/**
	 * @return
	 */
	public static Map<String, MapPixel> getPixelIndex() {
		return pixel_index;
	}

	/**
	 * @param id
	 * @return
	 */
	public static MapPixel getSpriteFromId(int id) {
		if (idFull.containsKey(id)){
			String key = idFull.get(id);
			return pixel_index.get(key);
		}else{
			logger.warn("ID non mappée : "+id);
			return null;
		}
	}
	
	/**
	 * @param id
	 * @return
	 */
	/*public static MapPixel getTileFromId(int id) {
		if (idFull.containsKey(id)){
			String key = idFull.get(id);
			return pixel_index.get(key);
		}else{
			logger.warn("ID non mappée : "+id);
			return null;
		}
	}*/

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
			System.exit(1);
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
			if((px.getAtlas().equals(getAtlasFromId(id)) & (px.getTex().equals(getTexFromId(id))))){
				px.setId(id);
			}else if(px.getTex().equals(getAtlasFromId(id))){
				px.setId(id);
			}
		}	
	}
}
