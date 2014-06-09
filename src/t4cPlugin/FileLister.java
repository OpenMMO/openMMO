package t4cPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileLister {

	public static List<File> lister(File repertoire, String extension){
		List<File> result = new ArrayList<File>();
		File[] list = repertoire.listFiles();
		for(int i=0;i<list.length;i++){
			if (list[i].isDirectory()){
				result.addAll(lister(list[i] , extension));
			}else{
				if(list[i].getName().endsWith(extension)){
					result.add(list[i]);
				} 
			}
		}
		return result;
	}
	
	public static List<File> listerDir(File repertoire){ 
		List<File> result = new ArrayList<File>();
		File[] list = repertoire.listFiles();
		for(int i=0;i<list.length;i++){
			if (list[i].isDirectory()){
				result.addAll(listerDir(list[i]));
				result.add(list[i]);
			}
		}
		return result;
	}
}
