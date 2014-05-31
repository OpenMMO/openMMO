package OpenT4C;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import t4cPlugin.FileLister;
import t4cPlugin.Params;

public class DDASprite {
	
	private static Logger logger = LogManager.getLogger(DDASprite.class.getSimpleName());
	
	final int[] clefDda = new int[]{0x1458AAAA, 0x62421234, 0xF6C32355, 0xAAAAAAF3, 0x12344321, 0xDDCCBBAA, 0xAABBCCDD};
	int[] header = new int[7];
	long taille_zip;//int
	long taille_unzip;//int
	DPDPalette palette = null;
	
	public DDASprite(ByteBuffer buf, Sprite sprite) {
		//essai de correspondance des palettes
		Iterator<DPDPalette> iter_pal = AtlasFactory.palettes.iterator();
		DPDPalette bright = null;
		while (iter_pal.hasNext()){
			DPDPalette pal = iter_pal.next();
			if (pal.nom.equals("Bright1")) bright = pal;
			String nom = sprite.getName();
			String nomPal = pal.nom;
			if (nom.length() >= nomPal.length()-1){
				//Si le nom du sprite et le nom de la palette commencent pareil, on attribue la palette au sprite
				if(nom.substring(0, nomPal.length()-1).toUpperCase().contains(nomPal.substring(0, nomPal.length()-1).toUpperCase()) && palette == null) palette = pal;
			}
		}
		//Si on a pas trouvé, on attribue la palette Bright1
		if (palette == null) palette = bright;
		sprite.palette = palette;
		ByteBuffer header_buf = ByteBuffer.allocate(7*Integer.SIZE);
		for(int i=0 ; i<7 ; i++){
			byte b1,b2,b3,b4;
			b1 = buf.get();
			b2 = buf.get();
			b3 = buf.get();
			b4 = buf.get();
			header[i] = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1}) ^ clefDda[i];
			header_buf.put((byte)((header[i]>>24)& 0xFF));
			//logger.info(""+(byte)((header[i]>>24)& 0xFF));

			header_buf.put((byte)((header[i]>>16)& 0xFF));
			//logger.info(""+(byte)((header[i]>>16)& 0xFF));

			header_buf.put((byte)((header[i]>>8)& 0xFF));
			//logger.info(""+(byte)((header[i]>>8)& 0xFF));

			header_buf.put((byte)((header[i]>>0)& 0xFF));
			//logger.info(""+(byte)((header[i]>>0)& 0xFF));

		}
		header_buf.rewind();
		//new Fast_Forward(header_buf,header_buf.capacity(),false,"Header : ");
		byte b1,b2,b3,b4;

		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.ombre = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
		//logger.info("ombre : "+ombre);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.type = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
		//logger.info("type : "+type);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.hauteur = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
		//logger.info("hauteur : "+didsprite.hauteur);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.largeur = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
		//logger.info("largeur : "+didsprite.largeur);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.offsetY = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
		//logger.info("offsetY : "+didsprite.offsetY);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.offsetX = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
		//logger.info("offsetY : "+didsprite.offsetX);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.offsetY2 = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
		//logger.info("offsetY2 : "+offsetY2+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));

		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.offsetX2 = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
		//logger.info("offsetX2 : "+offsetX2+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));

		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.couleurTrans = b2;//tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
		//logger.info("Couleur trans : "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		sprite.inconnu9 = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
		//logger.info("Inconnu : "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));

		b1 = header_buf.get();
		b2 = header_buf.get();
		b3 = header_buf.get();
		b4 = header_buf.get();			
		sprite.taille_unzip = tools.ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b1,b2,b3,b4});
		//logger.info("taille_unzip : "+taille_unzip);
		
		b1 = header_buf.get();
		b2 = header_buf.get();
		b3 = header_buf.get();
		b4 = header_buf.get();			
		sprite.taille_zip = tools.ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b1,b2,b3,b4});
		sprite.bufPos = buf.position();

		String nom = sprite.getName();
		Iterator<Integer> iter_id = sprite.id.iterator();
		while(iter_id.hasNext()){
			int id = iter_id.next();
			//logger.info("Calcul du modulo : "+sprite.nom+"@"+sprite.chemin+"{"+sprite.largeur+","+sprite.hauteur+"}");
			nom = sprite.getName();
			if ((sprite.largeur == 32) & (sprite.hauteur == 16) & (nom.contains("(")) & nom.contains(")")){
				sprite.tuile = true;
				AtlasFactory.tuiles.put(id,sprite);
				AtlasFactory.pixels.put(id,sprite);
				//System.err.println(sprite.moduloX+"|"+sprite.moduloY);
			} else {
				sprite.tuile = false;
				AtlasFactory.sprites.put(id,sprite);
				AtlasFactory.pixels.put(id,sprite);
				//System.err.println(sprite.hauteur+"|"+sprite.largeur);
			}
		}
		//logger.info("Sprite décrypté : "+sprite.chemin+" "+sprite.getName()+" "+sprite.largeur+","+sprite.hauteur+" "+sprite.moduloX+","+sprite.moduloY+" "+sprite.tuile);
	}
	
	public static void computeModulo(Sprite sprite){
		String nom = sprite.getName();
		FileLister explorer = new FileLister();
		ArrayList<File> sprites = new ArrayList<File>();
		sprites.addAll(explorer.lister(new File(Params.SPRITES+"tuiles"+File.separator+sprite.chemin+File.separator), nom.substring(nom.indexOf(')'))+".png"));
		int moduloX=1, moduloY=1;
		Iterator<File> iter = sprites.iterator();
		while (iter.hasNext()){
			File f = iter.next();
			try{
				int tmpX=1,tmpY=1;
				tmpX = Integer.parseInt(f.getName().substring(f.getName().indexOf('(')+1, f.getName().indexOf(',')));
				tmpY = Integer.parseInt(f.getName().substring(f.getName().indexOf(',')+2, f.getName().indexOf(')')));
				if (tmpX>moduloX)moduloX = tmpX;
				if (tmpY>moduloY)moduloY = tmpY;
			}catch(StringIndexOutOfBoundsException exc){
				System.err.println(nom+"=>"+f.getPath());
				System.exit(1);
			}
		}
		sprite.moduloX = moduloX;
		sprite.moduloY = moduloY;
		logger.info("Modulo : "+nom+" => "+moduloX+";"+moduloY);
	}
}
