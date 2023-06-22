package ViewModel;

import Model.IModel;
import Model.MovementDirection;
import Model.MyModel;
import View.MyViewController;
import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer {
    //private IModel model;
    private MyModel model;

    public MyViewModel(MyModel model) {
        this.model = model;
        this.model.assignObserver(this); //Observe the Model for it's changes
    }

    @Override
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers(arg);
    }

    public Maze getMaze() {
        return model.getMaze();
    }

    public void SameMaze() {
        model.SameMaze();
    }

    public int getPlayerRow() {
        return model.getPlayerRow();
    }

    public int getPlayerCol() {
        return model.getPlayerCol();
    }

    public Solution getSolution() {
        return model.getSolution();
    }

    public void generateMaze(int rows, int cols) throws IOException {
        model.generateMaze(rows, cols);
    }
    public void generateMaze() throws IOException {
        model.generateMaze();
    }

    public void movePlayer(KeyEvent keyEvent) {
        MovementDirection direction;
        switch (keyEvent.getCode()) {
            case NUMPAD1 -> direction = MovementDirection.DOWN_LEFT;
            case NUMPAD2 -> direction = MovementDirection.DOWN;
            case NUMPAD3 -> direction = MovementDirection.DOWN_RIGHT;
            case NUMPAD4 -> direction = MovementDirection.LEFT;
            case NUMPAD6 -> direction = MovementDirection.RIGHT;
            case NUMPAD7 -> direction = MovementDirection.UP_LEFT;
            case NUMPAD8 -> direction = MovementDirection.UP;
            case NUMPAD9 -> direction = MovementDirection.UP_RIGHT;
            default -> {
                // no need to move the player...
                return;
            }
        }
        model.updatePlayerLocation(direction);
    }

    public void solveMaze(Maze maze) throws IOException {
        model.solveMaze(maze);
    }

    public void setSolution(Solution s)
    {
        this.model.setSolution(s);
    }

    public boolean SaveMaze(File file){return model.SaveMaze(file);}
    public boolean OpenMaze(File file){return model.OpenMaze(file);}

}
