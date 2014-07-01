package opent4c;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
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
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private static List<MapPixel> unknownPixels = new ArrayList<MapPixel>();
	private static Map<Integer, List<MapPixel>> pixel_index = new HashMap<Integer, List<MapPixel>>();

	
	/**
	 * Loads sprite data from file
	 */
	public static void load(){
		loadPixelIndex();
	}

	/**
	 * 
	 */
	private static void loadPixelIndex() {
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
		MapPixel px = null;
		while(true){
			try {
				px = (MapPixel) ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				logger.fatal(e);
				System.exit(1);
			} catch (IOException e) {
				logger.info("Lecture de pixel_index terminée");
				break;
			}
			if(px.getId()!= -1){
				putPixel(px);
			}else{
				unknownPixels.add(px);
			}
		}
		try {
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
	}

	/**
	 * 
	 */
	public static void createPixelIndex() {
		
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
		Iterator<Integer> iter_id = pixel_index.keySet().iterator();
		while (iter_id.hasNext()){
			int id = iter_id.next();
			Iterator<MapPixel> iter_pixels = pixel_index.get(id).iterator();
			while (iter_pixels.hasNext()){
				MapPixel px = iter_pixels.next();
				try {
					oos.writeObject(px);
				} catch (IOException e) {
					e.printStackTrace();
					logger.fatal(e);
					System.exit(1);
				}
			}			
		}
		
		try {
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}			
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
				tmpX = Integer.parseInt(firstPart);
				tmpY = Integer.parseInt(secondPart);
				if (tmpX>moduloX)moduloX = tmpX;
				if (tmpY>moduloY)moduloY = tmpY;
			}catch(StringIndexOutOfBoundsException exc){
				logger.fatal("Erreur dans le calcul du modulo : "+tile.getName());
				exc.printStackTrace();
				System.exit(1);
			}
			Iterator<Integer> iter_id = pixel_index.keySet().iterator();
			while(iter_id.hasNext()){
				int key = iter_id.next();
				Iterator<MapPixel> iter_px = pixel_index.get(key).iterator();
				while(iter_px.hasNext()){
					MapPixel px = iter_px.next();
					if(px.getAtlas().equals(tileDir.getName())){
						px.setModulo(PointsManager.getPoint(moduloX, moduloY));
					}
				}
			}
		}
	}

	/**
	 * @param pixel
	 */
	public static void putPixel(MapPixel pixel) {
		if(pixel_index.containsKey(pixel.getId())){
			pixel_index.get(pixel.getId()).add(pixel);
		}else{
			List<MapPixel> list = new ArrayList<MapPixel>();
			list.add(pixel);
			pixel_index.put(pixel.getId(), list);
		}
	}

	public static void matchIdWithPixel(MapPixel pixel) {
		Iterator<Integer> iter = SpriteData.ids.keySet().iterator();
		while (iter.hasNext()){
			int key = iter.next();
			SpriteName sn = SpriteData.ids.get(key);
			if (pixel.getTex().equals(sn.getName())){//First, try a total match
				pixel.setId(key);
				return;
			}
		}
		iter = SpriteData.ids.keySet().iterator();
		while (iter.hasNext()){
			int key = iter.next();
			SpriteName sn = SpriteData.ids.get(key);
			if (pixel.getTex().startsWith(sn.getName())){//if unsuccessful, try a startswith match
				pixel.setId(key);
				return;			}
		}
		iter = SpriteData.ids.keySet().iterator();
		while (iter.hasNext()){
			int key = iter.next();
			SpriteName sn = SpriteData.ids.get(key);
			if (pixel.getTex().toLowerCase().contains(sn.getName().toLowerCase())){//if unsuccessful, try a lowercased contains match
				pixel.setId(key);
				return;			}
		}
			pixel.setId(-1);
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
					readIdLine(line);
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
	 * Reads a line from id.txt
	 * @param line
	 */
	static void readIdLine(String line){
		int key = 0;
		String value = "";
		key = Integer.parseInt(line.substring(0, line.indexOf(' ')));
		value = line.substring(line.indexOf(" ")+1);
		SpriteName name = new SpriteName(value);
		List<MapPixel> list = new ArrayList<MapPixel>();
		list.add(new MapPixel(key, name));
		pixel_index.put(key, list);
		ids.put(key,name);
	}

	/**
	 * @return
	 */
	public static Map<Integer, List<MapPixel>> getPixelIndex() {
		return pixel_index;
	}

	/**
	 * @param id
	 * @return
	 */
	public static MapPixel getPixelFromId(int id) {
		if(pixel_index.containsKey(id)){
			if(pixel_index.get(id).size()>1){
				return pixel_index.get(id).get(1);
			}
		}
		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public static boolean isKnownId(int id) {
		if(pixel_index.containsKey(id)){
			if(pixel_index.get(id).size() > 1)return true;
		}
		return false;
	}

	/**
	 * @param id
	 * @return
	 */
	public static List<MapPixel> getPixelsWithId(int id) {
		return pixel_index.get(id);
	}

	/**
	 * @return
	 */
	public static List<MapPixel> getUnknownPixels() {
		return unknownPixels;
	}
}
