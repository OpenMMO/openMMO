package opent4c;

import java.io.Serializable;

public class PalettePixel implements Serializable{
	private static final long serialVersionUID = 6929155579441043593L;
	short red;
	short green;
	short blue;
	public PalettePixel(short b, short c, short d) {
		red = b;
		green = c;
		blue = d;
	}
	
	public short getRed() {
		return red;
	}
	public void setRed(short red) {
		this.red = red;
	}
	public short getGreen() {
		return green;
	}
	public void setGreen(short green) {
		this.green = green;
	}
	public short getBlue() {
		return blue;
	}
	public void setBlue(short blue) {
		this.blue = blue;
	}
}