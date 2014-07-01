package opent4c;

import java.awt.Point;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import opent4c.utils.PointsManager;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;
import screens.MapManager;
import t4cPlugin.Places;

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
	private MapManager manager = null;
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
	private static final float movespeed = 1f;
	private boolean control_right = false;
	private boolean control_left = false;

	
	public InputManager(MapManager mapManager) {
		manager = mapManager;
		camera = MapManager.getCamera();
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
		if (keycode == Keys.ESCAPE){
			Gdx.app.exit();
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
				if (moveleft){
					int direction = 0;
					if(camera.position.x > Gdx.graphics.getWidth()/2) Gdx.app.postRunnable(RunnableCreatorUtil.getCameraMoverRunnable(camera, direction));					
				}else{
					
				}
			}
		};
		movingleft = ThreadsUtil.executePeriodicallyInThread(r, 0, movedelay_ms, TimeUnit.MILLISECONDS);
	}

	/**
	 * Starts moving right
	 */
	private void startMoveRight() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				if (moveright){
					int direction = 1;
					if(camera.position.x < (3072 * 32)-Gdx.graphics.getWidth()/2) Gdx.app.postRunnable(RunnableCreatorUtil.getCameraMoverRunnable(camera, direction));					
				}else{
					
				}
			}
		};
		movingright = ThreadsUtil.executePeriodicallyInThread(r, 0, movedelay_ms, TimeUnit.MILLISECONDS);
	}

	/**
	 * Starts moving up
	 */
	private void startMoveUp() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				if (moveup){
					int direction = 2;
					if(camera.position.y > Gdx.graphics.getHeight()/2) Gdx.app.postRunnable(RunnableCreatorUtil.getCameraMoverRunnable(camera, direction));					
				}else{
					
				}
			}
		};
		movingup = ThreadsUtil.executePeriodicallyInThread(r, 0, movedelay_ms, TimeUnit.MILLISECONDS);
	}

	/**
	 * Starts moving down
	 */
	private void startMoveDown() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				if (movedown){
					int direction = 3;
					if(camera.position.y < (3072 * 16)-1-Gdx.graphics.getHeight()/2) Gdx.app.postRunnable(RunnableCreatorUtil.getCameraMoverRunnable(camera, direction));					
				}else{
					
				}
			}
		};
		movingdown = ThreadsUtil.executePeriodicallyInThread(r, 0, movedelay_ms, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.LEFT){
			moveleft = false;
			stopMoveLeft();
		}
		if (keycode == Keys.RIGHT){
			moveright = false;
			stopMoveRight();
		}
		if (keycode == Keys.UP){
			moveup = false;
			stopMoveUp();
		}
		if (keycode == Keys.DOWN){
			movedown = false;
			stopMoveDown();
		}
		if (keycode == Keys.CONTROL_LEFT){
			control_left = false;
		}
		if (keycode == Keys.CONTROL_RIGHT){
			control_right = false;
		}
		if (keycode == Keys.F1){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("lh_temple")));
		}
		if (keycode == Keys.F2){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("wh_temple")));
		}
		if (keycode == Keys.F3){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("ss_temple")));
		}
		if (keycode == Keys.F4){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("sc_temple")));
		}
		if (keycode == Keys.F5){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("ar_rst")));
		}
		if (keycode == Keys.F6){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("rd_rst")));
		}
		if (keycode == Keys.F7){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("sh_rst")));
		}
		if (keycode == Keys.F8){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("ar_druides")));
		}
		if (keycode == Keys.F9){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("rd_druides")));
		}
		if (keycode == Keys.F10){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("sh_zo")));
		}
		if (keycode == Keys.F11){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("origin")));
		}
		if (keycode == Keys.F12){
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getChunkCreatorRunnable(Places.getPlace("center")));
		}
		if (keycode == Keys.ENTER){
			if(MapManager.isHighlighted()){
				manager.editMapAtCoord(PointsManager.getPoint(MapManager.getHighlight_tile().getX()/32,MapManager.getHighlight_tile().getY()/16));
			}
		}
		return true;
	}

	/**
	 * stops moving right
	 */
	private void stopMoveRight() {
		movingright.cancel(false);
		
	}

	/**
	 * stops moving up
	 */
	private void stopMoveUp() {
		movingup.cancel(false);
		
	}

	/**
	 * Stops moving down
	 */
	private void stopMoveDown() {
		movingdown.cancel(false);
	}

	/**
	 * Stops moving left
	 */
	private void stopMoveLeft() {
		movingleft.cancel(false);
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == Buttons.LEFT){
			mouseLeft = true;
			if(!MapManager.isHighlighted()){
				Point tuile_on_map = PointsManager.getPoint((int)((screenX+camera.position.x-camera.viewportWidth/2)/(32/camera.zoom)),(int)((screenY+camera.position.y-camera.viewportHeight/2)/(16/camera.zoom)));
				manager.highlight(tuile_on_map);
			}
		}
		if (button == Buttons.RIGHT){
			mouseRight = true;
			if (MapManager.isHighlighted()) MapManager.unHighlight();
		}
		if (button == Buttons.MIDDLE) mouseMiddle = true;
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (mouseLeft & button == Buttons.LEFT){
		}
		if (button == Buttons.LEFT){
			mouseLeft = false;
		}
		if (button == Buttons.RIGHT) {
			mouseRight = false;
		}
		if (button == Buttons.MIDDLE){
			mouseMiddle = false;
		}
		if (mouseMiddle & button == Buttons.MIDDLE){
		}
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (mouseRight)camera.translate(-Gdx.input.getDeltaX()*camera.zoom,-Gdx.input.getDeltaY()*camera.zoom);
		return true;
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
}
