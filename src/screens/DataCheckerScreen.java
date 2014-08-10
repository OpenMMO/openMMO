package screens;

import opent4c.UpdateDataCheckStatus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

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
	
	private TextButton sourcedataStatus;
	private TextButton mapsStatus;
	private TextButton dpdStatus;
	private TextButton didStatus;
	private TextButton ddaStatus;
	private TextButton spriteDataStatus;
	private TextButton atlasStatus;
	private TextButton status;

	public DataCheckerScreen(){
	}
	
	/**
	 * Called each frame for rendering
	 */
	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
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
		sourcedataStatus.setText("État des données sources : "+UpdateDataCheckStatus.getSourceDataStatus());
		mapsStatus.setText("État des cartes : "+UpdateDataCheckStatus.getMapsStatus());
		dpdStatus.setText("État du fichier DPD : "+UpdateDataCheckStatus.getDPDStatus());
		didStatus.setText("État du fichier DID : "+UpdateDataCheckStatus.getDIDStatus());
		ddaStatus.setText("État des fichiers DDA : "+UpdateDataCheckStatus.getDDAStatus());
		spriteDataStatus.setText("État de sprite_data : "+UpdateDataCheckStatus.getSpriteDataStatus());
		atlasStatus.setText("État des atlas : "+UpdateDataCheckStatus.getAtlasStatus());
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
		stage.setCamera(camera);
		style_normal.font = font;
		createButtons();
		alignButtons();
		positionButtons();
		addButtonsToStage();
		setButtonsAlpha(0f);
		fadeButtons(1f,3);
		Gdx.input.setInputProcessor(stage);
	}

	/**
	 * @param alpha
	 * @param time
	 */
	private void fadeButtons(float alpha, int time) {
		sourcedataStatus.addAction(Actions.alpha(alpha, time));
		mapsStatus.addAction(Actions.alpha(alpha, time));
		dpdStatus.addAction(Actions.alpha(alpha, time));
		didStatus.addAction(Actions.alpha(alpha, time));
		ddaStatus.addAction(Actions.alpha(alpha, time));
		spriteDataStatus.addAction(Actions.alpha(alpha, time));
		atlasStatus.addAction(Actions.alpha(alpha, time));		
		status.addAction(Actions.alpha(alpha, time));		
	}

	/**
	 * @param f
	 */
	private void setButtonsAlpha(float f) {
		sourcedataStatus.getColor().a = f;
		mapsStatus.getColor().a = f;
		dpdStatus.getColor().a = f;
		didStatus.getColor().a = f;
		ddaStatus.getColor().a = f;
		spriteDataStatus.getColor().a = f;
		atlasStatus.getColor().a = f;		
		status.getColor().a = f;		
	}

	/**
	 * 
	 */
	private void addButtonsToStage() {
		/*stage.addActor(sourcedataStatus);
		stage.addActor(mapsStatus);
		stage.addActor(dpdStatus);
		stage.addActor(didStatus);
		stage.addActor(ddaStatus);
		stage.addActor(spriteDataStatus);
		stage.addActor(atlasStatus);	*/	
		stage.addActor(status);		
	}

	/**
	 * 
	 */
	private void positionButtons() {
		sourcedataStatus.setPosition(Gdx.graphics.getWidth()/2-sourcedataStatus.getWidth()/2, 7*(Gdx.graphics.getHeight()/8));
		mapsStatus.setPosition(Gdx.graphics.getWidth()/2-mapsStatus.getWidth()/2, 6*(Gdx.graphics.getHeight()/8));
		dpdStatus.setPosition(Gdx.graphics.getWidth()/2-dpdStatus.getWidth()/2, 5*(Gdx.graphics.getHeight()/8));
		didStatus.setPosition(Gdx.graphics.getWidth()/2-didStatus.getWidth()/2, 4*(Gdx.graphics.getHeight()/8));
		ddaStatus.setPosition(Gdx.graphics.getWidth()/2-ddaStatus.getWidth()/2, 3*(Gdx.graphics.getHeight()/8));
		spriteDataStatus.setPosition(Gdx.graphics.getWidth()/2-spriteDataStatus.getWidth()/2, 2*(Gdx.graphics.getHeight()/8));
		atlasStatus.setPosition(Gdx.graphics.getWidth()/2-atlasStatus.getWidth()/2, 1*(Gdx.graphics.getHeight()/8));		
		status.setPosition(Gdx.graphics.getWidth()/2-atlasStatus.getWidth()/2, Gdx.graphics.getHeight()/2);		
	}

	/**
	 * 
	 */
	private void alignButtons() {
		sourcedataStatus.align(Align.left);
		mapsStatus.align(Align.left);
		dpdStatus.align(Align.left);
		didStatus.align(Align.left);
		ddaStatus.align(Align.left);
		spriteDataStatus.align(Align.left);
		atlasStatus.align(Align.left);		
		status.align(Align.left);		
	}

	/**
	 * 
	 */
	private void createButtons() {
		sourcedataStatus = new TextButton("DataCheckerScreen.show()", style_normal);
		mapsStatus = new TextButton("DataCheckerScreen.show()", style_normal);
		dpdStatus = new TextButton("DataCheckerScreen.show()", style_normal);
		didStatus = new TextButton("DataCheckerScreen.show()", style_normal);
		ddaStatus = new TextButton("DataCheckerScreen.show()", style_normal);
		spriteDataStatus = new TextButton("DataCheckerScreen.show()", style_normal);
		atlasStatus = new TextButton("DataCheckerScreen.show()", style_normal);		
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
