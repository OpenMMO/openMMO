package t4cPlugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.Serializable;

public class SpriteName implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8794893386652597065L;

	private static Logger logger = LogManager.getLogger(SpriteName.class);
	
	private String spriteName;
	private String spriteEscapedName;
	
	
	public SpriteName(String name) {
		if (name == null) {
			logger.warn("On crée un SpriteName avec un nom null!");
			setName("");
		}
		else {
			setName(name);
		}
	}
	
	public void setName(String name) {
		if (name == null) {
			logger.warn("On remplace le nom d'un SpriteName par null!");
			name = "";
		}
		spriteName = name;
		spriteEscapedName = escapeName(name);
	}
	
	public String getName() {
		return spriteEscapedName;
	}
	
	public String getUnescapedName() {
		return spriteName;
	}
	
	/**
	 * Modifie les caractères posant problèmes dans les noms de fichiers de certains OS.
	 * Sous Windows il s'agit des caractères \ / : * ? < > |
	 * @param name
	 * @return
	 */
	private String escapeName(String name) {
		String escaped = name.replaceAll("\\\\|/|:|\\*|\\?|<|>|\\|", "");
		return escaped;
	}

}
