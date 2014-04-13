package t4cPlugin;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/*
 * Pour chaque groupe de créatures:
{
	4		unsigned long		Temps d'apparition minimum
	4		unsigned long		Temps d'apparition maximum
	4		unsigned long		Distance maximale d'apparition par-rapport à la position indiquée
	4		unsigned long		Nombre minimal d'apparitions
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			Nom du groupe de créatures
	4		unsigned long		Nombre de créatures

	Pour chaque créature:
	{
		4		unsigned long		Taille de la chaîne de caractères suivante
		(variable)	char *			ID de la créature
	}

	4		unsigned long		Nombre de positions d'apparition

	Pour chaque position d'apparition:
	{
		4		unsigned long		Position X
		4		unsigned long		Position Y
		4		unsigned long		Numéro de la carte (indice Z)
	}
}
 */
public class CREATUREBLOC {
	int min_time;
	int max_time;
	int max_dist;
	int min_spawn;
	private int taille_nom;
	String nom="";
	int nb_creatures;
	int nb_pos;
	ArrayList<Creature> creatures = new ArrayList<Creature>();
	ArrayList<Position> positions = new ArrayList<Position>();
	
	class Creature{
		private int taille_id;
		String id="";
		public Creature(ByteBuffer buf){
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			taille_id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("		- Définition du nombre de blocs de créatures : "+nb_blocs);
			for (int i=0 ; i<taille_id ; i++){
				b1 = buf.get();
				String s = new String(new byte[]{b1});
				id += s;
			}
			//logger.info("			- id : "+id);
		}
	}
	
	class Position{
		int x;
		int y;
		int num_carte;
		public Position(ByteBuffer buf){
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			x = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- X : "+x);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			y = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- Y : "+y);
			
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			num_carte = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("				- num_carte : "+num_carte);
		}
		
	}
	
	public CREATUREBLOC(ByteBuffer buf) {
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		min_time = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- min_time : "+min_time);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		max_time = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- max_time : "+max_time);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		max_dist = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- max_dist : "+max_dist);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		min_spawn = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- min_spawn : "+min_spawn);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- taille_nom : "+taille_nom);
		for (int i=0 ; i<taille_nom ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nom += s;
		}
		//logger.info("			- nom : "+nom);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_creatures = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- nb_creatures : "+nb_creatures);
		
		for (int i = 0 ; i<nb_creatures ; i++){
			creatures.add(new Creature(buf));
		}
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_pos = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("			- nb_pos : "+nb_pos);
		
		for (int i = 0 ; i<nb_pos ; i++){
			positions.add(new Position(buf));
		}
	}

}
