/**
 * 
 */
package opent4c;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import opent4c.utils.FilesPath;
import opent4c.utils.SpriteName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tools.ByteArrayToNumber;
import tools.UnsignedInt;
import tools.DataInputManager;
import tools.UnsignedShort;

/**
 * @author synoga
 *
 */
public class SpriteUtils {

	static Logger logger = LogManager.getLogger(SpriteUtils.class.getSimpleName());
	private static int nb_extracted_from_dda = 0;
	private static int nb_writen = 0;


	
	static boolean doTheWriting(MapPixel pixel, ByteBuffer buf){
		boolean writen = false;
		switch(pixel.getType()){
			case 1 : SpriteWriter.writeType1SpriteToDisk(pixel, buf); break;
			case 2 : SpriteWriter.writeType2SpriteToDisk(pixel, buf); break;
			case 3 : SpriteWriter.writeType3SpriteToDisk(pixel); writen = true; break;
			case 9 : SpriteWriter.writeType9SpriteToDisk(pixel, buf); break;
		}
		if (pixel.isTuile()){
			File f = new File(FilesPath.getTuileDirectoryPath()+pixel.getAtlas()+File.separator+pixel.getTex()+".png");		
			if(f.exists())writen = true;			
		}else{
			File f = new File(FilesPath.getSpriteDirectoryPath()+pixel.getAtlas()+File.separator+pixel.getTex()+".png");
			if(f.exists())writen = true;			
		}
		return writen;
	}

	static void extractDDASprite(ByteBuffer buf, MapPixel pixel) {
		Palette p = extractPalette(pixel);
		if(p == null){
			logger.fatal("On n'a pas trouvé cette palette : "+pixel.getTex());
			System.exit(1);
		}
		pixel.setPalette(p);
		//logger.info("Palette : "+nom+"<=>"+sprite.getPaletteName());
		ByteBuffer header_buf = extractHeaderBuffer(buf);
		pixel.setOmbre(new UnsignedShort(extractShort(header_buf,true)));
		pixel.setType(new UnsignedShort(extractShort(header_buf,true)));
		pixel.setHauteur(new UnsignedShort(extractShort(header_buf,true)));
		pixel.setLargeur(new UnsignedShort(extractShort(header_buf,true)));
		pixel.setOffsetY(ByteArrayToNumber.bytesToShort(extractShort(header_buf,true)));
		pixel.setOffsetX(ByteArrayToNumber.bytesToShort(extractShort(header_buf,true)));
		pixel.setOffsetY2(ByteArrayToNumber.bytesToShort(extractShort(header_buf,true)));
		pixel.setOffsetX2(ByteArrayToNumber.bytesToShort(extractShort(header_buf,true)));
		pixel.setCouleurTrans(new UnsignedShort(extractShort(header_buf,true)));
		pixel.setInconnu9(new UnsignedShort(extractShort(header_buf,true)));
		pixel.setTaille_unzip(new UnsignedInt(extractInt(header_buf,true)));
		pixel.setTaille_zip(new UnsignedInt(extractInt(header_buf,true)));		
		pixel.setBufPos(buf.position());
		extractTuileInfo(pixel);
		//logger.info("Type : "+sprite.getType().getValue());
	}

	/**
	 * 
	 */
	private static void addOneExtractedFromDDA() {
		nb_extracted_from_dda++;
	}

