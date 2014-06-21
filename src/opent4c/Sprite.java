package opent4c;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.SpriteName;
import tools.ByteArrayToNumber;
import tools.UnsignedInt;
import tools.UnsignedShort;

public class Sprite {
	
	static Logger logger = LogManager.getLogger(Sprite.class.getSimpleName());

	private SpriteName nom;
	private String chemin = "";
	private boolean tuile = false;
	private ArrayList<Integer> id = new ArrayList<Integer>();
	//private ArrayList<Integer> pos = new ArrayList<Integer>();
	private UnsignedInt indexation;
	private UnsignedShort type;//short
	private UnsignedShort ombre;//short
	private UnsignedShort largeur;//short
	private UnsignedShort hauteur;//short
	private UnsignedShort inconnu9;//short
	private UnsignedShort couleurTrans;//short
	private UnsignedShort offsetX;//short
	private UnsignedShort offsetY;//short
	private UnsignedShort offsetX2;//short
	private UnsignedShort offsetY2;//short
	private long numDda;
	private UnsignedInt taille_unzip;
	private UnsignedInt taille_zip;
	private Palette palette;
	private int bufPos = -1;
	private int moduloX = 1;
	private int moduloY = 1;
	
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
		setIndexation(new UnsignedInt(new byte[]{b4,b3,b2,b1}));
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
		numDda = ByteArrayToNumber.bytesToLong(new byte[]{b1,b2,b3,b4,b5,b6,b7,b8});
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
	
	@Deprecated
	public Sprite(boolean tuile, String atlas, String tex, UnsignedShort offsetX,	UnsignedShort offsetY, int moduloX, int moduloY, int id) {
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

	public long compareTo(Sprite o2) {
		return getIndexation().getValue()-o2.getIndexation().getValue();
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

	public UnsignedShort getType() {
		return type;
	}

	public void setType(UnsignedShort type) {
		this.type = type;
	}

	public UnsignedShort getOmbre() {
		return ombre;
	}

	public void setOmbre(UnsignedShort ombre) {
		this.ombre = ombre;
	}

	public UnsignedShort getLargeur() {
		return largeur;
	}

	public void setLargeur(UnsignedShort largeur) {
		this.largeur = largeur;
	}

	public UnsignedShort getHauteur() {
		return hauteur;
	}

	public void setHauteur(UnsignedShort hauteur) {
		this.hauteur = hauteur;
	}

	public UnsignedShort getCouleurTrans() {
		return couleurTrans;
	}

	public void setCouleurTrans(UnsignedShort couleurTrans) {
		this.couleurTrans = couleurTrans;
	}

	public UnsignedShort getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(UnsignedShort offsetX) {
		this.offsetX = offsetX;
	}

	public UnsignedShort getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(UnsignedShort offsetY) {
		this.offsetY = offsetY;
	}

	public UnsignedShort getOffsetX2() {
		return offsetX2;
	}

	public void setOffsetX2(UnsignedShort offsetX2) {
		this.offsetX2 = offsetX2;
	}

	public UnsignedShort getOffsetY2() {
		return offsetY2;
	}

	public void setOffsetY2(UnsignedShort offsetY2) {
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

	public UnsignedInt getIndexation() {
		return indexation;
	}

	public void setIndexation(UnsignedInt indexation) {
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

	public UnsignedShort getInconnu9() {
		return inconnu9;
	}

	public void setInconnu9(UnsignedShort inconnu9) {
		this.inconnu9 = inconnu9;
	}

	public UnsignedInt getTaille_unzip() {
		return taille_unzip;
	}

	public void setTaille_unzip(UnsignedInt taille_unzip) {
		this.taille_unzip = taille_unzip;
	}

	public UnsignedInt getTaille_zip() {
		return taille_zip;
	}

	public void setTaille_zip(UnsignedInt taille_zip) {
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
	
	public void printInfos(){
		logger.info("Nom : "+getName());
		logger.info("Chemin : "+getChemin());
		logger.info("ID : "+getId());
		logger.info("Tuile : "+isTuile());
		logger.info("Largeur : "+getLargeur().getValue());
		logger.info("Hauteur : "+getHauteur().getValue());
		logger.info("OffsetX : "+getOffsetX().getValue());
		logger.info("OffsetY : "+getOffsetY().getValue());
		logger.info("OffsetX2 : "+getOffsetX2().getValue());
		logger.info("OffsetY2 : "+getOffsetY2().getValue());
		logger.info("Palette : "+getPalette().getName());
		logger.info("Buffer Position : "+getBufPos());
		logger.info("Couleur Transparence : "+getCouleurTrans().getValue());
		logger.info("Inconnu9 : "+getInconnu9().getValue());
		logger.info("Indexation : "+getIndexation().getValue());
		logger.info("ModuloX : "+getModuloX());
		logger.info("ModuloY : "+getModuloY());
		logger.info("Num DDA : "+getNumDda());
		logger.info("Ombre : "+getOmbre().getValue());
		logger.info("Taille unzip : "+getTaille_unzip().getValue());
		logger.info("Taille zip : "+getTaille_zip().getValue());
		logger.info("Type : "+getType().getValue());
	}
}
