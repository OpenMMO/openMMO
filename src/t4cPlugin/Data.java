package t4cPlugin;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Data {
	static ArrayList<File> did = new ArrayList<File>();
	static ArrayList<File> map = new ArrayList<File>();
	static ArrayList<File> dda = new ArrayList<File>();
	static ArrayList<File> dpd = new ArrayList<File>();
	static ArrayList<File> decrypted = new ArrayList<File>();
	//static HashMap<Integer,String> ids = new HashMap<Integer,String>(); //Pour stocker la liste ID -> sprites
	static HashMap<Point, Sprite> cases_sprites = new HashMap<Point, Sprite>(); // Pour piocher les Sprites à afficher
	//static HashMap<Dimension, t4cPlugin.Sprite> pixels = new HashMap<Dimension, t4cPlugin.Sprite>(); //Pour trouver des infos à partir de coordonnées
	//static HashMap<Integer,t4cPlugin.Sprite> idlist = new HashMap<Integer, t4cPlugin.Sprite>(); // Pour trouver des infos à partir d'une ID
	//static ArrayList<t4cPlugin.Sprite> noidlist = new ArrayList<t4cPlugin.Sprite>(); // Pour lister les Sprites sans id
	//static HashMap<Dimension, Integer> inconnues = new HashMap<Dimension, Integer>();
}
