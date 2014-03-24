package t4cPlugin;

import java.awt.Point;
import java.io.File;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTile.BlendMode;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

public class GdxMap implements Screen, InputProcessor{

	private boolean loaded = false;
	private boolean render_tuiles = true;
	private boolean render_sprites = true;
	private boolean render_infos = true;
	private boolean mouseLeft = false;
	private boolean mouseRight = false;
	private boolean mouseMiddle = false;
	private boolean zooming = false;
	private boolean moving = false;

	private MapFile map;
	
	private TiledMap tile_map;
	private TiledMapRenderer tile_renderer;
	private MapLayers tile_layers;
	private TiledMapTileLayer tile_layer;
	
	private TextButtonStyle style = new TextButtonStyle();
	private TextButton load;
	private TextButton status;
	private TextButton fps;
	private TextButton info;

	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Stage stage, ui;
	private Group menu, sprites, infos;

	private int nb_sprites = 0;
	private int nb_tuiles = 0;

	private Lieux lh_temple= new Lieux("LH TEMPLE", "v2_worldmap",new Point(2954,1052));
	private boolean debug = true;
	
	public GdxMap(ScreenManager sm) {
		tile_map = new TiledMap();
		tile_layers = tile_map.getLayers();
		tile_layer = new TiledMapTileLayer(3072, 3072, 32, 16);
	}

