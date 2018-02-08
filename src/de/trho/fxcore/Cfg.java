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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Cfg extends Properties {

  private static final long  serialVersionUID = 6920425932241661714L;
  public static final String DirPrefix        = "";
  public static final String ConfigFileName   = "config.cfg";
  public static boolean      DEV              = true;
  public final String        WorkingDirName;

  public File contextDir() {
    if (this.dir != null)
      return this.dir;
    try {
      final File dir = new File(System.getProperty("java.io.tmpdir"), WorkingDirName);
      if (!dir.exists()) {
        dir.mkdir();
        dir.createNewFile();
      }
      return dir;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  final File cfg;
  final File dir;

  public Cfg(final String dirSuffix) {
    super();
    this.WorkingDirName = Long.toHexString(serialVersionUID).toUpperCase() + dirSuffix;
    this.dir = contextDir();
    this.cfg = new File(contextDir(), ConfigFileName);
    if (DEV)
      cfg.delete();
    try {
      if (cfg.exists()) {
        this.load(Files.newInputStream(cfg.toPath(), StandardOpenOption.READ));
      } else {
        cfg.createNewFile();
        this.load(Cfg.class.getClassLoader().getResourceAsStream(DirPrefix + ConfigFileName));
        this.setProperty("file.workingdir", dir.getAbsolutePath());
        store();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void store() {
    try {
      this.store(Files.newOutputStream(cfg.toPath(), StandardOpenOption.WRITE),
          "Automatically generated configuration");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getTypedProperty(final String key, final Class<T> clazz) {
    final String p = this.getProperty(key);
    Object val = null;
    if (String.class.equals(clazz)) {
      val = p;
    } else if (Integer.class.equals(clazz)) {
      val = Integer.parseInt(p);
    } else if (Long.class.equals(clazz)) {
      val = Long.parseLong(p);
    } else if (Double.class.equals(clazz)) {
      val = Double.parseDouble(p);
    } else if (Float.class.equals(clazz)) {
      val = Float.parseFloat(p);
    } else if (Short.class.equals(clazz)) {
      val = Short.parseShort(p);
    } else if (Date.class.equals(clazz)) {
      final SimpleDateFormat sdf = new SimpleDateFormat(this.getProperty("framework.datepattern"));
      try {
        val = sdf.parse(p);
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return (T) val;
  }

  public <T> void setTypesafeProperty(final String key, final T value) {
    final String p = this.getProperty(key);
    Object val = null;
    if (value instanceof String) {
      val = p;
    } else if (value instanceof Date) {
      final SimpleDateFormat sdf = new SimpleDateFormat(this.getProperty("framework.datepattern"));
      val = sdf.format(value);
    } else {
      val = String.valueOf(value);
    }
    this.setProperty(key, String.valueOf(val));
  }

}
