package t4cPlugin;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

public class DPDPalette {

	/**
	 * PStructurePalette = ^StructurePalette;
  StructurePalette = Record
    Nom : Array [0..63] Of Char;
    Pixels : Array [0..255] Of Record
      Rouge : Byte;
      Vert : Byte;
      Bleu : Byte;
    End;
  End;
	 * @param bufUnZip
	 */
	
	private String nom = "";
	
	ArrayList<Pixel> pixels = new ArrayList<Pixel>();
	
	class Pixel{
		public Pixel(byte b, byte c, byte d) {
			red = b;
			green = c;
			blue = d;
		}
		byte red;
		byte green;
		byte blue;
	}
	
	public DPDPalette(ByteBuffer buf) {
		byte[] bytes = new byte[64];
		buf.get(bytes);
		nom = new String(bytes);
		nom = nom.substring(0, nom.indexOf(0x00));
		System.out.println("		- Nom : "+nom);
		
		for (int i=0 ; i<256 ; i++){
			pixels.add(new Pixel (buf.get(), buf.get(), buf.get()));
		}
		//System.out.println("		- Nombre de pixels extraits : "+pixels.size());
	}

	public String getNom() {
		return nom;
	}

	public ByteBuffer getPixels() {
		ByteBuffer palette = ByteBuffer.allocate(pixels.size()*3);
		Iterator<Pixel> iter = pixels.iterator();
		while (iter.hasNext()){
			Pixel p = iter.next();
			palette.put(p.red);
			palette.put(p.green);
			palette.put(p.blue);
		}
		palette.rewind();
		return palette;
	}
	
	public ByteBuffer getReds() {
		ByteBuffer reds = ByteBuffer.allocate(pixels.size());
		Iterator<Pixel> iter = pixels.iterator();
		while (iter.hasNext()){
			Pixel p = iter.next();
			reds.put(p.red);
		}
		reds.rewind();
		return reds;
	}
	
	public ByteBuffer getBlues() {
		ByteBuffer blues = ByteBuffer.allocate(pixels.size());
		Iterator<Pixel> iter = pixels.iterator();
		while (iter.hasNext()){
			Pixel p = iter.next();
			blues.put(p.red);
		}
		blues.rewind();
		return blues;
	}
	
	public ByteBuffer getGreens() {
		ByteBuffer greens = ByteBuffer.allocate(pixels.size());
		Iterator<Pixel> iter = pixels.iterator();
		while (iter.hasNext()){
			Pixel p = iter.next();
			greens.put(p.red);
		}
		greens.rewind();
		return greens;
	}
	
}
