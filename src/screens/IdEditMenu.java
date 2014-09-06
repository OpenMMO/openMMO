/**
 * 
 */
package screens;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import opent4c.MapPixel;
import opent4c.PixelIndex;
import opent4c.utils.FileLister;
import opent4c.utils.FilesPath;
import opent4c.utils.ID;
import opent4c.utils.Place;
import opent4c.utils.PointsManager;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

/**
 * @author synoga
 *
 */
public class IdEditMenu implements Screen, InputProcessor{
	private static Logger logger = LogManager.getLogger(IdEditMenu.class.getSimpleName());
	private static TextButtonStyle style = new TextButtonStyle();
	private static int CONSOLE_LINES = 35;
	private static List<TextButton> consoleTexts = new ArrayList<TextButton>(CONSOLE_LINES );
	private static TextButton input;
	private static int id;
	private static Point point;
	private static String tex;
	private static String atlas;
	private static boolean MIRROR = false;
	private static int historique = 0;
	private static int index = 0;
	private static Stage stage;
	private static SpriteBatch batch;
	private static String inputText = "";
	private static boolean validate = false;
	private static boolean editing = false;
	private static boolean shift = false;
	private static MapPixel px = null;

	/**
	 * Opens GUI.
	 * @param point
	 */
	public IdEditMenu(Point point) {
		unValidate();
		getEditInfos(point);
	}

	/**
	 * Asks for a command input on console.
	 */
	public static void showConsoleMainCommand() {
		printCommands();
		printSepLine();
		printIdInfos();
		MapPixel px = PixelIndex.getPixelFromIdAndPoint(id, point);
		if(px != null)printPixelInfos(px);
		print("Choisir une commande :");
		int cmd = readIntFromConsole(true, 0);
		unValidate();
		execNormalCommand(cmd);
	}

	/**
	 * Runs a main command.
	 * @param cmd
	 */
	public static void execNormalCommand(int cmd) {
		unValidate();
		if (cmd <= 0)exit();
		if (cmd == 1)editID(id);
		if (cmd == 2)editMirror();
		if (cmd == 3)markWrongPalette();
		if (cmd == 4)searchTex();
		if (cmd == 5)editSmoothing();
		if (cmd == 6)editOffsets();
		if (cmd == 7)editSpecificID();
		if (cmd == 8)editIdsOnMap();
		if (cmd == 9)teleport();
		if (cmd == 42)showHiddenMenu();		
	}

	/**
	 * Runs a command while editing ids.
	 * @param cmd
	 */
	private static void execEditIdsCommand(int cmd) {
		unValidate();
		if (cmd <= 0)return;
		if (cmd == 1)editID(id);
		if (cmd == 2)editMirror();
		if (cmd == 3)markWrongPalette();
		if (cmd == 4)searchTex();
		if (cmd == 5)editSmoothing();
		if (cmd == 6)editOffsets();
		if (cmd == 7)return;
		if (cmd == 42)showHiddenMenu();		
	}
	
	/**
	 * Exits edit menu
	 */
	public static void exit() {
		printSepLine();
		print("Au revoir Dave.");
		GameScreen.close_edit_menu();
	}
	
	/**
	 * 1 - Edits an ID;
	 */
	public static void editID(int id){
		printSepLine();
		printIdInfos();
		MapPixel px = PixelIndex.getPixelFromIdAndPoint(id, point);
		if(px != null){
			printPixelInfos(px);
		}
		printSepLine();
		print("Entrez un nouveau nom d'atlas. (Entrée pour laisser tel quel)");
		String input_atlas = readStringFromConsole();
		unValidate();
		if(input_atlas.equals("")){
			input_atlas = ID.getAtlasFromId(id);
		}
		printSepLine();
		print("Entrez un nouveau nom de texture. (Entrée pour laisser tel quel)");
		String input_tex = readStringFromConsole();
		unValidate();
		if(input_tex.equals("")){
			input_tex = ID.getTexFromId(id);
		}
		String input = input_atlas+":"+input_tex;
		printSepLine();
		printIdInfos();
		print("Modifier en - ID : "+id+" Dossier : "+input_atlas+" Nom : "+input_tex);
		if(askYesNo()){
			updateIdFile(id, input);
			updatePixelIndex();
		}else{
			print("Annuler");
		}
		if(!editing ) showConsoleMainCommand();
	}

