package t4cPlugin;

import java.nio.ByteBuffer;

/*
 * Pour chaque position d'objet:
{
	4		unsigned long		Taille de la chaîne de caractères suivante
	(variable)	char *			Nom de la position d'objet
	4		unsigned long		Position X
	4		unsigned long		Position Y
	4		unsigned long		Numéro de la carte (indice Z)
}
 */


public class ITEMPOS {
	
	int taille_nom;
	String nom = "";
	int x;
	int y;
	int num_carte;
	
	public ITEMPOS(ByteBuffer buf){
		byte b1, b2, b3, b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("		- Définition du nombre de sorts : "+nb_sorts);
		
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
		x = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- x : "+x);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- y : "+y);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("			- Numéro de carte  : "+num_carte);
	}
}
