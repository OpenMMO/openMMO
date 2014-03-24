package t4cPlugin;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class IG_Menu extends Actor{
	ShapeRenderer shapeRenderer = new ShapeRenderer();
	int x = 0;
	int y = 0;
	int w = 10;
	int h = 10;
	
	public IG_Menu(int x, int y, int w, int h){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
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
	
}
