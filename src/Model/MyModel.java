package Model;

import IO.MyDecompressorInputStream;
import Server.ServerStrategySolveSearchProblem;
import Server.Server;
import View.WinningController;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import Client.Client;
import Client.IClientStrategy;
import algorithms.search.Solution;
import Server.ServerStrategyGenerateMaze;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Server.Configurations;



public class MyModel extends Observable implements IModel {
    private int playerRow; //
    private int playerCol;
    private Solution tempSolution;
    private Solution solution;
    private MyMazeGenerator generator;
    private Maze mazeInstane;

    private boolean Show=false;

    public MyModel() {
        generator = new MyMazeGenerator();
    }

    public void generateMaze() throws IOException
    {
        generateMaze(mazeInstane.getRowSize(),mazeInstane.getRowSize());
    }

    @Override
    public void generateMaze(int rows, int cols) throws IOException {
        Server mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
        mazeGeneratingServer.start();
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{rows, cols};
                        toServer.writeObject(mazeDimensions); //send maze dimensions to server
                        toServer.flush();
                        byte[] compressedMaze = (byte[])fromServer.readObject();
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[rows*cols+24]; //allocating byte[] for the decompressed maze
                        //byte[] decompressedMaze =new byte[mazeInstane.toByteArray().length]; //allocating byte[] for the decompressed maze
                        is.read(decompressedMaze); //Fill decompressedMaze with bytes
                        Maze maze1 = new Maze(decompressedMaze);
                        mazeInstane = maze1; //set maze to be the new maze
                        tempSolution=null; //make sure the solution is null when new maze is generated
                        solution=null;
                        movePlayer(0, 0);
                    } catch (Exception e) {
                        System.out.println("error1");
                    }
                }
            });
            client.communicateWithServer();
            setChanged();
            notifyObservers("maze generated");
        } catch (UnknownHostException e) {
            System.out.println("error2");
        }
    }



    @Override
    public void solveMaze(Maze maze) throws IOException {
            Maze TempMaze = new Maze(mazeInstane.toByteArray()); //using tempMaze for changing the start position as the character move
            TempMaze.setStartPosition(playerRow,playerCol);
            solveMazeServer(TempMaze);
            //Collections.reverse(tempSolution.getSolutionPath());
            solution= new Solution(tempSolution.getSolutionPath()); //set the solution
            setChanged();
            notifyObservers("maze solved");
    }


    /**
     * this function solveMazeSever -using part 2 of the project
     * @param maze -the maze we want to get his solution
     */
    public void solveMazeServer(Maze maze) throws IOException {
        Server solveSearchProblemServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
        solveSearchProblemServer.start();

        try {
            Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        toServer.writeObject(maze); //send maze to server
                        toServer.flush();
                        Solution mazeSolution = (Solution) fromServer.readObject();
                        tempSolution=mazeSolution;
                    } catch (Exception e) {
                        System.out.println("error3");
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            System.out.println("error4");
        }
        solveSearchProblemServer.stop();
    }

    public Maze getMaze() {return this.mazeInstane;}

    private void movePlayer(int row, int col){
        this.playerRow = row;
        this.playerCol = col;
        setChanged();
        notifyObservers("player moved");
    }

    @Override
    public int getPlayerRow() {
        return playerRow;
    }

    @Override
    public int getPlayerCol() {
        return playerCol;
    }

    @Override
    public void assignObserver(Observer o) {
        this.addObserver(o);
    }


    @Override
    public Solution getSolution() {
        return solution;
    }

    /**
     * this function SaveMaze - save maze in a directory
     * @param file - the path for the file we want to save
     * @return status if true - success
     */
    @Override
    public boolean SaveMaze(File file) {
        if (file != null) {
            try {
                ObjectOutputStream toDirectory = new ObjectOutputStream(new FileOutputStream(file));
                toDirectory.writeObject(getMaze());
                toDirectory.flush();
                return true;
            }
            catch (Exception e){
                return false;
            }
        }
        return true;
    }



    /**
     * this function SameMaze - open restart the maze that is already saved
     */
    public void SameMaze(){
        playerRow=mazeInstane.getStartPosition().getRowIndex();
        playerCol=mazeInstane.getStartPosition().getColumnIndex();
        solution=null;
        tempSolution=null;
        setChanged();
        notifyObservers();
    }

    /**
     * this function OpenMaze - open maze from directory
     * @param file - the path of the file we want to open
     * @return status if true - success
     */
    @Override
    public boolean OpenMaze(File file){
        if (file != null) {
            try {
                ObjectInputStream fromDirectory = new ObjectInputStream(new FileInputStream(file));
                Maze tempMaze = (Maze) fromDirectory.readObject();
                playerRow=tempMaze.getStartPosition().getRowIndex(); //update the character position based on the new maze
                playerCol=tempMaze.getStartPosition().getColumnIndex();
                this.mazeInstane=tempMaze;
                solution=null;
                tempSolution=null;
                setChanged();
                notifyObservers("maze generated");
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


    @Override
    public void updatePlayerLocation(MovementDirection direction) {
        switch (direction) {
            case UP -> {
                if (playerRow > 0)
                {
                    if(this.mazeInstane.getMaze()[playerRow - 1][playerCol]==0)
                    {
                        movePlayer(playerRow - 1, playerCol);
                       // checkIfWin();
                    }
                }

            }
            case UP_LEFT -> {
                if (playerRow > 0 && playerCol > 0)
                {
                    if(this.mazeInstane.getMaze()[playerRow - 1][playerCol-1]==0)
                    {
                        // 0=there is 2 ways | 1= there is 1 way | 2=the diagnole is invalid
                        int check= this.mazeInstane.getMaze()[playerRow - 1][playerCol]+this.mazeInstane.getMaze()[playerRow][playerCol - 1];
                        if(check<=1)
                        {
                            movePlayer(playerRow - 1, playerCol-1);
                           // checkIfWin();
                        }

                    }
                }
            }
            case UP_RIGHT -> {
                if (playerRow > 0 && playerCol < this.mazeInstane.getMaze()[0].length - 1)
                {
                    if(this.mazeInstane.getMaze()[playerRow - 1][playerCol + 1]==0)
                    {
                        // 0=there is 2 ways | 1= there is 1 way | 2=the diagnole is invalid
                        int check= this.mazeInstane.getMaze()[playerRow - 1][playerCol]+this.mazeInstane.getMaze()[playerRow][playerCol + 1];
                        if(check<=1)
                        {
                            movePlayer(playerRow - 1, playerCol + 1);
                           // checkIfWin();
                        }
                    }
                }
            }
            case DOWN -> {
                if (playerRow < this.mazeInstane.getMaze().length - 1)
                {
                    if(this.mazeInstane.getMaze()[playerRow + 1][playerCol]==0)
                    {
                        movePlayer(playerRow + 1, playerCol);
                        //checkIfWin();

                    }
                }
            }
            case DOWN_LEFT -> {
                if (playerRow < this.mazeInstane.getMaze().length - 1 && playerCol > 0)
                {
                    if(this.mazeInstane.getMaze()[playerRow + 1][playerCol-1]==0)
                    {
                        // 0=there is 2 ways | 1= there is 1 way | 2=the diagnole is invalid
                        int check= this.mazeInstane.getMaze()[playerRow + 1][playerCol]+this.mazeInstane.getMaze()[playerRow][playerCol - 1];
                        if(check<=1)
                        {
                            movePlayer(playerRow + 1, playerCol-1);
                            //checkIfWin();
                        }
                    }
                }
            }
            case DOWN_RIGHT -> {
                if (playerRow < this.mazeInstane.getMaze().length - 1 && playerCol < this.mazeInstane.getMaze()[0].length - 1)
                {
                    if(this.mazeInstane.getMaze()[playerRow + 1][playerCol+1]==0)
                    {
                        // 0=there is 2 ways | 1= there is 1 way | 2=the diagnole is invalid
                        int check= this.mazeInstane.getMaze()[playerRow + 1][playerCol]+this.mazeInstane.getMaze()[playerRow][playerCol + 1];
                        if(check<=1)
                        {
                            movePlayer(playerRow + 1, playerCol+1);
                            //checkIfWin();
                        }
                    }
                }
            }
            case LEFT -> {
                if (playerCol > 0)
                {
                    if(this.mazeInstane.getMaze()[playerRow][playerCol - 1]==0)
                    {
                        movePlayer(playerRow, playerCol - 1);
                        //checkIfWin();
                    }
                }
            }
            case RIGHT -> {
                if (playerCol < this.mazeInstane.getMaze()[0].length - 1)
                {
                    if(this.mazeInstane.getMaze()[playerRow][playerCol + 1]==0)
                    {
                        movePlayer(playerRow, playerCol + 1);
                       // checkIfWin();
                    }
                }
            }
        }

    }

    public void setSolution(Solution s)
    {
        this.solution=null;
    }


}
