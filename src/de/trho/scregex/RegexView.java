package de.trho.scregex;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import de.trho.fxcore.AppController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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

  private void toCompilerConsole(final Exception e) {
    final StringBuilder sb = new StringBuilder();
    final String oldText = tx_compiler.getText();
    sb.append("Errormsg: ").append(e.getMessage()).append("\n");
    sb.append("Cause: ").append(e.getCause()).append("\n");
    for (StackTraceElement ee : e.getStackTrace()) {
      sb.append(ee.toString()).append("\n");
    }
    sb.append((oldText != null ? oldText : ""));
    tx_compiler.setText(sb.toString());
  }

  @FXML
  private HBox                c_checkboxes;
  @FXML
  private VBox                c_compiler, c_results;
  @FXML
  private JFXCheckBox         cb_req, cb_regex;
  @FXML
  private JFXTextField        tx_url;
  @FXML
  private JFXButton           bt_compile, bt_run, bt_headers;
  @FXML
  private Text                lbl_compiler, lbl_count;
  @FXML
  private JFXTextArea         tx_compiler, tx_result, tx_regex, tx_sample;

  private Dialog<?>           hDialog;

  private HeaderView          headerView;
  private volatile Pattern    pattern;
  private final AtomicBoolean compiled = new AtomicBoolean(false);
  private final String        example  = "http://api.soundcloud.com/track/434736414/";
  private final AppController ctrl     = AppController.instance();

  @FXML
  void initialize() {
    this.headerView = ctrl.loadFxml(HeaderView.class, "HeaderView.fxml");
    this.hDialog = new Dialog<>();
    this.hDialog.setTitle("Headers & Params");
    this.hDialog.setOnCloseRequest((event) -> {
      this.hDialog.hide();
    });
    this.hDialog.getDialogPane().getStylesheets().add(ctrl.getStylesheet());
    this.hDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    this.hDialog.getDialogPane().setContent(headerView);
  }

  void rua() throws IOException {
    final OkHttpClient c = new OkHttpClient();
    final Request r = new Request.Builder()
        .url("https://soundcloud.com/when-we-dip/sets/bodys-up-radioshow-hosted-by-1").get()
        .header("accept", "application/json").build();

    Response execute = c.newCall(r).execute();
    String body = execute.body().string();
    int ix = body.indexOf("<meta property=\"twitter:player\" content=\"");
    int x = body.indexOf("\n", ix);
    String embed = body.substring(ix, x);

    Pattern p = Pattern.compile(
        "(?<=w\\.soundcloud\\.com/player/\\?url\\=https%3A%2F%2Fapi\\.soundcloud\\.com%2F)[A-Za-z0-9\\.%]+[^&]");
    Matcher m = p.matcher(embed);
    String gr = "";
    if (m.matches()) {
      gr = m.group();
    } else
      gr = "Nope...";
  }

  @FXML
  void toggleHeaderView(ActionEvent event) {
    this.hDialog.show();
  }

  @FXML
  void compile(ActionEvent event) {
    final String input = tx_regex.getText();
    if (input == null || input.isEmpty()) {
      toCompilerConsole(new IllegalArgumentException("Empty pattern!"));
      return;
    }
    try {
      this.pattern = Pattern.compile(input);
      this.compiled.set(true);
    } catch (Exception e) {
      compiled.set(false);
      toCompilerConsole(e);
      return;
    }
    sampleMatcher();
  }

  private void sampleMatcher() {
    if (!compiled.get())
      return;

  }

  @FXML
  void run(ActionEvent event) {

  }

}
