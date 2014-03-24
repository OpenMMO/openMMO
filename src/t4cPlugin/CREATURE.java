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
			//System.out.println("				- Taille de l'ID: "+taille_id);
			for (int i=0 ; i<taille_degats ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				degats += s;
			}
			//System.out.println("				- degats: "+degats);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			lvl = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//System.out.println("				- lvl: "+lvl);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			succes = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//System.out.println("				- succes: "+succes);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			id_sort = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//System.out.println("				- id_sort: "+id_sort);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			distance_min = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//System.out.println("				- distance_min: "+distance_min);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			distance_max = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//System.out.println("				- distance_max: "+distance_max);
			//System.out.println("");
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
			//System.out.println("				- id : "+id);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			valeur = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//System.out.println("				- valeur : "+valeur);
			if (buf.get() == 1)incremente=true;
			//System.out.println("				- incremente : "+incremente);
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
			//System.out.println("				- id : "+id);
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			b5 = buf.get();
			b6 = buf.get();
			b7 = buf.get();
			b8 = buf.get();
			chance = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
			//System.out.println("			- chance: "+chance);
		}
	}
	
	public CREATURE(ByteBuffer buf){
		byte b1, b2, b3, b4, b5, b6, b7, b8;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		IDnum = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- ID Numérique: "+IDnum);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_ID = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille de l'ID: "+taille_id);
		for (int i=0 ; i<taille_ID ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			ID += s;
		}
		//System.out.println("			- ID : "+ID);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille de l'ID: "+taille_id);
		for (int i=0 ; i<taille_nom ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nom += s;
		}		
		//System.out.println("			- Nom : "+nom);

		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		force = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- force: "+force);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		endurance = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- endurance: "+endurance);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		agilite = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- agilite: "+agilite);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		intelligence = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- intelligence: "+intelligence);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		volonte = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- volonte: "+volonte);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		sagesse = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- sagesse: "+sagesse);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		chance = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- chance: "+chance);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_air = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- resistance_air: "+resistance_air);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_terre = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- resistance_terre: "+resistance_terre);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_eau = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- resistance_eau: "+resistance_eau);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_feu = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- resistance_feu: "+resistance_feu);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_necromancie = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- resistance_necromancie: "+resistance_necromancie);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		resistance_lumiere = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- resistance_lumiere: "+resistance_lumiere);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_air = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- puissance_air: "+puissance_air);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_terre = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- puissance_terre: "+puissance_terre);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_eau = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- puissance_eau: "+puissance_eau);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_feu = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- puissance_feu: "+puissance_feu);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_necromancie = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- puissance_necromancie: "+puissance_necromancie);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		puissance_lumiere = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- puissance_lumiere: "+puissance_lumiere);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		niveau = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- niveau: "+niveau);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		pv = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- pv: "+pv);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		esquive = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- esquive: "+esquive);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		b5 = buf.get();
		b6 = buf.get();
		b7 = buf.get();
		b8 = buf.get();
		CA = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
		//System.out.println("			- CA: "+CA);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_apparence = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- vet_apparence: "+vet_apparence);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_corps = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- vet_corps: "+vet_corps);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_pied = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- vet_pied: "+vet_pied);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_main = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- vet_main: "+vet_main);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_tete = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- vet_tete: "+vet_tete);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_jambe = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- vet_jambe: "+vet_jambe);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		arme = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- arme: "+arme);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		bouclier = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- bouclier: "+bouclier);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vet_dos = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- vet_dos: "+vet_dos);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		agressivite = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- agressivite: "+agressivite);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		id_clan = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- id_clan: "+id_clan);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		vitesse = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- vitesse: "+vitesse);
		
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		b5 = buf.get();
		b6 = buf.get();
		b7 = buf.get();
		b8 = buf.get();
		xp_coup = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
		//System.out.println("			- xp_coup: "+xp_coup);
		
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		b5 = buf.get();
		b6 = buf.get();
		b7 = buf.get();
		b8 = buf.get();
		xp_down = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
		//System.out.println("			- xp_down: "+xp_down);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		or_min = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- or_min: "+or_min);
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		or_max = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- or_max: "+or_max);
		
		if (buf.get() == 1) can_attack = true;
		//System.out.println("			- can_attack: "+can_attack);

		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_types_attaques = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- nb_types_attaques: "+nb_types_attaques);
		
		for (int i=0; i<nb_types_attaques ; i++){
			//System.out.println("			- attaque: "+i);
			attaques.add(new CREATUREATTAQUE(buf));
		}

		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_flags = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- nb_flags: "+nb_flags);
		
		for (int i=0; i<nb_flags ; i++){
			//System.out.println("			- flag: "+i);

			flags.add(new CREATUREFLAG(buf));
		}
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_drops = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- nb_drops: "+nb_drops);
		
		for (int i=0; i<nb_drops ; i++){
			//System.out.println("			- drop: "+i);
			drops.add(new CREATUREDROP(buf));
		}
	}
}
