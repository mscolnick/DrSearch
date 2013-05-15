DrSearch
========

Cell counter from image (default bmp) or group of images

Support:
  Made for Mac (some PCs might work, code may need to be adjusted)
	Made on 13" Screen (optimal size) - code can be adjusted to fit any size

Functions:

-	Rotation – in the top left corner, the knob icon can be clicked on to rotate the image about a pivot point directly in the center of the image. No resizing or mutation to the picture occurs.
-	Export Data (all)- Selecting this box in the top right corner will iterate through all pictures in the file; counting the cells in each picture to the selected florescence tolerance and cell size. The data for each picture is organized into an excel sheet which is exported to the desktop. It is titled as the date and time it was initialized.
-	Export Data (box) - to use this function, select a box on the screen and then click the “Export Data (box)” tab in top right corner. This causes the program to iterate through all the pictures and count the cells only in the region of the selected box. Like above, the data is exported into a excel sheet saved to the desktop titled with the date and time the function was initialized. 
-	Save Image – If the save image box is clicked when a box is selected on the screen, an option box will pop up and ask where the saved imaged should be saved to. Once selected, “Save Image” takes a screenshot of the box and crops it down to a small border around the image so the specific region can be analyzed further.

Settings:
-	Fill in Cell – is a box under the counted cell number that when checked, displays the parts of the cells recognized by the program by changing its color to red.
-	Cell Center- When this box is clicked, the center of the cells that the program recognizes is marked by a small blue dot. These represent all the cells the program sees and counts. When this box is unclicked, the program no longer tallies the cells on the screen.
-	Cell Edges- Selecting this box will show the edges of the cell recognized by the computer.
-	Cell Box- When this box is checked, a blue square will indicate the boundaries of the cell recognized by the program.
Display:
-	Degree of Rotation- The angle at which the picture is rotated around the pivot point is listed in the top right corner.
-	Image Index- The image number out of total images in the selected folder is given in the top right corner next to the degree of rotation value.
-	Cells- the cell count of all the cells in the selected region is given in the top middle of the display shown as “Cells: “ and then the value to the right
Commands:
-	Up/Down Arrows – The up and down arrow keys select the next and previous picture in the folder respectively.
-	Left/Right Arrows – The right and lift arrow keys increase or decrease the angle of rotation by 15 degrees.

