package opent4c;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.SpriteName;
import t4cPlugin.utils.LoadingStatus;
import t4cPlugin.utils.RunnableCreatorUtil;
import t4cPlugin.utils.ThreadsUtil;
import tools.ByteArrayToNumber;
import tools.DataInputManager;
import tools.UnsignedInt;

public class SpriteManager {
	
	static Logger logger = LogManager.getLogger(SpriteManager.class.getSimpleName());
	private static LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	//TODO initialiser la liste avec le nombre de sprites
	private static Map<SpriteName,Sprite> sprites;
	static Map<Integer,SpriteName> ids = null;
	static Map<String,Palette> palettes = null;
	static int nb_palettes_from_dpd = -1;
	static int nb_sprites_from_did = -1;
	private static boolean dpd_done = false;
	private static boolean did_done = false;
	private static boolean dda_done = false;


	/**
	 * Adds a new sprite
	 * @param buf
	 */
	public static void addSprite(ByteBuffer buf) {
		Sprite sprite = new Sprite();
		sprite.setSpriteName(SpriteUtils.extractName(buf));
		sprite.setChemin(SpriteUtils.extractChemin(buf));
		sprite.setIndexation(new UnsignedInt(SpriteUtils.extractInt(buf,false)));
		sprite.setNumDda(SpriteUtils.extractLong(buf,false));
		sprite.setId(getIds(sprite));
		manageSpecialCases(sprite);
		putSprite(sprite);
	}

	/**
	 * @param sprite
	 */
	private static void putSprite(Sprite sprite) {
		sprites.put(sprite.getSpriteName(), sprite);
	}

