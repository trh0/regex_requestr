package de.trho.scregex;

import de.trho.fxcore.AppController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage stage) {
    try {
      AppController.AppName = "regex";
      AppController.instance().initUi(stage);;
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    // try {
    // final RegexView controller = new RegexView();
    // FXMLLoader loader = new FXMLLoader();
    // loader.setLocation(App.class.getClassLoader().getResource("RegexView.fxml"));
    // loader.setRoot(controller);
    // loader.setController(controller);
    // RegexView root = (RegexView) loader.load();
    // Scene scene = new Scene(root);
    // scene.getStylesheets()
    // .add(App.class.getClassLoader().getResource("application.css").toExternalForm());
    // primaryStage.setScene(scene);
    // primaryStage.show();
    // primaryStage.setOnCloseRequest(event -> {
    // Platform.exit();
    // });
    // } catch (Exception e) {
    // e.printStackTrace();
    // System.exit(1);
    // }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
