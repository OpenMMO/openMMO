package t4cPlugin;

import java.io.File;
import java.util.ArrayList;

public class FileLister {
	//TODO classe utilitaire statique
	private static ArrayList<File> result = new ArrayList<File>();
	
	public FileLister(){}
	
	public ArrayList<File> lister(File repertoire, String extension){
		purge();
		File[] list = repertoire.listFiles();
		for(int i=0;i<list.length;i++){
			if (list[i].isDirectory()){
				recurse(list[i] , extension);
			}else{
				if(list[i].getName().endsWith(extension)){
					result.add(list[i]);
					//System.err.println("Trouvé : "+list[i].getName());
				} 
			}
		}
		return result;
	}
	
	public ArrayList<File> recurse(File repertoire, String extension){
		File[] list = repertoire.listFiles();
		for(int i=0;i<list.length;i++){
			if (list[i].isDirectory()){
				recurse(list[i] , extension);
			}else{
				if(list[i].getName().endsWith(extension)){
					result.add(list[i]);
					//System.err.println("Trouvé : "+list[i].getName());
				} 
			}
		}
		return result;
	}
	
	public ArrayList<File> listerDir(File repertoire){ 
		purge();
		File[] list = repertoire.listFiles();
		for(int i=0;i<list.length;i++){
			if (list[i].isDirectory()){
				recurseDir(list[i]);
				result.add(list[i]);
			}
		}
		return result;
	}

	public ArrayList<File> recurseDir(File repertoire){ 
		File[] list = repertoire.listFiles();
		for(int i=0;i<list.length;i++){
			if (list[i].isDirectory()){
				recurseDir(list[i]);
				result.add(list[i]);
			}
		}
		return result;
	}
	
	private void purge(){
		result.clear();
	}
}