	/**
	 * Cette classe sert à gérer les cas spéciaux dans les noms de sprites/tuiles afin d'uniforiser et optimiser la création et l'utilisation des atlas
	 * @param newSprite
	 */
	private static void manageSpecialCases(Sprite sprite) {
		//De base l'atlas Miscs fait plus de 300 pages, c'est long à charger, je le coupe suivant l'ordre alphabétique
		if (sprite.getChemin().equals("Miscs")){
			sprite.setChemin("Miscs-"+sprite.getName().toUpperCase().toCharArray()[0]);
		}
		//les sprites de montagne étant assez gros, je fais un atlas par sprite, pour éviter d'avoir à tout charger en une fois
		if (sprite.getChemin().equals("Montain")){
			sprite.setChemin(sprite.getName());
		}
		//les sprites Rockflor n'ont pas les coordonées dans leur nom, rendant impossible le calcul du modulo, je modifie les noms pour coller à mon standard de calcul de modulo.
		if (sprite.getName().contains("Rockflor")){
			if (sprite.getName().equals("Rockflor 1")){
				sprite.setSpriteName(new SpriteName("RockFlor (1, 1)"));
			}
			if (sprite.getName().equals("Rockflor 2")){
				sprite.setSpriteName(new SpriteName("RockFlor (1, 2)"));
			}
			if (sprite.getName().equals("Rockflor 3")){
				sprite.setSpriteName(new SpriteName("RockFlor (1, 3)"));
			}
			if (sprite.getName().equals("Rockflor 4")){
				sprite.setSpriteName(new SpriteName("RockFlor (2, 1)"));
			}
			if (sprite.getName().equals("Rockflor 5")){
				sprite.setSpriteName(new SpriteName("RockFlor (2, 2)"));
			}
			if (sprite.getName().equals("Rockflor 6")){
				sprite.setSpriteName(new SpriteName("RockFlor (2, 3)"));
			}
			if (sprite.getName().equals("Rockflor 7")){
				sprite.setSpriteName(new SpriteName("RockFlor (3, 1)"));
			}
			if (sprite.getName().equals("Rockflor 8")){
				sprite.setSpriteName(new SpriteName("RockFlor (3, 2)"));
			}
			if (sprite.getName().equals("Rockflor 9")){
				sprite.setSpriteName(new SpriteName("RockFlor (3, 3)"));
			}
		}
		//idem pour l'atlas lava
		if (sprite.getChemin().equals("OldLava")){
			if (sprite.getName().equals("Lava 1")){
				sprite.setSpriteName(new SpriteName("Lava (1, 1)"));
			}
			if (sprite.getName().equals("Lava 2")){
				sprite.setSpriteName(new SpriteName("Lava (1, 2)"));
			}
			if (sprite.getName().equals("Lava 3")){
				sprite.setSpriteName(new SpriteName("Lava (1, 3)"));
			}
			if (sprite.getName().equals("Lava 4")){
				sprite.setSpriteName(new SpriteName("Lava (1, 4)"));
			}
			if (sprite.getName().equals("Lava 5")){
				sprite.setSpriteName(new SpriteName("Lava (1, 5)"));
			}
			if (sprite.getName().equals("Lava 6")){
				sprite.setSpriteName(new SpriteName("Lava (2, 1)"));
			}
			if (sprite.getName().equals("Lava 7")){
				sprite.setSpriteName(new SpriteName("Lava (2, 2)"));
			}
			if (sprite.getName().equals("Lava 8")){
				sprite.setSpriteName(new SpriteName("Lava (2, 3)"));
			}
			if (sprite.getName().equals("Lava 9")){
				sprite.setSpriteName(new SpriteName("Lava (2, 4)"));
			}
			if (sprite.getName().equals("Lava 10")){
				sprite.setSpriteName(new SpriteName("Lava (2, 5)"));
			}
		}
		//idem
		if (sprite.getChemin().equals("DungeonFloorTorch")){
			if (sprite.getName().equals("DungeonFloorTorch1 1")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (1, 1)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch1 2")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (1, 2)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch1 3")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (1, 3)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch1 4")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (1, 4)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch1 5")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (1, 5)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch2 1")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (2, 1)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch2 2")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (2, 2)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch2 3")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (2, 3)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch2 4")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (2, 4)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch2 5")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (2, 5)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch3 1")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (3, 1)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch3 2")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (3, 2)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch3 3")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (3, 3)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch3 4")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (3, 4)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch3 5")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (3, 5)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch4 1")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (4, 1)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch4 2")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (4, 2)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch4 3")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (4, 3)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch4 4")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (4, 4)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch4 5")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (4, 5)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch5 1")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (5, 1)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch5 2")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (5, 2)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch5 3")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (5, 3)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch5 4")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (5, 4)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch5 5")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (5, 5)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch6 1")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (6, 1)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch6 2")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (6, 2)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch6 3")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (6, 3)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch6 4")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (6, 4)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch6 5")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (6, 5)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch7 1")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (7, 1)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch7 2")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (7, 2)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch7 3")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (7, 3)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch7 4")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (7, 4)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch7 5")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (7, 5)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch8 1")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (8, 1)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch8 2")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (8, 2)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch8 3")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (8, 3)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch8 4")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (8, 4)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch8 5")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (8, 5)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch9 1")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (9, 1)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch9 2")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (9, 2)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch9 3")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (9, 3)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch9 4")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (9, 4)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch9 5")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (9, 5)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch10 1")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (10, 1)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch10 2")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (10, 2)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch10 3")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (10, 3)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch10 4")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (10, 4)"));
			}
			if (sprite.getName().equals("DungeonFloorTorch10 5")){
				sprite.setSpriteName(new SpriteName("DungeonFloorTorch (10, 5)"));
			}
		}
		//idem
		if (sprite.getChemin().equals("Dungeon")){
			if (sprite.getName().equals("Dtm1 1")){
				sprite.setSpriteName(new SpriteName("Dtm (1, 1)"));
			}
			if (sprite.getName().equals("Dtm1 2")){
				sprite.setSpriteName(new SpriteName("Dtm (1, 2)"));
			}
			if (sprite.getName().equals("Dtm1 3")){
				sprite.setSpriteName(new SpriteName("Dtm (1, 3)"));
			}
			if (sprite.getName().equals("Dtm1 4")){
				sprite.setSpriteName(new SpriteName("Dtm (1, 4)"));
			}
			if (sprite.getName().equals("Dtm1 5")){
				sprite.setSpriteName(new SpriteName("Dtm (1, 5)"));
			}
			if (sprite.getName().equals("Dtm2 1")){
				sprite.setSpriteName(new SpriteName("Dtm (2, 1)"));
			}
			if (sprite.getName().equals("Dtm2 2")){
				sprite.setSpriteName(new SpriteName("Dtm (2, 2)"));
			}
			if (sprite.getName().equals("Dtm2 3")){
				sprite.setSpriteName(new SpriteName("Dtm (2, 3)"));
			}
			if (sprite.getName().equals("Dtm2 4")){
				sprite.setSpriteName(new SpriteName("Dtm (2, 4)"));
			}
			if (sprite.getName().equals("Dtm2 5")){
				sprite.setSpriteName(new SpriteName("Dtm (2, 5)"));
			}
			if (sprite.getName().equals("Dtm3 1")){
				sprite.setSpriteName(new SpriteName("Dtm (3, 1)"));
			}
			if (sprite.getName().equals("Dtm3 2")){
				sprite.setSpriteName(new SpriteName("Dtm (3, 2)"));
			}
			if (sprite.getName().equals("Dtm3 3")){
				sprite.setSpriteName(new SpriteName("Dtm (3, 3)"));
			}
			if (sprite.getName().equals("Dtm3 4")){
				sprite.setSpriteName(new SpriteName("Dtm (3, 4)"));
			}
			if (sprite.getName().equals("Dtm3 5")){
				sprite.setSpriteName(new SpriteName("Dtm (3, 5)"));
			}
			if (sprite.getName().equals("Dtm4 1")){
				sprite.setSpriteName(new SpriteName("Dtm (4, 1)"));
			}
			if (sprite.getName().equals("Dtm4 2")){
				sprite.setSpriteName(new SpriteName("Dtm (4, 2)"));
			}
			if (sprite.getName().equals("Dtm4 3")){
				sprite.setSpriteName(new SpriteName("Dtm (4, 3)"));
			}
			if (sprite.getName().equals("Dtm4 4")){
				sprite.setSpriteName(new SpriteName("Dtm (4, 4)"));
			}
			if (sprite.getName().equals("Dtm4 5")){
				sprite.setSpriteName(new SpriteName("Dtm (4, 5)"));
			}
			if (sprite.getName().equals("Dtm5 1")){
				sprite.setSpriteName(new SpriteName("Dtm (5, 1)"));
			}
			if (sprite.getName().equals("Dtm5 2")){
				sprite.setSpriteName(new SpriteName("Dtm (5, 2)"));
			}
			if (sprite.getName().equals("Dtm5 3")){
				sprite.setSpriteName(new SpriteName("Dtm (5, 3)"));
			}
			if (sprite.getName().equals("Dtm5 4")){
				sprite.setSpriteName(new SpriteName("Dtm (5, 4)"));
			}
			if (sprite.getName().equals("Dtm5 5")){
				sprite.setSpriteName(new SpriteName("Dtm (5, 5)"));
			}
		}
		//idem
		if (sprite.getChemin().equals("Wooden")){
			if (sprite.getName().equals("Floor Wooden 1")){
				sprite.setSpriteName(new SpriteName("Floor Wooden (1, 1)"));
			}
			if (sprite.getName().equals("Floor Wooden 2")){
				sprite.setSpriteName(new SpriteName("Floor Wooden (1, 2)"));
			}
			if (sprite.getName().equals("Floor Wooden 3")){
				sprite.setSpriteName(new SpriteName("Floor Wooden (1, 3)"));
			}
			if (sprite.getName().equals("Floor Wooden 4")){
				sprite.setSpriteName(new SpriteName("Floor Wooden (2, 1)"));
			}
			if (sprite.getName().equals("Floor Wooden 5")){
				sprite.setSpriteName(new SpriteName("Floor Wooden (2, 2)"));
			}
			if (sprite.getName().equals("Floor Wooden Separation")){
				sprite.setSpriteName(new SpriteName("Floor Wooden (2, 3)"));
			}
		}
		//Il manque un espace après la virgule dans le nom des tuiles Lava(x,x) ça empêche le calcul du modulo...
		if (sprite.getChemin().equals("Lava")){
			if (sprite.getName().equals("Lava (1,1)")){
				sprite.setSpriteName(new SpriteName("Lava (1, 1)"));
			}
			if (sprite.getName().equals("Lava (1,2)")){
				sprite.setSpriteName(new SpriteName("Lava (1, 2)"));
			}
			if (sprite.getName().equals("Lava (1,3)")){
				sprite.setSpriteName(new SpriteName("Lava (1, 3)"));
			}
			if (sprite.getName().equals("Lava (1,4)")){
				sprite.setSpriteName(new SpriteName("Lava (1, 4)"));
			}
			if (sprite.getName().equals("Lava (2,1)")){
				sprite.setSpriteName(new SpriteName("Lava (2, 1)"));
			}
			if (sprite.getName().equals("Lava (2,2)")){
				sprite.setSpriteName(new SpriteName("Lava (2, 2)"));
			}
			if (sprite.getName().equals("Lava (2,3)")){
				sprite.setSpriteName(new SpriteName("Lava (2, 3)"));
			}
			if (sprite.getName().equals("Lava (2,4)")){
				sprite.setSpriteName(new SpriteName("Lava (2, 4)"));
			}
			if (sprite.getName().equals("Lava (3,1)")){
				sprite.setSpriteName(new SpriteName("Lava (3, 1)"));
			}
			if (sprite.getName().equals("Lava (3,2)")){
				sprite.setSpriteName(new SpriteName("Lava (3, 2)"));
			}
			if (sprite.getName().equals("Lava (3,3)")){
				sprite.setSpriteName(new SpriteName("Lava (3, 3)"));
			}
			if (sprite.getName().equals("Lava (3,4)")){
				sprite.setSpriteName(new SpriteName("Lava (3, 4)"));
			}
			if (sprite.getName().equals("Lava (4,1)")){
				sprite.setSpriteName(new SpriteName("Lava (4, 1)"));
			}
			if (sprite.getName().equals("Lava (4,2)")){
				sprite.setSpriteName(new SpriteName("Lava (4, 2)"));
			}
			if (sprite.getName().equals("Lava (4,3)")){
				sprite.setSpriteName(new SpriteName("Lava (4, 3)"));
			}
			if (sprite.getName().equals("Lava (4,4)")){
				sprite.setSpriteName(new SpriteName("Lava (4, 4)"));
			}
		}
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

	public static Map<SpriteName,Sprite> getSprites() {
		return sprites;
	}

	/**
	 * Extracts sprites from .dda files
	 */
	public static void decryptDDA(boolean doWrite){
		logger.info("Décryptage des fichiers DDA.");
		File f = null;
		List<File> ddas = SourceDataManager.getDDA();
		Iterator<File>iter_dda = ddas.iterator();
		while (iter_dda.hasNext()){
			f = iter_dda.next();
			//logger.info("Décryptage du fichier : "+f.getName());
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getDDAExtractorRunnable(f, doWrite));
		}
	}

	/**
	 * Extracts sprite infos from .did file
	 */
	public static void decryptDID(){
		SpriteManager.initSpriteList();
		File f = SourceDataManager.getDID();
		logger.info("Décryptage du fichier DID.");
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
			DataInputManager in = new DataInputManager(f);
			while (header.position()<header.capacity()){
				header.put(in.readByte());
			}
			header.rewind();
			
			//header_hashMd5 = SpriteUtils.getHeaderHashMD5(header);
			header_hashMd5 = SpriteUtils.extractBytes(header, header_hashMd5.length);
			header_taille_unZip = ByteArrayToNumber.bytesToInt(SpriteUtils.extractInt(header,false));
			header_taille_zip = ByteArrayToNumber.bytesToInt(SpriteUtils.extractInt(header,false));
			//header_hashMd52 = SpriteUtils.getHeaderHashMd52(header);
			header_hashMd52 = SpriteUtils.extractBytes(header, header_hashMd52.length);

	
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
		bufUnZip = SpriteUtils.unzip(buf, header_taille_unZip);
		for (int i=0; i<bufUnZip.capacity(); i++){
			bufUnZip.array()[i] ^= clef;
		}
		nb_sprites_from_did = (header_taille_unZip/(64 + 256 + 4 + 8));
	
		for(int i=1 ; i<=nb_sprites_from_did ; i++){
			addSprite(bufUnZip);
			UpdateScreenManagerStatus.setSubStatus("Sprites lu depuis le fichier DID : "+i+"/"+nb_sprites_from_did);
		}
		setDid_done(true);
	}

	/**
	 * 
	 */
	private static void initSpriteList() {
		sprites = new HashMap<SpriteName,Sprite>(68559);
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
		logger.info("Décryptage du fichier DPD.");
		
		header = ByteBuffer.allocate(41);
		
		try {
			DataInputManager in = new DataInputManager (f);
			while (header.position()<header.capacity()){
				header.put(in.readByte());
			}
			header.rewind();
			
			//header_hashMd5 = SpriteUtils.getHeaderHashMD5(header);
			header_hashMd5 = SpriteUtils.extractBytes(header, header_hashMd5.length);
			header_taille_unZip = ByteArrayToNumber.bytesToInt(SpriteUtils.extractInt(header,false));
			header_taille_zip = ByteArrayToNumber.bytesToInt(SpriteUtils.extractInt(header,false));
			//header_hashMd52 = SpriteUtils.getHeaderHashMd52(header);
			header_hashMd52 = SpriteUtils.extractBytes(header, header_hashMd52.length);

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
		bufUnZip = SpriteUtils.unzip(buf,header_taille_unZip);
		
		for (int i=0; i<bufUnZip.capacity(); i++){
			bufUnZip.array()[i] ^= clef;
		}
		
		nb_palettes_from_dpd = header_taille_unZip/(64 + 768);
		palettes = SpriteUtils.extractPalettes(bufUnZip, nb_palettes_from_dpd);
		//TODO moyen de contrôle
		//logger.info("Fichier "+f.getName()+" lu : "+palettes.size()+" palettes.");
		setDpd_done(true);
	}

	public static boolean isDpd_done() {
		return dpd_done;
	}

	public static void setDpd_done(boolean dpd_done) {
		SpriteManager.dpd_done = dpd_done;
	}

	public static boolean isDid_done() {
		return did_done;
	}

	public static void setDid_done(boolean did_done) {
		SpriteManager.did_done = did_done;
	}

	public static boolean isDda_done() {
		return dda_done;
	}

	public static void setDda_done(boolean dda_done) {
		SpriteManager.dda_done = dda_done;
	}
}

