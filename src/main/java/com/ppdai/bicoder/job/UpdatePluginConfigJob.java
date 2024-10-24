package com.ppdai.bicoder.job;

import com.intellij.openapi.application.ApplicationManager;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.model.ConfigData;
import com.ppdai.bicoder.service.BiCoderService;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.TimerTask;
import java.util.concurrent.*;

public class UpdatePluginConfigJob {

    private static final UpdatePluginConfigJob INSTANCE = new UpdatePluginConfigJob();

    private ScheduledExecutorService executor;

    private final BiCoderService biCoderService;

    private UpdatePluginConfigJob() {
        executor = new ScheduledThreadPoolExecutor(1, Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        biCoderService = ApplicationManager.getApplication().getService(BiCoderService.class);
    }

    public static UpdatePluginConfigJob getInstance() {
        return INSTANCE;
    }

    public void startUpdateJob() {
        BiCoderLoggerUtils.getInstance(getClass()).info("start update plugin config job");
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(1, Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        }
        //每小时更新同步一次服务器配置
        executor.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateConfig();
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    public void stopUpdateJob() {
        BiCoderLoggerUtils.getInstance(getClass()).info("stop update plugin config job");
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    public void updateConfig() {
        try {
            UserSetting userSetting = UserSetting.getInstance();
            ConfigData config = biCoderService.getConfig();
            if (config == null) {
                return;
            }
            if (CollectionUtils.isNotEmpty(config.getAllowVersions())) {
                userSetting.setAllowVersion(config.getAllowVersions());
            }
            if (CollectionUtils.isNotEmpty(config.getFileWhitelist())) {
                userSetting.setFileWhitelist(config.getFileWhitelist());
            }
            if (config.getMaxConsecutiveInputIntervalTime() != null) {
                userSetting.setMaxConsecutiveInputIntervalTime(config.getMaxConsecutiveInputIntervalTime());
            }
            if (config.getMaxPrefixOffset() != null) {
                userSetting.setMaxPrefixOffset(config.getMaxPrefixOffset());
            }
            if (config.getMaxPrefixOffsetPercent() != null) {
                userSetting.setMaxPrefixOffsetPercent(config.getMaxPrefixOffsetPercent());
            }
            if (config.getMaxSuffixOffset() != null) {
                userSetting.setMaxSuffixOffset(config.getMaxSuffixOffset());
            }
            if (config.getMaxSuffixOffsetPercent() != null) {
                userSetting.setMaxSuffixOffsetPercent(config.getMaxSuffixOffsetPercent());
            }
            if (config.getSimilarityPercentage() != null) {
                userSetting.setSimilarityPercentage(config.getSimilarityPercentage());
            }
            if (config.getValidPrefixLength() != null) {
                userSetting.setValidPrefixLength(config.getValidPrefixLength());
            }
            if (StringUtils.isNotBlank(config.getValidSuffixReg())) {
                userSetting.setValidSuffixReg(config.getValidSuffixReg());
            }
            if (config.getUnitTestLanguageFrameworkMap() != null) {
                userSetting.setAllUintTestFramework(config.getUnitTestLanguageFrameworkMap());
            }
            if (config.getMaxCurrentOpenedEditorCacheUseSize() != null) {
                userSetting.setMaxCurrentOpenedEditorCacheUseSize(config.getMaxCurrentOpenedEditorCacheUseSize());
            }
            BiCoderLoggerUtils.getInstance(getClass()).info("updateConfig success,config:" + config);
        } catch (Exception e) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("updateConfig error", e);
        }
    }
}