	/**
	 * Ici on sépare les tuiles des sprites. la base c'est que les tuiles font 32 x 16 et les sprites non. Cependant ce ne serait pas drôle s'il n'y avait pas quelques exceptions
	 * donc on les gère au cas par cas (il n'y en a pas 12 000 non plus).
	 * @param sprite
	 * @return
	 */
	private static void extractTuileInfo(MapPixel pixel) {
		boolean result = false;
		if ((pixel.getLargeur() == 32) && (pixel.getHauteur() == 16)) result = true;
		if (pixel.getAtlas().equals("All"))result = false;
		if (pixel.getAtlas().equals("BlackLeatherBoots"))result = false;
		if (pixel.getAtlas().equals("CemeteryGates"))result = false;
		if (pixel.getAtlas().equals("DarkSword"))result = false;
		if (pixel.getAtlas().equals("GoldenMorningStar"))result = false;
		if (pixel.getAtlas().equals("LeatherBoots"))result = false;
		if (pixel.getAtlas().equals("MorningStar"))result = false;
		if (pixel.getAtlas().equals("OgreClub"))result = false;
		if (pixel.getAtlas().equals("PlateBoots"))result = false;
		if (pixel.getAtlas().equals("SkavenClub"))result = false;
		if (pixel.getAtlas().equals("StoneShard"))result = false;
		if (pixel.getAtlas().equals("PlateLeftArm"))result = false;
		if (pixel.getAtlas().equals("Bow07"))result = false;
		if (pixel.getAtlas().equals("Dague05"))result = false;
		if (pixel.getAtlas().equals("Hache06"))result = false;
		if (pixel.getAtlas().equals("Hammer01"))result = false;
		if (pixel.getAtlas().equals("LevelUp"))result = false;
		if (pixel.getAtlas().equals("NMFire"))result = false;
		if (pixel.getAtlas().equals("NMLightning"))result = false;
		if (pixel.getAtlas().equals("NMPoison"))result = false;
		if (pixel.getAtlas().equals("NMSSupraHeal"))result = false;
		if (pixel.getAtlas().equals("OnGround"))result = false;
		if (pixel.getAtlas().equals("SP03"))result = false;
		if (pixel.getAtlas().equals("SP04"))result = false;
		if (pixel.getAtlas().equals("SP05"))result = false;
		if (pixel.getAtlas().equals("Sword01"))result = false;
		if (pixel.getAtlas().equals("V2Effect"))result = false;
		if (pixel.getAtlas().equals("Weather"))result = false;
		//if (pixel.getAtlas().equals("GenericMerge3"))result = false;
		if (pixel.getAtlas().equals("Root"))result = false;
		if (pixel.getAtlas().equals("VSSmooth"))result = false;
		//if (pixel.getAtlas().equals("GenericMerge1"))result = false;
		//if (pixel.getAtlas().equals("GenericMerge2Wooden"))result = false;
		//if (pixel.getAtlas().equals("WoodenSmooth"))result = false;
		if (pixel.getAtlas().equals("Black"))result = false;
		pixel.setTuile(result);
	}

