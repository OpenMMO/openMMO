package t4cPlugin;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import t4cPlugin.SPELL.spell_effect;
import t4cPlugin.SPELL.spell_effect.param_effet;


/*
 * 
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
 * 
 * [OFFSET] [TAILLE]  [DESCRIPTION]
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


Structure des fichiers WDA :





Bloc des Téléportations
=======================
4		unsigned long		Nombre de téléportations

Pour chaque téléportation:
{
	4		unsigned long		Position X de départ
	4		unsigned long		Position Y de départ
	4		unsigned long		Numéro de la carte (indice Z) de départ
	4		unsigned long		Position X d'arrivée
	4		unsigned long		Position Y d'arrivée
	4		unsigned long		Numéro de la carte (indice Z) d'arrivée
}
*/
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
			System.out.println("		- x1 : "+x1);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			y1 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- y1 : "+y1);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			num_carte1 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- num_carte1 : "+num_carte1);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			x2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- x2 : "+x2);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			y2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- y2 : "+y2);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			num_carte2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- num_carte2 : "+num_carte2);

		}
	}
/*Bloc des Relations entre clans
==============================
4		unsigned long		ID du clan la plus élevée (cf. bloc des clans)
4		unsigned long		Nombre de relations entre clans

Pour chaque relation entre clans:
{
	2		unsigned short		ID du premier clan
	2		unsigned short		ID du second clan
	2		signed short		Relation entre les clans (-100 = amour, 0 = neutre, 100 = haine)
}
*/
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
			System.out.println("		- id1 : "+id1);
			
			b1 = buf.get();
			b2 = buf.get();
			id2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
			System.out.println("		- id2 : "+id2);
			
			b1 = buf.get();
			b2 = buf.get();
			rel = tools.ByteArrayToNumber.bytesToShort(new byte[]{b2,b1});
			System.out.println("		- rel : "+rel);
		}
	}
/*
Bloc des Clans
==============
4		unsigned long		Nombre de clans

Pour chaque clan:
{
	2		unsigned short		ID du clan
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			Nom du clan
}*/
	class Clan{
		int id;
		int taille_nom;
		String nom="";
		public Clan(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
			System.out.println("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			System.out.println("					- nom : "+nom);
		}
	}
/*
Bloc des Flags
==============
4		unsigned long		Nombre de flags

Pour chaque flag:
{
	4		unsigned long		ID du flag
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			Nom du flag
}
*/
	class Flag{
		int id;
		int taille_nom;
		String nom="";
		public Flag(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			System.out.println("					- nom : "+nom);
		}
	}
/*
1		unsigned char		Présence ou non de blocs supplémentaires

Bloc des Apparences d'objets
============================
4		unsigned long		Nombre d'apparences d'objets

Pour chaque apparence d'objet:
{
	4		unsigned long		ID de l'apparence d'objet
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			Nom de l'apparence d'objet
}
*/
	class Gfx_item{
		int id;
		int taille_nom;
		String nom="";
		public Gfx_item(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			System.out.println("					- nom : "+nom);
		}
	}
/*
Bloc des Apparences de créatures
================================
4		unsigned long		Nombre d'apparences de créatures

Pour chaque apparence de créature:
{
	4		unsigned long		ID de l'apparence n°1 de la créature
	4		unsigned long		ID de l'apparence n°2 de la créature (?)
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			Nom de l'apparence d'objet
}
*/
	class Gfx_creatures{
		int id;
		int id2;
		int taille_nom;
		String nom="";
		public Gfx_creatures(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- id2??? : "+id2);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			System.out.println("					- nom : "+nom);
		}
	}
/*
Bloc des Lieux
==============
4		unsigned long		Nombre de lieux

Pour chaque lieu:
{
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			Nom du lieu
}
*/
	class Lieu{
		int taille_nom;
		String nom="";	
		public Lieu(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			System.out.println("					- nom : "+nom);
		}


	}
/*
Bloc des Icônes
===============
4		unsigned long		Nombre d'icônes

Pour chaque icône:
{
	4		unsigned long		ID de l'icône
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			Nom de l'icône
}
*/
	class Icone{
		int id;
		int taille_nom;
		String nom="";
		public Icone(ByteBuffer buf) {
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- id : "+id);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			System.out.println("		- taille_nom : "+taille_nom);
			for (int i=0 ; i<taille_nom ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				nom += s;
			}
			System.out.println("					- nom : "+nom);
		}
		
	}
	
/*
Bloc des Effets de sort
=======================
4		unsigned long		Nombre d'effets de sort

Pour chaque effet de sort:
{
	4		unsigned long		ID de l'effet de sort
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			Nom de l'effet de sort
}
 * 
 */
class Fx_spell{
	int id;
	int taille_nom;
	String nom="";
	
	public Fx_spell(ByteBuffer buf) {
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- id : "+id);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- taille_nom : "+taille_nom);
		for (int i=0 ; i<taille_nom ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nom += s;
		}
		System.out.println("					- nom : "+nom);
	}
	
}

