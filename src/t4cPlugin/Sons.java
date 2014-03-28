package t4cPlugin;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import tools.ByteArrayToNumber;
import tools.DataInputManager;

public class Sons {
	
	ByteBuffer buf_snmci = null;
	ByteBuffer buf_snmcd = null;
	ByteBuffer buf_snmcf = null;
	
	static boolean ready = false;
	
	static ArrayList<SoundListInfo> soundList = new ArrayList<SoundListInfo>();
	
	long nbsound = 0;
	
	public void decrypt_index(File snmci_){
		//On commence par lire le fichier d'index snmci._
		System.out.println("	- Lecture de l'index des sons.");
		buf_snmci = ByteBuffer.allocate((int)snmci_.length());
		try {
			DataInputManager in = new DataInputManager(snmci_);
			for(long index = 0 ; index<snmci_.length(); index++){
				buf_snmci.put(in.readByte());
			}
			in.close();
		}
		catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		buf_snmci.rewind();

		//On récupère le nombre de sons à extraire.
		byte b1, b2, b3, b4;
		b1 = buf_snmci.get();
		b2 = buf_snmci.get();
		b3 = buf_snmci.get();
		b4 = buf_snmci.get();
		nbsound = ByteArrayToNumber.bytesToInt(new byte[]{b4,b3,b2,b1});
		Params.total_sons = nbsound;
		//System.out.println("		- Nombre de sons : "+nbsound);
		
		for (int i=0 ; i < nbsound; i++){
			//System.out.println("	- Son "+i+" :");
			soundList.add(new SoundListInfo());	
			soundList.get(i).taille_name = buf_snmci.get();
			//System.out.println("		- Longueur Du Nom : "+soundList.get(i).taille_name);
			soundList.get(i).name = new char[soundList.get(i).taille_name];
			for(int j=0 ; j<soundList.get(i).taille_name ; j++){
				soundList.get(i).name[j] = (char)buf_snmci.get();
			}
			//System.out.println("		- Nom : "+new String(soundList.get(i).name));
			
			b1 = buf_snmci.get();
			b2 = buf_snmci.get();
			b3 = buf_snmci.get();
			b4 = buf_snmci.get();
			soundList.get(i).start = ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b4,b3,b2,b1});
			//System.out.println("		- Start : "+soundList.get(i).start+" o/"+soundList.get(i).start/1024+" Ko");
			
