package t4cPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class FileLister {
	
	private static ArrayList<File> result = new ArrayList<File>();
	
	public FileLister(){}
	
	public ArrayList<File> lister(File repertoire, String extension){ 
		System.out.println("Parcourt du dossier :"+repertoire.getName()+" pour les fichiers d'extension : "+extension);
		File[] list = repertoire.listFiles();
		for(int i=0;i<list.length;i++){
			if (list[i].isDirectory()){
				System.out.println("	- Dossier : "+list[i].getName());
				lister(list[i] , extension);
			}else{
				if(list[i].getName().endsWith(extension)){
					System.out.println("		- "+list[i].getName());
					result.add(list[i]);
				} 
			}
		}
		System.out.println("	- Fin de dossier : "+repertoire.getName());
		return result;
	}
	
	public void purge(){
		print();
		System.out.println("");
		result.clear();
	}
	public void print(){
		System.out.println("");
		System.out.println("Fichiers trouvés :");
		Iterator<File> it = result.iterator();
		while(it.hasNext()){
			System.out.println("	- "+it.next().getName());
		}
	}
}
