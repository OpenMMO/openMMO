package tools;

import java.nio.ByteBuffer;

public class Fast_Forward{
	public Fast_Forward(ByteBuffer buf, int taille, boolean quit, String msg){
		if (taille > buf.capacity()-buf.position()) taille = buf.capacity()-buf.position();
		System.out.print(msg+" : ");
		for (int i=0 ; i<taille ; i++){
			System.out.print(tools.ByteArrayToHexString.print(new byte[]{buf.get()}));
		}
		if (quit) System.exit(0);
		System.out.println("");
		buf.position(buf.position()-taille);
	}
}