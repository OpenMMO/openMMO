/**
 * 
 */
package opent4c.utils;

import java.awt.Point;
import java.util.Map;

import opent4c.Chunk;
import screens.MapManager;

/**
 * @author synoga
 *
 */
public class ChunkMovement {
	
	/**
	 * drection : numpad : 2 down 6 right 8 up 4 left
	 */
	public static void move(int direction, Map<Integer,Chunk> map){
		if(direction == 0) return;
		if(direction == 1){
			move(2, map);
			move(4, map);
			return;
		}
		if(direction == 3){
			move(2, map);
			move(6, map);
			return;			
		}
		if(direction == 7){
			move(8, map);
			move(4, map);
			return;
		}
		if(direction == 9){
			move(8, map);
			move(6, map);
			return;
		}
		int[] deleted = getDeleted(direction);
		int[] moved = getMoved(direction);
		int[] created = getCreated(direction);
		Point point = getNewCenter(direction, map);
		
		map.put(deleted[0], map.get(moved[0]));
		map.put(deleted[1], map.get(moved[1]));
		map.put(deleted[2], map.get(moved[2]));

		map.put(moved[0], map.get(created[0]));
		map.put(moved[1], map.get(created[1]));
		map.put(moved[2], map.get(created[2]));
		
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point);
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		map.put(created[0],new Chunk("v2_worldmap",chunk_positions.get(created[0])));
		map.put(created[1],new Chunk("v2_worldmap",chunk_positions.get(created[1])));
		map.put(created[2],new Chunk("v2_worldmap",chunk_positions.get(created[2])));
		MapManager.renderChunks();

	}
	
	/**
	 * @param direction
	 * @return
	 */
	private static int[] getCreated(int direction) {
		switch(direction){
			case 2 : return new int[]{1,2,3};
			case 4 : return new int[]{7,4,1};
			case 6 : return new int[]{9,6,3};
			case 8 : return new int[]{7,8,9};
		}
		return null;
	}

	/**
	 * @param direction
	 * @return
	 */
	private static int[] getMoved(int direction) {
		switch(direction){
		case 2 : return new int[]{4,5,6};
		case 4 : return new int[]{8,5,2};
		case 6 : return new int[]{8,5,2};
		case 8 : return new int[]{4,5,6};
	}
		return null;
	}

	/**
	 * @param direction
	 * @return
	 */
	private static int[] getDeleted(int direction) {
		switch(direction){
		case 8 : return new int[]{1,2,3};
		case 6 : return new int[]{7,4,1};
		case 4 : return new int[]{9,6,3};
		case 2 : return new int[]{7,8,9};
	}		return null;
	}

	/**
	 * @param direction
	 * @param map 
	 * @return
	 */
	private static Point getNewCenter(int direction, Map<Integer, Chunk> map) {
		return map.get(direction).getCenter();
	}
}
