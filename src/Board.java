import java.util.HashMap;
import java.util.Map;

/**
 * Board.java
 * Minesweeper Solver
 * Created by Kilian Koeltzsch on 12.06.13.
 * It's all CC-BY-SA 3.0, baby!
 */

public class Board {
	public int boardWidth;
	public int boardHeight;
	public double boardPix;
	public int boardTopW;
	public int boardTopH;

	private HashMap<Pair,Tile> tiles;

	public Board(int boardWidth, int boardHeight, double boardPix, int boardTopW, int boardTopH) {
		this.boardWidth = boardWidth;
		this.boardHeight = boardHeight;
		this.boardPix = boardPix;
		this.boardTopW = boardTopW;
		this.boardTopH = boardTopH;
		tiles = new HashMap<Pair,Tile>(boardWidth * boardHeight);
	}

	public int getTile(int x, int y) {
		Tile tile;
		for (Map.Entry entry : tiles.entrySet()) {
			tile = (Tile)entry.getValue();
			if (tile.getX() == x && tile.getY() == y) return tile.getValue();
		}
		return -1;
	}

	public void logBoard() {
		//todo
	}
}
