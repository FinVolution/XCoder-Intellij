package com.ppdai.bicoder.service;

import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.model.*;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import com.ppdai.bicoder.utils.HttpUtils;
import com.ppdai.bicoder.utils.InfoUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class BiCoderService {

    private final Gson gson = new Gson();

    private final StateService stateService = ApplicationManager.getApplication().getService(StateService.class);

    public BiCoderCompletion getCompletion(BiCoderCompletionRequest request) {
        stateService.updateLoadingState(true);
        BiCoderCompletionResponse completionResponse = getCompletionResponse(request);
        BiCoderCompletion biCoderCompletion = null;
        if (completionResponse.getCode() != null && completionResponse.getCode() == 100000) {
            biCoderCompletion = new BiCoderCompletion();
            biCoderCompletion.setCompletionId(request.getGenerateUUID());
            biCoderCompletion.setText(completionResponse.getText());
            BiCoderLoggerUtils.getInstance(getClass()).info("getCompletion success! completionId:" + request.getGenerateUUID() + ",text:" + completionResponse.getText());
        } else {
            BiCoderLoggerUtils.getInstance(getClass()).warn("getCompletion failed! code:" + completionResponse.getCode() + ",msg:" + completionResponse.getMessage());
        }
        stateService.updateLoadingState(false);
        return biCoderCompletion;
    }

    public ConfigData getConfig() {
        BiCoderLoggerUtils.getInstance(getClass()).info("start getConfig!");
        String responseBody = HttpUtils.doPost(UserSetting.getInstance().getRequestHost() + PluginStaticConfig.CONFIG_URL, gson.toJson(new BiCoderConfigRequest.Builder().build()), null);
        if (StringUtils.isBlank(responseBody)) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("getConfig failed! responseBody is blank!");
        } else {
            try {
                BiCoderConfigResponse response = new Gson().fromJson(responseBody, BiCoderConfigResponse.class);
                if (response.getCode() != null && response.getCode() == 100000) {
                    BiCoderLoggerUtils.getInstance(getClass()).info("getConfig success! config:" + response.getData());
                    return response.getData();
                } else {
                    BiCoderLoggerUtils.getInstance(getClass()).warn("getConfig failed! code:" + response.getCode() + ",msg:" + response.getMessage());
                }
            } catch (Exception e) {
                BiCoderLoggerUtils.getInstance(getClass()).warn("getConfig failed! Exception:" + e.getMessage());
            }
        }
        return null;
    }


    public BiCoderCompletion getMockCompletion(BiCoderCompletionRequest request) {
        //mock 一个返回
        BiCoderCompletion completion = new BiCoderCompletion();
        completion.setText("int c = 10;");
        completion.setCompletionId("mockCompletionId");
        return completion;
    }

    @NotNull
    private BiCoderCompletionResponse getCompletionResponse(BiCoderCompletionRequest request) {
        BiCoderLoggerUtils.getInstance(getClass()).info("getCompletionResponse start!");
        String responseBody = HttpUtils.doPost(UserSetting.getInstance().getRequestHost() + PluginStaticConfig.COMPLETION_REQUEST_URL, gson.toJson(request), null);
        if (StringUtils.isBlank(responseBody)) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("getCompletionResponse failed! responseBody is blank!");
            return new BiCoderCompletionResponse();
        }
        return parseCompletionResponse(responseBody);
    }

    @NotNull
    private BiCoderCompletionResponse parseCompletionResponse(String responseBody) {
        BiCoderCompletionResponse biCoderCompletionResponse = new BiCoderCompletionResponse();
        try {
            biCoderCompletionResponse = new Gson().fromJson(responseBody, BiCoderCompletionResponse.class);
        } catch (Exception e) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("parseCompletionResponse failed! responseBody:" + responseBody);
        }
        return biCoderCompletionResponse;
    }

    /**
     * 将代码提示处理成单行,方便后续处理
     *
     * @param generatedText 生成的代码提示
     * @return 代码提示单行列表
     */
    @Nullable
    public String[] buildCompletionList(String generatedText) {
        if (StringUtils.isBlank(generatedText)) {
            return null;
        }
        String[] completionList = StringUtils.splitPreserveAllTokens(generatedText, "\n");
        if (completionList.length == 1 && completionList[0].trim().isEmpty()) {
            return null;
        }
        for (int i = 0; i < completionList.length - 1; i++) {
            completionList[i] += "\n";
        }
        return completionList;
    }


    /**
     * 版本过期提醒
     */
    public void VersionExpiredRemind() {
        ApplicationManager.getApplication().invokeLater(() -> {
            Messages.showMessageDialog(BiCoderBundle.get("plugin.version.expired.message"), "", Messages.getInformationIcon());
        });
    }

    /**
     * 判断版本是否过期
     *
     * @return 是否过期
     */
    public boolean isVersionExpired() {
        String version = InfoUtils.getVersion();
        if (StringUtils.isNotBlank(version)) {
            List<String> allowVersion = UserSetting.getInstance().getAllowVersion();
            if (CollectionUtils.isNotEmpty(allowVersion)) {
                if (allowVersion.contains(version)) {
                    return false;
                } else {
                    allowVersion.sort(this::compareVersions);

                    // 获取最大版本
                    String maxAllowedVersion = allowVersion.get(allowVersion.size() - 1);

                    // 比较给定的版本和最大版本
                    return compareVersions(version, maxAllowedVersion) < 0;
                }
            }

        }
        return false;
    }

    /**
     * 比较版本号
     * 例:2.0.2>2.0.1 , 2.0.1.1>2.0.1
     *
     * @param version1 版本1
     * @param version2 版本2
     * @return 比较结果
     */
    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (part1 < part2) {
                return -1;
            } else if (part1 > part2) {
                return 1;
            }
        }
        return 0;
    }
}
