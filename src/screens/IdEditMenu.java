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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import opent4c.MapPixel;
import opent4c.SpriteData;
import opent4c.SpriteUtils;
import opent4c.utils.FileLister;
import opent4c.utils.FilesPath;
import opent4c.utils.Places;
import opent4c.utils.RunnableCreatorUtil;
import opent4c.utils.ThreadsUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

/**
 * @author synoga
 *
 */
public class IdEditMenu{
	private static Logger logger = LogManager.getLogger(IdEditMenu.class.getSimpleName());
	private static int id;
	private TextButtonStyle style = new TextButtonStyle();
	private static Point point;
	private static String tex;
	private static String atlas;
	private static List<String> badPalettes = new ArrayList<String>();
	private static List<String> mirrors = new ArrayList<String>();

	/**
	 * @param point
	 * @param id
	 */
	public IdEditMenu(Point point, int id) {
		IdEditMenu.point = point;
		loadMirrors();
		loadBadPalettes();
		IdEditMenu.id = id;
		tex = "";
		style.font = new BitmapFont();
		if(SpriteData.getIdfull().containsKey(id)){
			tex = SpriteData.getTexFromId(id);
			atlas = SpriteData.getAtlasFromId(id);
		}else{
			tex = "non mappée";
			atlas = "non mappé";	
		}
		ThreadsUtil.executeInThread(RunnableCreatorUtil.getConsoleCommandInputRunnable());
	}

