import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 * Tile.java
 * Minesweeper Solver
 * Created by Kilian Koeltzsch on 12.06.13.
 * It's all CC-BY-SA 3.0, baby!
 */

public class Tile { //todo: rebuild Solver code completely, use this and Board class
	private int posX;
	private int posY;
	private BufferedImage image;
	private int value;

	//doesn't really matter where they are, as long as they are neighbors
	private LinkedList<Tile> neighbors = new LinkedList<Tile>();

	public Tile(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
		this.image = Solver.getSingleTile(posX, posY);
		this.value = refreshValue(image);
	}

	//only needs to be called once on every tile after all have been initialized
	public void fillNeighbors() {
		//todo: fill out neighbors[], somehow using BoardWidth & BoardHeight
	}

	public int refreshValue(BufferedImage image) {
		//todo: read value from given BufferedImage
		return 0;
	}

	public LinkedList<Tile> uncheckedNeighbors() {
		LinkedList<Tile> returnList = new LinkedList<Tile>();
		for (Tile tile : neighbors) {
			if (tile.getValue() == 0) returnList.add(tile);
		}
		return returnList;
	}

	private void setImage(BufferedImage image) {
		this.image = image;
		this.value = refreshValue(image);
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public int getX() {
		return posX;
	}

	public int getY() {
		return posY;
	}
}
