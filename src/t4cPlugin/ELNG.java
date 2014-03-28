package t4cPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;

import tools.DataInputManager;


/**
 * On décrypte ici les fichiers de langage.
 * A terme, on pourra formater le langage,
 * par exemple en XML ou JSON, pour y accéder plus facilement.
 * @author synoga
 *
 */
public class ELNG {	
	public void decrypt (File f){
		//System.out.println("	- Décryptage du fichier ELNG : "+f.getName());
		CharBuffer buf = CharBuffer.allocate((int)f.length());
		try {
			DataInputManager in = new DataInputManager (f);
			for(int index = 0 ; index<f.length(); index++){
				int character = (in.read() ^ ElngCryptKey.key[index%7823]);
				buf.put((char)character);
			}
			in.close();
		}
		catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		try {
			OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(Params.t4cOUT+"ELNG/"+f.getName()+".txt"),Params.CHARSET);
			pw.write(buf.array());
			pw.close();
		}
		catch(IOException exc){
			System.err.println("Erreur I/O");
			exc.printStackTrace();
			Iterator<String> iter = Charset.availableCharsets().keySet().iterator();
			while (iter.hasNext()){
				System.err.println(iter.next());
			}
		}
		System.out.println("	- Fichier "+Params.t4cOUT+"ELNG/"+f.getName()+" écrit.");
		Params.nb_elng++;
		//System.out.println("");
	}
}
