package t4cPlugin;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import t4cPlugin.DPDPalette.Pixel;

public class DDA {



	  /**
	  StructureDda = Array Of Record
	    Signature : Array [0..3] of Byte;
	    Sprites : TFastStream;
	  End;
	  */
	static HashMap<Integer,Sprite> tuiles = new HashMap<Integer,Sprite>();
	static HashMap<Integer, Sprite> sprites = new HashMap<Integer,Sprite>();
	public static HashMap<Integer, Sprite> pixels = new HashMap<Integer,Sprite>();
	static int current_sprite = 0;

	
	private ByteBuffer buf;
	private static int[] clefDda = new int[]{0x1458AAAA, 0x62421234, 0xF6C32355, 0xAAAAAAF3, 0x12344321, 0xDDCCBBAA, 0xAABBCCDD};
	private byte[] signature = new byte[4];
	private int numDDA;	
	
	public void decrypt(File f) {
		System.out.println("Lecture des entêtes dans le fichier "+f.getName());
		Params.total_sprites = DID.sprites_without_ids.size();
		numDDA = Integer.parseInt(f.getName().substring(f.getName().length()-6, f.getName().length()-4),10);
		
		HashMap<Integer,Sprite> didspritesindda = new HashMap<Integer,Sprite>();
		Iterator<Integer> iteri = DID.sprites_with_ids.keySet().iterator();
		while(iteri.hasNext()){
			int key = iteri.next();
			Sprite sp = DID.sprites_with_ids.get(key);
			if (sp.numDda == numDDA){
				didspritesindda.put(key, sp);
			}
		}
		buf = ByteBuffer.allocate((int)f.length());
		try {
			DataInputStream in = new DataInputStream (new FileInputStream(f));
			while (buf.position() < buf.capacity()){
				buf.put(in.readByte());
			}
			in.close();
		}catch(IOException exc){
			exc.printStackTrace();
			System.exit(1);
		}
		buf.rewind();
		
		signature[0] = buf.get();
		signature[1] = buf.get();
		signature[2] = buf.get();
		signature[3] = buf.get();
		
		Iterator<Integer> iter = didspritesindda.keySet().iterator();
		while(iter.hasNext()){
			int key = iter.next();
			Sprite sprite = didspritesindda.get(key);
			int indexation = sprite.indexation+4;
			try{
				buf.position(indexation);
			}catch(IllegalArgumentException e){
				e.printStackTrace();
				System.exit(1);
			}
			new DDASprite(buf, sprite);//lit l'entête du sprite et ajoute  les infos de l'entête dans le Sprite
		}
		if (Params.draw_sprites){
			didspritesindda = new HashMap<Integer,Sprite>();
			iteri = DID.sprites_without_ids.keySet().iterator();
			while(iteri.hasNext()){
				int key = iteri.next();
				Sprite sp = DID.sprites_without_ids.get(key);
				if (sp.numDda == numDDA){
					didspritesindda.put(key, sp);
				}
			}
			iter = didspritesindda.keySet().iterator();
			while(iter.hasNext()){
				int key = iter.next();
				Sprite sprite = didspritesindda.get(key);
				int indexation = sprite.indexation+4;
				try{
					buf.position(indexation);
				}catch(IllegalArgumentException e){
					e.printStackTrace();
					System.exit(1);
				}
				new DDASprite(buf, sprite);//lit l'entête du sprite et ajoute  les infos de l'entête dans le Sprite
			}
			iter = didspritesindda.keySet().iterator();
			while(iter.hasNext()){
				int key = iter.next();
				Sprite sprite = didspritesindda.get(key);
				int indexation = sprite.indexation+4;
				try{
					buf.position(indexation);
				}catch(IllegalArgumentException e){
					e.printStackTrace();
					System.exit(1);
				}
				new DDASprite(buf, sprite);//lit l'entête du sprite et ajoute  les infos de l'entête dans le Sprite
					switch (sprite.type){
						case 1 : write(sprite);
						break;
						case 2 : rleUncompress(sprite);
						break;
						case 3 : voidSprite(sprite);
						break;
						case 9 : if (sprite.taille_zip == sprite.taille_unzip){
						 	unzip(sprite);
						}else{
							unzipTwice(sprite);
						}
						break;
					}
			}
		}
	}
	
	
	private void voidSprite(Sprite didsprite) {
		System.out.println(++Params.nb_sprite+"/"+Params.total_sprites +" TYPE : VIDE "+didsprite.nom);
	}
	
