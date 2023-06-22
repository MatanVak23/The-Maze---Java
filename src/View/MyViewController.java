package View;

import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyViewController implements Initializable, Observer ,IView{
    public MyViewModel viewModel;
    public WinningController winController;
    public TextField textField_mazeRows;
    public TextField textField_mazeColumns;
    public MazeDisplayer mazeDisplayer;
    public Label playerRow;
    public Label playerCol;
    public MediaPlayer mediaPlayer;
    public static Stage PrimaryStage;
    private double MousePressedTranslateX;
    private double MousePressedTranslateY;
    private MouseEvent mouseEventPressed=null;


    public javafx.scene.control.Button btn_solveMaze;

    StringProperty updatePlayerRow = new SimpleStringProperty();
    StringProperty updatePlayerCol = new SimpleStringProperty();



    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addObserver(this);
    }

    public String getUpdatePlayerRow() {
        return updatePlayerRow.get();
    }

    public void setUpdatePlayerRow(int updatePlayerRow) {
        this.updatePlayerRow.set(updatePlayerRow + "");
    }

    public String getUpdatePlayerCol() {
        return updatePlayerCol.get();
    }

    public void setUpdatePlayerCol(int updatePlayerCol) {
        this.updatePlayerCol.set(updatePlayerCol + "");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Media music = new Media(new File("resources/music/menu.mp3").toURI().toString());
        mediaPlayer= new MediaPlayer(music);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.seek(Duration.ZERO);
            }
        });
        mediaPlayer.setAutoPlay(true);
        playerRow.textProperty().bind(updatePlayerRow);
        playerCol.textProperty().bind(updatePlayerCol);

    }

    public void generateMaze(ActionEvent actionEvent) throws IOException {
        int rows = Integer.valueOf(textField_mazeRows.getText());
        int cols = Integer.valueOf(textField_mazeColumns.getText());
        //ChooseChar();
        viewModel.generateMaze(rows, cols);
    }

    public void solveMaze() throws IOException {
        setShowSOl();
        viewModel.solveMaze(viewModel.getMaze()); //send to ViewModel to solve the maze
    }

    public void openFile(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open maze");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Maze files (*.maze)", "*.maze"));
        fc.setInitialDirectory(new File("./resources"));
        File chosen = fc.showOpenDialog(null);
        //...
    }

    public void keyPressed(KeyEvent keyEvent) {
        viewModel.movePlayer(keyEvent);
        keyEvent.consume();
    }

    public void setPlayerPosition(int row, int col){
        mazeDisplayer.setPlayerPosition(row, col);
        setUpdatePlayerRow(row);
        setUpdatePlayerCol(col);
        checkIfWin();

    }
    public void Winner() {
        try {
            mediaPlayer.pause();
            Media music2 = new Media(new File("resources/music/Winsong.mp3").toURI().toString());
            mediaPlayer= new MediaPlayer(music2);
            mediaPlayer.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    mediaPlayer.seek(Duration.ZERO);
                }
            });
            mediaPlayer.setAutoPlay(true);
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Winning.fxml").openStream());
            winController=fxmlLoader.getController();
            Scene scene = new Scene(root, 600, 400);
            scene.getStylesheets().add(getClass().getResource("MainStyle.css").toExternalForm());
            winController.won(this.viewModel); //todo
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.showAndWait();
        } catch (Exception e) {
            System.out.println("error 5");
        }
    }

    public void checkIfWin()
    {
        if(viewModel.getPlayerRow()==viewModel.getMaze().getRowSize()-1 && viewModel.getPlayerCol()==this.viewModel.getMaze().getColSize()-1)
        {
            Winner();
        }
    }


    public void mouseClicked(MouseEvent mouseEvent) {
        mazeDisplayer.requestFocus();
    }

    @Override
    public void update(Observable o, Object arg) {
        String change = (String) arg;
        switch (change){
            case "maze generated" -> mazeGenerated();
            case "player moved" -> playerMoved();
            case "maze solved" -> mazeSolved();
            default -> System.out.println("Not implemented change: " + change);
        }
    }

    private void mazeSolved() {
        mazeDisplayer.setSolution(viewModel.getSolution());
    }

    private void playerMoved() {
        setPlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol());
    }

    private void mazeGenerated() {
        mediaPlayer.pause();
        Media music = new Media(new File("resources/music/theme.mp3").toURI().toString());
        mediaPlayer= new MediaPlayer(music);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.seek(Duration.ZERO);
            }
        });
        mediaPlayer.setAutoPlay(true);
       mazeDisplayer.drawMaze(viewModel.getMaze().getMaze());
    }
    public void help(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Help");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Help.fxml").openStream());
            Scene scene = new Scene(root, 400, 350);
            scene.getStylesheets().add(getClass().getResource("MainStyle.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.show();
        } catch (Exception e) {

        }
    }

    public void about(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setTitle("About");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("About.fxml").openStream());
            Scene scene = new Scene(root, 400, 350);
            scene.getStylesheets().add(getClass().getResource("MainStyle.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.show();
        } catch (Exception e) {

        }
    }

    public void properties(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Properties");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Properties.fxml").openStream());
            Scene scene = new Scene(root, 400, 350);
            scene.getStylesheets().add(getClass().getResource("MainStyle.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.show();
        } catch (Exception e) {

        }
    }




    public void exit(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure you want to exit?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            // ... user chose OK
            // Close program
            alert.close();
            Platform.exit();
            System.exit(0);

        } else {
            // ... user chose CANCEL or closed the dialog
            alert.close();
        }
    }

    public void setShowSOl(){
        boolean flag=MazeDisplayer.ShowSOl;
        MazeDisplayer.ShowSOl=!flag ;
    }

    public void hideSolution()
    {
       mazeDisplayer.setSolution(null);
    }

    /**
     * this function SaveMaze - handle save from the menu
     * @param actionEvent
     */
    public void SaveMaze(ActionEvent actionEvent){
        //open the dialog to choose file
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(PrimaryStage);
        boolean ok=viewModel.SaveMaze(file); //send to viewModel to save the maze
        if(ok==false) //status ok if the maze is saved
        {
            showAlert("can't save this maze");
        }
    }
    /**
     * this function ShowAlert - open new alert
     * @param alertMessage- the message on the alert
     */
    private void showAlert(String alertMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(alertMessage);
        alert.show();
    }

    /**
     * this function OpenMaze - handle load from the menu
     * @param actionEvent
     */
    public void OpenMaze(ActionEvent actionEvent){
        //open the dialog to choose file
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(PrimaryStage);
        boolean ok=viewModel.OpenMaze(file);
        if(ok==false) //status ok if can open the maze
        {
            showAlert("You didn't choose maze");
        }

    }

    /**
     * this function zoomIn - handle the scroll event
     * @param scrollevent
     */
    public void zoomIn(ScrollEvent scrollevent) {
        if(scrollevent.isControlDown()) {
            double zoomFactor = 1.05;
            double deltaY = scrollevent.getDeltaY();
            if (deltaY < 0) {
                zoomFactor = 1/zoomFactor;
            }
            if(mazeDisplayer.getWidth()*zoomFactor>4000 || mazeDisplayer.getHeight()*zoomFactor>4000){
                mazeDisplayer.setHeight(4000);
                mazeDisplayer.setWidth(4000);
                mazeDisplayer.draw();
                scrollevent.consume();
                return;
            }
            mazeDisplayer.setWidth(mazeDisplayer.getWidth()*zoomFactor);
            mazeDisplayer.setHeight(mazeDisplayer.getHeight()*zoomFactor);
            mazeDisplayer.draw();
            scrollevent.consume();
        }
    }

    /**
     * this function NewMaze - handle new from the menu
     * @param  actionEvent
     */
    public void NewMaze(ActionEvent actionEvent) throws IOException {
        New(); //send to function that open new scene
    }

    /**
     * this function New - open new scene
     */
    public void New()throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("MazeWindow.fxml").openStream());
        Scene scene2 = new Scene(root,800,700);
        scene2.getStylesheets().add(getClass().getResource("MainStyle.css").toExternalForm());
        PrimaryStage.setScene(scene2);
        MyViewController view = fxmlLoader.getController();
        view.setViewModel(viewModel);
        //view.setResizeEvent(scene2);
        viewModel.addObserver(view);
        mediaPlayer.stop(); //stop the music from this scene
        PrimaryStage.show();

    }

