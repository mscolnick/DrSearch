import g4p_controls.G4P;
import g4p_controls.GAlign;
import g4p_controls.GButton;
import g4p_controls.GCheckbox;
import g4p_controls.GConstants;
import g4p_controls.GCustomSlider;
import g4p_controls.GEvent;
import g4p_controls.GKnob;
import g4p_controls.GLabel;
import g4p_controls.GTextField;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.UIManager;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import blobDetection.Blob;
import blobDetection.BlobDetection;
import blobDetection.EdgeVertex;

public class CellSearch extends PApplet {

	PImage img;

	PImage[] imgs;
	final int adjustment = 60;
	int cellColor;

	int xBox;
	int yBox;
	int boxWidth;
	int boxHeight;

	int numOfCells;
	boolean hasBox;

	boolean drawBlob;
	boolean drawEdge;
	boolean drawCenter;
	boolean drawFill;

	float tolerance;
	int cellSize;
	// SDrop drop;

	boolean validArea;

	File file;
	File[] files;

	BlobDetection theBlobDetection;
	float thresh;

	final JFileChooser fc = new JFileChooser();
	public int imgIndex;

	ArrayList<PVector> tomsPoints = new ArrayList<PVector>();

	@Override
	public void setup() {
		// System.out.println(java.lang.Runtime.getRuntime().maxMemory());
		img = loadImage("defaultPhoto.jpg");
		img.resize(displayWidth, displayHeight - adjustment);
		size(displayWidth, displayHeight);

		numOfCells = 0;
		theBlobDetection = new BlobDetection(img.width, img.height);
		// theBlobDetection.setPosDiscrimination(false);

		noFill();
		stroke(255);
		frameRate(30);
		cellColor = color(250, 0, 0); // red
		// noCursor();
		createGUI();
		cellSize = cellSizeSlider.getValueI();
		thresh = (float) thresholdSlider.getValueI() / 100;
		tolerance = (float) thresholdSlider.getValueI() * 255 / 100;

		validArea = true;
		// drop = new SDrop(this);
		imgIndex = 0;

		drawBlob = false;
		drawEdge = true;
		drawCenter = true;
		drawFill = true;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			e.printStackTrace();
		}

