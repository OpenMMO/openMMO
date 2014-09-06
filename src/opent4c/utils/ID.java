/**
 * 
 */
package opent4c.utils;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import opent4c.MapPixel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;

/**
 * @author synoga
 *
 */
public class ID {
	private static Logger logger = LogManager.getLogger(ID.class.getSimpleName());
	
	private static Map<Integer, String> all = null;
	
	private static Map<Integer, String> mirrors = null;
	
	private static Map<Integer, String> tiles = null;
	
	private static Map<Integer, String> sprites = null;
	
	private static Map<Integer, String> smooth = null;
	
	private static Map<Integer, String> todo_atlas = null;
	
	private static Map<Integer, String> todo_dtm = null;
	
	private static Map<Integer, String> todo_srf = null;
	
	private static Map<Integer, String> todo_smooth = null;
	
	private static Map<Integer, String> todo_wtf = null;
	
	public ID(){
		//Utility class
	}

	public static boolean containsId(int id){
		if(all.containsKey(id))return true;
		return false;
	}

	/**
	 * Is there a tile with this id?
	 * @param id
	 * @return
	 */
	public static boolean isTileId(int id) {
		if (tiles.containsKey(id))return true;
		return false;
	}
	
	/**
	 * Is there a sprite with this id?
	 * @param id
	 * @return
	 */
	public static boolean isSpriteId(int id) {
		if (sprites.containsKey(id))return true;
		return false;
	}
	
	/**
	 * Is there a mirror with this id?
	 * @param id
	 * @return
	 */
	public static boolean isMirrorId(int id) {
		if (mirrors.containsKey(id))return true;
		return false;
	}
	
	/**
	 * Is there a smoothing with this id?
	 * @param id
	 * @return
	 */
	public static boolean isSmoothId(int id) {
		if (smooth.containsKey(id))return true;
		return false;
	}
	
	/**
	 * Is there a todo atlas with this id?
	 * @param id
	 * @return
	 */
	public static boolean isTodo_AtlasId(int id) {
		if (todo_atlas.containsKey(id))return true;
		return false;
	}
	
	/**
	 * Is there a todo dtm with this id?
	 * @param id
	 * @return
	 */
	public static boolean isTodo_DtmId(int id) {
		if (todo_dtm.containsKey(id))return true;
		return false;
	}
	
	/**
	 * Is there a todo smooth with this id?
	 * @param id
	 * @return
	 */
	public static boolean isTodo_SmoothId(int id) {
		if (todo_smooth.containsKey(id))return true;
		return false;
	}
	
	/**
	 * Is there a todo srf with this id?
	 * @param id
	 * @return
	 */
	public static boolean isTodo_SrfId(int id) {
		if (todo_srf.containsKey(id))return true;
		return false;
	}
	
	/**
	 * Is there a todo wtf with this id?
	 * @param id
	 * @return
	 */
	public static boolean isTodo_WtfId(int id) {
		if (todo_wtf.containsKey(id))return true;
		return false;
	}
	
	/**
	 * Is there something to do with this id?
	 * @param id
	 * @return
	 */
	public static boolean isTodoId(int id) {
		if (todo_atlas.containsKey(id))return true;
		if (todo_dtm.containsKey(id))return true;
		if (todo_smooth.containsKey(id))return true;
		if (todo_srf.containsKey(id))return true;
		if (todo_srf.containsKey(id))return true;
		if (todo_wtf.containsKey(id))return true;
		return false;
	}

	public static String getAtlasFromId(int id){
		if(isTileId(id))return tiles.get(id).substring(0, tiles.get(id).indexOf(':'));
		if(isSpriteId(id))return sprites.get(id).substring(0, sprites.get(id).indexOf(':'));
		if(isMirrorId(id))return mirrors.get(id).substring(0, mirrors.get(id).indexOf(':'));
		if(isSmoothId(id))return smooth.get(id).substring(0, smooth.get(id).indexOf(':'));
		if(isTodo_AtlasId(id))return todo_atlas.get(id).substring(0, todo_atlas.get(id).indexOf(':'));
		if(isTodo_DtmId(id))return todo_dtm.get(id).substring(0, todo_dtm.get(id).indexOf(':'));
		if(isTodo_SmoothId(id))return todo_smooth.get(id).substring(0, todo_smooth.get(id).indexOf(':'));
		if(isTodo_SrfId(id))return todo_srf.get(id).substring(0, todo_srf.get(id).indexOf(':'));
		if(isTodo_WtfId(id))return todo_wtf.get(id).substring(0, todo_wtf.get(id).indexOf(':'));
		return "ATLAS ERROR";
	}

	public static String getTexFromId(int id){
		if(isTileId(id))return tiles.get(id).substring(1+tiles.get(id).indexOf(':'));
		if(isSpriteId(id))return sprites.get(id).substring(1+sprites.get(id).indexOf(':'));
		if(isMirrorId(id))return mirrors.get(id).substring(1+mirrors.get(id).indexOf(':'));
		if(isSmoothId(id))return smooth.get(id).substring(1+smooth.get(id).indexOf(':'));
		if(isTodo_AtlasId(id))return todo_atlas.get(id).substring(1+todo_atlas.get(id).indexOf(':'));
		if(isTodo_DtmId(id))return todo_dtm.get(id).substring(1+todo_dtm.get(id).indexOf(':'));
		if(isTodo_SmoothId(id))return todo_smooth.get(id).substring(1+todo_smooth.get(id).indexOf(':'));
		if(isTodo_SrfId(id))return todo_srf.get(id).substring(1+todo_srf.get(id).indexOf(':'));
		if(isTodo_WtfId(id))return todo_wtf.get(id).substring(1+todo_wtf.get(id).indexOf(':'));
		return "TEX ERROR";
	}

