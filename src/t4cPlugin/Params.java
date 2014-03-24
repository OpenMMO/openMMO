package t4cPlugin;

import java.io.File;

public class Params {
	public static final String t4cIN = "."+File.separator+"IN"+File.separator+"";
	public static final String t4cOUT = "."+File.separator+"OUT"+File.separator+"";
	public static String CHARSET = "";
	public static Character SLASH;
	public static Character ANTISLASH;
	public static String LINE;
	public static String OS;
	public static int NB = 100;
	
	//on se met quelques booléens pour activer/désactiver le calcul des différents types de fichiers
	final static boolean comp_elng = false;
	final static boolean comp_sons = false;
	final static boolean comp_map = true;
	final static boolean comp_wda = false;
	final static boolean comp_dpd = false;
	final static boolean comp_did = true;
	final static boolean comp_dda = false;
	public static String SPRITES = t4cOUT+"sprites"+File.separator;
	
	static int nb_open_thread = 0;
	
	public static int nb_elng = 0;
	public static int nb_sons = 0;
	public static int nb_map = 0;
	public static int nb_colmap = 0;
	public static int nb_palette = 0;
	public static int total_sprites = 0;
	public static int nb_sprite = 0;
	public static int nb_sorts = 0;
	public static int nb_effets_sorts = 0;
	public static int nb_item_pos = 0;
	public static int nb_item = 0;
	public static int nb_icones = 0;
	public static int nb_lieux = 0;
	public static int nb_gfx_creatures = 0;
	public static int nb_gfx_items = 0;
	public static int nb_flags = 0;
	public static int nb_clans = 0;
	public static int nb_clan_relations = 0;
	public static int nb_teleportations = 0;
	public static int nb_spawns = 0;
	public static int nb_creatures = 0;
	public static int total_palette = 0;
	public static int total_elng = 0;
	public static long total_sons = 0;
	public static int total_map = 0;
	public static int total_sorts = 0;
	public static int total_colmaps = 0;
	public static int total_items = 0;
	public static int total_item_pos = 0;
	public static int total_creatures = 0;
	public static int total_spawns = 0;
	public static int total_teleportations = 0;
	public static int total_clan_relations = 0;
	public static int total_clans = 0;
	public static int total_flags = 0;
	public static int total_gfx_items = 0;
	public static int total_gfx_creatures = 0;
	public static int total_lieux = 0;
	public static int total_icones = 0;
	public static int total_fx_sorts = 0;
	public static int total_pnjs = 0;
	protected static int dpdready = 0;
	protected static int didready = 0;
	protected static int ddaready = 0;
	public static boolean draw_sprites = false;
	public static String IDS = "";
	protected static String INFO = "Info : Chargement des données...Patienter.";
	public static String MEMLOAD = "";
	public static String STATUS = "Infos : ";



	
	public static void closeThread(){
		nb_open_thread--;
	}
}
