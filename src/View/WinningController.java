package View;

import Server.Configurations;
import ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class WinningController {
    public javafx.scene.control.Button newGameButtun;
    public javafx.scene.control.Button exitButtun;
    public javafx.scene.image.ImageView imageWin;
    public MyViewModel viewModel;
    public MazeDisplayer mazeDisplayer;
    public MediaPlayer mediaPlayer;

    public void mouseClicked(MouseEvent mouseEvent) {
        mazeDisplayer.requestFocus();
    }
    public void won(MyViewModel viewModel)
    {
        this.viewModel=viewModel;
        File file = new File("resources/images/win.jpg");
        Image player = new Image(file.toURI().toString());
        imageWin.setImage(player);
    }

    public void exit1()
    {
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

    public void newGame () throws IOException {
        viewModel.generateMaze();
        Stage stage = (Stage)newGameButtun.getScene().getWindow();
        stage.close(); //close the stage
    }

}
