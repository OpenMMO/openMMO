/**
 * 
 */
package opent4c;

import java.awt.Point;

import opent4c.utils.PointsManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author synoga
 *
 */
public class SpritePixel extends MapPixel{
	
	private static Logger logger = LogManager.getLogger(SpritePixel.class);
	
	private boolean flipX = false;

	
	/**
	 * @param id
	 * @param tuile
	 * @param atlas
	 * @param tex
	 * @param type
	 * @param ombre
	 * @param taille
	 * @param transColor
	 * @param offset
	 * @param offset2
	 * @param numDDA
	 * @param palette
	 * @param perfectMatch
	 */
	public SpritePixel(int id, String atlas, String tex,
			int type, int ombre, Point taille, int transColor, Point offset,
			Point offset2, int numDDA, String palette, boolean perfectMatch) {
		super(id,atlas,tex,type,ombre,taille,transColor,offset,offset2,numDDA, palette, perfectMatch);
	}

	/**
	 * @param pixel
	 */
	public SpritePixel(MapPixel pixel) {
		super(pixel.getId(),pixel.getAtlas(),pixel.getTex(),pixel.getType(),pixel.getOmbre(),PointsManager.getPoint(pixel.getLargeur(), pixel.getHauteur()),pixel.getCouleurTrans(),pixel.getOffset(),pixel.getOffset2(),pixel.getNumDDA(), pixel.getPaletteName(), pixel.isPerfectMatch());
	}

	public boolean isFlipX() {
		return flipX;
	}

	public void setFlipX(boolean flipX) {
		this.flipX = flipX;
	}
	

	


	public void printInfos(){
		logger.info("===============================");
		logger.info("SPRITE PIXEL INFO");
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
		logger.info("Num DDA : "+getNumDDA());
		logger.info("Ombre : "+getOmbre());
		logger.info("Taille unzip : "+getTaille_unzip());
		logger.info("Taille zip : "+getTaille_zip());
		logger.info("Type : "+getType());
		logger.info("===============================");
	}


	
}
