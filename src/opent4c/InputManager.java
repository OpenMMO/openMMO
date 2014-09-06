package opent4c;

import java.awt.Point;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opent4c.utils.Place;
import opent4c.utils.PointsManager;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;
import screens.GameScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Manages input, keyboard, mouse, touchscreen.
 * @author synoga
 *
 */
public class InputManager implements InputProcessor{
	private static Logger logger = LogManager.getLogger(InputManager.class.getSimpleName());

	private GameScreen manager = null;
	private boolean mouseLeft = false;
	private boolean mouseRight = false;
	private boolean mouseMiddle = false;
	private boolean moveleft = false;
	private boolean moveright = false;
	private boolean moveup = false;
	private boolean movedown = false;
	private ScheduledFuture<?> movingleft;
	private ScheduledFuture<?> movingright;
	private ScheduledFuture<?> movingup;
	private ScheduledFuture<?> movingdown;
	private OrthographicCamera camera = null;
	private final int movedelay_ms = 16;//un tout petit peu plus rapide que 60Hz
	private static final float movespeed = 2f;
	private boolean control_right = false;
	private boolean control_left = false;

	
	public InputManager(GameScreen mapManager) {
		manager = mapManager;
		camera = GameScreen.getCamera();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.LEFT){
			moveleft = true;
			startMoveLeft();
		}
		if (keycode == Keys.RIGHT){
			moveright = true;
			startMoveRight();
		}
		if (keycode == Keys.UP){
			moveup = true;
			startMoveUp();
		}
		if (keycode == Keys.DOWN){
			movedown = true;
			startMoveDown();
		}
		if (keycode == Keys.CONTROL_LEFT){
			control_left  = true;
		}
		if (keycode == Keys.CONTROL_RIGHT){
			control_right = true;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.LEFT){
			moveleft = false;
		}
		if (keycode == Keys.RIGHT){
			moveright = false;
		}
		if (keycode == Keys.UP){
			moveup = false;
		}
		if (keycode == Keys.DOWN){
			movedown = false;
		}
		if (keycode == Keys.CONTROL_LEFT){
			control_left = false;
		}
		if (keycode == Keys.CONTROL_RIGHT){
			control_right = false;
		}
		if (keycode == Keys.F1){
			teleport(Place.getPlace("lh_temple"));
		}
		if (keycode == Keys.F2){
			teleport(Place.getPlace("wh_temple"));
		}
		if (keycode == Keys.F3){
			teleport(Place.getPlace("ss_temple"));
		}
		if (keycode == Keys.F4){
			teleport(Place.getPlace("sc_temple"));
		}
		if (keycode == Keys.F5){
			teleport(Place.getPlace("ar_rst"));
		}
		if (keycode == Keys.F6){
			teleport(Place.getPlace("rd_rst"));
		}
		if (keycode == Keys.F7){
			teleport(Place.getPlace("sh_rst"));
		}
		if (keycode == Keys.F8){
			teleport(Place.getPlace("cavernmap"));
		}
		if (keycode == Keys.F9){
			teleport(Place.getPlace("dungeonmap"));
		}
		if (keycode == Keys.F10){
			teleport(Place.getPlace("underworld"));
		}
		if (keycode == Keys.F11){
			teleport(Place.getPlace("leoworld"));
		}
		if (keycode == Keys.F12){
			teleport(Place.getPlace("worldmap"));
		}
		if (keycode == Keys.ENTER){
			if(GameScreen.isHighlighted()){
				manager.editMapAtCoord(PointsManager.getPoint(GameScreen.getHighlight_tile().getX()/32,GameScreen.getHighlight_tile().getY()/16));
			}
		}
		if (keycode == Keys.ESCAPE){
			Gdx.app.exit();
		}
		if (keycode == Keys.SPACE){
			if(!(control_left||control_right)){
				GameScreen.toggleRenderSprites();
				GameScreen.toggleRenderDebug();
				GameScreen.toggleRenderSmootinhg();
			}else{
				GameScreen.toggleRenderDebug();
			}
			
		}
		return true;
	}

	/**
	 * Starts moving left
	 */
	private void startMoveLeft() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				while(moveleft){
					moveCam(4);
					sleep(movedelay_ms);
				}
			}
		};
		ThreadsUtil.executeInThread(r);
	}

	/**
	 * Starts moving right
	 */
	private void startMoveRight() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				while(moveright){
					moveCam(6);
					sleep(movedelay_ms);
				}
			}
		};
		ThreadsUtil.executeInThread(r);
	}

	/**
	 * Starts moving up
	 */
	private void startMoveUp() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				while(moveup){
					moveCam(8);
					sleep(movedelay_ms);
				}
			}
		};
		ThreadsUtil.executeInThread(r);
	}

	/**
	 * Starts moving down
	 */
	private void startMoveDown() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				while(movedown){
					moveCam(2);
					sleep(movedelay_ms);
				}
			}
		};
		ThreadsUtil.executeInThread(r);
	}
	
	/**
	 * @param time
	 */
	protected void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
	}

	/**
	 * teleports player in a new Place (and a nexwThread)
	 * @param place
	 */
	private void teleport(Place place) {
		GameScreen.teleport(place);
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == Buttons.LEFT){
			mouseLeft = true;

		}
		if (button == Buttons.RIGHT){
			mouseRight = true;
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (button == Buttons.LEFT){
			if(!GameScreen.isHighlighted()){
				Point tuile_on_map = PointsManager.getPoint((int)((screenX+camera.position.x-camera.viewportWidth/2)/(32/camera.zoom)),(int)((screenY+camera.position.y-camera.viewportHeight/2)/(16/camera.zoom)));
				GameScreen.highlight(tuile_on_map);
			}else{
				GameScreen.unHighlight();
			}
			mouseLeft = false;
		}
		if (button == Buttons.RIGHT) {
			mouseRight = false;
		}
		if (button == Buttons.MIDDLE){
			mouseMiddle = false;
		}
		return true;
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

	public static float getMovespeed() {
		return movespeed;
	}
	
	public void moveCam(int direction){
		switch(direction){
		case 4 : camera.translate(-2*InputManager.getMovespeed(),0); break;
		case 6 : camera.translate(2*InputManager.getMovespeed(),0); break;
		case 8 : camera.translate(0,-InputManager.getMovespeed()); break;
		case 2 : camera.translate(0,InputManager.getMovespeed()); break;
		}
	}
}
