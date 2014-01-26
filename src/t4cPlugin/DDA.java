package t4cPlugin;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import javax.imageio.ImageIO;

import t4cPlugin.DPDPalette.Pixel;
import tools.BitBuffer;

public class DDA {



	  /**
	  
	  
	  
	  Decompression RLE :
Procedure DecompressionRLE(Donnees : PByte; Sprite : PStructureSprites);
  Var
    I : LongInt;
    X, Y, NbPix : Word;
    SpriteTmp : PByte;
Begin
  Y := 0;
  GetMem(SpriteTmp, Sprite^.Largeur*Sprite^.Hauteur);
  FillChar(SpriteTmp^, Sprite^.Largeur*Sprite^.Hauteur, Sprite^.CouleurTrans);
  If Donnees <> Nil Then
    While True Do
      Begin
        X := PWord(LongInt(Donnees))^;
        Inc(Donnees, 2);
        NbPix := Donnees^ * 4 + PByte(LongInt(Donnees) + 1)^;
        Inc(Donnees, 2);
        // Ombre
        If MnuAffichageOmbre.Checked And (Donnees^ = Sprite^.Ombre) Then
          Begin
            For I := 0 To NbPix - 1 Do Pbyte(LongInt(SpriteTmp) + I+x+(y*Sprite^.Largeur))^ := Sprite^.Ombre;
          End;
       // x := x + nbpix;
        If Donnees^ <> 1 Then
          Begin
            For I := 0 To NbPix - 1 Do
              Begin
                Inc(Donnees);
                Pbyte(LongInt(SpriteTmp) + I+x+(y*Sprite^.Largeur))^:=Donnees^;
                If (I + X) = Sprite^.Largeur - 1 Then Break; // Secu
              End;
          End;
        Inc(Donnees);
        If Donnees^ = 0 Then break;
        Else If Donnees^ = 1 Then Inc(Donnees);
        Else If Donnees^ = 2 Then
          Begin
            Inc(Y);
            Inc(Donnees);
          End;
        If Y = Sprite^.Hauteur Then Break;
      End;
  SetLength(Sprite^.Donnees, Sprite^.Largeur * Sprite^.Hauteur);
  Move(SpriteTmp^, Sprite^.Donnees[0], Sprite^.Largeur * Sprite^.Hauteur);
  FreeMem(SpriteTmp);
End;
 
	  

	    
	    
	    
	  StructureDda = Array Of Record
	    Signature : Array [0..3] of Byte;
	    Sprites : TFastStream;
	  End;
	  */
	private static ByteBuffer buf;
	
	private int[] clefDda = new int[]{0x1458AAAA, 0x62421234, 0xF6C32355, 0xAAAAAAF3, 0x12344321, 0xDDCCBBAA, 0xAABBCCDD};
	private byte[] signature = new byte[4];
	private int numDDA;	
	private ArrayList<Sprite> sprites = new ArrayList<Sprite>();
	
	static int nb_sprites = 0;
	static int total_sprites = 0;
	
	public void decrypt(File f, ArrayList<DIDSprite> didsprites, ArrayList<DPDPalette> dpdpalettes) {
		total_sprites = didsprites.size();
		numDDA = Integer.parseInt(f.getName().substring(f.getName().length()-6, f.getName().length()-4),10);
		
		ArrayList<DIDSprite> didspritesindda = new ArrayList<DIDSprite>();
		for(int i=0; i<didsprites.size() ; i++){
			if (didsprites.get(i).getNumDDA() == numDDA){
				didspritesindda.add(didsprites.get(i));
			}
		}
		Collections.sort(didspritesindda, new Comparator<DIDSprite>(){
			@Override
			public int compare(DIDSprite o1, DIDSprite o2) {
			       return o1.compareTo(o2);
			}
		});
		for(int i=1; i<didspritesindda.size() ; i++){
			didspritesindda.get(i-1).setIndexNext(didspritesindda.get(i).getIndexation());
		}
		didspritesindda.get(didspritesindda.size()-1).setIndexNext((int)f.length());
		buf = ByteBuffer.allocate((int)f.length());
		try {
			System.out.println("- Lecture du fichier "+f.getCanonicalPath()+" : "+(int)f.length()+" octets = "+(int)f.length()/1024+"Ko");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			DataInputStream in = new DataInputStream (new FileInputStream(f));
			while (buf.position() < buf.capacity()){
				buf.put(in.readByte());
			}
			in.close();
		}catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		buf.rewind();
		
		signature[0] = buf.get();
		signature[1] = buf.get();
		signature[2] = buf.get();
		signature[3] = buf.get();
		
		//System.out.println("	- N° DDA : "+numDDA);
		//System.out.println("	- Signature : "+tools.ByteArrayToHexString.print(signature));
		Iterator<DIDSprite> iter = didspritesindda.iterator();
		while(iter.hasNext()){
			DIDSprite sprite = iter.next();
			int indexation = sprite.getIndexation()+4;
			int index_next = sprite.getIndexNext()+4;
			int taille = index_next - indexation;
			//System.out.println("		- Taille : "+index_next+"-"+indexation+"="+taille);
			//ByteBuffer sprite_buf = ByteBuffer.allocate(taille);
			/*for (int i=0 ; i<taille ; i++){
				//TODO chelou...
				/*if (buf.position()<buf.capacity()) sprite_buf.put(buf.get());
			}
			//sprite_buf.rewind();*/
			buf.position(indexation);
			//System.out.println("	- Sprite : "+sprite.getChemin()+sprite.getNom());
			sprites.add(new Sprite(buf, sprite, taille, dpdpalettes));
		}
	}

