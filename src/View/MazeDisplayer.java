package View;

import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import algorithms.search.AState;
import algorithms.search.MazeState;
import algorithms.search.Solution;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MazeDisplayer extends Canvas {
    private int[][] maze;
    private Solution solution;
    private int playerRow = 0;
    private int playerCol = 0;
    static boolean ShowSOl=false;


    StringProperty imageFileNameWall = new SimpleStringProperty();
    StringProperty imageFileNamePlayer = new SimpleStringProperty();
    StringProperty imageFileNamePass = new SimpleStringProperty();


    public boolean getShowSOl(Boolean show){
        return ShowSOl;
    }
    public int getPlayerRow() {
        return playerRow;
    }

    public int getPlayerCol() {
        return playerCol;
    }

    public void setPlayerPosition(int row, int col) {
        this.playerRow = row;
        this.playerCol = col;
        draw();
        //redraw();
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
        draw();
    }

    public String getImageFileNameWall() {
        return imageFileNameWall.get();
    }
    public String getImageFileNamePass() {
        return imageFileNamePass.get();
    }



    public String imageFileNameWallProperty() {
        return imageFileNameWall.get();
    }

    public void setImageFileNameWall(String imageFileNameWall) {
        this.imageFileNameWall.set(imageFileNameWall);
    }
    public void setImageFileNamePass(String imageFileNamePass) {
        this.imageFileNamePass.set(imageFileNamePass);
    }

    public String getImageFileNamePlayer() {
        return imageFileNamePlayer.get();
    }

    public String imageFileNamePlayerProperty() {
        return imageFileNamePlayer.get();
    }

    public void setImageFileNamePlayer(String imageFileNamePlayer) {
        this.imageFileNamePlayer.set(imageFileNamePlayer);
    }

    public void drawMaze(int[][] maze) {
        solution =null;//TODO
        this.maze = maze;
        draw();
    }



    public Solution getSolution() {
        return solution;
    }

    public void draw() {
        if(maze != null){
            double canvasHeight = getHeight();
            double canvasWidth = getWidth();
            int rows = maze.length;
            int cols = maze[0].length;

            double cellHeight = canvasHeight / rows;
            double cellWidth = canvasWidth / cols;

            GraphicsContext graphicsContext = getGraphicsContext2D();
            //clear the canvas:
            graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);
            //

            drawMazeFrame(graphicsContext,cellHeight,cellWidth,rows,cols);

//
            drawMazeWalls(graphicsContext, cellHeight, cellWidth, rows, cols);
            drawEndState(graphicsContext, cellHeight, cellWidth);
            if(solution != null)
            {

                drawSolution(graphicsContext, cellHeight, cellWidth);
            }

            drawPlayer(graphicsContext, cellHeight, cellWidth);
        }
    }

    private void drawMazeFrame(GraphicsContext graphicsContext, double cellHeight, double cellWidth, int rows, int cols) {
        double canvasHeight = rows * cellHeight;
        double canvasWidth = cols * cellWidth;

        // Clear the entire canvas
        graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);

        // Draw frame
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(3.0);

        // Draw top line
        graphicsContext.strokeLine(0, 0, canvasWidth, 0);

        // Draw left line
        graphicsContext.strokeLine(0, 0, 0, canvasHeight);

        // Draw bottom line
        graphicsContext.strokeLine(0, canvasHeight, canvasWidth, canvasHeight);

        // Draw right line
        graphicsContext.strokeLine(canvasWidth, 0, canvasWidth, canvasHeight);
    }


    public void drawSolution(GraphicsContext graphicsContext, double cellHeight, double cellWidth) {
            ArrayList<AState> mazeSolutionSteps = solution.getSolutionPath();
            Image pathPhoto = null;
            try{
                pathPhoto = new Image(new FileInputStream("./resources/images/sol1.jpg"));

            }
            catch (FileNotFoundException e)
            {
                System.out.println("There is no solution photo");
            }
            for (int i = 1; i < mazeSolutionSteps.size()-1; i++) {
                MazeState State = (MazeState) mazeSolutionSteps.get(i);
                graphicsContext.drawImage(pathPhoto, (State.get_Position().getColumnIndex()) * cellWidth, (State.get_Position().getRowIndex()) * cellHeight, cellWidth, cellHeight);
            }
            //graphicsContext.drawImage(pathPhoto, this.maze.length * cellWidth, this.maze[0].length * cellHeight, cellWidth, cellHeight);

            System.out.println("drawing solution...");

    }



    private void drawEndState(GraphicsContext graphicsContext, double cellHeight, double cellWidth) {
        Image pathPhoto = null;
        try{
            pathPhoto = new Image(new FileInputStream("./resources/images/end.png"));

        }
        catch (FileNotFoundException e)
        {
            System.out.println("There is no solution photo");
        }

        int endRow= this.maze.length-1;
        int endCol= this.maze[0].length-1;
        graphicsContext.drawImage(pathPhoto, endCol * cellHeight, endRow * cellWidth, cellWidth, cellHeight);

    }

    private void drawMazeWalls(GraphicsContext graphicsContext, double cellHeight, double cellWidth, int rows, int cols) {
        graphicsContext.setFill(Color.RED);

        Image wallImage = null;
        Image passImage = null;
        try{
            wallImage = new Image(new FileInputStream(getImageFileNameWall()));
            passImage = new Image(new FileInputStream(getImageFileNamePass()));
        } catch (FileNotFoundException e) {
            System.out.println("There is no wall image file");
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if(maze[i][j] == 1){
                    //if it is a wall:
                    double x = j * cellWidth;
                    double y = i * cellHeight;
                    if(wallImage == null)
                        graphicsContext.fillRect(x, y, cellWidth, cellHeight);
                    else
                        graphicsContext.drawImage(wallImage, x, y, cellWidth, cellHeight);
                }
                else{
                    double x = j * cellWidth;
                    double y = i * cellHeight;
                    if(passImage == null)
                        graphicsContext.fillRect(x, y, cellWidth, cellHeight);
                    else
                        graphicsContext.drawImage(passImage, x, y, cellWidth, cellHeight);
                }
            }
        }
    }

    private void drawPlayer(GraphicsContext graphicsContext, double cellHeight, double cellWidth) {
        double x = getPlayerCol() * cellWidth;
        double y = getPlayerRow() * cellHeight;
        graphicsContext.setFill(Color.GREEN);

        Image playerImage = null;
        try {
            playerImage = new Image(new FileInputStream(getImageFileNamePlayer()));
        } catch (FileNotFoundException e) {
            System.out.println("There is no player image file");
        }
        if(playerImage == null)
            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
        else
            graphicsContext.drawImage(playerImage, x, y, cellWidth, cellHeight);
    }


}
