package t4cPlugin;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import opent4c.utils.PointsManager;

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
		new Places("startpoint", "v2_worldmap",PointsManager.getPoint(2940,1065));//OK
		new Places("origin", "v2_worldmap",PointsManager.getPoint(Gdx.graphics.getWidth()/64,Gdx.graphics.getHeight()/32));//OK
		new Places("lh_temple", "v2_worldmap",PointsManager.getPoint(2951,1053));//OK
		new Places("wh_temple", "v2_worldmap",PointsManager.getPoint(1675,1183));//OK
		new Places("ss_temple", "v2_worldmap",PointsManager.getPoint(1562,2403));//OK
		new Places("sc_temple", "v2_worldmap",PointsManager.getPoint(210,675));//OK
		new Places("ar_rst", "v2_worldmap",PointsManager.getPoint(2327,727));//OK
		new Places("rd_rst", "v2_worldmap",PointsManager.getPoint(871,2085));//OK
		new Places("sh_rst", "v2_worldmap",PointsManager.getPoint(681,828));//OK
		new Places("ar_druides", "v2_worldmap",PointsManager.getPoint(2790,176));//OK
		new Places("rd_druides", "v2_worldmap",PointsManager.getPoint(1450,2868));//OK
		new Places("sh_zo", "v2_worldmap",PointsManager.getPoint(992,99));//OK
		new Places("center", "v2_worldmap",PointsManager.getPoint(1536,1536));//OK

	}
}
