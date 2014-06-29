/**
 * 
 */
package opent4c;

import java.awt.Point;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opent4c.utils.PointsManager;

/**
 * @author synoga
 *
 */
public class TilePixel extends MapPixel{
	
	private static Logger logger = LogManager.getLogger(TilePixel.class);
	
	private Point modulo = PointsManager.getPoint(1, 1);
	
	
	/**
	 * @param id
	 * @param atlas
	 * @param tex
	 * @param type
	 * @param ombre
	 * @param transColor
	 * @param offset
	 * @param offset2
	 * @param numDDA
	 * @param palette
	 * @param perfectMatch
	 */
	public TilePixel(int id, String atlas, String tex, int type, int ombre,
			int transColor, Point offset, Point offset2,
			int numDDA, String palette, boolean perfectMatch) {
		super(id, atlas, tex, type, ombre, PointsManager.getPoint(32, 16), transColor, offset, offset2, numDDA, palette, perfectMatch);
	}

	/**
	 * @param pixel
	 */
	public TilePixel(MapPixel pixel) {
		super(pixel.getId(),pixel.getAtlas(),pixel.getTex(),pixel.getType(),pixel.getOmbre(),PointsManager.getPoint(32, 16),pixel.getCouleurTrans(),pixel.getOffset(),pixel.getOffset2(),pixel.getNumDDA(), pixel.getPaletteName(), pixel.isPerfectMatch());
	}

	public Point getModulo() {
		return modulo;
	}

	public void setModulo(Point modulo) {
		this.modulo = modulo;
	}
	
	public void printInfos(){
		logger.info("===============================");
		logger.info("TILE PIXEL INFO");
		logger.info("Nom : "+getTex());
		logger.info("Atlas : "+getAtlas());
		logger.info("ID : "+getId());
		logger.info("Largeur : "+getLargeur());
		logger.info("Hauteur : "+getHauteur());
		logger.info("OffsetX : "+getOffset().x);
		logger.info("OffsetY : "+getOffset().y);
		logger.info("OffsetX2 : "+getOffset2().x);
		logger.info("OffsetY2 : "+getOffset2().y);
		logger.info("Palette : "+getPaletteName());
		logger.info("Buffer Position : "+getBufPos());
		logger.info("Couleur Transparence : "+getCouleurTrans());
		logger.info("Inconnu9 : "+getInconnu9());
		logger.info("Indexation : "+getIndexation());
		logger.info("ModuloX : "+getModulo().x);
		logger.info("ModuloY : "+getModulo().y);
		logger.info("Num DDA : "+getNumDDA());
		logger.info("Ombre : "+getOmbre());
		logger.info("Taille unzip : "+getTaille_unzip());
		logger.info("Taille zip : "+getTaille_zip());
		logger.info("Type : "+getType());
		logger.info("===============================");
	}

}
