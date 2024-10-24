package com.ppdai.bicoder.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.ppdai.bicoder.config.PluginStaticConfig;
import org.jetbrains.annotations.NotNull;

/**
 * 日志工具类
 *
 */
public class BiCoderLoggerUtils {

    public static Logger getInstance(@NotNull Class<?> clazz) {
        return Logger.getInstance(clazz.getSimpleName() + "-" + PluginStaticConfig.PLUGIN_NAME);
    }
}
