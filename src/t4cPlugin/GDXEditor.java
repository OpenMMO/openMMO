package t4cPlugin;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import tools.OSValidator;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class GDXEditor extends Game{
	
	public static boolean decrypt = false, repack_tuiles = false, format = false, mapData = false, repack_sprites = false, map_loaded = false;
	private static ArrayList<File> did = new ArrayList<File>();
	private static ArrayList<File> map = new ArrayList<File>();
	private static ArrayList<File> dda = new ArrayList<File>();
	private static ArrayList<File> dpd = new ArrayList<File>();
	private static ArrayList<File> decrypted = new ArrayList<File>();
	public static ScreenManager sm = null;
	private static ByteBuffer map_buffer;
	public static MapFile mapf;


		public static void main(String[] args) {
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
			if (decrypt)decrypt();
			if (format)format();
			if (mapData)writeMapData();
			if (repack_tuiles)repacktuiles();
			if (repack_sprites)repacksprites();
			sm.initMap();
		}

		private static void checkData() {
			//recherche de données
			Params.STATUS = "Chargement des fichiers de donnée.";
			FileLister explorer = new FileLister();
			dpd.addAll(explorer.lister(new File("."+File.separator+"data"+File.separator+"game_files"+File.separator), ".dpd"));
			did.addAll(explorer.lister(new File("."+File.separator+"data"+File.separator+"game_files"+File.separator), ".did"));
			map.addAll(explorer.lister(new File("."+File.separator+"data"+File.separator+"game_files"+File.separator), ".map"));
			dda.addAll(explorer.lister(new File("."+File.separator+"data"+File.separator+"game_files"+File.separator), ".dda"));
			decrypted.addAll(explorer.lister(new File("."+File.separator+"data"+File.separator), ".decrypt"));
			
			if (!new File (Params.SPRITES+"sprites_drawn").exists()) Params.draw_sprites = true;
			
			if (!new File (Params.SPRITES+"sprite_data").exists()){
				format = true;
				decrypt = true;
			}
			
			if (!new File ("."+File.separator+"data"+File.separator+"v2_cavernmap.map.decrypt.bin").exists())mapData = true;
			if (!new File ("."+File.separator+"data"+File.separator+"v2_dungeonmap.map.decrypt.bin").exists())mapData = true;
			if (!new File ("."+File.separator+"data"+File.separator+"v2_leoworld.map.decrypt.bin").exists())mapData = true;
			if (!new File ("."+File.separator+"data"+File.separator+"v2_underworld.map.decrypt.bin").exists())mapData = true;
			if (!new File ("."+File.separator+"data"+File.separator+"v2_worldmap.map.decrypt.bin").exists())mapData = true;
			if (!new File ("."+File.separator+"data"+File.separator+"v2_cavernmap.map.decrypt").exists())mapData = true;
			if (!new File ("."+File.separator+"data"+File.separator+"v2_dungeonmap.map.decrypt").exists())mapData = true;
			if (!new File ("."+File.separator+"data"+File.separator+"v2_leoworld.map.decrypt").exists())mapData = true;
			if (!new File ("."+File.separator+"data"+File.separator+"v2_underworld.map.decrypt").exists())mapData = true;
			if (!new File ("."+File.separator+"data"+File.separator+"v2_worldmap.map.decrypt").exists())mapData = true;
			
			int nb_tile_dir = -1;
			int nb_sprite_dir = -2;
			int nb_tile_atlas = -3;
			int nb_sprite_atlas = -4;
			
			
			File dir = new File("data"+File.separator+"sprites"+File.separator+"tuiles");
			if(dir.exists())nb_tile_dir = explorer.listerDir(dir).size();
			dir = new File("data"+File.separator+"sprites"+File.separator+"sprites");
			if(dir.exists())nb_sprite_dir = explorer.listerDir(dir).size();
			dir = new File("."+File.separator+"data"+File.separator+"atlas"+File.separator+"tuiles"+File.separator);
			if(dir.exists())nb_tile_atlas = explorer.lister(dir,".atlas").size();
			dir = new File("."+File.separator+"data"+File.separator+"atlas"+File.separator+"sprites"+File.separator);
			if(dir.exists())nb_sprite_atlas = explorer.lister(dir,".atlas").size();
			//System.err.println(nb_tile_atlas+"/"+nb_tile_dir+" "+nb_sprite_atlas+"/"+nb_sprite_dir);
			if (nb_tile_dir != nb_tile_atlas) repack_tuiles = true; 
			if (nb_sprite_dir != nb_sprite_atlas) repack_sprites = true;			
		}

		private static void decrypt(){
			Params.STATUS = "Décryptage des palettes.";
			//DPD
			System.out.println(dpd.size()+" fichier(s) DPD trouvé(s).");
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
			System.out.println(did.size()+" fichier(s) DID trouvé(s).");
			Iterator<File> iter_did = did.iterator();
			while(iter_did.hasNext()){
				File f = iter_did.next();
				DID didFile = new DID();
				didFile.decrypt(f);
			}
			Params.STATUS = "extraction des tuiles/sprites.";
			//DDA
			System.out.println(dda.size()+" fichier(s) DDA trouvé(s).");
			Iterator<File> iter_dda2 = dda.iterator();
			while(iter_dda2.hasNext()){
				File f = iter_dda2.next();
				DDA ddaFile = new DDA();
				ddaFile.decrypt(f);
			}
			Params.draw_sprites = false;

			File fi = new File (Params.SPRITES+"sprites_drawn");
			if (!fi.exists()){
				try {
					fi.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		private static void repacktuiles(){
			Params.STATUS = "Empaquetage des tuiles.";
			AssetsLoader.pack_tuiles();
		}

		private static void repacksprites(){
			Params.STATUS = "Empaquetage des sprites.";
			AssetsLoader.pack_sprites();
		}
		
		private static void format(){
			Params.STATUS = "Formattage des données.";
			OutputStreamWriter dat_file = null;
			try {
				dat_file = new OutputStreamWriter(new FileOutputStream(Params.SPRITES+"sprite_data"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}
			Iterator<Integer> iter_tuile = DDA.tuiles.keySet().iterator();
			while (iter_tuile.hasNext()){
				int key = iter_tuile.next();
				Sprite tuile = DDA.tuiles.get(key);
				try {
					//System.out.println("	- 1"+"|"+key+"|"+tuile.chemin+"|"+tuile.nom+"|"+tuile.type+"|"+tuile.ombre+"|"+tuile.largeur+"|"+tuile.hauteur+"|"+tuile.couleurTrans+"|"+tuile.offsetX+"|"+tuile.offsetY+"|"+tuile.offsetX2+"|"+tuile.offsetY2+"|"+tuile.numDda+"|"+tuile.moduloX+"|"+tuile.moduloY);
					dat_file.write("1"+";"+key+";"+tuile.chemin+";"+tuile.nom+";"+tuile.type+";"+tuile.ombre+";"+tuile.largeur+";"+tuile.hauteur+";"+tuile.couleurTrans+";"+tuile.offsetX+";"+tuile.offsetY+";"+tuile.offsetX2+";"+tuile.offsetY2+";"+tuile.numDda+";"+tuile.moduloX+";"+tuile.moduloY+Params.LINE);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			System.out.println("Tuiles OK.");
			
			Iterator<Integer> iter_sprite = DDA.sprites.keySet().iterator();
			while (iter_sprite.hasNext()){
				int key = iter_sprite.next();
				Sprite sprite = DDA.sprites.get(key);
				try {
					//System.out.println("	- 0"+"|"+key+"|"+sprite.chemin+"|"+sprite.nom+"|"+sprite.type+"|"+sprite.ombre+"|"+sprite.largeur+"|"+sprite.hauteur+"|"+sprite.couleurTrans+"|"+sprite.offsetX+"|"+sprite.offsetY+"|"+sprite.offsetX2+"|"+sprite.offsetY2+"|"+sprite.numDda+"|"+sprite.moduloX+"|"+sprite.moduloY);
					dat_file.write("0"+";"+key+";"+sprite.chemin+";"+sprite.nom+";"+sprite.type+";"+sprite.ombre+";"+sprite.largeur+";"+sprite.hauteur+";"+sprite.couleurTrans+";"+sprite.offsetX+";"+sprite.offsetY+";"+sprite.offsetX2+";"+sprite.offsetY2+";"+sprite.numDda+";-1;-1"+Params.LINE);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			System.out.println("Sprites OK.");
			
			try {
				dat_file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}
		
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
			System.out.println("Décryptage des cartes OK, lecture des cartes decryptées.");
			Params.STATUS = "Décodage des cartes OK.";
			FileLister explorer = new FileLister();
			decrypted.addAll(explorer.lister(new File("."+File.separator+"data"+File.separator), ".map.decrypt"));//Pour chaque fichier de carte décrypté
			Iterator<File> iter_decrypted = decrypted.iterator();
			while (iter_decrypted.hasNext()){
				f = iter_decrypted.next();
				if (!new File("."+File.separator+"data"+File.separator+f.getName()+".bin").exists()){
					System.out.println("Traitement de la carte "+f.getName());
					Params.STATUS = "Réécriture de la carte : "+f.getName();
					map_load(f);//On charge la carte
					m = new MapFile(new File("."+File.separator+"data"+File.separator+f.getName()+".bin"));
					BufferedReader sprite_data = null;
					try {
						sprite_data  = new BufferedReader(new FileReader(Params.SPRITES+"sprite_data"));
					} catch (IOException e1) {
						e1.printStackTrace();
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
						e2.printStackTrace();
						System.exit(1);
					}
					try {
						sprite_data.close();
					} catch (IOException e2) {
						e2.printStackTrace();
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
		
		private static int getID(Point coord){
			byte b1=0,b2=0;
			int result = 0;
			if(map_buffer != null){
				map_buffer.position((6144*coord.y)+(coord.x*2));
				//new Fast_Forward(map_buffer, 16, false, "getID");
				b1 = map_buffer.get();
				b2 = map_buffer.get();
			}else{
				System.err.println("Buffer de carte nul");
				System.exit(1);
			}
			result = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
			//System.err.println("ID trouvée : "+result);
			return result;
		}
		
		private static void map_load(File map){
			map_loaded = false;
			//System.out.println("	- Chargement de la carte : "+map.getName());
			map_buffer = ByteBuffer.allocate((int)map.length());
			try {
				DataInputStream in = new DataInputStream (new FileInputStream(map));
				while (map_buffer.position()<map_buffer.capacity()){
					map_buffer.put(in.readByte());
				}
				in.close();
			}catch(IOException exc){
				System.err.println("Erreur d'ouverture");
				exc.printStackTrace();
				System.exit(1);
			}
			map_buffer.rewind();
			map_loaded = true;
		}
		
		private static void map_unload(){
			map_loaded = false;
			map_buffer = ByteBuffer.allocate(1);
		}

		@Override
		public void create() {
			// TODO Auto-generated method stub
			
		}
}