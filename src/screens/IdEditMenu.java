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
import java.util.Iterator;
import java.util.List;

import opent4c.MapPixel;
import opent4c.SpriteData;
import opent4c.utils.FileLister;
import opent4c.utils.FilesPath;
import opent4c.utils.Places;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;

/**
 * @author synoga
 *
 */
public class IdEditMenu{
	private static Logger logger = LogManager.getLogger(IdEditMenu.class.getSimpleName());
	private static int id;
	private static Point point;
	private static String tex;
	private static String atlas;
	private static List<String> badPalettes = new ArrayList<String>();
	private static boolean MIRROR = false;
	private static int historique = 0;
	private static int index = 0;

	/**
	 * Opens CLI.
	 * @param point
	 */
	public IdEditMenu(Point point) {
		loadBadPalettes();
		getEditInfos(point);
		ThreadsUtil.executeInThread(RunnableCreatorUtil.getConsoleRunnable());
	}

	/**
	 * Asks for a command input on console.
	 */
	public static void showConsoleMainCommand() {
		printCommands();
		printSepLine();
		printIdInfos();
		MapPixel px = SpriteData.getPixelFromIdAndPoint(id, point);
		if(px != null)printPixelInfos(px);
		print("Choisir une commande :");
		int cmd = readIntFromConsole(true, 0);
		execNormalCommand(cmd);
		exit();
	}

	/**
	 * Runs a main command.
	 * @param cmd
	 */
	private static void execNormalCommand(int cmd) {
		if (cmd <= 0)cancel();
		if (cmd == 1)editID(id);
		if (cmd == 2)editMirror();
		if (cmd == 3)markWrongPalette();
		if (cmd == 4)searchTex();
		if (cmd == 5)editSmoothing();
		if (cmd == 6)editOffsets();
		if (cmd == 7)editSpecificID();
		if (cmd == 8)editIdsOnMap();
		if (cmd == 42)showHiddenMenu();		
	}

	/**
	 * Runs a command while editing ids.
	 * @param cmd
	 */
	private static void execEditIdsCommand(int cmd) {
		if (cmd <= 0)return;;
		if (cmd == 1)editID(id);
		if (cmd == 2)editMirror();
		if (cmd == 3)markWrongPalette();
		if (cmd == 4)editSmoothing();
		if (cmd == 5)editOffsets();
		if (cmd == 6)return;
		if (cmd == 42)showHiddenMenu();		
	}

	/**
	 * 0 - Cancel edition
	 */
	public static void cancel(){
		printSepLine();
		print("Annuler.");
	}
	
	/**
	 * Exits edit menu
	 */
	public static void exit() {
		printSepLine();
		print("Au revoir Dave.");
		MapManager.close_edit_menu();
	}
	
	/**
	 * 1 - Edits an ID;
	 */
	public static void editID(int id){
		printSepLine();
		printIdInfos();
		MapPixel px = SpriteData.getPixelFromIdAndPoint(id, point);
		if(px != null){
			printPixelInfos(px);
		}
		printSepLine();
		print("Entrez un nouveau nom d'atlas. (Entrée pour laisser tel quel)");
		String input_atlas = readStringFromConsole();
		if(input_atlas.equals("")){
			input_atlas = SpriteData.getAtlasFromId(id);
		}
		printSepLine();
		print("Entrez un nouveau nom de texture. (Entrée pour laisser tel quel)");
		String input_tex = readStringFromConsole();
		if(input_tex.equals("")){
			input_tex = SpriteData.getTexFromId(id);
		}
		String input = input_atlas+":"+input_tex;
		printSepLine();
		printIdInfos();
		print("Modifier en - ID : "+id+" Dossier : "+input_atlas+" Nom : "+input_tex);
		if(!askYesNo()){
			cancel();
		}else{
			IdEditMenu.updateIdFile(input);
			updatePixelIndex();
		}
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
		if(input_id == -1){
			cancel();	
		}else{
			printSepLine();
			printIdInfos();
			print("Modifier en - ID : "+id+" Type : MIRROR ID : "+input_id);
			if(!askYesNo()){
				cancel();
			}else{
				IdEditMenu.updateIdFile("MIRROR:"+input_id);
				updatePixelIndex();
			}
		}		
	}
	


	/**
	 * 3 - Marks a palette as wrong
	 */
	private static void markWrongPalette() {
		printSepLine();
		printIdInfos();
		print("Marquer la palette comme mauvaise.");
		if(!askYesNo()){
			cancel();
		}else{
			badPalettes.add(id+" : "+tex);
			saveBadPalettes();
			print("Palette marquée");
			exit();
		}
	}
	
