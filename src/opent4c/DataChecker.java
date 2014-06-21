package opent4c;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;

import t4cPlugin.AssetsLoader;
import t4cPlugin.FileLister;
import t4cPlugin.MAP;
import t4cPlugin.SpriteName;
import t4cPlugin.utils.FilesPath;
import t4cPlugin.utils.LoadingStatus;
import t4cPlugin.utils.MD5Checker;
import t4cPlugin.utils.RunnableCreatorUtil;
import t4cPlugin.utils.ThreadsUtil;

/**
 * Checks data and decodes what's missing.
 * @author synoga
 *
 */
public class DataChecker {

	private static Logger logger = LogManager.getLogger(DataChecker.class.getSimpleName());
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	private static boolean sprite_data_is_ok = false;
	private static boolean maps_are_ok = false;
	private static boolean atlas_are_ok = false;
	private static boolean sprites_are_ok = false;
	public final static int delta_ok = 11; //erreur autorisée pour la validation de l'extraction des sprites : 11/68450 = 0,01%
	public final static int nb_expected_atlas = 641;
	//TODO trouver pourquoi il nous manque 11 sprites sur le disque alors que l'écriture est validée par un booléen...
	
	/**
	 * Checks source data, then atlases, and finally maps.
	 */
	public static void runCheck() {
		SourceDataManager.populate();
		FilesPath.init();
		logger.info("Vérification des données source");
		checkSourceData();
		SpriteUtils.loadIdsFromFile();
		checkWhatNeedsToBeDone();
		//checkAtlas();
		//createSpriteDataIfAbsent();
		//checkMap();
		makeSureEverythingIsOk();
		Main.charger();
	}

	/**
	 * 
	 */
	private static void makeSureEverythingIsOk() {
		loadingStatus.waitUntilSpriteDataIsWritten();
		loadingStatus.waitUntilMapsAreDecrypted();
		loadingStatus.waitUntilSpritesPackaged();
		loadingStatus.waitUntilTilesPackaged();
	}

	/**
	 * sets booleans to know what has to be done and what has already be done. with that, we only do what's needed.
	 */
	private static void checkWhatNeedsToBeDone() {
		//présence sprite_data non vide
		File sprite_data = new File(FilesPath.getSpriteDataFilePath());
		if (sprite_data.exists() && sprite_data.length() != 0){
			sprite_data_is_ok = true;
		}
		logger.info("TEST présence sprite_data : "+sprite_data_is_ok);
		//présence cartes décryptées
		List<File> mapFiles = SourceDataManager.getMaps();
		List<File> decryptedMaps = new ArrayList<File>();
		decryptedMaps = FileLister.lister(new File(FilesPath.getMapDataDirectoryPath()), ".map.decrypt");
		//TODO trouver mieux que ça. md5 peut-être?
		if (mapFiles.size() == decryptedMaps.size()){
			maps_are_ok  = true;
		}
		logger.info("TEST présence cartes : "+maps_are_ok);

		//présence des atlas
		int nb_atlas = FileLister.lister(new File(FilesPath.getAtlasSpritePath()), ".atlas").size()+FileLister.lister(new File(FilesPath.getAtlasTuilePath()), ".atlas").size();
		if (nb_atlas >= nb_expected_atlas){
			atlas_are_ok  = true;
		}
		logger.info("TEST présence atlas : "+atlas_are_ok);

		//présence des sprites
		// TODO Trouver mieux pour vérifier la présence des sprites.
		int nb_sprites = FileLister.lister(new File(FilesPath.getSpriteDirectoryPath()), ".png").size()+FileLister.lister(new File(FilesPath.getTuileDirectoryPath()), ".png").size();
		if(nb_sprites >= (SpriteUtils.nb_expected_sprites-delta_ok)){
			sprites_are_ok = true;
		}
		logger.info("TEST présence sprites : "+sprites_are_ok);

		if (!maps_are_ok){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getMapExtractorRunnable());
		}
		
