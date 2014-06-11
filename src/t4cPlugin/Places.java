package t4cPlugin;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;

import t4cPlugin.utils.PointsManager;

public class Places {
	public static Map<String,Places> places = new HashMap<String,Places>();
	private String nom = "";
	private String carte = "";
	private Point coord = PointsManager.getPoint(0,0);
	
	public Places(String nom, String carte, Point coord){
		this.nom = nom;
		this.carte = carte;
		this.coord = coord;
		places.put(nom,this);
	}
	
	public Point getCoord(){
		return this.coord;
	}

	public String getNom() {
		return this.nom;
	}

	public String getMap() {
		return this.carte;
	}
	
	public static Places getPlace(String name){
		return places.get(name);
	}
	
	public static void createDefault(){
		new Places("startpoint", "v2_worldmap",PointsManager.getPoint(2940,1065));
		new Places("origin", "v2_worldmap",PointsManager.getPoint(Gdx.graphics.getWidth()/64,Gdx.graphics.getHeight()/32));
	}
}
