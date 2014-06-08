package OpenT4C;

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

public class DataCheckerScreen implements Screen{
	private SpriteBatch batch;
	private BitmapFont font;
	private OrthographicCamera camera;
	private Stage stage;
	private TextButton infos;
	private String status = "Initialisation";

	public DataCheckerScreen(ScreenManager screenManager){
		batch = new SpriteBatch();
		font = new BitmapFont();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();
		stage = new Stage();
		stage.setCamera(camera);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		setStatus();
		updateInfos();
		camera.update();
		stage.act(delta);
		
		batch.begin();
			stage.draw();
		batch.end();		
	}

	private void updateInfos() {
		infos.setText(status);
		Gdx.app.getGraphics().setTitle("OpenT4C v0.0 "+status);
	}

	private void setStatus(){
		status = "Satus : " + UpdateScreenManagerStatus.getReadableStatus() + " FPS: " + Gdx.graphics.getFramesPerSecond() + " RAM : " + getMemoryUsage();
	}
	
	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	
	private String getMemoryUsage(){
		return ((Runtime.getRuntime().totalMemory())/1024/1024) + " Mo";
	}
	
	@Override
	public void show() {
		final TextButtonStyle style_normal = new TextButtonStyle();
		style_normal.font = font;
		infos = new TextButton("DataCheckerScreen.show()", style_normal);
		infos.setX(Gdx.graphics.getWidth()/3);
		infos.setY(Gdx.graphics.getHeight()/2);
		stage.addActor(infos);
		infos.getColor().a = 0f;
		infos.addAction(Actions.alpha(1f, 3));
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		dispose();		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		batch.dispose();
		stage.dispose();
		font.dispose();
	}

}
