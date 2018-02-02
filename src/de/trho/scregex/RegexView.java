package de.trho.scregex;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.reactfx.EventStreams;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import de.trho.fxcore.AppController;
import de.trho.fxcore.Cfg;
import de.trho.fxcore.FXCoreUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegexView extends GridPane {

  public enum API {
    TRACK("track"), TRACKS("tracks"), PLAYLISTS("playlists"), PLAYLIST("playlist");
    public static final String APIURL = "https://api.soundcloud.com";
    private final String       route;
    private String             url;

    private API(final String s) {
      this.route = s;
      this.url = APIURL + "/" + s;
    }

    public API with(final String sub) {
      this.url = APIURL + "/" + this.route + "/" + sub;
      return this;
    }

    public String url(final String... params) {
      if (params != null)
        for (int i = 0; i < params.length; i += 2) {
          this.url += (i == 0) ? "?" : "&";
          this.url += params[i];
          this.url += params[i + 1];
        }
      return this.url;
    }

    public String withAuth() {
      return this.url("client_id", "b70168637ad772444b9f5c34b97fe543");
    }

    public String route() {
      return this.route;
    }
  }

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
  private VBox                c_compiler, c_results;
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
  @FXML
  private ScrollPane          c_scroll;
  private Dialog<?>           hDialog;

  private HeaderView          headerView;
  private volatile Pattern    pattern;
  private volatile boolean    compiled = false;
  private final AppController ctrl     = AppController.instance();
  private final OkHttpClient  client   = new OkHttpClient();

  @FXML
  void initialize() {
    c_results.prefWidthProperty().bind(c_scroll.widthProperty());
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

  void rua() throws IOException {
    final String example = "http://api.soundcloud.com/track/434736414/";

    final Request r = new Request.Builder()
        .url("https://soundcloud.com/when-we-dip/sets/bodys-up-radioshow-hosted-by-1").get()
        .header("accept", "application/json").build();

    Response execute = client.newCall(r).execute();
    String body = execute.body().string();
    int ix = body.indexOf("<meta property=\"twitter:player\" content=\"");
    int x = body.indexOf("\n", ix);
    String embed = body.substring(ix, x);

    Pattern p = Pattern.compile(
        "(?<=w\\.soundcloud\\.com/player/\\?url\\=https%3A%2F%2Fapi\\.soundcloud\\.com%2F)[A-Za-z0-9\\.%]+[^&]");
    Matcher m = p.matcher(embed);
    c_results.getChildren().clear();
    if (m.matches()) {
      final int groupCount = m.groupCount();
      this.lbl_count.setText(String.valueOf(groupCount));
      for (int i = 0; i < groupCount; i++) {
        final JFXTextField tf = new JFXTextField();
        tf.setDisable(true);
        tf.setText(m.group());
        c_results.getChildren().add(tf);
      }
      c_results.getChildren().forEach(e -> VBox.setVgrow(e, Priority.ALWAYS));
    } else {
      this.lbl_count.setText(String.valueOf(0));
    }
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
        final Matcher m = this.pattern.matcher(v);
        c_results.getChildren().clear();
        if (m.find()) {
          int gc = 0;
          do {
            final JFXTextField tf = new JFXTextField();
            tf.setDisable(true);
            tf.setText(m.group());
            c_results.getChildren().add(tf);
            gc++;
          } while (m.find());
          this.lbl_count.setText(String.valueOf(gc));
          c_results.getChildren().forEach(e -> VBox.setVgrow(e, Priority.ALWAYS));

        } else {
          this.lbl_count.setText(String.valueOf(0));
        }
      }
    }
  }

  @FXML
  void run(ActionEvent event) {
    final String url = this.tx_url.getText();
    if (url == null || url.isEmpty()) {
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
    Alert a = new Alert(AlertType.CONFIRMATION);
    a.setTitle("Confirm request");
    a.setContentText("URL:\n" + sb.toString() + "\n" + "Headers:\n" + headers.toString());
    a.setHeaderText("Please confirm the request.");
    Optional<ButtonType> bt = a.showAndWait();
    if (!bt.isPresent() || !bt.get().equals(ButtonType.OK)) {
      return;
    }
    try {
      final Request r = new Request.Builder().url(sb.toString()).headers(headers).get().build();
      final Response res = this.client.newCall(r).execute();
      tx_sample.setText(res.body().string());
    } catch (Exception e) {
      a = new Alert(AlertType.ERROR);
      a.setTitle("Unable to do request");
      a.setHeaderText("An error occured during the request");
      a.setContentText(e.toString());
      a.show();
    }

  }

}
