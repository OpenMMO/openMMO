package opent4c;

public class UpdateDataCheckStatus {
	private static String status = "not set (yet)";
	//private static Logger logger = LogManager.getLogger(UpdateDataCheckStatus.class.getSimpleName());

	
	public static String getStatus(){
		return status;
	}
	
	public static synchronized void setStatus(String s){
		status = s;
		//logger.info(s);
	}
}