	/**
	 * 4 - Search for a Texture
	 */
	private static void searchTex() {
		printSepLine();
		print("Entrer un motif de recherche :");
		String input = readStringFromConsole();
		printSepLine();
		print("Résultats pour : "+input);
		File f = new File(FilesPath.getSpriteDataDirectoryPath());
		List<File> list = FileLister.lister(f, ".png");
		for(File result : list){
			if(result.getPath().toLowerCase().contains(input.toLowerCase())){
				print("- "+result);
			}
		}
		showConsoleMainCommand();
	}

	/**
	 * 5 - Edits a smoothing tile.
	 */
	private static void editSmoothing() {
		printSepLine();
		print("Entrez un nouveau nom de template :");
		String input_template = readStringFromConsole();
		if(input_template.equals("")){
			cancel();
			return;
		}
		printSepLine();
		print("Entrez un ID pour T1 :");
		int input_tex1 = readIntFromConsole(true, 0);
		if(input_tex1 <= 0){
			cancel();
			return;
		}
		printSepLine();
		print("Entrez un ID pour T2 :");
		int input_tex2 = readIntFromConsole(true, 0);
		if(input_tex2 <= 0){
			cancel();
			return;
		}
		String input = SpriteData.getAtlasFromId(id)+":"+input_template+" T1 "+input_tex1+" T2 "+input_tex2;
		printSepLine();
		printIdInfos();
		print("Modifier en : "+input);
		if(!askYesNo()){
			cancel();
		}else{
			IdEditMenu.updateIdFile(input);
			updatePixelIndex();
		}		
	}

