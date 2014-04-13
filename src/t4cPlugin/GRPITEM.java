package t4cPlugin;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class GRPITEM {
	
	int nb_objets;
	ArrayList<Objet> objets = new ArrayList<Objet>();
	
	public GRPITEM(ByteBuffer buf) {
		
		//nombre d'objets contenus
		byte b1,b2,b3,b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_objets =tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- Nombre d'objets contenus : "+nb_objets);
		
		for (int i=0 ; i<nb_objets ; i++){
			objets.add(new Objet(buf));
		}
	}

}
