package t4cPlugin.utils;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.Places;
import OpenT4C.MapManager;
import OpenT4C.UpdateScreenManagerStatus;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
				TexturePacker.process(setting, file.getPath(), FilesPath.getAtlasTuileDirectoryPath(), file.getName());
				loadingStatus.addTilesAtlasPackaged(file.getName());
			}
		};
		return r;
	}
	
	public static Runnable getSpritePackerRunnable(final File file, final Settings setting) {
		Runnable r = new Runnable() {
			public void run() {
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
				UpdateScreenManagerStatus.setSubStatus("Tuiles chargées : "+loadingStatus.getNbTextureAtlasTile()+"/"+loadingStatus.getNbTilesAtlas());
				logger.info("Tuiles chargées : "+loadingStatus.getNbTextureAtlasTile()+"/"+loadingStatus.getNbTilesAtlas());
			}
		};
		return r;
	}
	
	@Deprecated
	public static Runnable getTextureAtlasSpriteCreatorRunnable(final String name) {
		Runnable r = new Runnable(){
			public void run(){
				String nom = name.substring(0, name.length()-6);
				TextureAtlas atlas = new TextureAtlas(FilesPath.getAtlasSpritesFilePath(nom));
				loadingStatus.addTextureAtlasSprite(nom, atlas);
				logger.info("Sprites chargés : "+loadingStatus.getNbTextureAtlasSprite()+"/"+loadingStatus.getNbSpritesAtlas());
			}
		};
		return r;
	}
	
	public static Runnable getForceTextureAtlasSpriteCreatorRunnable(final String name) {
		Runnable r = new Runnable(){
			public void run(){
				TextureAtlas atlas = null;
				if(name.equals("Unknown")){
					atlas = new TextureAtlas(FilesPath.getAtlasUnknownFilePath());
				}else{
					atlas = new TextureAtlas(FilesPath.getAtlasSpritesFilePath(name));
				}
				loadingStatus.addTextureAtlasSprite(name, atlas);
			}
		};
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

	public static Runnable getChunkCreatorRunnable(final Places place) {
		Runnable r = new Runnable(){
			public void run() {
				MapManager.teleport(place);
			}
		};
		return r;
	}
}
