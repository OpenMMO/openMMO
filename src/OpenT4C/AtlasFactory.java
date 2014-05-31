package OpenT4C;

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
import java.util.List;
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

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

import OpenT4C.DPDPalette.Pixel;
import t4cPlugin.FileLister;
import t4cPlugin.Params;
import t4cPlugin.SpriteName;
import t4cPlugin.utils.FilesPath;
import t4cPlugin.utils.LoadingStatus;
import t4cPlugin.utils.RunnableCreatorUtil;
import t4cPlugin.utils.ThreadsUtil;
import tools.DataInputManager;

public enum AtlasFactory {
	
	INSTANCE;
	
	private static Logger logger = LogManager.getLogger(AtlasFactory.class.getSimpleName());
	
	static ArrayList<DPDPalette> palettes = new ArrayList<DPDPalette>();
	static Map<Integer,SpriteName> ids = new HashMap<Integer,SpriteName>();
	static Map<Integer,Sprite> sprites_with_ids = new HashMap<Integer,Sprite>();
	static Map<Integer,Sprite> sprites_without_ids = new HashMap<Integer,Sprite>();
	static List<Sprite> sprite_list = new ArrayList<Sprite>();
	static Sprite black;
	static Map<Integer,Sprite> tuiles = new HashMap<Integer,Sprite>();
	static Map<Integer, Sprite> sprites = new HashMap<Integer,Sprite>();
	static Map<Integer, Sprite> pixels = new HashMap<Integer,Sprite>();
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;


	
	public static void make(){
		//On décrypte les palettes
		decryptDPD();
		//On décrypte les informations des sprites
		decryptDID();
		//On décrypte les images avec les informations.
		decryptDDA();
		//On crée les atlas de sprites
		pack_sprites();
		//On crée les atlas de tuiles
		pack_tuiles();
	}
	/**
	 * On extrait les palettes du fichier dpd
	 */
	private static void decryptDPD() {
		
		ByteBuffer buf = null;
		ByteBuffer header;
		ByteBuffer bufUnZip;
		
		byte[] header_hashMd5 = new byte[16];
		byte[] header_hashMd52 = new byte[17];
		byte[] header_hash = new byte[32];
		
		final byte clef = (byte) 0x66;
		
		int header_taille_unZip = 0;
		int header_taille_zip;
		int taille_unZip = 0;
		int nb_palettes;
		
		byte checksum;
		byte azt;
		
		File f = null;
		
		Iterator<File> iter_dpd = DataChecker.sourceData.keySet().iterator();
		while (iter_dpd.hasNext()){
			f = iter_dpd.next();
			if(f.getName().endsWith(".dpd")){
				break;
			}
		}
		
		logger.info("Lecture du fichier "+f.getName());
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
			//logger.info("	- HashMD5 : "+tools.ByteArrayToHexString.print(header_hashMd5));
			b1 = header.get();
			b2 = header.get();
			b3 = header.get();
			b4 = header.get();
			header_taille_unZip = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("	- header_taille_unZip : "+header_taille_unZip);
			b1 = header.get();
			b2 = header.get();
			b3 = header.get();
			b4 = header.get();
			header_taille_zip = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("	- header_taille_zip : "+header_taille_zip);
			for (int i=0 ; i<17 ; i++){
				header_hashMd52[i] = header.get();
			}
			//logger.info("	- HashMD52 : "+tools.ByteArrayToHexString.print(header_hashMd52));
			ByteBuffer buf_hash = ByteBuffer.allocate(33);
			buf_hash.put(header_hashMd5);
			buf_hash.put(header_hashMd52);
			buf_hash.rewind();
			buf_hash.get(header_hash);
			azt = header_hashMd52[16];
			//logger.info(new String(header_hash));
			//logger.info(tools.ByteArrayToHexString.print(header_hash));
			//logger.info(""+azt);
			//logger.info(tools.ByteArrayToHexString.print(new byte[]{azt}));
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
		//logger.info("	- Taille décompressée : "+taille_unZip);
		//Ensuite on décrypte le fichier avec la clé
		for (int i=0; i<bufUnZip.capacity(); i++){
			bufUnZip.array()[i] ^= clef;
		}
		nb_palettes = taille_unZip/(64 + 768);
		Params.total_palette = nb_palettes;
		//logger.info("	- Nombre de palettes : "+nb_palettes);
		for(int i=0 ; i<nb_palettes ; i++){
			palettes.add(new DPDPalette(bufUnZip));
		}
		//TODO Checksum
		logger.info("Fichier "+f.getName()+" lu : "+palettes.size()+" palettes.");
	}		