	/**
	 * 6 - Edit offsets
	 */
	private static void editOffsets() {
		if(!MIRROR){
			MapPixel px = SpriteData.getPixelFromId(id);
			if(px != null){
				editOffset(px);
			}else{
				printSepLine();
				print("Pas de pixel dans l'index pour l'ID : "+id);
				cancel();
			}
		}else{
			MapPixel mirror = SpriteData.getPixelFromId(Integer.parseInt(tex));
			if(mirror != null){
				editOffset2(mirror);
			}else{
				printSepLine();
				print("Pas de pixel dans l'index pour l'ID : "+Integer.parseInt(tex));
				cancel();
			}
		}
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
	    print("Entrer un nouvel y pour l'offset. (Entrée pour laisser tel quel)");
	    short offsetY = readShortFromConsole(false, px.getOffsetY());
		printSepLine();
		print("Offset original => Pixel : "+tex+" = "+px.getOffsetX()+";"+px.getOffsetY());
		print("Modifier en  : "+offsetX+";"+offsetY);
		if(!askYesNo()){
			cancel();
		}else{
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
	    print("Entrer un nouvel y pour l'offset. (Entrée pour laisser tel quel)");
	    short offsetY2 = readShortFromConsole(false, px.getOffsetY2());
		printSepLine();
		print("Offset original => Pixel : "+tex+" = "+px.getOffsetX2()+";"+px.getOffsetY2());
		print("Modifier en  : "+offsetX2+";"+offsetY2);
		if(!askYesNo()){
			cancel();
		}else{
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
		if(!SpriteData.getIdfull().containsKey(editId)){
			print("ID non mappée");
			
		}else{
			point = MapManager.getIdEditList().get(editId);
			id = editId;
			Gdx.app.postRunnable(new Runnable(){
				@Override
				public void run(){
					MapManager.teleport(new Places("editId", "v2_worldmap", point));
				}
			});
			editID(editId);
		}
	}
	
	/**
	 * 8 - Edit all Ids on map
	 */
	private static void editIdsOnMap() {
		printSepLine();
		int nb_ids = MapManager.getIdEditList().size();
		print(nb_ids+" Ids à éditer.");
		print("Auquel commencer à éditer? (Par défaut : "+index+")");
		index  = readIntFromConsole(true, index);
		Iterator<Integer> iter_ids = MapManager.getIdEditList().keySet().iterator();
		for(int i = 0 ; i < index ; i++){
			iter_ids.next();
		}
		print("Edition à partir de l'ID "+index);
		while(iter_ids.hasNext()){
			id = iter_ids.next();
			point = MapManager.getIdEditList().get(id);
			getEditInfos(point);
			Gdx.app.postRunnable(new Runnable(){
				@Override
				public void run(){
					MapManager.teleport(new Places("editId", "v2_worldmap", point));
				}
			});
			printIdEditCommands();
			printSepLine();
			printIdInfos();
			MapPixel px = SpriteData.getPixelFromIdAndPoint(id, point);
			if(px != null)printPixelInfos(px);
			print("Choisir une commande :");
			int cmd = readIntFromConsole(true, 0);
			execEditIdsCommand(cmd);
			if(cmd == 0)break;
			printSepLine();
			index++;
			print(index+"/"+nb_ids+" Ids édités.");
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
		id = MapManager.getIdAtCoordOnMap("v2_worldmap", point);
		tex = "";
		if(SpriteData.getIdfull().containsKey(id)){
			tex = SpriteData.getTexFromId(id);
			atlas = SpriteData.getAtlasFromId(id);
		}else{
			tex = "non mappée";
			atlas = "non mappé";	
		}
		if(atlas.equals("MIRROR")){
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
			print("C'est un plaisir d'exécuter ton ordre "+historique);
			break;
		}
		printSepLine();
	}
	
	/**
	 * Loads bad_palette file.
	 */
	private void loadBadPalettes() {
		File bad_palette_file = new File(FilesPath.getBadPaletteFilePath());
		if(!bad_palette_file.exists())return;
		try{
			BufferedReader buff = new BufferedReader(new FileReader(bad_palette_file.getPath()));			 
			try {
				String line;
				while ((line = buff.readLine()) != null) {
					badPalettes.add(line);
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Gdx.app.exit();
		}		
	}
	
	/**
	 * Saves bad_palette file
	 */
	private static void saveBadPalettes() {
		File badPalettesFile = new File(FilesPath.getBadPaletteFilePath());
		if (!badPalettesFile.exists()){
			try {
				badPalettesFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				Gdx.app.exit();
			}
		}else{
			badPalettesFile.delete();
			try {
				badPalettesFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				Gdx.app.exit();
			}
		}
		OutputStreamWriter dat_file = null;
		try {
			dat_file = new OutputStreamWriter(new FileOutputStream(FilesPath.getBadPaletteFilePath()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
		Iterator<String> iter = badPalettes.iterator();
		while (iter.hasNext()){
			String bad = iter.next();
			try {
				dat_file.write(bad+System.lineSeparator());
			} catch (IOException e) {
				e.printStackTrace();
				Gdx.app.exit();
			}	
		}
		try {
			dat_file.close();
		} catch (IOException e) {
			e.printStackTrace();
			Gdx.app.exit();
		}		
	}
	
	/**
	 * Updates idFull.txt
	 * @param input
	 */
	public static void updateIdFile(String input) {
		print("Mise à jour de idFull.txt");
		SpriteData.getIdfull().put(id, input);
		SpriteData.writeIdFullToFile();
	}
	
	/**
	 * Updates pixel_index in a new Thread.
	 */
	private static void updatePixelIndex() {
		print("Mise à jour de l'index");
		ThreadsUtil.executeInThread(RunnableCreatorUtil.getPixelIndexFileUpdaterRunnable(point));
	}
	
	/**
	 * Reads an Integer from console input.
	 * @param positive : true for a positive int.
	 * @return
	 */
	private static int readIntFromConsole(boolean positive, int error) {
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    String input = "";
		try {
			input = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			Gdx.app.exit();
		}
		int nb = 0;
		try{
			nb = Integer.parseInt(input);
		}catch(NumberFormatException e){
			return error;
		}
		if(!positive) return nb;
		if(nb >= 0 ) return nb;
		print("Doit être positif.");
		return -1;
	}

	/**
 	 * Reads a short from console input.
	 * @param positive : true for a positive short.
	 * @return
	 */
	private static short readShortFromConsole(boolean positive, int error) {
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    String input = "";
		try {
			input = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			Gdx.app.exit();
		}
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
		String input = "";
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    try {
			input = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.warn(e);
			return "";
		}
	    return input;
	}
	
	/**
	 * Ask for confirmation.
	 * @return
	 */
	private static boolean askYesNo() {
		print("Confirmer? (o/n)");
		String yesno = "";
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		try {
			yesno = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.warn(e);
			return false;
		}
		if(yesno.equalsIgnoreCase("y") || yesno.equalsIgnoreCase("yes") || yesno.equalsIgnoreCase("o") || yesno.equalsIgnoreCase("oui"))return true;
		return false;
	}
	
	/**
	 * Prints a line on console.
	 * @param line
	 */
	private static void print(String line) {
		System.out.println(line);		
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
		print("0 - Annuler");
		print("1 - Éditer le mappage de l'ID");
		print("2 - Éditer un mirroir");
		print("3 - Marquer un problème de palette");
		print("4 - Rechercher un fichier");
		print("5 - Editer un smoothing");
		print("6 - Editer un Offset");
		print("7 - Editer un ID spécifique");
		print("8 - Editer les ID dans l'ordre");		
	}
	
	/**
	 * Prints the command list
	 */
	private static void printIdEditCommands() {
		print("Commandes :");
		print("0 - Annuler");
		print("1 - Éditer le mappage de l'ID");
		print("2 - Éditer un mirroir");
		print("3 - Marquer un problème de palette");
		print("4 - Editer un smoothing");
		print("5 - Editer un Offset");
		print("6 - Editer les ID dans l'ordre");		
	}
	
	/**
	 * Prints an ID's infos.
	 */
	private static void printIdInfos() {
		print("Édition d'un ID : "+id+" Dossier : "+atlas+" Nom : "+tex);		
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
	}
}
