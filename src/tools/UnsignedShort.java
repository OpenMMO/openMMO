/**
 * 
 */
package tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author synoga
 *
 */
public class UnsignedShort{
	static Logger logger = LogManager.getLogger(ByteArrayToNumber.class.getSimpleName());
	int value;
	public UnsignedShort(byte[] bytes){
		if(bytes.length!=2){
			logger.warn("bytes doit avoir une longueur de 2");
			return;
		}
		byte[] array = new byte[4];
		array[0] = 0;
		array[1] = 0;
		array[2] = bytes[0];
		array[3] = bytes[1];
		value = ByteArrayToNumber.bytesToInt(array);
	}
	/**
	 * 
	 */
	public UnsignedShort() {
		value = 0;
	}
	
	public int getValue(){
		return value;
	}
}