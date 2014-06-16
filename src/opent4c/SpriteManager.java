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
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import opent4c.PalettePixel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.SpriteName;
import t4cPlugin.utils.FilesPath;
import tools.DataInputManager;

public class SpriteManager {
	
	private static Logger logger = LogManager.getLogger(SpriteManager.class.getSimpleName());
	//TODO initialiser la liste avec le nombre de sprites
	private static Map<SpriteName,Sprite> sprites = new HashMap<SpriteName,Sprite>();
	private static Map<Integer,SpriteName> ids = null;
	private static Map<String,Palette> palettes = null;
	private static int nb_palettes = -1;
	private static int nb_sprites = -1;


	/**
	 * Gets IDs from id.txt file
	 * puts info into a HashMap<Integer,SpriteName>
	 */
	public static void loadIdsFromFile(){
		ids = new HashMap<Integer,SpriteName>();
		File id_file = new File(FilesPath.getIdFilePath());
		logger.info("Lecture du fichier "+id_file.getName());
		try{
			BufferedReader buff = new BufferedReader(new FileReader(id_file.getPath()));			 
			try {
				String line;
				while ((line = buff.readLine()) != null) {
					readIdLine(line);
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
	private static void readIdLine(String line){
		int key = 0;
		String value = "";
		key = Integer.parseInt(line.substring(0, line.indexOf(' ')));
		value = line.substring(line.indexOf("Name: ")+6);
		SpriteName name = new SpriteName(value);
		ids.put(key,name);
	}
	
	/**
	 * Adds a new sprite
	 * @param buf
	 */
	public static void addSprite(ByteBuffer buf) {
		Sprite newSprite = new Sprite();
		newSprite.setSpriteName(extractName(buf));
		newSprite.setChemin(extractChemin(buf));
		newSprite.setIndexation(extractIndexation(buf));
		newSprite.setNumDda(extractNumDDA(buf));
		newSprite.setId(getIds(newSprite));
		getSprites().put(newSprite.getSpriteName(), newSprite);
	}

	/**
	 * Decrypts dpd file
	 */
	public static void decryptDPD(){
		ByteBuffer buf = null;
		ByteBuffer header;
		ByteBuffer bufUnZip;
		
		byte[] header_hashMd5 = new byte[16];
		byte[] header_hashMd52 = new byte[17];
		byte[] header_hash = new byte[32];
		
		final byte clef = (byte) 0x66;
		
		int header_taille_unZip = 0;
		int header_taille_zip;
		
		/*byte checksum;
		byte azt;*/
		
		File f = SourceDataManager.getDPD();
		logger.info("Lecture du fichier "+f.getName());
		
		header = ByteBuffer.allocate(41);
		
		try {
			DataInputManager in = new DataInputManager (f);
			while (header.position()<header.capacity()){
				header.put(in.readByte());
			}
			header.rewind();
			
			header_hashMd5 = getHeaderHashMD5(header);
			header_taille_unZip = getHeaderTailleUnzip(header);
			header_taille_zip = getHeaderTailleZip(header);
			header_hashMd52 = getHeaderHashMd52(header);

			ByteBuffer buf_hash = ByteBuffer.allocate(33);
			buf_hash.put(header_hashMd5);
			buf_hash.put(header_hashMd52);
			buf_hash.rewind();
			buf_hash.get(header_hash);
			//azt = header_hashMd52[16];			
			
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
		
	    bufUnZip = ByteBuffer.allocate(header_taille_unZip);
		bufUnZip = unzip(buf,header_taille_unZip);
		
		for (int i=0; i<bufUnZip.capacity(); i++){
			bufUnZip.array()[i] ^= clef;
		}
		
		nb_palettes = header_taille_unZip/(64 + 768);
		palettes = extractPalettes(bufUnZip);
		//TODO Checksum ou tout du moins moyen de contrôle
		logger.info("Fichier "+f.getName()+" lu : "+palettes.size()+" palettes.");
	}		
	
	/**
	 * Extracts Palettes from dpd file
	 * @param bufUnZip
	 * @return
	 */
	private static Map<String, Palette> extractPalettes(ByteBuffer bufUnZip) {
		Map<String,Palette> result = new HashMap<String,Palette>(nb_palettes);
		for(int i=0 ; i<nb_palettes ; i++){
			Palette p = new Palette(bufUnZip);
			UpdateScreenManagerStatus.setSubStatus("Palette extraite : "+(i+1)+" => "+p.nom);
			result.put(p.nom,p);
		}
		return result;
	}

	/**
	 * Unzips a dpd file
	 * @param buf
	 * @param size
	 * @return a ByteBuffer with unzipped data
	 */
	private static ByteBuffer unzip(ByteBuffer buf, int size) {
		
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
	private static byte[] getHeaderHashMd52(ByteBuffer header) {
		byte[] result = new byte[17];
		for (int i=0 ; i<17 ; i++){
			result[i] = header.get();
		}
		return result;
	}

	/**
	 * @param header
	 * @return zipped data size
	 */
	private static int getHeaderTailleZip(ByteBuffer header) {
		byte b1,b2,b3,b4;
		b1 = header.get();
		b2 = header.get();
		b3 = header.get();
		b4 = header.get();
		return tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
	}

	/**
	 * 
	 * @param buffer
	 * @return unzipped data size
	 */
	private static int getHeaderTailleUnzip(ByteBuffer buffer) {
		byte b1,b2,b3,b4;
		b1 = buffer.get();
		b2 = buffer.get();
		b3 = buffer.get();
		b4 = buffer.get();
		return tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
	}

	/**
	 * 
	 * @param buffer
	 * @return a byte array with the second part of the md5 checksum
	 */
	private static byte[] getHeaderHashMD5(ByteBuffer buffer) {
		byte[] result = new byte[16];
		for (int i=0 ; i<16 ; i++){
			result[i] = buffer.get();
		}
		return result;
	}

	/**
	 * 
	 * @param sprite
	 * @return The id list of a given sprite
	 */
	private static ArrayList<Integer> getIds(Sprite sprite) {
		List<Integer> result = new ArrayList<Integer>();
		Iterator<Integer> iter = ids.keySet().iterator();
		while (iter.hasNext()){
			int val = iter.next();
			SpriteName sn = ids.get(val);
			if (sprite.getName().startsWith(sn.getName())){
				result.add(val);
			}
		}
		return (ArrayList<Integer>) result;
	}

	/**
	 * extract the .dda number associated to a sprite
	 * @param buf
	 * @return
	 */
	private static long extractNumDDA(ByteBuffer buf) {
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
	}

	/**
	 * extract the position in the buffer to decode a sprite
	 * @param buf
	 * @return
	 */
	private static int extractIndexation(ByteBuffer buf) {
		byte b1,b2,b3,b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		return tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
	}

	/**
	 * Extracts a Sprite's path
	 * @param buf
	 * @return
	 */
	private static String extractChemin(ByteBuffer buf) {
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
	 * Extracts a Sprite's name
	 * @param buf
	 * @return
	 */
	private static SpriteName extractName(ByteBuffer buf) {
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
	 * Extracts sprite infos from .did file
	 */
	public static void decryptDID(){
		
		logger.info("Décryptage du fichier "+SourceDataManager.getDID());
		ByteBuffer buf = null;
		ByteBuffer header;
		ByteBuffer bufUnZip;
		
		byte clef = (byte) 0x99;
		
		byte[] header_hashMd5 = new byte[16];
		byte[] header_hashMd52 = new byte[17];
		
		int header_taille_unZip = 0;
		int header_taille_zip;
				
		header = ByteBuffer.allocate(41);
		try {
			DataInputManager in = new DataInputManager(SourceDataManager.getDID());
			while (header.position()<header.capacity()){
				header.put(in.readByte());
			}
			header.rewind();
			
			header_hashMd5 = getHeaderHashMD5(header);
			header_taille_unZip = getHeaderTailleUnzip(header);
			header_taille_zip = getHeaderTailleZip(header);
			header_hashMd52 = getHeaderHashMd52(header);

			buf = ByteBuffer.allocate(header_taille_zip);
			while(buf.position() < buf.capacity()){
				buf.put(in.readByte());
			}
			in.close();
		}catch(IOException exc){
			logger.fatal("Erreur d'ouverture");
			exc.printStackTrace();
			System.exit(1);
		}
		
		buf.rewind();
		bufUnZip = unzip(buf, header_taille_unZip);
		for (int i=0; i<bufUnZip.capacity(); i++){
			bufUnZip.array()[i] ^= clef;
		}
		nb_sprites = header_taille_unZip/(64 + 256 + 4 + 8);

		for(int i=0 ; i<nb_sprites ; i++){
			addSprite(bufUnZip);
			UpdateScreenManagerStatus.setSubStatus("Sprite décrypté : "+(i+1)+"/"+nb_sprites);
		}
	}
	
	/**
	 * Extracts sprites from .dda files
	 */
	public static void decryptDDA(boolean doWrite){
		File f = null;
		List<File> ddas = SourceDataManager.getDDA();
		Iterator<File>iter_dda = ddas.iterator();
		while (iter_dda.hasNext()){
			f = iter_dda.next();
			logger.info("Décryptage du fichier : "+f.getName());
			decrypt_dda_file(f,doWrite);
		}
		logger.info(getSprites().size()+" sprites décryptés");
	}
	
	/**
	 * Decrypts a dda file
	 * @param f
	 * @param doWrite
	 */
	private static void decrypt_dda_file(File f, boolean doWrite) {
		ByteBuffer buf;
		byte[] signature = new byte[4];
		int numDDA;
		//logger.info("Lecture des entêtes dans le fichier "+f.getName());
		numDDA = Integer.parseInt(f.getName().substring(f.getName().length()-6, f.getName().length()-4),10);
		
		Map<SpriteName,Sprite> sprites_in_dda = new HashMap<SpriteName,Sprite>();
		Iterator<SpriteName> iteri = getSprites().keySet().iterator();
		while(iteri.hasNext()){
			SpriteName key = iteri.next();
			Sprite sp = getSprites().get(key);
			if (sp.getNumDda() == numDDA){
				sprites_in_dda.put(key, sp);
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
		
		signature = getDDASignature(buf);
		int index = 1;
		Iterator<SpriteName> iter = sprites_in_dda.keySet().iterator();
		while(iter.hasNext()){
			SpriteName key = iter.next();
			Sprite sprite = sprites_in_dda.get(key);
			int indexation = sprite.getIndexation()+4;
			try{
				buf.position(indexation);
			}catch(IllegalArgumentException e){
				e.printStackTrace();
				System.exit(1);
			}
			extractDDASprite(buf, sprite);//lit l'entête du sprite et ajoute  les infos de l'entête dans le Sprite
			if(doWrite) doTheWriting(sprite, buf, index, sprites_in_dda.size());
			index++;

		}
	}
	
	/**
	 * Extract a sprite from a .dda file
	 * @param buf
	 * @param sprite
	 */
	private static void extractDDASprite(ByteBuffer buf, Sprite sprite) {
		final int[] clefDda = new int[]{0x1458AAAA, 0x62421234, 0xF6C32355, 0xAAAAAAF3, 0x12344321, 0xDDCCBBAA, 0xAABBCCDD};
		int[] header = new int[7];
		long taille_zip;//int
		long taille_unzip;//int
		Palette palette = null;
		
		//essai de correspondance des palettes
		Iterator<String> iter_pal = palettes.keySet().iterator();
		Palette bright = null;
		while (iter_pal.hasNext()){
			String pal = iter_pal.next();
			if (pal.equals("Bright1")) bright = palettes.get(pal);
			String chemin = sprite.getChemin();
			String nom = sprite.getName();
			if (nom.length() >= pal.length()-1){
				//Si le nom du sprite et le nom de la palette commencent pareil, on attribue la palette au sprite
				if(nom.substring(0, pal.length()-1).toUpperCase().contains(pal.substring(0, pal.length()-1).toUpperCase()) && palette == null){
					palette = palettes.get(pal);
					break;
				}
			}
			if(pal.toLowerCase().contains(chemin.toLowerCase()) || chemin.toLowerCase().contains(pal.toLowerCase())){
				palette = palettes.get(pal);
				break;
			}
		}
		//Si on a pas trouvé, on attribue la palette Bright1
		if (palette == null) palette = bright;
		sprite.setPalette(palette);
		ByteBuffer header_buf = ByteBuffer.allocate(7*Integer.SIZE);
		for(int i=0 ; i<7 ; i++){
			byte b1,b2,b3,b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			header[i] = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1}) ^ clefDda[i];
			header_buf.put((byte)((header[i]>>24)& 0xFF));
			//logger.info(""+(byte)((header[i]>>24)& 0xFF));

			header_buf.put((byte)((header[i]>>16)& 0xFF));
			//logger.info(""+(byte)((header[i]>>16)& 0xFF));

			header_buf.put((byte)((header[i]>>8)& 0xFF));
			//logger.info(""+(byte)((header[i]>>8)& 0xFF));

			header_buf.put((byte)((header[i]>>0)& 0xFF));
			//logger.info(""+(byte)((header[i]>>0)& 0xFF));

		}
		header_buf.rewind();
		//new Fast_Forward(header_buf,header_buf.capacity(),false,"Header : ");
		byte b1,b2,b3,b4;

		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.setOmbre(tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2}));
		//logger.info("ombre : "+ombre);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.setType(tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2}));
		//logger.info("type : "+type);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.setHauteur(tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2}));
		//logger.info("hauteur : "+didsprite.hauteur);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.setLargeur(tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2}));
		//logger.info("largeur : "+didsprite.largeur);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.setOffsetY(tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2}));
		//logger.info("offsetY : "+didsprite.offsetY);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.setOffsetX(tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2}));
		//logger.info("offsetY : "+didsprite.offsetX);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.setOffsetY2(tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2}));
		//logger.info("offsetY2 : "+offsetY2+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));

		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.setOffsetX2(tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2}));
		//logger.info("offsetX2 : "+offsetX2+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));

		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.setCouleurTrans(b2);//tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
		//logger.info("Couleur trans : "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.setInconnu9(tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2}));
		//logger.info("Inconnu : "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));

		b1 = header_buf.get();
		b2 = header_buf.get();
		b3 = header_buf.get();
		b4 = header_buf.get();			
		sprite.setTaille_unzip(tools.ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b1,b2,b3,b4}));
		//logger.info("taille_unzip : "+taille_unzip);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		b3 = header_buf.get();
		b4 = header_buf.get();			
		sprite.setTaille_zip(tools.ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b1,b2,b3,b4}));
		sprite.setBufPos(buf.position());

		String nom = sprite.getName();
		if ((sprite.getLargeur() == 32) & (sprite.getHauteur() == 16) & (nom.contains("(")) & nom.contains(")")){
			sprite.setTuile(true);
		} else {
			sprite.setTuile(false);
		}
	}

