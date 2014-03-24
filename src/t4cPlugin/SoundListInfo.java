package t4cPlugin;

import java.nio.ByteBuffer;

public class SoundListInfo {

	public byte taille_name;
	public char[] name;
	public long start;
	public long size;
	public int sampleRate;
	public long bit_depth;
	public byte format;
	public ByteBuffer sound;
	public long nbsound;
	
	public SoundListInfo(){}
}
