package t4cPlugin;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/*
 * Pour chaque créature:
{
	4		unsigned long		ID numérique (gamme 1 - LONG_MAX)
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			ID de la créature
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			Nom de la créature
	4		unsigned long		Force
	4		unsigned long		Endurance
	4		unsigned long		Agilité
	4		unsigned long		Intelligence
	4		unsigned long		??? (Volonté, toujours 0)
	4		unsigned long		Sagesse
	4		unsigned long		??? (Chance, toujours 0)
	4		unsigned long		Résistance Air
	4		unsigned long		Résistance Terre
	4		unsigned long		Résistance Eau
	4		unsigned long		Résistance Feu
	4		unsigned long		Résistance Nécromancie
	4		unsigned long		Résistance Lumière
	4		unsigned long		Puissance Air
	4		unsigned long		Puissance Terre
	4		unsigned long		Puissance Eau
	4		unsigned long		Puissance Feu
	4		unsigned long		Puissance Nécromancie
	4		unsigned long		Puissance Lumière
	4		unsigned long		Niveau
	4		unsigned long		Points de vie (HP)
	4		unsigned long		Esquive
	8		double			Classe d'armure
	4		unsigned long		Apparence
	4		unsigned long		ID d'objet (vêtement corps)
	4		unsigned long		ID d'objet (vêtement pieds)
	4		unsigned long		ID d'objet (vêtement mains)
	4		unsigned long		ID d'objet (vêtement tête)
	4		unsigned long		ID d'objet (vêtement jambes)
	4		unsigned long		ID d'objet (arme)
	4		unsigned long		ID d'objet (bouclier)
	4		unsigned long		ID d'objet (vêtement dos)
	4		unsigned long		Agressivité
	4		unsigned long		ID du clan de la créature
	4		unsigned long		??? (Vitesse, toujours 0)
	8		double			Expérience reçue par coup porté
	8		double			Expérience reçue à la mort de la créature
	4		unsigned long		Minimum de pièces d'or abandonné par la créature lors de sa mort
	4		unsigned long		Maximum de pièces d'or abandonné par la créature lors de sa mort
	1		bool			Peut attaquer
	4		unsigned long		Nombre de types d'attaque

	Pour chaque type d'attaque:
	{
		4		unsigned long		Taille de la chaîne de caractères suivante
		(variable)	char *			Formule de dégâts
		4		unsigned long		Niveau d'attaque (0 pour les sorts)
		4		unsigned long		Pourcentage de succès
		4		unsigned long		ID du sort lié à l'attaque
		4		unsigned long		Distance minimale de lancement du sort
		4		unsigned long		Distance maximale de lancement du sort
	}

	4		unsigned long		Nombre de flags donnés à la mort de la créature

	Pour chaque flag donné à la mort de la créature:
	{
		4		unsigned long		ID du flag
		4		unsigned long		Valeur du flag
		1		bool			Incrémenter la valeur précédente
	}

	4		unsigned long		Nombre d'objets abandonnés par la créature à sa mort

	Pour chaque objet abandonné par la créature à sa mort:
	{
		4		unsigned long		ID de l'objet abandonné
		8		double			Pourcentage de chances que cet objet soit abandonné
	}
}

 */

public class CREATURE {
	int IDnum;
	private int taille_ID;
	private int taille_nom;
	int force;
	int endurance;
	int agilite;
	int intelligence;
	int volonte;// (Volonté, toujours 0)
	int sagesse;
	int chance;// (Chance, toujours 0)
	int resistance_air;
	int resistance_terre;
	int resistance_eau;
	int resistance_feu;
	int resistance_necromancie;
	int resistance_lumiere;
	int puissance_air;
	int puissance_terre;
	int puissance_eau;
	int puissance_feu;
	int puissance_necromancie;
	int puissance_lumiere;
	int niveau;
	int pv;
	int esquive;

	
	long CA;
	
	int vet_apparence;
	int vet_corps;
	int vet_pied;
	int vet_main;
	int vet_tete;
	int vet_jambe;
	int arme;
	int bouclier;
	int vet_dos;
	int agressivite;
	int id_clan;
	int vitesse;
	
	long xp_coup;
	long xp_down;
	
	int or_min;
	int or_max;

	boolean can_attack = false;
	
	int nb_types_attaques;
	int nb_flags;
	int nb_drops;

	String nom="";
	String ID ="";
	
	ArrayList<CREATUREATTAQUE> attaques = new ArrayList<CREATUREATTAQUE>();
	ArrayList<CREATUREFLAG> flags = new ArrayList<CREATUREFLAG>();
	ArrayList<CREATUREDROP> drops = new ArrayList<CREATUREDROP>();
	
	class CREATUREATTAQUE{
		
		private int taille_degats;
		String degats="";
		int lvl;
		int succes;
		int id_sort;
		int distance_min;
		int distance_max;
		
