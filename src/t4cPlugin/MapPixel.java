package t4cPlugin;

import java.awt.Point;
import java.io.Serializable;

public class MapPixel implements Serializable{

	/**
	 * C'est un pixel de la carte. il contient toutes les infos sur cette zone.
	 */
	private static final long serialVersionUID = 6974577196048084070L;
	public boolean tuile;
	public String atlas;
	public String tex;
	public Point offset;
	public Point modulo;
	public int id;
	
	public MapPixel(boolean tuile, String atlas, String tex, Point offset, Point modulo, int id){
		this.tuile = tuile;
		this.atlas = atlas;
		this.tex = tex;
		this.offset = offset;
		this.modulo = modulo;
		this.id = id;
	}
}
