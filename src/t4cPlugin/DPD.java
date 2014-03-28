package t4cPlugin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

import tools.DataInputManager;

public class DPD {

/**
  
  PStructureDpd = ^StructureDpd;
  StructureDpd = Record
    HashMd5 : Array [0..15] Of Byte;
    TailleDecompressee : LongInt;
    TailleCompressee : LongInt;
    HashMd52 : Array [0..16] Of Byte;
    Palettes : Array Of StructurePalette;
    CheckSum : Byte;
    NbEntree : LongInt;
  End;*/
	private ByteBuffer buf;
	private ByteBuffer header;
	private ByteBuffer bufUnZip;
	
	private byte clef = (byte) 0x66;
	
	
	private byte[] header_hashMd5 = new byte[16];
	private byte[] header_hashMd52 = new byte[17];
	private byte[] header_hash = new byte[32];


	static ArrayList<DPDPalette> palettes = new ArrayList<DPDPalette>();
	
	private int header_taille_unZip;
	private int header_taille_zip;
	private int taille_unZip;
	private int nb_palettes;
	
	private byte checksum;
	private byte azt;
	
	public void decrypt(File f){
		System.out.println("Lecture du fichier "+f.getName());
		byte b1,b2,b3,b4;
		header = ByteBuffer.allocate(41);
		try {
			DataInputManager in = new DataInputManager (f);
			while (header.position()<header.capacity()){
				header.put(in.readByte());
			}
			header.rewind();
			for (int i=0 ; i<16 ; i++){
				header_hashMd5[i] = header.get();
			}
			//System.out.println("	- HashMD5 : "+tools.ByteArrayToHexString.print(header_hashMd5));
			b1 = header.get();
			b2 = header.get();
			b3 = header.get();
			b4 = header.get();
			header_taille_unZip = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//System.out.println("	- header_taille_unZip : "+header_taille_unZip);
			b1 = header.get();
			b2 = header.get();
			b3 = header.get();
			b4 = header.get();
			header_taille_zip = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//System.out.println("	- header_taille_zip : "+header_taille_zip);
			for (int i=0 ; i<17 ; i++){
				header_hashMd52[i] = header.get();
			}
			//System.out.println("	- HashMD52 : "+tools.ByteArrayToHexString.print(header_hashMd52));
			ByteBuffer buf_hash = ByteBuffer.allocate(33);
			buf_hash.put(header_hashMd5);
			buf_hash.put(header_hashMd52);
			buf_hash.rewind();
			buf_hash.get(header_hash);
			azt = header_hashMd52[16];
			//System.out.println(new String(header_hash));
			//System.out.println(tools.ByteArrayToHexString.print(header_hash));
			//System.out.println(""+azt);
			//System.out.println(tools.ByteArrayToHexString.print(new byte[]{azt}));


			buf = ByteBuffer.allocate(header_taille_zip);
			while (buf.position() < buf.capacity()){
				buf.put(in.readByte());
			}
			in.close();
		}catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		buf.rewind();
		
		//On opère la décompression Zlib
	     Inflater decompresser = new Inflater();
	     decompresser.setInput(buf.array(), 0, buf.capacity());
	     bufUnZip = ByteBuffer.allocate(header_taille_unZip);
	     try {
			taille_unZip = decompresser.inflate(bufUnZip.array());
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     decompresser.end();
		//System.out.println("	- Taille décompressée : "+taille_unZip);

		//Ensuite on décrypte le fichier avec la clé
		for (int i=0; i<bufUnZip.capacity(); i++){
			bufUnZip.array()[i] ^= clef;
		}
		nb_palettes = taille_unZip/(64 + 768);
		Params.total_palette = nb_palettes;
		//System.out.println("	- Nombre de palettes : "+nb_palettes);

		for(int i=0 ; i<nb_palettes ; i++){
			palettes.add(new DPDPalette(bufUnZip));
		}
		
		//TODO Checksum
		
		Iterator<DPDPalette> iter = palettes.iterator();
		while(iter.hasNext()){
			DPDPalette pal = iter.next();
			pal.nom = pal.nom.replace("_", "");
			File palette = new File("."+File.separator+"data"+File.separator+"palettes"+File.separator+pal.nom+".bmp");
			if (!palette.exists()){
				BufferedImage buf = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
				ByteBuffer pixels = pal.getPixels();
				for (int i=0 ; i<16 ; i++){
					for (int j=0 ; j<16 ; j++){
						int r = pixels.get();
						int g = pixels.get();
						int b = pixels.get();
						int col = (r << 16) | (g << 8) | b;
						buf.setRGB(j,i,col);
					}
				}
				
				try {
					ImageIO.write(buf, "bmp", palette);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				System.out.println("Palette écrite : "+"."+File.separator+"data"+File.separator+"palettes"+File.separator+pal.nom+".bmp");
				Params.nb_palette++;
			}
		}
		System.out.println("Fichier "+f.getName()+" lu.");
	}
}
