/**
 * 
 */
package screens;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import opent4c.SpriteData;
import opent4c.utils.FilesPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

/**
 * @author synoga
 *
 */
public class IdEditMenu implements InputProcessor {
	private static Logger logger = LogManager.getLogger(IdEditMenu.class.getSimpleName());
	private int id;
	private TextButtonStyle style = new TextButtonStyle();

	/**
	 * @param point
	 * @param id
	 */
	public IdEditMenu(Point point, int id) {
		this.id = id;
		String tex;
		style.font = new BitmapFont();
		if(SpriteData.getIds().containsKey(id)){
			tex = SpriteData.getIds().get(id);
		}else{
			tex = "non mappée";
		}
		System.out.println("##########################################################");
		System.out.println("Édition d'un ID : "+id);
		System.out.println("Actuellement mappé sur : "+tex);
		System.out.println("Appuyer sur Entrée pour éditer ou sur Échap pour annuler.");
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.InputProcessor#keyDown(int)
	 */
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.InputProcessor#keyUp(int)
	 */
	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Keys.ENTER){
			System.out.println("##########################################################");
			System.out.println("Entrez un nouveau nom :");
			System.out.println();
			String input = readInputName();
			System.out.println("Mise à jour de id.txt");
			updateIdFile(id, input);
			System.out.println("##########################################################");
			exit();
		}
		if(keycode == Keys.ESCAPE){
			System.out.println("##########################################################");
			System.out.println("Annuler.");
			System.out.println("##########################################################");
			exit();
		}
		return false;
	}

	/**
	 * 
	 */
	private void exit() {
		MapManager.close_edit_menu();
		
	}

	/**
	 * @return
	 */
	private String readInputName() {
		String result = "null";
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    try {
			result = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		return result;
	}

	/**
	 * @param id2
	 * @param input
	 */
	private void updateIdFile(int id, String input) {
		SpriteData.getIds().put(id, input);
		SpriteData.writeIdsToFile();
		new File(FilesPath.getPixelIndexFilePath()).deleteOnExit();
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.InputProcessor#keyTyped(char)
	 */
	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.InputProcessor#touchDown(int, int, int, int)
	 */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.InputProcessor#touchUp(int, int, int, int)
	 */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.InputProcessor#touchDragged(int, int, int)
	 */
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.InputProcessor#mouseMoved(int, int)
	 */
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.InputProcessor#scrolled(int)
	 */
	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
