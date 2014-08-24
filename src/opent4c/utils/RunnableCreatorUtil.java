package opent4c.utils;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import opent4c.Chunk;
import opent4c.InputManager;
import opent4c.SpriteData;
import opent4c.UpdateDataCheckStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import screens.IdEditMenu;
import screens.MapManager;
import tools.DataInputManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class RunnableCreatorUtil {

	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private static Logger logger = LogManager.getLogger(RunnableCreatorUtil.class.getSimpleName());
	
	private RunnableCreatorUtil() {
		//Utility class
	}
	
	public static Runnable getTuilePackerRunnable(final File file, final Settings setting)
	{
		Runnable r = new Runnable(){
			public void run(){
				logger.info("Empaquetage de l'atlas : "+file.getName());
				TexturePacker.processIfModified(setting, file.getPath(), FilesPath.getAtlasTuileDirectoryPath(), file.getName());
				loadingStatus.addTilesAtlasPackaged(file.getName());
			}
		};
		return r;
	}
	
	public static Runnable getSpritePackerRunnable(final File file, final Settings setting) {
		Runnable r = new Runnable() {
			public void run() {
				logger.info("Empaquetage de l'atlas : "+file.getName());
				TexturePacker.processIfModified(setting, file.getPath(), FilesPath.getAtlasSpriteDirectoryPath(), file.getName());
				loadingStatus.addSpritesAtlasPackaged(file.getName());
			}
		};
		
		return r;
	}
	
	public static Runnable getTextureAtlasTileCreatorRunnable(final String name) {
		Runnable r = new Runnable(){
			public void run(){
				String nom = name.substring(0, name.length()-6);
				TextureAtlas atlas = new TextureAtlas(FilesPath.getAtlasTilesFilePath(nom));
				loadingStatus.addTextureAtlasTile(nom , atlas);
				UpdateDataCheckStatus.setStatus("Tuiles "+nom+" chargée : "+loadingStatus.getNbTextureAtlasTile()+"/"+loadingStatus.getNbTilesAtlas());
				loadingStatus.addOneTileAtlasLoaded();
			}
		};
		return r;
	}
	
	public static Runnable getForceTextureAtlasSpriteCreatorRunnable(final String name) {
		Runnable r = null;
		if(name.equals("Utils")){
			r = new Runnable(){
				public void run(){
					Gdx.app.postRunnable(new Runnable(){
						public void run(){
							TextureAtlas atlas = new TextureAtlas(FilesPath.getAtlasUtilsFilePath());
							loadingStatus.addTextureAtlasSprite(name, atlas);
						}
					});
				}
			};
		}else {
			r = new Runnable(){
				public void run(){
					Gdx.app.postRunnable(new Runnable(){
						public void run(){
							TextureAtlas atlas = null;
							if(new File(FilesPath.getAtlasSpritesFilePath(name)).exists()){
								atlas = new TextureAtlas(FilesPath.getAtlasSpritesFilePath(name));
							} else{
								atlas = new TextureAtlas(FilesPath.getAtlasUtilsFilePath());
								//TODO c'est de la bidouille pour que le programme de ne plante pas, mais il faudra dégager ça une fois les atlas correctement empaquetés
								//logger.warn("Il semblerait qu'un atlas un soit pas empaqueté : "+name+". On le remplace par Unknown pour le moment.");
							}
							loadingStatus.addTextureAtlasSprite(name, atlas);
						}
					});
				}
			};
		}
		return r;
	}

	public static Runnable getChunkMapWatcherRunnable() {
		Runnable r = new Runnable(){
			public void run() {
				MapManager.updateChunkPositions();
			}
		};
		return r;
	}

	/*public static Runnable getChunkCreatorRunnable(final Places place) {
		Runnable r = new Runnable(){
			public void run() {
				MapManager.teleport(place);
			}
		};
		return r;
	}*/
	
	public static Runnable getModuloComputerRunnable(final File tileDir){
		Runnable r = new Runnable(){
			public void run(){
				SpriteData.computeModulo(tileDir);
				loadingStatus.addOneComputedModulo();
				UpdateDataCheckStatus.setStatus("Modulos calculés : "+loadingStatus.getNbComputedModulos()+"/"+loadingStatus.getNbModulosToBeComputed());
			}
		};
		return r;
	}


	/**
	 * @return
	 */
	public static Runnable getHighlighterRunnable() {
		Runnable r = new Runnable(){
			public void run(){
				MapManager.tileFadeIn();
				try {
					Thread.sleep((long) (500*MapManager.blink_period));
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.fatal(e);
					Gdx.app.exit();
				}
				MapManager.tileFadeOut();
			}
		};
		return r;
	}

	/**
	 * @param camera
	 * @param direction
	 * @return
	 */
	public static Runnable getCameraMoverRunnable(final OrthographicCamera camera, final int direction) {
	Runnable r = new Runnable(){
		public void run(){
			switch(direction){
			case 0 : camera.translate(-2*InputManager.getMovespeed(),0); break;
			case 1 : camera.translate(2*InputManager.getMovespeed(),0); break;
			case 2 : camera.translate(0,-InputManager.getMovespeed()); break;
			case 3 : camera.translate(0,InputManager.getMovespeed()); break;

			}
		}
	};
	return r;
	}

	/**
	 * @param point 
	 * @return
	 */
	public static Runnable getPixelIndexFileUpdaterRunnable(final Point point) {
		Runnable r = new Runnable(){

			public void run(){
				logger.info("mise à jour du fichier pixel_index en tâche de fond, l'affichage sera mis à jour lorsque ce sera terminé");
				SpriteData.loadIdFullFromFile();
				SpriteData.createPixelIndex();
				SpriteData.initPixelIndex();
				SpriteData.loadPixelIndex();
				MapManager.teleport(new Places("update ok", "v2_worldmap", point));
				logger.info("mise à jour terminée");
			}
		};
		return r;
	}

	/**
	 * @param atlas 
	 * @param tex 
	 * @param id 
	 * @return
	 */
	public static Runnable getConsoleRunnable() {
		Runnable r = new Runnable(){

			public void run(){
				IdEditMenu.command();
			}
		};
		return r;
	}

	public static Runnable getIdEditListCreatorRunnable() {
		Runnable r = new Runnable(){

			public void run(){
				logger.info("Création de la liste d'ID pour édition.");
				UpdateDataCheckStatus.setStatus("Création de la liste d'ID pour édition.");
				MapManager.setIdEditList(new HashMap<Integer, Point>());
				int last = -1;
				ByteBuffer map = MapManager.getIdMaps().get("v2_worldmap");
				map.rewind();
				while(map.position()<map.capacity()){
					byte b1=0,b2=0;
					b1 = map.get();
					b2 = map.get();
					int id = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
					if(id != last){
						if(!MapManager.getIdEditList().containsKey(id)){
							MapManager.getIdEditList().put(id, PointsManager.getPoint((map.position()/2)%3072, (map.position()/6144)));
						}
						last = id;
					}
				}
				MapManager.setIdEditListCreated(true);
				logger.info("Création de la liste d'ID pour édition terminée.");
				UpdateDataCheckStatus.setStatus("Création de la liste d'ID pour édition terminée.");
			}
		};
		return r;
	}

	public static Runnable getMapLoaderRunnable(final File f) {
		Runnable r = new Runnable(){

			public void run(){
					UpdateDataCheckStatus.setStatus("Chargement carte : "+f.getName());
					logger.info("Chargement carte : "+f.getName());
					ByteBuffer buf = ByteBuffer.allocate((int)f.length());
					try {
						DataInputManager in = new DataInputManager (f);
						while (buf.position() < buf.capacity()){
							buf.put(in.readByte());
						}
						in.close();
					}catch(IOException exc){
						exc.printStackTrace();
						Gdx.app.exit();
					}
					buf.rewind();
					MapManager.getIdMaps().put(f.getName().substring(0, f.getName().indexOf('.')),buf);
			}
		};
		return r;

	}
	
	public static Runnable getMapInitializerRunnable() {
		Runnable r = new Runnable(){

			public void run(){
				MapManager.init();
			}
		};
		return r;
	}

	public static Runnable getChunkRendererRunnable() {
		Runnable r = new Runnable(){

			public void run(){
				MapManager.renderChunks();
			}
		};
		return r;
	}
}
