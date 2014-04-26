package t4cPlugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Cette classe gère la liste statique de correspondances des ids avec les ressources graphiques
 * Nous disposons d'une liste partielle à compléter en fonction de nos avancées
 * @author synoga
 *
 */
public class IdList {
	private static final Map<Integer,String> idList = new HashMap<Integer,String>(3686);
	
	/**
	 * Charge la liste depuis le fichier texte inital sous la forme "ID :Adresse dans T4C.exe Name: VALEUR".
	 * @param f
	 */
	public static void initListFromFile(File f){
		try{
			BufferedReader buff = new BufferedReader(new FileReader(f));
			 
			try {
				String line;
				while ((line = buff.readLine()) != null) {
					int key = 0;
					String value = "";
					key = Integer.parseInt(line.substring(0, line.indexOf(' ')));
					value = line.substring(line.indexOf("Name: ")+6);
					idList.put(key,value);
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * sauvegarde la liste dans un fichier texte sous la forme "ID>VALEUR".
	 * @param f
	 */
	public static void writeToFile(File f){
		OutputStreamWriter id_file = null;
		try {
			id_file = new OutputStreamWriter(new FileOutputStream(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		Iterator<Integer> it = idList.keySet().iterator();
		while(it.hasNext()){
			int i = it.next();
			try {
				id_file.write(i+">"+idList.get(i));
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		try {
			id_file.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Charge la liste depuis un fichier texte sous la forme "ID>VALEUR".
	 * @param f
	 */
	public static void readFromFile(File f){
		try{
			BufferedReader buff = new BufferedReader(new FileReader(f));
			String line = "";
			while ((line = buff.readLine()) != null) {
				String[] index = line.split("\\>");
				if (index.length != 2) System.exit(1);//On vérifie qu'on a bien le bon nombre d'infos sur chaque ligne
				int key = Integer.parseInt(index[0]);
				String value = index[1];
				idList.put(key,value);
			}
			buff.close();
		}catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}
	
	//TODO Dans cette classe, on peut gérer le smoothing, en vérifiant que chacun des éléments à smoother est connu et mappé
}