	/**
	 * 2 - Edits a Mirror
	 */
	private static void editMirror() {
		printSepLine();
		printIdInfos();
		print("Marquer cet ID comme étant un mirroir.");
		print("Entrer l'ID d'origine.");
		int input_id = readIntFromConsole(true, -1);
		unValidate();
		if(input_id != -1){
			printSepLine();
			printIdInfos();
			print("Modifier en - ID : "+id+" Type : MIRROR ID : "+input_id);
			if(askYesNo()){
				updateIdFile(id, "MIRROR:"+input_id);
				updatePixelIndex();
			}
		}else{
			print("Annuler");
		}
		if(!editing ) showConsoleMainCommand();

	}
	


	/**
	 * 3 - Marks a palette as wrong
	 */
	private static void markWrongPalette() {
		printSepLine();
		printIdInfos();
		print("Marquer la palette comme mauvaise.");
		if(askYesNo()){
			px.setValidPalette(false);
			print("Palette marquée");
			updatePixelIndex();
		}
		if(!editing ) showConsoleMainCommand();
	}
	
	/**
	 * 4 - Search for a Texture
	 */
	private static void searchTex() {
		printSepLine();
		print("Entrer un motif de recherche :");
		String input = readStringFromConsole();
		unValidate();
		printSepLine();
		print("Recherche en cours : "+input);
		File f = new File(FilesPath.getSpriteDataDirectoryPath());
		List<File> list = FileLister.lister(f, ".png");
		print("Résultats : ");
		for(File result : list){
			if(result.getPath().toLowerCase().contains(input.toLowerCase())){
				print("- "+result);
			}
		}
		if(!editing ){
			showConsoleMainCommand();
		}else{
			showConsoleEditCommand(GameScreen.getIdEditList().size());
		}
	}

	/**
	 * 5 - Edits a smoothing tile.
	 */
	private static void editSmoothing() {
		printSepLine();
		print("Entrez un nouveau nom de template :");
		String input_template = readStringFromConsole();
		unValidate();
		if(input_template.equals("")){
			print("Annuler");
			if(!editing ) showConsoleMainCommand();
			return;
		}
		printSepLine();
		print("Entrez un ID pour T1 :");
		int input_tex1 = readIntFromConsole(true, 0);
		unValidate();
		if(input_tex1 <= 0){
			print("Annuler");
			if(!editing ) showConsoleMainCommand();
			return;
		}
		printSepLine();
		print("Entrez un ID pour T2 :");
		int input_tex2 = readIntFromConsole(true, 0);
		unValidate();
		if(input_tex2 <= 0){
			print("Annuler");
			if(!editing ) showConsoleMainCommand();
			return;
		}
		String input = ID.getAtlasFromId(id)+":"+input_template+" T1 "+input_tex1+" T2 "+input_tex2;
		printSepLine();
		printIdInfos();
		print("Modifier en : "+input);
		if(askYesNo()){
			IdEditMenu.updateIdFile(id, input);
			updatePixelIndex();
		}else{
			print("Annuler");
		}
		if(!editing ) showConsoleMainCommand();
	}

	/**
	 * 6 - Edit offsets
	 */
	private static void editOffsets() {
		if(!MIRROR){
			MapPixel px = PixelIndex.getPixelFromId(id);
			if(px != null){
				editOffset(px);
			}else{
				printSepLine();
				print("Pas de pixel dans l'index pour l'ID : "+id);
			}
		}else{
			MapPixel mirror = PixelIndex.getPixelFromId(Integer.parseInt(tex));
			if(mirror != null){
				editOffset2(mirror);
			}else{
				printSepLine();
				print("Pas de pixel dans l'index pour l'ID : "+Integer.parseInt(tex));
			}
		}
		if(!editing ) showConsoleMainCommand();
	}

	/**
	 * Edits an original offset.
	 * @param px
	 */
	private static void editOffset(MapPixel px) {
		printSepLine();
		printIdInfos();
		print("Edition d'un offset original.");
		print("Offset original => Pixel : "+tex+" = "+px.getOffsetX()+";"+px.getOffsetY());
		print("Entrer un nouvel x pour l'offset. (Entrée pour laisser tel quel)");
	    short offsetX = readShortFromConsole(false, px.getOffsetX());
	    unValidate();
	    print("Entrer un nouvel y pour l'offset. (Entrée pour laisser tel quel)");
	    short offsetY = readShortFromConsole(false, px.getOffsetY());
	    unValidate();
		printSepLine();
		print("Offset original => Pixel : "+tex+" = "+px.getOffsetX()+";"+px.getOffsetY());
		print("Modifier en  : "+offsetX+";"+offsetY);
		if(askYesNo()){
			px.setOffsetX(offsetX);
			px.setOffsetY(offsetY);
			updatePixelIndex();
		}
	}
	
