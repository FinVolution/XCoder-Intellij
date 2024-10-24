package com.ppdai.bicoder.config;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class BiCoderBundle extends DynamicBundle {

  private static final BiCoderBundle INSTANCE = new BiCoderBundle();

  private BiCoderBundle() {
    super("messages.bicoder");
  }

  public static String get(@NotNull @PropertyKey(resourceBundle = "messages.bicoder") String key) {
    return INSTANCE.getMessage(key);
  }

  public static String get(@NotNull @PropertyKey(resourceBundle = "messages.bicoder") String key, Object... params) {
    return INSTANCE.getMessage(key, params);
  }
}
