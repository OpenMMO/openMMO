/**
 * 
 */
package screens;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opent4c.Acteur;
import opent4c.MapPixel;
import opent4c.SpriteData;
import opent4c.SpriteManager;
import opent4c.SpritePixel;
import opent4c.SpriteUtils;
import opent4c.TilePixel;
import opent4c.utils.LoadingStatus;
import opent4c.utils.PointsManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

/**
 * @author synoga
 *
 */
public class Edit_menu extends Actor implements InputProcessor {
	private OrthographicCamera camera;
	private static Logger logger = LogManager.getLogger(Edit_menu.class.getSimpleName());
	private LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	
	private Stage stage = new Stage();
	private Stage ui = new Stage();
	private Acteur edited_tile;
	private TextButtonStyle style = new TextButtonStyle();
	private TextButton tex_button, atlas_button, palette_button, id_button, alternative, choosen_alternative_button;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	private int x = 10;
	private int y = 10;
	private int w = Gdx.graphics.getWidth()-20;
	private int h = Gdx.graphics.getHeight()-20;
	private Point point;
	private Acteur selected_alternative_acteur;
	private Group alternative_group = new Group();
	private Group alternative_button_group = new Group();
	private int nb_alternatives = 0;
	private int test_alternative = 0;
	int id = -1;
	List<SpritePixel> pixel_list = null;


	/**
	 * @param pt
	 */
	public Edit_menu(Point pt, Acteur edited) {
		camera = new OrthographicCamera();
		camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		stage.setCamera(camera);
		edited_tile = edited;
		style.font = new BitmapFont();
		int alt_number = 0;
		id = MapManager.getIdAtCoordOnMap("v2_worldmap", pt);
		pixel_list = SpriteData.getAllPixelswithId(id);
		if(pixel_list == null){
			logger.warn("Impossible d'éditer la tuile");
			tex_button = new TextButton("Texture : Unknown", style);
			atlas_button = new TextButton("Atlas : Unknown", style);
			palette_button = new TextButton("Palette : Unknown", style);
			id_button = new TextButton("ID on map : "+id,style);
			alternative = new TextButton("Alternatives : None", style);
			
			atlas_button.setPosition(40, Gdx.graphics.getHeight()-40);
			tex_button.setPosition(40, Gdx.graphics.getHeight()-60);
			palette_button.setPosition(40, Gdx.graphics.getHeight()-80);
			id_button.setPosition(40, Gdx.graphics.getHeight()-100);
			alternative.setPosition(40, Gdx.graphics.getHeight()-(120+(alt_number*20)));
			
			point = pt;
		}else{
			nb_alternatives = pixel_list.size();
			
			MapPixel px = SpriteData.getPixelFromId(id);
			tex_button = new TextButton("Texture : "+px.getTex(), style);
			atlas_button = new TextButton("Atlas : "+px.getAtlas(), style);
			palette_button = new TextButton("Palette : "+px.getPaletteName(), style);
			id_button = new TextButton("ID on map : "+id,style);
			alternative = new TextButton("Alternatives : "+nb_alternatives+"(choose with up & down arrows, validate with Enter, cancel with Escape. Changes will be seen on next launch)", style);
			
			atlas_button.setPosition(40, Gdx.graphics.getHeight()-40);
			tex_button.setPosition(40, Gdx.graphics.getHeight()-60);
			palette_button.setPosition(40, Gdx.graphics.getHeight()-80);
			id_button.setPosition(40, Gdx.graphics.getHeight()-100);
			alternative.setPosition(40, Gdx.graphics.getHeight()-(120+(alt_number*20)));
			
			point = pt;
			//edited_tile.flip(false, true);
			edited_tile.setPosition(40+edited.getWidth()/2, 20+edited.getHeight());
			stage.addActor(edited_tile);
			getAlternative(test_alternative);
			stage.addActor(alternative_group);
			ui.addActor(alternative_button_group);
		}
		ui.addActor(atlas_button);
		ui.addActor(tex_button);
		ui.addActor(palette_button);
		ui.addActor(id_button);
		ui.addActor(alternative);


	}

	private void getAlternative(int index){
		alternative_group.clear();
		alternative_button_group.clear();
		logger.info("Try Alternative :"+index);
		TextureAtlas atlas = null;
		MapPixel pix = pixel_list.get(index);
		if(pix instanceof TilePixel){
			atlas = loadingStatus.getTextureAtlasTile(pix.getAtlas());
		}else{
			atlas = loadingStatus.getTextureAtlasSprite(pix.getAtlas());				
		}
		choosen_alternative_button = new TextButton("Alternative choisie : "+index+" => "+pix.getTex(), style);
		choosen_alternative_button.setPosition(40, alternative.getY()-20);
		TextureRegion tex = atlas.findRegion(pix.getTex());
		selected_alternative_acteur = new Acteur(tex,PointsManager.getPoint(point.x*32, point.y*16),PointsManager.getPoint(0, 0));
		selected_alternative_acteur.setPosition(edited_tile.getX()+edited_tile.getWidth()+20+selected_alternative_acteur.getWidth(), edited_tile.getY());
		alternative_button_group.addActor(choosen_alternative_button);
		alternative_group.addActor(selected_alternative_acteur);
	}
	
    @Override
    public void draw(Batch batch, float parentAlpha) {
        shapeRenderer.begin(ShapeType.Filled);
	        shapeRenderer.setColor(0.3f,0.3f,0.3f,0.3f);
	        shapeRenderer.rect(x, y, w, h);
	    shapeRenderer.end();
	    stage.act(Gdx.graphics.getDeltaTime());
	    ui.act(Gdx.graphics.getDeltaTime());
        batch.begin();
        	stage.draw();
        	ui.draw();
        batch.end();
    }
	
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.ESCAPE){
			dispose();
		}
		if (keycode == Keys.ENTER){
			validateAlternative(test_alternative);
		}
		if (keycode == Keys.DOWN){
			test_alternative++;
			if (test_alternative >= nb_alternatives) test_alternative = nb_alternatives-1;
			getAlternative(test_alternative);
		}
		if (keycode == Keys.UP){
			test_alternative--;
			if (test_alternative <= 0) test_alternative = 0;
			getAlternative(test_alternative);
		}		
		return true;
	}


	/**
	 * @param alternative
	 */
	private void validateAlternative(int alternative) {
		MapPixel pix = pixel_list.get(alternative);
		SpriteManager.ids.get(id).setName(pix.getTex());
		SpriteUtils.writeIdsToFile();
		SpriteData.deleteSpriteDataFile();
		dispose();
	}

	/**
	 * 
	 */
	private void dispose() {
		//edited_tile.flip(false,true);
		stage.clear();
		MapManager.close_edit_menu();
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
