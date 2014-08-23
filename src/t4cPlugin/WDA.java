package t4cPlugin;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;

import tools.BitBuffer;
import tools.DataInputManager;


/*
[EFFET]            [PARAMETRE 1]       [PARAMETRE 2]       [PARAMETRE 3]       [PARAMETRE 4]
Modify health      Formule             Rayon               %age de succès
Attribute boost    RT upd. TRUE|FALSE  Nom de la stat      Formule
Modify flag        ID du flag          Valeur
Player edit        0 = Get | 1 = Set   Offset              Taille des données  ID flag dest
Effect 5
Summon             Type (Monster|NPC)  ID texte
Recall             Position X          Position Y          Monde
Effect 8
Spell hook         Sort                Type de hook        %age de succès      Délai initial
Drain health       Formule             Rayon               Dégâts              %age de succès
Sanctuary
Vaporize [GM]
Dispel             Sort                %age de succès
Deal exhaust       Epuis. attaque      Epuis. mental       Epuis. physique     %age de succès
Invisibility       %age de succès      Effet visuel
Detect invis.      %age de succès      Effet visuel
Detect hidden      %age de succès      Effet visuel

[OFFSET] [TAILLE]  [DESCRIPTION]
96       2 octets  Intelligence pure
98       2 octets  Endurance pure
100      2 octets  Force pure
102      2 octets  Agilité pure
106      2 octets  Sagesse pure
110      2 octets  Esquive pure
112      2 octets  Attaque pure
120      4 octets  ID du joueur dans la base de données
124      4 octets  ID du skin du joueur
128      4 octets  Karma
188      4 octets  Position Y sur la carte
192      4 octets  Numéro du monde (0,1,2,3...)
196      4 octets  Position X sur la carte
238      4 octets  Niveau
312      2 octets  Points de vie maximum
316      2 octets  Points de vie actuels
320      2 octets  Mana maximum
322      2 octets  Mana actuelle
336      4 octets  XP (données incomplètes)
408      4 octets  Or sur soi
480      2 octets  Points de compétence disponibles
482      2 octets  Points de caractéristiques disponibles
12512    2 octets  Poids maximal transportable
*/
class COLMAP {
	
	private static Logger logger = LogManager.getLogger(COLMAP.class.getSimpleName());
	
	short num_carte, tailleX, tailleY;
	private int taille_nom;
	String nom = "";
	DataBufferByte colMap;
	BufferedImage colMapImg;
	WritableRaster colRaster;
	
	public COLMAP(ByteBuffer buf, String wda_name) {
		byte b1,b2,b3,b4;

		b1 = buf.get();
		b2 = buf.get();
		num_carte = tools.ByteArrayToNumber.bytesToShort(new byte[]{b2,b1});
		//logger.info("			- Carte n° : "+indiceZ);
		
		//NOM
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_nom ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nom += s;
		}
		//logger.info("				- Nom : "+nom);
		
		//TailleX
		b1 = buf.get();
		b2 = buf.get();
		tailleX = tools.ByteArrayToNumber.bytesToShort(new byte[]{b2,b1});
		//logger.info("				- Taille X : "+tailleX);
		
		//TailleY
		b1 = buf.get();
		b2 = buf.get();
		tailleY = tools.ByteArrayToNumber.bytesToShort(new byte[]{b2,b1});
		//logger.info("				- Taille Y : "+tailleY);
		byte[] bytes = new byte[3072*3072/2];
		buf.get(bytes,0,3072*3072/2);
		BitBuffer bits = new BitBuffer(bytes);
		colMap = new DataBufferByte((tailleX*tailleY)*2);
		for (int i=0 ; i<tailleY ; i++){
			for(int j=0 ; j<tailleX ; j++){
				colMap.setElem((i*tailleX)+j,bits.getBits(4));
			}
		}
		colRaster = Raster.createPackedRaster(colMap,tailleX,tailleY,8,null);
		byte[] red =   new byte[]{(byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0xA0, (byte) 0xA0, (byte) 0x00, (byte) 0xA0, (byte) 0x7F};
		byte[] green = new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0xA0, (byte) 0xA0, (byte) 0x7F};
		byte[] blue =  new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0xA0, (byte) 0xA0, (byte) 0xA0, (byte) 0x7F};
		IndexColorModel model = new IndexColorModel(8,16,red,green,blue);
		colMapImg = new BufferedImage(model, colRaster, false, null);
		/*File f = new File(Params.t4cOUT+"COLMAPS"+File.separator+wda_name+"_"+nom+".png");
		try {
			ImageIO.write(colMapImg, "png", f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("	- Carte de collision écrite : "+Params.t4cOUT+"COLMAPS/"+nom+".png");
		Params.nb_colmap++;*/
	}

}


	class Teleportation{
		public static int id=0;
		int x1;
		int y1;
		int num_carte1;
		int x2;
		int y2;
		int num_carte2;

		public Teleportation(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			x1 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- x1 : "+x1);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			y1 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- y1 : "+y1);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			num_carte1 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- num_carte1 : "+num_carte1);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			x2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- x2 : "+x2);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			y2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- y2 : "+y2);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			num_carte2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- num_carte2 : "+num_carte2);

		}
	}
	
	class Clan_relation{
		static int id = 0;
		int id1;
		int id2;
		short rel;//Relation entre les clans (-100 = amour, 0 = neutre, 100 = haine)
		public Clan_relation(ByteBuffer buf) {
			byte b1, b2;
			b1 = buf.get();
			b2 = buf.get();
			id1 = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
			//logger.info("		- id1 : "+id1);
			
			b1 = buf.get();
			b2 = buf.get();
			id2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
			//logger.info("		- id2 : "+id2);
			
			b1 = buf.get();
			b2 = buf.get();
			rel = tools.ByteArrayToNumber.bytesToShort(new byte[]{b2,b1});
			//logger.info("		- rel : "+rel);
		}
	}

	class Clan{
		int id;
		private int taille_nom;
		String nom="";
		public Clan(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
			//logger.info("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			//logger.info("					- nom : "+nom);
		}
	}
	
	class Flag{
		int id;
		private int taille_nom;
		String nom="";
		public Flag(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			//logger.info("					- nom : "+nom);
		}
	}
	
	class Gfx_item{
		int id;
		private int taille_nom;
		String nom="";
		public Gfx_item(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			//logger.info("					- nom : "+nom);
		}
	}
	
	class Gfx_creatures{
		int id;
		int id2;
		private int taille_nom;
		String nom="";
		public Gfx_creatures(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- id2??? : "+id2);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			//logger.info("					- nom : "+nom);
		}
	}

	class Lieu{
		private int taille_nom;
		String nom="";	
		public Lieu(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			//logger.info("					- nom : "+nom);
		}
	}

	class Icone{
		int id;
		private int taille_nom;
		String nom="";
		public Icone(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			//logger.info("					- nom : "+nom);
		}
	}
	
	class Fx_spell{
		int id;
		private int taille_nom;
		String nom="";
		
		public Fx_spell(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				//if (b1 == 47 && !Params.OS.equals("Windows")) b1 = 92;
				String s = new String(new byte[]{b1});
				nom += s;
			}
		}
	}