public class WDA {
	private int marqueur = 0;//vérifier plus tard qu'il vaut 68775
	private byte type = 0; //Type de WDA (T4C Worlds(RO) = 0x01, T4C Edit(RW) = 0x00)
	private ByteBuffer buf;
	private static ArrayList<SPELL> sorts = new ArrayList<SPELL>();
	private static ArrayList<COLMAP> colmaps = new ArrayList<COLMAP>();
	private static ArrayList<ITEM> items = new ArrayList<ITEM>();
	private static ArrayList<ITEMPOS> item_pos = new ArrayList<ITEMPOS>();
	private static ArrayList<CREATURE> creatures = new ArrayList<CREATURE>();
	private static ArrayList<CREATUREBLOC> blocs_creatures = new ArrayList<CREATUREBLOC>();
	private static ArrayList<Teleportation> teleportations = new ArrayList<Teleportation>();
	private static ArrayList<Clan_relation> clan_relations = new ArrayList<Clan_relation>();
	private static ArrayList<Clan> clans = new ArrayList<Clan>();
	private static ArrayList<Flag> flags = new ArrayList<Flag>();
	private static ArrayList<Gfx_item> gfx_items = new ArrayList<Gfx_item>();
	private static ArrayList<Gfx_creatures> gfx_creatures = new ArrayList<Gfx_creatures>();
	private static ArrayList<Lieu> lieux = new ArrayList<Lieu>();
	private static ArrayList<Icone> icones = new ArrayList<Icone>();
	private static ArrayList<Fx_spell> fx_sorts = new ArrayList<Fx_spell>();

	

	public void decrypt (File wda_data){
		// Algorithme de cryptage, version C. Parcourt tout le buffer wda_data sur wda_data_size
		// octets et masque ou démasque l'encryption des données sur celui-ci.
		// Pour chaque octet de wda_data jusqu'à wda_data_size...
		int wda_data_size = (int) wda_data.length();
		buf = ByteBuffer.allocate(wda_data_size);
		try {
			System.out.println("- Lecture du fichier "+wda_data.getCanonicalPath()+" : "+(int)wda_data.length()+" octets = "+(int)wda_data.length()/1024+"Ko");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			DataInputStream in = new DataInputStream (new FileInputStream(wda_data));
			while (buf.position() < (int)wda_data.length()){
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
				System.out.println("	- Marqueur érroné : "+marqueur);
				System.exit(1);
			}else {
				System.out.println("	- Marqueur correct : "+marqueur+" : fichier PNJ");
				extractPNJ(buf);
				return;
			}
		} else{
			System.out.println("	- Marqueur correct : "+marqueur);			
		}
		type = buf.get();
		if (type == 0){
			System.out.println("	- Type de fichier WDA : T4C Edit");
			extract(buf);
		} else{
			System.out.println("	- Type de fichier WDA : T4C Worlds");
			extract(buf);
		}
	}

	private static void extract(ByteBuffer buf) {
		extractSPELL(buf);// pour les sorts
		extractMAP(buf);//pour les cartes
		extractITEM(buf);//pour les items
		extractCREATURE(buf);//pour les créatures
		extractCREATUREBLOC(buf);//pour les blocs de spawn
		extractTeleportations(buf);//pour les téléportations
		extractClanRelations(buf);//pour les relations entre les clans
		extractClans(buf);//pour les clans
		extractFlags(buf);//pour les flags
		if (buf.get() == 0) return;
		extractGfxItems(buf);//pour les apparences d'objets
		extractGfxCreatures(buf);//pour les apparences de creatures
		extractLieux(buf);//pour les lieux
		extractIcones(buf);//pour les icones
		extractFxSpells(buf);//pour les effets de sorts

	}

