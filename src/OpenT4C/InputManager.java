package OpenT4C;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import t4cPlugin.utils.ThreadsUtil;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;

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
	private int movedelay = 300;
	private boolean control_right = false;
	private boolean control_left = false;

	
	public InputManager(MapManager mapManager) {
		manager = mapManager;
		camera = manager.getCamera();
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
		if (keycode == Keys.A){
			if(control_left || control_right){
				manager.editNextUnmappedID();
			}
		}
		return true;
	}

	private void startMoveLeft() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				if (moveleft){
					camera.translate(-0.02f,0);					
				}else{
					
				}
			}
		};
		movingleft = ThreadsUtil.executePeriodicallyInThread(r, 0, movedelay, TimeUnit.MICROSECONDS);
	}

	private void startMoveRight() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				if (moveright){
					camera.translate(0.02f,0);					
				}else{
					
				}
			}
		};
		movingright = ThreadsUtil.executePeriodicallyInThread(r, 0, movedelay, TimeUnit.MICROSECONDS);
	}

	private void startMoveUp() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				if (moveup){
					camera.translate(0,-0.01f);					
				}else{
					
				}
			}
		};
		movingup = ThreadsUtil.executePeriodicallyInThread(r, 0, movedelay, TimeUnit.MICROSECONDS);
	}

	private void startMoveDown() {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				if (movedown){
					camera.translate(0,0.01f);					
				}else{
					
				}
			}
		};
		movingdown = ThreadsUtil.executePeriodicallyInThread(r, 0, movedelay, TimeUnit.MICROSECONDS);
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
		return true;
	}

	private void stopMoveRight() {
		movingright.cancel(true);
		
	}

	private void stopMoveUp() {
		movingup.cancel(true);
		
	}

	private void stopMoveDown() {
		movingdown.cancel(true);
		
	}

	private void stopMoveLeft() {
		movingleft.cancel(true);
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == Buttons.LEFT){
			mouseLeft = true;
			manager.pop_menu(screenX, screenY);
		}
		if (button == Buttons.RIGHT) mouseRight = true;
		if (button == Buttons.MIDDLE) mouseMiddle = true;
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (mouseLeft & button == Buttons.LEFT){
			manager.clearMenu();
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
