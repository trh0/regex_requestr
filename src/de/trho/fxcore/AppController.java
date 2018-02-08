/*
 * Copyright (C) 2018 trh0 - https://trho.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package de.trho.fxcore;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AppController {

  public static String AppName = "regexRequestR";

  private AppController() {
    this.config = new Cfg(AppName);
    this.rejectedExec = Executors.newSingleThreadExecutor();
    this.exec = new ScheduledThreadPoolExecutor(
        config.getTypedProperty("fxcore.executor.poolsize", Integer.class),
        (RejectedExecutionHandler) (r, executor) -> {
          this.rejectedExec.execute(r);
        });
    this.loader = new FXMLLoader();
    this.stages = new Vector<>();
  }

  private static AppController Instance = new AppController();

  public static AppController instance() {
    if (Instance == null)
      Instance = new AppController();
    return Instance;
  }

  private final FXMLLoader               loader;
  private final Cfg                      config;
  private final ScheduledExecutorService exec;
  private final ExecutorService          rejectedExec;
  private final List<Stage>              stages;
  private volatile Stage                 mStage;

  public String getConfigValue(final String key) {
    return config.getProperty(key);
  }

  public String setConfigValue(final String key, final String value) {
    return String.valueOf(config.setProperty(key, value));
  }

  public Future<Boolean> runTask(final Runnable r) {
    return exec.submit(Executors.callable(r, true));
  }

  public <T> Future<T> runTask(final Callable<T> c) {
    return exec.submit(c);
  }

  public <T> Future<Boolean> runTask(final Callable<T> c, final Property<T> valueTarget) {
    return exec.submit(() -> {
      try {
        final T result = c.call();
        valueTarget.setValue(result);
        return true;
      } catch (Exception e) {
        return false;
      }
    });
  }

  public void initUi(final Stage stage) {
    this.mStage = stage;
    mStage.setOnCloseRequest(event -> {
      try {
        this.terminate();
      } catch (Exception e) {
        Platform.exit();
      }
    });
    try {
      final Parent root = (Parent) loadFxml(Class.forName(config.getProperty("fxcore.root.class")),
          config.getProperty("fxcore.root.fxml"));
      final Scene scene = new Scene(root);
      scene.getStylesheets().add(getStylesheet());
      final String iconPath = config.getProperty("fxcore.iconpath");
      if (iconPath != null) {
        mStage.getIcons()
            .add(new Image(AppController.class.getClassLoader().getResourceAsStream(iconPath)));
      }
      mStage.setTitle(config.getProperty("fxcore.stage.title"));
      mStage.setWidth(config.getTypedProperty("fxcore.stage.width", Integer.class));
      mStage.setMinWidth(config.getTypedProperty("fxcore.stage.minwidth", Integer.class));
      mStage.setHeight(config.getTypedProperty("fxcore.stage.height", Integer.class));
      mStage.setMinHeight(config.getTypedProperty("fxcore.stage.minheight", Integer.class));
      mStage.setScene(scene);
      mStage.show();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      mStage.close();
    }
  }

  public <T> T loadFxml(final Class<T> controllerClass, final String fxmlLocation) {
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

  public void serveContent(final Scene scene, final boolean show, final double x, final double y) {
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

  public void serveContent(final Parent content, final boolean show, final double x,
      final double y) {
    this.serveContent(new Scene(content), show, x, y);
  }

  public String getStylesheet() {
    return AppController.class.getClassLoader().getResource(config.getProperty("fxcore.css.file"))
        .toExternalForm();
  }

  public List<Stage> stages() {
    return Collections.unmodifiableList(stages);
  }

  public void terminate() throws Exception {
    stages.forEach(s -> s.close());
    this.exec.shutdown();
    this.rejectedExec.shutdown();
    Platform.exit();
  }

}
