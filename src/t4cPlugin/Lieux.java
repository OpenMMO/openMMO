package t4cPlugin;

import java.awt.Point;
import java.util.ArrayList;

public class Lieux {
	public static ArrayList<Lieux> liste = new ArrayList<Lieux>();
	private String nom = "";
	private String carte = "";
	private Point coord = new Point(0,0);
	
	public Lieux(String nom, String carte, Point coord){
		this.nom = nom;
		this.carte = carte;
		this.coord = coord;
		liste.add(this);
	}
	
	public Point getCoord(){
		return this.coord;
	}

	public String getNom() {
		return this.nom;
	}
}