	/**
	 * 	essai de correspondance des palettes
	 *  dans l'idée, on cherche si le nom du sprite contient le nom d'une palette
	 *	sinon, on attribue la palette Bright1
	 * @return
	 */
	private static Palette extractPalette(MapPixel pixel) {
		Palette result = SpriteManager.palettes.get("Bright1");
		if (result == null){
			logger.fatal("On n'est pas parvenu à prendre une palette dans la liste : "+pixel.getTex()+" / "+SpriteManager.palettes.containsKey("Bright1"));
			System.exit(1);
		}
		Iterator<String> iter_pal = SpriteManager.palettes.keySet().iterator();
		while (iter_pal.hasNext()){
			String pal = iter_pal.next();
			String nom = pixel.getTex();
			if (nom.contains(pal)){
				result = SpriteManager.palettes.get(pal);
				if (result == null){
					logger.fatal("On n'est pas parvenu à prendre une palette dans la liste : "+nom+" / "+SpriteManager.palettes.containsKey(pal));
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
		List<MapPixel> sprites_in_dda = new ArrayList<MapPixel>();
		Iterator<Integer> iter_id = SpriteData.getPixelIndex().keySet().iterator();
		while(iter_id.hasNext()){
			int id = iter_id.next();
			Iterator<MapPixel> iter_px = SpriteData.getPixelIndex().get(id).iterator();
			while(iter_px.hasNext()){
				MapPixel px = iter_px.next();
				if(px.getNumDDA() == numDDA){
					sprites_in_dda.add(px);
				}
			}
		}
		ByteBuffer buf = readDDA(f);
		byte[] signature = new byte[4];
		signature = extractBytes(buf,signature.length);
		Iterator<MapPixel> iter = sprites_in_dda.iterator();
		while(iter.hasNext()){
			MapPixel pixel = iter.next();
			int indexation = (pixel.getIndexation()+4);
			try{
				buf.position(indexation);
			}catch(IllegalArgumentException e){
				e.printStackTrace();
				System.exit(1);
			}
			extractDDASprite(buf, pixel);//lit l'entête du sprite et ajoute les infos de l'entête dans le Sprite
			if(!doWrite)addOneExtractedFromDDA();
			if(doWrite){
				boolean writen = false;
				writen = doTheWriting(pixel, buf);
				if (!writen){
					logger.fatal("Sprite non écrit => "+pixel.getTex());
					System.exit(1);
				}
				addOneWriten();
			}
			if(!doWrite)UpdateDataCheckStatus.setStatus("Sprites extraits des fichiers DDA: "+nb_extracted_from_dda+"/"+DataChecker.nb_expected_sprites);
			if(doWrite)UpdateDataCheckStatus.setStatus("Sprites écrits: "+nb_writen+"/"+DataChecker.nb_expected_sprites);
		}
	}

	/**
	 * 
	 */
	private static void addOneWriten() {
		nb_writen++;
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
		nomExtrait = manageNameSpecialCases(nomExtrait);
		if (nomExtrait.equals("")){
			logger.fatal("nom extrait null : ");
			System.exit(1);
		};
		return new SpriteName(nomExtrait);
	}

	/**
	 * Modifies names to fit our scheme
	 * @param nom
	 * @return
	 */
	private static String manageNameSpecialCases(String nom) {
		nom = nom.substring(0, nom.indexOf(0x00));
		nom = nom.replace("_","");
		nom = nom.replace(":","");
		String result = nom;
		// Correction des noms pour les grilles
		if (nom.equals("Cemetery Gates /^"))result = "Cemetery Gates1";
		if (nom.equals("Cemetery Gates /"))result = "Cemetery Gates2";
		if (nom.equals("Cemetery Gates \\v"))result = "Cemetery Gates3";
		if (nom.equals("Cemetery Gates v"))result = "Cemetery Gates4";
		if (nom.equals("Cemetery Gates -"))result = "Cemetery Gates5";
		if (nom.equals("Cemetery Gates >"))result = "Cemetery Gates6";
		if (nom.equals("Cemetery Gates ^"))result = "Cemetery Gates7";
		if (nom.equals("Cemetery Gates X"))result = "Cemetery Gates8";
		if (nom.equals("Cemetery Gates .|"))result = "Cemetery Gates9";
		//les sprites Rockflor n'ont pas les coordonées dans leur nom, rendant impossible le calcul du modulo, je modifie les noms pour coller à mon standard de calcul de modulo.
		if (nom.equals("Rockflor 1")) result = "RockFlor (1, 1)";
		if (nom.equals("Rockflor 2")) result = "RockFlor (1, 2)";
		if (nom.equals("Rockflor 3")) result = "RockFlor (1, 3)";
		if (nom.equals("Rockflor 4")) result = "RockFlor (2, 1)";
		if (nom.equals("Rockflor 5")) result = "RockFlor (2, 2)";
		if (nom.equals("Rockflor 6")) result = "RockFlor (2, 3)";
		if (nom.equals("Rockflor 7")) result = "RockFlor (3, 1)";
		if (nom.equals("Rockflor 8")) result = "RockFlor (3, 2)";
		if (nom.equals("Rockflor 9")) result = "RockFlor (3, 3)";
		//idem
		if (nom.equals("Lava 1")) result = "Lava (1, 1)";
		if (nom.equals("Lava 2")) result = "Lava (1, 2)";
		if (nom.equals("Lava 3")) result = "Lava (1, 3)";
		if (nom.equals("Lava 4")) result = "Lava (1, 4)";
		if (nom.equals("Lava 5")) result = "Lava (1, 5)";
		if (nom.equals("Lava 6")) result = "Lava (2, 1)";
		if (nom.equals("Lava 7")) result = "Lava (2, 2)";
		if (nom.equals("Lava 8")) result = "Lava (2, 3)";
		if (nom.equals("Lava 9")) result = "Lava (2, 4)";
		if (nom.equals("Lava 10")) result = "Lava (2, 5)";
		//idem
		if (nom.equals("DungeonFloorTorch1 1"))result = "DungeonFloorTorch (1, 1)";
		if (nom.equals("DungeonFloorTorch1 2"))result = "DungeonFloorTorch (1, 2)";
		if (nom.equals("DungeonFloorTorch1 3"))result = "DungeonFloorTorch (1, 3)";
		if (nom.equals("DungeonFloorTorch1 4"))result = "DungeonFloorTorch (1, 4)";
		if (nom.equals("DungeonFloorTorch1 5"))result = "DungeonFloorTorch (1, 5)";
		if (nom.equals("DungeonFloorTorch2 1"))result = "DungeonFloorTorch (2, 1)";
		if (nom.equals("DungeonFloorTorch2 2"))result = "DungeonFloorTorch (2, 2)";
		if (nom.equals("DungeonFloorTorch2 3"))result = "DungeonFloorTorch (2, 3)";
		if (nom.equals("DungeonFloorTorch2 4"))result = "DungeonFloorTorch (2, 4)";
		if (nom.equals("DungeonFloorTorch2 5"))result = "DungeonFloorTorch (2, 5)";
		if (nom.equals("DungeonFloorTorch3 1"))result = "DungeonFloorTorch (3, 1)";
		if (nom.equals("DungeonFloorTorch3 2"))result = "DungeonFloorTorch (3, 2)";
		if (nom.equals("DungeonFloorTorch3 3"))result = "DungeonFloorTorch (3, 3)";
		if (nom.equals("DungeonFloorTorch3 4"))result = "DungeonFloorTorch (3, 4)";
		if (nom.equals("DungeonFloorTorch3 5"))result = "DungeonFloorTorch (3, 5)";
		if (nom.equals("DungeonFloorTorch4 1"))result = "DungeonFloorTorch (4, 1)";
		if (nom.equals("DungeonFloorTorch4 2"))result = "DungeonFloorTorch (4, 2)";
		if (nom.equals("DungeonFloorTorch4 3"))result = "DungeonFloorTorch (4, 3)";
		if (nom.equals("DungeonFloorTorch4 4"))result = "DungeonFloorTorch (4, 4)";
		if (nom.equals("DungeonFloorTorch4 5"))result = "DungeonFloorTorch (4, 5)";
		if (nom.equals("DungeonFloorTorch5 1"))result = "DungeonFloorTorch (5, 1)";
		if (nom.equals("DungeonFloorTorch5 2"))result = "DungeonFloorTorch (5, 2)";
		if (nom.equals("DungeonFloorTorch5 3"))result = "DungeonFloorTorch (5, 3)";
		if (nom.equals("DungeonFloorTorch5 4"))result = "DungeonFloorTorch (5, 4)";
		if (nom.equals("DungeonFloorTorch5 5"))result = "DungeonFloorTorch (5, 5)";
		if (nom.equals("DungeonFloorTorch6 1"))result = "DungeonFloorTorch (6, 1)";
		if (nom.equals("DungeonFloorTorch6 2"))result = "DungeonFloorTorch (6, 2)";
		if (nom.equals("DungeonFloorTorch6 3"))result = "DungeonFloorTorch (6, 3)";
		if (nom.equals("DungeonFloorTorch6 4"))result = "DungeonFloorTorch (6, 4)";
		if (nom.equals("DungeonFloorTorch6 5"))result = "DungeonFloorTorch (6, 5)";
		if (nom.equals("DungeonFloorTorch7 1"))result = "DungeonFloorTorch (7, 1)";
		if (nom.equals("DungeonFloorTorch7 2"))result = "DungeonFloorTorch (7, 2)";
		if (nom.equals("DungeonFloorTorch7 3"))result = "DungeonFloorTorch (7, 3)";
		if (nom.equals("DungeonFloorTorch7 4"))result = "DungeonFloorTorch (7, 4)";
		if (nom.equals("DungeonFloorTorch7 5"))result = "DungeonFloorTorch (7, 5)";
		if (nom.equals("DungeonFloorTorch8 1"))result = "DungeonFloorTorch (8, 1)";
		if (nom.equals("DungeonFloorTorch8 2"))result = "DungeonFloorTorch (8, 2)";
		if (nom.equals("DungeonFloorTorch8 3"))result = "DungeonFloorTorch (8, 3)";
		if (nom.equals("DungeonFloorTorch8 4"))result = "DungeonFloorTorch (8, 4)";
		if (nom.equals("DungeonFloorTorch8 5"))result = "DungeonFloorTorch (8, 5)";
		if (nom.equals("DungeonFloorTorch9 1"))result = "DungeonFloorTorch (9, 1)";
		if (nom.equals("DungeonFloorTorch9 2"))result = "DungeonFloorTorch (9, 2)";
		if (nom.equals("DungeonFloorTorch9 3"))result = "DungeonFloorTorch (9, 3)";
		if (nom.equals("DungeonFloorTorch9 4"))result = "DungeonFloorTorch (9, 4)";
		if (nom.equals("DungeonFloorTorch9 5"))result = "DungeonFloorTorch (9, 5)";
		if (nom.equals("DungeonFloorTorch10 1"))result = "DungeonFloorTorch (10, 1)";
		if (nom.equals("DungeonFloorTorch10 2"))result = "DungeonFloorTorch (10, 2)";
		if (nom.equals("DungeonFloorTorch10 3"))result = "DungeonFloorTorch (10, 3)";
		if (nom.equals("DungeonFloorTorch10 4"))result = "DungeonFloorTorch (10, 4)";
		if (nom.equals("DungeonFloorTorch10 5"))result = "DungeonFloorTorch (10, 5)";
		//idem
		if (nom.equals("Dtm1 1"))result = "Dtm (1, 1)";
		if (nom.equals("Dtm1 2"))result = "Dtm (1, 2)";
		if (nom.equals("Dtm1 3"))result = "Dtm (1, 3)";
		if (nom.equals("Dtm1 4"))result = "Dtm (1, 4)";
		if (nom.equals("Dtm1 5"))result = "Dtm (1, 5)";
		if (nom.equals("Dtm2 1"))result = "Dtm (2, 1)";
		if (nom.equals("Dtm2 2"))result = "Dtm (2, 2)";
		if (nom.equals("Dtm2 3"))result = "Dtm (2, 3)";
		if (nom.equals("Dtm2 4"))result = "Dtm (2, 4)";
		if (nom.equals("Dtm2 5"))result = "Dtm (2, 5)";
		if (nom.equals("Dtm3 1"))result = "Dtm (3, 1)";
		if (nom.equals("Dtm3 2"))result = "Dtm (3, 2)";
		if (nom.equals("Dtm3 3"))result = "Dtm (3, 3)";
		if (nom.equals("Dtm3 4"))result = "Dtm (3, 4)";
		if (nom.equals("Dtm3 5"))result = "Dtm (3, 5)";
		if (nom.equals("Dtm4 1"))result = "Dtm (4, 1)";
		if (nom.equals("Dtm4 2"))result = "Dtm (4, 2)";
		if (nom.equals("Dtm4 3"))result = "Dtm (4, 3)";
		if (nom.equals("Dtm4 4"))result = "Dtm (4, 4)";
		if (nom.equals("Dtm4 5"))result = "Dtm (4, 5)";
		if (nom.equals("Dtm5 1"))result = "Dtm (5, 1)";
		if (nom.equals("Dtm5 2"))result = "Dtm (5, 2)";
		if (nom.equals("Dtm5 3"))result = "Dtm (5, 3)";
		if (nom.equals("Dtm5 4"))result = "Dtm (5, 4)";
		if (nom.equals("Dtm5 5"))result = "Dtm (5, 5)";
		//idem
		if (nom.equals("Floor Wooden 1"))result = "Floor Wooden (1, 1)";
		if (nom.equals("Floor Wooden 2"))result = "Floor Wooden (1, 2)";
		if (nom.equals("Floor Wooden 3"))result = "Floor Wooden (1, 3)";
		if (nom.equals("Floor Wooden 4"))result = "Floor Wooden (2, 1)";
		if (nom.equals("Floor Wooden 5"))result = "Floor Wooden (2, 2)";
		if (nom.equals("Floor Wooden Separation"))result = "Floor Wooden (2, 3)";
		//Il manque un espace après la virgule dans le nom des tuiles Lava(x,x) ça empêche le calcul du modulo...
		if (nom.equals("Lava (1,1)"))result = "Lava (1, 1)";
		if (nom.equals("Lava (1,2)"))result = "Lava (1, 2)";
		if (nom.equals("Lava (1,3)"))result = "Lava (1, 3)";
		if (nom.equals("Lava (1,4)"))result = "Lava (1, 4)";
		if (nom.equals("Lava (2,1)"))result = "Lava (2, 1)";
		if (nom.equals("Lava (2,2)"))result = "Lava (2, 2)";
		if (nom.equals("Lava (2,3)"))result = "Lava (2, 3)";
		if (nom.equals("Lava (2,4)"))result = "Lava (2, 4)";
		if (nom.equals("Lava (3,1)"))result = "Lava (3, 1)";
		if (nom.equals("Lava (3,2)"))result = "Lava (3, 2)";
		if (nom.equals("Lava (3,3)"))result = "Lava (3, 3)";
		if (nom.equals("Lava (3,4)"))result = "Lava (3, 4)";
		if (nom.equals("Lava (4,1)"))result = "Lava (4, 1)";
		if (nom.equals("Lava (4,2)"))result = "Lava (4, 2)";
		if (nom.equals("Lava (4,3)"))result = "Lava (4, 3)";
		if (nom.equals("Lava (4,4)"))result = "Lava (4, 4)";
		return result;
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

	static String extractChemin(ByteBuffer buf, SpriteName sn) {
		byte[] bytes = new byte[256];
		buf.get(bytes);
		String result = new String(bytes);
		result = result.substring(0, result.indexOf(0x00));
		result = result.replace("_","");
		result = result.replace(".","");
		result = result.replace("\\", "/");
		String[] split = result.split("\\/");
		result = split[split.length-1];
		result = manageAtlasSpecialCases(result, sn.getName());
		return result;
	}
	
	/**
	 * Modifies original sprite data structure to reduce atlas size
	 * @param atlas
	 * @param nom
	 * @return
	 */
	private static String manageAtlasSpecialCases(String atlas, String nom) {
		String result = atlas;
		if (atlas.equals("Miscs")){
			result = "Miscs-"+nom.toUpperCase().toCharArray()[0];
		}
		if (atlas.equals("Montain")){
			result = nom;
		}
		return result;
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
			UpdateDataCheckStatus.setDpdStatus("Palettes extraites : "+i+"/"+nb_palettes);
			UpdateDataCheckStatus.setStatus("Palettes extraites : "+i+"/"+nb_palettes);
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
