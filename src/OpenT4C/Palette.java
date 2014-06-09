package OpenT4C;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

public class Palette {

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
	
	public String nom = "";
	
	ArrayList<Pixel> pixels = new ArrayList<Pixel>();
	
	class Pixel{
		short red;
		short green;
		short blue;
		public Pixel(short b, short c, short d) {
			red = b;
			green = c;
			blue = d;
		}
		
		public short getRed() {
			return red;
		}
		public void setRed(short red) {
			this.red = red;
		}
		public short getGreen() {
			return green;
		}
		public void setGreen(short green) {
			this.green = green;
		}
		public short getBlue() {
			return blue;
		}
		public void setBlue(short blue) {
			this.blue = blue;
		}
	}
	
	public Palette(ByteBuffer buf) {
		byte[] bytes = new byte[64];
		buf.get(bytes);
		nom = new String(bytes);
		nom = nom.substring(0, nom.indexOf(0x00));
		for (int i=0 ; i<256 ; i++){
			short tmp1,tmp2,tmp3;
			byte b;
			b = buf.get();
			tmp1 = tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b});
			b = buf.get();
			tmp2 = tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b});
			b = buf.get();
			tmp3 = tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b});
			pixels.add(new Pixel (tmp1, tmp2, tmp3));
		}
	}

	public ByteBuffer getPixels() {
		ByteBuffer palette = ByteBuffer.allocate(pixels.size()*3);
		Iterator<Pixel> iter = pixels.iterator();
		while (iter.hasNext()){
			Pixel p = iter.next();
			palette.put((byte)((p.red)&0xFF));
			palette.put((byte)((p.green)&0xFF));
			palette.put((byte)((p.blue)&0xFF));
		}
		palette.rewind();
		return palette;
	}
	
}