package opent4c;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import opent4c.utils.FilesPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SourceDataManager {

	private static Logger logger = LogManager.getLogger(SourceDataManager.class.getSimpleName());
	private static Map<File,String> source_data= new HashMap<File,String>(14);
	
	/**
	 * Create a HashMap binding T4C files with their MD5 Checksum, to be able to check their integrity later.
	 */
	public static void populate() {
		UpdateScreenManagerStatus.setSourceDataStatus("Peuplement données sources");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2_cavernmap.map","1f1848445f4cb1626f3ede0683388ff4");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2_dungeonmap.map","4df9dfc9466cca818ca2fd22ec560599");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2_leoworld.map","9aa2b7f484b47e3d3806e9b8a2590edc");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2_underworld.map","23186dd6acc47664dff79f6b128eb5a5");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2_worldmap.map","37bb2f1b8d27fd27d5005443bbdb4cd7");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2colori.dpd","ccde298d34934385fd9d4483685b4ea6");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2data00.dda","a234d1758ff5ef8de7a3a0c36cfb33d7");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2data01.dda","9ccdd21440fdb7946619a41a04abccf8");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2data02.dda","0b11f08c84af3cfcf562d953b01963fd");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2data03.dda","714a72aac7867e9b7f240948f66c3723");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2data04.dda","58fe5f0cf3bdbd9988a78150f1a41bca");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2data25.dda","7a86ce5a4103be85a8d57f7353a95e6e");
		addSourceFile(FilesPath.getSourceDataDirectoryPath()+"v2datai.did","9d31f9a3b24ee2bfe0d6c269953a2a28");
		//addSourceFile(FilesPath.getIdFilePath(),"aacf25cca611fc80574e7158684a82d9");
	}
	
	/**
	 * Adds a source file from a filepath and a checksum
	 * @param filepath
	 * @param checksum
	 */
	private static void addSourceFile(String filepath, String checksum){
		source_data.put(new File(filepath), checksum);
	}
	
	/**
	 * @return return the did file (for sprite infos)
	 */
	public static File getDID(){
		File result = null;
		Iterator<File> iter_did = source_data.keySet().iterator();
		while (iter_did.hasNext()){
			result = iter_did.next();
			if(result.getName().endsWith(".did")){
				return result;
			}
		}
		logger.warn("Attention, on retourne un fichier .did null");
		return null;
	}

	/**
	 * 
	 * @return a list of map files
	 */
	public static List<File> getMaps() {
		List<File> result = new ArrayList<File>(5);
		File f = null;
		Iterator<File> iter_map = source_data.keySet().iterator();
		while (iter_map.hasNext()){
			f = iter_map.next();
			if(f.getName().endsWith(".map")){
				result.add(f);
			}
		}
		return result;
	}

	/**
	 * 
	 * @return a Map with data files
	 */
	public static Map<File, String> getData() {
		return source_data;
	}

	/**
	 * 
	 * @return the dpd file (for palettes)
	 */
	public static File getDPD(){
		File result = null;
		Iterator<File> iter_dpd = source_data.keySet().iterator();
		while (iter_dpd.hasNext()){
			result = iter_dpd.next();
			if(result.getName().endsWith(".dpd")){
				return result;
			}
		}
		logger.warn("Attention, on retourne un fichier .dpd null");
		return null;
	}

	/**
	 * 
	 * @return a List with dda files (for sprites)
	 */
	public static ArrayList<File> getDDA() {
		List<File> result = new ArrayList<File>();
		File f = null;
		Iterator<File> iter_dda = source_data.keySet().iterator();
		while (iter_dda.hasNext()){
			f = iter_dda.next();
			if(f.getName().endsWith(".dda")){
				result.add(f);
			}
		}
		return (ArrayList<File>) result;
	}
}
