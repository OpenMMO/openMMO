package opent4c;

public class UpdateDataCheckStatus {
	
	private static String sourceDataStatus = "not set";
	private static String mapsStatus = "not set";
	private static String dpdStatus = "not set";
	private static String didStatus = "not set";
	private static String ddaStatus = "not set";
	private static String spriteDataStatus = "not set";
	private static String atlasStatus = "not set";
	private static String status = "not set (yet)";
	

	public static String getStatus(){
		return status;
	}
	
	public static synchronized void setStatus(String s){
		status = s;
	}
	
	/**
	 * @return
	 */
	public static String getSourceDataStatus() {
		return sourceDataStatus ;
	}

	/**
	 * @return
	 */
	public static String getMapsStatus() {
		return mapsStatus ;
	}

	/**
	 * @return
	 */
	public static String getDPDStatus() {
		return getDpdStatus() ;
	}

	/**
	 * @return
	 */
	public static String getDIDStatus() {
		return getDidStatus() ;
	}

	/**
	 * @return
	 */
	public static String getDDAStatus() {
		return getDdaStatus();
	}

	/**
	 * @return
	 */
	public static String getSpriteDataStatus() {
		return spriteDataStatus;
	}

	/**
	 * @return
	 */
	public static String getAtlasStatus() {
		return atlasStatus;
	}

	public static void setSourceDataStatus(String sourceDataStatus) {
		UpdateDataCheckStatus.sourceDataStatus = sourceDataStatus;
	}

	public static void setSpriteDataStatus(String spriteDataStatus) {
		UpdateDataCheckStatus.spriteDataStatus = spriteDataStatus;
	}

	public static String getDidStatus() {
		return didStatus;
	}

	public static void setDidStatus(String didStatus) {
		UpdateDataCheckStatus.didStatus = didStatus;
	}

	public static String getDpdStatus() {
		return dpdStatus;
	}

	public static void setDpdStatus(String dpdStatus) {
		UpdateDataCheckStatus.dpdStatus = dpdStatus;
	}

	public static String getDdaStatus() {
		return ddaStatus;
	}

	public static void setDdaStatus(String ddaStatus) {
		UpdateDataCheckStatus.ddaStatus = ddaStatus;
	}

	public static void setMapsStatus(String mapsStatus) {
		UpdateDataCheckStatus.mapsStatus = mapsStatus;
	}

	public static void setAtlasStatus(String atlasStatus) {
		UpdateDataCheckStatus.atlasStatus = atlasStatus;
	}
}
