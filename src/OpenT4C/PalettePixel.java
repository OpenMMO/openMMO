package OpenT4C;

public class PalettePixel{
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