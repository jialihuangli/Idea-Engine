package sample;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChangeScenes {
    public static void moveToCenter(URL ui, BorderPane borderpane){
        try {
            Parent root = FXMLLoader.load(ui);
            borderpane.setCenter(root);
        } catch (IOException ex) {
            Logger.getLogger(ChangeScenes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
