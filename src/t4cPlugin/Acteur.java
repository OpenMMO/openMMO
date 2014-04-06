package t4cPlugin;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Cette classe sera étendue plus tard.
 * Il s'agit de fournit un objet affichable pouvant recevoir et envoyer des actions.
 * Elle est à ajouter à une Stage pour être affichée
 * @author synoga
 *
 */
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