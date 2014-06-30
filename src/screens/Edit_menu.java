/**
 * 
 */
package screens;

import java.awt.Point;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opent4c.Acteur;
import opent4c.MapPixel;
import opent4c.SpriteData;
import opent4c.SpritePixel;
import opent4c.TilePixel;
import opent4c.utils.AssetsLoader;
import opent4c.utils.LoadingStatus;
import opent4c.utils.PointsManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

/**
 * @author synoga
 *
 */
public class Edit_menu extends Group implements InputProcessor {
	private static Logger logger = LogManager.getLogger(Edit_menu.class.getSimpleName());
	private LoadingStatus loadingStatus = LoadingStatus.INSTANCE;
	
	private Stage stage;
	private Acteur edited_tile;
	private TextButtonStyle style = new TextButtonStyle();
	private Point point;
	private Group alternative_group = new Group();
	private Group alternative_button_group = new Group();
	private Group edited_group = new Group();
	private int nb_alternatives = 0;
	private int test_alternative = 0;
	int id = -1;
	private List<SpritePixel> spritepixel_list = null;
	private List<TilePixel> tilepixel_list = null;
	private TextButton atlas_button;
	private TextButton palette_button;
	private TextButton id_button;
	private TextButton alternative_button;
	private TextButton tex_button;
	private Actor choosen_alternative_button;
	private boolean tuile;
	private TextButton info_button;


	/**
	 * @param pt
	 */
	public Edit_menu(Point pt, Acteur edited, Stage stage, boolean isTuile) {
		this.stage = stage;
		this.tuile = isTuile;
		point = pt;
		edited_tile = edited;
		id = MapManager.getIdAtCoordOnMap("v2_worldmap", point);
		style.font = new BitmapFont();
		if(checkIfEditedIsKnownTile()){
			tilepixel_list = SpriteData.getAllTileswithId(id);
			editKnownTile(test_alternative);
			return;
		} if(checkIfEditedIsKnownSprite()){
			spritepixel_list = SpriteData.getAllSpriteswithId(id);
			editKnownSprite(test_alternative);
			return;
		}
			editUnknown();
			return;
	}

	/**
	 * @return
	 */
	private boolean checkIfEditedIsKnownSprite() {
		if(!SpriteData.ids.containsKey(id)) return false;
		return !tuile;
	}

	/**
	 * @return
	 */
	private boolean checkIfEditedIsKnownTile() {
		if(!SpriteData.ids.containsKey(id)) return false;
		return tuile;
	}

	/**
	 * @param pt
	 */
	private void editUnknown() {
		String tex = null;
		if(SpriteData.ids.containsKey(id)){
			tex = SpriteData.ids.get(id).getName();
		}else{
			tex = "Texture : ID non mappée";
		}
		clearMenu();
		setButtonsTexts(tex, "Atlas : Unknown", "Palette : Unknown", "ID on map : "+id, "Alternatives : None");
		setButtonsPositions();
		addButtonsToMenu();
	}

	/**
	 * 
	 */
	private void clearMenu() {
		stage.clear();
		this.clear();
	}

	/**
	 * 
	 */
	private void addButtonsToMenu() {
		this.addActor(atlas_button);
		this.addActor(tex_button);
		this.addActor(palette_button);
		this.addActor(id_button);
		this.addActor(alternative_button);		
	}

	/**
	 * 
	 */
	private void setButtonsPositions() {
		atlas_button.setPosition(40, Gdx.graphics.getHeight()-40);
		tex_button.setPosition(40, Gdx.graphics.getHeight()-60);
		palette_button.setPosition(40, Gdx.graphics.getHeight()-80);
		id_button.setPosition(40, Gdx.graphics.getHeight()-100);
		alternative_button.setPosition(40, Gdx.graphics.getHeight()-120);
		info_button.setPosition(40, Gdx.graphics.getHeight()-140);

	}

	/**
	 * 
	 */
	private void setButtonsTexts(String tex, String atlas, String palette, String id, String alternative) {
		tex_button = new TextButton(tex, style);
		atlas_button = new TextButton(atlas, style);
		palette_button = new TextButton(palette, style);
		id_button = new TextButton(id,style);
		alternative_button = new TextButton(alternative, style);
		info_button = new TextButton("use Arrows/Enter/Escape. Changes will be seen on next launch.", style);
	}

	/**
	 * Edits a sprite with a known ID
	 */
	private void editKnownSprite(int test_alternative) {
		nb_alternatives = spritepixel_list.size()-1;
		SpritePixel px = spritepixel_list.get(test_alternative);
		String tex_button = "Texture : "+px.getTex();
		String atlas_button = "Atlas : "+px.getAtlas();
		String palette_button = "Palette : "+px.getPaletteName();
		String id_button = "ID on map : "+id;
		String alternative = "Alternatives : "+nb_alternatives;
		clearMenu();
		addAlternativeSpriteToMenu(test_alternative);
		setButtonsTexts(tex_button, atlas_button, palette_button, id_button, alternative);
		setButtonsPositions();
		addButtonsToMenu();
	}

