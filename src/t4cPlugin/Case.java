package t4cPlugin;

import java.awt.Dimension;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Case{
	boolean tuile;
	TextureRegion tex;
	int offsetX=0;
	int offsetY=0;
	int moduloX = 1;
	int moduloY = 1;
	String atlasName = "";
	String texName = "";
	public Case(boolean tuile, TextureRegion f, Dimension offset){
		this.tuile = tuile;
		this.tex = f;
		this.offsetX = offset.width;
		this.offsetY = offset.height;
	}
	
	public Case(boolean tuile, String atlas, String tex, Dimension offset, Dimension modulo){
		this.tuile = tuile;
		this.atlasName = atlas;
		this.texName = tex;
		this.offsetX = offset.width;
		this.offsetY = offset.height;
		this.moduloX = modulo.width;
		this.moduloY = modulo.height;
		
	}
	
}
