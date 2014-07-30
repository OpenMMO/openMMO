package opent4c;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import opent4c.utils.AssetsLoader;
import opent4c.utils.FileLister;
import opent4c.utils.FilesPath;
import opent4c.utils.LoadingStatus;
import opent4c.utils.T4CMAP;
import opent4c.utils.MD5Checker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	public final static int nb_expected_sprites = 68450;
	public final static int delta_ok = 11; //erreur autorisée pour la validation de l'extraction des sprites : 11/68450 = 0,01%
	public final static int nb_expected_atlas = 650;
	//TODO trouver pourquoi il nous manque 11 sprites sur le disque alors que l'écriture est validée par un booléen...
	//TODO Je me dis que plusieurs sprites doivent porter le même nom...
	
	/**
	 * Checks source data, then atlases, and finally maps.
	 */
	public static void runCheck() {
		logger.info("Vérification des données source.");
		SourceDataManager.populate();
		FilesPath.init();
		checkSourceData();
		logger.info("Vérification des données calculées.");
		SpriteData.loadIdsFromFile();
		checkWhatNeedsToBeDone();
		doWhatNeedsToBeDone();
		makeSureEverythingIsOk();
		logger.info("Vérification terminée.");
	}

	/**
	 * computes absent data
	 */
	private static void doWhatNeedsToBeDone() {
		doMaps();
		doSprites();
		doPixelIndex();
		doAtlas();
	}

	/**
	 * 
	 */
	private static void doPixelIndex() {
		if (!pixel_index_is_ok ){
			if(!SpriteManager.isDpd_done())SpriteManager.decryptDPD();
			if(!SpriteManager.isDid_done())SpriteManager.decryptDID();
			if(!SpriteManager.isDda_done())SpriteManager.decryptDDA(false);
			SpriteData.computeModulos();
			SpriteData.createPixelIndex();
		}
	}

	/**
	 * Packs atlas if needed
	 */
	private static void doAtlas() {
		if(!atlas_are_ok){
			AssetsLoader.pack_sprites();
			AssetsLoader.pack_tuiles();
		}
	}

	/**
	 * Decrypts sprites if needed
	 */
	private static void doSprites() {
		if (!sprites_are_ok){
			SpriteManager.decryptDPD();
			SpriteManager.decryptDID();
			SpriteManager.decryptDDA(true);
		}		
	}

	/**
	 * Decrypts maps if needed
	 */
	private static void doMaps() {
		if (!maps_are_ok){
			decryptMaps();
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
	 * sets booleans to know what has to be done and what has already be done. with that, we only do what's needed.
	 */
	private static void checkWhatNeedsToBeDone() {
		checkPixelIndex();
		checkMaps();
		checkAtlas();
		checkSprites();
	}

	/**
	 * 
	 */
	private static void checkPixelIndex() {
		File pixel_index = new File(FilesPath.getPixelIndexFilePath());
		if (pixel_index.exists()){
			pixel_index_is_ok = true;
		}
		logger.info("TEST présence pixel_index : "+pixel_index_is_ok);		
	}

	/**
	 * Tests sprite presence
	 */
	private static void checkSprites() {
		// TODO Trouver mieux pour vérifier la présence des sprites.
		int nb_sprites = FileLister.lister(new File(FilesPath.getSpriteDataDirectoryPath()), ".png").size();
		if(nb_sprites >= (nb_expected_sprites-delta_ok)){
			sprites_are_ok = true;
		}
		logger.info("TEST présence sprites : "+sprites_are_ok);
	}

	/**
	 * Tests presence atlas
	 */
	private static void checkAtlas() {
		int nb_atlas = FileLister.lister(new File(FilesPath.getAtlasSpritePath()), ".atlas").size()+FileLister.lister(new File(FilesPath.getAtlasTuilePath()), ".atlas").size();
		if (nb_atlas >= nb_expected_atlas){
			atlas_are_ok  = true;
		}
		logger.info("TEST présence atlas : "+atlas_are_ok);
	}

	/**
	 * Test presence decrypted maps
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
	}

	/**
	 * Checks the source data (presence and integrity)
	 */
	private static void checkSourceData() {
		List<File> absentFiles = checkAbsentFiles();
		stopIfAbsentFiles(absentFiles);
		List<File> badChecksumFiles = checkChecksumFiles();
		stopIfBadChecksum(badChecksumFiles);
		UpdateDataCheckStatus.setSourceDataStatus("OK");
		UpdateDataCheckStatus.setStatus("OK");
	}
	
	/**
	 * Checks if source files are absent
	 */
	private static List<File> checkAbsentFiles() {
		List<File> result = new ArrayList<File>();
		Iterator<File> iter_source = SourceDataManager.getData().keySet().iterator();
		while (iter_source.hasNext()){
			File f = iter_source.next();
			UpdateDataCheckStatus.setSourceDataStatus("Vérification présence : "+f.getName());
			UpdateDataCheckStatus.setStatus("Vérification présence : "+f.getName());
			if(!f.exists()){
				result.add(f);
			}
		}
		return result;
	}

	/**
	 * Stops the program if source files are missing
	 * @param absentFiles
	 */
	private static void stopIfAbsentFiles(List<File> absentFiles) {
		if (!absentFiles.isEmpty()){
			logger.fatal("Fichier(s) manquant(s) : "+absentFiles);
			downloadGameFiles(absentFiles);
			System.exit(1);
		}
	}

	/**
	 * @param files
	 */
	private static void downloadGameFiles(List<File> files) {
		//TODO intégrer ici la possibilité de télécharger ces fichiers à partir d'une liste de mirroirs		
	}

	/**
	 * Checks source file integrity
	 * @return a file list which did not pass the checksum test
	 */
	private static List<File> checkChecksumFiles() {
		List<File> result = new ArrayList<File>();
		Iterator<File> iter_source = SourceDataManager.getData().keySet().iterator();
		while (iter_source.hasNext()){
			File f = iter_source.next();
			UpdateDataCheckStatus.setSourceDataStatus("Vérification MD5 : "+f.getName());
			UpdateDataCheckStatus.setStatus("Vérification MD5 : "+f.getName());
			if(!MD5Checker.check(f,SourceDataManager.getData().get(f))){
				result.add(f);
			}
		}
		return result;
	}

	/**
	 * Stops the program if source files are corrupted
	 * @param badChecksumFiles
	 */
	private static void stopIfBadChecksum(List<File> badChecksumFiles) {
		if (!badChecksumFiles.isEmpty()){
			logger.fatal("Fichier(s) corrompu(s) : "+badChecksumFiles);
			downloadGameFiles(badChecksumFiles);
			System.exit(1);
		}		
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
			if (!new File(FilesPath.getMapFilePath(f.getName())).exists()){
				T4CMAP mapFile = new T4CMAP();
				mapFile.Map_load_block(f, 0x00002000);
			}
		}
		logger.info("Cartes Décryptées");
	}		
}
