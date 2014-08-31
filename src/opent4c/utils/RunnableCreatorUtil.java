package opent4c.utils;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import opent4c.Acteur;
import opent4c.Chunk;
import opent4c.InputManager;
import opent4c.SpriteData;
import opent4c.UpdateDataCheckStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import screens.IdEditMenu;
import screens.GameScreen;
import tools.DataInputManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
	
//////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////DATA CHECKING////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	
	public static Runnable getTuilePackerRunnable(final File file, final Settings setting)
	{
		Runnable r = new Thread("Tile packer"){
			public void run(){
				logger.info("Empaquetage de l'atlas : "+file.getName());
				TexturePacker.processIfModified(setting, file.getPath(), FilesPath.getAtlasTuileDirectoryPath(), file.getName());
				loadingStatus.addTilesAtlasPackaged(file.getName());
			}
		};
		return r;
	}
	
	public static Runnable getSpritePackerRunnable(final File file, final Settings setting) {
		Runnable r = new Thread("Sprite packer"){
			public void run() {
				logger.info("Empaquetage de l'atlas : "+file.getName());
				TexturePacker.processIfModified(setting, file.getPath(), FilesPath.getAtlasSpriteDirectoryPath(), file.getName());
				loadingStatus.addSpritesAtlasPackaged(file.getName());
			}
		};
		
		return r;
	}
	
	public static Runnable getTextureAtlasTileCreatorRunnable(final String name) {
		Runnable r = new Thread("Tile atlas loader"){
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
			Runnable r = new Thread("Sprite atlas loader"){
				public void run(){
							TextureAtlas atlas = null;
							if(new File(FilesPath.getAtlasSpritesFilePath(name)).exists()){
								atlas = new TextureAtlas(FilesPath.getAtlasSpritesFilePath(name));
							}else{
								atlas = new TextureAtlas(FilesPath.getAtlasUtilsFilePath());
								//TODO c'est de la bidouille pour que le programme de ne plante pas, mais il faudra dégager ça une fois les atlas correctement empaquetés
								//logger.warn("Il semblerait qu'un atlas un soit pas empaqueté : "+name+". On le remplace par Unknown pour le moment.");
							}
							loadingStatus.addTextureAtlasSprite(name, atlas);
				}
			};
		return r;
	}

	
//////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////MAP MANAGEMENT///////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////

	
	public static Runnable getChunkMapWatcherRunnable() {
		Runnable r = new Thread("Chunkmap watcher"){
			public void run() {
				GameScreen.updateChunkPosition();
			}
		};
		return r;
	}
	
	/**
	 * @return
	 */
	public static Runnable getHighlighterRunnable() {
		Runnable r = new Thread("Highlight"){
			public void run(){
				GameScreen.tileFadeIn();
				try {
					Thread.sleep((long) (500*GameScreen.blink_period));
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.fatal(e);
					Gdx.app.exit();
				}
				GameScreen.tileFadeOut();
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
		Runnable r = new Thread("Camera move"){
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
		Runnable r = new Thread("Pixel index update"){

			public void run(){
				logger.info("mise à jour du fichier pixel_index en tâche de fond, l'affichage sera mis à jour lorsque ce sera terminé");
				SpriteData.loadIdFullFromFile();
				SpriteData.createPixelIndex();
				SpriteData.initPixelIndex();
				SpriteData.loadPixelIndex();
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
		Runnable r = new Thread("Id edit list create"){

			public void run(){
				logger.info("Création de la liste d'ID pour édition.");
				UpdateDataCheckStatus.setStatus("Création de la liste d'ID pour édition.");
				GameScreen.setIdEditList(new HashMap<Integer, Point>());
				GameScreen.creatIdEditListforMap("v2_underworld");
				GameScreen.creatIdEditListforMap("v2_dungeonmap");
				GameScreen.creatIdEditListforMap("v2_cavernmap");
				GameScreen.creatIdEditListforMap("v2_leoworld");
				GameScreen.creatIdEditListforMap("v2_worldmap");
				GameScreen.setIdEditListCreated(true);
				logger.info("Création de la liste d'ID pour édition terminée.");
				UpdateDataCheckStatus.setStatus("Création de la liste d'ID pour édition terminée.");
			}
		};
		return r;
	}

	public static Runnable getMapLoaderRunnable(final File f) {
		Runnable r = new Thread("Map loader"){

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
					GameScreen.getIdMaps().put(f.getName().substring(0, f.getName().indexOf('.')),buf);
			}
		};
		return r;

	}
	
	public static Runnable getMapInitializerRunnable() {
		Runnable r = new Thread("Map initializer"){

			public void run(){
				GameScreen.init();
			}
		};
		return r;
	}

	public static Runnable getSmoothTemplateTileRunnable(final String tmpl, final Point point) {
		Runnable r = new Thread("Smoothing template adder"){

			@Override
			public void run() {
				Chunk.addTemplateTile(tmpl, point);
			}
		};
		return r;
	}

	/**
	 * Teleports player to a new place. Done in a new thread not to use the graphical thread.
	 * @param place
	 * @return
	 */
	public static Runnable getTeleporterRunnable(final Place place) {
		Runnable r = new Thread("Teleport"){

			@Override
			public void run() {
				GameScreen.teleport(place);
			}
		};
		return r;
	}

	public static Runnable getStageGroupAdderRunnable(final Stage stage, final Group group) {
		Runnable r = new Thread("Group add"){

			@Override
			public void run() {
				stage.addActor(group);
			}
		};
		return r;
	}

	public static Runnable getGroupClearerRunnable(final Group group) {
		Runnable r = new Thread("Group clear"){

			@Override
			public void run() {
				group.clear();
			}
		};
		return r;
	}

	public static Runnable getChunkMapTileEngineRunnable() {
		Runnable r = new Thread("Chunk Tile Engine"){

			@Override
			public void run() {
				Chunk.loadLastFromTileQueueIfNeeded();
			}
		};
		return r;
	}

	public static Runnable getChunkMapEngineAddToTilesRunnable(final Acteur acteur) {
		Runnable r = new Thread("Chunk Engine Tile Add"){

			@Override
			public void run() {
				GameScreen.addActorToTiles(acteur);
			}
		};
		return r;
	}

	public static Runnable getChunkMapSpriteEngineRunnable() {
		Runnable r = new Thread("Chunk Sprite Engine"){

			@Override
			public void run() {
				Chunk.loadLastFromSpriteQueueIfNeeded();
			}
		};
		return r;
	}

	public static Runnable getChunkMapEngineAddToSpritesRunnable(final Acteur acteur) {
		Runnable r = new Thread("Chunk Engine Sprites Add"){

			@Override
			public void run() {
				GameScreen.addActorToSprites(acteur);
			}
		};
		return r;
	}

	public static Runnable getChunkMapDebugEngineRunnable() {
		Runnable r = new Thread("Chunk Debug Engine"){

			@Override
			public void run() {
				Chunk.loadLastFromDebugQueueIfNeeded();
			}
		};
		return r;
	}

	public static Runnable getChunkMapEngineAddToDebugRunnable(final Acteur acteur) {
		Runnable r = new Thread("Chunk Engine Debug Add"){

			@Override
			public void run() {
				GameScreen.addActorToDebug(acteur);
			}
		};
		return r;
	}

	public static Runnable getChunkMapSmoothEngineRunnable() {
		Runnable r = new Thread("Chunk Smooth Engine"){

			@Override
			public void run() {
				Chunk.loadLastFromSmoothEngineQueueIfNeeded();
			}
		};
		return r;
	}

	public static Runnable getChunkMapEngineAddToSmoothRunnable(final Acteur acteur) {
		Runnable r = new Thread("Chunk Engine Smooth Add"){

			@Override
			public void run() {
				GameScreen.addActorToSmooth(acteur);
			}
		};
		return r;
	}

	public static Runnable getChunkMapCleanEngineRunnable() {
		Runnable r = new Thread("Chunk Engine Clean"){

			@Override
			public void run() {
				GameScreen.cleanMap();
			}
		};
		return r;
	}

	public static Runnable getMapCleanerRunnable(final Place place) {
		Runnable r = new Thread("Map Clean"){

			@Override
			public void run() {
				GameScreen.clearStages();
				ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(place));
			}
		};
		return r;
	}

	protected static Runnable getChunkCreatorRunnable(final Place place) {
		Runnable r = new Thread("Chunk Create"){

			@Override
			public void run() {
				GameScreen.createChunk(place);
			}
		};
		return r;
	}

	public static Runnable getChunkMapSmoothQueueRunnable() {
		Runnable r = new Thread("Chunk Smooth Engine"){

			@Override
			public void run() {
				Chunk.loadLastFromSmoothQueueIfNeeded();
			}
		};
		return r;
	}

	public static Runnable getChunkMapSmoothQueueAddToSmoothRunnable(final Smooth to_load) {
		Runnable r = new Thread("Chunk Engine Smooth Add"){

			@Override
			public void run() {
				Chunk.addSmoothToSmoothEngine(to_load);
			}
		};
		return r;
	}

	public static Runnable getExecCommandRunnable(final int nb) {
		Runnable r = new Thread("Exec Command"){

			@Override
			public void run() {
				IdEditMenu.execNormalCommand(nb);
				IdEditMenu.exit();
			}
		};
		return r;
	}

	public static Runnable getPrintLineRunnable(final String line) {
		Runnable r = new Thread("Print"){

			@Override
			public void run() {
				IdEditMenu.shiftConsoleTexts();
				IdEditMenu.getConsoleTexts().get(0).setText(line);
			}
		};
		return r;

	}
}
