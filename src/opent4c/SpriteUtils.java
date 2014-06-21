/**
 * 
 */
package opent4c;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.SpriteName;
import t4cPlugin.utils.FilesPath;
import t4cPlugin.utils.LoadingStatus;
import tools.ByteArrayToNumber;
import tools.UnsignedInt;
import tools.DataInputManager;
import tools.UnsignedShort;

/**
 * @author synoga
 *
 */
public class SpriteUtils {

	private static int nb_extracted_sprites = 0;
	private static Logger logger = LogManager.getLogger(SpriteUtils.class.getSimpleName());
	public final static int nb_expected_sprites = 68450;
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;

	
	static void drawImage(BufferedImage img, Sprite sprite){
		File f = null;
		if (sprite.isTuile()){
			File dir = new File(FilesPath.getTuileDirectoryPath()+sprite.getChemin());
			dir.mkdirs();
			f = new File(FilesPath.getTuileDirectoryPath()+sprite.getChemin()+File.separator+sprite.getName()+".png");
		}else{
			File dir = new File(FilesPath.getSpriteDirectoryPath()+sprite.getChemin());
			dir.mkdirs();
			f = new File(FilesPath.getSpriteDirectoryPath()+sprite.getChemin()+File.separator+sprite.getName()+".png");
		}
		if (f.exists())return;
		GraphicsConfiguration gc = img.createGraphics().getDeviceConfiguration();
		BufferedImage out =	gc.createCompatibleImage((int)sprite.getLargeur().getValue(), (int)sprite.getHauteur().getValue(), Transparency.BITMASK);
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

	static void unzipSpriteTwice(Sprite sprite, ByteBuffer buf) {
		byte b1,b2,b3,b4;
		Inflater unzip = new Inflater();
		ByteBuffer unzip_data1 = null;
		byte[] data = null;
		try{
			data = new byte[(int) sprite.getTaille_zip().getValue()];
		}catch(NegativeArraySizeException e){
			e.printStackTrace();
			System.exit(1);
		}
		buf.position(sprite.getBufPos());
		buf.get(data);
		unzip.setInput(data, 0, (int) sprite.getTaille_zip().getValue());
		unzip_data1 = ByteBuffer.allocate((int) sprite.getTaille_unzip().getValue());
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
		
		ArrayList<PalettePixel> pal = sprite.getPalette().pixels;
		BufferedImage img = null;
		if (sprite.isTuile()){
			img = new BufferedImage((int)sprite.getLargeur().getValue(), (int)sprite.getHauteur().getValue(), BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage((int)sprite.getLargeur().getValue(), (int)sprite.getHauteur().getValue(), BufferedImage.TYPE_INT_ARGB);
		}
		int y = 0, x = 0;
		while (y<sprite.getHauteur().getValue()){
			while (x<sprite.getLargeur().getValue()){
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
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,(byte) sprite.getCouleurTrans().getValue()})){
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
		drawImage(img,sprite);
	}

	static void unzipSprite(Sprite sprite, ByteBuffer buf) {
		byte b1,b2,b3,b4;
		buf.position(sprite.getBufPos());
		b1=buf.get();
		b2=buf.get();
		b3=buf.get();
		b4=buf.get();
		int taille_unzip2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		Inflater unzip = new Inflater();
		ByteBuffer unzip_data1 = null;
		byte[] data = new byte[(int) sprite.getTaille_zip().getValue()];
		buf.get(data);
		unzip.setInput(data, 0, (int) sprite.getTaille_zip().getValue());
		unzip_data1 = ByteBuffer.allocate((int)taille_unzip2);
		try {
			unzip.inflate(unzip_data1.array());
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		unzip.end();
		unzip_data1.rewind();
	
		ArrayList<PalettePixel> pal = sprite.getPalette().pixels;
		BufferedImage img = null;
		if (sprite.isTuile()){
			img = new BufferedImage((int)sprite.getLargeur().getValue(), (int)sprite.getHauteur().getValue(), BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage((int)sprite.getLargeur().getValue(), (int)sprite.getHauteur().getValue(), BufferedImage.TYPE_INT_ARGB);
		}			int y = 0, x = 0;
		while (y<sprite.getHauteur().getValue()){
			while (x<sprite.getLargeur().getValue()){
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
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,(byte) sprite.getCouleurTrans().getValue()})){
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
		drawImage(img,sprite);
	}

	static void rleUncompress(Sprite sprite, ByteBuffer buf) {
		byte b1,b2;
		int X,Y,nbpix;
		byte b = 0;
		ByteBuffer data = null;
		buf.position(sprite.getBufPos());
		Y=0;
		//pour ceux qui ont la compression Zlib en plus
		if (sprite.getLargeur().getValue() > 180 | sprite.getHauteur().getValue() > 180) {
			Inflater unzip = new Inflater();
			byte[] bytes = new byte[(int) sprite.getTaille_zip().getValue()];
			buf.get(bytes);
			unzip.setInput(bytes, 0, (int) sprite.getTaille_zip().getValue());
			data = ByteBuffer.allocate((int) sprite.getTaille_unzip().getValue());
			try {
				unzip.inflate(data.array());
			} catch (DataFormatException e) {
				e.printStackTrace();
				logger.fatal(e);
				System.exit(1);
			}
			unzip.end();
		}else{
			data = ByteBuffer.allocate((int) sprite.getTaille_zip().getValue());
			buf.get(data.array());
		}
		data.rewind();
		//on a les données du sprite dans data. on opère la décompression RLE
		ByteBuffer spriteTmp =  ByteBuffer.allocate((int) (sprite.getHauteur().getValue()*sprite.getLargeur().getValue()));
		spriteTmp.rewind();
		//on remplit de transparence
		for (int i=0 ; i<spriteTmp.capacity() ; i++){
			spriteTmp.put((byte) sprite.getCouleurTrans().getValue());
		}
		spriteTmp.rewind();
		if (data != null){
			while(Y != sprite.getHauteur().getValue()){
				b1 = data.get();
				b2 = data.get();
				X = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
				b1 = data.get();
				b2 = data.get();
				nbpix = ((tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b1})*4) + tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b2}));
				b = data.get();
				if (b != 1){
					for (int i=0 ; i<nbpix ; i++){
						spriteTmp.put((int) (spriteTmp.position()+i+X+(Y*sprite.getLargeur().getValue())), data.get());
						if ((i+X) == (sprite.getLargeur().getValue()-1)) break;//sécu pour être sur de pas dépasser;
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
		
		ArrayList<PalettePixel> pal = sprite.getPalette().pixels;
		BufferedImage img = null;
		if (sprite.isTuile()){
			img = new BufferedImage((int)sprite.getLargeur().getValue(), (int)sprite.getHauteur().getValue(), BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage((int)sprite.getLargeur().getValue(), (int)sprite.getHauteur().getValue(), BufferedImage.TYPE_INT_ARGB);
		}			int y = 0, x = 0;
		while (y<sprite.getHauteur().getValue()){
			while (x<sprite.getLargeur().getValue()){
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
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,(byte) sprite.getCouleurTrans().getValue()})){
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
		drawImage(img,sprite);
	}

	static void write(Sprite sprite, ByteBuffer buf) {
		ByteBuffer data = null;
		File f = null;
		if (sprite.isTuile()){
			//TODO vérifier un jour à quoi sert cet offset sur le premier de la zone de tuiles
			sprite.setOffsetX((short)0);
			sprite.setOffsetY((short)0);
			File dir = new File(FilesPath.getTuileDirectoryPath()+sprite.getChemin());
			dir.mkdirs();
			f = new File(FilesPath.getTuileDirectoryPath()+sprite.getChemin()+File.separator+sprite.getName()+".png");
		}else{
			File dir = new File(FilesPath.getSpriteDirectoryPath()+sprite.getChemin());
			dir.mkdirs();
			f = new File(FilesPath.getSpriteDirectoryPath()+sprite.getChemin()+File.separator+sprite.getName()+".png");
		}
		if (f.exists())return;
		data = ByteBuffer.allocate((int) (sprite.getLargeur().getValue()*sprite.getHauteur().getValue()));
		try{
			buf.position(sprite.getBufPos());
			buf.get(data.array());
		}catch(BufferUnderflowException e){
			e.printStackTrace();
			logger.info("sprite : "+sprite.getName());
			logger.info("chemin : "+sprite.getChemin());
			logger.info("type : "+sprite.getType());
			logger.info("ombre : "+sprite.getOmbre());
			logger.info("hauteur : "+sprite.getHauteur());
			logger.info("largeur : "+sprite.getLargeur());
			logger.info("offsetY : "+sprite.getOffsetY());
			logger.info("offsetX : "+sprite.getOffsetX());
			logger.info("offsetY2 : "+sprite.getOffsetY2());
			logger.info("offsetX2 : "+sprite.getOffsetX2());
			logger.info("Inconnu : "+sprite.getInconnu9());
			logger.info("Couleur trans : "+sprite.getCouleurTrans());
			logger.info("taille_zip : "+sprite.getTaille_zip());
			logger.info("taille_unzip : "+sprite.getTaille_unzip());
			System.exit(1);
		}
		data.rewind();
		ArrayList<PalettePixel> pal = sprite.getPalette().pixels;
		BufferedImage img = null;
		img = new BufferedImage((int)sprite.getLargeur().getValue(), (int)sprite.getHauteur().getValue(), BufferedImage.TYPE_INT_ARGB);
		int y = 0, x = 0;
		while (y<sprite.getHauteur().getValue()){
			while (x<sprite.getLargeur().getValue()){
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
				if (b == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,(byte) sprite.getCouleurTrans().getValue()})){
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
		drawImage(img, sprite);
	}

	static void voidSprite(Sprite didsprite) {
		//logger.info(++Params.nb_sprite+"/"+Params.total_sprites +" TYPE : VIDE "+didsprite.getName());
	}

	static boolean doTheWriting(Sprite sprite, ByteBuffer buf){
		boolean writen = false;
		switch(sprite.getType().getValue()){
			case 1 : write(sprite, buf); break;
			case 2 : rleUncompress(sprite, buf); break;
			case 3 : voidSprite(sprite); writen = true; break;
			case 9 : extractType9(sprite, buf); break;
		}
		File f;
		if (sprite.isTuile()){
			f = new File(FilesPath.getTuileDirectoryPath()+sprite.getChemin()+File.separator+sprite.getName()+".png");		
		}else{
			f = new File(FilesPath.getSpriteDirectoryPath()+sprite.getChemin()+File.separator+sprite.getName()+".png");
		}
		if(f.exists())writen = true;
		return writen;
	}

	/**
	 * @param sprite
	 * @param buf
	 */
	private static void extractType9(Sprite sprite, ByteBuffer buf) {
		if (sprite.getTaille_zip().getValue() == sprite.getTaille_unzip().getValue()){
			unzipSprite(sprite, buf);
		}else{
			//sprite.printInfos();
			unzipSpriteTwice(sprite, buf);
		}		
	}

	/**
	 * Extract a sprite from a .dda file
	 * @param buf
	 * @param sprite
	 */
	static void extractDDASprite(ByteBuffer buf, Sprite sprite) {
		Palette p = extractPalette(sprite);
		if(p == null){
			logger.fatal("On n'a pas trouvé une palette : "+sprite.getName());
			System.exit(1);
		}
		sprite.setPalette(p);
		logger.info("Palette : "+sprite.getName()+"<=>"+sprite.getPaletteName());
		ByteBuffer header_buf = extractHeaderBuffer(buf);
		sprite.setOmbre(new UnsignedShort(extractShort(header_buf,true)));
		sprite.setType(new UnsignedShort(extractShort(header_buf,true)));
		sprite.setHauteur(new UnsignedShort(extractShort(header_buf,true)));
		sprite.setLargeur(new UnsignedShort(extractShort(header_buf,true)));
		sprite.setOffsetY(ByteArrayToNumber.bytesToShort(extractShort(header_buf,true)));
		sprite.setOffsetX(ByteArrayToNumber.bytesToShort(extractShort(header_buf,true)));
		sprite.setOffsetY2(ByteArrayToNumber.bytesToShort(extractShort(header_buf,true)));
		sprite.setOffsetX2(ByteArrayToNumber.bytesToShort(extractShort(header_buf,true)));
		sprite.setCouleurTrans(new UnsignedShort(extractShort(header_buf,true)));
		sprite.setInconnu9(new UnsignedShort(extractShort(header_buf,true)));
		sprite.setTaille_unzip(new UnsignedInt(extractInt(header_buf,true)));
		sprite.setTaille_zip(new UnsignedInt(extractInt(header_buf,true)));		
		sprite.setBufPos(buf.position());
		sprite.setTuile(extractTuileInfo(sprite));
		//logger.info("Type : "+sprite.getType().getValue());
	}

	/**
	 * Ici on sépare les tuiles des sprites. la base c'est que les tuiles font 32 x 16 et les sprites non. Cependant ce ne serait pas drôle s'il n'y avait pas quelques exceptions
	 * donc on les gère au cas par cas (il n'y en a pas 12 000 non plus).
	 * @param sprite
	 * @return
	 */
	private static boolean extractTuileInfo(Sprite sprite) {
		boolean result = false;
		if ((sprite.getLargeur().getValue() == 32) && (sprite.getHauteur().getValue() == 16)) result = true;
		if (sprite.getChemin().equals("All"))result = false;
		if (sprite.getChemin().equals("BlackLeatherBoots"))result = false;
		if (sprite.getChemin().equals("CemeteryGates"))result = false;
		if (sprite.getChemin().equals("DarkSword"))result = false;
		if (sprite.getChemin().equals("GoldenMorningStar"))result = false;
		if (sprite.getChemin().equals("LeatherBoots"))result = false;
		if (sprite.getChemin().equals("MorningStar"))result = false;
		if (sprite.getChemin().equals("OgreClub"))result = false;
		if (sprite.getChemin().equals("PlateBoots"))result = false;
		if (sprite.getChemin().equals("SkavenClub"))result = false;
		if (sprite.getChemin().equals("StoneShard"))result = false;
		if (sprite.getChemin().equals("PlateLeftArm"))result = false;
		if (sprite.getChemin().equals("Bow07"))result = false;
		if (sprite.getChemin().equals("Dague05"))result = false;
		if (sprite.getChemin().equals("Hache06"))result = false;
		if (sprite.getChemin().equals("Hammer01"))result = false;
		if (sprite.getChemin().equals("LevelUp"))result = false;
		if (sprite.getChemin().equals("NMFire"))result = false;
		if (sprite.getChemin().equals("NMLightning"))result = false;
		if (sprite.getChemin().equals("NMPoison"))result = false;
		if (sprite.getChemin().equals("NMSSupraHeal"))result = false;
		if (sprite.getChemin().equals("OnGround"))result = false;
		if (sprite.getChemin().equals("SP03"))result = false;
		if (sprite.getChemin().equals("SP04"))result = false;
		if (sprite.getChemin().equals("SP05"))result = false;
		if (sprite.getChemin().equals("Sword01"))result = false;
		if (sprite.getChemin().equals("V2Effect"))result = false;
		if (sprite.getChemin().equals("Weather"))result = false;
		if (sprite.getChemin().equals("GenericMerge3"))result = false;
		if (sprite.getChemin().equals("Root"))result = false;
		if (sprite.getChemin().equals("VSSmooth"))result = false;
		if (sprite.getChemin().equals("GenericMerge1"))result = false;
		if (sprite.getChemin().equals("GenericMerge2Wooden"))result = false;
		if (sprite.getChemin().equals("WoodenSmooth"))result = false;
		if (sprite.getChemin().equals("Black"))result = false;
		if (sprite.getChemin().equals("Black"))result = false;
		
		return result;
	}

	/**
	 * 	essai de correspondance des palettes
	 *  dans l'idée, on cherche si le nom du sprite contient le nom d'une palette
	 *	sinon, on attribue la palette Bright1
	 * @return
	 */
	private static Palette extractPalette(Sprite sprite) {
		Palette result = SpriteManager.palettes.get("Bright1");
		if (result == null){
			logger.fatal("On n'est pas parvenu à prendre une palette dans la liste : "+sprite.getName()+" / "+SpriteManager.palettes.containsKey("Bright1"));
			System.exit(1);
		}
		Iterator<String> iter_pal = SpriteManager.palettes.keySet().iterator();
		while (iter_pal.hasNext()){
			String pal = iter_pal.next();
			String nom = sprite.getName();
			if (nom.contains(pal)){
				result = SpriteManager.palettes.get(pal);
				if (result == null){
					logger.fatal("On n'est pas parvenu à prendre une palette dans la liste : "+sprite.getName()+" / "+SpriteManager.palettes.containsKey(pal));
					System.exit(1);
				}
				break;
			}
		}

		return result;
	}

	/**
	 * @return
	 */
	private static ByteBuffer extractHeaderBuffer(ByteBuffer buf) {
		final int[] clefDda = new int[]{0x1458AAAA, 0x62421234, 0xF6C32355, 0xAAAAAAF3, 0x12344321, 0xDDCCBBAA, 0xAABBCCDD};
		ByteBuffer result = ByteBuffer.allocate(7*Integer.SIZE);
		int[] header = new int[7];
		for(int i=0 ; i<7 ; i++){
			byte b1,b2,b3,b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			header[i] = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1}) ^ clefDda[i];
			result.put((byte)((header[i]>>24)& 0xFF));
			result.put((byte)((header[i]>>16)& 0xFF));
			result.put((byte)((header[i]>>8)& 0xFF));
			result.put((byte)((header[i]>>0)& 0xFF));
		}
		result.rewind();
		return result;
	}

	/**
	 * @param buffer
	 * @return
	 */
	private static byte[] extractShort(ByteBuffer buffer, boolean bigEndian) {
		byte b1,b2;
		byte[] result = new byte[2];
		b1 = buffer.get();
		b2 = buffer.get();
		if (bigEndian){
			result[0] = b1;
			result[1] = b2;
		}else{
			result[0] = b2;
			result[1] = b1;
		}
		return result;
	}

	/**
	 * 
	 * @param header_buf
	 * @param bigEndian
	 * @return
	 */
	public static byte[] extractInt(ByteBuffer header_buf, boolean bigEndian) {
		byte b1,b2,b3,b4;
		b1 = header_buf.get();
		b2 = header_buf.get();
		b3 = header_buf.get();
		b4 = header_buf.get();
		byte[] result = new byte[4];
		if (bigEndian){
			result[0] = b1;
			result[1] = b2;
			result[2] = b3;
			result[3] = b4;
		}else{
			result[0] = b4;
			result[1] = b3;
			result[2] = b2;
			result[3] = b1;
		}

		return result;
	}

	/**
	 * @param header_buf
	 */
	public static long extractLong(ByteBuffer header_buf, boolean bigEndian) {
		byte b1,b2,b3,b4,b5,b6,b7,b8;
		b1 = header_buf.get();
		b2 = header_buf.get();
		b3 = header_buf.get();
		b4 = header_buf.get();	
		b5 = header_buf.get();
		b6 = header_buf.get();
		b7 = header_buf.get();
		b8 = header_buf.get();	
		byte[] result = new byte[8];
		if (bigEndian){
			result[0] = b1;
			result[1] = b2;
			result[2] = b3;
			result[3] = b4;
			result[4] = b5;
			result[5] = b6;
			result[6] = b7;
			result[7] = b8;
		}else{
			result[0] = b8;
			result[1] = b7;
			result[2] = b6;
			result[3] = b5;
			result[4] = b4;
			result[5] = b3;
			result[6] = b2;
			result[7] = b1;
		}
		return tools.ByteArrayToNumber.bytesToLong(result);
	}

	/**
	 * Decrypts a dda file
	 * @param f
	 * @param doWrite
	 */
	public static void decrypt_dda_file(File f, boolean doWrite) {
		int numDDA = Integer.parseInt(f.getName().substring(f.getName().length()-6, f.getName().length()-4),10);
		
		Map<SpriteName,Sprite> sprites_in_dda = new HashMap<SpriteName,Sprite>();
		Iterator<SpriteName> iteri = SpriteManager.getSprites().keySet().iterator();
		while(iteri.hasNext()){
			SpriteName key = iteri.next();
			Sprite sp = SpriteManager.getSprites().get(key);
			if (sp.getNumDda() == numDDA){
				sprites_in_dda.put(key, sp);
			}
		}
		ByteBuffer buf = readDDA(f);
		byte[] signature = new byte[4];
		signature = extractBytes(buf,signature.length);
		Iterator<SpriteName> iter = sprites_in_dda.keySet().iterator();
		while(iter.hasNext()){
			SpriteName key = iter.next();
			Sprite sprite = sprites_in_dda.get(key);
			int indexation = (int) (sprite.getIndexation().getValue()+4);
			try{
				buf.position(indexation);
			}catch(IllegalArgumentException e){
				e.printStackTrace();
				System.exit(1);
			}
			extractDDASprite(buf, sprite);//lit l'entête du sprite et ajoute  les infos de l'entête dans le Sprite
			//sprite.printInfos();
			loadingStatus.addOneSpriteFromDDA();
			if(doWrite){
				boolean writen = false;
				writen = doTheWriting(sprite, buf);
				if (!writen){
					logger.fatal("Sprite non écrit => "+sprite.getName());
					sprite.printInfos();
					System.exit(1);
				}
			}
			if (sprite.getType().getValue() != 9) nb_extracted_sprites++;
			UpdateScreenManagerStatus.setSubStatus("Sprites extraits des fichiers DDA: "+nb_extracted_sprites+"/"+nb_expected_sprites);
		}
	}

	/**
	 * @param f
	 * @return
	 */
	private static ByteBuffer readDDA(File f) {
		ByteBuffer result = ByteBuffer.allocate((int)f.length());
		try {
			DataInputManager in = new DataInputManager (f);
			while (result.position() < result.capacity()){
				result.put(in.readByte());
			}
			in.close();
		}catch(IOException exc){
			exc.printStackTrace();
			System.exit(1);
		}
		result.rewind();
		return result;
	}

	/**
	 * Extracts a Sprite's name
	 * @param buf
	 * @return
	 */
	static SpriteName extractName(ByteBuffer buf) {
		byte[] bytes = new byte[64];
		buf.get(bytes);
		String nomExtrait = new String(bytes);
		nomExtrait = nomExtrait.substring(0, nomExtrait.indexOf(0x00));
		nomExtrait = nomExtrait.replace("_","");
		if (nomExtrait.equals("Cemetery Gates /^"))nomExtrait = "Cemetery Gates1";
		if (nomExtrait.equals("Cemetery Gates /"))nomExtrait = "Cemetery Gates2";
		if (nomExtrait.equals("Cemetery Gates \\v"))nomExtrait = "Cemetery Gates3";
		if (nomExtrait.equals("Cemetery Gates v"))nomExtrait = "Cemetery Gates4";
		if (nomExtrait.equals("Cemetery Gates -"))nomExtrait = "Cemetery Gates5";
		if (nomExtrait.equals("Cemetery Gates >"))nomExtrait = "Cemetery Gates6";
		if (nomExtrait.equals("Cemetery Gates ^"))nomExtrait = "Cemetery Gates7";
		if (nomExtrait.equals("Cemetery Gates X"))nomExtrait = "Cemetery Gates8";
		if (nomExtrait.equals("Cemetery Gates .|"))nomExtrait = "Cemetery Gates9";
		return new SpriteName(nomExtrait);
	}

	/**
	 * Extracts a Sprite's path
	 * @param buf
	 * @return
	 */
	static String extractChemin(ByteBuffer buf) {
		byte[] bytes = new byte[256];
		buf.get(bytes);
		String result = new String(bytes);
		result = new String(bytes);
		result = result.substring(0, result.indexOf(0x00));
		result = result.replace("_","");
		result = result.replace(".","");
		result = result.replace("\\", "/");
		String[] split;
		split = result.split("\\/");
		result = split[split.length-1];
		return result;
	}

	/**
	 * extract the position in the buffer to decode a sprite
	 * @param buf
	 * @return
	 */
	/*static int extractIndexation(ByteBuffer buf) {
		byte b1,b2,b3,b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		return tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
	}*/

	/**
	 * extract the .dda number associated to a sprite
	 * @param buf
	 * @return
	 */
	/*static long extractNumDDA(ByteBuffer buf) {
		byte b1,b2,b3,b4,b5,b6,b7,b8;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		b5 = buf.get();
		b6 = buf.get();
		b7 = buf.get();
		b8 = buf.get();
		return tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
	}*/

	/**
	 * Gets IDs from id.txt file
	 * puts info into a HashMap<Integer,SpriteName>
	 */
	public static void loadIdsFromFile(){
		SpriteManager.ids = new HashMap<Integer,SpriteName>();
		File id_file = new File(FilesPath.getIdFilePath());
		logger.info("Lecture du fichier "+id_file.getName());
		try{
			BufferedReader buff = new BufferedReader(new FileReader(id_file.getPath()));			 
			try {
				String line;
				while ((line = buff.readLine()) != null) {
					SpriteUtils.readIdLine(line);
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Reads a line from id.txt
	 * @param line
	 */
	static void readIdLine(String line){
		int key = 0;
		String value = "";
		key = Integer.parseInt(line.substring(0, line.indexOf(' ')));
		value = line.substring(line.indexOf("Name: ")+6);
		value = value.replace("_","");
		SpriteName name = new SpriteName(value);
		SpriteManager.ids.put(key,name);
	}

	/**
	 * Extracts Palettes from dpd file
	 * @param bufUnZip
	 * @return
	 */
	static Map<String, Palette> extractPalettes(ByteBuffer bufUnZip, int nb_palettes) {
		Map<String,Palette> result = new HashMap<String,Palette>(nb_palettes);
		for(int i=1 ; i<=nb_palettes ; i++){
			Palette p = new Palette(bufUnZip);
			UpdateScreenManagerStatus.setSubStatus("Palettes extraites : "+i+"/"+nb_palettes);
			result.put(p.getNom(),p);
		}
		return result;
	}

	/**
	 * Unzips a file
	 * @param buf
	 * @param size
	 * @return a ByteBuffer with unzipped data
	 */
	static ByteBuffer unzip(ByteBuffer buf, int size) {
		
		ByteBuffer result = ByteBuffer.allocate(size);
		Inflater decompresser = new Inflater();
	    decompresser.setInput(buf.array(), 0, buf.capacity());
	    try {
			decompresser.inflate(result.array());
		} catch (DataFormatException e) {
			e.printStackTrace();
			logger.fatal("Décompression échouée");
			System.exit(1);
		}
	    decompresser.end();
	    return result;
	}

	/**
	 * 
	 * @param header
	 * @return a byte array with the second part of the md5 checksum
	 */
	/*static byte[] getHeaderHashMd52(ByteBuffer header) {
		byte[] result = new byte[17];
		for (int i=0 ; i<17 ; i++){
			result[i] = header.get();
		}
		return result;
	}*/

	/**
	 * 
	 * @param buffer
	 * @return a byte array with the second part of the md5 checksum
	 */
	/*static byte[] getHeaderHashMD5(ByteBuffer buffer) {
		byte[] result = new byte[16];
		for (int i=0 ; i<16 ; i++){
			result[i] = buffer.get();
		}
		return result;
	}*/

	/**
	 * 
	 * @param buffer
	 * @param size
	 * @return
	 */
	public static byte[] extractBytes(ByteBuffer buffer, int size){
		byte[] result = new byte[size];
		for (int i=0 ; i<size ; i++){
			result[i] = buffer.get();
		}
		return result;
	}
	
}
