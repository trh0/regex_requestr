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

import java.time.Duration;
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
            if (p.matcher(value).matches()) {
              target.getStyleClass().remove(invalidClass);
              target.getStyleClass().add(validClass);
            } else {
              target.getStyleClass().remove(validClass);
              target.getStyleClass().add(invalidClass);
            }
          } else {
            target.getStyleClass().remove(validClass);
            target.getStyleClass().remove(invalidClass);
          }
        });
  }

}
