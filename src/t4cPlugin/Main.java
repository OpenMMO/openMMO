package t4cPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import tools.OSValidator;

public class Main {

	/**
	 * Ce plugin est fait à partir des informations
	 * du wiki d'Angel Le Prophète (merci à ceux qui
	 * y ont contribué)
	 * et testé avec un client 1.6x modifié.
	 * @param args
	 */
	public static void main(String[] args) {
		
		DID didFile = null;
		DDA ddaFile = null;
		VSB vsbFile = null;
		DPD dpdFile = null;
		WDA wdaFile = null;
		
		//Détection de l'OS
		OSValidator.detect();
		Calendar date = new GregorianCalendar();
		long start,end;
		start = date.getTimeInMillis();
		
		//Création de la structure de dossier pour les sorties
		System.out.println("Création du dossier de sortie ELNG.");
		File outELNG = new File(Params.t4cOUT+"ELNG/");
		outELNG.mkdirs();
		System.out.println("Création du dossier de sortie WAVE.");
		File outWAVE = new File(Params.t4cOUT+"SONS/WAVE/");
		outWAVE.mkdirs();
		System.out.println("Création du dossier de sortie MP3.");
		File outMP3 = new File(Params.t4cOUT+"SONS/MP3/");
		outMP3.mkdirs();
		System.out.println("Création du dossier de sortie COLMAPS.");
		File outCOLMAPS = new File(Params.t4cOUT+"COLMAPS/");
		outCOLMAPS.mkdirs();
		System.out.println("Création du dossier de sortie MAP.");
		File outMAP = new File(Params.t4cOUT+"MAP/");
		outMAP.mkdirs();
		System.out.println("Création du dossier de sortie des SPRITES.");
		File outSPRITES = new File(Params.t4cOUT+"SPRITES/");
		outSPRITES.mkdirs();
		System.out.println("Création du dossier de sortie des SORTS.");
		File outSPELLS = new File(Params.t4cOUT+"SPELLS/");
		outSPELLS.mkdirs();
		System.out.println("Création du dossier de sortie des ITEMS.");
		File outITEMS = new File(Params.t4cOUT+"ITEMS/");
		outITEMS.mkdirs();
		System.out.println("Création du dossier de sortie des MONSTRES.");
		File outMONSTRES = new File(Params.t4cOUT+"MONSTRES/");
		outMONSTRES.mkdirs();
		System.out.println("Création du dossier de sortie des SPELLFX.");
		File outSPELLFX = new File(Params.t4cOUT+"SPELLFX/");
		outSPELLFX.mkdirs();
		System.out.println("Création du dossier de sortie des ICONES.");
		File outICONES = new File(Params.t4cOUT+"ICONES/");
		outICONES.mkdirs();
		System.out.println("Création du dossier de sortie des LIEUX.");
		File outLIEUX = new File(Params.t4cOUT+"LIEUX/");
		outLIEUX.mkdirs();
		System.out.println("Création du dossier de sortie des FLAGS.");
		File outFLAGS = new File(Params.t4cOUT+"FLAGS/");
		outFLAGS.mkdirs();
		System.out.println("Création du dossier de sortie des CLANS.");
		File outCLANS = new File(Params.t4cOUT+"CLANS/");
		outCLANS.mkdirs();
		System.out.println("Création du dossier de sortie des SPAWN.");
		File outSPAWNS = new File(Params.t4cOUT+"SPAWN/");
		outSPAWNS.mkdirs();
		System.out.println("Création du dossier de sortie des ITEM_POS.");
		File outITEM_POS = new File(Params.t4cOUT+"ITEM_POS/");
		outITEM_POS.mkdirs();
		
		
		//ensuite on liste les fichiers sources
		FileLister explorer = new FileLister();
		ArrayList<File> elng = new ArrayList<File>();
		ArrayList<File> sons = new ArrayList<File>();
		ArrayList<File> map = new ArrayList<File>();
		ArrayList<File> wda = new ArrayList<File>();
		ArrayList<File> vsb = new ArrayList<File>();
		ArrayList<File> dpd = new ArrayList<File>();
		ArrayList<File> did = new ArrayList<File>();
		ArrayList<File> dda = new ArrayList<File>();
		elng.addAll(explorer.lister(new File(Params.t4cIN), ".elng"));
		explorer.purge();
		sons.addAll(explorer.lister(new File(Params.t4cIN), "._"));
		explorer.purge();
		map.addAll(explorer.lister(new File(Params.t4cIN), ".map"));
		explorer.purge();
		wda.addAll(explorer.lister(new File(Params.t4cIN), ".WDA"));
		explorer.purge();
		dpd.addAll(explorer.lister(new File(Params.t4cIN), ".dpd"));
		explorer.purge();
		did.addAll(explorer.lister(new File(Params.t4cIN), ".did"));
		explorer.purge();
		dda.addAll(explorer.lister(new File(Params.t4cIN), ".dda"));
		explorer.purge();		
		try{
			//ELNG
			Iterator<File> iter = elng.iterator();
			System.out.println("Décryptage de "+elng.size()+" fichier(s) ELNG.");
			while(iter.hasNext()){				
				ELNG.decrypt(iter.next());
			}
			
			//WDA
			System.out.println("Décryptage de "+wda.size()+" fichier(s) WDA.");
			iter = wda.iterator();
			while(iter.hasNext()){
				wdaFile = new WDA();
				wdaFile.decrypt(iter.next());
			}
			
			//SONS
			System.out.println("Décryptage de "+sons.size()+" fichier(s) ._ .");
			File snmci = null;
			File snmcd = null;
			File snmcf = null;
			iter = sons.iterator();
			while(iter.hasNext()){
				File next = iter.next();
				if (next.getName().endsWith("i._")) snmci = next;
				if (next.getName().endsWith("d._")) snmcd = next;
				if (next.getName().endsWith("f._")) snmcf = next;				
			}
			Sons.decrypt(snmci, snmcd, snmcf);

			//MAP
			System.out.println("Décryptage de "+map.size()+" fichier(s) MAP.");
			iter = map.iterator();
			while(iter.hasNext()){
				MAP.Map_Load(iter.next(), (short)0x2000);//0x2000 : client version >= 1.6x ; 0x1000 : client version <= 1.6x
			}
			
			//DPD
			System.out.println("Décryptage de "+dpd.size()+" fichier(s) DPD.");
			iter = dpd.iterator();
			while(iter.hasNext()){
				dpdFile = new DPD();
				dpdFile.decrypt(iter.next());
			}
			
			//DID
			System.out.println("Décryptage de "+dpd.size()+" fichier(s) DID.");
			iter = did.iterator();
			while(iter.hasNext()){
				didFile = new DID();
				didFile.decrypt(iter.next());
			}
			
			//DDA
			System.out.println("Décryptage de "+dda.size()+" fichier(s) DDA.");
			iter = dda.iterator();
			while(iter.hasNext()){
				ddaFile = new DDA();
				ddaFile.decrypt(iter.next(),didFile.sprites,DPD.palettes);
			}
			
		}catch(NullPointerException e){
			e.printStackTrace();
			System.out.println("Usage :");
			System.out.println("Placer les fichier dans le dossier : "+Params.t4cIN);
			System.out.println("Les fichiers décryptés se trouveront dans : "+ Params.t4cOUT);
		}
		date = new GregorianCalendar();
		end = date.getTimeInMillis();
		System.out.println("TEMPS D'EXECUTION : "+(end-start)/1000+"s.");
		System.out.println("TERMINÉ, Sprites décodés : "+DDA.nb_sprites+"/"+DDA.total_sprites);
	}
}
