/**
 * 
 */
package opent4c;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opent4c.utils.FilesPath;

/**
 * @author synoga
 *
 */
public class SpriteWriter {

	private static Logger logger = LogManager.getLogger(SpriteWriter.class.getSimpleName());

	
	static void drawImage(BufferedImage img, MapPixel pixel){
		File f = null;
		if (pixel instanceof TilePixel){
			File dir = new File(FilesPath.getTuileDirectoryPath()+pixel.getAtlas());
			dir.mkdirs();
			f = new File(FilesPath.getTuileDirectoryPath()+pixel.getAtlas()+File.separator+pixel.getTex()+".png");
		}else{
			File dir = new File(FilesPath.getSpriteDirectoryPath()+pixel.getAtlas());
			dir.mkdirs();
			f = new File(FilesPath.getSpriteDirectoryPath()+pixel.getAtlas()+File.separator+pixel.getTex()+".png");
		}
		if (f.exists())return;
		GraphicsConfiguration gc = img.createGraphics().getDeviceConfiguration();
		BufferedImage out =	gc.createCompatibleImage(pixel.getLargeur(), pixel.getHauteur(), Transparency.BITMASK);
		Graphics2D g2d = out.createGraphics();
		g2d.setComposite(AlphaComposite.Src);
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		Iterator<ImageWriter> iter = null;
		try {
			iter = ImageIO.getImageWritersByFormatName("png");
			ImageWriter writer = (ImageWriter)iter.next();
			ImageWriteParam iwp = writer.getDefaultWriteParam();
			FileImageOutputStream output = new FileImageOutputStream(f);
			writer.setOutput(output);
			IIOImage image = new IIOImage(out, null, null);
			writer.write(null, image, iwp);
			writer.dispose();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		//logger.info(++Params.nb_sprite+"/"+Params.total_sprites +" TYPE : "+didsprite.type+" "+Params.t4cOUT+"SPRITES/"+didsprite.chemin+File.separator+f.getName()+" | Palette : "+didsprite.palette.getNom());
	}

	static void unzipSpriteTwice(MapPixel pixel, ByteBuffer buf) {
		byte b1,b2,b3,b4;
		Inflater unzip = new Inflater();
		ByteBuffer unzip_data1 = null;
		byte[] data = null;
		try{
			data = new byte[pixel.getTaille_zip()];
		}catch(NegativeArraySizeException e){
			e.printStackTrace();
			System.exit(1);
		}
		buf.position(pixel.getBufPos());
		buf.get(data);
		unzip.setInput(data, 0, pixel.getTaille_zip());
		unzip_data1 = ByteBuffer.allocate(pixel.getTaille_unzip());
		int resultLength = 0;
		try {
			resultLength = unzip.inflate(unzip_data1.array());
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		unzip.end();
		unzip_data1.rewind();
		b1=unzip_data1.get();
		b2=unzip_data1.get();
		b3=unzip_data1.get();
		b4=unzip_data1.get();
		resultLength -= 4;
		int inconnu = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		Inflater unzip2 = new Inflater();
		ByteBuffer unzip_data2 = null;
		byte[] data2 = new byte[(int) resultLength];
		unzip_data1.get(data2);
		unzip2.setInput(data2, 0, (int) resultLength);
		unzip_data2 = ByteBuffer.allocate(inconnu);
		try {
			unzip2.inflate(unzip_data2.array());
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		unzip2.end();
		
		ArrayList<PalettePixel> pal = pixel.getPal().pixels;
		BufferedImage img = null;
		if (pixel instanceof TilePixel){
			img = new BufferedImage(pixel.getLargeur(), pixel.getHauteur(), BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage(pixel.getLargeur(), pixel.getHauteur(), BufferedImage.TYPE_INT_ARGB);
		}
		int y = 0, x = 0;
		while (y<pixel.getHauteur()){
			while (x<pixel.getLargeur()){
				int c = 0;
				try{
					c = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,unzip_data2.get()});
				}catch (BufferUnderflowException e){
					e.printStackTrace();
					System.exit(1);
				}
				PalettePixel px = null;
				px = pal.get(c);
				int red=0,green=0,blue=0,alpha=255;
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,(byte) pixel.getCouleurTrans()})){
					img.setRGB(x, y, 0);
				}else{
					red = px.getRed();
					green = px.getGreen();
					blue = px.getBlue();
					int col = (alpha << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
				}
				x++;
				//logger.info("	- Pixel : "+x+","+y+" : ARGB"+tools.ByteArrayToHexString.print((byte)alpha)+","+px.red+","+px.green+","+px.blue+" index palette : "+b);
			}
			y++;
			x = 0;
		}
		drawImage(img,pixel);
	}