	/**
	 * Edits a mirror offset.
	 * @param px
	 */
	private static void editOffset2(MapPixel px) {
		printSepLine();
		printIdInfos();
		print("Edition d'un offset de mirroir.");
		print("Offset original => Pixel : "+tex+" = "+px.getOffsetX2()+";"+px.getOffsetY2());
		print("Entrer un nouvel x pour l'offset. (Entrée pour laisser tel quel)");
	    short offsetX2 = readShortFromConsole(false, px.getOffsetX2());
	    unValidate();
	    print("Entrer un nouvel y pour l'offset. (Entrée pour laisser tel quel)");
	    short offsetY2 = readShortFromConsole(false, px.getOffsetY2());
	    unValidate();
		printSepLine();
		print("Offset original => Pixel : "+tex+" = "+px.getOffsetX2()+";"+px.getOffsetY2());
		print("Modifier en  : "+offsetX2+";"+offsetY2);
		if(askYesNo()){
			px.setOffsetX2(offsetX2);
			px.setOffsetY2(offsetY2);
			updatePixelIndex();
		}
	}

	/**
	 * 7 - Edits a specific ID
	 */
	private static void editSpecificID() {
		printSepLine();
		print("Entrer un ID : ");
		int editId = readIntFromConsole(true,1);
		unValidate();
		if(!ID.containsId(editId)){
			print("ID non mappée");
		}else{
			point = GameScreen.getIdEditList().get(editId);
			setId(editId);
			editID(editId);
		}
	}
	
	/**
	 * 8 - Edit all Ids on map
	 */
	private static void editIdsOnMap() {
		printSepLine();
		int nb_ids = GameScreen.getIdEditList().size();
		print(nb_ids+" Ids à éditer.");
		print("Auquel commencer à éditer? (Par défaut : "+index+")");
		index  = readIntFromConsole(true, index);
		unValidate();
		Iterator<Integer> iter_ids = GameScreen.getIdEditList().keySet().iterator();
		for(int i = 0 ; i < index ; i++){
			iter_ids.next();
		}
		print("Edition à partir de l'ID "+index);
		editing = true;
		while(iter_ids.hasNext()){
			setId(iter_ids.next());
			showConsoleEditCommand(nb_ids);
			index++;
		}
		exit();
	}
	
	private static void showConsoleEditCommand(int nb_ids) {
		point = GameScreen.getIdEditList().get(id);
		getEditInfos(point);
		printIdEditCommands();
		printSepLine();
		printIdInfos();
		MapPixel px = PixelIndex.getPixelFromIdAndPoint(id, point);
		if(px != null)printPixelInfos(px);
		print(index+"/"+nb_ids+" Ids édités.");		
		print("Choisir une commande :");
		int cmd = readIntFromConsole(true, 0);
		execEditIdsCommand(cmd);
	}

	/**
	 * 9 - Teleport
	 */
	private static void teleport() {
		printSepLine();
		print("Téléportation");
		print("Choisissez une carte : 1 worldmap / 2 leoworld / 3 cavernmap");
		print("4 dungeonmap / 5 underworld / Entrée pour garder la carte actuelle");
	    int mapNb = readIntFromConsole(true, 0);
	    unValidate();
	    if(mapNb > 5 || mapNb < 0) mapNb = 0;
	    String mapName = GameScreen.getCurrentMap();
	    if(mapNb == 1) mapName = "v2_worldmap";
	    if(mapNb == 2) mapName = "v2_leoworld";
	    if(mapNb == 3) mapName = "v2_cavernmap";
	    if(mapNb == 4) mapName = "v2_dungeonmap";
	    if(mapNb == 5) mapName = "v2_underworld";
	    print("Choisissez une abscisse : (0-3071)");
	    int x = readIntFromConsole(true, 1536);
	    unValidate();
	    if (x > 3071) x = 3071;
		printSepLine();
	    print("Choisissez une ordonnée : (0-3071)");
	    int y = readIntFromConsole(true, 1536);
	    unValidate();
	    if (y > 3071) y = 3071;
		printSepLine();
		Place place = new Place("Teleport", mapName, PointsManager.getPoint(x*32, y*16));
		print("Téléportation vers : "+place.getMapName()+" ("+place.getCoord().x/32+";"+place.getCoord().y/16+")");
		if(askYesNo()){
			GameScreen.teleport(place);
			exit();
		}
	}
	
