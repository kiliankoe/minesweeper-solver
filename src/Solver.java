import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Solver.java
 * Minesweeper Solver
 * Created by Kilian Koeltzsch on 06.06.13.
 * It's all CC-BY-SA 3.0, baby!
 *
 * Minesweeper Solver Tool based on http://luckytoilet.wordpress.com/2012/12/23/2125/
 * tested with and developed for (a slightly modified) Minesweeper.app
 * by Ross Frankling http://rossfranklin.blogspot.com on OS X 10.8
 */

public class Solver {

	static int screenWidth = 1440;		//todo: get this with a function
	static int screenHeight = 900;

	static int BoardWidth = 0;
	static int BoardHeight = 1;
	static double BoardPix = 0;
	static int BoardTopW = 0;
	static int BoardTopH = 0;

	static int[][] gameBoard;		//[BoardWidth+2][BoardHeight+2]

	static int numMines = 10;			//todo: decrease every time a mine is flagged and if == 0 click any remaining unchecked tiles

	static Robot robot;


	static BufferedImage screenShotImage() {
		try {
			Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			screenWidth = captureSize.width;
			screenHeight = captureSize.height;
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
		return red + blue + green < 400;	//100 for normal, 400 for 'Mine Swept'
	}

	static int colorDifference(int r1, int g1, int b1, int r2, int g2, int b2) {
		return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs (b1 - b2);
	}

	//take screenshot, figure out board dimensions
	static void calibrate() {
		System.out.println("Calibrating screen");

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
				int rgb = bi.getRGB(w,h);	//todo: move down under if(w < 10... for minimal performance increase

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

		//initialize gameBoard array, fill with 50 = dummy field for the border
		gameBoard = new int[BoardWidth+2][BoardHeight+2];
		for (int i = 0; i < BoardWidth+2; i++)
			for (int j = 0; j < BoardHeight+2; j++) gameBoard[i][j] = 50;
		//now fill with zeros = unknown fields
		for (int i = 1; i <= BoardWidth; i++)
			for (int j = 1; j <= BoardHeight; j++) gameBoard[i][j] = 0;


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
	}

	static int mouseLocX = screenWidth / 2;
	static int mouseLocY = screenHeight / 2;

	static void moveMouse(int mouseX, int mouseY) throws Throwable {
//		int distance = Math.max(Math.abs(mouseX - mouseLocX), Math.abs(mouseY - mouseLocY));
//		int delay = distance / 4;
//		int numSteps = delay / 5;
//
//		double stepx = (double)(mouseX - mouseLocX) / (double)numSteps;
//		double stepy = (double)(mouseY - mouseLocY) / (double)numSteps;
//
//		for (int i = 0; i < numSteps; i++) {
//			robot.mouseMove(mouseLocX + (int)(i*stepx), mouseLocY + (int)(i*stepy));
//			Thread.sleep(5);
//		}
		robot.mouseMove(mouseX,mouseY);
		mouseLocX = mouseX;
		mouseLocY = mouseY;
	}

	static void clickOn(int i, int j) throws Throwable {
		i--;
		j--;
		int mouseX = BoardTopW + (int)(i * BoardPix);
		int mouseY = BoardTopH + (int)(j * BoardPix);
		moveMouse(mouseX,mouseY);

		robot.mousePress(16);
		Thread.sleep(5);
		robot.mouseRelease(16);
		Thread.sleep(10);
	}

	static void doubleClickOn(int i, int j) throws Throwable {
		i--; //adjustment for the 'new' array design
		j--;
		int mouseX = BoardTopW + (int)(i * BoardPix);
		int mouseY = BoardTopH + (int)(j * BoardPix);
		moveMouse(mouseX,mouseY);

		robot.mousePress(16);
		Thread.sleep(5);
		robot.mouseRelease(16);
		Thread.sleep(5);

		robot.mousePress(16);
		Thread.sleep(5);
		robot.mouseRelease(16);
		Thread.sleep(5);
	}

	static void flagOn(int i, int j) throws Throwable {
		i--;
		j--;
		int mouseX = BoardTopW + (int)(i*BoardPix);
		int mouseY = BoardTopH + (int)(j*BoardPix);
		moveMouse(mouseX,mouseY);

		robot.mousePress(4);
		Thread.sleep(5);
		robot.mouseRelease(4);
		Thread.sleep(10);
	}

	//loops through simple checks as long as stuff still happens
	static void simpleSolver() throws Throwable {
		boolean updateValue = true;
		while (updateValue) {
			updateGameBoard();
			logGameBoard();
			flagObviousBombs();
			updateGameBoard();
			logGameBoard();
			update();
			updateValue = updateGameBoard();
		}
	}

	static void update() throws Throwable {
		for (int i = 1; i <= BoardWidth; i++) {
			for (int j = 1; j <= BoardHeight; j++) {
				if (gameBoard[i][j] == -1) doubleClickOn(i,j);
				if (gameBoard[i][j] == -2) flagOn(i,j);
				if (gameBoard[i][j] == -3) clickOn(i,j);
			}
		}
	}

	//returns number of specified tiles surrounding the given one
	static int checkSurroundingTiles(int tileValue, int i, int j) {
		int count = 0;

		if (gameBoard[i][j-1] == tileValue) count++;		//N
		if (gameBoard[i+1][j-1] == tileValue) count++;		//NE
		if (gameBoard[i+1][j] == tileValue) count++;		//E
		if (gameBoard[i+1][j+1] == tileValue) count++;		//SE
		if (gameBoard[i][j+1] == tileValue) count++;		//S
		if (gameBoard[i-1][j+1] == tileValue) count++;		//SW
		if (gameBoard[i-1][j] == tileValue) count++;		//W
		if (gameBoard[i-1][j-1] == tileValue) count++;		//NW

		return count;
	}

	//flags last unchecked tiles next to found numbers
	static void flagsObviousBombs() throws Throwable {
		for (int i = 1; i <= BoardWidth; i++) {
			for (int j = 1; j <= BoardHeight; j++) {
				for (int k = 1; k < 9; k++) {
					//if a tile has it's amount or less unknown tiles around it and besides that no other flags or tiles flagged for being flagged, flag it's unknown neighbors
					if (gameBoard[i][j] == k && checkSurroundingTiles(0,i,j) <= k && checkSurroundingTiles(-2,i,j) == 0) {
						if (k - checkSurroundingTiles(0,i,j) == 0) { //no problemo
							flagNeighbors(i,j);
							break;
						}
						if (k - checkSurroundingTiles(0,i,j) == 1) {
							checkSurroundingTiles(10,i,j) == 0
						}
						flagNeighbors(i,j);
					}
				}
			}
		}
	}

	static void flagObviousBombs() throws Throwable {
		for (int i = 1; i <= BoardWidth; i++) {
			for (int j = 1; j <= BoardHeight; j++) {

			}
		}
	}

	//specified neighbors get flagged for clicking
	static void clickNeighbors(int tileValue, int i, int j) throws Throwable {
		if (gameBoard[i][j-1] == tileValue) gameBoard[i][j-1] = -3;
		if (gameBoard[i+1][j-1] == tileValue) gameBoard[i+1][j-1] = -3;
		if (gameBoard[i+1][j] == tileValue) gameBoard[i+1][j] = -3;
		if (gameBoard[i+1][j+1] == tileValue) gameBoard[i+1][j+1] = -3;
		if (gameBoard[i][j+1] == tileValue) gameBoard[i][j+1] = -3;
		if (gameBoard[i-1][j+1] == tileValue) gameBoard[i-1][j+1] = -3;
		if (gameBoard[i-1][j] == tileValue) gameBoard[i-1][j] = -3;
		if (gameBoard[i-1][j-1] == tileValue) gameBoard[i-1][j-1] = -3;
	}

	//unchecked neighbors get flagged for flagging
	static void flagNeighbors(int i, int j) throws Throwable {
		if (gameBoard[i][j-1] == 0) gameBoard[i][j-1] = -2;
		if (gameBoard[i+1][j-1] == 0) gameBoard[i+1][j-1] = -2;
		if (gameBoard[i+1][j] == 0) gameBoard[i+1][j] = -2;
		if (gameBoard[i+1][j+1] == 0) gameBoard[i+1][j+1] = -2;
		if (gameBoard[i][j+1] == 0) gameBoard[i][j+1] = -2;
		if (gameBoard[i-1][j+1] == 0) gameBoard[i-1][j+1] = -2;
		if (gameBoard[i-1][j] == 0) gameBoard[i-1][j] = -2;
		if (gameBoard[i-1][j-1] == 0) gameBoard[i-1][j-1] = -2;
	}

	//chord all possible tiles
	static void chordRun() throws Throwable {
		//if 1 mit 1 flag nachbarn
		//if 2 mit 2 flag nachbarn -> for schleife?
		for (int i = 1; i <= BoardWidth; i++) {
			for (int j = 1; j <= BoardHeight; j++) {
				if (gameBoard[i][j] == 1  && checkSurroundingTiles(10, i, j) == 1) {
//					System.out.println("1: " + i + "x" + j);
					doubleClickOn(i,j);
				} else if (gameBoard[i][j] == 2 && checkSurroundingTiles(10,i,j) == 2) {
//					System.out.println("2: " + i + "x" + j);
					doubleClickOn(i,j);
				}

//				for (int k = 0; k < 8; k++) {
//					if (gameBoard[i][j] == k && checkSurroundingTiles(10,i,j) == k) doubleClickOn(i,j);
//				}

			}
		}
	}

	//iterates through all tiles and checks their color values to determine their meaning and updates the gameBoard array
	//returns true if something changed
	static int detectionThreshold = 20;
	static boolean updateGameBoard() {
		boolean returnValue = false;
		int rgb1,red1,green1,blue1,rgb2,red2,green2,blue2;
		for (int i = 1; i <= BoardWidth; i++) {
			for (int j = 1; j <= BoardHeight; j++) {

				if (gameBoard[i][j] > 0) continue;		//only check necessary tiles to speed everything up

				BufferedImage tile = getSingleTile(i-1,j-1);

				//pixel from tile edge
				rgb1 = tile.getRGB(5,5);
				red1 = (rgb1 >> 16) & 0xFF;
				green1 = (rgb1 >> 8) & 0xFF;
				blue1 = rgb1 & 0xFF;

				//pixel from the middle
				rgb2 = tile.getRGB(20,20);
				red2 = (rgb2 >> 16) & 0xFF;
				green2 = (rgb2 >> 8) & 0xFF;
				blue2 = rgb2 & 0xFF;

//				System.out.println("Checking " + i + "x" + j);
//				System.out.println("Checking " + i + "x" + j + " - Colorvalue: (" + red1 + "," + green1 + "," + blue1 + ") & (" + red2 + "," + green2 + "," + blue2 + ")");

				//if white at edge, what number square is it
				if (colorDifference(red1,green1,blue1,250,249,250) < detectionThreshold) {

					if (colorDifference(red2,green2,blue2,76,77,230) < detectionThreshold) {
//						System.out.println("Found 1 - Light Blue");
						gameBoard[i][j] = 1;
						returnValue = true;
						continue;
					}

					if (colorDifference(red2,green2,blue2,51,153,51) < detectionThreshold) {
//						System.out.println("Found 2 - Green");
						gameBoard[i][j] = 2;
						returnValue = true;
						continue;
					}

					if (colorDifference(red2,green2,blue2,204,51,51) < detectionThreshold) {
//						System.out.println("Found 3 - Red");
						gameBoard[i][j] = 3;
						returnValue = true;
						continue;
					}

					if (colorDifference(red2,green2,blue2,51,51,152) < detectionThreshold) {
//						System.out.println("Found 4 - Dark Blue");
						gameBoard[i][j] = 4;
						returnValue = true;
						continue;
					}

					if (colorDifference(red2,green2,blue2,26,77,25) < detectionThreshold) {
//						System.out.println("Found 5 - Dark Green");
						gameBoard[i][j] = 5;
						returnValue = true;
						continue;
					}

					if (colorDifference(red2,green2,blue2,128,50,51) < detectionThreshold) {
//						System.out.println("Found 6 - Matte Red");
						gameBoard[i][j] = 6;
						returnValue = true;
						continue;
					}

					if (colorDifference(red2,green2,blue2,102,26,128) < detectionThreshold) {
//						System.out.println("Found 7 - Violet");
						gameBoard[i][j] = 7;
						returnValue = true;
						continue;
					}

					if (colorDifference(red2,green2,blue2,26,26,26) < detectionThreshold) {
//						System.out.println("Found 8 - Black");
						gameBoard[i][j] = 8;
						returnValue = true;
						continue;
					}

					if (colorDifference(red2,green2,blue2,251,250,251) < detectionThreshold) {
//						System.out.println("Found Empty - White");
						gameBoard[i][j] = 20;
						returnValue = true;
					}

				} else {

					if (colorDifference(red1,green1,blue1,254,199,57) < detectionThreshold) {
//						System.out.println("Found Flag - Yellow");
						gameBoard[i][j] = 10;
						returnValue = true;
						continue;
					}

					if (colorDifference(red1,green1,blue1,242,81,79) < detectionThreshold) {
//						System.out.println("Mine exploded - Red");
						System.exit(0);
					}

					//otherwise tile is still unchecked and can be left at 0

				}

			}
		}
		return returnValue;
	}

	//dumps the gameBoard array into the log / debug stuff
	static void logGameBoard() {
		for (int j = 1; j <= BoardHeight; j++) {
			for (int i = 1; i <= BoardWidth; i++) {
				if (gameBoard[i][j] == 10) {
					System.out.printf("F ");
				} else if (gameBoard[i][j] == 20) {
					System.out.printf("X ");
				} else if (gameBoard[i][j] == 50) {
					System.out.printf("+ ");
				} else {
					System.out.printf("%d ",gameBoard[i][j]);
				}
			}
			System.out.println();
		}
		System.out.println();
	}

	//saves a passed image to the hard drive / debug stuff
	static int imageCounter = 1;
	static void saveImage(BufferedImage bi) {
		try {
			ImageIO.write(bi, "png", new File("/Users/kilian/tmp/mines/out" + imageCounter + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		imageCounter++;
	}

	static BufferedImage getSingleTile(int i, int j) {
//		return screenShotImage().getSubimage(BoardTopW + (int)(i * BoardPix)-((int)BoardPix/2),BoardTopH
//				+ (int)(j * BoardPix)-((int)BoardPix/2),(int)BoardPix-((int)BoardPix/6),(int)BoardPix-((int)BoardPix/6));
		//todo BoardPix/2 instead of 20
		return screenShotImage().getSubimage((BoardTopW-22)+(int)(i*BoardPix),(BoardTopH-22)+(int)(j*BoardPix),(int)BoardPix,(int)BoardPix);
	}



	public static void main(String[] args) throws Throwable {
//		Thread.sleep(2000);
		robot = new Robot();
		calibrate();

		clickOn(BoardHeight/2, BoardWidth/2);

		updateGameBoard();
//		logGameBoard();
		flagObviousBombs();
		update();
//		updateGameBoard();
//		logGameBoard();
		chordRun();
//		updateGameBoard();
//		logGameBoard();

//		flagObviousBombs();
//		updateGameBoard();
//		logGameBoard();

	}

}
