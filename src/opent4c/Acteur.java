package opent4c;

import java.awt.Point;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Cette classe sera étendue plus tard.
 * Il s'agit de fournir un objet affichable pouvant recevoir et envoyer des actions.
 * Elle est à ajouter à une Stage pour être affichée
 * @author synoga
 *
 */
public class Acteur extends Actor {
    TextureRegion tex;
	private Sprite sp;

    public Acteur (TextureRegion tex, Point coord, Point offset) {
        this.tex = tex;
        if (!tex.isFlipY()) tex.flip(false, true);
        setScale(1);
        setX(32*coord.x+offset.x);
        setY(16*coord.y+offset.y);
        setWidth(tex.getRegionWidth());
        setHeight(tex.getRegionHeight());
    }
    
    public Acteur(Sprite sp, Point coord, Point offset) {
        this.sp = sp;
        if (!sp.isFlipY()) sp.flip(false, true);
        setScale(1);
        setX(32*coord.x+offset.x);
        setY(16*coord.y+offset.y);
        setWidth(sp.getRegionWidth());
        setHeight(sp.getRegionHeight());	}

	public void flip(boolean flipX, boolean flipY){
    	this.tex.flip(flipX, flipY);
    }
    
    @Override
    public void draw (Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        if(tex != null)batch.draw(tex, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        if(sp != null)batch.draw(sp, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }
}