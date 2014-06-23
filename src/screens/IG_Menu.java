package screens;

import java.awt.Point;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class IG_Menu extends Actor{
	ShapeRenderer shapeRenderer = new ShapeRenderer();
	private int x = 0;
	private int y = 0;
	private int w = 10;
	private int h = 10;
	private Point point;
	
	public IG_Menu(int x, int y, int w, int h, Point p){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.setPoint(p);
	}
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
        batch.begin();
    }
	public Point getPoint() {
		return point;
	}
	public void setPoint(Point point) {
		this.point = point;
	}	
}
