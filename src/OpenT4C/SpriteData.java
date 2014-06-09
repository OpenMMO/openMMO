package OpenT4C;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.FileLister;
import t4cPlugin.MapPixel;
import t4cPlugin.SpriteName;
import t4cPlugin.utils.FilesPath;
import t4cPlugin.utils.PointsManager;

public class SpriteData {
	private static Logger logger = LogManager.getLogger(SpriteData.class.getSimpleName());
	private static Map<Integer,MapPixel> sprite_data = new HashMap<Integer,MapPixel>();
	
	public static void load(){
		BufferedReader buf = null;
		try {
			buf  = new BufferedReader(new FileReader(FilesPath.getSpriteDataFilePath()));
		} catch (IOException e1) {
			logger.fatal(e1);
			System.exit(1);
		}
		String line = "";
		try {
			while((line = buf.readLine()) != null){//On lit le fichier sprite_data
				String[] index = line.split("\\;");//On lit chaque ligne du fichier, et pour chaque ligne :
				boolean tuile = false;
				if (index[0].equals("1"))tuile = true;//On vérifie si c'est une tuile
				int id = Integer.parseInt(index[1]);//On récupère l'ID
				String atlas = index[2];//On récupère le nom de l'atlas
				String tex = index[3];//On récupère le nom de la texture
				int offsetX = Integer.parseInt(index[9]);//On récupère l'Offset
				int offsetY = Integer.parseInt(index[10]);
				int moduloX = Integer.parseInt(index[14]);//On récupère le modulo pour appliquer l'effet de zone
				int moduloY = Integer.parseInt(index[15]);
				//System.err.println("ID "+id+" : "+tex+" "+moduloX+","+moduloY+"|"+tuile);
				sprite_data.put(id, new MapPixel(tuile, atlas, tex, PointsManager.getPoint(offsetX,offsetY), PointsManager.getPoint(moduloX,moduloY), id));//On enregistre une liste avec les ID, les coordonnées et les références graphiques
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

	public static void create(){
		SpriteManager.decryptDPD();
		SpriteManager.decryptDID();
		SpriteManager.decryptDDA(false);
		computeModulo();
		OutputStreamWriter dat_file = null;
		try {
			dat_file = new OutputStreamWriter(new FileOutputStream(FilesPath.getSpriteDataDirectoryPath()+"sprite_data"));
		} catch (FileNotFoundException e) {
			logger.fatal(e);
			System.exit(1);
		}
		Iterator<SpriteName> iter_sprites = SpriteManager.getSprites().keySet().iterator();
		while (iter_sprites.hasNext()){
			SpriteName key = iter_sprites.next();
			Sprite sp = SpriteManager.getSprites().get(key);
			int tuile = 0;
			if (sp.isTuile()) tuile = 1;
			try {
				dat_file.write(tuile+";"+sp.getId()+";"+sp.getChemin()+";"+sp.getName()+";"+sp.getType()+";"+sp.getOmbre()+";"+sp.getLargeur()+";"+sp.getHauteur()+";"+sp.getCouleurTrans()+";"+sp.getOffsetX()+";"+sp.getOffsetY()+";"+sp.getOffsetX2()+";"+sp.getOffsetY2()+";"+sp.getNumDda()+";"+sp.getModuloX()+";"+sp.getModuloY()+System.lineSeparator());
			} catch (IOException e) {
				logger.fatal(e);
				System.exit(1);
			}
		}
		try {
			dat_file.close();
		} catch (IOException e) {
			logger.fatal(e);
			e.printStackTrace();
			System.exit(1);
		}
		logger.info("sprite_data écrit.");
	}
	
	
	public static void computeModulo(){
		//logger.info("Modulo à calculer : "+sprite.getChemin());
		ArrayList<File> tileDirs = new ArrayList<File>();
		tileDirs.addAll(FileLister.listerDir(new File(FilesPath.getTuileDirectoryPath())));
		logger.info("Nombre de modulos à calculer : "+tileDirs.size());
		Iterator<File> iter_tiledirs = tileDirs.iterator();
		while (iter_tiledirs.hasNext()){
			int moduloX=1, moduloY=1;
			File tileDir = iter_tiledirs.next();
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
					//TODO Réparer le nom de fichier de la tuile Lava : pas d'espace après la virgule
					if (secondPart.equals("")){
						secondPart = tile.getName().substring(tile.getName().indexOf(',')+1, tile.getName().indexOf(')'));
					}
					tmpX = Integer.parseInt(firstPart);
					tmpY = Integer.parseInt(secondPart);
					if (tmpX>moduloX)moduloX = tmpX;
					if (tmpY>moduloY)moduloY = tmpY;
				}catch(StringIndexOutOfBoundsException exc){
					logger.fatal("Erreur dans le calcul du modulo : "+tile.getName());
					System.exit(1);
				}
				Iterator<SpriteName>iter_sprites = SpriteManager.getSprites().keySet().iterator();
				while(iter_sprites.hasNext()){
					SpriteName key = iter_sprites.next();
					Sprite sprite = SpriteManager.getSprites().get(key);
					if (sprite.getName().contains(tileDir.getName())){
						sprite.setModuloX(moduloX);
						sprite.setModuloY(moduloY);
					}
				}
			}
			logger.info("Modulo : "+tileDir.getName()+" => "+moduloX+";"+moduloY);
		}
	}
	
	public static MapPixel getPixelFromId(int id) {
		return sprite_data.get(id);
	}
}
