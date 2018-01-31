package de.trho.fxcore;

import java.time.Duration;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.reactfx.EventStreams;
import javafx.scene.control.TextField;

public class FXCoreUtils {
  private FXCoreUtils() {}

  public static void addValidator(final String regex, final TextField target,
      final String validClass, final String invalidClass) {
    final Pattern p = Pattern.compile(regex);
    EventStreams.valuesOf(target.textProperty()).successionEnds(Duration.ofMillis(250))
        .subscribe((value) -> {
          if (value != null && !value.isEmpty()) {
            final char[] cs = value.toCharArray();
            final int[] ar = new int[cs.length];
            for (int i = 0; i < cs.length; i++) {
              ar[i] = cs[i];
            }
            System.out.println(value + " " + Arrays.toString(ar));
            if (p.matcher(value).matches()) {
              target.getStyleClass().remove(invalidClass);
              target.getStyleClass().add(validClass);
            } else {
              target.getStyleClass().remove(validClass);
              target.getStyleClass().add(invalidClass);
            }
          }
        });
  }

}
