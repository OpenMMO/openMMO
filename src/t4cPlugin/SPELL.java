package t4cPlugin;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class SPELL {
	int ID = -1;
	private int taille_nom;
	String nom = "";
	private int taille_epuisement_mental;
	String epuisement_mental = "";	
	private int taille_epuisement_physique;
	String epuisement_physique = "";
	private int taille_epuisement_attaque;
	String epuisement_attaque = "";
	private int taille_duree;
	String duree = "";
	private int taille_frequence;
	String frequence = "";
	int element;
	private int taille_cout_mana;
	String cout_mana = "";
	int rayon_degats;
	int index_gfx;
	int index_gfx_cible;
	int type_cible;
	int type_attaque;//1 physique ; 2 mentale
	int req_sag;
	int req_int;
	int req_lvl;
	byte ligne_de_vue;//boolean
	byte controle_pvp;//boolean
	byte sort_attaque;//boolean
	int index_icone;
	private int taille_success_rate;
	String success_rate = "";
	private int taille_description;
	String description = "";
	int nb_effets;
	ArrayList<spell_effect> effets = new ArrayList<spell_effect>();
	
		class spell_effect{
			int type_effet;
			int nb_params;
			ArrayList<param_effet> params = new ArrayList<param_effet>();
			
			class param_effet{
				int num_param;
				private int taille_param;
				String param = "";
				
				public param_effet(){};
			}
			
			public spell_effect(){};
			
			public param_effet addParam(ByteBuffer buf) {
				param_effet param = new param_effet();
				byte b1,b2,b3,b4;
				//num_param
				b1 = buf.get();
				b2 = buf.get();
				b3 = buf.get();
				b4 = buf.get();
				param.num_param = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
				//logger.info("					- Paramètre n° : "+param.num_param);
				
				//Param
				b1 = buf.get();
				b2 = buf.get();
				b3 = buf.get();
				b4 = buf.get();
				param.taille_param = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
				//logger.info("				- Taille du paramètre: "+param.taille_param);
				for (int i=0 ; i<param.taille_param ; i++){
					b1 = buf.get();
					String s = new String(new byte[]{b1});
					param.param += s;
				}
				//logger.info("					- Paramètre : "+param.param);
				
				return param;
			}
		}
		
	int nb_req_spells;
	
	ArrayList<Integer> req_spells = new ArrayList<Integer>();
	
	public SPELL(ByteBuffer buf){
		//logger.info("		- Extraction d'un sort :");
		byte b1, b2, b3, b4;
		
		//ID
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.ID = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- ID : "+ID);
		
		//NOM
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_nom ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nom += s;
		}
		//logger.info("			- Nom : "+nom);
		//épuisement mental
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.taille_epuisement_mental = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille de l'épuisement mental: "+taille_epuisement_mental);
		for (int i=0 ; i<taille_epuisement_mental ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			epuisement_mental += s;
		}
		//logger.info("			- Épuisement mental : "+epuisement_mental);
		
		//épuisement physique
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.taille_epuisement_physique = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille de l'épuisement physique: "+taille_epuisement_mental);
		for (int i=0 ; i<taille_epuisement_physique ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			epuisement_physique += s;
		}
		//logger.info("			- Épuisement physique : "+epuisement_physique);
		
		//épuisement attaque
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.taille_epuisement_attaque = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille de l'épuisement d'attaque : "+taille_epuisement_attaque);
		for (int i=0 ; i<taille_epuisement_attaque ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			epuisement_attaque += s;
		}
		//logger.info("			- Épuisement d'attaque : "+epuisement_attaque);
		
		
		//duree
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.taille_duree = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille de la durée : "+taille_duree);
		for (int i=0 ; i<taille_duree ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			duree += s;
		}
		//logger.info("			- Durée : "+duree);
	
		//fréquence
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.taille_frequence = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille de la fréquence : "+taille_frequence);
		for (int i=0 ; i<taille_frequence ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			frequence += s;
		}
		//logger.info("			- Fréquence : "+frequence);
	
		//élément
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.element = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Élément : "+element);
		
		
		//cout mana
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.taille_cout_mana = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du cout mana : "+taille_cout_mana);
		for (int i=0 ; i<taille_cout_mana ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			cout_mana += s;
		}
		//logger.info("			- Cout en Mana : "+cout_mana);
	
		//rayon dégats
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.rayon_degats = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Rayon dégats : "+rayon_degats);
		
		//index_gfx
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.index_gfx = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Index GFX : "+index_gfx);
		
		//index_gfx_cible
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.index_gfx_cible = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Index GFX cible : "+index_gfx_cible);
		
		//type_cible
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.type_cible = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Type de cible : "+type_cible);
		
		//type_attaque
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.type_attaque = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Type d'attaque : "+type_attaque);
		
		//req_sag
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.req_sag = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Sagesse requise : "+req_sag);
		
		//req_int
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.req_int = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Intellect requis : "+req_int);
		
		//req_lvl
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.req_lvl = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- LVL requis : "+req_lvl);
	
		//ligne de vue
		ligne_de_vue = buf.get();
		if (ligne_de_vue == 0){
			//logger.info("			- Ligne de vue : FALSE");
		}else{
			//logger.info("			- Ligne de vue : TRUE");
		}
		
		//controle pvp
		controle_pvp = buf.get();
		if (controle_pvp == 0){
			//logger.info("			- Controle PVP : FALSE");
		}else{
			//logger.info("			- Controle PVP : TRUE");
		}
		
		//sort attaque
		sort_attaque = buf.get();
		if (sort_attaque == 0){
			//logger.info("			- Sort d'attaque : FALSE");
		}else{
			//logger.info("			- Sort d'attaque : TRUE");
		}
		
		//index_icone
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.index_icone = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Index icône : "+index_icone);
		
		//success rate
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.taille_success_rate = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille du taux de succès : "+taille_success_rate);
		for (int i=0 ; i<taille_success_rate ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			success_rate += s;
		}
		//logger.info("			- Taux de succès : "+success_rate);
		
		//description
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.taille_description = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille de la description: "+taille_description);
		for (int i=0 ; i<taille_description ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			description += s;
		}
		//logger.info("			- Description : "+description);
		
		//nb_effets
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.nb_effets = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Nombre d'effets : "+nb_effets);
		
		//effets
		for (int i = 0 ; i<nb_effets ; i++){
			//logger.info("			- Effet n° : "+nb_effets);
			effets.add(addEffect(buf));
		}
		
		//nb_req_spells
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		this.nb_req_spells= tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Nombre de sorts requis : "+nb_req_spells);
		
		for (int i=0 ; i<nb_req_spells ; i++){
			//nb_req_spells
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			req_spells.add(tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1}));
			//logger.info("				- ID du sort requis : "+req_spells.get(i));
		} 
		//logger.info("		- Fin du Sort");
		//logger.info("");
	}

	private spell_effect addEffect(ByteBuffer buf) {
		spell_effect fx = new spell_effect();
		byte b1,b2,b3,b4;
		//type_effet
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		fx.type_effet = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Type : "+fx.type_effet);
		
		//nb_params
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		fx.nb_params = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- Nombre de paramètres : "+fx.nb_params);
		
		//Params
		for (int i= 0 ; i<fx.nb_params ; i++){
			fx.params.add(fx.addParam(buf));
		}
		return fx;
	}

	
}