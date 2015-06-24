import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 * Tile.java
 * Minesweeper Solver
 * Created by Kilian Koeltzsch on 12.06.13.
 * It's all CC-BY-SA 3.0, baby!
 */

public class Tile {
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

		//pixel from tile edge
		int rgb1 = image.getRGB(5,5);
		int red1 = (rgb1 >> 16) & 0xFF;
		int green1 = (rgb1 >> 8) & 0xFF;
		int blue1 = rgb1 & 0xFF;

		//pixel from the middle
		int rgb2 = image.getRGB(20,20);
		int red2 = (rgb2 >> 16) & 0xFF;
		int green2 = (rgb2 >> 8) & 0xFF;
		int blue2 = rgb2 & 0xFF;

		//if white at edge, what number square is it
		if (MSolver.colorDifference(red1,green1,blue1,250,249,250) < 20) {

			if (MSolver.colorDifference(red2,green2,blue2,76,77,230) < 20) {
				value = 1;
			}

			if (MSolver.colorDifference(red2,green2,blue2,51,153,51) < 20) {
				value = 2;
			}

			if (MSolver.colorDifference(red2,green2,blue2,204,51,51) < 20) {
				value = 3;
			}

			if (MSolver.colorDifference(red2,green2,blue2,51,51,152) < 20) {
				value = 4;
			}

			if (MSolver.colorDifference(red2,green2,blue2,26,77,25) < 20) {
				value = 5;
			}

			if (MSolver.colorDifference(red2,green2,blue2,128,50,51) < 20) {
				value = 6;
			}

			if (MSolver.colorDifference(red2,green2,blue2,102,26,128) < 20) {
				value = 7;
			}

			if (MSolver.colorDifference(red2,green2,blue2,26,26,26) < 20) {
				value = 8;
			}

			if (MSolver.colorDifference(red2,green2,blue2,251,250,251) < 20) {
				value = 20;
			}

		} else {

			if (MSolver.colorDifference(red1,green1,blue1,254,199,57) < 20) {
				value = 10;
			}

			if (MSolver.colorDifference(red1,green1,blue1,242,81,79) < 20) {
				System.exit(0);
			}

			//otherwise tile is still unchecked and can be left at 0

		}

		return value;
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