public class WDA {
	
	private static Logger logger = LogManager.getLogger(WDA.class.getSimpleName());
	
	private int marqueur = 0;//vérifier plus tard qu'il vaut 68775
	private byte type = 0; //Type de WDA (T4C Worlds(RO) = 0x01, T4C Edit(RW) = 0x00)
	private ByteBuffer buf;
	private ArrayList<SPELL> sorts = new ArrayList<SPELL>();
	private ArrayList<COLMAP> colmaps = new ArrayList<COLMAP>();
	private ArrayList<ITEM> items = new ArrayList<ITEM>();
	private ArrayList<ITEMPOS> item_pos = new ArrayList<ITEMPOS>();
	private ArrayList<CREATURE> creatures = new ArrayList<CREATURE>();
	private ArrayList<CREATUREBLOC> blocs_creatures = new ArrayList<CREATUREBLOC>();
	private ArrayList<Teleportation> teleportations = new ArrayList<Teleportation>();
	private ArrayList<Clan_relation> clan_relations = new ArrayList<Clan_relation>();
	private ArrayList<Clan> clans = new ArrayList<Clan>();
	private ArrayList<Flag> flags = new ArrayList<Flag>();
	private ArrayList<Gfx_item> gfx_items = new ArrayList<Gfx_item>();
	private ArrayList<Gfx_creatures> gfx_creatures = new ArrayList<Gfx_creatures>();
	private ArrayList<Lieu> lieux = new ArrayList<Lieu>();
	private ArrayList<Icone> icones = new ArrayList<Icone>();
	private ArrayList<Fx_spell> fx_sorts = new ArrayList<Fx_spell>();
	private ArrayList<PNJ> pnjs = new ArrayList<PNJ>();

	public void decrypt (File wda){
		int wda_data_size = (int) wda.length();
		buf = ByteBuffer.allocate(wda_data_size);
		try {
			DataInputManager in = new DataInputManager (wda);
			while (buf.position() < (int)wda.length()){
				buf.put(in.readByte());
			}
			in.close();
		}catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		for (int i = 0; i < wda_data_size; i++){
			buf.array()[i] ^= WDACryptKey.key[i % 3418]; // Ou exclusif avec la clé par blocs de 3418 octets
		}
		buf.rewind();
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		marqueur = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		if (marqueur != 68775){
			if (marqueur != 1){
				//logger.info("	- Marqueur érroné : "+marqueur);
				Gdx.app.exit();
			}else {
				//logger.info("	- Marqueur correct : "+marqueur+" : fichier PNJ");
				//extractPNJ(buf);
			}
		}else {
			//logger.info("	- Marqueur correct : "+marqueur+" : fichier WORLD/EDIT");
			extract(buf, wda);
		}
		
		/*Params.total_sorts = sorts.size();
		Params.total_colmaps = colmaps.size();
		Params.total_items = items.size();
		Params.total_item_pos = item_pos.size();
		Params.total_creatures = creatures.size();
		Params.total_spawns = blocs_creatures.size();
		Params.total_teleportations = teleportations.size();
		Params.total_clan_relations = clan_relations.size();
		Params.total_clans = clans.size();
		Params.total_flags = flags.size();
		Params.total_gfx_items = gfx_items.size();
		Params.total_gfx_creatures = gfx_creatures.size();
		Params.total_lieux = lieux.size();
		Params.total_icones = icones.size();
		Params.total_fx_sorts = fx_sorts.size();
		Params.total_pnjs = pnjs.size();*/
		
	}

	private void extract(ByteBuffer buf, File wda) {
		type = buf.get();
		extractSPELL(buf, wda);// pour les sorts
		extractCOLMAP(buf, wda);//pour les cartes
		extractITEM(buf, wda);//pour les items
		extractCREATURE(buf, wda);//pour les créatures
		extractCREATUREBLOC(buf, wda);//pour les blocs de spawn
		extractTeleportations(buf, wda);//pour les téléportations
		extractClanRelations(buf, wda);//pour les relations entre les clans
		extractClans(buf, wda);//pour les clans
		extractFlags(buf, wda);//pour les flags
		if (buf.get() == 0) return;
		extractGfxItems(buf, wda);//pour les apparences d'objets
		extractGfxCreatures(buf, wda);//pour les apparences de creatures
		extractLieux(buf, wda);//pour les lieux
		extractIcones(buf, wda);//pour les icones
		extractFxSpells(buf, wda);//pour les effets de sorts

	}

	private void extractFxSpells(ByteBuffer buf, File wda) {
		int nb_fx;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_fx = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de blocs d'effets de sorts : "+nb_fx);
		
		for (int i=0 ; i<nb_fx ; i++){
			fx_sorts.add(new Fx_spell(buf));
		}
		
		Iterator<Fx_spell> iter_fx_sorts = fx_sorts.iterator();
		while(iter_fx_sorts.hasNext()){
			Fx_spell fx = iter_fx_sorts.next();
			/*File f = new File (Params.t4cOUT+"SPELLFX/"+wda.getName()+"_"+fx.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				System.err.println(Params.t4cOUT+"SPELLFX/"+wda.getName()+"_"+fx.nom+".txt");
				e.printStackTrace();
				Gdx.app.exit();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"SPELLFX/"+f.getName()),Params.CHARSET);
				pw.write("SPELLFX : "+fx.nom+Params.LINE);
				pw.write("	- id : "+fx.id+Params.LINE);
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- EFFET_SORT écrit : "+Params.t4cOUT+"SPELLFX/"+wda.getName()+"_"+fx.nom+".txt");
			Params.nb_effets_sorts++;*/
		}		
	}
	
	private void extractIcones(ByteBuffer buf, File wda) {
		int nb_icones;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_icones = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		///logger.info("		- Définition du nombre d'icones : "+nb_icones);
		
		for (int i=0 ; i<nb_icones ; i++){
			icones.add(new Icone(buf));
		}
		
		Iterator<Icone> iter_icones = icones.iterator();
		while(iter_icones.hasNext()){
			Icone ic = iter_icones.next();
			/*File f = new File (Params.t4cOUT+"ICONES/"+wda.getName()+"_"+ic.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"ICONES/"+f.getName()),Params.CHARSET);
				pw.write("ICONES : "+ic.nom+Params.LINE);
				pw.write("	- id : "+ic.id+Params.LINE);
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- ICONE écrite : "+Params.t4cOUT+"ICONES/"+wda.getName()+"_"+ic.nom+".txt");
			Params.nb_icones++;*/
		}		
	}
	
