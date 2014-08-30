package screens;

import org.lwjgl.opengl.GL30;

import opent4c.UpdateDataCheckStatus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * This is the screen seen while checking and loading data
 * @author synoga
 *
 */
public class DataCheckerScreen implements Screen{
	private SpriteBatch batch;
	private BitmapFont font;
	private OrthographicCamera camera;
	private Stage stage;
	
	final TextButtonStyle style_normal = new TextButtonStyle();
	
	private TextButton status;

	public DataCheckerScreen(){
	}
	
	/**
	 * Called each frame for rendering
	 */
	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		updateInfos();
		camera.update();
		stage.act(delta);
		
		batch.begin();
			stage.draw();
		batch.end();		
	}

	/**
	 * Updates infos on the screen
	 */
	private void updateInfos() {
		setButtonsText();
		Gdx.app.getGraphics().setTitle("OpenT4C v0 " + " FPS: " + Gdx.graphics.getFramesPerSecond() + " RAM : " + getMemoryUsage());
	}
	
	/**
	 * 
	 */
	private void setButtonsText() {
		status.setText(UpdateDataCheckStatus.getStatus());
	}

	@Override
	public void resize(int width, int height) {
	}

	/**
	 * @return the amount of memory used by the program
	 */
	private String getMemoryUsage(){
		return ((Runtime.getRuntime().totalMemory())/1024/1024) + " Mo";
	}
	
	@Override
	public void show() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();
		stage = new Stage();
		stage.setViewport(new ScreenViewport(camera));
		style_normal.font = font;
		createButtons();
		alignButtons();
		positionButtons();
		addButtonsToStage();
		setButtonsAlpha(0f);
		fadeButtons(1f,3);
		//Gdx.input.setInputProcessor(stage);
	}

	/**
	 * @param alpha
	 * @param time
	 */
	private void fadeButtons(float alpha, int time) {
		status.addAction(Actions.alpha(alpha, time));		
	}

	/**
	 * @param f
	 */
	private void setButtonsAlpha(float f) {
		status.getColor().a = f;		
	}

	/**
	 * 
	 */
	private void addButtonsToStage() {
		stage.addActor(status);		
	}

	/**
	 * 
	 */
	private void positionButtons() {
		status.setPosition(Gdx.graphics.getWidth()/2-status.getWidth()/2, Gdx.graphics.getHeight()/2);		
	}

	/**
	 * 
	 */
	private void alignButtons() {
		status.align(Align.left);		
	}

	/**
	 * 
	 */
	private void createButtons() {
		status = new TextButton("DataCheckerScreen.show()", style_normal);		
	}

	@Override
	public void hide() {
		fadeButtons(0,1);
		dispose();
	}

	@Override
	public void pause() {
		stage.addAction(Actions.alpha(0.3f, 1));
	}

	@Override
	public void resume() {
		stage.addAction(Actions.alpha(1f, 1));
	}

	@Override
	public void dispose() {
		batch.dispose();
		stage.dispose();
		font.dispose();
	}

}
