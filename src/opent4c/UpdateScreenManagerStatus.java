package opent4c;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import screens.ScreenManager;

public class UpdateScreenManagerStatus {
	
	private static Logger logger = LogManager.getLogger(UpdateScreenManagerStatus.class.getSimpleName());
	private static ScreenManager sm = null;
	private static String sourceDataStatus = "not set";
	private static String mapsStatus = "not set";
	private static String dpdStatus = "not set";
	private static String didStatus = "not set";
	private static String ddaStatus = "not set";
	private static String spriteDataStatus = "not set";
	private static String atlasStatus = "not set";
	
	public static void setScreenManager(ScreenManager manager){
		sm = manager;
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
		UpdateScreenManagerStatus.sourceDataStatus = sourceDataStatus;
	}

	public static void setSpriteDataStatus(String spriteDataStatus) {
		UpdateScreenManagerStatus.spriteDataStatus = spriteDataStatus;
	}

	public static String getDidStatus() {
		return didStatus;
	}

	public static void setDidStatus(String didStatus) {
		UpdateScreenManagerStatus.didStatus = didStatus;
	}

	public static String getDpdStatus() {
		return dpdStatus;
	}

	public static void setDpdStatus(String dpdStatus) {
		UpdateScreenManagerStatus.dpdStatus = dpdStatus;
	}

	public static String getDdaStatus() {
		return ddaStatus;
	}

	public static void setDdaStatus(String ddaStatus) {
		UpdateScreenManagerStatus.ddaStatus = ddaStatus;
	}

	public static void setMapsStatus(String mapsStatus) {
		UpdateScreenManagerStatus.mapsStatus = mapsStatus;
	}

	public static void setAtlasStatus(String atlasStatus) {
		UpdateScreenManagerStatus.atlasStatus = atlasStatus;
	}
}