	private void write(Sprite didsprite) {
		ByteBuffer data = null;
		File f = null;
		if (didsprite.tuile){
			//TODO vérifier un jour à qui sert cet offset sur le premier de la zone de tuiles
			didsprite.offsetX = 0;
			didsprite.offsetY = 0;
			File dir = new File(Params.SPRITES+"tuiles"+File.separator+didsprite.chemin);
			dir.mkdirs();
			f = new File(Params.SPRITES+"tuiles"+File.separator+didsprite.chemin+File.separator+didsprite.nom+".png");
		}else{
			File dir = new File(Params.SPRITES+"sprites"+File.separator+didsprite.chemin);
			dir.mkdirs();
			f = new File(Params.SPRITES+"sprites"+File.separator+didsprite.chemin+File.separator+didsprite.nom+".png");
		}
		if (f.exists())return;
		data = ByteBuffer.allocate(didsprite.largeur*didsprite.hauteur);
		try{
			buf.position(didsprite.bufPos);
			buf.get(data.array());
		}catch(BufferUnderflowException e){
			e.printStackTrace();
			System.out.println("sprite : "+didsprite.nom);
			System.out.println("chemin : "+didsprite.chemin);
			System.out.println("type : "+didsprite.type);
			System.out.println("ombre : "+didsprite.ombre);
			System.out.println("hauteur : "+didsprite.hauteur);
			System.out.println("largeur : "+didsprite.largeur);
			System.out.println("offsetY : "+didsprite.offsetY);
			System.out.println("offsetX : "+didsprite.offsetX);
			System.out.println("offsetY2 : "+didsprite.offsetY2);
			System.out.println("offsetX2 : "+didsprite.offsetX2);
			System.out.println("Inconnu : "+didsprite.inconnu9);
			System.out.println("Couleur trans : "+didsprite.couleurTrans);
			System.out.println("taille_zip : "+didsprite.taille_zip);
			System.out.println("taille_unzip : "+didsprite.taille_unzip);
			System.exit(1);
		}
		data.rewind();
		ArrayList<Pixel> pal = didsprite.palette.pixels;
		BufferedImage img = null;
		img = new BufferedImage(didsprite.largeur, didsprite.hauteur, BufferedImage.TYPE_INT_ARGB);
		int y = 0, x = 0;
		while (y<didsprite.hauteur){
			while (x<didsprite.largeur){
				int b = 0;
				try{
					b = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,data.get()});
				}catch (BufferUnderflowException e){
					e.printStackTrace();
					System.exit(1);
				}
				Pixel px = null;
				px = pal.get(b);
				int red=0,green=0,blue=0,alpha=255;
				if (b == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,didsprite.couleurTrans})){
					img.setRGB(x, y, 0);
				}else{
					red = px.red;
					green = px.green;
					blue = px.blue;
					int col = (alpha << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
				}
				x++;
				//System.out.println("	- Pixel : "+x+","+y+" : ARGB"+tools.ByteArrayToHexString.print((byte)alpha)+","+px.red+","+px.green+","+px.blue+" index palette : "+b);
			}
			y++;
			x = 0;
		}
		//System.out.println(Sprite.maxX/*+(2*Sprite.maxOffsetX)*/+" "+ Sprite.maxY/*+(2*Sprite.maxOffsetY)*/+" "+ Transparency.BITMASK);
		GraphicsConfiguration gc = img.createGraphics().getDeviceConfiguration();
		//BufferedImage out =	gc.createCompatibleImage(DDASprite.maxs.get(didsprite.chemin).width+(DDASprite.maxOffsets.get(didsprite.chemin).width)/*+(DDASprite.minOffsets.get(didsprite.chemin).width)*/, DDASprite.maxs.get(didsprite.chemin).height+(DDASprite.maxOffsets.get(didsprite.chemin).height)/*+(DDASprite.minOffsets.get(didsprite.chemin).height)*/, Transparency.BITMASK);
		BufferedImage out =	gc.createCompatibleImage(didsprite.largeur, didsprite.hauteur, Transparency.BITMASK);
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
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println(++Params.nb_sprite+"/"+Params.total_sprites +" TYPE : SIMPLE "+Params.t4cOUT+"SPRITES/"+didsprite.chemin+File.separator+f.getName()+" | Palette : "+didsprite.palette.nom);	
	}

	private void rleUncompress(Sprite didsprite) {
		byte b1,b2;
		int X,Y,nbpix;
		byte b = 0;
		ByteBuffer data = null;
		buf.position(didsprite.bufPos);
		Y=0;
		//pour ceux qui ont la compression Zlib en plus
		if (didsprite.largeur > 180 | didsprite.hauteur > 180) {
			Inflater unzip = new Inflater();
			byte[] bytes = new byte[(int) didsprite.taille_zip];
			buf.get(bytes);
			unzip.setInput(bytes, 0, (int) didsprite.taille_zip);
			data = ByteBuffer.allocate((int) didsprite.taille_unzip);
			try {
				unzip.inflate(data.array());
			} catch (DataFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			unzip.end();
		}else{
			data = ByteBuffer.allocate((int) didsprite.taille_zip);
			buf.get(data.array());
		}
		data.rewind();
		//on a les données du sprite dans data. on opère la décompression RLE
		ByteBuffer spriteTmp =  ByteBuffer.allocate(didsprite.hauteur*didsprite.largeur);
		spriteTmp.rewind();
		//on remplit de transparence
		for (int i=0 ; i<spriteTmp.capacity() ; i++){
			spriteTmp.put(didsprite.couleurTrans);
		}
		spriteTmp.rewind();
		if (data != null){
			while(Y != didsprite.hauteur){
				b1 = data.get();
				b2 = data.get();
				X = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
				b1 = data.get();
				b2 = data.get();
				nbpix = ((tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b1})*4) + tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b2}));
				b = data.get();
				if (b != 1){
					for (int i=0 ; i<nbpix ; i++){
						spriteTmp.put((spriteTmp.position()+i+X+(Y*didsprite.largeur)), data.get());
						if ((i+X) == (didsprite.largeur-1)) break;//sécu pour être sur de pas dépasser;
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
		
		ArrayList<Pixel> pal = didsprite.palette.pixels;
		BufferedImage img = null;
		if (didsprite.tuile){
			img = new BufferedImage(didsprite.largeur, didsprite.hauteur, BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage(didsprite.largeur, didsprite.hauteur, BufferedImage.TYPE_INT_ARGB);
		}			int y = 0, x = 0;
		while (y<didsprite.hauteur){
			while (x<didsprite.largeur){
				int c = 0;
				try{
					c = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,spriteTmp.get()});
				}catch (BufferUnderflowException e){
					e.printStackTrace();
					System.exit(1);
				}
				Pixel px = null;
				px = pal.get(c);
				int red=0,green=0,blue=0,alpha=255;
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,didsprite.couleurTrans})){
					red = px.red;
					green = px.green;
					blue = px.blue;
					int col = (0 << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
				}else{
					red = px.red;
					green = px.green;
					blue = px.blue;
					int col = (alpha << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
				}
				x++;
				//System.out.println("	- Pixel : "+x+","+y+" : ARGB"+tools.ByteArrayToHexString.print((byte)alpha)+","+px.red+","+px.green+","+px.blue+" index palette : "+b);
			}
			y++;
			x = 0;
		}
		drawImage(img,didsprite);
	}
	
	private void unzip(Sprite didsprite) {
		byte b1,b2,b3,b4;
		//new Fast_Forward(buf, 16, false, "ZLIB1");
		buf.position(didsprite.bufPos);
		b1=buf.get();
		b2=buf.get();
		b3=buf.get();
		b4=buf.get();
		int taille_unzip2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		Inflater unzip = new Inflater();
		ByteBuffer unzip_data1 = null;
		byte[] data = new byte[(int) didsprite.taille_zip];
		buf.get(data);
		unzip.setInput(data, 0, (int) didsprite.taille_zip);
		unzip_data1 = ByteBuffer.allocate((int)taille_unzip2);
		try {
			unzip.inflate(unzip_data1.array());
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		unzip.end();
		unzip_data1.rewind();
	
		ArrayList<Pixel> pal = didsprite.palette.pixels;
		BufferedImage img = null;
		if (didsprite.tuile){
			img = new BufferedImage(didsprite.largeur, didsprite.hauteur, BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage(didsprite.largeur, didsprite.hauteur, BufferedImage.TYPE_INT_ARGB);
		}			int y = 0, x = 0;
		while (y<didsprite.hauteur){
			while (x<didsprite.largeur){
				int c = 0;
				try{
					c = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,unzip_data1.get()});
				}catch (BufferUnderflowException e){
					e.printStackTrace();
					System.exit(1);
				}
				Pixel px = null;
				px = pal.get(c);
				int red=0,green=0,blue=0,alpha=255;
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,didsprite.couleurTrans})){
					img.setRGB(x, y, 0);
				}else{
					red = px.red;
					green = px.green;
					blue = px.blue;
					int col = (alpha << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
				}
				x++;
				//System.out.println("	- Pixel : "+x+","+y+" : ARGB"+tools.ByteArrayToHexString.print((byte)alpha)+","+px.red+","+px.green+","+px.blue+" index palette : "+b);
			}
			y++;
			x = 0;
		}
		drawImage(img,didsprite);
	}
	
	private void unzipTwice(Sprite didsprite) {
		byte b1,b2,b3,b4;
		Inflater unzip = new Inflater();
		ByteBuffer unzip_data1 = null;
		byte[] data = null;
		try{
			data = new byte[(int) didsprite.taille_zip];
		}catch(NegativeArraySizeException e){
			e.printStackTrace();
			System.exit(1);
		}
		buf.position(didsprite.bufPos);
		buf.get(data);
		unzip.setInput(data, 0, (int) didsprite.taille_zip);
		unzip_data1 = ByteBuffer.allocate((int) didsprite.taille_unzip);
		int resultLength = 0;
		try {
			resultLength = unzip.inflate(unzip_data1.array());
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		unzip.end();
		unzip_data1.rewind();
		//new Fast_Forward(unzip_data1, 16, false, "ZLIB2");
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
		
		ArrayList<Pixel> pal = didsprite.palette.pixels;
		BufferedImage img = null;
		if (didsprite.tuile){
			img = new BufferedImage(didsprite.largeur, didsprite.hauteur, BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage(didsprite.largeur, didsprite.hauteur, BufferedImage.TYPE_INT_ARGB);
		}
		int y = 0, x = 0;
		while (y<didsprite.hauteur){
			while (x<didsprite.largeur){
				int c = 0;
				try{
					c = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,unzip_data2.get()});
				}catch (BufferUnderflowException e){
					e.printStackTrace();
					System.exit(1);
				}
				Pixel px = null;
				px = pal.get(c);
				int red=0,green=0,blue=0,alpha=255;
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,didsprite.couleurTrans})){
					img.setRGB(x, y, 0);
				}else{
					red = px.red;
					green = px.green;
					blue = px.blue;
					int col = (alpha << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
				}
				x++;
				//System.out.println("	- Pixel : "+x+","+y+" : ARGB"+tools.ByteArrayToHexString.print((byte)alpha)+","+px.red+","+px.green+","+px.blue+" index palette : "+b);
			}
			y++;
			x = 0;
		}
		drawImage(img,didsprite);
	}
	
	private void drawImage(BufferedImage img, Sprite didsprite){
		File f = null;
		if (didsprite.tuile){
			File dir = new File(Params.SPRITES+"tuiles"+File.separator+didsprite.chemin);
			dir.mkdirs();
			f = new File(Params.SPRITES+"tuiles"+File.separator+didsprite.chemin+File.separator+didsprite.nom+".png");
		}else{
			File dir = new File(Params.SPRITES+"sprites"+File.separator+didsprite.chemin);
			dir.mkdirs();
			f = new File(Params.SPRITES+"sprites"+File.separator+didsprite.chemin+File.separator+didsprite.nom+".png");
		}
		if (f.exists())return;
		GraphicsConfiguration gc = img.createGraphics().getDeviceConfiguration();
		BufferedImage out =	gc.createCompatibleImage(didsprite.largeur, didsprite.hauteur, Transparency.BITMASK);
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
		//System.out.println(++Params.nb_sprite+"/"+Params.total_sprites +" TYPE : "+didsprite.type+" "+Params.t4cOUT+"SPRITES/"+didsprite.chemin+File.separator+f.getName()+" | Palette : "+didsprite.palette.getNom());
	}
		
	public static class DDASprite{
		int[] header = new int[7];
		long taille_zip;//int
		long taille_unzip;//int
		DPDPalette palette = null;
		
		public DDASprite(ByteBuffer buf, Sprite sprite) {
			//essai de correspondance des palettes
			if(Params.draw_sprites){
				Iterator<DPDPalette> iter_pal = DPD.palettes.iterator();
				DPDPalette bright = null;
				while (iter_pal.hasNext()){
					DPDPalette pal = iter_pal.next();
					if (pal.nom.equals("Bright1")) bright = pal;
					String nom = sprite.nom;
					String nomPal = pal.nom;
					if (nom.length() >= nomPal.length()-1){
						//Si le nom du sprite et le nom de la palette commencent pareil, on attribue la palette au sprite
						if(nom.substring(0, nomPal.length()-1).toUpperCase().contains(nomPal.substring(0, nomPal.length()-1).toUpperCase()) && palette == null) palette = pal;
					}
				}
				//Si on a pas trouvé, on attribue la palette Bright1
				if (palette == null) palette = bright;
			}
			sprite.palette = palette;
			ByteBuffer header_buf = ByteBuffer.allocate(7*Integer.SIZE);
			for(int i=0 ; i<7 ; i++){
				byte b1,b2,b3,b4;
				b1 = buf.get();
				b2 = buf.get();
				b3 = buf.get();
				b4 = buf.get();
				header[i] = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1}) ^ clefDda[i];
				header_buf.put((byte)((header[i]>>24)& 0xFF));
				//System.out.println(""+(byte)((header[i]>>24)& 0xFF));

				header_buf.put((byte)((header[i]>>16)& 0xFF));
				//System.out.println(""+(byte)((header[i]>>16)& 0xFF));

				header_buf.put((byte)((header[i]>>8)& 0xFF));
				//System.out.println(""+(byte)((header[i]>>8)& 0xFF));

				header_buf.put((byte)((header[i]>>0)& 0xFF));
				//System.out.println(""+(byte)((header[i]>>0)& 0xFF));

			}
			header_buf.rewind();
			//new Fast_Forward(header_buf,header_buf.capacity(),false,"Header : ");
			byte b1,b2,b3,b4;

			b1 = header_buf.get();
			b2 = header_buf.get();
			sprite.ombre = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("ombre : "+ombre);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			sprite.type = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("type : "+type);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			sprite.hauteur = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("hauteur : "+didsprite.hauteur);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			sprite.largeur = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("largeur : "+didsprite.largeur);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			sprite.offsetY = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
			//System.out.println("offsetY : "+didsprite.offsetY);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			sprite.offsetX = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
			//System.out.println("offsetY : "+didsprite.offsetX);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			sprite.offsetY2 = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
			//System.out.println("offsetY2 : "+offsetY2+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));

			b1 = header_buf.get();
			b2 = header_buf.get();
			sprite.offsetX2 = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
			//System.out.println("offsetX2 : "+offsetX2+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));

			b1 = header_buf.get();
			b2 = header_buf.get();
			sprite.couleurTrans = b2;//tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("Couleur trans : "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			sprite.inconnu9 = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("Inconnu : "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));

			b1 = header_buf.get();
			b2 = header_buf.get();
			b3 = header_buf.get();
			b4 = header_buf.get();			
			sprite.taille_unzip = tools.ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b1,b2,b3,b4});
			//System.out.println("taille_unzip : "+taille_unzip);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			b3 = header_buf.get();
			b4 = header_buf.get();			
			sprite.taille_zip = tools.ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b1,b2,b3,b4});
			sprite.bufPos = buf.position();

			if ((sprite.nom.contains("(")) & (sprite.nom.contains(", ")) & (sprite.nom.contains(")")) & (!Params.draw_sprites)){
				String in = sprite.nom;
				FileLister explorer = new FileLister();
				ArrayList<File> sprites = new ArrayList<File>();
				//System.err.println("TILE "+Params.SPRITES+"tuiles"+File.separator+sprite.chemin+File.separator+" "+Params.SPRITES+sprite.chemin+File.separator);
				sprites.addAll(explorer.lister(new File(Params.SPRITES+"tuiles"+File.separator+sprite.chemin+File.separator), in.substring(in.indexOf(')'))+".png"));
				int moduloX=1, moduloY=1;
				Iterator<File> iter = sprites.iterator();
				while (iter.hasNext()){
					File f = iter.next();
					try{
						int tmpX=1,tmpY=1;
						tmpX = Integer.parseInt(f.getName().substring(f.getName().indexOf('(')+1, f.getName().indexOf(',')));
						tmpY = Integer.parseInt(f.getName().substring(f.getName().indexOf(',')+2, f.getName().indexOf(')')));
						if (tmpX>moduloX)moduloX = tmpX;
						if (tmpY>moduloY)moduloY = tmpY;
						//System.err.println("ZONE "+sprite.moduloX+"|"+sprite.moduloY + " : "+sprite.nom);
					}catch(StringIndexOutOfBoundsException exc){
						System.err.println(in+"=>"+f.getPath());
						System.exit(1);
					}
				}
				sprite.moduloX = moduloX;
				sprite.moduloY = moduloY;
			}
			Iterator<Integer> iter_id = sprite.id.iterator();
			while(iter_id.hasNext()){
				int id = iter_id.next();
				//System.out.println("Calcul du modulo : "+sprite.nom+"@"+sprite.chemin+"{"+sprite.largeur+","+sprite.hauteur+"}");
				if ((sprite.largeur == 32) & (sprite.hauteur == 16) & (sprite.nom.contains("(")) & sprite.nom.contains(")")){
					sprite.tuile = true;
					DDA.tuiles.put(id,sprite);
					DDA.pixels.put(id,sprite);
					//System.err.println(sprite.moduloX+"|"+sprite.moduloY);
				} else {
					sprite.tuile = false;
					DDA.sprites.put(id,sprite);
					DDA.pixels.put(id,sprite);
					//System.err.println(sprite.hauteur+"|"+sprite.largeur);
				}
			}
			Params.STATUS = "Sprite décrypté : "+sprite.chemin+" "+sprite.nom+" "+sprite.largeur+","+sprite.hauteur+" "+sprite.moduloX+","+sprite.moduloY+" "+sprite.tuile;
		}
	}
}