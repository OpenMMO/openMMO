package t4cPlugin;

import java.awt.Dimension;
import java.io.File;

public class SpriteXML {
	boolean tuile;
	File file;
	Dimension coord;
	
	public SpriteXML(boolean tuile, Dimension coord , File file){
		this.tuile = tuile;
		this.coord = coord;
		this.file = file;
	}
}
