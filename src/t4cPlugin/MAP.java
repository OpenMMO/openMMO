package t4cPlugin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class MAP {
	
	private static HashMap<Integer,byte[]> blocs = new HashMap<Integer, byte[]>();
	private static ArrayList<Integer> adresses = new ArrayList<Integer>();
	private static final int nb_blocs = 24 * 24;
	private static final int bloc_size = 128 * 128;
	private static ByteBuffer buf, image_data, buf_unpacked;

	
	public static void Map_Load (File raw_data, short rle_value){
		System.out.println("	- Décryptage du fichier MAP : "+raw_data.getName());
		image_data = ByteBuffer.allocate(nb_blocs * bloc_size *2);
		buf_unpacked = ByteBuffer.allocate(((nb_blocs * bloc_size *2)+576*4));
		System.out.println("		- Taille du Tampon d'image : "+(nb_blocs * bloc_size *2)+" octets = "+(nb_blocs * bloc_size *2/1024)+"Ko");
		File map_data = new File(Params.t4cOUT+"MAP/"+raw_data.getName());
		buf = ByteBuffer.allocate((int)raw_data.length());
		System.out.println("		- Lecture du fichier source : "+(int)raw_data.length()+" octets = "+(int)raw_data.length()/1024+"Ko");
		try {
			DataInputStream in = new DataInputStream (new FileInputStream(raw_data));
			while (buf.position() < (int)raw_data.length()){
				buf.put(in.readByte());
			}
			in.close();
		}catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		buf.rewind();
		//Lecture des adresses
		for (int k = 0 ; k<nb_blocs ; k++){
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			byte[] bytes = new byte[]{b4,b3,b2,b1};
			int adr = tools.ByteArrayToNumber.bytesToInt(bytes);
			adresses.add(adr);
			buf_unpacked.put(b1);
			buf_unpacked.put(b2);
			buf_unpacked.put(b3);
			buf_unpacked.put(b4);
		}
		System.out.println("		- Adresses des blocs lues. Première/Dernière "+adresses.get(0)+"/"+adresses.get(adresses.size()-1));
		//Décompression des blocs
		Iterator<Integer> iter = adresses.iterator();
		int current = iter.next();
		while(iter.hasNext()){
			int next = iter.next();
			RLE_UncompressBlock(current, next, rle_value);
			current = next;
		}
		//Un dernier tour pour le dernier bloc
		RLE_UncompressBlock(current, (int)raw_data.length(), rle_value);
		buf.clear();

		
		//Écriture de la Map
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(map_data));
			out.write(buf_unpacked.array());
			out.close();
			System.out.println("	- Fichier MAP "+map_data.getCanonicalPath()+" écrit.");
			System.out.println("");
		}catch(IOException exc){
			System.err.println("Erreur I/O");
			exc.printStackTrace();
		}
		image_data.clear();
		buf_unpacked.clear();
		adresses.clear();
	}

	private static void RLE_UncompressBlock(int current, int next, short rle_value) {
		//System.out.println("			- Décompression bloc "+ i++ +" : "+current+"->"+next+" taille : "+(next-current));
		ArrayList<Short> positionList = new ArrayList<Short>();
		buf.position((int)current);
		short val;
		short val2;
		while (buf.position() < next){
			//System.out.println(buf.position()+"-"+next);
			byte b1, b2, b3, b4;
			b1 = buf.get();
			b2 = buf.get();
			byte[] read = new byte[]{b2,b1};
			val = tools.ByteArrayToNumber.bytesToShort(read);
			if(val<rle_value){
				//System.out.println("			- Position non-compressée : "+(short) (0x354 + (val & 0xFFFF) * 4));
				positionList.add((short) (0x354 + (val & 0xFFFF) * 4));
				buf_unpacked.putShort((short) (0x354 + (val & 0xFFFF) * 4));
			}else{
				b3 = buf.get();
				b4 = buf.get();
				read = new byte[]{b4,b3};
				val2 = tools.ByteArrayToNumber.bytesToShort(read);
				//System.out.println("			- Positions compressées : "+(val - rle_value)+" x "+(short) (0x354 + (val2 & 0xFFFF) * 4));
				for (int i = 0 ; i<val - rle_value ; i++){
					positionList.add((short) (0x354 + (val2 & 0xFFFF) * 4));
					buf_unpacked.putShort((short) (0x354 + (val2 & 0xFFFF) * 4));
				}
			}
		}
		ByteBuffer tmp = ByteBuffer.allocate(positionList.size()*Short.SIZE/8);
		Iterator<Short> i = positionList.iterator();
		while (i.hasNext()){
			tmp.putShort(i.next());
		}
		blocs.put(adresses.indexOf(current), tmp.array());
		positionList.clear();
	}
}