	/**
	 * 	  PStructureSprites = ^StructureSprites;
	  StructureSprites = Record
	    Donnees : Array Of Byte; // Données du sprite
	    Case Integer Of
	      0 : (
	        TypeSprite : Word; // Type du sprite 1 : Normal; 2 : Compressé; 3 : Vide; 9 : Double Compression
	        Ombre : Word; // Ombre
	        Largeur : Word; // Largeur du sprite
	        Hauteur : Word; // Hauteur du sprite
	        OffsetX : SmallInt;
	        OffsetY : SmallInt;
	        OffsetX2 : SmallInt;
	        OffsetY2 : SmallInt;
	        Inconnu9 : Word;
	        CouleurTrans : Word;
	        NbBytes : DWord; // Nombre de bytes du sprite decompresser
	        NbBytesC : DWord // Nombre de bytes du sprite compresser
	      );
	      1 : ( BloqueDWord : Array [0..6] Of DWord );
	    End;
	 * @param buf
	 */
	
	public class Sprite{
		int[] header = new int[7];
		int type;//short
		int ombre;//short
		int largeur;//short
		int hauteur;//short
		int inconnu9;//short
		int couleurTrans;//short
		int offsetX;//short
		int offsetY;//short
		int offsetX2;//short
		int offsetY2;//short
		long taille_zip;//int
		long taille_unzip;//int
		DIDSprite didsprite = null;
		DPDPalette palette = null;
		ByteBuffer data = null;
		int taille_data;
		
		public Sprite(ByteBuffer buf, DIDSprite sprite, int taille, ArrayList<DPDPalette> palettes) {
			didsprite = sprite;
			
			//essai de correspondance des palettes
			Iterator<DPDPalette> iter_pal = palettes.iterator();
			while (iter_pal.hasNext()){
				DPDPalette pal = iter_pal.next();
				if (pal.getNom().contains(didsprite.getNom())){
					palette = pal;
				}else{
					if (pal.getNom().equals("bright1") && palette == null) palette = pal;
				}
			}
			taille_data = taille;
			ByteBuffer header_buf = ByteBuffer.allocate(7*Integer.SIZE);
			for(int i=0 ; i<7 ; i++){
				byte b1,b2,b3,b4;
				b1 = buf.get();
				b2 = buf.get();
				b3 = buf.get();
				b4 = buf.get();
				header[i] = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1}) ^ clefDda[i];
				header_buf.put((byte)((header[i]>>24)& 0xFF));
				//System.out.println(""+(byte)((header[i]>>24)& 0xFF));

				header_buf.put((byte)((header[i]>>16)& 0xFF));
				//System.out.println(""+(byte)((header[i]>>16)& 0xFF));

				header_buf.put((byte)((header[i]>>8)& 0xFF));
				//System.out.println(""+(byte)((header[i]>>8)& 0xFF));

				header_buf.put((byte)((header[i]>>0)& 0xFF));
				//System.out.println(""+(byte)((header[i]>>0)& 0xFF));

			}
			header_buf.rewind();
			
			byte b1,b2,b3,b4;

			b1 = header_buf.get();
			b2 = header_buf.get();
			ombre = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("ombre : "+ombre);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			type = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("type : "+type);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			hauteur = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("hauteur : "+hauteur);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			largeur = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("largeur : "+largeur);
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			offsetY = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
			//System.out.println("offsetY : "+offsetY+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			offsetX = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
			//System.out.println("offsetX : "+offsetX+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			offsetY2 = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
			//System.out.println("offsetY2 : "+offsetY2+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));

			b1 = header_buf.get();
			b2 = header_buf.get();
			offsetX2 = tools.ByteArrayToNumber.bytesToShort(new byte[]{b1,b2});
			//System.out.println("offsetX2 : "+offsetX2+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
			
