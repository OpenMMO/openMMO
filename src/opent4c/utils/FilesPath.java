package opent4c.utils;

import java.io.File;

public class FilesPath {
	
	private FilesPath () {}
	
	/**
	 * creates every needed directory in case it doesn't exist
	 */
	public static void init(){
		new File(getSpriteDataDirectoryPath()).mkdirs();
		new File(getSpriteDirectoryPath()).mkdirs();
		new File(getAtlasSpriteDirectoryPath()).mkdirs();
		new File(getTuileDirectoryPath()).mkdirs();
		new File(getAtlasTuileDirectoryPath()).mkdirs();
		new File(getDataDirectoryPath()).mkdirs();
		new File(getMapDataDirectoryPath()).mkdirs();
	}

	/**
	 * @return the error log file path
	 */
	public static String getErrorLogFilePath(){
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("errors.log");
		
		return sb.toString();
	}
	
	/**
	 * @return the sprite path
	 */
	public static String getSpritePath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("sprites");		
		sb.append(File.separator);
		sb.append("sprites");
		
		return sb.toString();
	}
	
	public static String getSpriteDataDirectoryPath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("sprites");		
		sb.append(File.separator);
		
		return sb.toString();
	}
	
	public static String getSmoothingDirectoryPath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("sprites");		
		sb.append(File.separator);
		sb.append("smoothing");
		sb.append(File.separator);
		
		return sb.toString();
	}
	
	public static String getSpriteDirectoryPath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("sprites");		
		sb.append(File.separator);
		sb.append("sprites");
		sb.append(File.separator);
		
		return sb.toString();
	}
	
	public static String getAtlasSpriteDirectoryPath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("atlas");		
		sb.append(File.separator);
		sb.append("sprites");
		sb.append(File.separator);
		
		return sb.toString();
	}
	
	public static String getAtlasSpritePath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("atlas");		
		sb.append(File.separator);
		sb.append("sprites");
		
		return sb.toString();
	}
	
	public static String getTuilePath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("sprites");		
		sb.append(File.separator);
		sb.append("tuiles");
		
		return sb.toString();
	}
	
	public static String getTuileDirectoryPath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("sprites");		
		sb.append(File.separator);
		sb.append("tuiles");
		sb.append(File.separator);
		
		return sb.toString();
	}
	
	public static String getAtlasTuileDirectoryPath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("atlas");		
		sb.append(File.separator);
		sb.append("tuiles");
		sb.append(File.separator);
		
		return sb.toString();
	}
	
	public static String getAtlasTuilePath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("atlas");		
		sb.append(File.separator);
		sb.append("tuiles");
		
		return sb.toString();
	}
	
	public static String getAtlasSpritesFilePath(String fileName) {
		StringBuilder sb = new StringBuilder(24 + fileName.length());
		
		sb.append("data");
		sb.append(File.separator);
		sb.append("atlas");
		sb.append(File.separator);
		sb.append("sprites");
		sb.append(File.separator);
		sb.append(fileName);
		sb.append(".atlas");
		
		return sb.toString();
	}
	
	public static String getAtlasTilesFilePath(String fileName) {
		StringBuilder sb = new StringBuilder(24 + fileName.length());
		
		sb.append("data");
		sb.append(File.separator);
		sb.append("atlas");
		sb.append(File.separator);
		sb.append("tuiles");
		sb.append(File.separator);
		sb.append(fileName);
		sb.append(".atlas");
		
		return sb.toString();
	}

	public static String getDataDirectoryPath() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("data");
		sb.append(File.separator);
		
		return sb.toString();
	}
	
	public static String getSourceDataDirectoryPath() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("data");
		sb.append(File.separator);
		sb.append("game_files");
		sb.append(File.separator);

		return sb.toString();
	}
	
	public static String getMapDataDirectoryPath(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("data");
		sb.append(File.separator);
		sb.append("maps");
		sb.append(File.separator);
		
		return sb.toString();
	}
	
	public static String getMapFilePath(String fileName) {
		StringBuilder sb = new StringBuilder(24 + fileName.length());
		
		sb.append("data");
		sb.append(File.separator);
		sb.append("maps");
		sb.append(File.separator);
		sb.append(fileName);
		sb.append(".decrypt");
		
		return sb.toString();
	}

	public static String getAtlasUnknownFilePath() {
		StringBuilder sb = new StringBuilder(24 + 7);
		
		sb.append("data");
		sb.append(File.separator);
		sb.append("Unknown.atlas");
		
		return sb.toString();
	}

	public static String getAtlasHighlightFilePath() {
		StringBuilder sb = new StringBuilder(24 + 7);
		
		sb.append("data");
		sb.append(File.separator);
		sb.append("Highlight.atlas");
		
		return sb.toString();
	}

	/**
	 * @return
	 */
	public static String getPixelIndexFilePath() {
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("pixel_index");
		
		return sb.toString();
	}

	/**
	 * @return
	 */
	public static String getBadPaletteFilePath() {
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("bad_palettes");
		
		return sb.toString();
	}

	/**
	 * @return
	 */
	public static String getMirrorFilePath() {
		StringBuilder sb = new StringBuilder();
		sb.append("data");
		sb.append(File.separator);
		sb.append("mirrors");
		
		return sb.toString();
	}

	public static String getIdFullFilePath() {
		StringBuilder sb = new StringBuilder();
		sb.append("data");		
		sb.append(File.separator);
		sb.append("idfull.txt");		
		
		return sb.toString();
	}
}
