package t4cPlugin;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tools.DataInputManager;
import tools.OSValidator;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class GDXEditor extends Game{
	
	private static Logger logger = LogManager.getLogger(GDXEditor.class.getSimpleName());
	
	public static boolean decrypt = false, repack_tuiles = false, format = false, mapData = false, repack_sprites = false, map_loaded = false;
	private static ArrayList<File> did = new ArrayList<File>();
	private static ArrayList<File> map = new ArrayList<File>();
	private static ArrayList<File> dda = new ArrayList<File>();
	private static ArrayList<File> dpd = new ArrayList<File>();
	private static ArrayList<File> decrypted = new ArrayList<File>();
	public static ScreenManager sm = null;
	private static ByteBuffer map_buffer;
	public static MapFile mapf;
		
	private static List<String> mapsName = Arrays.asList("v2_cavernmap.map", "v2_dungeonmap.map", "v2_leoworld.map", "v2_underworld.map", "v2_worldmap.map");
	
		/**
		 * Pour améliorer le décryptage, je pense revoir les fonction 'à l'envers'
		 * Actuellement, on décrypte tout, puis on regroupe tout, puis on réécrit tout.
		 * Maintenant je sais ce qu'on a à écrire, donc je pense mettre en place une
		 * fonction qui écrive les atlas directement depuis les fichiers dda et did,
		 * puis une fonction qui écrive les cartes en clair directement depuis les cartes
		 * cryptées.
		 * 
		 * @param args
		 */
		public static void main(String[] args) {
			logger.info("Démarrage.");
			//paramétrage de l'appli en fonction de l'os.
			OSValidator.detect();	
			Params.IDS = "."+File.separator+"data"+File.separator+"id.txt";
			Params.SPRITES = "."+File.separator+"data"+File.separator+"sprites"+File.separator;
			
			LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
			cfg.title = "T4C Map Viewer 0.1a (by Syno)";
			cfg.useGL20 = true;
			cfg.width = 1280;
			cfg.height = 720;
			sm = new ScreenManager("v2_worldmap");
			new LwjglApplication(sm, cfg);
						
			checkData();
			
		//	forceReload(true,true,true,true,true);
			
			if (decrypt)decrypt();
			if (format)format();
			if (mapData)writeMapData();
			if (repack_tuiles)repacktuiles();
			if (repack_sprites)repacksprites();
			
			sm.initMap();
		}
		
		/**
		 * Fonction de debug. Sert a forcer le reload des elements, même s'ils sont déjà présents.
		 * TODO il faudrait ajouter un paramètre pour pouvoir forcer le reload en production, en cas de données corrompues.
		 * @param fDecrypt
		 * @param fFormat
		 * @param fMapData
		 * @param fRepackTuiles
		 * @param fRepackSprites
		 */
		private static void forceReload(boolean fDecrypt, boolean fFormat, boolean fMapData, boolean fRepackTuiles, boolean fRepackSprites) {
			decrypt = fDecrypt;
			format = fFormat;
			mapData = fMapData;
			repack_tuiles = fRepackTuiles;
			repack_sprites = fRepackSprites;
		}

		private static void checkData() {
			//recherche de données
			logger.info("Vérification des données.");
			Params.STATUS = "Chargement des fichiers de donnée.";
			FileLister explorer = new FileLister();
			File game_files = new File("."+File.separator+"data"+File.separator+"game_files"+File.separator);
			dpd.addAll(explorer.lister(game_files, ".dpd"));
			did.addAll(explorer.lister(game_files, ".did"));
			map.addAll(explorer.lister(game_files, ".map"));
			dda.addAll(explorer.lister(game_files, ".dda"));
			decrypted.addAll(explorer.lister(new File("."+File.separator+"data"+File.separator), ".decrypt"));
			
			if (!new File (Params.SPRITES+"sprites_drawn").exists()) Params.draw_sprites = true;
			
			if (!new File (Params.SPRITES+"sprite_data").exists()){
				logger.info("Il est nécessaire de formater et décrypter.");
				format = true;
				decrypt = true;
			}
			
			mapData = checkMapExists();
			
			repack_tuiles = checkRepack("tuiles");
			repack_sprites = checkRepack("sprites");
			
		}
		
		/**
		 * Vérifie que les maps ont déjà été écrites en clair.
		 * @param fileName
		 * @return true si toutes les cartes existent, false si au moins une n'existe pas.
		 */
		private static Boolean checkMapExists() {
			String baseFileName = "."+File.separator+"data"+File.separator;
			String decrypt = ".decrypt";
			String decryptBin = ".decrypt.bin";
			Boolean mapNotFound = false;
			Iterator<String> mapsNameIt = mapsName.iterator();
			
			while (mapsNameIt.hasNext() && !mapNotFound) {
				String mapName = mapsNameIt.next();
				//Vérification de l'existance de la version .decrypt et .decrypt.bin
				mapNotFound = !checkFileExists(baseFileName + mapName + decrypt) ||
						!checkFileExists(baseFileName + mapName + decryptBin);
			}
			logger.info("Cartes décryptées : " + !mapNotFound);
			return mapNotFound;
		}
		
		/**
		 * Il faut repack les elements choisis si le nombre dans l'atlas et le repertoire ne sont pas identiques ou
		 * si un répertoire est manquant.
		 * @param explorer
		 * @param repackElementsName
		 * @return
		 */
		private static Boolean checkRepack(String repackElementsName)
		{
			try {
				int nb_dir = getNbElementsDir(repackElementsName);
				int nb_atlas = getNbElementsAtlas(repackElementsName);
				
				return nb_dir != nb_atlas; 
			} catch (FileNotFoundException e) {
				//Le repertoire n'existant pas, il faut repack
				logger.info("Il faut repack les " + repackElementsName);
				return true;
			}
		}
		
		/**
		 * 
		 * @param elementsName
		 * @return le nombre d'éléments du type voulu dans le répertoire de sprite
		 * @throws FileNotFoundException si le répertoire n'existe pas
		 */
		private static int getNbElementsDir(String elementsName) throws FileNotFoundException
		{
			File dir = new File("data"+File.separator+"sprites"+File.separator+elementsName);
			
			if(dir.exists()) {
				return (new FileLister()).listerDir(dir).size();
			}
			else {
				throw new FileNotFoundException();
			}
		}
		
		/**
		 * 
		 * @param elementsName
		 * @return le nombre d'éléments du type voulu dans le répertoire d'atlas
		 * @throws FileNotFoundException
		 */
		private static int getNbElementsAtlas(String elementsName) throws FileNotFoundException
		{
			File atlas = new File("."+File.separator+"data"+File.separator+"atlas"+File.separator
					+elementsName+File.separator);
			
			if(atlas.exists()) {
				return (new FileLister()).lister(atlas, ".atlas").size();
			}
			else {
				throw new FileNotFoundException();
			}
		}
		
		private static Boolean checkFileExists(String fileName) {
			File file = new File(fileName);
			return file.exists();
		}

		/**
		 * Décrypte les fichiers T4C. D'abord on extrait les palettes des fichiers .dpd
		 * ensuite on extrait les informations sur les ressources graphiques contenues
		 * dans les fichiers .did. Grâce à ces informations, on extrait d'autres informations
		 * ainsi que les ressources graphiques des fichiers .dda. Enfin on écrit un fichier
		 * Params.SPRITES/sprites_drawn pour confirmer que les ressources graphiques ont été
		 * extraites.
		 */
		private static void decrypt(){
			Params.STATUS = "Décryptage des palettes.";
			//DPD
			logger.info(dpd.size()+" fichier(s) DPD trouvé(s).");
			File dir = new File ("."+File.separator+"data"+File.separator+"palettes"+File.separator);
			if (!dir.exists()) dir.mkdirs();
			Iterator<File> iter_dpd = dpd.iterator();
			while(iter_dpd.hasNext()){
				File f = iter_dpd.next();
				DPD dpdFile = new DPD();
				dpdFile.decrypt(f);
			}
			Params.STATUS = "décryptage des informations de tuiles/sprites.";
			//DID
			logger.info(did.size()+" fichier(s) DID trouvé(s).");
			Iterator<File> iter_did = did.iterator();
			while(iter_did.hasNext()){
				File f = iter_did.next();
				DID didFile = new DID();
				didFile.decrypt(f);
			}
			Params.STATUS = "extraction des tuiles/sprites.";
			//DDA
			logger.info(dda.size()+" fichier(s) DDA trouvé(s).");
			Iterator<File> iter_dda2 = dda.iterator();
			while(iter_dda2.hasNext()){
				File f = iter_dda2.next();
				DDA ddaFile = new DDA();
				ddaFile.decrypt(f);
			}
			Params.draw_sprites = false;

			//TODO Ici on pourra ajouter un moyen de contrôle pour être certains que les ressources graphiques ont été correctement extraites.
			
			File fi = new File (Params.SPRITES+"sprites_drawn");
			if (!fi.exists()){
				try {
					fi.createNewFile();
				} catch (IOException e) {
					logger.fatal(e.getLocalizedMessage(),e);
					System.exit(1);
				}
			}
		}
		
		private static void repacktuiles(){
			logger.info("Empaquetage des tuiles.");
			Params.STATUS = "Empaquetage des tuiles.";
			AssetsLoader.pack_tuiles();
		}

		private static void repacksprites(){
			logger.info("Empaquetage des sprites.");
			Params.STATUS = "Empaquetage des sprites.";
			AssetsLoader.pack_sprites();
		}
		
		/**
		 * On utilise les données extraites pour écrire les informations
		 * en clair. Ça permettra de les utiliser pour écrire des cartes
		 * plus utilisables.
		 * 
		 * On écrit un fichier texte Params.SPRITES/sprite_data contenant
		 * ligne par ligne toutes les informations concernant les sprites
		 * dont on dispose. On sépare les tuiles (cases de sol de 32 x 16)
		 * des sprites (éléments de décors de tailles diverses, personnages,
		 * effets graphiques).
		 */
		private static void format(){
			Params.STATUS = "Formattage des données.";
			OutputStreamWriter dat_file = null;
			try {
				dat_file = new OutputStreamWriter(new FileOutputStream(Params.SPRITES+"sprite_data"));
			} catch (FileNotFoundException e) {
				logger.fatal(e);
				System.exit(1);
			}
			Iterator<Integer> iter_tuile = DDA.getTuiles().keySet().iterator();
			while (iter_tuile.hasNext()){
				int key = iter_tuile.next();
				Sprite tuile = DDA.getTuiles().get(key);
				try {
					//logger.info("	- 1"+"|"+key+"|"+tuile.chemin+"|"+tuile.nom+"|"+tuile.type+"|"+tuile.ombre+"|"+tuile.largeur+"|"+tuile.hauteur+"|"+tuile.couleurTrans+"|"+tuile.offsetX+"|"+tuile.offsetY+"|"+tuile.offsetX2+"|"+tuile.offsetY2+"|"+tuile.numDda+"|"+tuile.moduloX+"|"+tuile.moduloY);
					dat_file.write("1"+";"+key+";"+tuile.chemin+";"+tuile.getName()+";"+tuile.type+";"+tuile.ombre+";"+tuile.largeur+";"+tuile.hauteur+";"+tuile.couleurTrans+";"+tuile.offsetX+";"+tuile.offsetY+";"+tuile.offsetX2+";"+tuile.offsetY2+";"+tuile.numDda+";"+tuile.moduloX+";"+tuile.moduloY+Params.LINE);
				} catch (IOException e) {
					logger.fatal(e);
					System.exit(1);
				}
			}
			logger.info("Tuiles OK.");
			
			Iterator<Integer> iter_sprite = DDA.getSprites().keySet().iterator();
			while (iter_sprite.hasNext()){
				int key = iter_sprite.next();
				Sprite sprite = DDA.getSprites().get(key);
				try {
					//logger.info("	- 0"+"|"+key+"|"+sprite.chemin+"|"+sprite.nom+"|"+sprite.type+"|"+sprite.ombre+"|"+sprite.largeur+"|"+sprite.hauteur+"|"+sprite.couleurTrans+"|"+sprite.offsetX+"|"+sprite.offsetY+"|"+sprite.offsetX2+"|"+sprite.offsetY2+"|"+sprite.numDda+"|"+sprite.moduloX+"|"+sprite.moduloY);
					dat_file.write("0"+";"+key+";"+sprite.chemin+";"+sprite.getName()+";"+sprite.type+";"+sprite.ombre+";"+sprite.largeur+";"+sprite.hauteur+";"+sprite.couleurTrans+";"+sprite.offsetX+";"+sprite.offsetY+";"+sprite.offsetX2+";"+sprite.offsetY2+";"+sprite.numDda+";-1;-1"+Params.LINE);
				} catch (IOException e) {
					logger.fatal(e);
					System.exit(1);
				}
			}
			logger.info("Sprites OK.");
			
			try {
				dat_file.close();
			} catch (IOException e1) {
				logger.fatal(e1);
				System.exit(1);
			}
		}
		
		/**
		 * Décrypte les cartes puis les réécrit en clair grâce au
		 * fichier sprite_data.
		 */
		private static void writeMapData(){
			Params.STATUS = "Écriture des cartes en clair.";
			MapFile m = null;
			File f = null;
			//lecture des cartes
			Iterator<File> iter_maps = map.iterator();//Pour chaque fichier de carte
			while (iter_maps.hasNext()){
				f = iter_maps.next();
				if (!new File("."+File.separator+"data"+File.separator+f.getName()+".decrypt").exists()){
					Params.STATUS = "Décodage de la carte : "+f.getName();
					MAP mapFile = new MAP();
					mapFile.Map_load_block(f, 0x00002000);//On décrypte
				}
			}
			logger.info("Décryptage des cartes OK, lecture des cartes decryptées.");
			Params.STATUS = "Décodage des cartes OK.";
			FileLister explorer = new FileLister();
			decrypted.addAll(explorer.lister(new File("."+File.separator+"data"+File.separator), ".map.decrypt"));//Pour chaque fichier de carte décrypté
			Iterator<File> iter_decrypted = decrypted.iterator();
			while (iter_decrypted.hasNext()){
				f = iter_decrypted.next();
				if (!new File("."+File.separator+"data"+File.separator+f.getName()+".bin").exists()){
					logger.info("Traitement de la carte "+f.getName());
					Params.STATUS = "Réécriture de la carte : "+f.getName();
					map_load(f);//On charge la carte
					m = new MapFile(new File("."+File.separator+"data"+File.separator+f.getName()+".bin"));
					BufferedReader sprite_data = null;
					try {
						sprite_data  = new BufferedReader(new FileReader(Params.SPRITES+"sprite_data"));
					} catch (IOException e1) {
						logger.fatal(e1);
						System.exit(1);
					}
					String line = "";
					try {
						while((line = sprite_data.readLine()) != null){//On lit le fichier sprite_data
							String[] index = line.split("\\;");//On lit chaque ligne du fichier, et pour chaque ligne :
							boolean tuile = false;
							if (index[0].equals("1"))tuile = true;//On vérifie si c'est une tuile
							int id = Integer.parseInt(index[1]);//On récupère l'ID
							String atlas = index[2];//On récupère le nom de l'atlas
							String tex = index[3];//On récupère le nom de la texture
							int offsetX = Integer.parseInt(index[9]);//On récupère l'Offset
							int offsetY = Integer.parseInt(index[10]);
							int moduloX = Integer.parseInt(index[14]);//On récupère le modulo pour appliquer l'effet de zone
							int moduloY = Integer.parseInt(index[15]);
							//System.err.println("ID "+id+" : "+tex+" "+moduloX+","+moduloY+"|"+tuile);
							m.ids.put(id, new MapPixel(tuile, atlas, tex, new Point(offsetX,offsetY), new Point(moduloX,moduloY), id));//On enregistre une liste avec les ID, les coordonnées et les références graphiques
						}
					} catch (NumberFormatException | IOException e2) {
						logger.fatal(e2);
						System.exit(1);
					}
					try {
						sprite_data.close();
					} catch (IOException e2) {
						logger.fatal(e2);
						System.exit(1);
					}
					for (int y=0 ; y<3072 ; y++){//On parcourt chaque ligne
						for (int x=0 ; x<3072 ; x++){//et chaque case de la ligne
							Params.STATUS = "Traitement de la carte "+m.getName()+" : "+x+","+y;
							Point coord = new Point(x,y);//On récupère les coordonnées
							int id = getID(coord);//On récupère l'ID
							if(m.ids.containsKey(id)){//On vérifie dans notre liste de Cases si l'ID est présent
								MapPixel pixel = m.ids.get(id);//Si oui, on le récupère
								//System.err.println("Modulo : "+pixel.tex+" "+pixel.modulo.x+","+pixel.modulo.y);
								m.addPixel(coord, pixel);
								//m.setZone(coord);
							}else{//Si le pixel est introuvable, c'est que l'id n'est pas mappée
								logger.info("ID non mappée : " + id);
								m.addPixel(coord, false, "foo", "bar", new Point(0, 0), new Point(0, 0), id);
							}
						}
					}
					Params.STATUS = "Écriture du fichier " + m.getName();
					map_unload();
					m.write();
				}
			}
		}
		
		/**
		 * Permet de connaître l'ID d'un point sur la carte chargée en lecture.
		 * @param coord : Coordonnées du point sur la carte dont on veut récupérer l'ID
		 * @return l'ID recherché.
		 */
		private static int getID(Point coord){
			byte b1=0,b2=0;
			int result = 0;
			if(map_buffer != null){
				map_buffer.position((6144*coord.y)+(coord.x*2));
				//new Fast_Forward(map_buffer, 16, false, "getID");
				b1 = map_buffer.get();
				b2 = map_buffer.get();
			}else{
				logger.fatal("Buffer de carte nul");
				System.exit(1);
			}
			result = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
			//System.err.println("ID trouvée : "+result);
			return result;
		}
		
		/**
		 * Charge une carte en vue de la décrypter.
		 * @param map : le fichier contenant la carte à décrypter.
		 */
		private static void map_load(File map){
			map_loaded = false;
			map_buffer = ByteBuffer.allocate((int)map.length());
			try {
				DataInputManager in = new DataInputManager (map);
				while (map_buffer.position()<map_buffer.capacity()){
					map_buffer.put(in.readByte());
				}
				in.close();
			}catch(IOException exc){
				logger.warn("Erreur d'ouverture");
				logger.fatal(exc);
				System.exit(1);
			}
			map_buffer.rewind();
			map_loaded = true;
		}
		
		/**
		 * Réduit la taille du tampon utilisé pour charger les cartes à 1 octet.
		 */
		private static void map_unload(){
			map_loaded = false;
			map_buffer = ByteBuffer.allocate(1);
		}

		@Override
		public void create() {
			// TODO Auto-generated method stub
			
		}
}