		browse();
	}

	@Override
	public void draw() {
		if (img != null) {

			background(250);

			// Un-comment for rotation
			pushMatrix();
			imageMode(CENTER);
			translate(img.width / 2, img.height / 2);
			rotate(rotaterKnob.getValueF() * PI);

			image(img, 0, adjustment); // fast way to draw an image
			colorMode(RGB);

			if (drawFill) {
				colorCells();
			}
			
			strokeWeight(0);
			fill(0, 0, 255);

			numOfCells = 0;

			translate(-img.width / 2, -img.height / 2);
			popMatrix();

			if (theBlobDetection != null) {
				if (theBlobDetection.getBlobNb() > BlobDetection.blobMaxNumber - 100) {
					thresholdSlider.setValue(thresh * 100 + 3);
				}
				drawBlobsAndEdgesandCenters(drawBlob, drawEdge, drawCenter);
			}
			loadingLabel.setText(((Integer) numOfCells).toString());

			// Un-comment for rotation
			
			fill(0, 200, 150);
			for (PVector p : tomsPoints) {
				ellipse(p.x, p.y, 8, 8);
			}
			strokeWeight(2);
			stroke(0, 255, 0); // green
			noFill();
			rect(xBox, yBox, boxWidth, boxHeight);
			fill(250, 0, 0); // red
		} else {
			background(10);
			text("Drag an Image", 200, 200);
		}
	}

	public void drawBlobsAndEdgesandCenters(final boolean drawBlobs,
			final boolean drawEdges, final boolean drawCenters) {
		Blob b;
		EdgeVertex eA, eB;
		PVector tempVector1;
		PVector tempVector2;
		for (int n = 0; n < theBlobDetection.getBlobNb(); n++) {
			b = theBlobDetection.getBlob(n);
			if (b != null && inBox(b)) {
				// Edges
				noFill();
				if (drawEdges) {
					strokeWeight(1);
					stroke(255, 255, 153);
					for (int m = 0; m < b.getEdgeNb(); m++) {
						eA = b.getEdgeVertexA(m);
						eB = b.getEdgeVertexB(m);
						if (eA != null && eB != null) {
							tempVector1 = rotatePoints(eA.x * img.width, eA.y
									* img.height + adjustment);
							tempVector2 = rotatePoints(eB.x * img.width, eB.y
									* img.height + adjustment);
							line(tempVector1.x, tempVector1.y, tempVector2.x,
									tempVector2.y);
						}
					}
				}

				// Blobs
				if (drawBlobs) {
					strokeWeight(2);
					stroke(64, 224, 208);
					tempVector1 = rotatePoints(b.xMin * img.width, b.yMin
							* img.height + adjustment);
					tempVector2 = rotatePoints(b.w * img.width, b.h
							* img.height);
					rect(tempVector1.x, tempVector1.y, tempVector2.x,
							tempVector2.y);
				}
				// Centers
				if (drawCenters) {
					fill(0, 0, 255);
					stroke(0);
					strokeWeight(0);
					if (b.getEdgeNb() > cellSize) {
						tempVector1 = rotatePoints(b.x * img.width, b.y
								* img.height + adjustment);
						ellipse(tempVector1.x, tempVector1.y, 6, 6);
						numOfCells++;
					}
				}
			}
		}
	}

	public void colorCells() {
		if (!hasBox) {
			return;
		}

		loadPixels();
		for (int x = xBox; x < xBox + boxWidth; x++) {
			for (int y = yBox; y < yBox + boxHeight; y++) {
				final int loc = constrain(y * img.width + x, 0,
						pixels.length - 1);
				if (brightness(pixels[loc]) > tolerance) {
					pixels[loc] = cellColor;

				}
			}
		}
		updatePixels();
	}

	public boolean inBox(final Blob b) {

		float x = b.x * img.width;
		float y = b.y * img.height + adjustment;

		PVector rotatedPoints = rotatePoints(x, y);

		x = rotatedPoints.x;
		y = rotatedPoints.y;

		return x > xBox && x < xBox + boxWidth && y > yBox
				&& y < yBox + boxHeight;

	}

	public PVector rotatePoints(float x, float y) {
		float xO = x - img.width / 2;
		float yO = img.height / 2 - y;

		float tempX = xO;
		float tempY = yO;

		xO = tempX * (float) Math.cos(rotaterKnob.getValueF() * PI) + tempY
				* (float) Math.sin(rotaterKnob.getValueF() * PI);
		yO = -tempX * (float) Math.sin(rotaterKnob.getValueF() * PI) + tempY
				* (float) Math.cos(rotaterKnob.getValueF() * PI);

		x = xO + img.width / 2;
		y = img.height / 2 - yO;

		return new PVector(x, y);
	}

	public void browse() {

		int returnVal = 0;
		try {
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fc.setDialogTitle("Pick an Image or Folder of Images");
			fc.setApproveButtonText("Accept file");
			returnVal = fc.showOpenDialog(this);
		} catch (final ArrayIndexOutOfBoundsException e) {
			System.out.println("Error...");
		}

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			System.out.println(file);

			final FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(final File directory,
						final String fileName) {
					return fileName.endsWith(".bmp")
							|| fileName.endsWith(".png");
				}
			};
			try {
				if (file.isDirectory()) {
					files = file.listFiles(filter);

					imgs = new PImage[files.length];
					BufferedImage bimg;
					PImage imag;
					for (int i = 0; i < files.length; i++) {
						bimg = ImageIO.read(files[i]);
						imag = new PImage(bimg.getWidth(), bimg.getHeight(),
								PConstants.ARGB);
						bimg.getRGB(0, 0, imag.width, imag.height, imag.pixels,
								0, imag.width);
						imgs[i] = imag;
					}
				} else if (file.isFile()) {
					imgs = new PImage[1];
					BufferedImage bimg;
					bimg = ImageIO.read(file);
					final PImage imag = new PImage(bimg.getWidth(),
							bimg.getHeight(), PConstants.ARGB);
					bimg.getRGB(0, 0, imag.width, imag.height, imag.pixels, 0,
							imag.width);
					imgs[0] = imag;
				}
				switchImage(imgs[0]);

			} catch (final IOException e) {
				e.printStackTrace();
			}

		} else {
			println("Open command cancelled by user.");
		}
	}

	public boolean validArea() {
		return mouseY > adjustment;
	}

	public void resetBox() {
		hasBox = false;

		boxWidth = 0;
		boxHeight = 0;
		xBox = 0;
		yBox = 0;

	}

	@Override
	public void mousePressed() {
		if (validArea()) {
			resetBox();
			blobber();
			boxWidth = 0;
			boxHeight = 0;
			xBox = mouseX;
			yBox = mouseY;
			hasBox = true;
		} else {
			validArea = false;
		}
	}

	@Override
	public void mouseDragged() {
		if (validArea() && validArea) {
			boxWidth = mouseX - xBox;
			boxHeight = mouseY - yBox;
		}
	}

	@Override
	public void mouseReleased() {
		if (validArea() && validArea) {
			if (boxWidth < 0) {
				xBox = mouseX;
				boxWidth *= -1;
			}
			if (boxHeight < 0) {
				yBox = mouseY;
				boxHeight *= -1;
			}
			blX.setText(Integer.toString(xBox));
			blY.setText(Integer.toString(displayHeight - yBox - boxHeight)); // -67
			// if
			// not
			// full
			// screen
			trX.setText(Integer.toString(xBox + boxWidth));
			trY.setText(Integer.toString(displayHeight - yBox)); // -67 if not
			// full
			// screen
		}
		validArea = true;
	}

	public void blobber() {
		theBlobDetection.setThreshold(thresh);
		theBlobDetection.computeBlobs(img.pixels);
	}

	@Override
	public void keyPressed() {

		// if (keyCode == 'Q') {
		// thresh += .05;
		// }
		// if (keyCode == 'W') {
		// thresh -= .05;
		// }

		if (keyCode == ENTER) {
			blobber();
			
		}

		if (keyCode == UP && imgs != null && imgIndex < imgs.length - 1) {
			imgIndex++;
			switchImage(imgs[imgIndex]);
			resetBox();
		}
		if (keyCode == DOWN && imgs != null && imgIndex > 0) {
			imgIndex--;
			switchImage(imgs[imgIndex]);
			resetBox();
		}


	}

	//
	// public void dropEvent(DropEvent theDropEvent) {
	// // println("");
	// // println("isFile()\t" + theDropEvent.isFile());
	// // println("isImage()\t" + theDropEvent.isImage());
	// // println("isURL()\t" + theDropEvent.isURL());
	//
	// BufferedImage bimg;
	// try {
	// bimg = ImageIO.read(new File(theDropEvent.filePath()));
	// PImage imag= new
	// PImage(bimg.getWidth(),bimg.getHeight(),PConstants.ARGB);
	// bimg.getRGB(0, 0, imag.width, imag.height, imag.pixels, 0, imag.width);
	// imag.updatePixels();
	// imag.resize(displayWidth*9/10, displayHeight-adjustment);
	// img = imag;
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// boxWidth = 0;
	// boxHeight = 0;
	// xBox = 0;
	// yBox = 0;

	//
	// }

	public void savePic() {
		fc.setDialogTitle("Pick a destination to save your Image");
		final int result = fc.showSaveDialog(this);

		Rectangle screenRect;
		if (xBox == 0 && yBox == 0) {
			screenRect = new Rectangle(displayWidth, displayHeight);
		} else {
			screenRect = new Rectangle(xBox + 20, yBox - 40, boxWidth + 100,
					boxHeight + 100);
		}
		final File file = fc.getSelectedFile();
		if (result == JFileChooser.APPROVE_OPTION) {
			;
		}
		{
			BufferedImage capture;
			try {
				Thread.sleep(200);
				capture = new Robot().createScreenCapture(screenRect);
				ImageIO.write(capture, "bmp", file);
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (final AWTException e) {
				e.printStackTrace();
			} catch (final InterruptedException e1) {
				e1.printStackTrace();
			}
		}

	}

	// public void exportData(){
	// String outFileName = "testData";
	// PrintWriter data = null;
	// try {
	// data = new PrintWriter(new FileOutputStream(outFileName));
	// data.println("# Results for " + imgs.length + " images");
	// data.println("# ---------------");
	// data.println("Number of Cells  |  Img Name");
	// data.println("# ---------------");
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// }
	//
	// for(PImage pic: imgs){
	// img = pic;
	// image(img, 0, adjustment);
	// resetPic();
	//
	// loadPixels();
	//
	// cells = countCells(0,adjustment,img.width,img.height);
	// points = new ArrayList<PVector>();
	// for (Cell c: cells) {
	// points.add(new PVector((int)c.center.x,(int) c.center.y));
	// }
	//
	// updatePixels();
	//
	// data.println(img.toString() + "   " + cells.size());
	//
	// }
	// data.close();
	//
	// }

	public void switchImage(final PImage image) {
		img = image;
		img.resize(displayWidth, displayHeight - adjustment);
		resetBox();

		blobber();
	}

	public void exportData(final boolean boxOnly) {

		final int regionX = xBox;
		final int regionY = yBox;
		final int regionHeight = boxHeight;
		final int regionWidth = boxWidth;

		WritableWorkbook workbook;
		try {
			final Date date = new Date();
			workbook = Workbook.createWorkbook(new File(date.toString()));
			final WritableSheet sheet = workbook.createSheet("Cell Count", 0);

			final Label nameLabel = new Label(0, 0, "Results for "
					+ imgs.length + " images");
			sheet.addCell(nameLabel);
			final Label imageLabel = new Label(0, 1, "A label record");
			sheet.addCell(imageLabel);
			final Label cellLabel = new Label(1, 1, "Number of Cells");
			sheet.addCell(cellLabel);
			final Label timeLabel = new Label(2, 1, "Time Ellapsed");
			sheet.addCell(timeLabel);

			Number imageName;
			Number cellCount;
			Number timeEllapse;
			int count;

			double totalCells = 0;
			final float totalTime = 0;

			final float start = System.currentTimeMillis();

			float tStart;
			float tFinish;
			for (int i = 0; i < imgs.length; i++) {
				tStart = System.currentTimeMillis();
				img = imgs[i];
				switchImage(img);

				loadPixels();
				count = 0;
				Blob b;
				for (int n = 0; n < theBlobDetection.getBlobNb(); n++) {
					b = theBlobDetection.getBlob(n);

					if (boxOnly) {
						final float x = b.x * img.width;
						final float y = b.y * img.height + adjustment;
						if (!(x > regionX && x < regionX + regionWidth
								&& y > regionY && y < regionY + regionHeight)) {
							continue;
						}
					}

					if (b.getEdgeNb() > cellSize) {
						count++;
					}
				}
				totalCells += count;
				System.out.println("Export  " + count);
				updatePixels();

				cellCount = new Number(1, i + 2, count);
				sheet.addCell(cellCount);
				imageName = new Number(0, i + 2, i);
				sheet.addCell(imageName);

				tFinish = System.currentTimeMillis();
				timeEllapse = new Number(2, i + 2, tFinish - tStart);
				sheet.addCell(timeEllapse);

			}
			Label totalLabel = new Label(0, imgs.length + 4, "Total cells");
			sheet.addCell(totalLabel);
			Label averageLabel = new Label(0, imgs.length + 5,
					"Avergae cell per image");
			sheet.addCell(averageLabel);
			totalLabel = new Label(0, imgs.length + 6, "Total time");
			sheet.addCell(totalLabel);
			averageLabel = new Label(0, imgs.length + 7,
					"Avergae time per image");
			sheet.addCell(averageLabel);

			Number average;
			Number total;

			average = new Number(1, imgs.length + 4, totalCells);
			sheet.addCell(average);
			total = new Number(1, imgs.length + 5, totalCells / imgs.length);
			sheet.addCell(total);

			average = new Number(1, imgs.length + 6, System.currentTimeMillis()
					- start);
			sheet.addCell(average);
			total = new Number(1, imgs.length + 7, totalTime / imgs.length);
			sheet.addCell(total);

			workbook.write();
			workbook.close();
			System.out.println("Data Exported");
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final RowsExceededException e) {
			e.printStackTrace();
		} catch (final WriteException e) {
			e.printStackTrace();
		}
	}

	/*
	 * ========================================================= ==== WARNING
	 * === ========================================================= The code in
	 * this tab has been generated from the GUI form designer and care should be
	 * taken when editing this file. Only add/edit code inside the event
	 * handlers i.e. only use lines between the matching comment tags. e.g.
	 * 
	 * void myBtnEvents(GButton button) { //_CODE_:button1:12356: // It is safe
	 * to enter your event code here } //_CODE_:button1:12356:
	 * 
	 * Do not rename this tab!
	 * =========================================================
	 */

	public void thresholdSliderChange(final GCustomSlider source,
			final GEvent event) { // _CODE_:toleranceSlider:327215:
		// println("toleranceSliderChange - GCustomSlider event occured " +
		// System.currentTimeMillis()%10000000 );
		thresh = (float) thresholdSlider.getValueI() / 100;
		tolerance = (float) thresholdSlider.getValueI() * 255 / 100;
		blobber();

	} // _CODE_:toleranceSlider:327215:

	public void cellSizeSliderChange(final GCustomSlider source,
			final GEvent event) { // _CODE_:cellSizeSlider:950381:
		// println("custom_slider2 - GCustomSlider event occured " +
		// System.currentTimeMillis()%10000000 );
		cellSize = cellSizeSlider.getValueI();
	} // _CODE_:cellSizeSlider:950381:

	public void blXChange(final GTextField source, final GEvent event) { // _CODE_:blX:801935:
		// println("textfield1 - GTextField event occured " +
		// System.currentTimeMillis()%10000000 );

	} // _CODE_:blX:801935:

	public void blYChange(final GTextField source, final GEvent event) { // _CODE_:blY:250226:
		// println("textfield2 - GTextField event occured " +
		// System.currentTimeMillis()%10000000 );
	} // _CODE_:blY:250226:

	public void trXChange(final GTextField source, final GEvent event) { // _CODE_:trX:581812:
		// println("textfield3 - GTextField event occured " +
		// System.currentTimeMillis()%10000000 );
	} // _CODE_:trX:581812:

	public void trYChange(final GTextField source, final GEvent event) { // _CODE_:trY:922832:
		// println("textfield4 - GTextField event occured " +
		// System.currentTimeMillis()%10000000 );
	} // _CODE_:trY:922832:

	public void makePointsChange(final GButton source, final GEvent event) { // _CODE_:makePoints:458437:
		// println("makePoints - GButton event occured " +
		// System.currentTimeMillis()%10000000 );
		xBox = parseInt(blX.getText());
		yBox = displayHeight - parseInt(trY.getText()); // -67 if not full
		// screen
		boxWidth = parseInt(trX.getText()) - xBox;
		boxHeight = parseInt(trY.getText()) - parseInt(blY.getText());
	} // _CODE_:makePoints:458437:

	public void saveButtonChange(final GButton source, final GEvent event) { // _CODE_:makePoints:458437:
		// savePic();
	} // _CODE_:makePoints:458437:

	public void browseButtonChange(final GButton source, final GEvent event) { // _CODE_:makePoints:458437:
		browse();
	} // _CODE_:makePoints:458437:

	public void exportButtonChange(final GButton source, final GEvent event) { // _CODE_:makePoints:458437:
		exportData(false);
	} // _CODE_:makePoints:458437:

	public void exportBoxButtonChange(final GButton source, final GEvent event) { // _CODE_:makePoints:458437:
		exportData(true);
	} // _CODE_:makePoints:458437:

	public void rotaterKnobTurn(final GKnob source, final GEvent event) { // _CODE_:rotaterKnob:715282:
		// println("rotaterKnob - GKnob event occured " +
		// System.currentTimeMillis()%10000000 );
		blobber();
	} // _CODE_:rotaterKnob:715282:

	public void blobCheckClicked(final GCheckbox source, final GEvent event) { // _CODE_:blobCheck:349341:
		// println("checkbox1 - GCheckbox event occured " +
		// System.currentTimeMillis()%10000000 );
		drawBlob = !drawBlob;
	} // _CODE_:blobCheck:349341:

	public void centerCheckClicked(final GCheckbox source, final GEvent event) { // _CODE_:centerCheck:580768:
		// println("checkbox2 - GCheckbox event occured " +
		// System.currentTimeMillis()%10000000 );
		drawCenter = !drawCenter;
	} // _CODE_:centerCheck:580768:

	public void edgesCheckClicked(final GCheckbox source, final GEvent event) { // _CODE_:edgesCheck:209331:
		// println("addEdges - GCheckbox event occured " +
		// System.currentTimeMillis()%10000000 );
		drawEdge = !drawEdge;
	} // _CODE_:edgesCheck:209331:

	public void fillCheckClicked(final GCheckbox source, final GEvent event) { // _CODE_:fillCheck:796862:
		// println("fillCheck - GCheckbox event occured " +
		// System.currentTimeMillis()%10000000 );
		drawFill = !drawFill;
	} // _CODE_:fillCheck:796862:

	// Create all the GUI controls.
	// autogenerated do not edit
	public void createGUI() {
		G4P.messagesEnabled(false);
		G4P.setGlobalColorScheme(GConstants.BLUE_SCHEME);
		G4P.setCursor(ARROW);
		if (frame != null) {
			frame.setTitle("Sketch Window");
		}
		thresholdSlider = new GCustomSlider(this, 65, 5, 230, 52, "blue18px");
		thresholdSlider.setShowValue(true);
		thresholdSlider.setShowLimits(true);
		thresholdSlider.setLimits(70, 10, 100);
		thresholdSlider.setNumberFormat(GConstants.INTEGER, 2);
		thresholdSlider.setOpaque(false);
		thresholdSlider.addEventHandler(this, "thresholdSliderChange");
		thresholdLabel = new GLabel(this, 130, 37, 80, 20);
		thresholdLabel.setTextBold();
		thresholdLabel.setText("Tolerance");
		thresholdLabel.setOpaque(false);
		cellSizeSlider = new GCustomSlider(this, 300, 5, 230, 52, "blue18px");
		cellSizeSlider.setShowValue(true);
		cellSizeSlider.setShowLimits(true);
		cellSizeSlider.setLimits(30, 10, 100);
		cellSizeSlider.setNumberFormat(GConstants.INTEGER, 2);
		cellSizeSlider.setOpaque(false);
		cellSizeSlider.addEventHandler(this, "cellSizeSliderChange");
		cellSizeLabel = new GLabel(this, 370, 37, 80, 20);
		cellSizeLabel.setTextBold();
		cellSizeLabel.setText("Cell Size");
		cellSizeLabel.setOpaque(false);
		cellLabel = new GLabel(this, 500, 2, 93, 34);
		cellLabel.setTextBold();
		cellLabel.setText("Cells:");
		cellLabel.setOpaque(false);
		bottomLeftLabel = new GLabel(this, 600, -10, 110, 41);
		bottomLeftLabel.setText("Bottom Left (x,y)");
		bottomLeftLabel.setOpaque(false);
		loadingLabel = new GLabel(this, 510, 0, 119, 40);
		loadingLabel.setTextBold();
		loadingLabel.setText("0");
		loadingLabel.setOpaque(false);
		blX = new GTextField(this, 595, 20, 55, 20, GConstants.SCROLLBARS_NONE);
		blX.addEventHandler(this, "blXChange");
		topRightLabel = new GLabel(this, 734, -10, 106, 39);
		topRightLabel.setText("Top Right (x,y)");
		topRightLabel.setOpaque(false);
		blY = new GTextField(this, 655, 20, 55, 20, GConstants.SCROLLBARS_NONE);
		blY.addEventHandler(this, "blYChange");
		trX = new GTextField(this, 724, 20, 55, 20, GConstants.SCROLLBARS_NONE);
		trX.addEventHandler(this, "trXChange");
		trY = new GTextField(this, 784, 20, 55, 20, GConstants.SCROLLBARS_NONE);
		trY.addEventHandler(this, "trYChange");
		makePoints = new GButton(this, 850, 13, 82, 30);
		makePoints.setText("Generate Box");
		makePoints.addEventHandler(this, "makePointsChange");
		browseButton = new GButton(this, 935, 13, 82, 30);
		browseButton.setText("Browse Image(s)");
		browseButton.addEventHandler(this, "browseButtonChange");
		saveButton = new GButton(this, 1020, 13, 82, 30);
		saveButton.setText("Save Image");
		saveButton.addEventHandler(this, "saveButtonChange");

		exportButton = new GButton(this, 1105, 13, 82, 30);
		exportButton.setText("Export Data (all)");
		exportButton.addEventHandler(this, "exportButtonChange");

		exportBoxButton = new GButton(this, 1190, 13, 83, 30);
		exportBoxButton.setText("Export Data (box)");
		exportBoxButton.addEventHandler(this, "exportBoxButtonChange");

		rotaterKnob = new GKnob(this, 3, 3, 50, 50, (float) 0.8);
		rotaterKnob.setTurnRange(0, 360);
		rotaterKnob.setTurnMode(GConstants.CTRL_ANGULAR);
		rotaterKnob.setShowArcOnly(false);
		rotaterKnob.setOverArcOnly(false);
		rotaterKnob.setIncludeOverBezel(false);
		rotaterKnob.setShowTrack(true);
		rotaterKnob.setLimits(0, 0, 2);
		rotaterKnob.setNbrTicks(4);
		rotaterKnob.setShowTicks(true);
		rotaterKnob.setOpaque(false);
		rotaterKnob.addEventHandler(this, "rotaterKnobTurn");

		blobCheck = new GCheckbox(this, 710, 40, 90, 20);
		blobCheck.setTextBold();
		blobCheck.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
		blobCheck.setText("Cell Border");
		blobCheck.setOpaque(false);
		blobCheck.addEventHandler(this, "blobCheckClicked");
		centerCheck = new GCheckbox(this, 620, 40, 90, 20);
		centerCheck.setTextBold();
		centerCheck.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
		centerCheck.setText("Cell Center");
		centerCheck.setOpaque(false);
		centerCheck.setSelected(true);
		centerCheck.addEventHandler(this, "centerCheckClicked");
		edgesCheck = new GCheckbox(this, 800, 40, 90, 20);
		edgesCheck.setTextBold();
		edgesCheck.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
		edgesCheck.setText("Cell Edges");
		edgesCheck.setOpaque(false);
		edgesCheck.setSelected(true);
		edgesCheck.addEventHandler(this, "edgesCheckClicked");
		fillCheck = new GCheckbox(this, 530, 40, 90, 20);
		fillCheck.setTextBold();
		fillCheck.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
		fillCheck.setText("Fill In Cell");
		fillCheck.setOpaque(false);
		fillCheck.setSelected(true);
		fillCheck.addEventHandler(this, "fillCheckClicked");
	}

	// Variable declarations
	// autogenerated do not edit
	GCustomSlider thresholdSlider;
	GLabel thresholdLabel;
	GCustomSlider cellSizeSlider;
	GLabel cellSizeLabel;
	GLabel cellLabel;
	GLabel bottomLeftLabel;
	GLabel loadingLabel;
	GTextField blX;
	GLabel topRightLabel;
	GTextField blY;
	GTextField trX;
	GTextField trY;
	GButton makePoints;
	GButton browseButton;
	GButton saveButton;
	GButton exportButton;
	GButton exportBoxButton;
	GKnob rotaterKnob;

	GCheckbox blobCheck;
	GCheckbox centerCheck;
	GCheckbox edgesCheck;
	GCheckbox fillCheck;

	public static void main(final String args[]) {
		PApplet.main(new String[] { "--present", "CellSearch" });
	}

}