	private void extractLieux(ByteBuffer buf, File wda) {
		int nb_lieux;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_lieux = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de lieux : "+nb_lieux);
		
		for (int i=0 ; i<nb_lieux ; i++){
			lieux.add(new Lieu(buf));
		}
		
		Iterator<Lieu> iter_lieux = lieux.iterator();
		while(iter_lieux.hasNext()){
			Lieu l = iter_lieux.next();
			/*File f = new File (Params.t4cOUT+"LIEUX/"+wda.getName()+"_"+l.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"LIEUX/"+f.getName()),Params.CHARSET);
				pw.write("LIEU : "+l.nom+Params.LINE);
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- LIEU écrit : "+Params.t4cOUT+"LIEUX/"+wda.getName()+"_"+l.nom+".txt");
			Params.nb_lieux++;*/
		}		
	}
	
	private void extractGfxCreatures(ByteBuffer buf, File wda) {
		int nb_gfx_creatures;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_gfx_creatures = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre d'apparences de créatures : "+nb_gfx_creatures);
		
		for (int i=0 ; i<nb_gfx_creatures ; i++){
			gfx_creatures.add(new Gfx_creatures(buf));
		}
		
		Iterator<Gfx_creatures> iter_gfx_creatures = gfx_creatures.iterator();
		while(iter_gfx_creatures.hasNext()){
			Gfx_creatures ct = iter_gfx_creatures.next();
			/*File f = new File (Params.t4cOUT+"MONSTRES/"+wda.getName()+"_GFX_"+ct.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"MONSTRES/"+f.getName()),Params.CHARSET);
				pw.write("GFX MONSTRE : "+ct.nom+Params.LINE);
				pw.write("	- id : "+ct.id+Params.LINE);
				pw.write("	- id2 : "+ct.id2+Params.LINE);
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- GFX CREATURE écrit : "+Params.t4cOUT+"MONSTRES/"+wda.getName()+"_GFX_"+ct.nom+".txt");
			Params.nb_gfx_creatures++;*/
		}		
	}
	
	private void extractGfxItems(ByteBuffer buf, File wda) {
		int nb_gfx_items;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_gfx_items = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre d'apparences d'objets : "+nb_gfx_items);
		
		for (int i=0 ; i<nb_gfx_items ; i++){
			gfx_items.add(new Gfx_item(buf));
		}
		
		Iterator<Gfx_item> iter_gfx_items = gfx_items.iterator();
		while(iter_gfx_items.hasNext()){
			Gfx_item it = iter_gfx_items.next();
			/*File f = new File (Params.t4cOUT+"ITEMS/"+wda.getName()+"_GFX_"+it.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"ITEMS/"+f.getName()),Params.CHARSET);
				pw.write("GFX ITEM : "+it.nom+Params.LINE);
				pw.write("	- id : "+it.id+Params.LINE);
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- GFX ITEM écrit : "+Params.t4cOUT+"ITEMS/"+wda.getName()+"_GFX_"+it.nom+".txt");
			Params.nb_gfx_items++;*/
		}		
	}

	
	private void extractFlags(ByteBuffer buf, File wda) {
		int nb_flags;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_flags = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de flags : "+nb_flags);
		
		for (int i=0 ; i<nb_flags ; i++){
			flags.add(new Flag(buf));
		}
		
		Iterator<Flag> iter_flags = flags.iterator();
		while(iter_flags.hasNext()){
			Flag fl = iter_flags.next();
			/*File f = new File (Params.t4cOUT+"FLAGS/"+wda.getName()+"_"+fl.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"FLAGS/"+f.getName()),Params.CHARSET);
				pw.write("FLAGS : "+fl.nom+Params.LINE);
				pw.write("	- id : "+fl.id+Params.LINE);
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- FLAG écrit : "+Params.t4cOUT+"FLAGS/"+wda.getName()+"_"+fl.nom+".txt");
			Params.nb_flags++;*/
		}		
	}

	
	private void extractClans(ByteBuffer buf, File wda) {
		int nb_clans;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_clans = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de clans : "+nb_clans);
		
		for (int i=0 ; i<nb_clans ; i++){
			clans.add(new Clan(buf));
		}
		
		Iterator<Clan> iter_clans = clans.iterator();
		while(iter_clans.hasNext()){
			Clan cl = iter_clans.next();
			/*File f = new File (Params.t4cOUT+"CLANS/"+wda.getName()+"_"+cl.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"CLANS/"+f.getName()),Params.CHARSET);
				pw.write("CLANS : "+cl.nom+Params.LINE);
				pw.write("	- id : "+cl.id+Params.LINE);
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- CLAN écrit : "+Params.t4cOUT+"CLANS/"+wda.getName()+"_"+cl.nom+".txt");
			Params.nb_clans++;*/
		}		
	}

	
	private void extractClanRelations(ByteBuffer buf, File wda) {
		int nb_clan_relations;
		int id_sup;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		id_sup = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition de l'id supérieure des clans : "+id_sup);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_clan_relations = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de blocs de relations entre clans : "+nb_clan_relations);
		
		for (int i=0 ; i<nb_clan_relations ; i++){
			clan_relations.add(new Clan_relation(buf));
		}
		
		Iterator<Clan_relation> iter_clan_relations = clan_relations.iterator();
		while(iter_clan_relations.hasNext()){
			Clan_relation clr = iter_clan_relations.next();
			/*File f = new File (Params.t4cOUT+"CLANS/"+wda.getName()+"_REL_"+Params.nb_clan_relations+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"CLANS/"+f.getName()),Params.CHARSET);
				pw.write("RELATION : "+Params.nb_clan_relations+Params.LINE);
				pw.write("			- id1 : "+clr.id1+Params.LINE);						
				pw.write("			- id2 : "+clr.id2+Params.LINE);						
				pw.write("			- rel : "+clr.rel+Params.LINE);						
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- RELATION INTERCLAN écrite : "+Params.t4cOUT+"CLANS/"+wda.getName()+"_REL_"+Params.nb_clan_relations+".txt");
			Params.nb_clan_relations++;*/
		}		
	}

	
	private void extractTeleportations(ByteBuffer buf, File wda) {
		int nb_teleportations;;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_teleportations = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de blocs de téléportations : "+nb_teleportations);
		
		for (int i=0 ; i<nb_teleportations ; i++){
			teleportations.add(new Teleportation(buf));
		}
		
		Iterator<Teleportation> iter_teleportations = teleportations.iterator();
		while(iter_teleportations.hasNext()){
			Teleportation t = iter_teleportations.next();
			/*File f = new File (Params.t4cOUT+"LIEUX/"+wda.getName()+"_TEL_"+Params.nb_teleportations+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"LIEUX/"+f.getName()),Params.CHARSET);
				pw.write("TELEPORTATION : "+Teleportation.id+Params.LINE);
				pw.write("	- x1 : "+t.x1+Params.LINE);
				pw.write("	- y1 : "+t.y1+Params.LINE);
				pw.write("	- num_carte1 : "+t.num_carte1+Params.LINE);
				pw.write("	- x2 : "+t.x2+Params.LINE);
				pw.write("	- y2 : "+t.y2+Params.LINE);
				pw.write("	- num_carte2 : "+t.num_carte2+Params.LINE);
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- TELEPORTATION écrite : "+Params.t4cOUT+"LIEUX/"+wda.getName()+"_TEL_"+Params.nb_teleportations+".txt");
			Params.nb_teleportations++;*/
		}		
	}

