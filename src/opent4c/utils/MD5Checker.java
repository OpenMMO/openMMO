package opent4c.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.badlogic.gdx.Gdx;

public class MD5Checker {
	/**
	 * Vérifie le MD5 du fichier f
	 * @param f
	 * @return
	 */
	public static boolean check(File f, String md5Ref){
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Gdx.app.exit();
		} 
        FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
        BufferedInputStream bis = new BufferedInputStream(fis);
        DigestInputStream   dis = new DigestInputStream(bis, md5);

        try {
			while (dis.read() != -1);
		} catch (IOException e) {
			e.printStackTrace();
			Gdx.app.exit();
		}

        try {
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
        
        String hash = tools.ByteArrayToHexString.print(md5.digest());
        //logger.info("java MD5 : " + hash);
        //logger.info("System MD5 : " + sourceData.get(f));
        if(hash.equalsIgnoreCase(md5Ref)){
        	return true;
        } else {
        	return false;
        }
	}
}
