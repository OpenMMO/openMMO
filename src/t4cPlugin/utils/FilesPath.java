package t4cPlugin.utils;

import java.io.File;

public class FilesPath {
	
	private FilesPath () {}
	
	
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
}
