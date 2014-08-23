package opent4c;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateDataCheckStatus {
	private static String status = "not set (yet)";
	private static Logger logger = LogManager.getLogger(UpdateDataCheckStatus.class.getSimpleName());

	
	public static String getStatus(){
		return status;
	}
	
	public static synchronized void setStatus(String s){
		status = s;
		logger.info(s);
	}
}
