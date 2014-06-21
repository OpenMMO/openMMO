package t4cPlugin.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import opent4c.DataChecker;
import opent4c.SourceDataManager;
import opent4c.SpriteManager;
import opent4c.SpriteUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.FileLister;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public enum LoadingStatus {
	INSTANCE; //Singleton
	
	private static Logger logger = LogManager.getLogger(LoadingStatus.class.getSimpleName());
	//TODO perfo check number of tiles for accordingly sizing lists.
	private final int tilesAtlasMax = 5000;
	//TODO perfo check number of sprites for accordingly sizing lists.
	private final int spritesAtlasMax = 5000;
	
	//TODO perfo Check performance of Collections.synchronizedList
	private List<String> tilesAtlasToPackage = Collections.synchronizedList(new ArrayList<String>(tilesAtlasMax));
	private List<String> tilesAtlasPackaged = Collections.synchronizedList(new ArrayList<String>(tilesAtlasMax));
	//TODO Is the atlas number files not the same than tilesAtlasPackaged?
	private int nbTilesAtlas = 0;
	private Map<String, TextureAtlas> tile_atlas = new ConcurrentHashMap<String, TextureAtlas>(tilesAtlasMax);
	
	private List<String> spritesAtlasToPackage = Collections.synchronizedList(new ArrayList<String>(spritesAtlasMax));
	private List<String> spritesAtlasPackaged = Collections.synchronizedList(new ArrayList<String>(spritesAtlasMax));
	//TODO Is the atlas number files not the same than spritesAtlasPackaged?
	private int nbSpritesAtlas = 0;
	private Map<String, TextureAtlas> sprite_atlas = new ConcurrentHashMap<String, TextureAtlas>(spritesAtlasMax);
	
	private List<String> ddaToExtract = Collections.synchronizedList(new ArrayList<String>(SourceDataManager.getDDA().size()));
	private List<String> ddaExtracted = Collections.synchronizedList(new ArrayList<String>(SourceDataManager.getDDA().size()));
	
	private final int waitLoadingTime = 100;
	
	public void addTilesAtlasToPackage(String tileName) {
		tilesAtlasToPackage.add(tileName);
	}
	
	public void addTilesAtlasPackaged(String tileName) {
		addElementToLoadedList(tileName, tilesAtlasPackaged, tilesAtlasToPackage);
	}
	
	public boolean isTilesPackaged() {
		// We consider Tiles loaded if there is none left on toLoad state.
		return tilesAtlasToPackage.isEmpty();
	}
	
	public void addSpritesAtlasToPackage(String spriteName) {
		spritesAtlasToPackage.add(spriteName);
	}
	
	public void addSpritesAtlasPackaged(String spriteName) {
		addElementToLoadedList(spriteName, spritesAtlasPackaged, spritesAtlasToPackage);
	}
	
	public boolean isSpritesPackaged() {
		// We consider Sprite loaded if there is none on toLoad state.
		return spritesAtlasToPackage.isEmpty();
	}
	
	/**
	 * Wait until tiles are all packaged. This will pause the thread.
	 */
	public void waitUntilTilesPackaged() {
		while (!isTilesPackaged()) {
			waitLoaded();
		}
	}
	
	/**
	 * Wait until sprite are all packaged. This will pause the thread.
	 */
	public void waitUntilSpritesPackaged() {
		while (!isSpritesPackaged()) {
			waitLoaded();
		}
	}
	
	
	
	/**
	 * Wait until tiles' atlas are converted in TextureAtlas. This will pause the thread.
	 */
	public void waitUntilTextureAtlasTilesCreated() {
		while (!areTextureAtlasTileCreated()) {
			waitLoaded();
		}
	}
	
	/**
	 * Wait until sprites' atlas are converted in TextureAtlas. This will pause the thread.
	 */
	public void waitUntilTextureAtlasSpritesCreated() {
		while (!areTextureAtlasSpriteCreated()) {
			waitLoaded();
		}
	}
	
	/**
	 * Get the TextureAtlasSprite associated to name. If no TextureAtlas is found, it waits until one is added.
	 * @param name
	 * @return
	 */
	public TextureAtlas waitForTextureAtlasSprite(String name) {
		TextureAtlas ta = getTextureAtlasSprite(name);
		
		while(ta == null)
		{
			waitLoaded();
			ta = getTextureAtlasSprite(name);
		}
		
		return ta;
	}
	
	private void waitLoaded() {
		try {
			Thread.sleep(waitLoadingTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void addElementToLoadedList(String elementName, List<String> listLoaded, List<String> listToLoad)
	{
		//When adding an element to loaded list, we remove it from the toLoad list.
		listLoaded.add(elementName);
		if (listToLoad.remove(elementName)) {
			//TODO log removing successful
		}
		else {
			//TODO Log element was not in the list.
		}
	}

	public int getNbTilesAtlas() {
		return nbTilesAtlas;
	}

	public void setNbTilesAs(int nbTilesAs) {
		this.nbTilesAtlas = nbTilesAs;
	}
	
	public void addTextureAtlasTile(String name, TextureAtlas atlas) {
		tile_atlas.put(name, atlas);
	}
	
	public TextureAtlas getTextureAtlasTile(String name) {
		return tile_atlas.get(name);
	}
	
	public int getNbTextureAtlasTile() {
		return tile_atlas.size();
	}
	
	public boolean areTextureAtlasTileCreated() {
		return tile_atlas.size() == nbTilesAtlas;
	}

	public boolean areDdaFilesProcessed() {
		//TODO ça chie dans la colle au niveau de la création de sprite_data lorsque les sprites sont déjà extraits.
		int nb_sprites = FileLister.lister(new File(FilesPath.getSpriteDirectoryPath()), ".png").size()+FileLister.lister(new File(FilesPath.getTuileDirectoryPath()), ".png").size();
		if(nb_sprites < (SpriteUtils.nb_expected_sprites-DataChecker.delta_ok))return false;
		return true;
	}
	
	public void addTextureAtlasSprite(String name, TextureAtlas atlas) {
		sprite_atlas.put(name, atlas);
	}
	
	public TextureAtlas getTextureAtlasSprite(String name) {
		return sprite_atlas.get(name);
	}
	
	public int getNbTextureAtlasSprite() {
		return sprite_atlas.size();
	}
	
	public boolean areTextureAtlasSpriteCreated() {
		return sprite_atlas.size() == nbSpritesAtlas;
	}

	public int getNbSpritesAtlas() {
		return nbSpritesAtlas;
	}

	public void addOneSpriteAtlas() {
		this.nbSpritesAtlas ++;
	}
	
	public Collection<TextureAtlas> getTexturesAtlasTiles()
	{
		return tile_atlas.values();
	}
	
	public Collection<TextureAtlas> getTexturesAtlasSprites()
	{
		return sprite_atlas.values();
	}

	/**
	 * Adds a dda file to be extracted
	 */
	public void addDDAtoExtract(String ddaFileName) {
		ddaToExtract.add(ddaFileName);
	}
	
	/**
	 * Adds an extracted dda file
	 */
	public void addExtractedDDA(String ddaFileName) {
		addElementToLoadedList(ddaFileName, ddaExtracted, ddaToExtract);
	}
	
	/**
	 * Wait until dda files are processed. This will pause the thread.
	 */
	public void waitUntilDdaFilesProcessed() {
		while (!areDdaFilesProcessed()) {
			waitLoaded();
		}
		SpriteManager.setDda_done(true);
	}
}
