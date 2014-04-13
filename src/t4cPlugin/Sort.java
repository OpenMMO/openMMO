package t4cPlugin;

import java.nio.ByteBuffer;

public class Sort {
	
	int id;
	int enclenchement;
	int reqLVL;


	public Sort(ByteBuffer buf) {
		byte b1,b2,b3,b4;
		//ID
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		id = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- ID : "+id);
		
		//Senclenchement
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		enclenchement = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- Enclenchement : "+enclenchement);
		
		//LVL requis
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		reqLVL = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//logger.info("					- LVL requis : "+reqLVL);
	}

}
