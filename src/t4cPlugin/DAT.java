package t4cPlugin;

public class DAT {

	/**Taille		type			Description
	===============================================================================
	4		unsigned long		Taille décompressée des images et leurs palettes

		Pour chaque taille d'images compréssées (En fonction du nombre de cartes):
		{
			4		unsigend long		Taille de l'image et sa palette comprésée
		}
		Pour chaque offset d'images (En fonction du nombre de cartes):
		{
			4		unsigend long		Offset d'emplacement de l'image et sa palette
		}	
		Pour chaque images et leurs palettes (En fonction du nombre de cartes):
		{
			4		stream			Image et sa palette comprésée ZLib 1.4
		}	
	}
	
	
	Taille		type			Description
===============================================================================
	Pour chaque images (En fonction du nombre de cartes):
	{
		TailleImg - 768	unsigend long		Image
		768		char *			Palette de l'image
	}
}
	
	*/

}
