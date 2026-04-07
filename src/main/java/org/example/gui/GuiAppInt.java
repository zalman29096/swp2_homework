package org.example.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.App;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class GuiAppInt extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Locale initial = Locale.US;
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("Translations", initial, new App.UTF8Control());
        } catch (MissingResourceException ex) {
            bundle = ResourceBundle.getBundle("Translations", Locale.US, new App.UTF8Control());
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/main_view.fxml"), bundle);
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.setStage(primaryStage);
        controller.setInitialLocale(initial);

        Scene scene = new Scene(root, 820, 560);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
