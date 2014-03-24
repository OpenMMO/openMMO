package tools;

import java.nio.ByteBuffer;

public class ByteArrayToNumber {
	
	public static long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(8);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getLong();
	}
	
	public static int bytesToInt(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(4);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getInt();
	}
	
	public static short bytesToShort(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(2);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getShort();
	}
}
