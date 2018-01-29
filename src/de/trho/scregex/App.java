package de.trho.scregex;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage primaryStage) {
    try {

      final RegexView controller = new RegexView();
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("RegexView.fxml"));
      loader.setRoot(controller);
      loader.setController(controller);
      RegexView root = (RegexView) loader.load();
      Scene scene = new Scene(root);
      scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
      primaryStage.setScene(scene);
      primaryStage.show();
      primaryStage.setOnCloseRequest(event -> {
        Platform.exit();
      });
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
