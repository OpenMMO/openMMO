package opent4c;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import opent4c.utils.FileLister;
import opent4c.utils.FilesPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SourceDataManager {

	private static Logger logger = LogManager.getLogger(SourceDataManager.class.getSimpleName());
	private static List<File> source_data= new ArrayList<File>();
	
	/**
	 * Create a HashMap binding T4C files with their MD5 Checksum, to be able to check their integrity later.
	 */
	public static void populate() {
		UpdateDataCheckStatus.setStatus("Recherche des fichiers sources");
		List<File> files = FileLister.lister(new File(FilesPath.getSourceDataDirectoryPath()), ".did");
		Iterator<File> iter_files = files.iterator();
		while(iter_files.hasNext()){
			addSourceFile(iter_files.next());
		}
		files = FileLister.lister(new File(FilesPath.getSourceDataDirectoryPath()), ".dda");
		iter_files = files.iterator();
		while(iter_files.hasNext()){
			addSourceFile(iter_files.next());
		}
		files = FileLister.lister(new File(FilesPath.getSourceDataDirectoryPath()), ".dpd");
		iter_files = files.iterator();
		while(iter_files.hasNext()){
			addSourceFile(iter_files.next());
		}
		files = FileLister.lister(new File(FilesPath.getSourceDataDirectoryPath()), ".map");
		iter_files = files.iterator();
		while(iter_files.hasNext()){
			addSourceFile(iter_files.next());
		}
		files = FileLister.lister(new File(FilesPath.getSourceDataDirectoryPath()), "._");
		iter_files = files.iterator();
		while(iter_files.hasNext()){
			addSourceFile(iter_files.next());
		}
	}
	
	/**
	 * Adds a source file from a filepath and a checksum
	 * @param filepath
	 * @param checksum
	 */
	private static void addSourceFile(File f){
		if (f.exists())source_data.add(f);
	}
	
	/**
	 * @return return the did file (for sprite infos)
	 */
	public static File getDID(){
		File result = null;
		Iterator<File> iter_did = source_data.iterator();
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
		Iterator<File> iter_map = source_data.iterator();
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
	public static List<File> getData() {
		return source_data;
	}

	/**
	 * 
	 * @return the dpd file (for palettes)
	 */
	public static File getDPD(){
		File result = null;
		Iterator<File> iter_dpd = source_data.iterator();
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
		Iterator<File> iter_dda = source_data.iterator();
		while (iter_dda.hasNext()){
			f = iter_dda.next();
			if(f.getName().endsWith(".dda")){
				result.add(f);
			}
		}
		return (ArrayList<File>) result;
	}
}