	/**
	 * 42 - Show hidden menu.
	 */
	private static void showHiddenMenu() {
		printSepLine();
		print("Menu caché");
		print("0 - Retour");
		print("Choisir une commande :");
		int cmd = readIntFromConsole(true, 0);
		unValidate();
		if (cmd <= 0)showConsoleMainCommand();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////UTILS/////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Sets up the console.
	 * @param pt
	 */
	private static void getEditInfos(Point pt) {
		point = pt;
		id = GameScreen.getIdAtCoordOnMap(GameScreen.getCurrentMap(), point);
		tex = "";
		if(ID.containsId(id)){
			tex = ID.getTexFromId(id);
			atlas = ID.getAtlasFromId(id);
		}else{
			print("ID inexistant : "+id);
			tex = "non mappée";
			atlas = "non mappé";	
		}
		px = PixelIndex.getPixelFromIdAndPoint(id, point);
		if (px == null){
			print("Pas de pixel associé dans l'index : "+id+" "+atlas+" "+tex);
		}
		if(ID.isMirrorId(id)){
			MIRROR = true;
		}else{
			MIRROR = false;
		}		
	}
	
	/**
	 * Starts a new console.
	 */
	public static void command() {
		printWelcomeMsg();
		showConsoleMainCommand();
	}
	
	/**
	 * Prints the welcome message
	 */
	private static void printWelcomeMsg() {
		historique++;
		printSepLine();
		switch(historique){
		case 1 :
			print("Bonjour! Je suis l'ordinateur de bord!");
			print("C'est ta première fois dans la ligne de commande.");
			print("Passes un bon moment à taper ton clavier!");
			break;
		case 42 :
			print("Si tu connais la réponse à la vie à l'univers et à tout le reste,");
			print("tu peux trouver un menu caché.");
			break;
		default : 
			print("Te revoilà!");
			print("C'est un plaisir d'exécuter ton "+historique+"ème ordre.");
			break;
		}
		printSepLine();
	}
	
	
	/**
	 * Updates an id
	 * @param input
	 */
	public static void updateIdFile(int id, String input) {
		print("Mise à jour de id.txt");
		ID.updateId(id, input);
	}
	
	/**
	 * Updates pixel_index in a new Thread.
	 */
	private static void updatePixelIndex() {
		print("Mise à jour de l'index");
		ThreadsUtil.queueInSingleThread(RunnableCreatorUtil.getPixelIndexFileUpdaterRunnable(PointsManager.getPoint(point.x*32, point.y*16)));
		GameScreen.teleport(new Place("editId", "v2_worldmap", point));
	}
	
	/**
	 * Reads an Integer from console input.
	 * @param positive : true for a positive int.
	 * @return
	 */
	private static int readIntFromConsole(boolean positive, int error) {
	    String input = readStringFromConsole();
		int nb = 0;
		try{
			nb = Integer.parseInt(input);
		}catch(NumberFormatException e){
			return error;
		}
		if(!positive) return nb;
		if(nb >= 0 ) return nb;
		print("Doit être positif.");
		return error;
	}

	/**
 	 * Reads a short from console input.
	 * @param positive : true for a positive short.
	 * @return
	 */
	private static short readShortFromConsole(boolean positive, int error) {
	    String input = readStringFromConsole();
		short nb = 0;
		try{
			nb = Short.parseShort(input);
		}catch(NumberFormatException e){
			System.err.println("Entrer un entier");
			return (short) error;
		}
		if(!positive) return nb;
		if(nb >= 0 ) return nb;
		print("Doit être positif.");
		return -1;
	}
	
	/**
	 * Reads a String from console input.
	 * @return
	 */
	private static String readStringFromConsole() {
		while(!validate){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	    return inputText;
	}
	
	/**
	 * Ask for confirmation.
	 * @return
	 */
	private static boolean askYesNo() {
		print("Confirmer? (o/n)");
		String yesno = readStringFromConsole();
		unValidate();
		if(yesno.equalsIgnoreCase("y") || yesno.equalsIgnoreCase("yes") || yesno.equalsIgnoreCase("o") || yesno.equalsIgnoreCase("oui"))return true;
		return false;
	}
	
	/**
	 * Prints a line on console.
	 * @param line
	 */
	private static void print(String line) {
		ThreadsUtil.executeInGraphicalThread(RunnableCreatorUtil.getPrintLineRunnable(line));
	}
	
	public static void shiftConsoleTexts() {
		for(int i = CONSOLE_LINES ; i > 0 ; i--){
			TextButton button = getConsoleTexts().get(i);
			button.setText(getConsoleTexts().get(i-1).getText().toString());
		}
	}

	/**
	 * Prints a line of ###
	 */
	private static void printSepLine(){
		print("####################################################################################################################");
	}
	
	/**
	 * Prints the command list
	 */
	private static void printCommands() {
		print("Commandes :");
		print("0 - Sortir");
		print("1 - Éditer le mappage de l'ID");
		print("2 - Éditer un mirroir");
		print("3 - Marquer un problème de palette");
		print("4 - Rechercher un fichier");
		print("5 - Editer un smoothing");
		print("6 - Editer un Offset");
		print("7 - Editer un ID spécifique");
		print("8 - Editer les ID dans l'ordre");
		print("9 - Se téléporter");
	}
	
	/**
	 * Prints the command list
	 */
	private static void printIdEditCommands() {
		print("Commandes :");
		print("0 - Sortir");
		print("1 - Éditer le mappage de l'ID");
		print("2 - Éditer un mirroir");
		print("3 - Marquer un problème de palette");
		print("4 - Rechercher un fichier");
		print("5 - Editer un smoothing");
		print("6 - Editer un Offset");
		print("7 - Passer au suivant");		
	}
	
	/**
	 * Prints an ID's infos.
	 */
	private static void printIdInfos() {
		print("Édition d'un ID : "+id+" Dossier : "+ID.getAtlasFromId(id)+" Nom : "+ID.getTexFromId(id));		
	}
	
	/**
	 * Prints a MapPixel's infos.
	 * @param px
	 */
	private static void printPixelInfos(MapPixel px) {
		print("Pixel Atlas : "+px.getAtlas());
		print("Pixel Texture : "+px.getTex());
		print("Pixel Id : "+px.getId());
		print("Pixel Palette : "+px.getPaletteName());
		print("Pixel Offset : "+px.getOffset().x+";"+px.getOffset().y);
		print("Pixel Offset2 : "+px.getOffset2().x+";"+px.getOffset2().y);
		print("Pixel Largeur : "+px.getLargeur());
		print("Pixel Hauteur : "+px.getHauteur());
		print("Pixel palette valide : "+px.isPaletteValid());
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		batch.begin();
			stage.draw();
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		Gdx.graphics.setTitle("EDIT : "+point.x+";"+point.y);
		style.font = new BitmapFont(Gdx.files.internal((FilesPath.getFontFilePath())));
		input = new TextButton("", style);
		input.setPosition(Gdx.graphics.getWidth()/2, 20);
		stage = new Stage();
		stage.addActor(input);
		batch = new SpriteBatch();
		initConsoleText();
		ThreadsUtil.queueInSingleThread(RunnableCreatorUtil.getConsoleRunnable());
	}

	private void initConsoleText() {
		getConsoleTexts().clear();
		inputText = "";
		for(int i = 0 ; i <= CONSOLE_LINES ; i++){
			addConsoleLign(i);
		}
	}

	private void addConsoleLign(int i) {
		TextButton button = new TextButton("", style);
		button.setPosition(Gdx.graphics.getWidth()/2, 40+(20*i));
		stage.addActor(button);
		getConsoleTexts().add(button);
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
		stage.dispose();
		batch.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.SHIFT_LEFT || keycode == Keys.SHIFT_RIGHT){
			shift  = true;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.ESCAPE){
			exit();
		}else if(keycode == Keys.BACKSPACE){
			deleteChar();
		}else if(keycode == Keys.SPACE){
			typeChar(" ");
		}else if(keycode == Keys.F1){
		}else if(keycode == Keys.F2){
		}else if(keycode == Keys.F3){
		}else if(keycode == Keys.F4){
		}else if(keycode == Keys.F5){
		}else if(keycode == Keys.F6){
		}else if(keycode == Keys.F7){
		}else if(keycode == Keys.F8){
		}else if(keycode == Keys.F9){
		}else if(keycode == Keys.F10){
		}else if(keycode == Keys.F11){
		}else if(keycode == Keys.F12){
		}else if(keycode == Keys.TAB){
		}else if(keycode == Keys.SHIFT_LEFT){
			shift = false;
		}else if(keycode == Keys.CONTROL_LEFT){
		}else if(keycode == Keys.ALT_LEFT){
		}else if(keycode == Keys.ALT_RIGHT){
		}else if(keycode == Keys.CONTROL_RIGHT){
		}else if(keycode == Keys.SHIFT_RIGHT){
			shift = false;
		}else if(keycode == Keys.LEFT){
		}else if(keycode == Keys.DOWN){
		}else if(keycode == Keys.RIGHT){
		}else if(keycode == Keys.UP){
		}else if(keycode == Keys.INSERT){
		}else if(keycode == Keys.DEL){
		}else if(keycode == Keys.HOME){
		}else if(keycode == Keys.END){
		}else if(keycode == Keys.PAGE_UP){
		}else if(keycode == Keys.PAGE_DOWN){
		}else if(keycode == Keys.NUM){
		}else if(keycode == Keys.NUMPAD_0){
			typeChar("0");
		}else if(keycode == Keys.NUMPAD_1){
			typeChar("1");
		}else if(keycode == Keys.NUMPAD_2){
			typeChar("2");
		}else if(keycode == Keys.NUMPAD_3){
			typeChar("3");
		}else if(keycode == Keys.NUMPAD_4){
			typeChar("4");
		}else if(keycode == Keys.NUMPAD_5){
			typeChar("5");
		}else if(keycode == Keys.NUMPAD_6){
			typeChar("6");
		}else if(keycode == Keys.NUMPAD_7){
			typeChar("7");
		}else if(keycode == Keys.NUMPAD_8){
			typeChar("8");
		}else if(keycode == Keys.NUMPAD_9){
			typeChar("9");
		}else if(keycode == Keys.NUM_0){
			if(!shift){
				typeChar("à");
			}else{
				typeChar("0");
			}
		}else if(keycode == Keys.NUM_1){
			if(!shift){
				typeChar("&");
			}else{
				typeChar("1");
			}
		}else if(keycode == Keys.NUM_2){
			if(!shift){
				typeChar("é");
			}else{
				typeChar("2");
			}
		}else if(keycode == Keys.NUM_3){
			if(!shift){
				typeChar("\"");
			}else{
				typeChar("3");
			}
		}else if(keycode == Keys.NUM_4){
			if(!shift){
				typeChar("\'");
			}else{
				typeChar("4");
			}
		}else if(keycode == Keys.NUM_5){
			if(!shift){
				typeChar("(");
			}else{
				typeChar("5");
			}
		}else if(keycode == Keys.NUM_6){
			if(!shift){
				typeChar("-");
			}else{
				typeChar("6");
			}
		}else if(keycode == Keys.NUM_7){
			if(!shift){
				typeChar("è");
			}else{
				typeChar("7");
			}
		}else if(keycode == Keys.NUM_8){
			if(!shift){
				typeChar("_");
			}else{
				typeChar("8");
			}
		}else if(keycode == Keys.NUM_9){
			if(!shift){
				typeChar("ç");
			}else{
				typeChar("9");
			}
		}else if(keycode == Keys.LEFT_BRACKET){
			typeChar(")");
		}else if(keycode == Keys.ENTER){
			validate();
		}else {
			typeChar(Keys.toString(keycode).toLowerCase());
		}
		return true;
	}

	private void deleteChar() {
		if(inputText.length() > 0) inputText  = inputText.substring(0, inputText.length()-1);
		input.setText(inputText);		
	}

	private void typeChar(String c) {
		inputText  = inputText+c;
		input.setText(inputText);
	}

	public void validate(){
		validate  = true;
	}

	public static void unValidate() {
		validate = false;
		inputText  = "";
		if(input != null) input.setText(inputText);
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
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

	public static boolean isValid() {
		return validate;
	}

	public static void setId(int id) {
		IdEditMenu.id = id;
	}

	public static List<TextButton> getConsoleTexts() {
		return consoleTexts;
	}

	public static void setConsoleTexts(List<TextButton> consoleTexts) {
		IdEditMenu.consoleTexts = consoleTexts;
	}
}