//
//    public void setResizeEvent(Scene scene) {
//        long width = 0;
//        long height = 0;
//        scene.widthProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
//            }
//        });
//        scene.heightProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
//            }
//        });
//    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        if (viewModel.getMaze().getMaze() == null)
            return;

        int mouseX, mouseY, PlayerX, PlayerY;

        mouseX = (int) (mouseEvent.getX() / (mazeDisplayer.getWidth() / viewModel.getMaze().getMaze()[0].length));
        mouseY = (int) (mouseEvent.getY() / (mazeDisplayer.getHeight() / viewModel.getMaze().getMaze().length));

        PlayerY = viewModel.getPlayerRow();
        PlayerX = viewModel.getPlayerCol();


        if (mouseY < PlayerY && mouseX == PlayerX)
        {
            KeyEvent k1= new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.NUMPAD8, false, false, false, false);
            viewModel.movePlayer(k1);

        }

        else if (mouseY > PlayerY && mouseX == PlayerX)
        {
            KeyEvent k1= new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.NUMPAD2, false, false, false, false);
            viewModel.movePlayer(k1);

        }

        else if (mouseY == PlayerY && mouseX > PlayerX)
        {
            KeyEvent k1=new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.NUMPAD6, false, false, false, false);
            viewModel.movePlayer(k1);

        }

        else if (mouseY == PlayerY && mouseX < PlayerX)
        {
            KeyEvent k1=new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.NUMPAD4, false, false, false, false);
            viewModel.movePlayer(k1);
        }

        else if (mouseY < PlayerY && mouseX > PlayerX)
        {
            KeyEvent k1=new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.NUMPAD9, false, false, false, false);
            viewModel.movePlayer(k1);
        }

        else if (mouseY > PlayerY && mouseX > PlayerX)
        {
            KeyEvent k1=new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.NUMPAD3, false, false, false, false);
            viewModel.movePlayer(k1);
        }

        else if (mouseY > PlayerY && mouseX < PlayerX)
        {
            KeyEvent k1=new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.NUMPAD1, false, false, false, false);
            viewModel.movePlayer(k1);

        }

        else if (mouseY < PlayerY && mouseX < PlayerX)
        {
            KeyEvent k1=new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.NUMPAD7, false, false,false,false);
            viewModel.movePlayer(k1);

        }
    }





    public void onMouseClicked(MouseEvent mouseEvent)
    {
        MousePressedTranslateX = mazeDisplayer.getTranslateX();
        MousePressedTranslateY = mazeDisplayer.getTranslateY();
        mouseEventPressed = mouseEvent;
    }


}
