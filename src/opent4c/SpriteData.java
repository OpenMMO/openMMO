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
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
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
	public static Map<Integer,String> ids = null;
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private static List<MapPixel> unknownPixels = new ArrayList<MapPixel>();
	private static Map<Integer, List<MapPixel>> pixel_index = new HashMap<Integer, List<MapPixel>>();
	private static Map<String, Point> modulos = new HashMap<String, Point>();

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static void loadPixelIndex() {
		pixel_index.clear();
		unknownPixels.clear();
		
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
			pixel_index = (HashMap<Integer,List<MapPixel>>) ois.readObject();
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
		unknownPixels = pixel_index.get(-1);
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
		logger.info("Computed modulo : "+tileDir.getName()+"=>"+PointsManager.getPoint(moduloX, moduloY));
		modulos.put(tileDir.getName(), PointsManager.getPoint(moduloX, moduloY));
	}

	
	public static void applyModulos(){
		Iterator<Integer> iter_id = pixel_index.keySet().iterator();
		while(iter_id.hasNext()){
			int key = iter_id.next();
			Iterator<MapPixel> iter_px = pixel_index.get(key).iterator();
			while(iter_px.hasNext()){
				MapPixel px = iter_px.next();
				Point modulo = modulos.get(px.getAtlas());
				if(modulo != null){
					px.setModulo(modulo);
					//logger.info("Set modulo : "+px.getAtlas()+"=>"+modulo);
				}
			}
		}
	}
	
	/**
	 * @param pixel
	 */
	public static void putPixel(int id, MapPixel pixel) {
		if(pixel_index.containsKey(id)){
			pixel_index.get(id).add(pixel);
		}else{
			List<MapPixel> list = new ArrayList<MapPixel>();
			list.add(pixel);
			pixel_index.put(id, list);
		}
	}

	public static List<Integer> matchIdWithPixel(MapPixel pixel) {
		List<Integer> result = new ArrayList<Integer>();
		Iterator<Integer> iter = SpriteData.ids.keySet().iterator();
		while (iter.hasNext()){
			int key = iter.next();
			String sn = SpriteData.ids.get(key);
			if (pixel.getTex().equals(sn)){
				result.add(key);
				//logger.info(key+"/"+ids.size()+" : "+sn.getName()+"<=>"+pixel.getTex());
			}
		}
			if(result.size() == 0) result.add(-1);
			return result;
	}

	/**
	 * Gets IDs from id.txt file
	 * puts info into a HashMap<Integer,SpriteName>
	 */
	public static void loadIdsFromFile(){
		ids = new HashMap<Integer,String>();
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
			String sn = ids.get(id);
			try {
				dat_file.write(id+" "+sn+System.lineSeparator());
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
		value = line.substring(line.indexOf(' ')+1);
		ids.put(key,value);
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
			return pixel_index.get(id).get(0);
		}
		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public static boolean isKnownId(int id) {
		if(pixel_index.containsKey(id))return true;
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

	/**
	 * 
	 */
	public static void initPixelIndex() {
		pixel_index = new HashMap<Integer, List<MapPixel>>();
		unknownPixels = new ArrayList<MapPixel>();
	}

	/**
	 * 
	 */
	public static void matchIdWithTiles() {
		Iterator<MapPixel> iter = pixel_index.get(-1).iterator();
		Map<Integer,List<MapPixel>> to_add = new HashMap<Integer,List<MapPixel>>();
		List<Integer> to_remove = new ArrayList<Integer>();
		int index = 0;
		while(iter.hasNext()){
			UpdateDataCheckStatus.setSpriteDataStatus("Match Id With Tiles : "+index+"/"+(pixel_index.get(-1).size()-1));
			UpdateDataCheckStatus.setStatus("Match Id With Tiles : "+index+"/"+(pixel_index.get(-1).size()-1));
			MapPixel px = iter.next();
			Iterator<Integer> iter_id = ids.keySet().iterator();
			while(iter_id.hasNext()){
				int id = iter_id.next();
				String sn = ids.get(id);
				if((px.isTuile()) & (px.getAtlas().equals(sn))){
					px.setId(id);
					if(to_add.containsKey(id)){
						to_add.get(id).add(px);
					}else{
						List<MapPixel> list = new ArrayList<MapPixel>();
						list.add(px);
						to_add.put(id, list);
					}
					if(!to_remove.contains(index))to_remove.add(index);
				}else if(px.isTuile()){
					if(px.getTex().substring(0, px.getTex().indexOf('(')-1).equals(sn)){
						px.setId(id);
						if(to_add.containsKey(id)){
							to_add.get(id).add(px);
						}else{
							List<MapPixel> list = new ArrayList<MapPixel>();
							list.add(px);
							to_add.put(id, list);
						}
					}
				}
			}
			index++;
		}
		Iterator<Integer> iter_to_remove = to_remove.iterator();
		int i = 0;
		while(iter_to_remove.hasNext()){
			pixel_index.get(-1).remove(iter_to_remove.next());
			UpdateDataCheckStatus.setStatus("Mise à jour de pixel_index (suppression des tuiles découvertes de la liste des inconnus) : "+i+"/"+to_remove.size());
			i++;
		}
		i=0;
		Iterator<Integer> iter_to_add = to_add.keySet().iterator();
		while(iter_to_add.hasNext()){
			UpdateDataCheckStatus.setStatus("Mise à jour de pixel_index (ajout des tuiles découvertes de les listes principales) : "+i+"/"+to_add.size());
			int id = iter_to_add.next();
			if(pixel_index.containsKey(id)){
				pixel_index.get(id).addAll(to_add.get(id));
			}else{
				pixel_index.put(id, to_add.get(id));
			}
			i++;
		}
		to_add.clear();
		to_remove.clear();
	}

	/**
	 * @param id
	 * @return
	 */
	public static List<MapPixel> getPixelsWithSameMapping(int id){
		String mapping = ids.get(id);
		List<MapPixel> list = new ArrayList<MapPixel>();
		Iterator<Integer> iter = ids.keySet().iterator();
		while(iter.hasNext()){
			int i = iter.next();
			String s = ids.get(i);
			if(s.equals(mapping))list.addAll(pixel_index.get(i));
		}
		return list;
	}

	/**
	 * @param id
	 * @return
	 */
	public static List<MapPixel> getPixelsWithSameAtlas(int id) {
		String mapping = getPixelFromId(id).getAtlas();
		List<MapPixel> list = new ArrayList<MapPixel>();
		Iterator<MapPixel> iter_px = unknownPixels.iterator();
		while(iter_px.hasNext()){
			MapPixel px = iter_px.next();
			if(mapping.equals(px.getAtlas()))list.add(px);
		}
		Iterator<Integer> iter = pixel_index.keySet().iterator();
		while(iter.hasNext()){
			int i = iter.next();
			iter_px = pixel_index.get(i).iterator();
			while(iter_px.hasNext()){
				MapPixel px = iter_px.next();
				if(mapping.equals(px.getAtlas()))list.add(px);
			}
		}

		return list;
	}
}
