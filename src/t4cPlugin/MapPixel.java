package t4cPlugin;

import java.awt.Point;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapPixel implements Serializable{

	private static Logger logger = LogManager.getLogger(MapPixel.class);
	
	/**
	 * C'est un pixel de la carte. il contient toutes les infos sur cette zone.
	 */
	private static final long serialVersionUID = 6974577196048084070L;
	private boolean tuile;
	private String atlas;
	private SpriteName name;
	private Point offset;
	private Point modulo;
	private int id;
	private String palette;
	
	public MapPixel(boolean tuile, String atlas, String tex, Point offset, Point modulo, int id, String palette){
		this.tuile = tuile;
		this.atlas = atlas;
		this.name = new SpriteName(tex);
		this.offset = offset;
		this.modulo = modulo;
		this.id = id;
		this.setPalette(palette);
	}

	public String getTex() {
		if (atlas == null){
			logger.warn("Attention, on tente de récupérer une texture null.");
			logger.warn("ID : "+this.id);
			logger.warn("nom : "+this.name);
		}
		return name.getName();
	}

	public void setTex(String tex) {
		if(tex == null) {
			logger.warn("On essaye de mettre un tex null dans un MapPixel!");
		}
		else {
			this.name.setName(tex);
		}
	}
	
	
	public boolean isTuile() {
		return tuile;
	}
	
	//TODO vérifier l'interet de ce flag. Faire deux classes (avec héritage) ne serait-il pas plus judicieux?
	public void setTuileFlag(boolean isTuile) {
		tuile = isTuile;
	}

	public String getAtlas() {
		if (atlas == null){
			logger.warn("Attention, on tente de récupérer un atlas null.");
			logger.warn("ID : "+this.id);
			logger.warn("nom : "+this.name);
			logger.warn("texture : "+this.getTex());
		}
		return atlas;
	}

	//TODO rajouter une methode pour retirer les comparaisons a "foo" et "bar"
	public void setAtlas(String atlas) {
		this.atlas = atlas;
	}

	public Point getOffset() {
		return offset;
	}

	public void setOffset(Point offset) {
		this.offset = offset;
	}

	public Point getModulo() {
		return modulo;
	}

	public void setModulo(Point modulo) {
		this.modulo = modulo;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPalette() {
		return palette;
	}

	public void setPalette(String palette) {
		this.palette = palette;
	}
}
