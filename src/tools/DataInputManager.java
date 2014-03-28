package tools;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Goal of this class is to allow updates on the way bytes are read without change every related line in the code.
 */
public class DataInputManager {

	private DataInputStream dis;
	
	public DataInputManager(File file) throws FileNotFoundException
	{
		FileInputStream fis = new FileInputStream(file);
		//Huge performance gain with a bufferedInputStream VS use of DataInputManager alone.
		//Still can be better but it is enough for now.
		dis = new DataInputStream(new BufferedInputStream(fis));
	}
	
	
	/**
	 * @throws IOException 
	 * @see java.io.DataInputStream#readByte()
	 */
	public byte readByte() throws IOException
	{
		return dis.readByte();
	}
	
	/**
	 * @throws IOException 
	 * @see java.io.DataInputStream#close()
	 */
	public void close() throws IOException
	{
		dis.close();
	}
	
	/**
	 * 
	 * @throws IOException
	 * @see java.io.DataInputStream#read()
	 */
	public int read() throws IOException
	{
		return dis.read();
	}
}