	private static byte[] getDDASignature(ByteBuffer buf) {
		byte[] result = new byte[4];
		result[0] = buf.get();
		result[1] = buf.get();
		result[2] = buf.get();
		result[3] = buf.get();
		return result;
	}

	private static void doTheWriting(Sprite sprite, ByteBuffer buf, int index, int nb_in_dda){
		UpdateScreenManagerStatus.setSubStatus("Sprite écrit : "+index+"/"+nb_in_dda);
		switch (sprite.getType()){
		case 1 : write(sprite, buf);
		break;
		case 2 : rleUncompress(sprite, buf);
		break;
		case 3 : voidSprite(sprite);
		break;
		case 9 : if (sprite.getTaille_zip() == sprite.getTaille_unzip()){
			unzipSprite(sprite, buf);
		}else{
			unzipSpriteTwice(sprite, buf);
		}
		break;
		}
	}
	
	private static void voidSprite(Sprite didsprite) {
		//logger.info(++Params.nb_sprite+"/"+Params.total_sprites +" TYPE : VIDE "+didsprite.getName());
	}
	
	private static void write(Sprite didsprite, ByteBuffer buf) {
		ByteBuffer data = null;
		File f = null;
		if (didsprite.isTuile()){
			//TODO vérifier un jour à qoui sert cet offset sur le premier de la zone de tuiles
			didsprite.setOffsetX(0);
			didsprite.setOffsetY(0);
			File dir = new File(FilesPath.getTuileDirectoryPath()+didsprite.getChemin());
			dir.mkdirs();
			f = new File(FilesPath.getTuileDirectoryPath()+didsprite.getChemin()+File.separator+didsprite.getName()+".png");
		}else{
			File dir = new File(FilesPath.getSpriteDirectoryPath()+didsprite.getChemin());
			dir.mkdirs();
			f = new File(FilesPath.getSpriteDirectoryPath()+didsprite.getChemin()+File.separator+didsprite.getName()+".png");
		}
		if (f.exists())return;
		data = ByteBuffer.allocate(didsprite.getLargeur()*didsprite.getHauteur());
		try{
			buf.position(didsprite.getBufPos());
			buf.get(data.array());
		}catch(BufferUnderflowException e){
			e.printStackTrace();
			logger.info("sprite : "+didsprite.getName());
			logger.info("chemin : "+didsprite.getChemin());
			logger.info("type : "+didsprite.getType());
			logger.info("ombre : "+didsprite.getOmbre());
			logger.info("hauteur : "+didsprite.getHauteur());
			logger.info("largeur : "+didsprite.getLargeur());
			logger.info("offsetY : "+didsprite.getOffsetY());
			logger.info("offsetX : "+didsprite.getOffsetX());
			logger.info("offsetY2 : "+didsprite.getOffsetY2());
			logger.info("offsetX2 : "+didsprite.getOffsetX2());
			logger.info("Inconnu : "+didsprite.getInconnu9());
			logger.info("Couleur trans : "+didsprite.getCouleurTrans());
			logger.info("taille_zip : "+didsprite.getTaille_zip());
			logger.info("taille_unzip : "+didsprite.getTaille_unzip());
			System.exit(1);
		}
		data.rewind();
		ArrayList<PalettePixel> pal = didsprite.getPalette().pixels;
		BufferedImage img = null;
		img = new BufferedImage(didsprite.getLargeur(), didsprite.getHauteur(), BufferedImage.TYPE_INT_ARGB);
		int y = 0, x = 0;
		while (y<didsprite.getHauteur()){
			while (x<didsprite.getLargeur()){
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
				if (b == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,didsprite.getCouleurTrans()})){
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
		drawImage(img, didsprite);
	}
	
	private static void rleUncompress(Sprite didsprite, ByteBuffer buf) {
		byte b1,b2;
		int X,Y,nbpix;
		byte b = 0;
		ByteBuffer data = null;
		buf.position(didsprite.getBufPos());
		Y=0;
		//pour ceux qui ont la compression Zlib en plus
		if (didsprite.getLargeur() > 180 | didsprite.getHauteur() > 180) {
			Inflater unzip = new Inflater();
			byte[] bytes = new byte[(int) didsprite.getTaille_zip()];
			buf.get(bytes);
			unzip.setInput(bytes, 0, (int) didsprite.getTaille_zip());
			data = ByteBuffer.allocate((int) didsprite.getTaille_unzip());
			try {
				unzip.inflate(data.array());
			} catch (DataFormatException e) {
				e.printStackTrace();
				logger.fatal(e);
				System.exit(1);
			}
			unzip.end();
		}else{
			data = ByteBuffer.allocate((int) didsprite.getTaille_zip());
			buf.get(data.array());
		}
		data.rewind();
		//on a les données du sprite dans data. on opère la décompression RLE
		ByteBuffer spriteTmp =  ByteBuffer.allocate(didsprite.getHauteur()*didsprite.getLargeur());
		spriteTmp.rewind();
		//on remplit de transparence
		for (int i=0 ; i<spriteTmp.capacity() ; i++){
			spriteTmp.put(didsprite.getCouleurTrans());
		}
		spriteTmp.rewind();
		if (data != null){
			while(Y != didsprite.getHauteur()){
				b1 = data.get();
				b2 = data.get();
				X = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
				b1 = data.get();
				b2 = data.get();
				nbpix = ((tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b1})*4) + tools.ByteArrayToNumber.bytesToShort(new byte[]{0,b2}));
				b = data.get();
				if (b != 1){
					for (int i=0 ; i<nbpix ; i++){
						spriteTmp.put((spriteTmp.position()+i+X+(Y*didsprite.getLargeur())), data.get());
						if ((i+X) == (didsprite.getLargeur()-1)) break;//sécu pour être sur de pas dépasser;
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
		
		ArrayList<PalettePixel> pal = didsprite.getPalette().pixels;
		BufferedImage img = null;
		if (didsprite.isTuile()){
			img = new BufferedImage(didsprite.getLargeur(), didsprite.getHauteur(), BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage(didsprite.getLargeur(), didsprite.getHauteur(), BufferedImage.TYPE_INT_ARGB);
		}			int y = 0, x = 0;
		while (y<didsprite.getHauteur()){
			while (x<didsprite.getLargeur()){
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
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,didsprite.getCouleurTrans()})){
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
	
	private static void unzipSprite(Sprite didsprite, ByteBuffer buf) {
		byte b1,b2,b3,b4;
		buf.position(didsprite.getBufPos());
		b1=buf.get();
		b2=buf.get();
		b3=buf.get();
		b4=buf.get();
		int taille_unzip2 = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		Inflater unzip = new Inflater();
		ByteBuffer unzip_data1 = null;
		byte[] data = new byte[(int) didsprite.getTaille_zip()];
		buf.get(data);
		unzip.setInput(data, 0, (int) didsprite.getTaille_zip());
		unzip_data1 = ByteBuffer.allocate((int)taille_unzip2);
		try {
			unzip.inflate(unzip_data1.array());
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		unzip.end();
		unzip_data1.rewind();
	
		ArrayList<PalettePixel> pal = didsprite.getPalette().pixels;
		BufferedImage img = null;
		if (didsprite.isTuile()){
			img = new BufferedImage(didsprite.getLargeur(), didsprite.getHauteur(), BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage(didsprite.getLargeur(), didsprite.getHauteur(), BufferedImage.TYPE_INT_ARGB);
		}			int y = 0, x = 0;
		while (y<didsprite.getHauteur()){
			while (x<didsprite.getLargeur()){
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
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,didsprite.getCouleurTrans()})){
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
	
	private static void unzipSpriteTwice(Sprite didsprite, ByteBuffer buf) {
		byte b1,b2,b3,b4;
		Inflater unzip = new Inflater();
		ByteBuffer unzip_data1 = null;
		byte[] data = null;
		try{
			data = new byte[(int) didsprite.getTaille_zip()];
		}catch(NegativeArraySizeException e){
			e.printStackTrace();
			System.exit(1);
		}
		buf.position(didsprite.getBufPos());
		buf.get(data);
		unzip.setInput(data, 0, (int) didsprite.getTaille_zip());
		unzip_data1 = ByteBuffer.allocate((int) didsprite.getTaille_unzip());
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
		
		ArrayList<PalettePixel> pal = didsprite.getPalette().pixels;
		BufferedImage img = null;
		if (didsprite.isTuile()){
			img = new BufferedImage(didsprite.getLargeur(), didsprite.getHauteur(), BufferedImage.TYPE_INT_RGB);
		}else{
			img = new BufferedImage(didsprite.getLargeur(), didsprite.getHauteur(), BufferedImage.TYPE_INT_ARGB);
		}
		int y = 0, x = 0;
		while (y<didsprite.getHauteur()){
			while (x<didsprite.getLargeur()){
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
				if (c == tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,didsprite.getCouleurTrans()})){
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
		if (didsprite.isTuile()){
			File dir = new File(FilesPath.getTuileDirectoryPath()+didsprite.getChemin());
			dir.mkdirs();
			f = new File(FilesPath.getTuileDirectoryPath()+didsprite.getChemin()+File.separator+didsprite.getName()+".png");
		}else{
			File dir = new File(FilesPath.getSpriteDirectoryPath()+didsprite.getChemin());
			dir.mkdirs();
			f = new File(FilesPath.getSpriteDirectoryPath()+didsprite.getChemin()+File.separator+didsprite.getName()+".png");
		}
		if (f.exists())return;
		GraphicsConfiguration gc = img.createGraphics().getDeviceConfiguration();
		BufferedImage out =	gc.createCompatibleImage(didsprite.getLargeur(), didsprite.getHauteur(), Transparency.BITMASK);
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



	public static Map<SpriteName,Sprite> getSprites() {
		return sprites;
	}

	public static void setSprites(Map<SpriteName,Sprite> sprites) {
		SpriteManager.sprites = sprites;
	}
}