	private void extractCREATUREBLOC(ByteBuffer buf, File wda) {
		int nb_blocs;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_blocs = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de blocs de créatures : "+nb_blocs);
		
		for (int i=0 ; i<nb_blocs ; i++){
			blocs_creatures.add(new CREATUREBLOC(buf));
		}
		
		Iterator<CREATUREBLOC> iter_creature_bloc = blocs_creatures.iterator();
		while(iter_creature_bloc.hasNext()){
			CREATUREBLOC sp = iter_creature_bloc.next();
			/*File f = new File (Params.t4cOUT+"SPAWN/"+wda.getName()+"_"+sp.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"SPAWN/"+f.getName()),Params.CHARSET);
				pw.write("SPAWN BLOC : "+sp.nom+Params.LINE);
				pw.write("	- max_dist : "+sp.max_dist+Params.LINE);
				pw.write("	- min_time : "+sp.min_time+Params.LINE);
				pw.write("	- max_time : "+sp.max_time+Params.LINE);
				pw.write("	- min_spawn : "+sp.min_spawn+Params.LINE);
				pw.write("	- nb_creatures : "+sp.nb_creatures+Params.LINE);
				for (int i=0 ; i<sp.nb_creatures ; i++){
					pw.write("			- id : "+sp.creatures.get(i).id+Params.LINE);						
				}
				pw.write("	- nb_pos : "+sp.nb_pos+Params.LINE);
				for (int i=0 ; i<sp.nb_pos ; i++){
					pw.write("		- x : "+sp.positions.get(i).x+Params.LINE);						
					pw.write("		- y : "+sp.positions.get(i).y+Params.LINE);						
					pw.write("		- num_carte : "+sp.positions.get(i).num_carte+Params.LINE);						
				}
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- SPAWN écrit : "+Params.t4cOUT+"SPAWN/"+wda.getName()+"_"+sp.nom+".txt");
			Params.nb_spawns++;*/
		}
	}

	private void extractCREATURE(ByteBuffer buf, File wda) {
		int nb_creature;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_creature = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de créatures : "+nb_creature);
		
		for (int i=0 ; i<nb_creature ; i++){
			creatures.add(new CREATURE(buf));
		}
		
		Iterator<CREATURE> iter_creatures = creatures.iterator();
		while(iter_creatures.hasNext()){
			CREATURE c = iter_creatures.next();
			/*File f = new File (Params.t4cOUT+"MONSTRES/"+wda.getName()+"_"+c.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"MONSTRES/"+f.getName()),Params.CHARSET);
				pw.write("MONSTRE : "+c.nom+Params.LINE);
				pw.write("	- ID : "+c.ID+Params.LINE);
				pw.write("	- agilite : "+c.agilite+Params.LINE);
				pw.write("	- IDnum : "+c.IDnum+Params.LINE);
				pw.write("	- agressivite : "+c.agressivite+Params.LINE);
				pw.write("	- arme : "+c.arme+Params.LINE);
				pw.write("	- bouclier : "+c.bouclier+Params.LINE);
				pw.write("	- CA : "+c.CA+Params.LINE);
				pw.write("	- chance : "+c.chance+Params.LINE);
				pw.write("	- endurance : "+c.endurance+Params.LINE);
				pw.write("	- esquive : "+c.esquive+Params.LINE);
				pw.write("	- force : "+c.force+Params.LINE);
				pw.write("	- ID : "+c.ID+Params.LINE);
				pw.write("	- id_clan : "+c.id_clan+Params.LINE);
				pw.write("	- IDnum : "+c.IDnum+Params.LINE);
				pw.write("	- intelligence : "+c.intelligence+Params.LINE);
				pw.write("	- niveau : "+c.niveau+Params.LINE);
				pw.write("	- or_max : "+c.or_max+Params.LINE);
				pw.write("	- or_min : "+c.or_min+Params.LINE);
				pw.write("	- puissance_air : "+c.puissance_air+Params.LINE);
				pw.write("	- puissance_eau : "+c.puissance_eau+Params.LINE);
				pw.write("	- puissance_feu : "+c.puissance_feu+Params.LINE);
				pw.write("	- puissance_lumiere : "+c.puissance_lumiere+Params.LINE);
				pw.write("	- puissance_necromancie : "+c.puissance_necromancie+Params.LINE);
				pw.write("	- puissance_terre : "+c.puissance_terre+Params.LINE);
				pw.write("	- resistance_air : "+c.resistance_air+Params.LINE);
				pw.write("	- resistance_eau : "+c.resistance_eau+Params.LINE);
				pw.write("	- resistance_feu : "+c.resistance_feu+Params.LINE);
				pw.write("	- resistance_lumiere : "+c.resistance_lumiere+Params.LINE);
				pw.write("	- illimite : "+c.resistance_necromancie+Params.LINE);
				pw.write("	- resistance_necromancie : "+c.resistance_terre+Params.LINE);
				pw.write("	- sagesse : "+c.sagesse+Params.LINE);
				pw.write("	- non_cach : "+c.vet_apparence+Params.LINE);
				pw.write("	- vet_apparence : "+c.vet_corps+Params.LINE);
				pw.write("	- non_jet : "+c.vet_dos+Params.LINE);
				pw.write("	- vet_dos : "+c.vet_jambe+Params.LINE);
				pw.write("	- vet_main : "+c.vet_main+Params.LINE);
				pw.write("	- vet_pied : "+c.vet_pied+Params.LINE);
				pw.write("	- vet_tete : "+c.vet_tete+Params.LINE);
				pw.write("	- vitesse : "+c.vitesse+Params.LINE);
				pw.write("	- volonte : "+c.volonte+Params.LINE);
				pw.write("	- xp_coup : "+c.xp_coup+Params.LINE);
				pw.write("	- xp_down : "+c.xp_down+Params.LINE);
				pw.write("	- can_attack : "+c.can_attack+Params.LINE);
				pw.write("	- nb_types_attaques : "+c.nb_types_attaques+Params.LINE);
				for (int i=0 ; i<c.nb_types_attaques ; i++){
					pw.write("		- Attaque "+i+Params.LINE);
					pw.write("			- degats : "+c.attaques.get(i).degats+Params.LINE);						
					pw.write("			- distance_max : "+c.attaques.get(i).distance_max+Params.LINE);						
					pw.write("			- distance_min : "+c.attaques.get(i).distance_min+Params.LINE);						
					pw.write("			- id_sort : "+c.attaques.get(i).id_sort+Params.LINE);						
					pw.write("			- lvl : "+c.attaques.get(i).lvl+Params.LINE);						
					pw.write("			- succes : "+c.attaques.get(i).succes+Params.LINE);						
				}
				pw.write("	- nb_flags : "+c.nb_flags+Params.LINE);
				for (int i=0 ; i<c.nb_flags ; i++){
					pw.write("		- flag "+i+Params.LINE);
					pw.write("			- id : "+c.flags.get(i).id+Params.LINE);						
					pw.write("			- valeur : "+c.flags.get(i).valeur+Params.LINE);						
					pw.write("			- incremente : "+c.flags.get(i).incremente+Params.LINE);						
				}
				pw.write("	- nb_drops : "+c.nb_drops+Params.LINE);
				for (int i=0 ; i<c.nb_drops ; i++){
					pw.write("		- drop "+i+Params.LINE);
					pw.write("			- id : "+c.drops.get(i).id+Params.LINE);						
					pw.write("			- Chance : "+c.drops.get(i).chance+Params.LINE);						
				}
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- MONSTRE écrit : "+Params.t4cOUT+"MONSTRES/"+wda.getName()+"_"+c.nom+".txt");
			Params.nb_creatures++;*/
		}
	}

