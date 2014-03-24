package t4cPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class AssetsLoader {
	private static ArrayList<File> sprites = new ArrayList<File>();
	public static ArrayList<File> spritlas = new ArrayList<File>();
	private static ArrayList<File> tuiles = new ArrayList<File>();
	public static ArrayList<File> tuilas = new ArrayList<File>();
	public static HashMap<String, TextureAtlas> tile_atlases = new HashMap<String, TextureAtlas>();
	public static HashMap<String, TextureAtlas> sprite_atlases = new HashMap<String, TextureAtlas>();
	
	private static boolean processed = false;
	public static boolean loadsols = false;
	public static boolean loadsprites = false;
	
	private static int loaded = 0;



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
		sprites.addAll(explorer.listerDir(new File("data"+File.separator+"sprites"+File.separator+"sprites")));
		Iterator<File> iter_sprites = sprites.iterator();
		String last ="";
		while (iter_sprites.hasNext()){
			processed = false;
			final File f = iter_sprites.next();
			final File at = new File("data/atlas/sprites/"+f.getName()+".atlas");
			Params.STATUS = "Pack Sprites : "+at.getName();
			final Settings setting = settings;
			if (!f.getName().equals(last) & f.isDirectory() & !at.exists()){
				Thread p = new Thread("SPRITEPACKER"){
					public void run(){
						Params.STATUS = "Pack Sprite : "+f.getName();
						TexturePacker.processIfModified(setting, f.getPath(), "data/atlas/sprites/", f.getName());
						processed = true;
					}
				};
				p.start();
				while(!processed){
					try{
						Thread.sleep(1000);
					}catch(InterruptedException e){
						e.printStackTrace();
						System.exit(1);
					}
				}
				last = f.getName();
			}
		}
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
		tuiles.addAll(explorer.listerDir(new File("data"+File.separator+"sprites"+File.separator+"tuiles")));
		Iterator<File> iter_tuiles = tuiles.iterator();
		String last ="";
		while (iter_tuiles.hasNext()){
			processed = false;
			final File f = iter_tuiles.next();
			//System.err.println(f.getPath()+" "+f.getName());
			final Settings setting = settings;
			final File at = new File("data/atlas/tuiles/"+f.getName()+".atlas");
			Params.STATUS = "Pack Tuiles : "+at.getName();
			if (!f.getName().equals(last) & f.isDirectory() & !at.exists()){
				Thread p = new Thread("TUILEPACKER"){
					public void run(){
						Params.STATUS = "Pack Tuile : "+f.getName();
						TexturePacker.process(setting, f.getPath(), "data/atlas/tuiles/", f.getName());
						processed = true;
					}
				};
				p.start();
				while(!processed){
					try{
						Thread.sleep(1000);
					}catch(InterruptedException e){
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
			last = f.getName();
		}
	}
	
	public static void loadSprites(){
		System.out.println("LoadSprites");
		FileLister explorer = new FileLister();
		spritlas.addAll(explorer.lister(new File("data"+File.separator+"atlas"+File.separator+"sprites"), ".atlas"));
		Iterator<File> iter_spritlas = spritlas.iterator();
		while(iter_spritlas.hasNext()){
			final String name = iter_spritlas.next().getName();
			Gdx.app.postRunnable(new Runnable(){
				public void run(){
					String nom = name.substring(0, name.length()-6);
					TextureAtlas atlas = new TextureAtlas("data/atlas/sprites/"+nom+".atlas");
					sprite_atlases.put(nom , atlas);
				}
			});		
			while (loaded != sprite_atlases.size()){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			loaded++;
			//System.out.println("Info : Sprites chargés : "+AssetsLoader.sprite_atlases.size()+"/"+AssetsLoader.spritlas.size());
			Params.STATUS = "Sprites chargés : "+AssetsLoader.sprite_atlases.size()+"/"+AssetsLoader.spritlas.size();
		}
//		System.out.println("LoadSprites OK");
		loadsprites = true;

	}
	
	public static TextureAtlas load(final String name){
		System.out.println("Loading Sprite Atlas : " +name);
		Gdx.app.postRunnable(new Runnable(){
			public void run(){
				TextureAtlas atlas = new TextureAtlas("data/atlas/sprites/"+name+".atlas");
				sprite_atlases.put(name , atlas);
				loaded++;
			}
		});		
		while (sprite_atlases.get(name) == null){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		System.out.println("Sprite Atlas : " +name+" loaded.");
		return sprite_atlases.get(name);
	}
	
	public static void loadSols() {
		int loaded = 1;
		System.out.println("LoadSols");
		FileLister explorer = new FileLister();
		tuilas.addAll(explorer.lister(new File("data"+File.separator+"atlas"+File.separator+"tuiles"), ".atlas"));
		Iterator<File> iter_tuilas = tuilas.iterator();
		while(iter_tuilas.hasNext()){
			final String name = iter_tuilas.next().getName();
			Gdx.app.postRunnable(new Runnable(){
				public void run(){
					String nom = name.substring(0, name.length()-6);
					TextureAtlas atlas = new TextureAtlas("data/atlas/tuiles/"+nom+".atlas");
					tile_atlases.put(nom , atlas);
				}
			});	
			while (loaded != tile_atlases.size()){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			loaded++;
			//System.out.println("Info : Tuiles chargées : "+AssetsLoader.tile_atlases.size()+"/"+AssetsLoader.tuilas.size());
			Params.STATUS = "Tuiles chargées : "+AssetsLoader.tile_atlases.size()+"/"+AssetsLoader.tuilas.size();
		}
//		System.out.println("LoadSols OK");
		loadsols = true;
	}
	
	public static void dispose(){
		Iterator<String> iter_tuiles = tile_atlases.keySet().iterator();
		while(iter_tuiles.hasNext()){
			String s= iter_tuiles.next();
			tile_atlases.get(s).dispose();
		}
		Iterator<String> iter_sprites = sprite_atlases.keySet().iterator();
		while(iter_sprites.hasNext()){
			String s= iter_sprites.next();
			sprite_atlases.get(s).dispose();
		}
	}

	/*public static TextureRegion getTex() {
		// TODO Auto-generated method stub
		return null;
	}*/

	
	/*public static TextureRegion loadSprite(String nom){
		TextureRegion result;
		//result = Params.atlas.findRegion(nom);
		return result;
	}*/
}