	public void load(){
		while(!AssetsLoader.loadsols){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		Gdx.app.postRunnable(new Runnable(){
			public void run(){
				style.font = new BitmapFont();
				stage = new Stage();
				ui = new Stage();
				menu = new Group();
				sprites = new Group();
				infos = new Group();
				batch = new SpriteBatch();
				camera = new OrthographicCamera();
				camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				camera.update();
				stage.setCamera(camera);
				ui.addActor(menu);
				ui.addActor(infos);
				setLoadInfos();
			}
		});
		Gdx.input.setInputProcessor(this);
	}
	
	public void readPIXMLMAP(String file) {
		Params.STATUS = "Chargement de la carte "+file+".map.decrypt.bin";
		map = new MapFile(new File("."+File.separator+"data"+File.separator+file+".map.decrypt.bin"));
		map = map.read();
		Params.STATUS = file+".map.decrypt.bin lue, analyse des cases.";
		TextureRegion texRegion = null;
		TextureAtlas texAtlas = null;
		StaticTiledMapTile tile = null;
		Sprite sprite = null;
		for (int y = 0 ; y< 3072 ; y++){
			for (int x = 0 ; x< 3072 ; x++){
				MapPixel px = map.pixels.get(new Point(x,y));
				if(px.atlas.equals("foo") & px.tex.equals("bar")){//Case inconnue
					//System.err.println("Case Inconnue : "+px.id+"@"+x+","+y);
					nb_tuiles++;
				}else{//Case connue
					if(px.tuile){//tuile
						//System.out.println(px.tex);
						texRegion = map.getCell(new Point(x,y));
						Cell cell = new Cell();
						if (texRegion == null){
							System.exit(1);
						}
						tile = new StaticTiledMapTile(texRegion);
						tile.setBlendMode(BlendMode.NONE);
						cell.setTile(tile);
						cell.setFlipVertically(true);
						tile_layer.setCell(x, y, cell);
						nb_tuiles++;
					}else{//sprite
						//Data.pixels.put(new Dimension(x,y), Data.idlist.get(px.id));
						texAtlas = AssetsLoader.sprite_atlases.get(px.atlas);
						//Si on trouve l'atlas
						if (texAtlas != null){
							texRegion = texAtlas.findRegion(px.tex);
							//Si on ne trouve pas le sprite
							if (texRegion == null) {
								System.err.println("Sprite non trouvé : "+px.atlas+"|"+px.tex);//on affiche un message d'erreur
							}
							//Si on trouve le sprite
							else{
								sprite = new Sprite(texRegion);
								sprite.flip(false, true);
								Data.cases_sprites.put(new Point(x,y), sprite);
								nb_sprites++;
								//System.err.println("SPRITE");
							}
						}
						//Si on ne trouve pas l'atlas de sprite
						else{
							//System.err.println("Atlas de sprite non trouvé : "+px.atlas+"|"+px.tex+" "+px.tuile);//On affiche un message d'erreur
							final String at = px.atlas;
							final String tx = px.tex;
							final Point c = new Point(x,y);
							texAtlas = AssetsLoader.load(at);
							texRegion = texAtlas.findRegion(tx);
							sprite = new Sprite(texRegion);
							sprite.flip(false, true);
							Data.cases_sprites.put(c, sprite);
							nb_sprites++;
						}
					}
				}
				Params.STATUS = "Chargement : "+((nb_sprites+nb_tuiles)*100)/(3072*3072)+"%"+" Cases restantes : "+ ((3072*3072)-(nb_sprites+nb_tuiles));
			}
		}
		Params.STATUS = "Chargement terminé.";
		tile_layer.setOpacity(1f);
		tile_layers.add(tile_layer);
		Gdx.app.postRunnable(new Runnable(){
			public void run(){
				tile_renderer = new OrthogonalTiledMapRenderer(tile_map);
			}
		});
		if (render_sprites)	stage.addActor(sprites);
		loaded = true;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		//La caméra
		render_camera();
		stage.act(delta);
		ui.act(delta);
		//Les tuiles
		if(render_tuiles & loaded) render_tuiles();
		batch.begin();
			//Les Sprites
			if(render_sprites & loaded) getSpritesInView();
			stage.draw();
		batch.end();
		batch.begin();
			//Les infos
			if(render_infos){
				render_infos();
				ui.draw();	
			}
		batch.end();
		
	}

	private void getSpritesInView() {
		sprites.clear();
		Point d = new Point(0,0);
		for (int y = (int) ((camera.position.y/16)-(25+(camera.zoom * camera.viewportHeight)/32)) ; y<(int) ((camera.position.y/16)+(25+(camera.zoom * camera.viewportHeight)/32)) ; y++){
			for (int x = (int) ((camera.position.x/32)-(25+(camera.zoom * camera.viewportWidth)/64)) ; x<(int) ((camera.position.x/32)+(25+(camera.zoom * camera.viewportWidth)/64)) ; x++){
				d = new Point(x,y);
				if (Data.cases_sprites.get(d) != null){
					Sprite sp = Data.cases_sprites.get(d);
					float spx = (sp.getScaleX()*map.pixels.get(d).offset.x)+(x*32);
					float spy = (sp.getScaleY()*map.pixels.get(d).offset.y)+(y*16);
					sp.setPosition(spx, spy);
					sprites.addActor(new Acteur(sp));
				}
			}
		}
	}
	private void setLoadInfos(){
		load = new TextButton(Params.STATUS, style);
		load.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		status = new TextButton("", style);
		info = new TextButton("", style);
		fps = new TextButton("", style);
		if (debug){
			fps = new TextButton(Gdx.graphics.getFramesPerSecond()+" fps", style);
		}
		fps.setPosition(Gdx.graphics.getWidth()-50, Gdx.graphics.getHeight()-30);
		infos.addActor(fps);
		infos.addActor(load);
		infos.addActor(status);
		infos.addActor(info);
	}
	
	private void render_infos() {
		if(!loaded){
			load.setText(Params.STATUS);
			if(debug)fps.setText(Gdx.graphics.getFramesPerSecond()+" fps");
			Gdx.app.getGraphics().setTitle("T4C Map Viewer 0.1a (by Syno) FPS: " + Gdx.graphics.getFramesPerSecond() + " RAM : " + ((Runtime.getRuntime().totalMemory())/1024/1024) + " Mo");
		}else{
			Gdx.app.getGraphics().setTitle("T4C Map Viewer 0.1a (by Syno) FPS: " + Gdx.graphics.getFramesPerSecond() + " RAM : " + ((Runtime.getRuntime().totalMemory())/1024/1024) + " Mo");
			load.setText("");
			status.setText(Params.STATUS);
			status.setPosition(200,20);
			if(debug)fps.setText(""+Gdx.graphics.getFramesPerSecond()+" fps");
			info.setText("X: " + (((int)camera.position.x/32)) + " Y: " + (((int)camera.position.y/16)) + " Zoom : " + camera.zoom);
			info.setPosition(100, camera.viewportHeight - 20);
		}
	}

	private void render_tuiles() {
		tile_renderer.setView(camera);
		tile_renderer.render();		
	}

	private void render_camera() {
		camera.update();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
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
		tile_map.dispose();
		stage.dispose();
		ui.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
        return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.F1){
			render_tuiles = toggle(render_tuiles);
			System.out.println("TOGGLE TUILES : "+render_tuiles);
		}
		if (keycode == Keys.F2){
			render_sprites = toggle(render_sprites);
			System.out.println("TOGGLE SPRITES : "+render_sprites);
		}
		if (keycode == Keys.F3){
			Point dest = new Point(lh_temple.getCoord().x, lh_temple.getCoord().y);
			if (!moving)move(dest);
			System.out.println("Goto : "+lh_temple.getNom());
		}
		if (keycode == Keys.SPACE){
			tools.HeapDumper.dumpHeap("."+File.separator+"data"+File.separator+"heap"+File.separator+"heap.bin", true);
		}
		return true;
	}