	private void extractCOLMAP(ByteBuffer buf, File wda) {
		int nb_map;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_map = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de cartes de collision : "+nb_map);
		for (int i = 0 ; i<nb_map ; i++){
			colmaps.add(new COLMAP(buf, wda.getName()));
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//											SORT															  //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void extractSPELL(ByteBuffer buf, File wda) {
		int nb_sorts;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_sorts = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de sorts : "+nb_sorts);
		for (int i = 0 ; i<nb_sorts ; i++){
			sorts.add(new SPELL(buf));
		}
		Iterator<SPELL> iter_spells = sorts.iterator();
		while(iter_spells.hasNext()){
			SPELL s = iter_spells.next();
			/*File f = new File (Params.t4cOUT+"SPELLS/"+wda.getName()+"_"+s.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"SPELLS/"+f.getName()),Params.CHARSET);
				pw.write("Nom : "+s.nom+Params.LINE);
				pw.write("Description : "+s.description+Params.LINE);
				pw.write("ID : "+s.ID+Params.LINE);
				pw.write("type_attaque : "+s.type_attaque+Params.LINE);
				pw.write("type_cible : "+s.type_cible+Params.LINE);
				pw.write("req_lvl : "+s.req_lvl+Params.LINE);
				pw.write("req_int : "+s.req_int+Params.LINE);
				pw.write("req_sag : "+s.req_sag+Params.LINE);
				pw.write("cout_mana : "+s.cout_mana+Params.LINE);
				pw.write("duree : "+s.duree+Params.LINE);
				pw.write("element : "+s.element+Params.LINE);
				pw.write("epuisement_attaque : "+s.epuisement_attaque+Params.LINE);
				pw.write("epuisement_mental : "+s.epuisement_mental+Params.LINE);
				pw.write("epuisement_physique : "+s.epuisement_physique+Params.LINE);
				pw.write("frequence : "+s.frequence+Params.LINE);
				pw.write("index_gfx : "+s.index_gfx+Params.LINE);
				pw.write("index_gfx_cible : "+s.index_gfx_cible+Params.LINE);
				pw.write("index_icone : "+s.index_icone+Params.LINE);
				if (s.ligne_de_vue == 0){
					pw.write("ligne_de_vue : FALSE"+Params.LINE);
				}else{
					pw.write("ligne_de_vue : TRUE"+Params.LINE);
				}
				if (s.controle_pvp == 0){
					pw.write("controle_pvp : FALSE"+Params.LINE);
				}else{
					pw.write("controle_pvp : TRUE"+Params.LINE);
				}
				if (s.sort_attaque == 0){
					pw.write("sort_attaque : FALSE"+Params.LINE);
				}else{
					pw.write("sort_attaque : TRUE"+Params.LINE);
				}
				pw.write("success_rate : "+s.success_rate+Params.LINE);
				pw.write("duree : "+s.duree+Params.LINE);
				pw.write("rayon_degats : "+s.rayon_degats+Params.LINE);
				pw.write("Nombre de sort(s) requis : "+s.nb_req_spells+Params.LINE);
				if (s.nb_req_spells != 0){
					Iterator<Integer> iter_reqspells = s.req_spells.iterator();
					while(iter_reqspells.hasNext()){
						pw.write("	- ID : "+iter_reqspells.next()+Params.LINE);
						//TODO get nom by id;
					}
				}
				pw.write("nb_effets : "+s.nb_effets+Params.LINE);
				if (s.nb_effets != 0){
					Iterator<spell_effect> iter_spelleffects = s.effets.iterator();
					while(iter_spelleffects.hasNext()){
						spell_effect fx = iter_spelleffects.next();
						pw.write("	- Type d'effet : "+fx.type_effet+Params.LINE);
						pw.write("	- Nombre de Paramètres : "+fx.nb_params+Params.LINE);
						if (fx.nb_params !=0){
							Iterator<param_effet> iter_param = fx.params.iterator();
							while (iter_param.hasNext()){
								param_effet p = iter_param.next();
								pw.write("		- "+p.num_param+" : "+p.param+Params.LINE);
							}
						}
					}
				}
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- SORT écrit : "+Params.t4cOUT+"SPELLS/"+wda.getName()+"_"+s.nom+".txt");
			Params.nb_sorts++;*/
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//											ITEM															  //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private void extractITEM(ByteBuffer buf, File wda) {
		int nb_item;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_item = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre d' objets : "+nb_item);
		
		for (int i = 0 ; i<nb_item ; i++){
			items.add(new ITEM(buf));
		}
		
		Iterator<ITEM> iter_items = items.iterator();
		while(iter_items.hasNext()){
			ITEM o = iter_items.next();
			/*File f = new File (Params.t4cOUT+"ITEMS/"+wda.getName()+"_"+o.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"ITEMS/"+f.getName()),Params.CHARSET);
				pw.write("ITEM : "+o.nom+Params.LINE);
				pw.write("	- ID : "+o.ID+Params.LINE);
				pw.write("	- IDgfx : "+o.IDgfx+Params.LINE);
				pw.write("	- IDnum : "+o.IDnum+Params.LINE);
				pw.write("	- charges : "+o.charges+Params.LINE);
				pw.write("	- degats : "+o.degats+Params.LINE);
				pw.write("	- difficulteSerrure : "+o.difficulteSerrure+Params.LINE);
				pw.write("	- emplacement : "+o.emplacement+Params.LINE);
				pw.write("	- malusEsquive : "+o.malusEsquive+Params.LINE);
				pw.write("	- nomSerrure : "+o.nomSerrure+Params.LINE);
				pw.write("	- OR : "+o.OR+Params.LINE);
				pw.write("	- poids : "+o.poids+Params.LINE);
				pw.write("	- posEquip : "+o.posEquip+Params.LINE);
				pw.write("	- radiance : "+o.radiance+Params.LINE);
				pw.write("	- recup : "+o.recup+Params.LINE);
				pw.write("	- reqAGI : "+o.reqAGI+Params.LINE);
				pw.write("	- reqATT : "+o.reqATT+Params.LINE);
				pw.write("	- reqEND : "+o.reqEND+Params.LINE);
				pw.write("	- reqEND : "+o.reqEND+Params.LINE);
				pw.write("	- reqFOR : "+o.reqFOR+Params.LINE);
				pw.write("	- reqINT : "+o.reqINT+Params.LINE);
				pw.write("	- reqSAG : "+o.reqSAG+Params.LINE);
				pw.write("	- respawn : "+o.respawn+Params.LINE);
				pw.write("	- respawnGlobal : "+o.respawnGlobal+Params.LINE);
				pw.write("	- sellPrice : "+o.sellPrice+Params.LINE);
				pw.write("	- structure : "+o.structure+Params.LINE);
				pw.write("	- texteSigne : "+o.texteSigne+Params.LINE);
				pw.write("	- arc : "+o.arc+Params.LINE);
				pw.write("	- arme : "+o.arme+Params.LINE);
				pw.write("	- armure : "+o.armure+Params.LINE);
				pw.write("	- bijou : "+o.bijou+Params.LINE);
				pw.write("	- detr_renaiss : "+o.detr_renaiss+Params.LINE);
				pw.write("	- illimite : "+o.illimite+Params.LINE);
				pw.write("	- invocation : "+o.invocation+Params.LINE);
				pw.write("	- magique : "+o.magique+Params.LINE);
				pw.write("	- non_cach : "+o.non_cach+Params.LINE);
				pw.write("	- non_equip : "+o.non_equip+Params.LINE);
				pw.write("	- non_jet : "+o.non_jet+Params.LINE);
				pw.write("	- non_rebut : "+o.non_rebut+Params.LINE);
				pw.write("	- non_mul : "+o.non_mul+Params.LINE);
				pw.write("	- non_vol : "+o.non_vol+Params.LINE);
				pw.write("	- potion : "+o.potion+Params.LINE);
				pw.write("	- rebut : "+o.rebut+Params.LINE);
				pw.write("	- signe : "+o.signe+Params.LINE);
				pw.write("	- unique : "+o.unique+Params.LINE);
				pw.write("	- unknown : "+o.unknown+Params.LINE);
				pw.write("	- unknown2 : "+o.unknown2+Params.LINE);
				pw.write("	- nb_grpITEM : "+o.nb_grpITEM+Params.LINE);
				for (int i=0 ; i<o.nb_grpITEM ; i++){
					pw.write("		- Groupe "+i);
					for (int j=0 ; j<o.grpITEM.get(i).nb_objets ; j++){
						pw.write("			- Objet : "+o.grpITEM.get(i).objets.get(j).id);						
					}
				}
				pw.write("	- nb_boost : "+o.nb_boost+Params.LINE);
				for (int i=0 ; i<o.nb_boost ; i++){
					pw.write("		- Boost : "+o.boosts.get(i).id+Params.LINE);						
					pw.write("		- INT requis : "+o.boosts.get(i).reqINT+Params.LINE);						
					pw.write("		- SAG requis : "+o.boosts.get(i).reqSAG+Params.LINE);						
					pw.write("		- Stat : "+o.boosts.get(i).stat+Params.LINE);
					pw.write("		- Valeur : "+o.boosts.get(i).valeur+Params.LINE);						
				}
				pw.write("	- nb_sorts : "+o.nb_sorts+Params.LINE);
				for (int i=0 ; i<o.nb_sorts ; i++){
					pw.write("		- Sort : "+o.sorts.get(i).id+Params.LINE);						
					pw.write("		- LVL requis : "+o.sorts.get(i).reqLVL+Params.LINE);						
					pw.write("		- Enclenchement : "+o.sorts.get(i).enclenchement+Params.LINE);						
				}
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- ITEM écrite : "+Params.t4cOUT+"ITEMS/"+wda.getName()+"_"+o.nom+".txt");
			Params.nb_item++;	*/
		}
		
		
		int nb_pos_item;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_pos_item = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de positions d' objets : "+nb_pos_item);
		
		for (int i = 0 ; i<nb_pos_item ; i++){
			item_pos.add(new ITEMPOS(buf));
		}
		
		Iterator<ITEMPOS> iter_item_pos = item_pos.iterator();
		while(iter_item_pos.hasNext()){
			ITEMPOS p = iter_item_pos.next();
			/*File f = new File (Params.t4cOUT+"ITEM_POS/"+p.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"ITEM_POS/"+f.getName()),Params.CHARSET);
				pw.write("ITEM : "+p.nom+Params.LINE);
				pw.write("X : "+p.x+Params.LINE);
				pw.write("Y : "+p.y+Params.LINE);
				pw.write("Numéro de carte : "+p.num_carte+Params.LINE);
				pw.close();
			}
			catch(IOException exc){
				System.err.println("Erreur I/O");
				exc.printStackTrace();
				Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
				while (iter.hasNext()){
					System.err.println(iter.next());
				}
			}
			logger.info("	- ITEM_POS écrite : "+Params.t4cOUT+"ITEM_POS/"+p.nom+".txt");
			Params.nb_item_pos++;*/
		}
	}
	
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//												PNJ															  //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private void extractPNJ(ByteBuffer buf) {
		int nb_pnj;
		byte b1,b2,b3,b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_pnj = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("		- Définition du nombre de PNJ : "+nb_pnj);
		
		for (int i=0; i<nb_pnj ; i++){
			pnjs.add(new PNJ(buf));
		}
	}
}

class PNJ_sell_item{
	private int taille_id;
	String id="";
	int prix;
	public PNJ_sell_item(ByteBuffer buf){
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_id ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id += s;
		}
		//logger.info("					- id : "+id);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		prix = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- Vend: "+id+" : "+prix);
	}
}