			b1 = buf_snmci.get();
			b2 = buf_snmci.get();
			b3 = buf_snmci.get();
			b4 = buf_snmci.get();
			soundList.get(i).size = ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b4,b3,b2,b1});
			//System.out.println("		- Sound Data Size : "+soundList.get(i).size+" o/"+soundList.get(i).size/1024+" Ko");
			
			b1 = buf_snmci.get();
			b2 = buf_snmci.get();
			soundList.get(i).sampleRate = (int)ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,0,0,b2,b1});
			//System.out.println("		- Sample Rate : "+soundList.get(i).sampleRate);
			
			b1 = buf_snmci.get();
			soundList.get(i).bit_depth = ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,0,0,0,b1});
			//System.out.println("		- Bit Depth : "+soundList.get(i).bit_depth);
			
			soundList.get(i).format = buf_snmci.get();
			//System.out.println("		- format : "+soundList.get(i).format);
		}
		ready = true;
	}
	
	public void decrypt_mp3(File snmcd_){

		//puis le fichier contenant les MP3, snmcd._
		System.out.println("	- Lecture du ficher des MP3.");
		buf_snmcd = ByteBuffer.allocate((int)snmcd_.length());
		try {
			DataInputManager in = new DataInputManager (snmcd_);
			for(long index = 0 ; index<snmcd_.length(); index++){
				buf_snmcd.put(in.readByte());
			}
			in.close();
		}
		catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		
		buf_snmcd.rewind();
		while (!ready){
			try{
				System.err.println("MP3 WAITING");
				Thread.sleep(1000);
			}catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		for (int i=0 ; i<soundList.size() ; i++){
			soundList.get(i).sound = ByteBuffer.allocate((int)soundList.get(i).size);
			SoundListInfo son = soundList.get(i);
			String nom_fichier = new String(son.name);
			if (soundList.get(i).format == 0){
				for (int k=0 ; k<soundList.get(i).size ; k++){
					soundList.get(i).sound.put(buf_snmcd.get());
				}
				try {
					DataOutputStream out = new DataOutputStream(new FileOutputStream(Params.t4cOUT+"SONS/MP3/"+nom_fichier+".mp3"));
					out.write(son.sound.array());
					out.close();
				}
				catch(IOException exc){
					System.err.println("Erreur I/O");
					exc.printStackTrace();
				}
				System.out.println("	- MP3 écrit : "+Params.t4cOUT+"SONS/MP3/"+nom_fichier+".mp3");
				Params.nb_sons++;
			}	
		}
	}
	
	public void decrypt_wave(File snmcf_){

		System.out.println("	- Lecture du fichier des WAVE.");
		buf_snmcf = ByteBuffer.allocate((int)snmcf_.length());
		try {
			DataInputManager in = new DataInputManager (snmcf_);
			for(long index = 0 ; index<snmcf_.length(); index++){
				buf_snmcf.put(in.readByte());
			}
			in.close();
		}
		catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		
		buf_snmcf.rewind();
		while (!ready){
			try{
				System.err.println("WAVE WAITING");
				Thread.sleep(1000);
			}catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		for (int i=0 ; i<soundList.size() ; i++){
			soundList.get(i).sound = ByteBuffer.allocate((int)soundList.get(i).size);
			SoundListInfo son = soundList.get(i);
			String nom_fichier = new String(son.name);
			if (soundList.get(i).format == 1){
				for (int l=0 ; l<soundList.get(i).size ; l++){
					soundList.get(i).sound.put(buf_snmcf.get());
				}
				//System.out.println("	- Son "+i+" :"+Params.t4cOUT+"SONS/WAVE/"+nom_fichier+".wav");
				SoundHeader header = new SoundHeader();
				header.fileSize = (int) (soundList.get(i).size+36);
				//System.out.println("		- FileSize : "+header.fileSize);
				header.frequence = soundList.get(i).sampleRate;
				//System.out.println("		- Frequence : "+header.frequence);
				header.bitsPerSample = (short) soundList.get(i).bit_depth;
				//System.out.println("		- BitsPerSample : "+header.bitsPerSample);
				header.bytePerBloc = (short) (header.nbrCanaux * header.bitsPerSample/8);
				//System.out.println("		- BytePerBloc : "+header.bytePerBloc);
				header.bytePerSec = (int) (header.frequence * header.bytePerBloc);
				//System.out.println("		- BytePerSec : "+header.bytePerSec);
				header.dataSize = (int) soundList.get(i).size;
				//System.out.println("		- DataSize : "+header.dataSize);
				byte[] header_data = header.getData();
				ByteBuffer buf = ByteBuffer.allocate(header_data.length+son.sound.array().length);
				buf.put(header_data);
				buf.put(son.sound.array());
				try {
					DataOutputStream out = new DataOutputStream(new FileOutputStream(Params.t4cOUT+"SONS/WAVE/"+nom_fichier+".wav"));
					out.write(buf.array());
					out.close();
				}
				catch(IOException exc){
					System.err.println("Erreur I/O");
					exc.printStackTrace();
				}
				System.out.println("	- WAVE écrit : "+Params.t4cOUT+"SONS/WAVE/"+nom_fichier+".wav");
				Params.nb_sons++;
			}
		}
	}
	
	
	public void decrypt(File snmci_, File snmcd_, File snmcf_){
		
		//On commence par lire le fichier d'index snmci._
		System.out.println("	- Lecture de l'index des sons.");
		long nbsound = 0;
		ByteBuffer buf_snmci = ByteBuffer.allocate((int)snmci_.length());
		try {
			DataInputManager in = new DataInputManager (snmci_);
			for(long index = 0 ; index<snmci_.length(); index++){
				buf_snmci.put(in.readByte());
			}
			in.close();
		}
		catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}
		
		//puis le fichier contenant les MP3, snmcf._
		System.out.println("	- Lecture du ficher des MP3.");
		ByteBuffer buf_snmcf = ByteBuffer.allocate((int)snmcf_.length());
		try {
			DataInputManager in = new DataInputManager (snmcf_);
			for(long index = 0 ; index<snmcf_.length(); index++){
				buf_snmcf.put(in.readByte());
			}
			in.close();
		}
		catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}

		
		//puis le fichier contenant les WAVE, snmcd._
		System.out.println("	- Lecture du fichier des WAVE.");
		ByteBuffer buf_snmcd = ByteBuffer.allocate((int)snmcd_.length());
		try {
			DataInputManager in = new DataInputManager (snmcd_);
			for(long index = 0 ; index<snmcd_.length(); index++){
				buf_snmcd.put(in.readByte());
			}
			in.close();
		}
		catch(IOException exc){
			System.err.println("Erreur d'ouverture");
			exc.printStackTrace();
		}

		
		//On remet les tampons au début pour pouvoir les relire
		buf_snmci.rewind();
		buf_snmcd.rewind();
		buf_snmcf.rewind();

		//À partir de là on peut attaquer le décryptage
		System.out.println("	- Décryptage de l'index des sons :");
		
		//On récupère le nombre de sons à extraire.
		byte b1, b2, b3, b4;
		b1 = buf_snmci.get();
		b2 = buf_snmci.get();
		b3 = buf_snmci.get();
		b4 = buf_snmci.get();
		nbsound = ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b4,b3,b2,b1});
		System.out.println("		- Nombre de sons : "+nbsound);
		
		//On Crée une liste de SoundListInfo et on la remplit avec les données et metadonnées récupérées dans les 3 fichiers.
		ArrayList<SoundListInfo> soundList = new ArrayList<SoundListInfo>();
		for (int i=0 ; i < nbsound; i++){
			//System.out.println("	- Son "+i+" :");
			soundList.add(new SoundListInfo());	
			soundList.get(i).taille_name = buf_snmci.get();
			System.out.println("		- Longueur Du Nom : "+soundList.get(i).taille_name);
			soundList.get(i).name = new char[soundList.get(i).taille_name];
			for(int j=0 ; j<soundList.get(i).taille_name ; j++){
				soundList.get(i).name[j] = (char)buf_snmci.get();
			}
			System.out.println("		- Nom : "+new String(soundList.get(i).name));
			
			b1 = buf_snmci.get();
			b2 = buf_snmci.get();
			b3 = buf_snmci.get();
			b4 = buf_snmci.get();
			soundList.get(i).start = ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b4,b3,b2,b1});
			System.out.println("		- Start : "+soundList.get(i).start+" o/"+soundList.get(i).start/1024+" Ko");
			
			b1 = buf_snmci.get();
			b2 = buf_snmci.get();
			b3 = buf_snmci.get();
			b4 = buf_snmci.get();
			soundList.get(i).size = ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,b4,b3,b2,b1});
			System.out.println("		- Sound Data Size : "+soundList.get(i).size+" o/"+soundList.get(i).size/1024+" Ko");
			
			b1 = buf_snmci.get();
			b2 = buf_snmci.get();
			soundList.get(i).sampleRate = (int)ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,0,0,b2,b1});
			System.out.println("		- Sample Rate : "+soundList.get(i).sampleRate);
			
			b1 = buf_snmci.get();
			soundList.get(i).bit_depth = ByteArrayToNumber.bytesToLong(new byte[]{0,0,0,0,0,0,0,b1});
			System.out.println("		- Bit Depth : "+soundList.get(i).bit_depth);
			
			soundList.get(i).format = buf_snmci.get();
			
			soundList.get(i).sound = ByteBuffer.allocate((int)soundList.get(i).size);
			if (soundList.get(i).format == 0){
				System.out.println("		- Format : MP3");
				for (int k=0 ; k<soundList.get(i).size ; k++){
					soundList.get(i).sound.put(buf_snmcd.get());
				}
			}else{
				System.out.println("		- Format : WAVE");
				for (int l=0 ; l<soundList.get(i).size ; l++){
					soundList.get(i).sound.put(buf_snmcf.get());
				}
			}
		}
		
		//Puis on ajoute les entêtes dans les fichier par rapport aux infos dans les SoundListInfo
		System.out.println("	- On ajoute les entêtes et on écrit les fichiers.");
		for (int i=0 ; i<soundList.size() ; i++){
			SoundListInfo son = soundList.get(i);
			String nom_fichier = new String(son.name);
			if(son.format == 0){
				//System.out.println("	- Son "+i+" :"+Params.t4cOUT+"SONS/MP3/"+nom_fichier+".mp3");
				try {
					DataOutputStream out = new DataOutputStream(new FileOutputStream(Params.t4cOUT+"SONS/MP3/"+nom_fichier+".mp3"));
					out.write(son.sound.array());
					out.close();
				}
				catch(IOException exc){
					System.err.println("Erreur I/O");
					exc.printStackTrace();
				}
			}else{
				//System.out.println("	- Son "+i+" :"+Params.t4cOUT+"SONS/WAVE/"+nom_fichier+".wav");
				SoundHeader header = new SoundHeader();
				header.fileSize = (int) (soundList.get(i).size+36);
				//System.out.println("		- FileSize : "+header.fileSize);
				header.frequence = soundList.get(i).sampleRate;
				//System.out.println("		- Frequence : "+header.frequence);
				header.bitsPerSample = (short) soundList.get(i).bit_depth;
				//System.out.println("		- BitsPerSample : "+header.bitsPerSample);
				header.bytePerBloc = (short) (header.nbrCanaux * header.bitsPerSample/8);
				//System.out.println("		- BytePerBloc : "+header.bytePerBloc);
				header.bytePerSec = (int) (header.frequence * header.bytePerBloc);
				//System.out.println("		- BytePerSec : "+header.bytePerSec);
				header.dataSize = (int) soundList.get(i).size;
				//System.out.println("		- DataSize : "+header.dataSize);
				byte[] header_data = header.getData();
				ByteBuffer buf = ByteBuffer.allocate(header_data.length+son.sound.array().length);
				buf.put(header_data);
				buf.put(son.sound.array());
				try {
					DataOutputStream out = new DataOutputStream(new FileOutputStream(Params.t4cOUT+"SONS/WAVE/"+nom_fichier+".wav"));
					out.write(buf.array());
					out.close();
				}
				catch(IOException exc){
					System.err.println("Erreur I/O");
					exc.printStackTrace();
				}
			}
		}
	}
}
