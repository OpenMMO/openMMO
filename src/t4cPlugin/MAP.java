package t4cPlugin;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import opent4c.utils.FilesPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tools.DataInputManager;


public class MAP {
	
	private static Logger logger = LogManager.getLogger(MAP.class.getSimpleName());
	
	HashMap<Integer,byte[]> blocs = new HashMap<Integer, byte[]>();
	ArrayList<Integer> adresses = new ArrayList<Integer>();
	final int nb_blocs = 24 * 24;
	final int bloc_size = 128 * 128;
	ByteBuffer map_data, image_data, buf_unpacked;

	
	public void Map_load_block(File f, int rle_value){
		logger.info("	- Décryptage de la carte "+f.getName());
		File decryptedMap = new File(FilesPath.getMapFilePath(f.getName()));
		int block_number;
		int offset;
		int line_number;
		int pixel_address;
		ByteBuffer raw_data, tmp_unpack,image_data;
		byte b1,b2,b3,b4;
		
		image_data = ByteBuffer.allocate(3072*3072*2);
		tmp_unpack = ByteBuffer.allocate(128 * 128 * Short.SIZE);
		raw_data = ByteBuffer.allocate((int)f.length());
		try {
			DataInputManager in = new DataInputManager (f);
			while (raw_data.position() < (int)f.length()){
				raw_data.put(in.readByte());
			}
			in.close();
		}catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		raw_data.rewind();
		
		for (block_number = 0 ; block_number < 24 * 24 ; block_number++){
			raw_data.position(block_number*4);
			b1=raw_data.get();
			b2=raw_data.get();
			b3=raw_data.get();
			b4=raw_data.get();
			offset = tools.ByteArrayToNumber.bytesToInt(new  byte[]{b4,b3,b2,b1});
			raw_data.position(offset);
			RLE_UncompressBlock(raw_data, tmp_unpack, rle_value);
			
			for (line_number=0 ; line_number<128 ; line_number++){
				pixel_address = (block_number % 24) * 128 * 2 + ((block_number / 24) * 3072 * 2 * 128) + line_number * 3072 * 2;
				for (int j=0 ; j< 128*2 ; j++){
					image_data.array()[pixel_address+j] = tmp_unpack.array()[j + line_number * 128 * 2];
					//System.err.println(image_data.array()[pixel_address+j]);
				}
			}
			tmp_unpack.clear();
		}
		//Écriture de la Map
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(decryptedMap));
			out.write(image_data.array());
			out.close();
			Params.nb_map++;
			//logger.info("");
		}catch(IOException exc){
			System.err.println("Erreur I/O");
			exc.printStackTrace();
		}
		//Map_Write(image_data, f.getName());
	}
	
	private void RLE_UncompressBlock(ByteBuffer packed_data, ByteBuffer unpacked_data, int rle_value) {
		int val;
		int i = 0;
		int start_offset;
		byte b1,b2;
		
		start_offset = unpacked_data.position();
		while ((unpacked_data.position()-start_offset) < (128*128*2)){
			b1 = packed_data.get();
			b2 = packed_data.get();
			val = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
			if (val < rle_value ){
				unpacked_data.put(b1);
				unpacked_data.put(b2);
			} else {
				b1 = packed_data.get();
				b2 = packed_data.get();
				
				for (i = 0; i < val - rle_value; i++){
					unpacked_data.put(b1);
					unpacked_data.put(b2);
				}
			}
		}
		
	}

	public void Map_Write(ByteBuffer map_data, String name) {
		boolean trouve = false;
		int scale = 1;
		int w = 32/scale;
		int h = 16/scale;
		map_data.rewind();
		int valprecedente = -1;
		int valactuelle;
		byte b1,b2;
		BufferedImage blackTile = null;

		//TODO on stock le sprite black mais on get le 0? Pourquoi?
		Sprite black = DID.getSprites_with_ids().get(0);
		try {
			blackTile = ImageIO.read(new File(Params.t4cOUT+"SPRITES/"+black.getChemin()+black.getName()+".bmp"));
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}

		/*
		 * On parcourt toute la carte, et on détermine à quelle position quel sprite est affiché,
		 * on stocke ça dans les sprites. au passage on crée une liste des sprites trouvés dans la map.
		 */
		ArrayList<Sprite> sprites_in_map = new ArrayList<Sprite>();
		Sprite sp = null;
		while (map_data.position() < map_data.capacity()){
			b1 = map_data.get();
			b2 = map_data.get();
			int pos = (map_data.position()-2)/2;
			valactuelle = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});//On récupère l'id de la prochaine tuile sur la carte
			sp = DID.getSprites_with_ids().get(valactuelle);//on récupère le sprite associé à cet ID
			if (sp == null){
				sp = black;
			}
			//logger.info(sp.nom);
			if (valprecedente != valactuelle){//si elle est différente de la précédente
				if(sprites_in_map.size() != 0){//si notre liste de sprite utilisés n'est pas vide
					Iterator<Sprite> iter_spritemap = sprites_in_map.iterator();
					while (iter_spritemap.hasNext() & !trouve){//on cherche si on a déjà utilisé ce même sprite sur cette map
						Sprite s = iter_spritemap.next();
						if (sp.getName().equals(s.getName())){//si oui
							s.pos.add(pos);//on ajoute la position à la liste de positions du sprite.
							trouve = true;//on dit qu'on a trouvé
						}
					}
					if (!trouve){//si jamais on a jamais utilisé ce sprite
						sp.pos.add(pos);//on ajoute la position au sprite
						sprites_in_map.add(sp);//on ajoute le sprite à la liste
						//logger.info(sp.nom+" "+sprites_in_map.size());
					}
					trouve = false;//on réinitialise pour pouvoir faire une nouvelle recherche.
				}else{//si la liste de sprite utilisés est vide
					sp.pos.add(pos);//on ajoute la position au sprite
					sprites_in_map.add(sp);//on ajoute le sprite à la liste
				}
				
			}else {//si c'est la même id que la précedente
					sp.pos.add(pos);//on ajoute la position
			}
			valprecedente = valactuelle;//l' id actuelle devient l'idée , et on recommence!
		}
		map_data.rewind();
		
		/*
		 * Ici, il faut délimiter le rendu à une zone, sinon on s'en sort pas, genre une ville ça doit paser.
		 * le mieux c'est de pouvoir travailler avec les coordonnées de la carte de colision, donc des entiers X1/X2/Y1/Y2 compris entre 0 et 3072
		 */
		//3RD->leoworld
		/*int X1 = 2965;
		int Y1 = 2810;
		int X2 = 3025;
		int Y2 = 2870;*/
		
		//LH->world
		/*int X1 = 2711;
		int Y1 = 841;
		int X2 = 3071;
		int Y2 = 1278;*/
		
		int X1 = 2923;
		int Y1 = 1029;
		int X2 = 2974;
		int Y2 = 1080;
		
		if (X1 < 0) X1=0;
		if (X1 > 3070) X1=3070;
		if (Y1 < 0) Y1=0;
		if (Y1 > 3070) Y1=3070;
		if (X2 < 1) X2=1;
		if (X2 > 3071) X2=3071;
		if (Y2 < 1) Y2=1;
		if (Y2 > 3071) Y2=3071;
		int tmp = X1;
		if(tmp>X2){
			X1=X2;
			X2=tmp;
		}
		tmp = Y1;
		if(tmp>Y2){
			Y1=Y2;
			Y2=tmp;
		}
		
		BufferedImage combined = new BufferedImage((X2-X1)*w, (Y2-Y1)*h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = combined.getGraphics();
				
		Sprite sprite = null;
		/*
		 * Ensuite on parcourt la liste de sprites présents dans la map et
		 * on les dessine autant de fois qu'ils sont présents, à leur place.
		 */
		BufferedImage overlay = null;
		int currentTile=0;
		int lastTile=-1000000;
		Iterator<Sprite> iter_sprite = sprites_in_map.iterator();
		while(iter_sprite.hasNext()){
			sprite = iter_sprite.next();
			//logger.info(sprite.nom);
			Iterator<Integer> iter_pos = sprite.pos.iterator();
			while (iter_pos.hasNext()){
				int pos = iter_pos.next();
				if((pos%3072 > X1)&(pos%3072 < X2)&((pos/3072)>Y1)&((pos/3072)<Y2)){
					File f=null;
					String nom = sprite.getName();
					if(nom.contains("(") & nom.contains(",")){
						//logger.info(sprite.nom);
						f = checkZone(sprite, pos);
					}else{
						f = new File(Params.t4cOUT+"SPRITES"+File.separator+sprite.getChemin()+sprite.getName()+".png");
					}
					try {
						overlay = ImageIO.read(f);

					} catch (IOException ex) {
						f = new File(Params.t4cOUT+"SPRITES"+File.separator+sprite.getChemin()+sprite.getName()+".png");
						//logger.info(sprite.chemin+sprite.nom+" | "+f.getName());
						try {
							overlay = ImageIO.read(f);

						} catch (IOException exc) {
							overlay = blackTile;
						}
					}
					if (overlay == null){
						System.err.println("Overlay null");
						//System.err.println(sprite.chemin+f.getName());
						System.exit(1);
					}
					//logger.info(sprite.chemin+sprite.nom);
					if (((currentTile*100)/((X2-X1)*(Y2-Y1))) != ((lastTile*100)/((X2-X1)*(Y2-Y1)))){
						logger.info(((currentTile*100)/((X2-X1)*(Y2-Y1)))+"% des tuiles dessinée(s)");
					}
					if ((overlay.getWidth() == 32) & (overlay.getHeight() == 16)) {
						g.drawImage(overlay, ((pos%3072)-X1)*w, ((pos/3072)-Y1)*h, overlay.getWidth()/scale, overlay.getHeight()/scale, null);
					}
					lastTile=currentTile;
					currentTile++;
				}
			}
			sprite = null;
		}
		currentTile = 0;
		lastTile=-1000000;
		iter_sprite = sprites_in_map.iterator();
		while(iter_sprite.hasNext()){
			sprite = iter_sprite.next();
			//logger.info(sprite.nom);
			Iterator<Integer> iter_pos = sprite.pos.iterator();
			while (iter_pos.hasNext()){
				int pos = iter_pos.next();
				if((pos%3072 > X1)&(pos%3072 < X2)&((pos/3072)>Y1)&((pos/3072)<Y2)){
					File f=null;
					String nom = sprite.getName();
					if(nom.contains("(") & nom.contains(",")){
						//logger.info(sprite.nom);
						f = checkZone(sprite, pos);
					}else{
						f = new File(Params.t4cOUT+"SPRITES"+File.separator+sprite.getChemin()+nom+".png");
					}
					try {
						overlay = ImageIO.read(f);

					} catch (IOException ex) {
						f = new File(Params.t4cOUT+"SPRITES"+File.separator+sprite.getChemin()+nom+".png");
						//logger.info(sprite.chemin+sprite.nom+" | "+f.getName());
						try {
							overlay = ImageIO.read(f);

						} catch (IOException exc) {
							overlay = blackTile;
						}
					}
					if (overlay == null){
						System.err.println("Overlay null");
						System.err.println(sprite.getChemin()+f.getName());
						System.exit(1);
					}
					//logger.info(sprite.chemin+sprite.nom);
					if (((currentTile*100)/((X2-X1)*(Y2-Y1))) != ((lastTile*100)/((X2-X1)*(Y2-Y1)))){
						logger.info(((currentTile*100)/((X2-X1)*(Y2-Y1)))+"% des Sprites dessiné(s)");
					}
					if (!(overlay.getWidth() == 32) | !(overlay.getHeight() == 16)) {
						g.drawImage(overlay, (((pos%3072)-X1)*w)+sprite.getOffsetX(), (((pos/3072)-Y1)*h)+sprite.getOffsetY(), overlay.getWidth()/scale, overlay.getHeight()/scale, null);
					}
					lastTile=currentTile;
					currentTile++;
				}
			}
			sprite = null;
		}
		currentTile = 0;
		lastTile=-1000000;
		iter_sprite = sprites_in_map.iterator();
		while(iter_sprite.hasNext()){
			sprite = iter_sprite.next();
			//logger.info(sprite.nom);
			Iterator<Integer> iter_pos = sprite.pos.iterator();
			while (iter_pos.hasNext()){
				int pos = iter_pos.next();
				if((pos%3072 > X1)&(pos%3072 < X2)&((pos/3072)>Y1)&((pos/3072)<Y2)){
					File f=null;
					String nom = sprite.getName();
					if(nom.contains("(") & nom.contains(",")){
						//logger.info(sprite.nom);
						f = checkZone(sprite, pos);
					}else{
						f = new File(Params.t4cOUT+"SPRITES"+File.separator+sprite.getChemin()+nom+".png");
					}
					try {
						overlay = ImageIO.read(f);

					} catch (IOException ex) {
						f = new File(Params.t4cOUT+"SPRITES"+File.separator+sprite.getChemin()+nom+".png");
						//logger.info(sprite.chemin+sprite.nom+" | "+f.getName());
						try {
							overlay = ImageIO.read(f);

						} catch (IOException exc) {
							overlay = blackTile;
						}
					}
					if (overlay == null){
						System.err.println("Overlay null");
						System.err.println(sprite.getChemin()+f.getName());
						System.exit(1);
					}
					//logger.info(sprite.chemin+sprite.nom);
					if (((currentTile*100)/((X2-X1)*(Y2-Y1))) != ((lastTile*100)/((X2-X1)*(Y2-Y1)))){
						//logger.info(((currentTile*100)/((X2-X1)*(Y2-Y1)))+"% des ID dessiné(s)");
					}
					if (!(overlay.getWidth() == 32) | !(overlay.getHeight() == 16)) {
						map_data.position(pos*2);
						b1=map_data.get();
						b2=map_data.get();
						int id = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b2,b1});
						g.setColor(Color.RED);
						g.drawString(""+id, (((pos%3072)-X1)*w), (((pos/3072)-Y1)*h)+16);
						logger.info(((((pos%3072)-X1)*w)+sprite.getOffsetX())+","+((((pos/3072)-Y1)*h)+sprite.getOffsetY())+" : "+sprite.getName()+" "+id+" => "+sprite.id+" | "+sprite.getChemin());
					}
					lastTile=currentTile;
					currentTile++;
				}
			}
			sprite = null;
		}

		logger.info("	- Écriture de l'image : "+Params.t4cOUT+"MAPS/"+name.substring(0,name.length()-4)+"."+X1+"."+Y1+"."+X2+"."+Y2+".png");
		try {
			File f = new File(Params.t4cOUT+"MAPS"+File.separator+name.substring(0,name.length()-4)+"."+X1+"."+Y1+"."+X2+"."+Y2+".png");
			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
			ImageWriter writer = (ImageWriter)iter.next();
			ImageWriteParam iwp = writer.getDefaultWriteParam();
			FileImageOutputStream output = new FileImageOutputStream(f);
			writer.setOutput(output);
			IIOImage image = new IIOImage(combined, null, null);
			writer.write(null, image, iwp);
			writer.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("	- Fichier MAP "+Params.t4cOUT+"MAPS/"+name.substring(0,name.length()-4)+"."+X1+"."+Y1+"."+X2+"."+Y2+".png"+" écrit.");
		combined = null;
		g = null;
	}
	
	private File checkZone(Sprite sprite, int pos) {
		FileLister explorer = new FileLister();
		ArrayList<File> sprites = new ArrayList<File>();
		sprites.addAll(explorer.lister(new File(Params.t4cOUT+"SPRITES"+File.separator+sprite.getChemin()), ".png"));
		int moduloX=0, moduloY=0;
		Iterator<File> iter = sprites.iterator();
		while (iter.hasNext()){
			File f = iter.next();
			String nom = sprite.getName();
			if (f.getName().contains(nom.substring(0, nom.indexOf('(')))){
				int tmpX=0,tmpY=0;
				tmpX = Integer.parseInt(f.getName().substring(f.getName().indexOf('(')+1, f.getName().indexOf(',')));
				tmpY = Integer.parseInt(f.getName().substring(f.getName().indexOf(',')+2, f.getName().indexOf(')')));
				if (tmpX>moduloX)moduloX = tmpX;
				if (tmpY>moduloY)moduloY = tmpY;	
			}
		}
		File result = null;
		int x = (pos%moduloX)+1;
		int y = ((pos/3072)%moduloY)+1;
		
		result = new File(Params.t4cOUT+"SPRITES"+File.separator+sprite.getChemin()+sprite.getName().substring(0,sprite.getName().indexOf('(')+1)+x+", "+y+").bmp");
		return result;
		
	}	
}