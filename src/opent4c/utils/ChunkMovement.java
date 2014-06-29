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
	 * Moves ChunkMap up
	 */
	public static void moveChunksUp(Map<Integer,Chunk> map) {
		Point point = map.get(7).getCenter();
		map.put(16, map.get(17));
		map.put(15, map.get(4));
		map.put(14, map.get(3));
		map.put(13, map.get(2));
		map.put(12, map.get(11));

		map.put(17, map.get(18));
		map.put(4, map.get(5));
		map.put(3, map.get(0));
		map.put(2, map.get(1));
		map.put(11, map.get(10));

		map.put(18, map.get(19));
		map.put(5, map.get(6));
		map.put(0, map.get(7));
		map.put(1, map.get(8));
		map.put(10, map.get(9));

		map.put(19, map.get(20));
		map.put(6, map.get(21));
		map.put(7, map.get(22));
		map.put(8, map.get(23));
		map.put(9, map.get(24));

		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, MapManager.getChunkSize());
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		map.put(20,new Chunk("v2_worldmap",chunk_positions.get(20)));
		map.put(21,new Chunk("v2_worldmap",chunk_positions.get(21)));
		map.put(22,new Chunk("v2_worldmap",chunk_positions.get(22)));
		map.put(23,new Chunk("v2_worldmap",chunk_positions.get(23)));
		map.put(24,new Chunk("v2_worldmap",chunk_positions.get(24)));
		MapManager.renderChunks();
	}
	
	/**
	 * Moves ChunkMap up and right
	 */
	public static void moveChunksUpRight(Map<Integer,Chunk> map) {
		Point point = map.get(8).getCenter();
		map.put(16, map.get(4));
		
		map.put(17, map.get(5));
		map.put(15, map.get(3));
		
		map.put(18, map.get(6));
		map.put(14, map.get(0));
		map.put(4, map.get(2));
		
		map.put(19, map.get(21));
		map.put(5, map.get(7));
		map.put(3, map.get(1));
		map.put(13, map.get(11));

		map.put(6, map.get(22));
		map.put(0, map.get(8));
		map.put(2, map.get(10));
		
		map.put(7, map.get(23));
		map.put(1, map.get(9));

		map.put(8, map.get(24));
		
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, MapManager.getChunkSize());
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		map.put(24,new Chunk("v2_worldmap",chunk_positions.get(24)));
		map.put(9,new Chunk("v2_worldmap",chunk_positions.get(9)));
		map.put(10,new Chunk("v2_worldmap",chunk_positions.get(10)));
		map.put(11,new Chunk("v2_worldmap",chunk_positions.get(11)));
		map.put(12,new Chunk("v2_worldmap",chunk_positions.get(12)));
		map.put(13,new Chunk("v2_worldmap",chunk_positions.get(13)));
		map.put(14,new Chunk("v2_worldmap",chunk_positions.get(14)));
		map.put(15,new Chunk("v2_worldmap",chunk_positions.get(15)));
		map.put(16,new Chunk("v2_worldmap",chunk_positions.get(16)));
		MapManager.renderChunks();
	}
	
	/**
	 * Moves ChunkMap up and left
	 */
	public static void moveChunksUpLeft(Map<Integer,Chunk> map) {
		Point point = map.get(6).getCenter();
		map.put(12, map.get(2));
		
		map.put(13, map.get(3));
		map.put(11, map.get(1));
		
		map.put(14, map.get(4));
		map.put(2, map.get(0));
		map.put(10, map.get(8));
		
		map.put(15, map.get(17));
		map.put(3, map.get(5));
		map.put(1, map.get(7));
		map.put(9, map.get(23));

		map.put(4, map.get(18));
		map.put(0, map.get(6));
		map.put(8, map.get(22));
		
		map.put(5, map.get(19));
		map.put(7, map.get(21));

		map.put(6, map.get(20));
		
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, MapManager.getChunkSize());
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		map.put(24,new Chunk("v2_worldmap",chunk_positions.get(24)));
		map.put(23,new Chunk("v2_worldmap",chunk_positions.get(23)));
		map.put(22,new Chunk("v2_worldmap",chunk_positions.get(22)));
		map.put(21,new Chunk("v2_worldmap",chunk_positions.get(21)));
		map.put(20,new Chunk("v2_worldmap",chunk_positions.get(20)));
		map.put(19,new Chunk("v2_worldmap",chunk_positions.get(19)));
		map.put(18,new Chunk("v2_worldmap",chunk_positions.get(18)));
		map.put(17,new Chunk("v2_worldmap",chunk_positions.get(17)));
		map.put(16,new Chunk("v2_worldmap",chunk_positions.get(16)));
		MapManager.renderChunks();
	}

	/**
	 * Moves ChunkMap left
	 */
	public static void moveChunksLeft(Map<Integer,Chunk> map) {
		Point point = map.get(5).getCenter();
		map.put(24, map.get(23));
		map.put(9, map.get(8));
		map.put(10, map.get(1));
		map.put(11, map.get(2));
		map.put(12, map.get(13));

		map.put(23, map.get(22));
		map.put(8, map.get(7));
		map.put(1, map.get(0));
		map.put(2, map.get(3));
		map.put(13, map.get(14));

		map.put(22, map.get(21));
		map.put(7, map.get(6));
		map.put(0, map.get(5));
		map.put(3, map.get(4));
		map.put(14, map.get(15));

		map.put(21, map.get(20));
		map.put(6, map.get(19));
		map.put(5, map.get(18));
		map.put(4, map.get(17));
		map.put(15, map.get(16));

		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, MapManager.getChunkSize());
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		map.put(20,new Chunk("v2_worldmap",chunk_positions.get(20)));
		map.put(19,new Chunk("v2_worldmap",chunk_positions.get(19)));
		map.put(18,new Chunk("v2_worldmap",chunk_positions.get(18)));
		map.put(17,new Chunk("v2_worldmap",chunk_positions.get(17)));
		map.put(16,new Chunk("v2_worldmap",chunk_positions.get(16)));
		MapManager.renderChunks();
	}

	/**
	 * Moves ChunkMap down and left
	 */
	public static void moveChunksDownLeft(Map<Integer,Chunk> map) {
		Point point = map.get(4).getCenter();
		map.put(24, map.get(8));
		
		map.put(23, map.get(7));
		map.put(9, map.get(1));
		
		map.put(22, map.get(6));
		map.put(8, map.get(0));
		map.put(10, map.get(2));
		
		map.put(21, map.get(19));
		map.put(7, map.get(5));
		map.put(1, map.get(3));
		map.put(11, map.get(13));

		map.put(6, map.get(18));
		map.put(0, map.get(4));
		map.put(2, map.get(14));
		
		map.put(5, map.get(17));
		map.put(3, map.get(15));

		map.put(4, map.get(16));
		
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, MapManager.getChunkSize());
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		map.put(20,new Chunk("v2_worldmap",chunk_positions.get(20)));
		map.put(19,new Chunk("v2_worldmap",chunk_positions.get(19)));
		map.put(18,new Chunk("v2_worldmap",chunk_positions.get(18)));
		map.put(17,new Chunk("v2_worldmap",chunk_positions.get(17)));
		map.put(16,new Chunk("v2_worldmap",chunk_positions.get(16)));
		map.put(15,new Chunk("v2_worldmap",chunk_positions.get(15)));
		map.put(14,new Chunk("v2_worldmap",chunk_positions.get(14)));
		map.put(13,new Chunk("v2_worldmap",chunk_positions.get(13)));
		map.put(12,new Chunk("v2_worldmap",chunk_positions.get(12)));
		MapManager.renderChunks();
	}

	/**
	 * Moves ChunkMap down
	 */
	public static void moveChunksDown(Map<Integer,Chunk> map) {
		Point point = map.get(3).getCenter();
		map.put(20, map.get(19));
		map.put(21, map.get(6));
		map.put(22, map.get(7));
		map.put(23, map.get(8));
		map.put(24, map.get(9));

		map.put(19, map.get(18));
		map.put(6, map.get(5));
		map.put(7, map.get(0));
		map.put(8, map.get(1));
		map.put(9, map.get(10));

		map.put(18, map.get(17));
		map.put(5, map.get(4));
		map.put(0, map.get(3));
		map.put(1, map.get(2));
		map.put(10, map.get(11));

		map.put(17, map.get(16));
		map.put(4, map.get(15));
		map.put(3, map.get(14));
		map.put(2, map.get(13));
		map.put(11, map.get(12));

		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, MapManager.getChunkSize());
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		map.put(16,new Chunk("v2_worldmap",chunk_positions.get(16)));
		map.put(15,new Chunk("v2_worldmap",chunk_positions.get(15)));
		map.put(14,new Chunk("v2_worldmap",chunk_positions.get(14)));
		map.put(13,new Chunk("v2_worldmap",chunk_positions.get(13)));
		map.put(12,new Chunk("v2_worldmap",chunk_positions.get(12)));
		MapManager.renderChunks();
	}

	/**
	 * Moves ChunkMap down and right
	 */
	public static void moveChunksDownRight(Map<Integer,Chunk> map) {
		Point point = map.get(2).getCenter();
		map.put(20, map.get(6));
		
		map.put(19, map.get(5));
		map.put(21, map.get(7));
		
		map.put(18, map.get(4));
		map.put(6, map.get(0));
		map.put(22, map.get(8));
		
		map.put(17, map.get(15));
		map.put(5, map.get(3));
		map.put(7, map.get(1));
		map.put(23, map.get(9));

		map.put(4, map.get(14));
		map.put(0, map.get(2));
		map.put(8, map.get(10));
		
		map.put(3, map.get(13));
		map.put(1, map.get(11));

		map.put(2, map.get(12));
		
		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, MapManager.getChunkSize());
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		map.put(24,new Chunk("v2_worldmap",chunk_positions.get(24)));
		map.put(9,new Chunk("v2_worldmap",chunk_positions.get(9)));
		map.put(10,new Chunk("v2_worldmap",chunk_positions.get(10)));
		map.put(11,new Chunk("v2_worldmap",chunk_positions.get(11)));
		map.put(12,new Chunk("v2_worldmap",chunk_positions.get(12)));
		map.put(13,new Chunk("v2_worldmap",chunk_positions.get(13)));
		map.put(14,new Chunk("v2_worldmap",chunk_positions.get(14)));
		map.put(15,new Chunk("v2_worldmap",chunk_positions.get(15)));
		map.put(16,new Chunk("v2_worldmap",chunk_positions.get(16)));
		MapManager.renderChunks();
	}

	/**
	 * Moves ChunkMap right
	 */
	public static void moveChunksRight(Map<Integer,Chunk> map) {
		Point point = map.get(1).getCenter();
		map.put(16, map.get(17));
		map.put(15, map.get(4));
		map.put(14, map.get(3));
		map.put(13, map.get(2));
		map.put(12, map.get(11));

		map.put(17, map.get(18));
		map.put(4, map.get(5));
		map.put(3, map.get(0));
		map.put(2, map.get(1));
		map.put(11, map.get(10));

		map.put(18, map.get(19));
		map.put(5, map.get(6));
		map.put(0, map.get(7));
		map.put(1, map.get(8));
		map.put(10, map.get(9));

		map.put(19, map.get(20));
		map.put(6, map.get(21));
		map.put(7, map.get(22));
		map.put(8, map.get(23));
		map.put(9, map.get(24));

		Map<Integer,Point> chunk_positions = Chunk.computeChunkPositions(point, MapManager.getChunkSize());
		//TODO Attention lorsqu'on gèrera plusieurs cartes
		map.put(20,new Chunk("v2_worldmap",chunk_positions.get(20)));
		map.put(21,new Chunk("v2_worldmap",chunk_positions.get(21)));
		map.put(22,new Chunk("v2_worldmap",chunk_positions.get(22)));
		map.put(23,new Chunk("v2_worldmap",chunk_positions.get(23)));
		map.put(24,new Chunk("v2_worldmap",chunk_positions.get(24)));
		MapManager.renderChunks();
	}
}