class PNJ_buy_type{
	int id;
	int prix_min;
	int prix_max;
	public PNJ_buy_type(ByteBuffer buf){
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- id: "+id);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		prix_min = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- prix_min: "+prix_min);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		prix_max = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- achète: "+id+" "+prix_min+"/"+prix_max);
	}
}

class PNJ_train{
	int id;
	int points_max;
	int prix;
	public PNJ_train(ByteBuffer buf){
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- id: "+id);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		points_max = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- points_max: "+points_max);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		prix = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- prix: "+prix);
	}
}

class PNJ_learn{
	int id;
	int cout_pc;
	int prix;
	public PNJ_learn(ByteBuffer buf){
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- id: "+id);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		cout_pc = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- cout_pc: "+cout_pc);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		prix = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- prix: "+prix);
	}
}

class Keyword{
	
	private int taille_nom;
	String nom="";

	public Keyword(ByteBuffer buf){
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- capacité buffer: "+buf.position()+"/"+buf.capacity());
		for (int i=0 ; i<taille_nom ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nom += s;
		}
		//logger.info("						- Nom : "+nom);
	}
}

class PNJ_replique{
	int inconnu;
	byte type;//(0 = normale, 1 = texte initial, 2 = par défaut)
	int keyword_rel;//(1 = ET, 2 = OU, 3 = ordonné)
	int nb_keyword;
	ArrayList<Keyword> keywords = new ArrayList<Keyword>();
	ArrayList<PNJ_instruction> instructions = new ArrayList<PNJ_instruction>();
	
