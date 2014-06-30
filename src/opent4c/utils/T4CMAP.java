package opent4c.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tools.DataInputManager;


public class T4CMAP {
	
	private static Logger logger = LogManager.getLogger(T4CMAP.class.getSimpleName());
	
	HashMap<Integer,byte[]> blocs = new HashMap<Integer, byte[]>();
	ArrayList<Integer> adresses = new ArrayList<Integer>();
	final int nb_blocs = 24 * 24;
	final int bloc_size = 128 * 128;
	ByteBuffer map_data, image_data, buf_unpacked;

	
	public void Map_load_block(File f, int rle_value){
		logger.info("	- Décryptage de la carte "+f.getName());
		File decryptedMap = new File(FilesPath.getMapFilePath(f.getName()));
		int block_number;
		int offset;
		int line_number;
		int pixel_address;
		ByteBuffer raw_data, tmp_unpack,image_data;
		byte b1,b2,b3,b4;
		
		image_data = ByteBuffer.allocate(3072*3072*2);
		tmp_unpack = ByteBuffer.allocate(128 * 128 * Short.SIZE);
		raw_data = ByteBuffer.allocate((int)f.length());
		try {
			DataInputManager in = new DataInputManager (f);
			while (raw_data.position() < (int)f.length()){
				raw_data.put(in.readByte());
			}
			in.close();
		}catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		raw_data.rewind();
		
		for (block_number = 0 ; block_number < 24 * 24 ; block_number++){
			raw_data.position(block_number*4);
			b1=raw_data.get();
			b2=raw_data.get();
			b3=raw_data.get();
			b4=raw_data.get();
			offset = tools.ByteArrayToNumber.bytesToInt(new  byte[]{b4,b3,b2,b1});
			raw_data.position(offset);
			RLE_UncompressBlock(raw_data, tmp_unpack, rle_value);
			
			for (line_number=0 ; line_number<128 ; line_number++){
				pixel_address = (block_number % 24) * 128 * 2 + ((block_number / 24) * 3072 * 2 * 128) + line_number * 3072 * 2;
				for (int j=0 ; j< 128*2 ; j++){
					image_data.array()[pixel_address+j] = tmp_unpack.array()[j + line_number * 128 * 2];
				}
			}
			tmp_unpack.clear();
		}
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(decryptedMap));
			out.write(image_data.array());
			out.close();
		}catch(IOException exc){
			System.err.println("Erreur I/O");
			exc.printStackTrace();
		}
	}
	
	private void RLE_UncompressBlock(ByteBuffer packed_data, ByteBuffer unpacked_data, int rle_value) {
		int val;
		int i = 0;
		int start_offset;
		byte b1,b2;
		
		start_offset = unpacked_data.position();
		while ((unpacked_data.position()-start_offset) < (128*128*2)){
			b1 = packed_data.get();
			b2 = packed_data.get();
			val = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
			if (val < rle_value ){
				unpacked_data.put(b1);
				unpacked_data.put(b2);
			} else {
				b1 = packed_data.get();
				b2 = packed_data.get();
				
				for (i = 0; i < val - rle_value; i++){
					unpacked_data.put(b1);
					unpacked_data.put(b2);
				}
			}
		}
	}
}