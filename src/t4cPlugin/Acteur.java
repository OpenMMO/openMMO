package t4cPlugin;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Acteur extends Actor {
    Sprite sprite;

    public Acteur (Sprite sp) {
        sprite = sp;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        sprite.draw(batch);
    }
}