import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * MSolver.java
 * Minesweeper Solver
 * Created by Kilian Koeltzsch on 12.06.13.
 * It's all CC-BY-SA 3.0, baby!
 *
 * Minesweeper Solver Tool based on http://luckytoilet.wordpress.com/2012/12/23/2125/
 * tested with and developed for (a slightly modified) Minesweeper.app
 * by Ross Frankling http://rossfranklin.blogspot.com on OS X 10.8
 *
 * v0.2
 */

public class MSolver {

	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	static private int screenWidth = (int)screenSize.getWidth();
	static private int screenHeight = (int)screenSize.getHeight();

	static int numMines;

	static Board board;
	static Robot robot;


	/**
	 * Image and Color
	 */

	static BufferedImage screenShotImage() {
		try {
			Rectangle captureSize = new Rectangle(screenSize);
			return robot.createScreenCapture(captureSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static boolean isDark(int rgb) {
		int red = (rgb >> 16) & 0xFF;
		int green = (rgb >> 8) & 0xFF;
		int blue = rgb & 0xFF;
		return red + blue + green < 200;
	}

	static int colorDifference(int r1, int g1, int b1, int r2, int g2, int b2) {
		return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs (b1 - b2);
	}

	static void calibrate() {
		int BoardTopW;
		int BoardTopH;
		int BoardWidth = 0;
		int BoardHeight = 1;
		double BoardPix;

		System.out.println("Calibrating screen...");

		BufferedImage bi = screenShotImage();
		bi.createGraphics();
		Graphics2D g = (Graphics2D)bi.getGraphics();

		int hh = 0;		//boardheight of previous column
		int firh = 0;	//position of first found
		int firw = 0;
		int lash = 0;	//position of last found
		int lasw = 0;
		int tot = 0;	//total number of crosses found

		for (int w = 0; w < screenWidth; w++) {
			for (int h = 0; h < screenHeight; h++) {
				int rgb = bi.getRGB(w,h);

				if (isDark(rgb)) {
					if (w < 10 || h < 10 || w > screenWidth - 10 || h > screenHeight - 10) continue;	//don't test close to the screen edges

					// look for the cross shape to indicate position on board
					// we consider it a cross if:
					//   - the square is dark
					//   - four selected pixels to the N,S,E,W are dark
					//   - four selected pixels to the NE, SE, NW, SW are not dark

					//check N,S,E,W far
					if (isDark(bi.getRGB(w+7,h)) && isDark(bi.getRGB(w-7,h)) && isDark(bi.getRGB(w,h+7)) && isDark(bi.getRGB(w,h-7))) {
						//check N,S,E,W near
						if (isDark(bi.getRGB(w+3,h)) && isDark(bi.getRGB(w-3,h)) && isDark(bi.getRGB(w,h+3)) && isDark(bi.getRGB(w,h-3))) {
							//check NE,SE,SW,NW far
							if (!isDark(bi.getRGB(w-7,h-7)) && !isDark(bi.getRGB(w+7,h-7)) && !isDark(bi.getRGB(w-7,h+7)) && !isDark(bi.getRGB(w+7,h+7))) {
								//check NE,SE,SW,NW near
								if (!isDark(bi.getRGB(w-3,h-3)) && !isDark(bi.getRGB(w+3,h-3)) && !isDark(bi.getRGB(w-3,h+3)) && !isDark(bi.getRGB(w+3,h+3))) {
									g.setColor(Color.YELLOW);
									g.fillRect(w-3,h-3,7,7);
									tot++;
									BoardHeight++;

									//first cross
									if (firh == 0) {
										firh = h;
										firw = w;
									}

									//last cross
									lash = h;
									lasw = w;
								}
							}
						}
					}
				}

			}

			//update BoardHeight
			if (BoardHeight > 1) {
				hh = BoardHeight;
				BoardHeight = 1;
			}
		}

		//determine BoardWidth from total and BoardHeight
		BoardHeight = hh;
		if (tot % (BoardHeight - 1) == 0)
			BoardWidth = tot / (BoardHeight - 1) + 1;
		else BoardWidth = 0;

		//determine BoardPix
		BoardPix = 0.5 * ((double)(lasw - firw) / (double)(BoardWidth - 2))
				+ 0.5 * ((double)(lash - firh) / (double)(BoardHeight - 2));

		//determine first cell position
		int halfsiz = (int)BoardPix / 2;
		BoardTopW = firw - halfsiz + 3;
		BoardTopH = firh - halfsiz + 3;

		System.out.printf("BoardWidth=%d, BoardHeight=%d, BoardPix=%f\n", BoardWidth, BoardHeight, BoardPix);
		System.out.printf("BoardTopW=%d, BoardTopH=%d\n", BoardTopW, BoardTopH);

		if(BoardWidth < 5 || BoardHeight < 5 || BoardWidth > 30 || BoardWidth > 30){
			System.out.println("Calibration Failed.");
			System.out.println("Please increase the tile size or check");
			System.out.println("if anything is obstructing the game window.");

			System.exit(1);
		}

		board = new Board(BoardWidth,BoardHeight,BoardPix,BoardTopW,BoardTopH);
	}

	/**
	 * Movement and Clicking
	 */

	static void moveMouse(int x, int y) {
		robot.mouseMove(x,y);
	}

	static void clickOn(int x, int y) {
		int mouseX = board.boardTopW + (int)(x * board.boardPix):
		int mouseY = board.boardTopH + (int)(y * board.boardPix);
		try {
			moveMouse(mouseX,mouseY);
			robot.mousePress(16);
			Thread.sleep(5);
			robot.mouseRelease(16);
			Thread.sleep(10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void doubleClickOn(int x, int y) {
		int mouseX = board.boardTopW + (int)(x * board.boardPix):
		int mouseY = board.boardTopH + (int)(y * board.boardPix);
		try {
			moveMouse(mouseX,mouseY);
			robot.mousePress(16);
			Thread.sleep(5);
			robot.mouseRelease(16);
			Thread.sleep(10);
			robot.mousePress(16);
			Thread.sleep(5);
			robot.mouseRelease(16);
			Thread.sleep(10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void flagOn(int x, int y) {
		int mouseX = board.boardTopW + (int)(x * board.boardPix):
		int mouseY = board.boardTopH + (int)(y * board.boardPix);
		try {
			moveMouse(mouseX,mouseY);
			robot.mousePress(4);
			Thread.sleep(5);
			robot.mouseRelease(4);
			Thread.sleep(10);
		} catch (Exception e) {
			e.printStackTrace();
		}

		numMines--;
	}





	public static void main(String[] args) {

	}

}
