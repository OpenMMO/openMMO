package t4cPlugin;

import java.nio.ByteBuffer;

public class Boost {

	int id;
	int stat;
	int taille_valeur;
	int reqINT;
	int reqSAG;
	
	String valeur = "";
	
	public Boost(ByteBuffer buf) {
		byte b1,b2,b3,b4;
		//Id du boost (id de la bdd)
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("					- Id du boost : "+id);
		
		//stat à booster
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		stat = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("					- Stat à booster : "+stat);
		
		//Valeur
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_valeur = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("						- Taille de l'emplacement: "+taille_valeur);
		for (int i=0 ; i<taille_valeur ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			valeur += s;
		}
		//System.out.println("					- Valeur : "+valeur);

		//sINT requis
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqINT = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("					- INT requis : "+reqINT);
		
		//SAG requis
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqSAG = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("					- Sag requis : "+reqSAG);
		
	}

}
