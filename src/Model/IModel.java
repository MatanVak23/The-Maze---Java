package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;

import java.io.File;
import java.io.IOException;
import java.util.Observer;

public interface IModel {
    void generateMaze(int rows, int cols) throws IOException;
    Maze getMaze();
    void updatePlayerLocation(MovementDirection direction);
    int getPlayerRow();
    int getPlayerCol();
    void assignObserver(Observer o);
    void solveMaze(Maze maze) throws IOException;
    Solution getSolution();
    boolean SaveMaze(File file);
    boolean OpenMaze(File file);

    void SameMaze();

    //String GetProp(String s);
}