	public PNJ_replique(ByteBuffer buf){
		byte b1,b2,b3,b4;

		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		inconnu = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- inconnu: "+inconnu);
		
		type = buf.get();
		//if (type == 0)logger.info("					- type: Normale");
		//if (type == 1)logger.info("					- type: Initiale");
		//if (type == 2)logger.info("					- type: Par défaut");

		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		keyword_rel = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//if (keyword_rel == 1)logger.info("					- Relation entre les mot-clés : ET");
		//if (keyword_rel == 2)logger.info("					- Relation entre les mot-clés : OU");
		//if (keyword_rel == 3)logger.info("					- Relation entre les mot-clés : Ordonnée");
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_keyword = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- Nombre de mot-clés : "+nb_keyword);

		for (int j=0 ; j<nb_keyword ; j++){
			keywords.add(new Keyword(buf));
		}

		extractInstructions(buf);

	}

	public PNJ_replique() {
		// TODO Auto-generated constructor stub
	}

	void extractInstructions(ByteBuffer buf) {
		int inconnu1;
		int inconnu2;
		int nb_instructions;
		
		byte b1,b2,b3,b4;

		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		inconnu1 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- inconnu1: "+inconnu1);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		inconnu2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- inconnu2: "+inconnu2);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_instructions = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- Nombre d'instructions : "+nb_instructions);
		
		for (int i=0 ; i<nb_instructions ; i++){
			instructions.add(new PNJ_instruction(buf, this));
		}
	}
}

class PNJ_instruction{
	
	private static Logger logger = LogManager.getLogger(PNJ_instruction.class.getSimpleName());

	/*
	 *  Valeur     Structure   Rôle
================================================================================================
2 (02000000)    IF      Evaluation d'expression et branchement conditionnel
5 (05000000)    FOR     Répétition d'un bloc un nombre défini de fois
6 (06000000)    WHILE       Répétition d'un bloc tant qu'une condition évaluée est vraie

Cas spécial:
20 (14000000)   ASSIGNATION Assignation d'une valeur à une variable temporaire

Instructions originales présentes dans la version 1.25 de Vircom 
Valeur      Nom de la commande  Params  Paramètre 1    Paramètre 2    Paramètre 3
================================================================================================
7 (07000000)    GiveItem        1   ID de l'objet
8 (08000000)    GiveXP          1   Formule
9 (09000000)    SetFlag         2   ID du flag  Formule
10 (0A000000)   HealPlayer      1   Formule
11 (0B000000)   SayText         1   Texte
12 (0C000000)   BreakConversation   0
13 (0D000000)   FightPlayer     0
14 (0E000000)   TakeItem        1   ID de l'objet
15 (0F000000)   Teleport        3   Position X  Position Y  Monde
16 (10000000)   CastSpell       1   ID du sort
17 (11000000)   CastSpellOnNPC      1   ID du sort
18 (12000000)   GiveGold        1   Formule
19 (13000000)   TakeGold        1   Formule
21 (15000000)   DisplayItemsSoldByNPC   0
22 (16000000)   DisplayItemsBoughtByNPC 0
23 (17000000)   DisplayTrainedSkills    0
24 (18000000)   DisplayTaughtSkills 0

Additions de Dialsoft pour la V2 
Valeur      Nom de la commande  Params  Paramètre 1    Paramètre 2    Paramètre 3
================================================================================================
25 (19000000)   PrivateSysMessage   1   Texte
26 (1A000000)   GlobalSysMessage    1   Texte
27 (1B000000)   ShoutMessage        2   Nom du canal    Message
28 (1E000000)   SetGlobalFlag       2   ID du flag  Formule
29 (1F000000)   GiveKarma       1   Formule
	 */
	final byte IF = 0x02;//OK
	final byte FOR = 0x05;
	final byte WHILE = 0x06;
	final byte ASSIGNATION = 0x14;
	final byte GiveItem = 0x07;//OK
	final byte GiveXP = 0x08;//OK
	final byte SetFlag = 0x09;//OK
	final byte HealPlayer = 0x0A;
	final byte SayText = 0x0B;//OK
	final byte BreakConversation = 0x0C;//OK
	final byte FightPlayer = 0x0D;//OK
	final byte TakeItem = 0x0E;
	final byte Teleport = 0x0F;//OK
	final byte CastSpell = 0x10;//OK
	final byte CastSpellOnNPC = 0x11;//OK
	final byte GiveGold = 0x12;//OK
	final byte TakeGold = 0x13;
	final byte DisplayItemsSoldByNPC = 0x15;//OK
	final byte DisplayItemsBoughtByNPC = 0x16;//OK
	final byte DisplayTrainedSkills = 0x17;//OK
	final byte DisplayTaughtSkills = 0x18;//OK
	final byte PrivateSysMessage = 0x19;
	final byte GlobalSysMessage = 0x1A;
	final byte ShoutMessage = 0x1B;//OK
	final byte SetGlobalFlag = 0x1E;
	final byte GiveKarma = 0x1F;
	
	byte ID;
	
	int param;
	private int taille_texte;
	String texte = "";
	
