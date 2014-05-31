package OpenT4C;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;

/**
 * On vérifie la présence des données utiles, et on créé une liste de données manquantes.
 * En parallèle, on informe l'affichage des résultats.
 * @author synoga
 *
 */
public class DataChecker {

	public static final int IS_IDLE = 42;
	public static final int IS_CHECKING_SOURCE_DATA = 0;
	public static final int IS_CHECKING_ATLAS = 1;
	public static final int CHECK_MAPS = 2;
	
	private static Logger logger = LogManager.getLogger(DataChecker.class.getSimpleName());
	private ScreenManager sm =  null;
	public static HashMap<File,String> sourceData = null;
	
	/**
	 * On va vérifier successivement les différentes données nécessaires au bon fonctionnement du logiciel.
	 * On lie le ScreenManager pour pouvoir informer facilement l'affichage de l'état de la vérification.
	 * @param sm
	 */
	public DataChecker(ScreenManager sm) {
		this.sm = sm;
		DataChecker.sourceData = new HashMap<File,String>();
		populateSourceData();
		setStatus(DataChecker.IS_IDLE);
	}

	/**
	 * On ajoute les fichiers source de T4C dans la liste, il suffira d'en ajouter ici, si jamais on doit en gérer plus.
	 */
	private void populateSourceData() {
		File f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2_cavernmap.map");
		String checksum = "1f1848445f4cb1626f3ede0683388ff4";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2_dungeonmap.map");
		checksum = "4df9dfc9466cca818ca2fd22ec560599";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2_leoworld.map");
		checksum = "9aa2b7f484b47e3d3806e9b8a2590edc";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2_underworld.map");
		checksum = "23186dd6acc47664dff79f6b128eb5a5";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2_worldmap.map");
		checksum = "37bb2f1b8d27fd27d5005443bbdb4cd7";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2colori.dpd");
		checksum = "ccde298d34934385fd9d4483685b4ea6";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2data00.dda");
		checksum = "a234d1758ff5ef8de7a3a0c36cfb33d7";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2data01.dda");
		checksum = "9ccdd21440fdb7946619a41a04abccf8";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2data02.dda");
		checksum = "0b11f08c84af3cfcf562d953b01963fd";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2data03.dda");
		checksum = "714a72aac7867e9b7f240948f66c3723";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2data04.dda");
		checksum = "58fe5f0cf3bdbd9988a78150f1a41bca";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2data25.dda");
		checksum = "7a86ce5a4103be85a8d57f7353a95e6e";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"game_files"+File.separatorChar+"v2datai.did");
		checksum = "9d31f9a3b24ee2bfe0d6c269953a2a28";
		sourceData.put(f,checksum);
		f = new File("data"+File.separatorChar+"id.txt");
		checksum = "aacf25cca611fc80574e7158684a82d9";
		sourceData.put(f,checksum);		
	}
	
	/**
	 * D'abord les données sources, puis les données des atlas, puis les données des cartes.
	 */
	public void runCheck() {
		//sm.switchCheckDataScreen();
		logger.info("Vérification des données source");
		checkSourceData();
		logger.info("Vérification des atlas");
		checkAtlas();
		System.exit(0);
		logger.info("Vérification des cartes");
		checkMap();
	}

	/**
	 * Vérifie la présence des fichiers source T4C et contrôle leur intégrité.
	 */
	private void checkSourceData() {
		
		ArrayList<File> absentFiles = new ArrayList<File>();
		ArrayList<File> badChecksumFiles = new ArrayList<File>();
		
		setStatus(DataChecker.IS_CHECKING_SOURCE_DATA);
		
		Iterator<File> iter_source = sourceData.keySet().iterator();
		while (iter_source.hasNext()){
			File f = iter_source.next();
			if(!f.exists()){
				absentFiles.add(f);
			}else if(!checkMD5(f)){
				badChecksumFiles.add(f);
			}
		}
		
		if (!absentFiles.isEmpty()){
			logger.fatal("Fichiers manquants : " + absentFiles);
			System.exit(1);
		}else{
			logger.info("Fichiers source présents.");
		}
		
		if (!badChecksumFiles.isEmpty()){
			logger.fatal("Mauvais MD5 : " + badChecksumFiles);
			System.exit(1);
		}else{
			logger.info("MD5 OK");
		}
		
		setStatus(DataChecker.IS_IDLE);
	}

	/**
	 * Vérifie le MD5 du fichier f
	 * @param f
	 * @return
	 */
	private boolean checkMD5(File f){
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		} 
        FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
        BufferedInputStream bis = new BufferedInputStream(fis);
        DigestInputStream   dis = new DigestInputStream(bis, md5);

        try {
			while (dis.read() != -1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

        try {
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
        
        String hash = tools.ByteArrayToHexString.print(md5.digest());
        //logger.info("java MD5 : " + hash);
        //logger.info("System MD5 : " + sourceData.get(f));
        if(hash.equalsIgnoreCase(sourceData.get(f))){
        	return true;
        } else {
        	return false;
        }
	}
	
	/**
	 * contrôle la présence des atlas
	 */
	private void checkAtlas() {
		setStatus(DataChecker.IS_CHECKING_ATLAS);
		// TODO Savoir si tous les atlas sont là.
		
		//Si les atlas ne sont pas tous présents
		AtlasFactory.make();
	}

	/**
	 * contrôle la présence des cartes
	 */
	private void checkMap() {
		// TODO Auto-generated method stub
		
	}

	private void setStatus(final int status){
		sm.setStatus(status);
	}
	
}
