package opent4c.utils;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;

public class Place {
	private static Logger logger = LogManager.getLogger(Place.class.getSimpleName());
	public static Map<String,Place> places = new HashMap<String,Place>();
	private String nom;
	private String carte;
	private Point coord;
	
	public Place(String nom, String carte, Point coord){
		this.nom = nom;
		this.carte = carte;
		this.coord = coord;
		if (this.coord == null){
			logger.warn("On essaye d'ajouter un endroit null");
			return;
		}
		places.put(nom,this);
	}
	
	public Point getCoord(){
		return this.coord;
	}

	public Point getMapCoord(){
		return PointsManager.getPoint(this.coord.x/32, this.coord.y/16);
	}
	
	public String getNom() {
		return this.nom;
	}

	public String getMapName() {
		return this.carte;
	}
	
	public static Place getPlace(String name){
		return places.get(name);
	}
	
	public static void createDefault(){
		new Place("startpoint", "v2_worldmap",PointsManager.getPoint(2940*32,1065*16));//OK
		new Place("origin", "v2_worldmap",PointsManager.getPoint(0,0));//OK
		new Place("lh_temple", "v2_worldmap",PointsManager.getPoint(2951*32,1053*16));//OK
		new Place("wh_temple", "v2_worldmap",PointsManager.getPoint(1675*32,1183*16));//OK
		new Place("ss_temple", "v2_worldmap",PointsManager.getPoint(1562*32,2403*16));//OK
		new Place("sc_temple", "v2_worldmap",PointsManager.getPoint(210*32,675*16));//OK
		new Place("ar_rst", "v2_worldmap",PointsManager.getPoint(2327*32,727*16));//OK
		new Place("rd_rst", "v2_worldmap",PointsManager.getPoint(871*32,2085*16));//OK
		new Place("sh_rst", "v2_worldmap",PointsManager.getPoint(681*32,828*16));//OK
		new Place("ar_druides", "v2_worldmap",PointsManager.getPoint(2790*32,176*16));//OK
		new Place("rd_druides", "v2_worldmap",PointsManager.getPoint(1450*32,2868*16));//OK
		new Place("sh_zo", "v2_worldmap",PointsManager.getPoint(992*32,99*16));//OK
		new Place("worldmap", "v2_worldmap",PointsManager.getPoint(1536*32,1536*16));//OK
		new Place("underworld", "v2_underworld",PointsManager.getPoint(1536*32,1536*16));//OK
		new Place("leoworld", "v2_leoworld",PointsManager.getPoint(1536*32,1536*16));//OK
		new Place("cavernmap", "v2_cavernmap",PointsManager.getPoint(1536*32,1536*16));//OK
		new Place("dungeonmap", "v2_dungeonmap",PointsManager.getPoint(1536*32,1536*16));//OK

	}
}
