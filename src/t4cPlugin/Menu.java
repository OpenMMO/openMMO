package t4cPlugin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

public class Menu {
	private TextButton nom;
	private TextButtonStyle style;
	protected OrthographicCamera camera;
	protected Stage stage;
	protected Group menu;
	//les coordonées spatiales sur l'ecran 
	//le rendu de cet UI
	protected SpriteBatch batch;
	//indique si on a chargé le contenu
	protected boolean isLoaded;

	public Menu(){
		this.isLoaded = false;
	}
	
	public Menu init(float viewX, float viewY, float x, float y){
		style = new TextButtonStyle();
		style.font = new BitmapFont();
		
		batch = new SpriteBatch();
		this.menu = new Group();
		
		this.stage = new Stage();
		this.stage.addActor(menu);
		nom = new TextButton("Menu", style);
		//mise en place de la camera
		setCamera(new OrthographicCamera(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f));
		setCameraPosition(x, y);
		stage.setCamera(camera);
		stage.addActor(nom);
		
		//conversion entre les points de vue
		float echelle_x = getViewX()/viewX;
		float echelle_y = getViewY()/viewY;
		
		nom.setX((camera.viewportWidth/100)*5);
		nom.setY((camera.viewportHeight/100)*5);
		
		this.isLoaded = true;
		return this;
	}
/*-------------------------------------ACCESSEURS--------------------------------*/
	/** Indique si on a deja chargé la fenetre */
	public boolean isLoaded(){
		return this.isLoaded;
	}
	
	/** Retourne la taille sur X de la zone de vue en fonction du zoom */
	protected float getZoomViewX(){
		return this.camera.viewportWidth*camera.zoom;
	}
	
	/** Retourne la taille sur Y de la zone de vue en fonction du zoom */
	protected float getZoomViewY(){
		return this.camera.viewportHeight*camera.zoom;
	}
	/** Retourne la taille sur X de la zone de vue sans le zoom */
	protected float getViewX(){
		return this.camera.viewportWidth;
	}
	
	/** Retourne la taille sur Y de la zone de vue sans le zoom */
	protected float getViewY(){
		return this.camera.viewportHeight;
	}
/*-------------------------------------SETTEURS--------------------------------*/
	protected void setCamera(OrthographicCamera camera){
		this.camera = camera;
	}
	protected void setCameraPosition(float x, float y){
		this.camera.position.x = x;
		this.camera.position.y = y;
	}
/*--------------------------------------FONCTIONS--------------------------------*/	
	public void render() {
		if (isLoaded){
			batch.setProjectionMatrix(camera.combined);
			camera.update();
			
			batch.begin();
				stage.draw();
			batch.end();
		}
	}
	
	/** Synchronize les zooms */
	public void synchronizeZoom(float zoom){
		this.camera.zoom = zoom;
	}
	
	/** Synchronize les position en prenant en compte les différences d'echelle sur le zoom */
	public void synchronizePos(float x, float y, float viewX, float viewY) {
		float new_x = (getZoomViewX()/viewX)*x;
		float new_y = (getZoomViewY()/viewY)*y;
		camera.position.set(new_x, new_y, 0);
	}

	public void dispose() {
		batch.dispose();
		stage.dispose();
	}
}
