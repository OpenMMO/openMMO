package t4cPlugin;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import tools.BitBuffer;

public class ITEM {
	
	String ID = "";
	String nom = "";
	String degats = "";
	String nomSerrure = "";
	String texteSigne = "";
	String recup = "";
	String emplacement = "";

	private int taille_emplacement;
	private int taille_recup;
	private int taille_ID;
	private int taille_nom;
	private int taille_degats;
	private int taille_nomSerrure;
	private int taille_texteSigne;
	int IDnum;
	int structure;
	int IDgfx;
	int posEquip;
	int sellPrice;
	int poids;
	int malusEsquive;
	int reqEND;
	int reqFOR;
	int reqATT;
	int reqAGI;
	int difficulteSerrure;
	int OR;
	int respawnGlobal;
	int respawn;
	int radiance;
	int charges;
	int reqINT;
	int reqSAG;
	int nb_grpITEM;
	int nb_boost;
	int nb_sorts;
	
	byte bitmask;
	byte bitmask2;
	boolean unique = false;
	boolean invocation = false;
	boolean arc = false;
	boolean illimite = false;
	
	//Bitmask (type d'objet: ???|rebut|bijou|potion|magique|signe|armure|arme)
	boolean unknown = false;
	boolean rebut = false;
	boolean bijou = false;
	boolean potion = false;
	boolean magique = false;
	boolean signe = false;
	boolean armure = false;
	boolean arme = false;
	//Bitmask2 Non-mul|détr.renaiss|non-cach|non-équip|non-rebut|non-vol|???|non-jet
	boolean non_mul = false;
	boolean detr_renaiss = false;
	boolean non_cach = false;
	boolean non_equip = false;
	boolean non_rebut = false;
	boolean non_vol = false;
	boolean unknown2 = false;
	boolean non_jet = false;
	
	private long CA;

	ArrayList<GRPITEM> grpITEM = new ArrayList<GRPITEM>();
	ArrayList<Boost> boosts = new ArrayList<Boost>();
	ArrayList<Sort> sorts = new ArrayList<Sort>();

	
	public ITEM(ByteBuffer buf){
		
		byte b1,b2,b3,b4,b5,b6,b7,b8;
		
		//ID
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_ID = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille de l'ID: "+taille_ID);
		for (int i=0 ; i<taille_ID ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			ID += s;
		}
		//System.out.println("				- ID : "+ID);
		
		//IDnum
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		IDnum = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- IDnum : "+IDnum);
		
		//Structure
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		structure = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Structure : "+structure);
		
		//Nom
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_nom ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nom += s;
		}
		//System.out.println("				- Nom : "+nom);
		
		//IDgfx
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		IDnum = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- IDgfx : "+IDgfx);
		
		//Bitmask (type d'objet: ???|rebut|bijou|potion|magique|signe|armure|arme)
		bitmask = buf.get();
		BitBuffer bits = new BitBuffer(new byte[]{bitmask});
		int tmp = bits.getBits(1);
		if (tmp == 1) unknown = true;
		//System.out.println("				- Unknown : "+unknown);
		tmp = bits.getBits(1);
		if (tmp == 1) rebut = true;
		//System.out.println("				- Rebut : "+rebut);
		tmp = bits.getBits(1);
		if (tmp == 1) bijou = true;
		//System.out.println("				- Bijou : "+bijou);
		tmp = bits.getBits(1);
		if (tmp == 1) potion = true;
		//System.out.println("				- Potion : "+potion);
		tmp = bits.getBits(1);
		if (tmp == 1) magique = true;
		//System.out.println("				- Magique : "+magique);
		tmp = bits.getBits(1);
		if (tmp == 1) signe = true;
		//System.out.println("				- Signe : "+signe);
		tmp = bits.getBits(1);
		if (tmp == 1) armure = true;
		//System.out.println("				- Armure : "+armure);
		tmp = bits.getBits(1);
		if (tmp == 1) arme = true;
		//System.out.println("				- Arme : "+arme);
		
		//3 bytes non utilisés, toujours à 0
		buf.get();
		buf.get();
		buf.get();
		
		//PosEquip position dans l'équipement
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		posEquip = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- PosEquip : "+posEquip);

		//SellPrice
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		sellPrice = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Sell Price : "+sellPrice);

		//Poids
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		poids = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Poids : "+poids);
	
		//CA
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		b5 = buf.get();
		b6 = buf.get();
		b7 = buf.get();
		b8 = buf.get();
		CA = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
		//System.out.println("				- CA : "+CA);		
		
		//MalusEsquive
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		malusEsquive = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Malus d'esquive : "+malusEsquive);
		
		//ReqEND
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqEND = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- END requis : "+reqEND);
		
		//Dégats
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_degats = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille de la formule des dégats: "+taille_degats);
		for (int i=0 ; i<taille_degats ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			degats += s;
		}
		//System.out.println("				- Formule des dégats : "+degats);
		
