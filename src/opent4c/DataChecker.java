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

/**
 * Checks data and decodes what's missing.
 * @author synoga
 *
 */
public class DataChecker {

	 private static Logger logger = LogManager.getLogger(DataChecker.class.getSimpleName());
	 private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	 public final static int delta_ok = 11; //erreur autorisée pour la validation de l'extraction des sprites : 11/68450 = 0,01%
	 //TODO trouver pourquoi il nous manque 11 sprites sur le disque alors que l'écriture est validée par un booléen...
	
	/**
	 * Checks source data, then atlases, and finally maps.
	 */
	public static void runCheck() {
		SourceDataManager.populate();
		FilesPath.init();
		logger.info("Vérification des données source");
		checkSourceData();
		logger.info("Vérification des atlas");
		checkAtlas();
		logger.info("Vérification des données de sprites");
		createSpriteDataIfAbsent();
		logger.info("Vérification des cartes");
		checkMap();
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
			//TODO intégrer ici la possibilité de télécharger ces fichiers à partir d'une liste de mirroirs
			System.exit(1);
		}
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
			//TODO intégrer ici la possibilité de télécharger ces fichiers à partir d'une liste de mirroirs
			System.exit(1);
		}		
	}
	
	/**
	 * Checks the number of atlases and build them if some is missing
	 */
	private static void checkAtlas() {
		UpdateScreenManagerStatus.checkingAtlas();
		// TODO Trouver mieux pour vérifier la présence des atlas.
		int nb_atlas = FileLister.lister(new File(FilesPath.getAtlasSpritePath()), ".atlas").size()+FileLister.lister(new File(FilesPath.getAtlasTuilePath()), ".atlas").size();
		if (nb_atlas < 620){
			buildAtlas();
		}
	}

	/**
	 * Checks the number of present sprites and build them if some are missing.
	 * Then build atlases.
	 */
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
	private static void buildSprites() {
		SpriteManager.decryptDPD();
		SpriteManager.decryptDID();
		SpriteManager.decryptDDA(true);		
	}

	/**
	 * Checks if sprite_data file is present and build it if absent.
	 */
	private static void createSpriteDataIfAbsent() {
		loadingStatus.waitUntilDdaFilesProcessed();
		UpdateScreenManagerStatus.checkingSpriteData();
		SpriteUtils.loadIdsFromFile();
		if (!new File(FilesPath.getSpriteDataFilePath()).exists()){
			SpriteData.create();
		}
	}

	/**
	 * Checks if maps are present and decrypt them if absent.
	 */
	private static void checkMap() {
		UpdateScreenManagerStatus.checkingMaps();
		List<File> mapFiles = SourceDataManager.getMaps();
		List<File> decryptedMaps = new ArrayList<File>();
		decryptedMaps = FileLister.lister(new File(FilesPath.getMapDataDirectoryPath()), ".map.decrypt");
		//TODO trouver mieux que ça. md5 peut-être?
		if (mapFiles.size() != decryptedMaps.size()){
			decryptMaps(mapFiles);
		}
	}
	
	/**
	 * Decrypts maps from source data
	 * @param mapFiles
	 */
	private static void decryptMaps(List<File> mapFiles) {
		File f = null;
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