			b1 = header_buf.get();
			b2 = header_buf.get();
			inconnu9 = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("Inconnu : "+tools.ByteArrayToHexString.print(new byte[]{b1})+" "+tools.ByteArrayToHexString.print(new byte[]{b2}));

			b1 = header_buf.get();
			b2 = header_buf.get();
			couleurTrans = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,b1,b2});
			//System.out.println("Couleur trans : "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
					
			b1 = header_buf.get();
			b2 = header_buf.get();
			b3 = header_buf.get();
			b4 = header_buf.get();			
			this.taille_zip = tools.ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b1,b2,b3,b4});
			//System.out.println("this.taille : "+this.taille);

			b1 = header_buf.get();
			b2 = header_buf.get();
			b3 = header_buf.get();
			b4 = header_buf.get();			
			taille_unzip = tools.ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b1,b2,b3,b4});
			//System.out.println("taille_unzip : "+taille_unzip);

			if ((largeur*hauteur) > 0){
				data = ByteBuffer.allocate(largeur*hauteur);
				if (data.capacity()>(buf.capacity()-buf.position())){
					System.out.println("sprite : "+didsprite.getNom());
					System.out.println("type : "+type);
					System.out.println("ombre : "+ombre);
					System.out.println("hauteur : "+hauteur);
					System.out.println("largeur : "+largeur);
					System.out.println("offsetY : "+offsetY+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
					System.out.println("offsetX : "+offsetX+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
					System.out.println("offsetY2 : "+offsetY2+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
					System.out.println("offsetX2 : "+offsetX2+" "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
					System.out.println("Inconnu : "+tools.ByteArrayToHexString.print(new byte[]{b1})+" "+tools.ByteArrayToHexString.print(new byte[]{b2}));
					System.out.println("Couleur trans : "+tools.ByteArrayToHexString.print(new byte[]{b1,b2}));
					System.out.println("taille_zip : "+taille_zip);
					System.out.println("taille_unzip : "+taille_unzip);
				}else{
					switch (type){
					case 1 : write();
							 break;
					case 2 : rleUncompress();
							 break;
					case 3 : voidSprite();
							 break;
					case 9 : unzip();
						 	 break;
					}
				}
			}
		}
		
		private void voidSprite() {
			System.out.println("			- Sprite vide : "+didsprite.getNom());			
		}

		private void write() {
			byte b1,b2,b3,b4;
			File dir = new File(Params.t4cOUT+"SPRITES/"+didsprite.getChemin());
			dir.mkdirs();
			File f = new File(Params.t4cOUT+"SPRITES/"+didsprite.getChemin()+didsprite.getNom()+".png");
			try {
				f.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			buf.get(data.array());
			data.rewind();
			BufferedImage img = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
			int y = 0, x = 0;
			while (y<hauteur){
				while (x<largeur){
					ArrayList<Pixel> pix = palette.pixels;
					int b = 0;
					try{
						b = tools.ByteArrayToNumber.bytesToInt(new byte[]{0,0,0,data.get()});
					}catch (BufferUnderflowException e){
						e.printStackTrace();
						System.out.println("				- Sprite : "+didsprite.getNom());			
						System.out.println("				- type : "+type);
						System.out.println("				- ombre : "+ombre);
						System.out.println("				- hauteur : "+hauteur);
						System.out.println("				- largeur : "+largeur);
						System.out.println("				- offsetY : "+offsetY);
						System.out.println("				- offsetX : "+offsetX);
						System.out.println("				- offsetY2 : "+offsetY2);
						System.out.println("				- offsetX2 : "+offsetX2);
						System.out.println("				- Inconnu : "+inconnu9);
						System.out.println("				- Couleur trans : "+couleurTrans);
						System.out.println("				- taille_zip : "+taille_zip);
						System.out.println("				- taille_unzip : "+taille_unzip);
						return;
					}
					Pixel px = null;
					px = pix.get(b);
					int red=0,green=0,blue=0,alpha=1;
					if (b == couleurTrans-1) alpha = 0;
					red = px.red;
					green = px.green;
					blue = px.blue;
					int col = (alpha << 24) | (red << 16) | (green << 8) | blue;
					img.setRGB(x,y,col);
					x++;
					//System.out.println("	- Pixel : "+x+","+y+" : "+alpha+" "+red+" "+green+" "+blue);
				}
				y++;
				x = 0;
			}
			try {
				ImageIO.write(img, "png", f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DDA.nb_sprites++;
			System.out.println("			- Sprite écrit : "+Params.t4cOUT+"SPRITES/"+didsprite.getChemin()+didsprite.getNom()+".png | Palette : "+palette.getNom());			
		}


		private void rleUncompress() {
			// TODO Auto-generated method stub
			
		}


		private void unzip() {
			// TODO Auto-generated method stub
			
		}
		
	}

	
}
