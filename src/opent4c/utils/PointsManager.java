package opent4c.utils;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class PointsManager {

	private static int xMax = 3072;
	private static int yMax = 3072;
	private static Map<Integer, Point> points = new HashMap<Integer, Point>(xMax * yMax);
	private static Map<Integer, Point> xNegPoints = new HashMap<Integer, Point>();
	private static Map<Integer, Point> yNegPoints = new HashMap<Integer, Point>();
	private static Map<Integer, Point> NegPoints = new HashMap<Integer, Point>();
	
	private PointsManager() {
		//classe utilitaire
	}
	
	/**
	 * @param x
	 * @param y
	 * @return Le point ayant pour coordonnées x et y. 
	 */
	public static Point getPoint(int x, int y) {
		//On récupère la liste contenant les coordonnées voulues
		Map<Integer, Point> mapPoints = getMapPoints(x, y);
		//Puis le point voulu dans la dite liste
		Point point = getPointIn(mapPoints, x, y);
		return point;
	}
	
	private static Map<Integer, Point> getMapPoints(int x, int y) {
		if (x>=0) {
			if(y>=0) {
				//Liste des points positifs
				return points;
			}
			else {
				//Liste des points avec x positif mais y négatif
				return yNegPoints;
			}
		}
		else {
			if(y>=0) {
				//Liste des points avec x négatif et y positif
				return xNegPoints;
			}
			else {
				//Liste des points avec coordonnées négatives
				return NegPoints;
			}
		}
	}
	
	private static Point getPointIn(Map<Integer, Point> listePoints, int x, int y) {
		int index = getIndexPoint(x, y);
		Point point = listePoints.get(index);
		if (point == null) {
			//Point non trouvé, on le créé et on le stocke
			point = new Point(x,y);
			listePoints.put(index, point);
		}
		return point;
	}
	
	private static int getIndexPoint(int x, int y) {
		//les index ne se basent que sur les valeurs absolues, plus simples à gérer
		x = Math.abs(x);
		y = Math.abs(y);
		
		int index = x * xMax + y;
		
		return index;
	}

	public static Point getPoint(float x, float y) {
		Point p = getPoint((int)x,(int)y);
		return p;
	}
}
