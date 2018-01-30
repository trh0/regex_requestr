package de.trho.fxcore;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AppController {

  public static String AppName = "";

  private AppController() {
    this.config = new Cfg(AppName);
    this.exec =
        new ThreadPoolExecutor(1, config.getTypedProperty("framework.poolsize", Integer.class), 50,
            TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
    this.loader = new FXMLLoader();
    this.stages = new Vector<>();
  }

  private static AppController Instance = new AppController();

  public static AppController instance() {
    if (Instance == null)
      Instance = new AppController();
    return Instance;
  }

  private final FXMLLoader         loader;
  private final Cfg                config;
  private final ThreadPoolExecutor exec;
  private final List<Stage>        stages;

  public String getConfigValue(final String key) {
    return config.getProperty(key);
  }

  public String setConfigValue(final String key, final String value) {
    return String.valueOf(config.setProperty(key, value));
  }

  public void runTask(Runnable r) {
    exec.execute(r);
  }

  public void initUi(final Stage stage) {
    stage.setOnCloseRequest(event -> {
      Platform.exit();
    });
    try {
      final Parent root = (Parent) loadFxml(Class.forName(config.getProperty("ui.root.class")),
          config.getProperty("ui.root.fxml"));
      final Scene scene = new Scene(root);
      scene.getStylesheets().add(AppController.class.getClassLoader()
          .getResource(config.getProperty("ui.css.file")).toExternalForm());
      final String iconPath = config.getProperty("framework.iconpath");
      if (iconPath != null) {
        stage.getIcons()
            .add(new Image(AppController.class.getClassLoader().getResourceAsStream(iconPath)));
      }
      stage.setTitle(config.getProperty("framework.stage.title"));
      stage.setWidth(config.getTypedProperty("framework.stage.width", Integer.class));
      stage.setMinWidth(config.getTypedProperty("framework.stage.minwidth", Integer.class));
      stage.setHeight(config.getTypedProperty("framework.stage.height", Integer.class));
      stage.setMinHeight(config.getTypedProperty("framework.stage.minheight", Integer.class));
      stage.setScene(scene);
      stage.show();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      stage.close();
    }
  }

  public <T> T loadFxml(Class<T> controllerClass, String fxmlLocation) {
    T viewController = null;
    try {
      viewController = controllerClass.newInstance();
      loader.setLocation(this.getClass().getClassLoader().getResource(fxmlLocation));
      loader.setRoot(viewController);
      loader.setController(viewController);
      loader.load();

    } catch (InstantiationException | IllegalAccessException | IOException e) {
      e.printStackTrace();
    }
    return viewController;
  }

  public void serveScene(final Scene scene, final boolean show) {
    final Stage stage = new Stage();
    stage.setScene(scene);
    this.stages.add(stage);
    stage.setOnCloseRequest(event -> {
      stage.setScene(null);
      stage.close();
      stages.remove(stage);
    });
    if (show)
      stage.show();
  }

  public List<Stage> stages() {
    return Collections.unmodifiableList(stages);
  }

  public void terminate() throws Exception {
    stages.forEach(s -> s.close());
    this.exec.shutdown();
  }

}
