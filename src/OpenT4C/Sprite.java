package OpenT4C;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.SpriteName;


/**
 *   PStructureNomsSprites = ^StructureNomsSprites;
  StructureNomsSprites = Record
    Nom : Array [0..63] Of Char;
    Chemin : Array [0..255] Of Char;
    Indexation : LongInt;
    NumDda : Int64;
  End;
 * @author synoga
 *
 */

public class Sprite {
	
	private static Logger logger = LogManager.getLogger(Sprite.class.getSimpleName());
	private SpriteName nom;
	private String chemin = "";
	private boolean tuile = false;
	private boolean hasID = false;
	private ArrayList<Integer> id = new ArrayList<Integer>();
	private ArrayList<Integer> pos = new ArrayList<Integer>();
	private int indexation;
	private int index_next;
	private int zoneX = 1;
	private int zoneY = 1;
	private int type;//short
	private int ombre;//short
	private int largeur;//short
	private int hauteur;//short
	private int inconnu9;//short
	private byte couleurTrans;//short
	private int offsetX;//short
	private int offsetY;//short
	private int offsetX2;//short
	private int offsetY2;//short
	private static int maxOffsetX = 0;
	private static int maxOffsetY = 0;
	private static int maxX = 0;
	private static int maxY = 0;
	private long numDda;
	private long taille_unzip = -1;
	private long taille_zip = -1;
	private Palette palette;
	private int bufPos = -1;
	private int moduloX = 1;
	private int moduloY = 1;
	private static int last = -1;
	
	public Sprite(){}
	
	@Deprecated
	public Sprite(ByteBuffer buf) {
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

		
		bytes = new byte[256];
		buf.get(bytes);
		chemin = new String(bytes);
		chemin = chemin.substring(0, chemin.indexOf(0x00));
		chemin = chemin.replace("_","");
		chemin = chemin.replace(".","");
		chemin = chemin.replace("\\", "/");
		String[] split;
		split = chemin.split("\\/");
		chemin = split[split.length-1];
		//if (chemin.startsWith("Cemetery Gates")) chemin = "Cemetery Gates";
		//logger.info("		- Chemin : "+chemin+" Nom : "+nom);
		
		byte b1,b2,b3,b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		setIndexation(tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1}));
		//logger.info("		- Indexation : "+indexation);
		
		byte b5,b6,b7,b8;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		b5 = buf.get();
		b6 = buf.get();
		b7 = buf.get();
		b8 = buf.get();
		numDda = tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1});
		//logger.info("		- N° DDA : "+numDda);
		
		/*Iterator <Integer> iter = AtlasFactory.getIds().keySet().iterator();
		while (iter.hasNext()){
			int val = iter.next();
			SpriteName sn = AtlasFactory.getIds().get(val);
			if (nomExtrait.contains(sn.getName())){
				getId().add(val);
				correspondances++;
				AtlasFactory.getSprites_with_ids().put(val, this);
				if(AtlasFactory.getSprites_with_ids().size() != last) logger.info("Nombre de Sprites avec ID : "+AtlasFactory.getSprites_with_ids().size()+". Ajout : "+val+" => "+nomExtrait);
			}
			/*if (nom.contains("(")&nom.contains(", ")&nom.contains(")")){
				if (nom.contains(DID.ids.get(val))){
					id.add(val);
					correspondances++;
					DID.sprites_with_ids.put(val, this);
					if(DID.sprites_with_ids.size() != last) logger.info(DID.sprites_with_ids.size()+" ID ajoutée(s).");

				}
			}
			last = AtlasFactory.getSprites_with_ids().size();
			if (nomExtrait.equals("Black Tile")){
				AtlasFactory.setBlack(this);
			}
		}
		
		//logger.info("Nouveau Sprite : "+chemin+nom);
		setNom(new SpriteName(nomExtrait));
		AtlasFactory.getSprites_without_ids().put(AtlasFactory.getSprites_without_ids().size(), this);*/


	}
	
	public Sprite(boolean tuile, String atlas, String tex, int offsetX,	int offsetY, int moduloX, int moduloY, int id) {
		this.setTuile(tuile);
		this.chemin = atlas;
		this.setSpriteName(new SpriteName(tex));
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.setModuloX(moduloX);
		this.setModuloY(moduloY);
		this.setId(new ArrayList<Integer>());
		this.id.add(id);
	}

	public int compareTo(Sprite o2) {
		return getIndexation()-o2.getIndexation();
	}
	
	public String getName() {
		return getSpriteName().getName();
	}

	public String getChemin() {
		return chemin;
	}

	public void setChemin(String chemin) {
		this.chemin = chemin;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getOmbre() {
		return ombre;
	}

	public void setOmbre(int ombre) {
		this.ombre = ombre;
	}

	public int getLargeur() {
		return largeur;
	}

	public void setLargeur(int largeur) {
		this.largeur = largeur;
	}

	public int getHauteur() {
		return hauteur;
	}

	public void setHauteur(int hauteur) {
		this.hauteur = hauteur;
	}

	public byte getCouleurTrans() {
		return couleurTrans;
	}

	public void setCouleurTrans(byte couleurTrans) {
		this.couleurTrans = couleurTrans;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}

	public int getOffsetX2() {
		return offsetX2;
	}

	public void setOffsetX2(int offsetX2) {
		this.offsetX2 = offsetX2;
	}

	public int getOffsetY2() {
		return offsetY2;
	}

	public void setOffsetY2(int offsetY2) {
		this.offsetY2 = offsetY2;
	}

	public long getNumDda() {
		return numDda;
	}

	public void setNumDda(long numDda) {
		this.numDda = numDda;
	}

	public SpriteName getSpriteName() {
		return nom;
	}

	public void setSpriteName(SpriteName nom) {
		this.nom = nom;
	}

	public int getIndexation() {
		return indexation;
	}

	public void setIndexation(int indexation) {
		this.indexation = indexation;
	}

	/**
	 * donne le premier id de la liste ou -1 si le sprite n'a pas d'id associé
	 */
	public Integer getId() {
		if (id.size() != 0){
			return id.get(0);
		} else{
			return -1;
		}
	}

	public void setId(ArrayList<Integer> id) {
		this.id = id;
	}

	public boolean isTuile() {
		return tuile;
	}

	public void setTuile(boolean tuile) {
		this.tuile = tuile;
	}

	public Palette getPalette() {
		return palette;
	}

	public void setPalette(Palette palette) {
		this.palette = palette;
	}

	public int getInconnu9() {
		return inconnu9;
	}

	public void setInconnu9(int inconnu9) {
		this.inconnu9 = inconnu9;
	}

	public long getTaille_unzip() {
		return taille_unzip;
	}

	public void setTaille_unzip(long taille_unzip) {
		this.taille_unzip = taille_unzip;
	}

	public long getTaille_zip() {
		return taille_zip;
	}

	public void setTaille_zip(long taille_zip) {
		this.taille_zip = taille_zip;
	}

	public int getBufPos() {
		return bufPos;
	}

	public void setBufPos(int bufPos) {
		this.bufPos = bufPos;
	}

	public int getModuloX() {
		return moduloX;
	}

	public void setModuloX(int moduloX) {
		this.moduloX = moduloX;
	}

	public int getModuloY() {
		return moduloY;
	}

	public void setModuloY(int moduloY) {
		this.moduloY = moduloY;
	}
}