	/**
	 * 
	 */
	private void loadMirrors() {
		File mirror_file = new File(FilesPath.getMirrorFilePath());
		if(!mirror_file.exists())return;
		try{
			BufferedReader buff = new BufferedReader(new FileReader(mirror_file.getPath()));			 
			try {
				String line;
				while ((line = buff.readLine()) != null) {
					mirrors.add(line);
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}		
	}

	/**
	 * 
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
			System.exit(1);
		}		
	}

	public static void editID(){
		System.out.println("##########################################################");
		System.out.println("Entrez un nouveau nom d'atlas :");
		String input_atlas = "null";
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    try {
			input_atlas = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		if(input_atlas.equals("")){
			input_atlas = SpriteData.getAtlasFromId(id);
		}
		System.out.println("##########################################################");
		System.out.println("Entrez un nouveau nom de texture :");
		String input_tex = "null";
	    bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    try {
	    	input_tex = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		if(input_tex.equals("")){
			input_tex = SpriteData.getTexFromId(id);
		}
		String input = input_atlas+":"+input_tex;
		System.out.println("##########################################################");
		System.out.println("Informations d'origine - ID : "+id+" Dossier : "+atlas+" Mappage : "+tex);
		System.out.println("Modifier en - ID : "+id+" Dossier : "+input_atlas+" Mappage : "+input_tex);
		System.out.println("Confirmer? (o/n)");
	    String confirm = "";
		try {
			confirm = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		if(!confirm.equals("o")){
			cancel();
			return;
		}else{
			System.out.println("Mise à jour de id.txt");
			IdEditMenu.updateIdFile(input);
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getPixelIndexFileUpdaterRunnable(point));
		}
	}

	/**
	 * Cancel edition
	 */
	public static void cancel(){
		System.out.println("##########################################################");
		System.out.println("Annuler.");
		exit();
	}
	
	/**
	 * Exits edit menu
	 */
	public static void exit() {
		System.out.println("##########################################################");
		MapManager.close_edit_menu();
	}

	/**
	 * 
	 */
	private static void saveMirrors() {
		File mirrorFile = new File(FilesPath.getMirrorFilePath());
		if(!mirrorFile.exists()){
			try {
				mirrorFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}else{
			mirrorFile.delete();
			try {
				mirrorFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}
		OutputStreamWriter dat_file = null;
		try {
			dat_file = new OutputStreamWriter(new FileOutputStream(FilesPath.getMirrorFilePath()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		Iterator<String> iter = mirrors.iterator();
		while (iter.hasNext()){
			String mirror = iter.next();
			try {
				dat_file.write(mirror+System.lineSeparator());
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}	
		}
		try {
			dat_file.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}

	/**
	 * 
	 */
	private static void saveBadPalettes() {
		File badPalettesFile = new File(FilesPath.getBadPaletteFilePath());
		if (!badPalettesFile.exists()){
			try {
				badPalettesFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}else{
			badPalettesFile.delete();
			try {
				badPalettesFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}
		OutputStreamWriter dat_file = null;
		try {
			dat_file = new OutputStreamWriter(new FileOutputStream(FilesPath.getBadPaletteFilePath()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		Iterator<String> iter = badPalettes.iterator();
		while (iter.hasNext()){
			String bad = iter.next();
			try {
				dat_file.write(bad+System.lineSeparator());
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}	
		}
		try {
			dat_file.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}

	/**
	 * @param input
	 */
	public static void updateIdFile(String input) {
		SpriteData.getIdfull().put(id, input);
		SpriteData.writeIdFullToFile();
	}

	/**
	 * @param tex
	 * @param atlas
	 */
	public static void showConsoleAskCommand() {
		System.out.println("##########################################################");
		System.out.println("Édition d'un ID : "+id);
		System.out.println("Fichier actuellement mappé sur : "+tex);
		System.out.println("Dans le dossier : "+atlas);		
		System.out.println("Choisir une commande :");
		System.out.println("0 - Annuler");
		System.out.println("1 - Éditer le mappage de l'ID");
		System.out.println("2 - Marquer l'ID en mirroir");
		System.out.println("3 - Marquer un problème de palette");
		System.out.println("4 - Rechercher un fichier");
		System.out.println("5 - Editer un smoothing");
		String input = "null";
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    try {
			input = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		int cmd = 0;
		try{
			cmd = Integer.parseInt(input);
		}catch(NumberFormatException e){
			System.err.println("Entrer un entier naturel");
			cancel();
			return;
		}
		if (cmd == 0){
			cancel();
			return;
		}
		if (cmd == 1)editID();
		if (cmd == 2)markMirror();
		if (cmd == 3)markWrongPalette();
		if (cmd == 4)search();
		if (cmd == 5)editSmoothing();
	}

	private static void editSmoothing() {
		System.out.println("##########################################################");
		System.out.println("Entrez un nouveau nom de template :");
		String input_template = "null";
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    try {
			input_template = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		if(input_template.equals("")){
			cancel();
		}
		System.out.println("##########################################################");
		System.out.println("Entrez un ID pour T1 :");
		String input_tex1 = "null";
	    bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    try {
	    	input_tex1 = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		if(input_tex1.equals("")){
			cancel();
		}
		System.out.println("##########################################################");
		System.out.println("Entrez un ID pour T2 :");
		String input_tex2 = "null";
	    bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    try {
	    	input_tex2 = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		if(input_tex2.equals("")){
			cancel();
		}
		String input = SpriteData.getAtlasFromId(id)+":"+input_template+" T1 "+input_tex1+" T2 "+input_tex2;
		System.out.println("##########################################################");
		System.out.println("Informations d'origine - ID : "+id+" Smoothing : "+tex);
		System.out.println("Modifier en : "+input);
		System.out.println("Confirmer? (o/n)");
	    String confirm = "";
		try {
			confirm = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		if(!confirm.equals("o")){
			cancel();
			return;
		}else{
			System.out.println("Mise à jour de id.txt");
			IdEditMenu.updateIdFile(input);
			ThreadsUtil.executeInThread(RunnableCreatorUtil.getPixelIndexFileUpdaterRunnable(point));
		}		
	}

	/**
	 * 
	 */
	private static void markWrongPalette() {
		System.out.println("##########################################################");
		System.out.println("Informations d'origine - ID : "+id+" Dossier : "+atlas+" Mappage : "+tex);
		System.out.println("Marquer la palette comme mauvaise.");
		System.out.println("Confirmer? (o/n)");
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    String confirm = "";
		try {
			confirm = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		if(!confirm.equals("o")){
			cancel();
			return;
		}else{
			badPalettes.add(id+" : "+tex);
			saveBadPalettes();
			System.out.println("Palette marquée");
			exit();
		}
	}

	/**
	 * 
	 */
	private static void markMirror() {
		System.out.println("##########################################################");
		System.out.println("Informations d'origine - ID : "+id+" Dossier : "+atlas+" Mappage : "+tex);
		System.out.println("Marquer l'ID comme le mirroir de la texture.");
		System.out.println("Confirmer? (o/n)");
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    String confirm = "";
		try {
			confirm = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		if(!confirm.equals("o")){
			cancel();
			return;
		}else{
			mirrors.add(id+" : "+tex);
			saveMirrors();
			System.out.println("Miroir marqué");
			exit();
		}		
	}

	/**
	 * 
	 */
	private static void search() {
		System.out.println("##########################################################");
		System.out.println("Entrer un motif de recherche :");
		String input = "null";
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    try {
			input = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal(e);
			System.exit(1);
		}
		final String pattern = input;
		System.out.println("##########################################################");
		System.out.println("Résultats pour : "+pattern);
		File f = new File(FilesPath.getSpriteDataDirectoryPath());
		List<File> list = FileLister.lister(f, ".png");
		for(File result : list){
			if(result.getPath().toLowerCase().contains(pattern.toLowerCase())){
				System.out.println("- "+result);
			}
		}
		showConsoleAskCommand();
	}
}