	static void unzipSprite(MapPixel pixel, ByteBuffer buf) {
		byte b1,b2,b3,b4;
		buf.position(pixel.getBufPos());
		b1=buf.get();
		b2=buf.get();
		b3=buf.get();
		b4=buf.get();
		int taille_unzip2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		Inflater unzip = new Inflater();
		ByteBuffer unzip_data1 = null;
		byte[] data = new byte[(int) pixel.getTaille_zip()];
		buf.get(data);
		unzip.setInput(data, 0, (int) pixel.getTaille_zip());
		unzip_data1 = ByteBuffer.allocate((int)taille_unzip2);
		try {
			unzip.inflate(unzip_data1.array());
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		unzip.end();
		unzip_data1.rewind();
	
		ArrayList<PalettePixel> pal = pixel.getPal().pixels;
		BufferedImage img = null;
		if (pixel instanceof TilePixel){
			img = new BufferedImage(pixel.getLargeur(), pixel.getHauteur(), BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage(pixel.getLargeur(), pixel.getHauteur(), BufferedImage.TYPE_INT_ARGB);
		}			int y = 0, x = 0;
		while (y<pixel.getHauteur()){
			while (x<pixel.getLargeur()){
				int c = 0;
				try{
					c = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,unzip_data1.get()});
				}catch (BufferUnderflowException e){
					e.printStackTrace();
					System.exit(1);
				}
				PalettePixel px = null;
				px = pal.get(c);
				int red=0,green=0,blue=0,alpha=255;
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,(byte) pixel.getCouleurTrans()})){
					img.setRGB(x, y, 0);
				}else{
					red = px.getRed();
					green = px.getGreen();
					blue = px.getBlue();
					int col = (alpha << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
				}
				x++;
				//logger.info("	- Pixel : "+x+","+y+" : ARGB"+tools.ByteArrayToHexString.print((byte)alpha)+","+px.red+","+px.green+","+px.blue+" index palette : "+b);
			}
			y++;
			x = 0;
		}
		drawImage(img,pixel);
	}

	static void writeType2SpriteToDisk(MapPixel pixel, ByteBuffer buf) {
		byte b1,b2;
		int X,Y,nbpix;
		byte b = 0;
		ByteBuffer data = null;
		buf.position(pixel.getBufPos());
		Y=0;
		//pour ceux qui ont la compression Zlib en plus
		if (pixel.getLargeur() > 180 | pixel.getHauteur() > 180) {
			Inflater unzip = new Inflater();
			byte[] bytes = new byte[(int) pixel.getTaille_zip()];
			buf.get(bytes);
			unzip.setInput(bytes, 0, (int) pixel.getTaille_zip());
			data = ByteBuffer.allocate((int) pixel.getTaille_unzip());
			try {
				unzip.inflate(data.array());
			} catch (DataFormatException e) {
				e.printStackTrace();
				logger.fatal(e);
				System.exit(1);
			}
			unzip.end();
		}else{
			data = ByteBuffer.allocate(pixel.getTaille_zip());
			buf.get(data.array());
		}
		data.rewind();
		//on a les données du sprite dans data. on opère la décompression RLE
		ByteBuffer spriteTmp =  ByteBuffer.allocate((pixel.getHauteur()*pixel.getLargeur()));
		spriteTmp.rewind();
		//on remplit de transparence
		for (int i=0 ; i<spriteTmp.capacity() ; i++){
			spriteTmp.put((byte) pixel.getCouleurTrans());
		}
		spriteTmp.rewind();
		if (data != null){
			while(Y != pixel.getHauteur()){
				b1 = data.get();
				b2 = data.get();
				X = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
				b1 = data.get();
				b2 = data.get();
				nbpix = ((tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b1})*4) + tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b2}));
				b = data.get();
				if (b != 1){
					for (int i=0 ; i<nbpix ; i++){
						spriteTmp.put((int) (spriteTmp.position()+i+X+(Y*pixel.getLargeur())), data.get());
						if ((i+X) == (pixel.getLargeur()-1)) break;//sécu pour être sur de pas dépasser;
					}
				}
				b = data.get();
				if (b == 0) {
					break;
				}else if (b == 2){
					Y++;
				}
			}
		}
		spriteTmp.rewind();
		
		ArrayList<PalettePixel> pal = pixel.getPal().pixels;
		BufferedImage img = null;
		if (pixel instanceof TilePixel){
			img = new BufferedImage(pixel.getLargeur(), pixel.getHauteur(), BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage(pixel.getLargeur(), pixel.getHauteur(), BufferedImage.TYPE_INT_ARGB);
		}
		int y = 0, x = 0;
		while (y<pixel.getHauteur()){
			while (x<pixel.getLargeur()){
				int c = 0;
				try{
					c = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,spriteTmp.get()});
				}catch (BufferUnderflowException e){
					e.printStackTrace();
					System.exit(1);
				}
				PalettePixel px = null;
				px = pal.get(c);
				int red=0,green=0,blue=0,alpha=255;
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,(byte) pixel.getCouleurTrans()})){
					red = px.getRed();
					green = px.getGreen();
					blue = px.getBlue();
					int col = (0 << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
				}else{
					red = px.getRed();
					green = px.getGreen();
					blue = px.getBlue();
					int col = (alpha << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
				}
				x++;
				//logger.info("	- Pixel : "+x+","+y+" : ARGB"+tools.ByteArrayToHexString.print((byte)alpha)+","+px.red+","+px.green+","+px.blue+" index palette : "+b);
			}
			y++;
			x = 0;
		}
		drawImage(img,pixel);
	}

	static void writeType1SpriteToDisk(MapPixel pixel, ByteBuffer buf) {
		ByteBuffer data = null;
		File f = null;
		if (pixel instanceof TilePixel){
			//TODO vérifier un jour à quoi sert cet offset sur le premier de la zone de tuiles
			pixel.setOffsetX((short)0);
			pixel.setOffsetY((short)0);
			File dir = new File(FilesPath.getTuileDirectoryPath()+pixel.getAtlas());
			dir.mkdirs();
			f = new File(FilesPath.getTuileDirectoryPath()+pixel.getAtlas()+File.separator+pixel.getTex()+".png");
		}else{
			File dir = new File(FilesPath.getSpriteDirectoryPath()+pixel.getAtlas());
			dir.mkdirs();
			f = new File(FilesPath.getSpriteDirectoryPath()+pixel.getAtlas()+File.separator+pixel.getTex()+".png");
		}
		if (f.exists())return;
		data = ByteBuffer.allocate((int) (pixel.getLargeur()*pixel.getHauteur()));
		try{
			buf.position(pixel.getBufPos());
			buf.get(data.array());
		}catch(BufferUnderflowException e){
			e.printStackTrace();
			SpriteUtils.logger.info("sprite : "+pixel.getTex());
			SpriteUtils.logger.info("chemin : "+pixel.getAtlas());
			SpriteUtils.logger.info("type : "+pixel.getType());
			SpriteUtils.logger.info("ombre : "+pixel.getOmbre());
			SpriteUtils.logger.info("hauteur : "+pixel.getHauteur());
			SpriteUtils.logger.info("largeur : "+pixel.getLargeur());
			SpriteUtils.logger.info("offsetX : "+pixel.getOffset().x);
			SpriteUtils.logger.info("offsetY : "+pixel.getOffset().y);
			SpriteUtils.logger.info("offsetX2 : "+pixel.getOffset2().x);
			SpriteUtils.logger.info("offsetY2 : "+pixel.getOffset2().y);
			SpriteUtils.logger.info("Inconnu : "+pixel.getInconnu9());
			SpriteUtils.logger.info("Couleur trans : "+pixel.getCouleurTrans());
			SpriteUtils.logger.info("taille_zip : "+pixel.getTaille_zip());
			SpriteUtils.logger.info("taille_unzip : "+pixel.getTaille_unzip());
			System.exit(1);
		}
		data.rewind();
		ArrayList<PalettePixel> pal = pixel.getPal().pixels;
		BufferedImage img = null;
		img = new BufferedImage(pixel.getLargeur(), pixel.getHauteur(), BufferedImage.TYPE_INT_ARGB);
		int y = 0, x = 0;
		while (y<pixel.getHauteur()){
			while (x<pixel.getLargeur()){
				int b = 0;
				try{
					b = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,data.get()});
				}catch (BufferUnderflowException e){
					e.printStackTrace();
					System.exit(1);
				}
				PalettePixel px = null;
				px = pal.get(b);
				int red=0,green=0,blue=0,alpha=255;
				if (b == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,(byte) pixel.getCouleurTrans()})){
					img.setRGB(x, y, 0);
				}else{
					red = px.getRed();
					green = px.getGreen();
					blue = px.getBlue();
					int col = (alpha << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
				}
				x++;
				//logger.info("	- Pixel : "+x+","+y+" : ARGB"+tools.ByteArrayToHexString.print((byte)alpha)+","+px.red+","+px.green+","+px.blue+" index palette : "+b);
			}
			y++;
			x = 0;
		}
		drawImage(img, pixel);
	}

	/**
	 * @param pixel
	 * @param buf
	 */
	static void writeType9SpriteToDisk(MapPixel pixel, ByteBuffer buf) {
		if (pixel.getTaille_zip() == pixel.getTaille_unzip()){
			unzipSprite(pixel, buf);
		}else{
			unzipSpriteTwice(pixel, buf);
		}		
	}

	static void writeType3SpriteToDisk(MapPixel pixel) {
		//logger.info(++Params.nb_sprite+"/"+Params.total_sprites +" TYPE : VIDE "+didnom);
	}

}
