package de.trho.scregex;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import de.trho.fxcore.FXCoreUtils;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class HeaderView extends GridPane {

  @FXML
  private VBox                      c_headers, c_params;

  @FXML
  private JFXButton                 bt_addHeader, bt_addParam;

  private final Map<String, String> tmp = new HashMap<>();

  public Map<String, String> getHeaders() {
    tmp.clear();
    fillMap(c_headers);
    return this.tmp;
  }

  public Map<String, String> getParams() {
    tmp.clear();
    fillMap(c_params);
    return this.tmp;
  }

  private void fillMap(final Pane parent) {
    final ObservableList<Node> chlds = parent.getChildren();
    for (Node c : chlds) {
      if (c instanceof HBox) {
        final Node n1 = ((HBox) c).getChildren().get(0);
        if (n1 instanceof TextField) {
          final Node n2 = ((HBox) c).getChildren().get(1);
          final String key = ((TextField) n1).getText();
          final String value = ((TextField) n2).getText();
          if (key == null || key.isEmpty())
            continue;
          tmp.put(key, value);
        }
      }
    }
  }

  @FXML
  void addParam(final ActionEvent event) {
    addInputForm(event, c_params, true, "^[A-Za-z0-9\\%\\-\\_]+$");
  }

  @FXML
  void addHeader(final ActionEvent event) {
    addInputForm(event, c_headers, false,
        "^[\\x21\\x25\\x26\\x28\\x29\\x2D\\x2B\\x2E-\\x39\\x3F\\x40-\\x5A\\x5F\\x61-\\x7A\\x7C]+$");
  }

  private void addInputForm(final ActionEvent e, final Pane target, final boolean encoder,
      final String regex) {
    final HBox c = new HBox();
    final JFXTextField key = new JFXTextField();
    final JFXTextField value = new JFXTextField();
    final JFXButton remove = new JFXButton("-");
    remove.setOnAction((ev) -> {
      target.getChildren().remove(c);
    });
    FXCoreUtils.addValidator(regex, key, "pass", "warn");
    FXCoreUtils.addValidator(regex, value, "pass", "warn");
    c.getChildren().addAll(key, value);
    if (encoder) {
      final JFXButton enc = new JFXButton("ENC");
      enc.setOnAction((ev) -> {
        final String s = value.getText();
        if (s != null && !s.isEmpty())
          try {
            value.setText(URLEncoder.encode(value.getText(),
                java.nio.charset.StandardCharsets.UTF_8.toString()));
          } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
          }
      });
      c.getChildren().add(enc);
    }
    c.getChildren().add(remove);
    c.getChildren().forEach(cld -> HBox.setHgrow(cld, Priority.ALWAYS));
    target.getChildren().add(c);
    VBox.setVgrow(c, Priority.ALWAYS);
    e.consume();
  }

  @FXML
  void initialize() {

  }

}