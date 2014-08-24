package opent4c.utils;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;

public class Places {
	private static Logger logger = LogManager.getLogger(Places.class.getSimpleName());
	public static Map<String,Places> places = new HashMap<String,Places>();
	private String nom;
	private String carte;
	private Point coord;
	
	public Places(String nom, String carte, Point coord){
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
	
	public static Places getPlace(String name){
		return places.get(name);
	}
	
	public static void createDefault(){
		new Places("startpoint", "v2_worldmap",PointsManager.getPoint(2940*32,1065*16));//OK
		new Places("origin", "v2_worldmap",PointsManager.getPoint(0,0));//OK
		new Places("lh_temple", "v2_worldmap",PointsManager.getPoint(2951*32,1053*16));//OK
		new Places("wh_temple", "v2_worldmap",PointsManager.getPoint(1675*32,1183*16));//OK
		new Places("ss_temple", "v2_worldmap",PointsManager.getPoint(1562*32,2403*16));//OK
		new Places("sc_temple", "v2_worldmap",PointsManager.getPoint(210*32,675*16));//OK
		new Places("ar_rst", "v2_worldmap",PointsManager.getPoint(2327*32,727*16));//OK
		new Places("rd_rst", "v2_worldmap",PointsManager.getPoint(871*32,2085*16));//OK
		new Places("sh_rst", "v2_worldmap",PointsManager.getPoint(681*32,828*16));//OK
		new Places("ar_druides", "v2_worldmap",PointsManager.getPoint(2790*32,176*16));//OK
		new Places("rd_druides", "v2_worldmap",PointsManager.getPoint(1450*32,2868*16));//OK
		new Places("sh_zo", "v2_worldmap",PointsManager.getPoint(992*32,99*16));//OK
		new Places("center", "v2_worldmap",PointsManager.getPoint(1536*32,1536*16));//OK

	}
}