	public static void writeIdFile() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(FilesPath.getIdFilePath(), "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.fatal(e);
			Gdx.app.exit();
		}
		Iterator<Integer> iter = all.keySet().iterator();
		while(iter.hasNext()){
			int id = iter.next();
			writer.println(id+":"+getAtlasFromId(id)+":"+getTexFromId(id));
		}
		writer.close();
	}

	/**
	 * Gets ids from id file
	 */
	public static void loadIdFile(){
		all = new HashMap<Integer,String>();
		tiles = new HashMap<Integer,String>();
		sprites = new HashMap<Integer,String>();
		smooth = new HashMap<Integer,String>();
		todo_atlas = new HashMap<Integer,String>();
		todo_dtm = new HashMap<Integer,String>();
		todo_srf = new HashMap<Integer,String>();
		todo_smooth = new HashMap<Integer,String>();
		todo_wtf = new HashMap<Integer,String>();
		mirrors = new HashMap<Integer,String>();
		File id_file = new File(FilesPath.getIdFilePath());
		logger.info("Lecture du fichier "+id_file.getName());
		try{
			BufferedReader buff = new BufferedReader(new FileReader(id_file.getPath()));			 
			try {
				String line;
				while ((line = buff.readLine()) != null) {
					readIdFileLine(line);
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Gdx.app.exit();
		}
	}
	
	public static void readIdFileLine(String line) {
		int id = 0;
		String atlas = "";
		String tex = "";
		String[] split = line.split("\\:");
		try{
			id = Integer.parseInt(split[0]);
		}catch(NumberFormatException e){
			logger.fatal("Ligne invalide dans id.txt : "+line);
			return;
		}
		atlas = split[1];
		tex = split[2];
		if(atlas.equals("TODOATLAS")){
			todo_atlas.put(id, atlas+":"+tex);
			all.put(id, atlas+":"+tex);
			return;
		}
		if(atlas.equals("TODODTM")){
			todo_dtm.put(id, atlas+":"+tex);
			all.put(id, atlas+":"+tex);
			return;
		}
		if(atlas.equals("TODOSMOOTH")){
			todo_smooth.put(id, atlas+":"+tex);
			all.put(id, atlas+":"+tex);
			return;
		}
		if(atlas.equals("TODOSRF")){
			todo_srf.put(id, atlas+":"+tex);
			all.put(id, atlas+":"+tex);
			return;
		}
		if(atlas.equals("TODOWTF")){
			todo_wtf.put(id, atlas+":"+tex);
			all.put(id, atlas+":"+tex);
			return;
		}
		if(atlas.equals("MIRROR")){
			mirrors.put(id, atlas+":"+tex);
			all.put(id, atlas+":"+tex);
			return;
		}
		if(atlas.equals("SMOOTH")){
			smooth.put(id, atlas+":"+tex);
			all.put(id, atlas+":"+tex);
			return;
		}
		if(tex.startsWith("modulos(")){
			tiles.put(id, atlas+":"+tex);
			all.put(id, atlas+":"+tex);
			return;
		}
		sprites.put(id, atlas+":"+tex);
		all.put(id, atlas+":"+tex);
	}

	/**
	 * @param id
	 * @return
	 */
	public static String getSpriteIdInfos(int id) {
		return sprites.get(id);
	}

	/**
	 * @param next
	 */
	public static void matchUnknownTileWithId(MapPixel px) {
		Iterator<Integer> iter_id = tiles.keySet().iterator();
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
		Iterator<Integer> iter_id = sprites.keySet().iterator();
		while(iter_id.hasNext()){
			int id = iter_id.next();
			if((px.getAtlas().equals(getAtlasFromId(id))&(px.getTex().equals(getTexFromId(id))))){
				px.setId(id);
			}
		}		
	}

	public static void matchIdWithPixel(MapPixel px) {
		if(isSpritePixel(px)){
			matchUnknownSpriteWithId(px);
			return;
		}
		if(isTilePixel(px)){
			matchUnknownTileWithId(px);
			return;
		}
	}

	/**
	 * Is that pixel a sprite?
	 * @param atlas
	 * @return
	 */
	private static boolean isSpritePixel(MapPixel px) {
		Iterator<String> iter = sprites.values().iterator();
		while(iter.hasNext()){
			if(iter.next().equals(px.getAtlas())) return true;
		}
		return false;
	}
	
	/**
	 * Is that pixel a tile?
	 * @param atlas
	 * @return
	 */
	private static boolean isTilePixel(MapPixel px) {
		Iterator<String> iter = sprites.values().iterator();
		while(iter.hasNext()){
			if(iter.next().equals(px.getTex())) return true;
		}
		return false;
	}

	/**
	 * @param moduloY2 
	 * @param moduloX 
	 * @param px
	 * @param point
	 * @return a textureRegion name with zone effect
	 */
	public static String getModuledTexNameFromPoint(int id, Point point) {
		String tx = getTexFromId(id);
		int moduloX = Integer.parseInt(tx.substring(tx.indexOf('(')+1,tx.indexOf(',')));
		int moduloY = Integer.parseInt(tx.substring(tx.indexOf(',')+1,tx.indexOf(')')));
		return getAtlasFromId(id)+" ("+((point.x % moduloX)+1)+", "+((point.y % moduloY)+1)+")";	
	}

	/**
	 * Update an id
	 * @param id
	 * @param input
	 */
	public static void updateId(int id, String input) {
		all.put(id, input);
		writeIdFile();
		loadIdFile();
	}

	/**
	 * @return
	 */
	public static Map<Integer, String> getSpriteIds() {
		return sprites;
	}

}
