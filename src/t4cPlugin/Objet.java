package t4cPlugin;

import java.nio.ByteBuffer;

public class Objet {

	String id="";
	int taille_id;
	
	public Objet(ByteBuffer buf) {
		byte b1,b2,b3,b4;
		//ID de l'objet contenu
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille de l'ID: "+taille_id);
		for (int i=0 ; i<taille_id ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			id += s;
		}
		//System.out.println("						- ID : "+id);
	}

}
