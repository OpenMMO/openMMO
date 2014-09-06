package opent4c;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import opent4c.utils.AssetsLoader;
import opent4c.utils.FileLister;
import opent4c.utils.FilesPath;
import opent4c.utils.ID;
import opent4c.utils.LoadingStatus;
import opent4c.utils.T4CMAP;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;

/**
 * Checks data and decodes what's missing.
 * @author synoga
 *
 */
public class DataChecker {

	private static Logger logger = LogManager.getLogger(DataChecker.class.getSimpleName());
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private static boolean maps_are_ok = false;
	private static boolean atlas_are_ok = false;
	private static boolean sprites_are_ok = false;
	private static boolean pixel_index_is_ok = false;
	public static int nb_expected_sprites = -1;
	public final static int nb_expected_atlas = 649;
	
	/**
	 * Checks everything is ok to be loaded.
	 */
	public static void runCheck() {
		UpdateDataCheckStatus.setStatus("Vérification.");
		SourceDataManager.populate();
		FilesPath.init();
		ID.loadIdFile();
		checkWhatNeedsToBeDone();
		doWhatNeedsToBeDone();
		makeSureEverythingIsOk();
		UpdateDataCheckStatus.setStatus("Vérification terminée.");
	}
	
	/**
	 * Sets booleans to know what has to be done and what has already be done. With that, we only do what's needed.
	 */
	private static void checkWhatNeedsToBeDone() {
		checkPixelIndex();
		checkMaps();
		checkAtlas();
		checkSprites();
	}

	/**
	 * Tests pixel_index presence.
	 */
	private static void checkPixelIndex() {
		File pixel_index = new File(FilesPath.getPixelIndexFilePath());
		if (pixel_index.exists()){
			pixel_index_is_ok = true;
		}
		logger.info("TEST présence pixel_index : "+pixel_index_is_ok);
		UpdateDataCheckStatus.setStatus("TEST présence pixel_index : "+pixel_index_is_ok);
	}

	/**
	 * Tests sprite presence.
	 */
	private static void checkSprites() {
		// TODO Trouver mieux pour vérifier la présence des sprites.
		UpdateDataCheckStatus.setStatus("TEST présence sprites.");
		int nb_sprites = FileLister.lister(new File(FilesPath.getSpriteDataDirectoryPath()), ".png").size();
		if(nb_sprites >= (nb_expected_sprites)){
			sprites_are_ok = true;
		}
		logger.info("TEST présence sprites : "+sprites_are_ok+"=>"+nb_sprites+"/"+(nb_expected_sprites));
		UpdateDataCheckStatus.setStatus("TEST présence sprites : "+sprites_are_ok+"=>"+nb_sprites+"/"+(nb_expected_sprites));
	}

	/**
	 * Tests presence atlas.
	 */
	private static void checkAtlas() {
		int nb_atlas = FileLister.lister(new File(FilesPath.getAtlasSpritePath()), ".atlas").size()+FileLister.lister(new File(FilesPath.getAtlasTuilePath()), ".atlas").size();
		if (nb_atlas >= nb_expected_atlas){
			atlas_are_ok  = true;
		}
		logger.info("TEST présence atlas : "+atlas_are_ok+"=>"+nb_atlas+"/"+nb_expected_atlas);
		UpdateDataCheckStatus.setStatus("TEST présence atlas : "+atlas_are_ok+"=>"+nb_atlas+"/"+nb_expected_atlas);
	}

	/**
	 * Test presence decrypted maps.
	 */
	private static void checkMaps() {
		List<File> mapFiles = SourceDataManager.getMaps();
		List<File> decryptedMaps = new ArrayList<File>();
		decryptedMaps = FileLister.lister(new File(FilesPath.getMapDataDirectoryPath()), ".map.decrypt");
		//TODO trouver mieux que ça. md5 peut-être?
		if (mapFiles.size() == decryptedMaps.size()){
			maps_are_ok  = true;
		}
		logger.info("TEST présence cartes : "+maps_are_ok);
		UpdateDataCheckStatus.setStatus("TEST présence cartes : "+maps_are_ok);
	}

	/**
	 * Sets up data.
	 */
	private static void doWhatNeedsToBeDone() {
		doMaps();
		doSprites();
		doPixelIndex();
		doAtlas();
	}

	/**
	 * Decrypts maps if needed.
	 */
	private static void doMaps() {
		if (!maps_are_ok){
			decryptMaps();
		}		
	}
	

	/**
	 * Decrypts sprites if needed.
	 */
	private static void doSprites() {
		if (!sprites_are_ok){
			SpriteManager.decryptDPD();
			SpriteManager.decryptDID();
			SpriteManager.decryptDDA(true);
		}		
	}
	
	/**
	 * Creates pixel_index if needed.
	 */
	private static void doPixelIndex() {
		if (!pixel_index_is_ok ){
			if(!SpriteManager.isDpd_done())SpriteManager.decryptDPD();
			if(!SpriteManager.isDid_done())SpriteManager.decryptDID();
			if(!SpriteManager.isDda_done())SpriteManager.decryptDDA(false);
			//SpriteData.computeModulos();
			PixelIndex.createPixelIndex();
		}
	}

	/**
	 * Packs atlas if needed.
	 */
	private static void doAtlas() {
		if(!atlas_are_ok){
			AssetsLoader.pack_sprites();
			loadingStatus.waitUntilTextureAtlasSpritesCreated();
			AssetsLoader.pack_tuiles();
			loadingStatus.waitUntilTextureAtlasTilesCreated();
		}
	}

	/**
	 * Wait until everything is done before loading game
	 */
	private static void makeSureEverythingIsOk() {
		logger.info("Attente de la fin des tâches de vérification");
		loadingStatus.waitUntilPixelIndexIsWritten();
		loadingStatus.waitUntilMapsAreDecrypted();
		loadingStatus.waitUntilSpritesPackaged();
		loadingStatus.waitUntilTilesPackaged();
	}

	/**
	 * Stops the program if source files are missing
	 * @param absentFiles
	 */
	private static void stopIfAbsentFiles(List<File> absentFiles) {
		if (!absentFiles.isEmpty()){
			logger.fatal("Fichier(s) manquant(s) : "+absentFiles);
			UpdateDataCheckStatus.setStatus("Fichier(s) manquant(s) : "+absentFiles);
			downloadGameFiles(absentFiles);
			Gdx.app.exit();
		}
	}

	/**
	 * @param files
	 */
	private static void downloadGameFiles(List<File> files) {
		//TODO intégrer ici la possibilité de télécharger ces fichiers à partir d'une liste de mirroirs		
	}

	/**
	 * Decrypts maps from source data
	 * @param mapFiles
	 */
	public static void decryptMaps() {
		File f = null;
		List<File> mapFiles = SourceDataManager.getMaps();
		Iterator<File> iter_maps = mapFiles.iterator();
		while (iter_maps.hasNext()){
			f = iter_maps.next();
			logger.info("Décryptage : "+f.getName());
			UpdateDataCheckStatus.setStatus("Décryptage : "+f.getName());
			if (!new File(FilesPath.getMapFilePath(f.getName())).exists()){
				T4CMAP mapFile = new T4CMAP();
				mapFile.Map_load_block(f, 0x00002000);
			}
		}
		logger.info("Cartes Décryptées");
		UpdateDataCheckStatus.setStatus("Cartes Décryptées");
	}
	
	public static void setNbSprites(int nb){
		nb_expected_sprites = nb;
	}
}
