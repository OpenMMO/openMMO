package t4cPlugin;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import tools.BitBuffer;

public class COLMAP {
	
	public COLMAP(ByteBuffer buf) {
		byte b1,b2,b3,b4;
		short indiceZ, tailleX, tailleY;
		int taille_nom;
		String nom = "";
		DataBufferByte colMap;
		BufferedImage colMapImg;
		WritableRaster colRaster;
		b1 = buf.get();
		b2 = buf.get();
		indiceZ = tools.ByteArrayToNumber.bytesToShort(new byte[]{b2,b1});
		//System.out.println("			- Carte n° : "+indiceZ);
		
		//NOM
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		taille_nom = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("				- Taille du nom: "+taille_nom);
		for (int i=0 ; i<taille_nom ; i++){
			b1 = buf.get();
			String s = new String(new byte[]{b1});
			nom += s;
		}
		//System.out.println("				- Nom : "+nom);
		
		//TailleX
		b1 = buf.get();
		b2 = buf.get();
		tailleX = tools.ByteArrayToNumber.bytesToShort(new byte[]{b2,b1});
		//System.out.println("				- Taille X : "+tailleX);
		
		//TailleY
		b1 = buf.get();
		b2 = buf.get();
		tailleY = tools.ByteArrayToNumber.bytesToShort(new byte[]{b2,b1});
		//System.out.println("				- Taille Y : "+tailleY);
		byte[] bytes = new byte[3072*3072/2];
		buf.get(bytes,0,3072*3072/2);
		BitBuffer bits = new BitBuffer(bytes);
		colMap = new DataBufferByte((tailleX*tailleY)*2);
		for (int i=0 ; i<tailleY ; i++){
			for(int j=0 ; j<tailleX ; j++){
				colMap.setElem((i*tailleX)+j,bits.getBits(4));
			}
		}
		colRaster = Raster.createPackedRaster(colMap,tailleX,tailleY,8,null);
		byte[] red =   new byte[]{(byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0xA0, (byte) 0xA0, (byte) 0x00, (byte) 0xA0, (byte) 0x7F};
		byte[] green = new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0xA0, (byte) 0xA0, (byte) 0x7F};
		byte[] blue =  new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0xA0, (byte) 0xA0, (byte) 0xA0, (byte) 0x7F};
		IndexColorModel model = new IndexColorModel(8,16,red,green,blue);
		colMapImg = new BufferedImage(model, colRaster, false, null);
		File f = new File(Params.t4cOUT+"COLMAPS/"+nom+".png");
		try {
			ImageIO.write(colMapImg, "png", f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("			- Carte de collision écrite : "+Params.t4cOUT+"COLMAPS/"+nom+".png");
		//System.out.println("");
	}

}