	private boolean toggle(boolean b) {
		boolean result = false;
		if (b == false) result = true;
		if (b == true) result = false;
		return result;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == Buttons.LEFT){
			mouseLeft = true;
			pop_menu(screenX, screenY);
		}
		if (button == Buttons.RIGHT) mouseRight = true;
		if (button == Buttons.MIDDLE) mouseMiddle = true;
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (mouseLeft & button == Buttons.LEFT){
			menu.clear();
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
			resetZoom();
		}
		return true;
	}

	private void resetZoom() {
		moving = false;
		zooming = false;
		camera.zoom = 1;
	}

	private void pop_menu(int screenX, int screenY) {
		//On récupère les coordonnées T4C du point cliqué
		Point p = new Point((int)((screenX+camera.position.x-camera.viewportWidth/2)/(32/camera.zoom)),(int)((screenY+camera.position.y-camera.viewportHeight/2)/(16/camera.zoom)));
		if (map.pixels.containsKey(p)){
			MapPixel px = map.pixels.get(p);	
			System.out.println(p.x +","+ p.y +" "+px.atlas+" "+px.tex+" id : "+px.id+" Modulo : "+px.modulo.x+","+px.modulo.y);
			Params.STATUS = p.x +","+ p.y +" "+px.atlas+" "+px.tex+" id : "+px.id+" Modulo : "+px.modulo.x+","+px.modulo.y;
			if(px.tuile){

				TextButton pixel_info0 = new TextButton(p.x +","+ p.y,style);
				TextButton pixel_info1 = new TextButton(px.atlas+" "+px.tex,style);
				TextButton pixel_info2 = new TextButton("id : "+px.id+" Modulo : "+px.modulo.x+","+px.modulo.y,style);
				pixel_info0.setPosition(screenX+10,(int)(camera.viewportHeight-screenY+5));
				pixel_info1.setPosition(screenX+10,(int)(camera.viewportHeight-screenY+25));
				pixel_info2.setPosition(screenX+10,(int)(camera.viewportHeight-screenY+45));

				TextureRegion texRegion = map.getCell(p);
				Sprite sp = new Sprite(texRegion);
				sp.setPosition(screenX-16+pixel_info1.getWidth()/2,(int)(camera.viewportHeight-screenY+73));
				
				menu.addActor(new IG_Menu(screenX,(int) camera.viewportHeight-screenY,(int) (pixel_info1.getWidth()+20),100));
				menu.addActor(pixel_info0);
				menu.addActor(pixel_info1);
				menu.addActor(pixel_info2);
				menu.addActor(new Acteur(sp));
				
			}else{
				if(!px.atlas.equals("foo")){
					
					Sprite sp = new Sprite(AssetsLoader.sprite_atlases.get(px.atlas).findRegion(px.tex));
					sp.setPosition(screenX,(int)(camera.viewportHeight-screenY));
					
					menu.addActor(new IG_Menu(screenX,(int)camera.viewportHeight-screenY,(int) sp.getWidth(), (int) sp.getHeight()));

					menu.addActor(new Acteur(sp));
					
				}else{
					
					TextButton pixel_info0 = new TextButton(p.x +","+ p.y+" : ID "+px.id+" inconnu",style);
					pixel_info0.setPosition(screenX+10,(int)(camera.viewportHeight-screenY+5));
					menu.addActor(new IG_Menu(screenX,(int)camera.viewportHeight-screenY,(int) (pixel_info0.getWidth()+20),(int) (pixel_info0.getHeight()+10)));
					menu.addActor(pixel_info0);
					
				}
			}
		}

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
		if (!zooming){
			zoom((float) (camera.zoom + (0.3f*amount)));
		}
		return true;
	}
	
	public void move(Point dest){
		moving = true;
		//on commence par borner la destination à la carte
        if (dest.x > 3072) dest.x = 3072;
    	if (dest.x < 0) dest.x = 0;
        if (dest.y > 3072) dest.y = 3072;
    	if (dest.y < 0) dest.y = 0;
		final Point pos_final = dest;
		//Ensuite on détermine le sens dans lequel on va devoir se déplacer sur x et sur y
		float sx = 1;
		float sy = 1;
		if (camera.position.x > pos_final.x*32) sx = -1f;
		if (camera.position.y > pos_final.y*16) sy = -1f;
		final float sensX = sx;
		final float sensY = sy;
		//On détermine le pas de déplacement
		final float stepx = 32;
		final float stepy = 16;
		//On stocke me zoom initial de la camera pour pouvoir y revenir
		final float init_zoom = camera.zoom;
		Thread move = new Thread("MOVE"){
			public void run(){
				//on met la camera à un zoom de 4
				zoom(4);
				//Tant qu'on est à 10 pas de la destination et qu'on a pas demandé à arrêter de bouger.
				while ((Math.abs(camera.position.x-(pos_final.x*32)))>=(100 * stepx) | (Math.abs(camera.position.y-(pos_final.y*16)))>=(100 * stepy) & moving){
					Gdx.app.postRunnable(new Runnable(){
						public void run(){
							if(Math.abs(camera.position.x-pos_final.x*32)>=stepx){
								camera.translate(sensX*stepx, 0);
							}
							if(Math.abs(camera.position.y-pos_final.y*16)>=stepy){
								camera.translate(0, sensY*stepy);
							}
						}
					});
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
				//On est suffisamment proches, donc on retourne au zoom initial
				zoom(init_zoom);

				//Tant qu'on est pas arrivés et qu'on a pas demandé à arrêter de bouger
				while ((Math.abs(camera.position.x-(pos_final.x*32)))>=stepx|(Math.abs(camera.position.y-(pos_final.y*16)))>=stepy & moving){
					Gdx.app.postRunnable(new Runnable(){
						public void run(){
							if(Math.abs(camera.position.x-pos_final.x*32)>=stepx){
								camera.translate(sensX*stepx, 0);
							}
							if(Math.abs(camera.position.y-pos_final.y*16)>=stepy){
								camera.translate(0, sensY*stepy);
							}
						}
					});
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
				
				//On se trouve à moins d'un pas de distance de la destination
				Gdx.app.postRunnable(new Runnable(){
					public void run(){
						camera.position.x = pos_final.x*32;
						camera.position.y = pos_final.y*16;
						moving = false;
					}
				});

			}
		};
		move.start();
	}
	
	public void zoom(float dest){
		if (dest == camera.zoom)return;
		zooming = true;
        if (dest > 10) dest = 10;
    	if (dest < 0.4f) dest = 0.4f;
		final float zoom_final = dest;
		Thread zoom = new Thread("ZOOM"){
			public void run(){
				float sens = 1;
				if (camera.zoom > zoom_final) sens = -1;
				final float s = sens;
				final float dest = zoom_final;
				final float step = s*0.01f;
				while (Math.abs(camera.zoom-dest)>=0.01f & zooming){
					Gdx.app.postRunnable(new Runnable(){
						public void run(){
							camera.zoom += step;
							if (camera.zoom > 10) camera.zoom = 10;
							if (camera.zoom < 0.4f) camera.zoom = 0.4f;
						}
					});
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
				Gdx.app.postRunnable(new Runnable(){
					public void run(){
						camera.zoom = dest;
						zooming = false;
					}
				});
			}
		};
		zoom.start();
	}
}