	private static void extractFxSpells(ByteBuffer buf) {
		int nb_fx;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_fx = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre de blocs d'effets de sorts : "+nb_fx);
		
		for (int i=0 ; i<nb_fx ; i++){
			fx_sorts.add(new Fx_spell(buf));
		}
		
		Iterator<Fx_spell> iter_fx_sorts = fx_sorts.iterator();
		while(iter_fx_sorts.hasNext()){
			Fx_spell fx = iter_fx_sorts.next();
			File f = new File (Params.t4cOUT+"SPELLFX/"+fx.nom+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		}		
	}
	
	private static void extractIcones(ByteBuffer buf) {
		int nb_icones;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_icones = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre d'icones : "+nb_icones);
		
		for (int i=0 ; i<nb_icones ; i++){
			icones.add(new Icone(buf));
		}
		
		Iterator<Icone> iter_icones = icones.iterator();
		while(iter_icones.hasNext()){
			Icone ic = iter_icones.next();
			File f = new File (Params.t4cOUT+"ICONES/"+ic.nom+".txt");
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
		}		
	}
	
	private static void extractLieux(ByteBuffer buf) {
		int nb_lieux;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_lieux = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre de lieux : "+nb_lieux);
		
		for (int i=0 ; i<nb_lieux ; i++){
			lieux.add(new Lieu(buf));
		}
		
		Iterator<Lieu> iter_lieux = lieux.iterator();
		while(iter_lieux.hasNext()){
			Lieu l = iter_lieux.next();
			File f = new File (Params.t4cOUT+"LIEUX/"+l.nom+".txt");
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
		}		
	}
	
	private static void extractGfxCreatures(ByteBuffer buf) {
		int nb_gfx_creatures;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_gfx_creatures = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre d'apparences de créatures : "+nb_gfx_creatures);
		
		for (int i=0 ; i<nb_gfx_creatures ; i++){
			gfx_creatures.add(new Gfx_creatures(buf));
		}
		
		Iterator<Gfx_creatures> iter_gfx_creatures = gfx_creatures.iterator();
		while(iter_gfx_creatures.hasNext()){
			Gfx_creatures ct = iter_gfx_creatures.next();
			File f = new File (Params.t4cOUT+"MONSTRES/GFX_"+ct.nom+".txt");
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
		}		
	}
	
	private static void extractGfxItems(ByteBuffer buf) {
		int nb_gfx_items;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_gfx_items = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre d'apparences d'objets : "+nb_gfx_items);
		
		for (int i=0 ; i<nb_gfx_items ; i++){
			gfx_items.add(new Gfx_item(buf));
		}
		
		Iterator<Gfx_item> iter_gfx_items = gfx_items.iterator();
		while(iter_gfx_items.hasNext()){
			Gfx_item it = iter_gfx_items.next();
			File f = new File (Params.t4cOUT+"ITEMS/GFX_"+it.nom+".txt");
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
		}		
	}

	
	private static void extractFlags(ByteBuffer buf) {
		int nb_flags;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_flags = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre de flags : "+nb_flags);
		
		for (int i=0 ; i<nb_flags ; i++){
			flags.add(new Flag(buf));
		}
		
		Iterator<Flag> iter_flags = flags.iterator();
		while(iter_flags.hasNext()){
			Flag fl = iter_flags.next();
			File f = new File (Params.t4cOUT+"FLAGS/"+fl.nom+".txt");
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
		}		
	}

	
	private static void extractClans(ByteBuffer buf) {
		int nb_clans;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_clans = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre de clans : "+nb_clans);
		
		for (int i=0 ; i<nb_clans ; i++){
			clans.add(new Clan(buf));
		}
		
		Iterator<Clan> iter_clans = clans.iterator();
		while(iter_clans.hasNext()){
			Clan cl = iter_clans.next();
			File f = new File (Params.t4cOUT+"CLANS/"+cl.nom+".txt");
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
		}		
	}

	
	private static void extractClanRelations(ByteBuffer buf) {
		int nb_clan_relations;
		int id_sup;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		id_sup = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition de l'id supérieure des clans : "+id_sup);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_clan_relations = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre de blocs de relations entre clans : "+nb_clan_relations);
		
		for (int i=0 ; i<nb_clan_relations ; i++){
			clan_relations.add(new Clan_relation(buf));
		}
		
		Iterator<Clan_relation> iter_clan_relations = clan_relations.iterator();
		while(iter_clan_relations.hasNext()){
			Clan_relation clr = iter_clan_relations.next();
			File f = new File (Params.t4cOUT+"CLANS/REL_"+Clan_relation.id+".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"CLANS/"+f.getName()),Params.CHARSET);
				pw.write("RELATION : "+Clan_relation.id+Params.LINE);
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
			Clan_relation.id++;
		}		
	}

	
	private static void extractTeleportations(ByteBuffer buf) {
		int nb_teleportations;;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_teleportations = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre de blocs de téléportations : "+nb_teleportations);
		
		for (int i=0 ; i<nb_teleportations ; i++){
			teleportations.add(new Teleportation(buf));
		}
		
		Iterator<Teleportation> iter_teleportations = teleportations.iterator();
		while(iter_teleportations.hasNext()){
			Teleportation t = iter_teleportations.next();
			File f = new File (Params.t4cOUT+"LIEUX/TEL_"+Teleportation.id+".txt");
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
			Teleportation.id++;
		}		
	}

/*
	 * Bloc des Groupes de créatures
=============================
4		unsigned long		Nombre de groupes de créatures


	 */
private static void extractCREATUREBLOC(ByteBuffer buf) {
	int nb_blocs;
	byte b1, b2, b3, b4;
	b1 = buf.get();
	b2 = buf.get();
	b3 = buf.get();
	b4 = buf.get();
	nb_blocs = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
	System.out.println("		- Définition du nombre de blocs de créatures : "+nb_blocs);
	
	for (int i=0 ; i<nb_blocs ; i++){
		blocs_creatures.add(new CREATUREBLOC(buf));
	}
	
	Iterator<CREATUREBLOC> iter_creature_bloc = blocs_creatures.iterator();
	while(iter_creature_bloc.hasNext()){
		CREATUREBLOC sp = iter_creature_bloc.next();
		File f = new File (Params.t4cOUT+"SPAWN/"+sp.nom+".txt");
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
	}
}

	/*
 * Bloc des Créatures
==================
4		unsigned long		Nombre de créatures

 */
	private static void extractCREATURE(ByteBuffer buf) {
		int nb_creature;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_creature = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre de créatures : "+nb_creature);
		
		for (int i=0 ; i<nb_creature ; i++){
			creatures.add(new CREATURE(buf));
		}
		
		Iterator<CREATURE> iter_creatures = creatures.iterator();
		while(iter_creatures.hasNext()){
			CREATURE c = iter_creatures.next();
			File f = new File (Params.t4cOUT+"MONSTRES/"+c.nom+".txt");
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
		}
	}

	private static void extractMAP(ByteBuffer buf) {
		int nb_map;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_map = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre de cartes de collision : "+nb_map);
		
		for (int i = 0 ; i<nb_map ; i++){
			colmaps.add(new COLMAP(buf));
		}
	}

	private static void extractSPELL(ByteBuffer buf) {
		int nb_sorts;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_sorts = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre de sorts : "+nb_sorts);
		for (int i = 0 ; i<nb_sorts ; i++){
			sorts.add(new SPELL(buf));
		}
		
		Iterator<SPELL> iter_spells = sorts.iterator();
		while(iter_spells.hasNext()){
			SPELL s = iter_spells.next();
			File f = new File (Params.t4cOUT+"SPELLS/"+s.nom+".txt");
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
		}
	}

	private static void extractITEM(ByteBuffer buf) {
		int nb_item;
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_item = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre d' objets : "+nb_item);
		
		for (int i = 0 ; i<nb_item ; i++){
			items.add(new ITEM(buf));
		}
		
		Iterator<ITEM> iter_items = items.iterator();
		while(iter_items.hasNext()){
			ITEM o = iter_items.next();
			File f = new File (Params.t4cOUT+"ITEMS/"+o.nom+".txt");
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
		}
		int nb_pos_item;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_pos_item = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		System.out.println("		- Définition du nombre de positions d' objets : "+nb_pos_item);
		
		
		for (int i = 0 ; i<nb_pos_item ; i++){
			item_pos.add(new ITEMPOS(buf));
		}
		
		Iterator<ITEMPOS> iter_item_pos = item_pos.iterator();
		while(iter_item_pos.hasNext()){
			ITEMPOS p = iter_item_pos.next();
			File f = new File (Params.t4cOUT+"ITEM_POS/"+p.nom+".txt");
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
		}
	}
	
	private static void extractPNJ(ByteBuffer buf) {
		
	}
}
