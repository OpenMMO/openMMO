package opent4c;

import java.awt.Point;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Player extends Actor{
	private static String name = "foo";
	private static Point position;
	private static Sprite avatar;
	
    public Player(Sprite sp, Point coord, Point offset) {
        this.avatar = sp;
        if (!sp.isFlipY()) sp.flip(false, true);
        setScale(1);
        setX(32*coord.x+offset.x);
        setY(16*coord.y+offset.y);
        setWidth(sp.getRegionWidth());
        setHeight(sp.getRegionHeight());
    }
	
	
    @Override
    public void draw (Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(avatar, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }
}
