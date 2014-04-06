package t4cPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import t4cPlugin.utils.FilesPath;
import t4cPlugin.utils.LoadingStatus;
import t4cPlugin.utils.RunnableCreatorUtil;
import t4cPlugin.utils.ThreadsUtil;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public enum AssetsLoader {
	
	INSTANCE;
	
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	
	public static void pack_sprites(){
		Settings settings = new Settings();
		settings.pot = false;
		settings.maxWidth = 1284;
		settings.maxHeight = 772;
		settings.rotation = false;
		settings.ignoreBlankImages = false;
		settings.edgePadding = false;
		//settings.combineSubdirectories = true;
		settings.flattenPaths = true;
		settings.grid = true;
		settings.limitMemory = true;

		FileLister explorer = new FileLister();
		List<File> sprites = new ArrayList<File>();
		sprites.addAll(explorer.listerDir(new File(FilesPath.getSpritePath())));
		Iterator<File> iter_sprites = sprites.iterator();
		String last ="";
		while (iter_sprites.hasNext()){
			final File f = iter_sprites.next();
			final File at = new File(FilesPath.getAtlasSpritesFilePath(f.getName()));
			Params.STATUS = "Pack Sprites : "+at.getName();
			if (!f.getName().equals(last) & f.isDirectory() & !at.exists()){
				loadingStatus.addSpritesAtlasToPackage(f.getName());
				executeSpritePacking(f, settings);
				last = f.getName();
			}
		}
	}
	
	private static void executeSpritePacking(File f, Settings s) {
		Runnable r = RunnableCreatorUtil.getSpritePackerRunnable(f, s);
		ThreadsUtil.executeInThread(r);
	}
	
	public static void pack_tuiles(){
		Settings settings = new Settings();
		settings.pot = false;
		settings.maxWidth = 1152;
		settings.maxHeight = 640;
		settings.rotation = false;
		settings.ignoreBlankImages = false;
		settings.edgePadding = false;
		//settings.combineSubdirectories = true;
		settings.flattenPaths = true;
		settings.grid = true;
		settings.limitMemory = true;

		FileLister explorer = new FileLister();
		List<File> tuiles = new ArrayList<File>();
		tuiles.addAll(explorer.listerDir(new File(FilesPath.getTuilePath())));
		Iterator<File> iter_tuiles = tuiles.iterator();
		String last ="";
		while (iter_tuiles.hasNext()){
			File f = iter_tuiles.next();
			//System.err.println(f.getPath()+" "+f.getName());
			final File at = new File(FilesPath.getAtlasTilesFilePath(f.getName()));
			Params.STATUS = "Pack Tuiles : "+at.getName();
			if (!f.getName().equals(last) & f.isDirectory() & !at.exists()){
				loadingStatus.addTilesAtlasToPackage(f.getName());
				executeTuilesPacking(f, settings);
			}
			last = f.getName();
		}
	}
	
	private static void executeTuilesPacking(File f, Settings settings) {
		Runnable r = RunnableCreatorUtil.getTuilePackerRunnable(f, settings);
		ThreadsUtil.executeInThread(r);
	}
	
	public static void loadSprites(){
		System.out.println("LoadSprites");
		FileLister explorer = new FileLister();
		List<File> spritlas = new ArrayList<File>();
		spritlas.addAll(explorer.lister(new File(FilesPath.getAtlasSpritePath()), ".atlas"));
//		loadingStatus.setNbSpritesAtlas(spritlas.size());
		Iterator<File> iter_spritlas = spritlas.iterator();
		while(iter_spritlas.hasNext()){
			loadingStatus.addOneSpriteAtlas();
			final String name = iter_spritlas.next().getName();
			Gdx.app.postRunnable(RunnableCreatorUtil.getTextureAtlasSpriteCreatorRunnable(name));		
		}
	}
	
	public static TextureAtlas load(final String name){
		System.out.println("Loading Sprite Atlas : " +name);
		loadingStatus.addOneSpriteAtlas();
		Gdx.app.postRunnable(RunnableCreatorUtil.getForceTextureAtlasSpriteCreatorRunnable(name));		
		TextureAtlas ta = loadingStatus.waitForTextureAtlasSprite(name);
		System.out.println("Sprite Atlas : " +name+" loaded.");
		return ta;
	}
	
	public static void loadSols() {
		System.out.println("LoadSols");
		
		//Ensure tiles are packaged before try to use them
		loadingStatus.waitUntilTilesPackaged();
		
		List<File> tuilas = new ArrayList<File>();
		
		FileLister explorer = new FileLister();
		tuilas.addAll(explorer.lister(new File(FilesPath.getAtlasTuilePath()), ".atlas"));
		//Keep the number of tiles' atlas. Will be used to know if all atlas are processed.
		//TODO use the same method than sprite
		loadingStatus.setNbTilesAs(tuilas.size());
		
		Iterator<File> iter_tuilas = tuilas.iterator();
		while(iter_tuilas.hasNext()){
			final String name = iter_tuilas.next().getName();
			Gdx.app.postRunnable(RunnableCreatorUtil.getTextureAtlasTileCreatorRunnable(name));
		}
//		System.out.println("LoadSols OK");
	}
	
	public static void dispose(){
		//TODO ensure all elements are loaded and no thread will update a map during the iteration
		Iterator<TextureAtlas> iter_tuiles = loadingStatus.getTexturesAtlasTiles().iterator();
		while(iter_tuiles.hasNext()){
			iter_tuiles.next().dispose();
		}
		Iterator<TextureAtlas> iter_sprites = loadingStatus.getTexturesAtlasSprites().iterator();
		while(iter_sprites.hasNext()){
			iter_sprites.next().dispose();
		}
	}

}