		public CREATUREATTAQUE(ByteBuffer buf){
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_degats = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- Taille de l'ID: "+taille_id);
			for (int i=0 ; i<taille_degats ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				degats += s;
			}
			//logger.info("				- degats: "+degats);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			lvl = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- lvl: "+lvl);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			succes = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- succes: "+succes);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id_sort = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- id_sort: "+id_sort);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			distance_min = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- distance_min: "+distance_min);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			distance_max = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- distance_max: "+distance_max);
			//logger.info("");
		}
	}
	
	class CREATUREFLAG{
		int id;
		int valeur;
		boolean incremente=false;
		public CREATUREFLAG(ByteBuffer buf){
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- id : "+id);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			valeur = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- valeur : "+valeur);
			if (buf.get() == 1)incremente=true;
			//logger.info("				- incremente : "+incremente);
		}
	}
	
	class CREATUREDROP{
		int id;
		long chance;
		public CREATUREDROP(ByteBuffer buf){
			byte b1, b2, b3, b4, b5, b6, b7, b8;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- id : "+id);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			b5 = buf.get();
			b6 = buf.get();
			b7 = buf.get();
			b8 = buf.get();
			chance = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
			//logger.info("			- chance: "+chance);
		}
	}
	
	public CREATURE(ByteBuffer buf){
		byte b1, b2, b3, b4, b5, b6, b7, b8;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		IDnum = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- ID Numérique: "+IDnum);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_ID = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille de l'ID: "+taille_id);
		for (int i=0 ; i<taille_ID ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			ID += s;
		}
		//logger.info("			- ID : "+ID);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("				- Taille de l'ID: "+taille_id);
		for (int i=0 ; i<taille_nom ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nom += s;
		}		
		//logger.info("			- Nom : "+nom);

		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		force = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- force: "+force);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		endurance = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- endurance: "+endurance);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		agilite = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- agilite: "+agilite);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		intelligence = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- intelligence: "+intelligence);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		volonte = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- volonte: "+volonte);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		sagesse = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- sagesse: "+sagesse);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		chance = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- chance: "+chance);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_air = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- resistance_air: "+resistance_air);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_terre = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- resistance_terre: "+resistance_terre);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_eau = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- resistance_eau: "+resistance_eau);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_feu = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- resistance_feu: "+resistance_feu);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_necromancie = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- resistance_necromancie: "+resistance_necromancie);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_lumiere = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- resistance_lumiere: "+resistance_lumiere);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_air = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- puissance_air: "+puissance_air);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_terre = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- puissance_terre: "+puissance_terre);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_eau = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- puissance_eau: "+puissance_eau);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_feu = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- puissance_feu: "+puissance_feu);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_necromancie = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- puissance_necromancie: "+puissance_necromancie);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_lumiere = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- puissance_lumiere: "+puissance_lumiere);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		niveau = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- niveau: "+niveau);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		pv = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- pv: "+pv);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		esquive = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- esquive: "+esquive);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		b5 = buf.get();
		b6 = buf.get();
		b7 = buf.get();
		b8 = buf.get();
		CA = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
		//logger.info("			- CA: "+CA);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_apparence = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- vet_apparence: "+vet_apparence);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_corps = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- vet_corps: "+vet_corps);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_pied = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- vet_pied: "+vet_pied);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_main = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- vet_main: "+vet_main);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_tete = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- vet_tete: "+vet_tete);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_jambe = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- vet_jambe: "+vet_jambe);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		arme = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- arme: "+arme);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		bouclier = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- bouclier: "+bouclier);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_dos = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- vet_dos: "+vet_dos);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		agressivite = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- agressivite: "+agressivite);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		id_clan = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- id_clan: "+id_clan);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vitesse = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- vitesse: "+vitesse);
		
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		b5 = buf.get();
		b6 = buf.get();
		b7 = buf.get();
		b8 = buf.get();
		xp_coup = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
		//logger.info("			- xp_coup: "+xp_coup);
		
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		b5 = buf.get();
		b6 = buf.get();
		b7 = buf.get();
		b8 = buf.get();
		xp_down = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
		//logger.info("			- xp_down: "+xp_down);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		or_min = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- or_min: "+or_min);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		or_max = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- or_max: "+or_max);
		
		if (buf.get() == 1) can_attack = true;
		//logger.info("			- can_attack: "+can_attack);

		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_types_attaques = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- nb_types_attaques: "+nb_types_attaques);
		
		for (int i=0; i<nb_types_attaques ; i++){
			//logger.info("			- attaque: "+i);
			attaques.add(new CREATUREATTAQUE(buf));
		}

		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_flags = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- nb_flags: "+nb_flags);
		
		for (int i=0; i<nb_flags ; i++){
			//logger.info("			- flag: "+i);

			flags.add(new CREATUREFLAG(buf));
		}
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_drops = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- nb_drops: "+nb_drops);
		
		for (int i=0; i<nb_drops ; i++){
			//logger.info("			- drop: "+i);
			drops.add(new CREATUREDROP(buf));
		}
	}
}
