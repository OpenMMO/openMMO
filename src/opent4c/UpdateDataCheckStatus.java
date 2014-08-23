package opent4c;

public class UpdateDataCheckStatus {
	private static String status = "not set (yet)";
	
	public static String getStatus(){
		return status;
	}
	
	public static synchronized void setStatus(String s){
		status = s;
	}
}