	int inconnu1;
	String condition="";
	private int taille_condition;
	byte cas_general;
	int nb_cas;
	private int taille_cas;

	
	public PNJ_instruction(ByteBuffer buf, PNJ_replique r) {
		
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		ID = (byte) (b1);
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));

		switch(ID){
		case SayText : sayText(buf);
		break;
		case DisplayItemsSoldByNPC : displayItemsSoldByNPC(buf);
		break;
		case BreakConversation : breakConversation(buf);
		break;
		case DisplayItemsBoughtByNPC : displayItemsBoughtByNPC(buf);
		break;
		case IF : iF(buf, r);
		return;
		case ShoutMessage : shoutMessage(buf);
		break;
		case GiveItem : giveItem(buf);
		break;
		case GiveXP : giveXP(buf);
		break;
		case GiveGold : giveGold(buf);
		break;
		case SetFlag : setFlag(buf);
		break;
		case Teleport : teleport(buf);
		break;
		case DisplayTaughtSkills : displayTaughtSkills(buf);
		break;
		case DisplayTrainedSkills : displayTrainedSkills(buf);
		break;
		case FightPlayer : fightPlayer(buf);
		break;
		case CastSpellOnNPC : castSpellOnNPC(buf);
		break;
		case CastSpell : castSpell(buf);
		break;
		}
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		inconnu1 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- FIN D'INSTRUCTION");
	}

	private void castSpell(ByteBuffer buf) {
		int taille,nb_param;
		String id="";
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_param = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- Donner item : "+id);
		for (int i=0 ; i<taille ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id += s;
		}
		//logger.info("						- Jeter le sort : "+id);		
	}

	private void castSpellOnNPC(ByteBuffer buf) {
		int taille,nb_param;
		String id="";
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_param = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- Donner item : "+id);
		for (int i=0 ; i<taille ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id += s;
		}
		//logger.info("						- Jeter le sort sur le NPC: "+id);		
	}

	private void fightPlayer(ByteBuffer buf) {
		//nb_param == 0;
		buf.get();
		buf.get();
		buf.get();
		buf.get();
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		logger.info("						- Combattre.");		
	}

	private void displayTrainedSkills(ByteBuffer buf) {
		//nb_param == 0;
		buf.get();
		buf.get();
		buf.get();
		buf.get();
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		logger.info("						- Faire apparaître l'écran d'entrainement de compétences.");
	}

	private void displayTaughtSkills(ByteBuffer buf) {
		//nb_param == 0;
		buf.get();
		buf.get();
		buf.get();
		buf.get();
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		logger.info("						- Faire apparaître l'écran d'apprentissage de compétences.");		
	}

	private void teleport(ByteBuffer buf) {
		int taille,nb_param;
		String id="";
		String id2="";
		String id3="";
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_param = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- Donner item : "+id);
		for (int i=0 ; i<taille ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id += s;
		}
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- Donner item : "+id);
		for (int i=0 ; i<taille ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id2 += s;
		}
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- Donner item : "+taille);
		for (int i=0 ; i<taille ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id3 += s;
		}
		//logger.info("						- Téléporter en (X,Y,MONDE) "+id+","+id2+","+id3);
	}

	private void setFlag(ByteBuffer buf) {
		int taille,nb_param;
		String id="";
		String id2="";
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_param = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- Donner item : "+id);
		for (int i=0 ; i<taille ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id += s;
		}
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- Donner item : "+taille);
		for (int i=0 ; i<taille ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id2 += s;
		}
		//logger.info("						- FLAG "+id+" : "+id2);		
	}

	private void giveGold(ByteBuffer buf) {
		int taille,nb_param;
		String id="";
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_param = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- Donner item : "+id);
		for (int i=0 ; i<taille ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id += s;
		}
		//logger.info("						- Donner OR : "+id);		
	}

	private void giveXP(ByteBuffer buf) {
		int taille,nb_param;
		String id="";
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_param = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- Donner item : "+id);
		for (int i=0 ; i<taille ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id += s;
		}
		//logger.info("						- Donner XP : "+id);		
	}

	private void giveItem(ByteBuffer buf) {
		int taille,nb_param;
		String id="";
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_param = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- Donner item : "+id);
		for (int i=0 ; i<taille ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id += s;
		}
		//logger.info("						- Donner ITEM : "+id);
	}

	private void shoutMessage(ByteBuffer buf) {
		int nb_params;
		String param1 ="";
		int taille_param1;
		String param2 ="";
		int taille_param2;
		byte b1,b2,b3,b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_params = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- nb_params: "+nb_params);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_param1 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_param1 ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			param1 += s;
		}
		//logger.info("						- CC : "+param1);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_param2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_param2 ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			param2 += s;
		}
		//logger.info("						- Crier : "+param2+" sur CC : "+param1);

	}

	private void iF(ByteBuffer buf, PNJ_replique r) {
		byte b1,b2,b3,b4;
		String cas ="";
		String condition="";
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		int taille_condition = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_condition ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			condition += s;
		}
		//logger.info("						- IF : "+condition);
		int cas_general = buf.get();//ELSE
		if (cas_general == 1) r.extractInstructions(buf);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		int nb_cas = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- nb_cas: "+nb_cas);
		
		for (int i=0 ; i<nb_cas ; i++){
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			int taille_cas = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("						- taille_cas: "+taille_cas);
			for (int j=0 ; j<taille_cas ; j++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				cas += s;
			}
			//logger.info("						- cas: "+cas);
			cas ="";
			r.extractInstructions(buf);
		}
		//logger.info("						- ENDIF");
		r.extractInstructions(buf);
	}

	private void displayItemsBoughtByNPC(ByteBuffer buf) {
		//nb_param == 0;
		buf.get();
		buf.get();
		buf.get();
		buf.get();
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		//logger.info("						- Faire apparaître le magasin (ce que le PNJ achète).");		
	}

	private void breakConversation(ByteBuffer buf) {
		//nb_param == 0;
		buf.get();
		buf.get();
		buf.get();
		buf.get();
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		//logger.info("						- Fin de la conversation.");		
	}

	private void displayItemsSoldByNPC(ByteBuffer buf) {
		//nb_param == 0;
		buf.get();
		buf.get();
		buf.get();
		buf.get();
		//logger.info("						- ID: "+tools.ByteArrayToHexString.print(new byte[]{ID}));
		//logger.info("						- Faire apparaître le magasin (ce que le PNJ vend).");
	}

	private void sayText(ByteBuffer buf) {
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		param = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("						- param: "+param);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();

		taille_texte = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom: "+taille_texte);
		if (taille_texte == 1) return;
		for (int i=0 ; i<taille_texte ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			texte += s;
		}
		//logger.info("						- Dire : "+texte);
	}
	
}

class PNJ{
	private int taille_nom;
	String nom="";
	String apparence="";
	private int taille_apparence;
	int unknown;
	String id="";
	private int taille_id;
	int nb_sell_items;
	int nb_buy_types;
	int nb_train;
	int nb_learn;
	
	ArrayList<PNJ_sell_item> sell_items = new ArrayList<PNJ_sell_item>();
	ArrayList<PNJ_buy_type> buy_types = new ArrayList<PNJ_buy_type>();
	ArrayList<PNJ_train> entrainements = new ArrayList<PNJ_train>();
	ArrayList<PNJ_learn> enseignements = new ArrayList<PNJ_learn>();
	ArrayList<PNJ_replique> repliques = new ArrayList<PNJ_replique>();

	int inconnu1;
	int inconnu2;
	int nb_repliques;

	public PNJ(ByteBuffer buf){
		byte b1,b2,b3,b4;
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_nom ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nom += s;
		}
		//logger.info("				- Nom : "+nom);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_id ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id += s;
		}
		//logger.info("				- id : "+id);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		unknown = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- unknown: "+unknown);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_apparence = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_apparence ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			apparence += s;
		}
		//logger.info("				- apparence : "+apparence);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_sell_items = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Nombre d'ITEM vendues: "+nb_sell_items);
		
		for (int i=0 ; i<nb_sell_items ; i++){
			sell_items.add(new PNJ_sell_item(buf));
		}
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_buy_types = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Nombre de types d'ITEM achetés: "+nb_buy_types);
		
		for (int i=0 ; i<nb_buy_types ; i++){
			buy_types.add(new PNJ_buy_type(buf));
		}
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_train = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Nombre de compétences entraînées: "+nb_train);
		
		for (int i=0 ; i<nb_train ; i++){
			entrainements.add(new PNJ_train(buf));
		}
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_learn = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Nombre de compétences apprises: "+nb_learn);
		
		for (int i=0 ; i<nb_learn ; i++){
			enseignements.add(new PNJ_learn(buf));
		}
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		inconnu1 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- inconnu1: "+inconnu1);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		inconnu2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- inconnu2: "+inconnu2);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_repliques = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Nombre de répliques: "+nb_repliques);
		
		for (int i=0 ; i<nb_repliques ; i++){
			repliques.add(new PNJ_replique(buf));
		}
		
	}
}

