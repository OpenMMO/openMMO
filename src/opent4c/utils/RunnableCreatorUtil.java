package opent4c.utils;

import java.io.File;

import opent4c.SpriteData;
import opent4c.SpriteManager;
import opent4c.SpriteUtils;
import opent4c.UpdateScreenManagerStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import screens.MapManager;
import t4cPlugin.Places;

import com.badlogic.gdx.Gdx;
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
				TexturePacker.processIfModified(setting, file.getPath(), FilesPath.getAtlasTuileDirectoryPath(), file.getName());
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
				UpdateScreenManagerStatus.setSpriteDataStatus("Tuiles chargées : "+loadingStatus.getNbTextureAtlasTile()+"/"+loadingStatus.getNbTilesAtlas());
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
		Runnable r = null;
		if(name.equals("Unknown")){
			r = new Runnable(){
				public void run(){
					Gdx.app.postRunnable(new Runnable(){
						public void run(){
							TextureAtlas atlas = new TextureAtlas(FilesPath.getAtlasUnknownFilePath());
							loadingStatus.addTextureAtlasSprite(name, atlas);
						}
					});
				}
			};
		} else if(name.equals("Highlight")){
			r = new Runnable(){
				public void run(){
					Gdx.app.postRunnable(new Runnable(){
						public void run(){
							TextureAtlas atlas = new TextureAtlas(FilesPath.getAtlasHighlightFilePath());
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
								atlas = new TextureAtlas(FilesPath.getAtlasUnknownFilePath());
								//TODO c'est de la bidouille pour que le programme de ne plante pas, mais il faudra dégager ça une fois les atlas correctement empaquetés
								logger.warn("Il semblerait qu'un atlas un soit pas empaqueté : "+name+". On le remplace par Unknown pour le moment.");
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

	public static Runnable getChunkCreatorRunnable(final Places place) {
		Runnable r = new Runnable(){
			public void run() {
				MapManager.teleport(place);
			}
		};
		return r;
	}
	
	public static Runnable getDDAExtractorRunnable(final File f, final boolean doWrite){
		Runnable r = new Runnable(){
			public void run(){
				SpriteUtils.decrypt_dda_file(f,doWrite);
			}
		};
		return r;
	}
	
	public static Runnable getModuloComputerRunnable(final File tileDir){
		Runnable r = new Runnable(){
			public void run(){
				SpriteData.computeModulo(tileDir);
				loadingStatus.addOneComputedModulo();
				UpdateScreenManagerStatus.setSpriteDataStatus("Modulos calculés : "+loadingStatus.getNbComputedModulos()+"/"+loadingStatus.getNbModulosToBeComputed());
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
					System.exit(1);
				}
				MapManager.tileFadeOut();
			}
		};
		return r;
	}
}