	/**
	 * Edits a tile with a known ID
	 */
	private void editKnownTile(int test_alternative) {
		nb_alternatives = tilepixel_list.size()-1;
		TilePixel px = tilepixel_list.get(test_alternative);
		String tex_button = "Texture : "+px.getTex();
		String atlas_button = "Atlas : "+px.getAtlas();
		String palette_button = "Palette : "+px.getPaletteName();
		String id_button = "ID on map : "+id;
		String alternative = "Alternatives : "+nb_alternatives;
		clearMenu();
		setButtonsTexts(tex_button, atlas_button, palette_button, id_button, alternative);
		setButtonsPositions();
		addButtonsToMenu();
		addAlternativeTileToMenu(test_alternative);
	}

	private void addAlternativeTileToMenu(int index){
		alternative_group.clear();
		alternative_button_group.clear();
		TilePixel pix = tilepixel_list.get(index);
		String alt = "Alternative choisie : "+index+" => "+pix.getTex();
		addAlternativeButtonToMenu(alt);
		TextureAtlas atlas = loadingStatus.getTextureAtlasTile(pix.getAtlas());
		TextureRegion tex = atlas.findRegion(pix.getTex());
		addAlternativeToMenu(tex);
		logger.info("Try Alternative :"+index+" "+pix.getTex());
	}
	
	/**
	 * @param tex
	 */
	private void addAlternativeToMenu(TextureRegion tex) {
		//Point coord = PointsManager.getPoint(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		Acteur alt = new Acteur(tex,PointsManager.getPoint(edited_tile.getX(), edited_tile.getY()),PointsManager.getPoint(0, 0));
		//Acteur alt = new Acteur(tex,PointsManager.getPoint(point.x*32, point.y*16),PointsManager.getPoint(0, 0));
		//alt.setPosition(edited_tile.getX(), edited_tile.getY());
		alternative_group.addActor(alt);
		stage.addActor(alternative_group);		
	}

	/**
	 * 
	 */
	private void addAlternativeButtonToMenu(String alt) {
		choosen_alternative_button = new TextButton(alt, style);
		choosen_alternative_button.setPosition(40, Gdx.graphics.getHeight()-160);
		alternative_button_group.addActor(choosen_alternative_button);
		this.addActor(alternative_button_group);
	}

	private void addAlternativeSpriteToMenu(int index){
		alternative_group.clear();
		alternative_button_group.clear();
		SpritePixel pix = spritepixel_list.get(index);
		String alt = "Alternative choisie : "+index+" => "+pix.getTex();
		addAlternativeButtonToMenu(alt);
		TextureAtlas atlas = loadingStatus.getTextureAtlasSprite(pix.getAtlas());
		if(atlas == null) atlas = AssetsLoader.load(pix.getAtlas());
		TextureRegion tex = atlas.findRegion(pix.getTex());
		addAlternativeToMenu(tex);
		logger.info("Try Alternative :"+index+" "+pix.getTex());
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
			return true;
		}
		if (keycode == Keys.ENTER){
			if(!checkIfEditedIsKnownTile() & checkIfEditedIsKnownSprite()){
				validateAlternative(test_alternative);
				return true;
			}
		}
		if (keycode == Keys.DOWN | keycode == Keys.LEFT){
			if(checkIfEditedIsKnownTile()){
				if(test_alternative < nb_alternatives ) test_alternative++;
				editKnownTile(test_alternative);
				return true;
			}
			if(checkIfEditedIsKnownSprite()){
				if(test_alternative < nb_alternatives ) test_alternative++;
				editKnownSprite(test_alternative);
				return true;
			}
			editUnknown();
			return true;
		}
		if (keycode == Keys.UP | keycode == Keys.RIGHT){
			if(checkIfEditedIsKnownTile()){
				if(test_alternative > 0 ) test_alternative--;
				editKnownTile(test_alternative);
				return true;
			}
			if(checkIfEditedIsKnownSprite()){
				if(test_alternative > 0 ) test_alternative--;
				editKnownSprite(test_alternative);
				return true;
			}
			editUnknown();
			return true;
		}		
		return false;
	}


	/**
	 * @param alternative
	 */
	private void validateAlternative(int alternative) {
			MapPixel pix = spritepixel_list.get(alternative);
			SpriteData.ids.get(id).setName(pix.getTex());
			SpriteData.writeIdsToFile();
			SpriteData.deleteSpriteDataFileOnExit();
			dispose();
	}

	/**
	 * 
	 */
	private void dispose() {
		this.clear();
		edited_group.clear();
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
