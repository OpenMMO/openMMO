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
		//logger.info("				- Taille de l'ID: "+taille_ID);
		for (int i=0 ; i<taille_ID ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			ID += s;
		}
		//logger.info("				- ID : "+ID);
		
		//IDnum
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		IDnum = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- IDnum : "+IDnum);
		
		//Structure
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		structure = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Structure : "+structure);
		
		//Nom
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
		
		//IDgfx
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		IDnum = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- IDgfx : "+IDgfx);
		
		//Bitmask (type d'objet: ???|rebut|bijou|potion|magique|signe|armure|arme)
		bitmask = buf.get();
		BitBuffer bits = new BitBuffer(new byte[]{bitmask});
		int tmp = bits.getBits(1);
		if (tmp == 1) unknown = true;
		//logger.info("				- Unknown : "+unknown);
		tmp = bits.getBits(1);
		if (tmp == 1) rebut = true;
		//logger.info("				- Rebut : "+rebut);
		tmp = bits.getBits(1);
		if (tmp == 1) bijou = true;
		//logger.info("				- Bijou : "+bijou);
		tmp = bits.getBits(1);
		if (tmp == 1) potion = true;
		//logger.info("				- Potion : "+potion);
		tmp = bits.getBits(1);
		if (tmp == 1) magique = true;
		//logger.info("				- Magique : "+magique);
		tmp = bits.getBits(1);
		if (tmp == 1) signe = true;
		//logger.info("				- Signe : "+signe);
		tmp = bits.getBits(1);
		if (tmp == 1) armure = true;
		//logger.info("				- Armure : "+armure);
		tmp = bits.getBits(1);
		if (tmp == 1) arme = true;
		//logger.info("				- Arme : "+arme);
		
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
		//logger.info("				- PosEquip : "+posEquip);

		//SellPrice
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		sellPrice = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Sell Price : "+sellPrice);

		//Poids
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		poids = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Poids : "+poids);
	
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
		//logger.info("				- CA : "+CA);		
		
		//MalusEsquive
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		malusEsquive = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Malus d'esquive : "+malusEsquive);
		
		//ReqEND
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqEND = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- END requis : "+reqEND);
		
		//Dégats
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_degats = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille de la formule des dégats: "+taille_degats);
		for (int i=0 ; i<taille_degats ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			degats += s;
		}
		//logger.info("				- Formule des dégats : "+degats);
		
		//ReqATT
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqATT = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- ATT requis : "+reqATT);
		
		//ReqFOR
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqFOR = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- FOR requis : "+reqFOR);
		
		//ReqAGI
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqAGI = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- AGI requis : "+reqAGI);
		
		//nom de l'objet serrure
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nomSerrure = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom de l'objet serrure: "+taille_degats);
		for (int i=0 ; i<taille_nomSerrure ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nomSerrure += s;
		}
		//logger.info("				- Nom de l'objet serrure : "+nomSerrure);
		
		//Difficulté de la serrure
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		difficulteSerrure = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Difficulté de la serrure : "+difficulteSerrure);
		
		//texte du signe
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_texteSigne = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du texte du signe: "+taille_texteSigne);
		for (int i=0 ; i<taille_texteSigne ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			texteSigne += s;
		}
		//logger.info("				- Texte du signe : "+texteSigne);
		
		//OR contenu
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		OR = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- OR contenu : "+OR);
		
		//Respawn global
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		respawnGlobal = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Temps de Respawn Global : "+respawnGlobal);
		
		//Respawn
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		respawn = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Temps de Respawn : "+respawn);
		
		//temps de récupération
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_recup = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du temps de récupération: "+taille_recup);
		for (int i=0 ; i<taille_recup ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			recup += s;
		}
		//logger.info("				- Temps de récupération : "+recup);
		
		//Radiance
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		radiance = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Radiance : "+radiance);
		
		//Charges
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		charges = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Nombre de charges : "+charges);
		
		//reqINT
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqINT = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- INT requis : "+reqINT);
		
		//reqSAG
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqSAG = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- SAG requis : "+reqSAG);
		
		//byte libre, toujours 0.
		buf.get();
		buf.get();
		buf.get();
		buf.get();
		
		//Bitmask2 Non-mul|détr.renaiss|non-cach|non-équip|non-rebut|non-vol|???|non-jet

		bitmask2 = buf.get();
		//logger.info("				- Bitmask 2 : "+bitmask2);
		bits = new BitBuffer(new byte[]{bitmask2});
		tmp = bits.getBits(1);
		if (tmp == 1) non_mul = true;
		//logger.info("				- non mul??? : "+non_mul);
		tmp = bits.getBits(1);
		if (tmp == 1) detr_renaiss = true;
		//logger.info("				- détruit à la renaissance : "+detr_renaiss);
		tmp = bits.getBits(1);
		if (tmp == 1) non_cach = true;
		//logger.info("				- non cachable : "+non_cach);
		tmp = bits.getBits(1);
		if (tmp == 1) non_equip = true;
		//logger.info("				- non équipable : "+non_equip);
		tmp = bits.getBits(1);
		if (tmp == 1) non_rebut = true;
		//logger.info("				- non rebutable : "+non_rebut);
		tmp = bits.getBits(1);
		if (tmp == 1) non_vol = true;
		//logger.info("				- non volable : "+non_vol);
		tmp = bits.getBits(1);
		if (tmp == 1) unknown2 = true;
		//logger.info("				- unknown2 : "+unknown2);
		tmp = bits.getBits(1);
		if (tmp == 1) non_jet = true;
		//logger.info("				- non jetable : "+non_jet);
		
		//3 bytes non utilisés, toujours à 0
		buf.get();
		buf.get();
		buf.get();
		
		//Objet unique
		byte tmp_byte;
		tmp_byte = buf.get();
		if (tmp_byte == 1) unique = true;
		//logger.info("				- Objet unique : "+unique);
		
		//Emplacement
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_emplacement = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille de l'emplacement: "+taille_emplacement);
		for (int i=0 ; i<taille_emplacement ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			emplacement += s;
		}
		//logger.info("				- Emplacement : "+emplacement);
		
		//Invocation
		tmp_byte = buf.get();
		if (tmp_byte == 1) invocation = true;
		//logger.info("				- Invocation : "+invocation);
		
		//Arc
		tmp_byte = buf.get();
		if (tmp_byte == 1) arc = true;
		//logger.info("				- Arc : "+arc);
		
		//Illimité
		tmp_byte = buf.get();
		if (tmp_byte == 1) illimite = true;
		//logger.info("				- Illimité : "+illimite);
		
		//nombre de groupes contenus
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_grpITEM = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Nombre de groupes contenus : "+nb_grpITEM);
		
		for (int i=0 ; i<nb_grpITEM ; i++){
			grpITEM.add(new GRPITEM(buf));
		}
		
		//nombre de boosts
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_boost = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Nombre de boosts : "+nb_boost);

		for (int i=0 ; i<nb_boost ; i++){
			boosts.add(new Boost(buf));
		}
		
		//nombre de sorts
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_sorts = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Nombre de sorts : "+nb_sorts);
		
		for (int i=0 ; i<nb_sorts ; i++){
			sorts.add(new Sort(buf));
		}
		//logger.info("");
	}
}
