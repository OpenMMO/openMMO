package t4cPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import tools.DataInputManager;


/**
 * pt_pack=data;
x=y=0;

while(1)
{
   // Index du premier pixel à decompresser
   x=*(short *)pt_pack;
   pt_pack+=2;

   // Nombre de pixels à decompresser
   nb_pix=pt_pack[0]*4+pt_pack[1];
   pt_pack+=2;

   pt_dpack=&tmp_sprite[x+(y*lg)];

   if (*pt_pack++ != 1)
   {
      for (i=0; i<nb_pix; i++)
         pt_dpack[i]=*pt_pack++;
   }
   else
      x+=nb_pix;

   if (*pt_pack == 0)   // Fin de sprite
      break;
   if (*pt_pack++ == 2) // Fin de ligne
      y++;

   if (y==ht)           // Par sécurité
      break;
}; 


typedef struct
{
   unsigned char  type;
   unsigned int   index;
   unsigned int   parent;
   unsigned short lg_nom;
   char           nom[];
} VSFFOLDER, *LPVSFFOLDER;

typedef struct
{
   unsigned char  type;
   unsigned int   offset;
   unsigned short lg_nom;
   char           nom[];
} VSFFILE, *LPVSFFILE;


typedef struct
{
   unsigned long id_folder;
   union
   {
      unsigned char type_pack;
      unsigned char pal_datas;
   };
   unsigned char  dummy1;
   unsigned short lg_sprite;
   unsigned short ht_sprite;
   short          offset_x;
   short          offset_y;
   short          offset_x2;
   short          offset_y2;
   unsigned char  dummy2[2];
   unsigned int   flag_floor;
   unsigned char  color_key;
   unsigned char  dummy3[3];
   unsigned int   lg_datas;
   unsigned char  datas;
} VSFOBJECT, *LPVSFOBJECT;

 * @author synoga
 *
 */
public class VSB {
	
	private ByteBuffer buf;
	
	private int unLong;
	private int nb_chunks;
	
	public void decrypt(File f){
		int size = (int) f.length();
		buf = ByteBuffer.allocate(size);
		try {
			System.out.println("- Lecture du fichier "+f.getCanonicalPath()+" : "+(int)f.length()+" octets = "+(int)f.length()/1024+"Ko");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			DataInputManager in = new DataInputManager (f);
			while (buf.position() < (int)f.length()){
				buf.put(in.readByte());
			}
			in.close();
		}catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		//On commence par décrypter le fichier avec la clé
		for (int i=0; i<buf.capacity(); i++){
			   buf.array()[i] ^= VsfCryptKey.key[(i / 4096) & 0xfff];
		}
		buf.rewind();
		
		//Ensuite on peut décoder les chunks
		byte b1,b2,b3,b4;
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		unLong = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("	- Un Long : "+unLong);
		
		b1 = buf.get();
		b2 = buf.get();
		b3 = buf.get();
		b4 = buf.get();
		nb_chunks = tools.ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		//System.out.println("	- Nombre de chunks : "+nb_chunks);

	}
}
