package OpenT4C;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.AssetsLoader;
import t4cPlugin.FileLister;
import t4cPlugin.MAP;
import t4cPlugin.utils.FilesPath;
import t4cPlugin.utils.MD5Checker;

/**
 * On vérifie la présence des données utiles, et on créé une liste de données manquantes.
 * On informe aussi l'affichage de là où on en est.
 * @author synoga
 *
 */
public class DataChecker {

	 private static Logger logger = LogManager.getLogger(DataChecker.class.getSimpleName());
	
	/**
	 * D'abord les données sources, puis les données des atlas, puis les données des cartes.
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
	 * Vérifie la présence des fichiers source T4C et contrôle leur intégrité.
	 */
	private static void checkSourceData() {
		UpdateScreenManagerStatus.checkingSourceData();
		List<File> absentFiles = checkAbsentFiles();
		stopIfAbsentFiles(absentFiles);
		List<File> badChecksumFiles = checkChecksumFiles();
		stopIfBadChecksum(badChecksumFiles);
		UpdateScreenManagerStatus.idle();
	}
	
	
	private static List<File> checkAbsentFiles() {
		List<File> result = new ArrayList<File>();
		Iterator<File> iter_source = SourceDataManager.getData().keySet().iterator();
		while (iter_source.hasNext()){
			File f = iter_source.next();
			if(!f.exists()){
				result.add(f);
			}
		}
		return result;
	}

	private static void stopIfAbsentFiles(List<File> absentFiles) {
		if (!absentFiles.isEmpty()){
			logger.fatal("Fichier(s) manquant(s) : "+absentFiles);
			//TODO intégrer ici la possibilité de télécharger ces fichiers à partir d'une liste de mirroirs
			System.exit(1);
		}
	}

	private static List<File> checkChecksumFiles() {
		List<File> result = new ArrayList<File>();
		Iterator<File> iter_source = SourceDataManager.getData().keySet().iterator();
		while (iter_source.hasNext()){
			File f = iter_source.next();
			if(!MD5Checker.check(f,SourceDataManager.getData().get(f))){
				result.add(f);
			}
		}
		return result;
	}

	private static void stopIfBadChecksum(List<File> badChecksumFiles) {
		if (!badChecksumFiles.isEmpty()){
			logger.fatal("Fichier(s) corrompu(s) : "+badChecksumFiles);
			//TODO intégrer ici la possibilité de télécharger ces fichiers à partir d'une liste de mirroirs
			System.exit(1);
		}		
	}
	
	/**
	 * contrôle la présence des atlas
	 */
	private static void checkAtlas() {
		UpdateScreenManagerStatus.checkingAtlas();
		// TODO Trouver mieux pour vérifier la présence des atlas et des sprites.
		int nb_atlas = FileLister.lister(new File(FilesPath.getAtlasSpritePath()), ".atlas").size()+FileLister.lister(new File(FilesPath.getAtlasTuilePath()), ".atlas").size();
		//Si les atlas ne sont pas tous présents
		if (nb_atlas < 620){
			SpriteManager.loadIdsFromFile();
			//si les sprites ne sont pas tous présents
			int nb_sprites = FileLister.lister(new File(FilesPath.getSpriteDirectoryPath()), ".png").size()+FileLister.lister(new File(FilesPath.getTuileDirectoryPath()), ".png").size();
			if(nb_sprites < 68400){
				SpriteManager.decryptDPD();
				SpriteManager.decryptDID();
				SpriteManager.decryptDDA(true);
			}
			AssetsLoader.pack_tuiles();
			AssetsLoader.pack_sprites();
		}
		UpdateScreenManagerStatus.idle();
	}

	/**
	 * On vérifie la présence du fichier sprite_data, s'il est là, on arrête, s'il est pas là, on le créé.
	 */
	private static void createSpriteDataIfAbsent() {
		UpdateScreenManagerStatus.checkingSpriteData();
		SpriteManager.loadIdsFromFile();
		if (!new File(FilesPath.getSpriteDataFilePath()).exists()){
			SpriteData.create();
		}
		UpdateScreenManagerStatus.idle();
	}

	/**
	 * contrôle la présence des cartes. Si elles sont là, on arrête, sinon, on les décrypte.
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
		UpdateScreenManagerStatus.idle();
	}
	
	/**
	 * décrypte les cartes source
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
