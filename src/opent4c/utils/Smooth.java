package opent4c.utils;

import java.awt.Point;

import com.badlogic.gdx.graphics.Pixmap;

public class Smooth {
	private int id = 0;
	private Pixmap template;
	private Point point;
	private int color = 0;
	
	public Smooth(int parseId, Point coord, Pixmap tmpl, int threshold){
		setId(parseId);
		setTemplate(tmpl);
		setPoint(coord);
		setColor(threshold);
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Pixmap getTemplate() {
		return template;
	}

	public void setTemplate(Pixmap template) {
		this.template = template;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}
