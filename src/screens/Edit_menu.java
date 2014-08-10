/**
 * 
 */
package screens;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opent4c.Acteur;
import opent4c.MapPixel;
import opent4c.SpriteData;
import opent4c.utils.LoadingStatus;
import opent4c.utils.PointsManager;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.SpriteName;
import opent4c.utils.ThreadsUtil;

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
	private Group alternative_group = new Group();
	private Group alternative_button_group = new Group();
	private Group edited_group = new Group();
	private int nb_alternatives = 0;
	private int test_alternative = 0;
	int id = -1;
	private List<MapPixel> pixel_list = null;
	private TextButton atlas_button;
	private TextButton palette_button;
	private TextButton id_button;
	private TextButton tex_button;
	private TextButton choosen_alternative_button;
	private TextButton info_button;


	/**
	 * @param pt
	 */
	public Edit_menu(Point pt, Acteur edited, Stage stage, int id) {
		this.stage = stage;
		edited_tile = edited;
		this.id = id;
		style.font = new BitmapFont();
		if(SpriteData.isKnownId(id)){
			pixel_list = new ArrayList<MapPixel>();
			pixel_list.addAll(getPixelsWithSimilarName(id));
			editKnownPixel(test_alternative);
			return;
		}else {
			editUnknown(test_alternative);
			return;
		}
	}

	/**
	 * @param id
	 * @return
	 */
	private List<MapPixel> getPixelsWithSimilarName(int id) {
		List<MapPixel> lst = new ArrayList<MapPixel>();
		lst.addAll(SpriteData.getPixelsWithSameAtlas(id));
		return lst;
	}

	/**
	 * @param alt 
	 * @param pt
	 */
	private void editUnknown(int test_alternative) {
		String tex = "";
		if (pixel_list == null){
			if(SpriteData.getIds().containsKey(id)){
				tex = SpriteData.getIds().get(id);
				pixel_list = getUnknownAlternatives(tex);
			}else{
				tex = "non présente";
				pixel_list = SpriteData.getUnknownPixels();
			}	
		}
		nb_alternatives = pixel_list.size()-1;
		clearMenu();
		if(nb_alternatives > 0){
			MapPixel px = pixel_list.get(test_alternative);
			String tex_button = "Texture : "+tex;
			String atlas_button = "Atlas : "+px.getAtlas();
			String palette_button = "Palette : "+px.getPaletteName();
			String id_button = "ID on map : "+id+" => "+tex;
			alternative_group.clear();
			alternative_button_group.clear();
			MapPixel pix = pixel_list.get(test_alternative);
			String alt = "Alternative choisie : "+test_alternative+"/"+nb_alternatives+" => "+pix.getTex();
			addAlternativeButtonToMenu(alt);
			TextureAtlas atlas = null;
			if(pix.isTuile()){
				atlas = loadingStatus.getTextureAtlasTile(pix.getAtlas());
			}else{
				atlas = loadingStatus.getTextureAtlasSprite(pix.getAtlas());
			}
			TextureRegion texture = atlas.findRegion(pix.getTex());
			Acteur alter = new Acteur(texture,PointsManager.getPoint(0, 0),PointsManager.getPoint(0, 0));
			alter.setPosition(edited_tile.getX(), edited_tile.getY()-alter.getHeight()+edited_tile.getHeight());
			alternative_group.addActor(alter);
			stage.addActor(alternative_group);
			logger.info("Try Alternative :"+test_alternative+" "+pix.getTex());
			setButtonsTexts(tex_button, atlas_button, palette_button, id_button);
		}else{
			setButtonsTexts("Texture : "+tex, ".", ".", ".");
		}
		setButtonsPositions();
		addButtonsToMenu();
	}

	/**
	 * @param tex 
	 * @return
	 */
	private List<MapPixel> getUnknownAlternatives(String tex) {
		List<MapPixel> list = SpriteData.getUnknownPixels();
		Iterator<Integer> iter_px = SpriteData.getPixelIndex().keySet().iterator();
		while(iter_px.hasNext()){
			List<MapPixel> tmp = SpriteData.getPixelIndex().get(iter_px.next());
			list.addAll(tmp);
		}
		List<MapPixel> result = new ArrayList<MapPixel>();
		Iterator<MapPixel> iter = list.iterator();
		while(iter.hasNext()){
			MapPixel px = iter.next();
			if(isPixelSuitableAlternative(tex, px))result.add(px);
		}
		return result;
	}

	/**
	 * @param tex
	 * @param px
	 * @return
	 */
	private boolean isPixelSuitableAlternative(String tex, MapPixel px) {
		if ((px.getId() > id-10) & (px.getId() < id+10) & (px.getId() != -1)) return true;
		if(px.getAtlas().contains(tex))return true;
		if(px.getTex().contains(tex))return true;
		return false;
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
	}

	/**
	 * 
	 */
	private void setButtonsPositions() {
		atlas_button.setPosition(40, Gdx.graphics.getHeight()-40);
		tex_button.setPosition(40, Gdx.graphics.getHeight()-60);
		palette_button.setPosition(40, Gdx.graphics.getHeight()-80);
		id_button.setPosition(40, Gdx.graphics.getHeight()-100);
		info_button.setPosition(40, Gdx.graphics.getHeight()-120);

	}

	/**
	 * 
	 */
	private void setButtonsTexts(String tex, String atlas, String palette, String id) {
		tex_button = new TextButton(tex, style);
		atlas_button = new TextButton(atlas, style);
		palette_button = new TextButton(palette, style);
		id_button = new TextButton(id,style);
		info_button = new TextButton("use Arrows/Enter/Escape. Changes will be seen on next launch.", style);
	}

	/**
	 * Edits a tile with a known ID
	 */
	private void editKnownPixel(int test_alternative) {
		nb_alternatives = pixel_list.size()-1;
		MapPixel px = pixel_list.get(test_alternative);
		String tex_button = "Texture : "+px.getTex();
		String atlas_button = "Atlas : "+px.getAtlas();
		String palette_button = "Palette : "+px.getPaletteName();
		String id_button = "ID on map : "+id;
		clearMenu();
		setButtonsTexts(tex_button, atlas_button, palette_button, id_button);
		setButtonsPositions();
		addButtonsToMenu();
		addAlternativePixelToMenu(test_alternative);
	}
	
	/**
	 * @param test_alternative2
	 */
	private void addAlternativePixelToMenu(int index) {
		alternative_group.clear();
		alternative_button_group.clear();
		MapPixel pix = pixel_list.get(index);
		String alt = "Alternative choisie : "+index+"/"+nb_alternatives+" => "+pix.getTex();
		addAlternativeButtonToMenu(alt);
		TextureAtlas atlas = null;
		if(pix.isTuile()){
			atlas = loadingStatus.getTextureAtlasTile(pix.getAtlas());
		}else{
			atlas = loadingStatus.getTextureAtlasSprite(pix.getAtlas());
		}
		TextureRegion tex = atlas.findRegion(pix.getTex());
		addAlternativeToMenu(tex);
		logger.info("Try Alternative :"+index+" "+pix.getTex());
	}

	/**
	 * @param tex
	 */
	private void addAlternativeToMenu(TextureRegion tex) {
		Acteur alt = new Acteur(tex,PointsManager.getPoint(0, 0),PointsManager.getPoint(0, 0));
		alt.setPosition(edited_tile.getX(), edited_tile.getY());
		alternative_group.addActor(alt);
		stage.addActor(alternative_group);		
	}

	/**
	 * 
	 */
	private void addAlternativeButtonToMenu(String alt) {
		choosen_alternative_button = new TextButton(alt, style);
		choosen_alternative_button.setPosition(40, Gdx.graphics.getHeight()-120);
		alternative_button_group.addActor(choosen_alternative_button);
		this.addActor(alternative_button_group);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		return false;
	}


	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.ESCAPE){
			dispose();
			return true;
		}
		if (keycode == Keys.ENTER){
				validateAlternative(test_alternative);
				return true;
		}
		if (keycode == Keys.DOWN | keycode == Keys.RIGHT){
			if(SpriteData.isKnownId(id)){
				if(test_alternative < nb_alternatives ) test_alternative++;
				editKnownPixel(test_alternative);
				return true;
			}else {
				if(test_alternative < nb_alternatives ) test_alternative++;
				editUnknown(test_alternative);
				return true;
			}
		}
		if (keycode == Keys.UP | keycode == Keys.LEFT){
			if(SpriteData.isKnownId(id)){
				if(test_alternative > 1 ) test_alternative--;
				editKnownPixel(test_alternative);
				return true;
			}else {
				if(test_alternative > 1 ) test_alternative--;
				editUnknown(test_alternative);
				return true;
			}
		}		
		return false;
	}


	/**
	 * @param alternative
	 */
	private void validateAlternative(int alternative) {
			MapPixel pix = pixel_list.get(alternative);
			SpriteData.getIds().put(id, pix.getTex());
			logger.info("Validation du mapping : "+id+"=>"+pix.getTex());
			pixel_list = null;
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getPixelIndexFileUpdaterRunnable());
			dispose();
	}

	/**
	 * 
	 */
	private void dispose() {
		this.clear();
		alternative_group.clear();
		edited_group.clear();
		MapManager.close_edit_menu();
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}


	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}


	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}


	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