		//ReqATT
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqATT = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- ATT requis : "+reqATT);
		
		//ReqFOR
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqFOR = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- FOR requis : "+reqFOR);
		
		//ReqAGI
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqAGI = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- AGI requis : "+reqAGI);
		
		//nom de l'objet serrure
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nomSerrure = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille du nom de l'objet serrure: "+taille_degats);
		for (int i=0 ; i<taille_nomSerrure ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nomSerrure += s;
		}
		//System.out.println("				- Nom de l'objet serrure : "+nomSerrure);
		
		//Difficulté de la serrure
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		difficulteSerrure = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Difficulté de la serrure : "+difficulteSerrure);
		
		//texte du signe
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_texteSigne = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille du texte du signe: "+taille_texteSigne);
		for (int i=0 ; i<taille_texteSigne ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			texteSigne += s;
		}
		//System.out.println("				- Texte du signe : "+texteSigne);
		
		//OR contenu
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		OR = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- OR contenu : "+OR);
		
		//Respawn global
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		respawnGlobal = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Temps de Respawn Global : "+respawnGlobal);
		
		//Respawn
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		respawn = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Temps de Respawn : "+respawn);
		
		//temps de récupération
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_recup = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille du temps de récupération: "+taille_recup);
		for (int i=0 ; i<taille_recup ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			recup += s;
		}
		//System.out.println("				- Temps de récupération : "+recup);
		
		//Radiance
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		radiance = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Radiance : "+radiance);
		
		//Charges
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		charges = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Nombre de charges : "+charges);
		
		//reqINT
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqINT = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- INT requis : "+reqINT);
		
		//reqSAG
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqSAG = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- SAG requis : "+reqSAG);
		
		//byte libre, toujours 0.
		buf.get();
		buf.get();
		buf.get();
		buf.get();
		
		//Bitmask2 Non-mul|détr.renaiss|non-cach|non-équip|non-rebut|non-vol|???|non-jet

		bitmask2 = buf.get();
		//System.out.println("				- Bitmask 2 : "+bitmask2);
		bits = new BitBuffer(new byte[]{bitmask2});
		tmp = bits.getBits(1);
		if (tmp == 1) non_mul = true;
		//System.out.println("				- non mul??? : "+non_mul);
		tmp = bits.getBits(1);
		if (tmp == 1) detr_renaiss = true;
		//System.out.println("				- détruit à la renaissance : "+detr_renaiss);
		tmp = bits.getBits(1);
		if (tmp == 1) non_cach = true;
		//System.out.println("				- non cachable : "+non_cach);
		tmp = bits.getBits(1);
		if (tmp == 1) non_equip = true;
		//System.out.println("				- non équipable : "+non_equip);
		tmp = bits.getBits(1);
		if (tmp == 1) non_rebut = true;
		//System.out.println("				- non rebutable : "+non_rebut);
		tmp = bits.getBits(1);
		if (tmp == 1) non_vol = true;
		//System.out.println("				- non volable : "+non_vol);
		tmp = bits.getBits(1);
		if (tmp == 1) unknown2 = true;
		//System.out.println("				- unknown2 : "+unknown2);
		tmp = bits.getBits(1);
		if (tmp == 1) non_jet = true;
		//System.out.println("				- non jetable : "+non_jet);
		
		//3 bytes non utilisés, toujours à 0
		buf.get();
		buf.get();
		buf.get();
		
		//Objet unique
		byte tmp_byte;
		tmp_byte = buf.get();
		if (tmp_byte == 1) unique = true;
		//System.out.println("				- Objet unique : "+unique);
		
		//Emplacement
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_emplacement = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille de l'emplacement: "+taille_emplacement);
		for (int i=0 ; i<taille_emplacement ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			emplacement += s;
		}
		//System.out.println("				- Emplacement : "+emplacement);
		
		//Invocation
		tmp_byte = buf.get();
		if (tmp_byte == 1) invocation = true;
		//System.out.println("				- Invocation : "+invocation);
		
		//Arc
		tmp_byte = buf.get();
		if (tmp_byte == 1) arc = true;
		//System.out.println("				- Arc : "+arc);
		
		//Illimité
		tmp_byte = buf.get();
		if (tmp_byte == 1) illimite = true;
		//System.out.println("				- Illimité : "+illimite);
		
		//nombre de groupes contenus
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_grpITEM = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Nombre de groupes contenus : "+nb_grpITEM);
		
		for (int i=0 ; i<nb_grpITEM ; i++){
			grpITEM.add(new GRPITEM(buf));
		}
		
		//nombre de boosts
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_boost = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Nombre de boosts : "+nb_boost);

		for (int i=0 ; i<nb_boost ; i++){
			boosts.add(new Boost(buf));
		}
		
		//nombre de sorts
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_sorts = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Nombre de sorts : "+nb_sorts);
		
		for (int i=0 ; i<nb_sorts ; i++){
			sorts.add(new Sort(buf));
		}
		//System.out.println("");
	}
}
