package t4cPlugin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SoundHeader {
	
	public byte[] fileTypeBlocID = new byte[]{0x52,0x49,0x46,0x46};
	public int fileSize = 0;
	public byte[] fileFormatID = new byte[]{0x57,0x41,0x56,0x45};
	public byte[] formatBlocID = new byte[]{0x66,0x6D,0x74,0x20};
	public int blocSize = 16;
	public short audioFormat = 1;
	public short nbrCanaux = 1;
	public int frequence;
	public int bytePerSec;
	public short bytePerBloc;
	public short bitsPerSample;
	public byte[] dataBlocID = new byte[]{0x64,0x61,0x74,0x61};
	public int dataSize;
	public static final int headerSize = 44;
	
	public SoundHeader(){}

	public byte[] getData() {
		ByteBuffer result = ByteBuffer.allocate(headerSize);
		result.order(ByteOrder.LITTLE_ENDIAN);
		result.put(fileTypeBlocID);
		result.putInt(fileSize);
		result.put(fileFormatID);
		result.put(formatBlocID);
		result.putInt(blocSize);
		result.putShort(audioFormat);
		result.putShort(nbrCanaux);
		result.putInt(frequence);
		result.putInt(bytePerSec);
		result.putShort(bytePerBloc);
		result.putShort(bitsPerSample);
		result.put(dataBlocID);
		result.putInt(dataSize);
		return result.array();
	}
}
