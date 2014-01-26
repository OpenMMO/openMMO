package t4cPlugin;

import java.nio.ByteBuffer;
import java.util.ArrayList;


/**
 *   PStructureNomsSprites = ^StructureNomsSprites;
  StructureNomsSprites = Record
    Nom : Array [0..63] Of Char;
    Chemin : Array [0..255] Of Char;
    Indexation : LongInt;
    NumDda : Int64;
  End;
 * @author synoga
 *
 */

public class DIDSprite {

	private String nom = "";
	private String chemin = "";
	
	private int indexation;
	private int index_next;
	
	private long numDda;
	
	public DIDSprite(ByteBuffer buf) {
		byte[] bytes = new byte[64];
		buf.get(bytes);
		nom = new String(bytes);
		nom = nom.substring(0, nom.indexOf(0x00));
		if (nom.startsWith("Cemetery Gates /")) nom = "Cemetery Gates \\";
		//System.out.println("		- Nom : "+nom+" longueur : "+nom.length());
		
		bytes = new byte[256];
		buf.get(bytes);
		chemin = new String(bytes);
		chemin = chemin.replace('\\', Params.SLASH);
		chemin = chemin.substring(0, chemin.indexOf(0x00));
		if (!chemin.endsWith(""+Params.SLASH)) chemin = chemin+Params.SLASH;
		//System.out.println("		- Chemin : "+chemin);
		
		byte b1,b2,b3,b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		indexation = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("		- Indexation : "+indexation);
		
		byte b5,b6,b7,b8;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		b5 = buf.get();
		b6 = buf.get();
		b7 = buf.get();
		b8 = buf.get();
		numDda = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
		//System.out.println("		- N° DDA : "+numDda);
	}
	
	public String getNom(){
		return nom;
	}

	public String getChemin(){
		return chemin;
	}
	
	public int getIndexation(){
		return indexation;
	}
	
	public int getIndexNext(){
		return index_next;
	}

	public void setIndexNext(int index){
		index_next = index;
	}
	
	public long getNumDDA(){
		return numDda;
	}

	public int compareTo(DIDSprite o2) {
		return indexation-o2.getIndexation();
	}
}
