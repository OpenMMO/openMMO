package OpenT4C;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Palette {
	
	public String nom = "";
	ArrayList<PalettePixel> pixels = new ArrayList<PalettePixel>();
	private static Logger logger = LogManager.getLogger(DataChecker.class.getSimpleName());

	/**
	 * Creates a palette from a ByteBuffer (.dpd file)
	 * @param buf
	 */
	public Palette(ByteBuffer buf) {
		byte[] bytes = new byte[64];
		buf.get(bytes);
		nom = new String(bytes);
		nom = nom.substring(0, nom.indexOf(0x00));
		nom = nom.replace("_","");
		logger.info("Palette décodée : "+nom);
		for (int i=0 ; i<256 ; i++){
			short tmp1,tmp2,tmp3;
			byte b;
			b = buf.get();
			tmp1 = tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b});
			b = buf.get();
			tmp2 = tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b});
			b = buf.get();
			tmp3 = tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b});
			pixels.add(new PalettePixel (tmp1, tmp2, tmp3));
		}
	}

	/**
	 * 
	 * @return a ByteBuffer with alla the palettes pixels
	 */
	public ByteBuffer getPixels() {
		ByteBuffer palette = ByteBuffer.allocate(pixels.size()*3);
		Iterator<PalettePixel> iter = pixels.iterator();
		while (iter.hasNext()){
			PalettePixel p = iter.next();
			palette.put((byte)((p.red)&0xFF));
			palette.put((byte)((p.green)&0xFF));
			palette.put((byte)((p.blue)&0xFF));
		}
		palette.rewind();
		return palette;
	}
	
}
