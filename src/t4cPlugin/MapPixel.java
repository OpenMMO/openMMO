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
	public boolean tuile;
	public String atlas;
	//TODO passer tout private
	private SpriteName name;
	public Point offset;
	public Point modulo;
	public int id;
	
	public MapPixel(boolean tuile, String atlas, String tex, Point offset, Point modulo, int id){
		this.tuile = tuile;
		this.atlas = atlas;
		this.name = new SpriteName(tex);
		this.offset = offset;
		this.modulo = modulo;
		this.id = id;
	}

	public String getTex() {
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
	
}
