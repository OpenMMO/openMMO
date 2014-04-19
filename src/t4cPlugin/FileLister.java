package t4cPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileLister {
	/*TODO cette classe est mal construite. Deux possibilitées
	 * 1. C'est une classe utilitaire statique. Il ne doit donc pas y avoir de variable interne ni de méthode purge.
	 *    Lorsque la récursion est utilisée, les nouvelles valeurs doivent être retournée PUIS ajouter au résultat
	 *    dans la fonction appelante. Il ne faut pas les ajouter au résultat avant de sortir de la récursion.
	 * 2. Ce n'est pas une classe statique, auquel cas le code de lister doit être exécuté à la création de l'objet.
	 *    Il est correct d'ajouter les nouvelles valeurs pendant la récursion. Par contre il ne faut jamais purger
	 *    car pour chaque appel un nouvel objet doit impérativement être créé. L'objet contenant déjà la liste des 
	 *    fichiers, il n'est pas nécessaire de copier cette liste dans une nouvelle, autant garder une référence
	 *    directement sur cet objet. GDXEditor.did devenant un FileLister au lieu d'une arrayList.
	 *    
	 * Les deux approches sont correctes mais sont mutuellement exclusives. On ne doit pas avoir de variables
	 * internes modifiées en fonction des appels aux fonctions dans une classe statique, et il ne faut pas purger 
	 * l'objet pour le réutiliser plusieurs fois en copiant son contenu si on demande de l'instancier. 
	 * 
	 * De plus les niveaux d'acces des fonctions (public/privée) est mal réglée et est actuellement source de bug.
	 * Si je me trompe et appele directement recurse au lieu de lister, je me retrouve avec les résultats de 
	 * l'exécution précédante, ce qui ne devrait pas arriver. Recurse devrait donc être privée.
	*/
	
	private static List<File> result = new ArrayList<File>();
	
	public FileLister(){}
	
	public List<File> lister(File repertoire, String extension){
		purge();
		//TODO copier coller de la fonction recurse. 
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
	
	 
	public List<File> recurse(File repertoire, String extension){
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
	
	public List<File> listerDir(File repertoire){ 
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

	public List<File> recurseDir(File repertoire){ 
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