		if (!sprites_are_ok){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getSpriteExtractorRunnable(true));
		}
		
		if (!sprite_data_is_ok){
			if(sprites_are_ok)ThreadsUtil.executeInThread(RunnableCreatorUtil.getSpriteExtractorRunnable(false));
			loadingStatus.waitUntilDdaFilesProcessed();
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getSpriteDataCreatorRunnable());
		}
		
		if(!atlas_are_ok){
			if (!sprites_are_ok) loadingStatus.waitUntilDdaFilesProcessed();
			AssetsLoader.pack_sprites();
			AssetsLoader.pack_tuiles();
		}
	}

	/**
	 * Checks the source data (presence and integrity)
	 */
	private static void checkSourceData() {
		UpdateScreenManagerStatus.checkingSourceData();
		List<File> absentFiles = checkAbsentFiles();
		stopIfAbsentFiles(absentFiles);
		List<File> badChecksumFiles = checkChecksumFiles();
		stopIfBadChecksum(badChecksumFiles);
	}
	
	/**
	 * Checks if source files are absent
	 */
	private static List<File> checkAbsentFiles() {
		List<File> result = new ArrayList<File>();
		Iterator<File> iter_source = SourceDataManager.getData().keySet().iterator();
		while (iter_source.hasNext()){
			File f = iter_source.next();
			UpdateScreenManagerStatus.setSubStatus("Vérification présence : "+f.getName());
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
			UpdateScreenManagerStatus.setSubStatus("Vérification MD5 : "+f.getName());
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
	 * Checks the number of atlases and build them if some is missing
	 */
	@Deprecated
	private static void checkAtlas() {
		UpdateScreenManagerStatus.checkingAtlas();
		// TODO Trouver mieux pour vérifier la présence des atlas.
		int nb_atlas = FileLister.lister(new File(FilesPath.getAtlasSpritePath()), ".atlas").size()+FileLister.lister(new File(FilesPath.getAtlasTuilePath()), ".atlas").size();
		if (nb_atlas < nb_expected_atlas){
			buildAtlas();
		}
	}

	/**
	 * Checks the number of present sprites and build them if some are missing.
	 * Then build atlases.
	 */
	@Deprecated
	private static void buildAtlas() {
		SpriteUtils.loadIdsFromFile();
		// TODO Trouver mieux pour vérifier la présence des sprites.
		int nb_sprites = FileLister.lister(new File(FilesPath.getSpriteDirectoryPath()), ".png").size()+FileLister.lister(new File(FilesPath.getTuileDirectoryPath()), ".png").size();
		if(nb_sprites < (SpriteUtils.nb_expected_sprites-delta_ok)){
			buildSprites();
			loadingStatus.waitUntilDdaFilesProcessed();
			nb_sprites = FileLister.lister(new File(FilesPath.getSpriteDirectoryPath()), ".png").size()+FileLister.lister(new File(FilesPath.getTuileDirectoryPath()), ".png").size();
			if(nb_sprites < (SpriteUtils.nb_expected_sprites-delta_ok)){
				printBuildSpriteReport();
				Gdx.app.exit();
				System.exit(1);
			}
		}
		//TODO intégrer les offsets de sprites aux atlas plutot que de la appliquer manuellement à la volée
		AssetsLoader.pack_tuiles();
		AssetsLoader.pack_sprites();
	}

	/**
	 * 
	 */
	private static void printBuildSpriteReport() {
		int nb_type_1 = 0;
		int nb_type_2 = 0;
		int nb_type_3 = 0;
		int nb_type_9 = 0;
		Iterator<SpriteName> iter_sprites = SpriteManager.getSprites().keySet().iterator();
		while (iter_sprites.hasNext()){
			SpriteName spn = iter_sprites.next();
			Sprite sp = SpriteManager.getSprites().get(spn);
			if (sp.getType() != null){
				switch(sp.getType().getValue()){
					case 1 : nb_type_1++; break;
					case 2 : nb_type_2++; break;
					case 3 : nb_type_3++; break;
					case 9 : nb_type_9++; break;
				}
			}
		}
		int nb_sprites = FileLister.lister(new File(FilesPath.getSpriteDirectoryPath()), ".png").size()+FileLister.lister(new File(FilesPath.getTuileDirectoryPath()), ".png").size();
		logger.info("Rapport d'extraction de sprites.");
		logger.info("Nombre de sprites trouvés sur le disque : "+nb_sprites);
		logger.info("nombre de sprites de type 1 : "+nb_type_1);
		logger.info("nombre de sprites de type 2 : "+nb_type_2);
		logger.info("nombre de sprites de type 3 : "+nb_type_3);
		logger.info("nombre de sprites de type 9 : "+nb_type_9);
		logger.info("nombre de sprites total sans les void: "+(nb_type_1+nb_type_2+nb_type_9));
	}

	/**
	 * Builds Sprites from source data.
	 */
	@Deprecated
	private static void buildSprites() {
		SpriteManager.decryptDPD();
		SpriteManager.decryptDID();
		SpriteManager.decryptDDA(true);		
	}

	/**
	 * Checks if sprite_data file is present and build it if absent.
	 */
	@Deprecated
	private static void createSpriteDataIfAbsent() {
		UpdateScreenManagerStatus.checkingSpriteData();
		if (!new File(FilesPath.getSpriteDataFilePath()).exists()){
			loadingStatus.waitUntilDdaFilesProcessed();
			SpriteData.create();
		}
	}

	/**
	 * Checks if maps are present and decrypt them if absent.
	 */
	@Deprecated
	private static void checkMap() {
		UpdateScreenManagerStatus.checkingMaps();
		List<File> mapFiles = SourceDataManager.getMaps();
		List<File> decryptedMaps = new ArrayList<File>();
		decryptedMaps = FileLister.lister(new File(FilesPath.getMapDataDirectoryPath()), ".map.decrypt");
		//TODO trouver mieux que ça. md5 peut-être?
		if (mapFiles.size() != decryptedMaps.size()){
			decryptMaps();
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
				MAP mapFile = new MAP();
				mapFile.Map_load_block(f, 0x00002000);
			}
		}
		logger.info("Cartes Décryptées");
	}		
}
