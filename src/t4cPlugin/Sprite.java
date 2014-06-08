package t4cPlugin;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
	
	static int correspondances = 0;
	private SpriteName nom;
	private String chemin = "";
	boolean tuile = false;
	ArrayList<Integer> id = new ArrayList<Integer>();
	ArrayList<Integer> pos = new ArrayList<Integer>();
	int indexation;
	int index_next;
	int zoneX = 1;
	int zoneY = 1;
	private int type;//short
	private int ombre;//short
	private int largeur;//short
	private int hauteur;//short
	int inconnu9;//short
	private byte couleurTrans;//short
	private int offsetX;//short
	private int offsetY;//short
	private int offsetX2;//short
	private int offsetY2;//short
	static int maxOffsetX = 0;
	static int maxOffsetY = 0;
	static int maxX = 0;
	static int maxY = 0;
	private long numDda;
	public long taille_unzip = -1;
	public long taille_zip = -1;
	public DPDPalette palette;
	public int bufPos = -1;
	public int moduloX = 1;
	public int moduloY = 1;
	static int last = -1;
	
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
		setChemin(new String(bytes));
		setChemin(getChemin().substring(0, getChemin().indexOf(0x00)));
		setChemin(getChemin().replace("_",""));
		setChemin(getChemin().replace(".",""));
		setChemin(getChemin().replace("\\", "/"));
		String[] split;
		split = getChemin().split("\\/");
		setChemin(split[split.length-1]);
		//if (chemin.startsWith("Cemetery Gates")) chemin = "Cemetery Gates";
		//logger.info("		- Chemin : "+chemin+" Nom : "+nom);
		
		byte b1,b2,b3,b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		indexation = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
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
		setNumDda(tools.ByteArrayToNumber.bytesToLong(new byte[]{b8,b7,b6,b5,b4,b3,b2,b1}));
		//logger.info("		- N° DDA : "+numDda);
		
		Iterator <Integer> iter = DID.getIds().keySet().iterator();
		while (iter.hasNext()){
			int val = iter.next();
			SpriteName sn = DID.getIds().get(val);
			if (nomExtrait.contains(sn.getName())){
				id.add(val);
				correspondances++;
				DID.getSprites_with_ids().put(val, this);
				if(DID.getSprites_with_ids().size() != last) logger.info("Nombre de Sprites avec ID : "+DID.getSprites_with_ids().size()+". Ajout : "+val+" => "+nomExtrait);
			}
			/*if (nom.contains("(")&nom.contains(", ")&nom.contains(")")){
				if (nom.contains(DID.ids.get(val))){
					id.add(val);
					correspondances++;
					DID.sprites_with_ids.put(val, this);
					if(DID.sprites_with_ids.size() != last) logger.info(DID.sprites_with_ids.size()+" ID ajoutée(s).");

				}
			}*/
			last = DID.getSprites_with_ids().size();
			if (nomExtrait.equals("Black Tile")){
				DID.setBlack(this);
			}
		}
		
		//logger.info("Nouveau Sprite : "+chemin+nom);
		this.nom = new SpriteName(nomExtrait);
		Params.STATUS = "Nouveau Sprite : "+getChemin()+nomExtrait;
		DID.getSprites_without_ids().put(DID.getSprites_without_ids().size(), this);


	}
	
	public Sprite(boolean tuile, String atlas, String tex, int offsetX,	int offsetY, int moduloX, int moduloY, int id) {
		this.tuile = tuile;
		this.setChemin(atlas);
		this.nom = new SpriteName(tex);
		this.setOffsetX(offsetX);
		this.setOffsetY(offsetY);
		this.moduloX = moduloX;
		this.moduloY = moduloY;
		this.id = new ArrayList<Integer>();
		this.id.add(id);
	}

	public int compareTo(Sprite o2) {
		return indexation-o2.indexation;
	}
	
	public String getName() {
		return nom.getName();
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
}
