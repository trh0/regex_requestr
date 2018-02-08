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
package de.trho.scregex;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.reactfx.EventStreams;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import de.trho.fxcore.AppController;
import de.trho.fxcore.Cfg;
import de.trho.fxcore.FXCoreUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegexView extends GridPane {

  private void toCompilerConsole(final Object value) {
    final StringBuilder sb = new StringBuilder();
    if (value instanceof Exception) {
      final Exception e = (Exception) value;
      sb.append("Errormsg: ").append(e.getMessage()).append("\n");
    } else {
      sb.append(String.valueOf(value)).append('\n');
    }
    tx_compiler.setText(sb.toString());
  }

  @FXML
  private HBox                c_checkboxes;
  @FXML
  private VBox                c_compiler;
  @FXML
  private JFXListView<String> listView;
  @FXML
  private JFXCheckBox         cb_req;
  @FXML
  private JFXTextField        tx_url;
  @FXML
  private JFXButton           bt_headers;
  @FXML
  private Text                lbl_compiler, lbl_count;
  @FXML
  private JFXTextArea         tx_compiler, tx_regex, tx_sample;
  private Dialog<?>           hDialog;

  private HeaderView          headerView;
  private volatile Pattern    pattern;
  private volatile boolean    compiled = false;
  private final AppController ctrl     = AppController.instance();
  private final OkHttpClient  client   = new OkHttpClient();

  @FXML
  void initialize() {
    listView.getItems();
    this.headerView = ctrl.loadFxml(HeaderView.class, Cfg.DirPrefix + "HeaderView.fxml");
    this.hDialog = new Dialog<>();
    this.hDialog.setTitle("Headers & Params");
    this.hDialog.setOnCloseRequest((event) -> {
      this.hDialog.hide();
    });
    this.hDialog.getDialogPane().getStylesheets().add(ctrl.getStylesheet());
    this.hDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    this.hDialog.getDialogPane().setContent(headerView);
    EventStreams.valuesOf(tx_sample.textProperty()).successionEnds(Duration.ofMillis(500))
        .subscribe((v) -> this.sampleMatcher(v));
    EventStreams.valuesOf(tx_regex.textProperty()).successionEnds(Duration.ofMillis(1000))
        .subscribe((v) -> this.compile(v));
    FXCoreUtils.addValidator(
        "^(http(s)?(:\\/\\/))?(www\\.)?[a-zA-Z0-9-_\\.]+(\\.[a-zA-Z0-9]{2,})([-a-zA-Z0-9:%_\\+.~#?&//=]*)",
        this.tx_url, "pass", "warn");
  }

  @FXML
  void toggleHeaderView(ActionEvent event) {
    this.hDialog.show();
    event.consume();
  }

  void compile(final String input) {
    if (input == null || input.isEmpty()) {
      toCompilerConsole("Empty pattern.");
      return;
    }
    try {
      this.pattern = Pattern.compile(input);
      this.compiled = true;
      toCompilerConsole("Compiled - ok!");
      this.sampleMatcher(this.tx_sample.getText());
    } catch (Exception e) {
      this.compiled = false;
      toCompilerConsole(e);
      return;
    }
  }

  private void sampleMatcher(final String v) {
    if (this.compiled) {
      if (v != null && !v.isEmpty()) {
        this.listView.getItems().clear();
        ctrl.runTask(() -> {
          final AtomicInteger gc = new AtomicInteger(0);
          final Matcher m = this.pattern.matcher(v);
          if (m.find()) {
            final List<String> results = new ArrayList<>();
            do {
              results.add(m.group());
              gc.incrementAndGet();
            } while (m.find());
            Platform.runLater(() -> this.listView.getItems().addAll(results));
          }
          Platform.runLater(() -> this.lbl_count.setText(String.valueOf(gc.get())));
        });
      }
    }
  }

  @FXML
  void run(ActionEvent event) {
    final String url = this.tx_url.getText();
    if (url == null || url.trim().isEmpty()) {
      final Alert alert = new Alert(AlertType.WARNING);
      alert.setTitle("Invalid URL");
      alert.setHeaderText("The URL You provided is empty.");
      alert.show();
      return;
    }
    final Map<String, String> hmap = this.headerView.getHeaders();
    final Map<String, String> params = this.headerView.getParams();
    final Headers headers = Headers.of(hmap);
    final StringBuilder sb = new StringBuilder();
    int i = 0;
    sb.append(this.tx_url.getText());
    for (Entry<String, String> entry : params.entrySet()) {
      if (i == 0) {
        sb.append('?');
        i++;
      } else {
        sb.append('&');
      }
      sb.append(entry.getKey()).append('=').append(entry.getValue());
    }

    final Alert alert_conf = new Alert(AlertType.CONFIRMATION);
    alert_conf.setTitle("Confirm request");
    alert_conf.setContentText("URL:\n" + sb.toString() + "\n" + "Headers:\n" + headers.toString());
    alert_conf.setHeaderText("Please confirm the request.");
    final Optional<ButtonType> bt = alert_conf.showAndWait();
    if (!bt.isPresent() || !bt.get().equals(ButtonType.OK)) {
      return;
    }
    ctrl.runTask(() -> {
      try {
        final Request r = new Request.Builder().url(sb.toString()).headers(headers).get().build();
        final Response res = this.client.newCall(r).execute();
        tx_sample.setText(res.body().string());
      } catch (final Exception e) {
        Platform.runLater(() -> {
          final Alert alert_ex = new Alert(AlertType.ERROR);
          alert_ex.setTitle("Unable to do request");
          alert_ex.setHeaderText("An error occured during the request");
          alert_ex.setContentText(e.toString());
          alert_ex.show();
        });
      }
    });
  }

}
