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
public class UnsignedInt {
	
	static Logger logger = LogManager.getLogger(ByteArrayToNumber.class.getSimpleName());
	long value;
	
	public UnsignedInt(byte[] bytes){
		if(bytes.length!=4){
			logger.warn("l'argument bytes doit avoir une longueur de 4");
			return;
		}
		byte[] array = new byte[8];
		array[0] = 0;
		array[1] = 0;
		array[2] = 0;
		array[3] = 0;
		array[4] = bytes[0];
		array[5] = bytes[1];
		array[6] = bytes[2];
		array[7] = bytes[3];
		value = ByteArrayToNumber.bytesToLong(array);
	}
	public long getValue(){
		return value;
	}
}