	/**
	 * On extrait les informations de sprites du fichier did
	 */
	private static void decryptDID(){
		
		ByteBuffer buf = null;
		ByteBuffer header;
		ByteBuffer bufUnZip;
		
		byte clef = (byte) 0x99;
		
		byte[] header_hashMd5 = new byte[16];
		byte[] header_hashMd52 = new byte[17];
		
		int header_taille_unZip = 0;
		int header_taille_zip;
		int taille_unZip = 0;
		int nb_sprites;
				
		File f = null;
		Iterator<File> iter_did = DataChecker.sourceData.keySet().iterator();
		while (iter_did.hasNext()){
			f = iter_did.next();
			if(f.getName().endsWith(".did")){
				break;
			}
		}
		
		File id_file = null;
		Iterator<File> iter_id = DataChecker.sourceData.keySet().iterator();
		while (iter_id.hasNext()){
			id_file = iter_id.next();
			if(id_file.getName().equals("id.txt")){
				break;
			}
		}
		
		logger.info("Lecture du fichier "+f.getName());
		//d'abord on récupère les ID depuis notre fichier
		//et on formate tout ça pour avoir une hashmap <int, string>
		String filePath = id_file.getPath();
		try{
			BufferedReader buff = new BufferedReader(new FileReader(filePath));
			 
			try {
				String line;
				while ((line = buff.readLine()) != null) {
					int key = 0;
					String value = "";
					key = Integer.parseInt(line.substring(0, line.indexOf(' ')));
					value = line.substring(line.indexOf("Name: ")+6);
					SpriteName name = new SpriteName(value);
					ids.put(key,name);
					logger.info("ID mappée : "+key+"=>"+name.getName());
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();System.exit(1);
		}
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
			//logger.info("	- HashMD5 : "+tools.ByteArrayToHexString.print(header_hashMd5));
			b1 = header.get();
			b2 = header.get();
			b3 = header.get();
			b4 = header.get();
			header_taille_unZip = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("	- header_taille_unZip : "+header_taille_unZip);
			b1 = header.get();
			b2 = header.get();
			b3 = header.get();
			b4 = header.get();
			header_taille_zip = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
			//logger.info("	- header_taille_zip : "+header_taille_zip);
			for (int i=0 ; i<17 ; i++){
				header_hashMd52[i] = header.get();
			}
			//logger.info("	- HashMD52 : "+tools.ByteArrayToHexString.print(header_hashMd52));
			//logger.info("	- Taille header : "+header.position());

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
		//logger.info("	- Taille décompressée : "+taille_unZip+"/"+header_taille_zip);

		//Ensuite on décrypte le fichier avec la clé
		for (int i=0; i<bufUnZip.capacity(); i++){
			bufUnZip.array()[i] ^= clef;
		}
		nb_sprites = taille_unZip/(64 + 256 + 4 + 8);
		//logger.info("	- Nombre de sprites : "+nb_sprites);

		for(int i=0 ; i<nb_sprites ; i++){
			//logger.info("Ajout du sprite "+i+"/"+nb_sprites);
			sprite_list.add(new Sprite(bufUnZip));
		}
	}
	
	/**
	 * On extrait les images des fichiers dda
	 */
	private static void decryptDDA(){
		File f = null;
		ArrayList<File> ddas = new ArrayList<File>();
		Iterator<File> iter_dda = DataChecker.sourceData.keySet().iterator();
		while (iter_dda.hasNext()){
			f = iter_dda.next();
			if(f.getName().endsWith(".dda")){
				ddas.add(f);
			}
		}
		
		iter_dda = ddas.iterator();
		while (iter_dda.hasNext()){
			f = iter_dda.next();
			logger.info("Extraction des images dans "+f.getName());
			decrypt_dda_file(f);
		}
		
		logger.info("Calcul des modulos sur les tuiles");
		Iterator<Integer>iter_tuiles = tuiles.keySet().iterator();
		while(iter_tuiles.hasNext()){
			DDASprite.computeModulo(tuiles.get(iter_tuiles.next()));
		}
		
	}
	
	private static void decrypt_dda_file(File f) {
		ByteBuffer buf;
		byte[] signature = new byte[4];
		int numDDA;
		//logger.info("Lecture des entêtes dans le fichier "+f.getName());
		Params.total_sprites = AtlasFactory.getSprites_without_ids().size();
		numDDA = Integer.parseInt(f.getName().substring(f.getName().length()-6, f.getName().length()-4),10);
		
		Map<Integer,Sprite> didspritesindda = new HashMap<Integer,Sprite>();
		Iterator<Integer> iteri = AtlasFactory.getSprites_with_ids().keySet().iterator();
		while(iteri.hasNext()){
			int key = iteri.next();
			Sprite sp = AtlasFactory.getSprites_with_ids().get(key);
			if (sp.numDda == numDDA){
				didspritesindda.put(key, sp);
			}
		}
		buf = ByteBuffer.allocate((int)f.length());
		try {
			DataInputManager in = new DataInputManager (f);
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
			switch (sprite.type){
				case 1 : write(sprite, buf);
				break;
				case 2 : rleUncompress(sprite, buf);
				break;
				case 3 : voidSprite(sprite);
				break;
				case 9 : if (sprite.taille_zip == sprite.taille_unzip){
					unzip(sprite, buf);
				}else{
					unzipTwice(sprite, buf);
				}
				break;
			}
			
			didspritesindda = new HashMap<Integer,Sprite>();
			iteri = AtlasFactory.getSprites_without_ids().keySet().iterator();
			while(iteri.hasNext()){
				key = iteri.next();
				Sprite sp = AtlasFactory.getSprites_without_ids().get(key);
				if (sp.numDda == numDDA){
					didspritesindda.put(key, sp);
				}
			}
			iter = didspritesindda.keySet().iterator();
			while(iter.hasNext()){
				key = iter.next();
				sprite = didspritesindda.get(key);
				indexation = sprite.indexation+4;
				try{
					buf.position(indexation);
				}catch(IllegalArgumentException e){
					e.printStackTrace();
					System.exit(1);
				}
				new DDASprite(buf, sprite);//lit l'entête du sprite et ajoute  les infos de l'entête dans le Sprite
				switch (sprite.type){
				case 1 : write(sprite, buf);
				break;
				case 2 : rleUncompress(sprite, buf);
				break;
				case 3 : voidSprite(sprite);
				break;
				case 9 : if (sprite.taille_zip == sprite.taille_unzip){
					unzip(sprite, buf);
				}else{
					unzipTwice(sprite, buf);
				}
				break;
				}
			}
		}
	}
	
	private static void voidSprite(Sprite didsprite) {
		//logger.info(++Params.nb_sprite+"/"+Params.total_sprites +" TYPE : VIDE "+didsprite.getName());
	}
	
	private static void write(Sprite didsprite, ByteBuffer buf) {
		ByteBuffer data = null;
		File f = null;
		if (didsprite.tuile){
			//TODO vérifier un jour à qui sert cet offset sur le premier de la zone de tuiles
			didsprite.offsetX = 0;
			didsprite.offsetY = 0;
			File dir = new File(Params.SPRITES+"tuiles"+File.separator+didsprite.chemin);
			dir.mkdirs();
			f = new File(Params.SPRITES+"tuiles"+File.separator+didsprite.chemin+File.separator+didsprite.getName()+".png");
		}else{
			File dir = new File(Params.SPRITES+"sprites"+File.separator+didsprite.chemin);
			dir.mkdirs();
			f = new File(Params.SPRITES+"sprites"+File.separator+didsprite.chemin+File.separator+didsprite.getName()+".png");
		}
		if (f.exists())return;
		data = ByteBuffer.allocate(didsprite.largeur*didsprite.hauteur);
		try{
			buf.position(didsprite.bufPos);
			buf.get(data.array());
		}catch(BufferUnderflowException e){
			e.printStackTrace();
			logger.info("sprite : "+didsprite.getName());
			logger.info("chemin : "+didsprite.chemin);
			logger.info("type : "+didsprite.type);
			logger.info("ombre : "+didsprite.ombre);
			logger.info("hauteur : "+didsprite.hauteur);
			logger.info("largeur : "+didsprite.largeur);
			logger.info("offsetY : "+didsprite.offsetY);
			logger.info("offsetX : "+didsprite.offsetX);
			logger.info("offsetY2 : "+didsprite.offsetY2);
			logger.info("offsetX2 : "+didsprite.offsetX2);
			logger.info("Inconnu : "+didsprite.inconnu9);
			logger.info("Couleur trans : "+didsprite.couleurTrans);
			logger.info("taille_zip : "+didsprite.taille_zip);
			logger.info("taille_unzip : "+didsprite.taille_unzip);
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
		//logger.info(Sprite.maxX/*+(2*Sprite.maxOffsetX)*/+" "+ Sprite.maxY/*+(2*Sprite.maxOffsetY)*/+" "+ Transparency.BITMASK);
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
		//logger.info(++Params.nb_sprite+"/"+Params.total_sprites +" TYPE : SIMPLE "+Params.t4cOUT+"SPRITES/"+didsprite.chemin+File.separator+f.getName()+" | Palette : "+didsprite.palette.nom);	
	}
	
	private static void rleUncompress(Sprite didsprite, ByteBuffer buf) {
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
		drawImage(img,didsprite);
	}
	
	private static void unzip(Sprite didsprite, ByteBuffer buf) {
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
		drawImage(img,didsprite);
	}
	
	private static void unzipTwice(Sprite didsprite, ByteBuffer buf) {
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
		drawImage(img,didsprite);
	}
	
	private static void drawImage(BufferedImage img, Sprite didsprite){
		File f = null;
		if (didsprite.tuile){
			File dir = new File(Params.SPRITES+"tuiles"+File.separator+didsprite.chemin);
			dir.mkdirs();
			f = new File(Params.SPRITES+"tuiles"+File.separator+didsprite.chemin+File.separator+didsprite.getName()+".png");
		}else{
			File dir = new File(Params.SPRITES+"sprites"+File.separator+didsprite.chemin);
			dir.mkdirs();
			f = new File(Params.SPRITES+"sprites"+File.separator+didsprite.chemin+File.separator+didsprite.getName()+".png");
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
		//logger.info(++Params.nb_sprite+"/"+Params.total_sprites +" TYPE : "+didsprite.type+" "+Params.t4cOUT+"SPRITES/"+didsprite.chemin+File.separator+f.getName()+" | Palette : "+didsprite.palette.getNom());
	}

	/**
	 * On empacte les sprites dans des atlas.
	 * Pour retrouver plus tard les ressources graphiques,
	 * il faut chercher un atlas correspondant au nom de dossier,
	 * puis une région d'atlas correspondant au nom du sprite.
	 */
	private static void pack_sprites(){
		Settings settings = new Settings();
		settings.pot = false;
		settings.maxWidth = 1284;
		settings.maxHeight = 772;
		settings.rotation = false;
		settings.ignoreBlankImages = false;
		settings.edgePadding = false;
		settings.flattenPaths = true;
		settings.grid = true;
		settings.limitMemory = true;

		FileLister explorer = new FileLister();
		List<File> sprites = new ArrayList<File>();
		sprites.addAll(explorer.listerDir(new File(FilesPath.getSpritePath())));
		Iterator<File> iter_sprites = sprites.iterator();
		String last ="";
		while (iter_sprites.hasNext()){
			final File f = iter_sprites.next();
			final File at = new File(FilesPath.getAtlasSpritesFilePath(f.getName()));
			logger.info("Pack Sprites : "+at.getName());
			if (!f.getName().equals(last) & f.isDirectory() & !at.exists()){
				loadingStatus.addSpritesAtlasToPackage(f.getName());
				executeSpritePacking(f, settings);
				last = f.getName();
			}
		}
	}
	
	private static void executeSpritePacking(File f, Settings s) {
		Runnable r = RunnableCreatorUtil.getSpritePackerRunnable(f, s);
		ThreadsUtil.executeInThread(r);
	}
	
	private static void pack_tuiles(){
		Settings settings = new Settings();
		settings.pot = false;
		settings.maxWidth = 1152;
		settings.maxHeight = 640;
		settings.rotation = false;
		settings.ignoreBlankImages = false;
		settings.edgePadding = false;
		settings.flattenPaths = true;
		settings.grid = true;
		settings.limitMemory = true;

		FileLister explorer = new FileLister();
		List<File> tuiles = new ArrayList<File>();
		tuiles.addAll(explorer.listerDir(new File(FilesPath.getTuilePath())));
		Iterator<File> iter_tuiles = tuiles.iterator();
		String last ="";
		while (iter_tuiles.hasNext()){
			File f = iter_tuiles.next();
			final File at = new File(FilesPath.getAtlasTilesFilePath(f.getName()));
			logger.info("Pack Tuiles : "+at.getName());
			Params.STATUS = "Pack Tuiles : "+at.getName();
			if (!f.getName().equals(last) & f.isDirectory() & !at.exists()){
				loadingStatus.addTilesAtlasToPackage(f.getName());
				executeTuilesPacking(f, settings);
				}
			last = f.getName();
		}
	}
	
	private static void executeTuilesPacking(File f, Settings settings) {
		Runnable r = RunnableCreatorUtil.getTuilePackerRunnable(f, settings);
		ThreadsUtil.executeInThread(r);
	}
	
	public static Map<Integer, SpriteName> getIds() {
		return ids;
	}
		public static Map<Integer, Sprite> getSprites_with_ids() {
		return sprites_with_ids;
	}
		public static Map<Integer, Sprite> getSprites_without_ids() {
		return sprites_without_ids;
	}
		public static void setBlack(Sprite black) {
		AtlasFactory.black = black;
	}
		
		/**
		 * Dans libGDX on doit se débarasser manuellement d'un certain nombre d'objets.
		 * une liste se trouve sur le wiki de libGDX.
		 */
		public static void dispose(){
			//TODO ensure all elements are loaded and no thread will update a map during the iteration
			Iterator<TextureAtlas> iter_tuiles = loadingStatus.getTexturesAtlasTiles().iterator();
			while(iter_tuiles.hasNext()){
				iter_tuiles.next().dispose();
			}
			Iterator<TextureAtlas> iter_sprites = loadingStatus.getTexturesAtlasSprites().iterator();
			while(iter_sprites.hasNext()){
				iter_sprites.next().dispose();
			}
		}
}
