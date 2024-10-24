package com.ppdai.bicoder.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * 用户配置,应用级生效
 *
 */
@State(name = "com.ppdai.bicoder.config.UserSetting", storages = @Storage("BiCoderUserSettings.xml"))
public class UserSetting implements PersistentStateComponent<UserSetting> {

    /**
     * 是否启用插件
     */
    private boolean enablePlugin = true;

    /**
     * 是否启用缓存
     */
    private boolean enableCache = false;

    /**
     * 服务器地址
     */
    private String requestHost = PluginStaticConfig.REQUEST_HOST;

    /**
     * 是否启用方法快捷按钮
     */
    private boolean enableMethodHint = true;

    /**
     * 允许触发补全文件名单,在名单内的文件会触发补全,但还需要配合下部黑名单disableFileTypeList
     */
    private List<String> fileWhitelist = PluginStaticConfig.DEFAULT_FILE_WHITELIST;

    /**
     * 文件黑名单,在黑名单内的文件不会触发补全
     */
    private Set<String> disableFileTypeList = new HashSet<>();

    /**
     * 已经选择的单测框架
     * key为语言,value为框架名集合
     */
    private Map<String, List<String>> allUintTestFramework = PluginStaticConfig.DEFAULT_UNIT_TEST_FRAMEWORK;

    /**
     * 合法后缀正则
     */
    private String validSuffixReg = PluginStaticConfig.VALID_SUFFIX_REG;

    /**
     * 至少需要的前缀长度
     */
    private int validPrefixLength = PluginStaticConfig.VALID_PREFIX_LENGTH;


    /**
     * 连续输入最长拦截间隔
     */
    private int maxConsecutiveInputIntervalTime = PluginStaticConfig.MAX_CONSECUTIVE_INPUT_INTERVAL_TIME;

    /**
     * 代码比对相似度界限
     */
    private float similarityPercentage = PluginStaticConfig.SIMILARITY_PERCENTAGE;

    /**
     * 允许版本,为空即为全允许
     */
    private List<String> allowVersion = new ArrayList<>();


    /**
     * 采集的光标前代码最大偏移量
     */
    private int maxPrefixOffset = 8000;

    /**
     * 采集的光标后代码最大偏移量
     */
    private int maxSuffixOffset = 200;


    /**
     * 采集的光标前代码最大偏移量百分比
     */
    private float maxPrefixOffsetPercent = 0.85f;

    /**
     * 采集的光标后代码最大偏移量百分比
     */
    private float maxSuffixOffsetPercent = 0.15f;

    /**
     * 用户名
     */
    private String username = "";


    /**
     * 补全结果缓存数量
     */
    private int completionCacheSize = 100;

    /**
     * 模型
     */
    private String modelName = "codellama";

    /**
     * 一个或多个标记序列，这些标记序列在生成的文本中不应出现。当模型生成文本时，它会一直生成，直到遇到stop序列中的标记为止，此时模型将停止生成并返回截至该标记的生成文本
     */
    private List<String> stop = List.of();

    /**
     * 模型温度值,用于控制生成代码的随机性和创造性,较高的温度值（接近1）会让输出更随机，而较低的温度值（接近0）则使输出更确定，更有可能选择最可能的输出。
     */
    private Float temperature = 0.5f;

    /**
     * stream 参数是一个可选参数，用于控制 API 返回值的格式，它只在 Completion 类的实例上可用。当 stream 参数设置为 True 时，API 将以流式响应的形式返回多个 Completion 实例，每个实例都是一个包含生成的文本和相关元数据的 JSON 对象。这个参数通常用于处理大量的生成文本或在后台运行的实时系统，而不是在单个 API 请求中返回一个完整的 Completion 实例
     */
    private Boolean stream = false;

    /**
     * 另一种控制输出随机性的参数,top_p参数的值介于0和1之间（包括0和1），越接近0，生成的文本将越保守，可能性较高的token将更有可能被选中；越接近1，生成的文本将越大胆，更多的token会被选中，这会导致生成的文本更加多样化
     */
    private Float topP = 1f;


    /**
     * 返回建议条数
     */
    private Integer resultSize = 1;

    /**
     * 最大允许使用的token数
     */
    private Integer maxCompletionTokens = 256;


    /**
     * 是否使用缩进符做trim操作
     */
    private Boolean trimByIndentation = true;

    /**
     * 是否使用默认颜色
     */
    private boolean useDefaultColor = true;

    /**
     * 是否开启自动请求代码补全
     */
    private boolean autoCompletionEnabled = true;


    /**
     * 已打开的编辑器缓存文件最小行数
     */
    public Integer openedEditorCacheFileMinLine = PluginStaticConfig.OPENED_EDITOR_CACHE_FILE_MIN_LINE;

    /**
     * 已打开的编辑器缓存文件最大行数
     */
    public Integer openedEditorCacheFileMaxLine = PluginStaticConfig.OPENED_EDITOR_CACHE_FILE_MAX_LINE;

    /**
     * 最大已打开的编辑器缓存使用个数
     */
    public Integer maxCurrentOpenedEditorCacheUseSize = PluginStaticConfig.MAX_CURRENT_OPENED_EDITOR_CACHE_USE_SIZE;


    private int colorState = PluginStaticConfig.DEFAULT_COLOR;

    public List<String> getFileWhitelist() {
        return fileWhitelist;
    }

    public void setFileWhitelist(List<String> fileWhitelist) {
        this.fileWhitelist = fileWhitelist;
    }

    public boolean getEnablePlugin() {
        return enablePlugin;
    }

    public void setEnablePlugin(boolean enablePlugin) {
        this.enablePlugin = enablePlugin;
    }

    public boolean getEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public boolean getEnableMethodHint() {
        return enableMethodHint;
    }

    public String getRequestHost() {
        return requestHost;
    }

    public void setRequestHost(String requestHost) {
        this.requestHost = requestHost;
    }

    public void setEnableMethodHint(boolean enableMethodHint) {
        this.enableMethodHint = enableMethodHint;
    }

    public Set<String> getDisableFileTypeList() {
        return disableFileTypeList;
    }

    public void setDisableFileTypeList(Set<String> disableFileTypeList) {
        this.disableFileTypeList = disableFileTypeList;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public int getMaxPrefixOffset() {
        return maxPrefixOffset;
    }

    public void setMaxPrefixOffset(int maxPrefixOffset) {
        this.maxPrefixOffset = maxPrefixOffset;
    }

    public int getMaxSuffixOffset() {
        return maxSuffixOffset;
    }

    public void setMaxSuffixOffset(int maxSuffixOffset) {
        this.maxSuffixOffset = maxSuffixOffset;
    }

    public int getCompletionCacheSize() {
        return completionCacheSize;
    }

    public void setCompletionCacheSize(int completionCacheSize) {
        this.completionCacheSize = completionCacheSize;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<String> getStop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public Float getTopP() {
        return topP;
    }

    public void setTopP(Float topP) {
        this.topP = topP;
    }

    public Integer getResultSize() {
        return resultSize;
    }

    public void setResultSize(Integer resultSize) {
        this.resultSize = resultSize;
    }

    public Integer getMaxCompletionTokens() {
        return maxCompletionTokens;
    }

    public void setMaxCompletionTokens(Integer maxCompletionTokens) {
        this.maxCompletionTokens = maxCompletionTokens;
    }

    public Boolean getTrimByIndentation() {
        return trimByIndentation;
    }

    public void setTrimByIndentation(Boolean trimByIndentation) {
        this.trimByIndentation = trimByIndentation;
    }

    public boolean isUseDefaultColor() {
        return useDefaultColor;
    }

    public void setUseDefaultColor(boolean useDefaultColor) {
        this.useDefaultColor = useDefaultColor;
    }

    public boolean isAutoCompletionEnabled() {
        return autoCompletionEnabled;
    }

    public void setAutoCompletionEnabled(boolean autoCompletionEnabled) {
        this.autoCompletionEnabled = autoCompletionEnabled;
    }

    public String getValidSuffixReg() {
        return validSuffixReg;
    }

    public void setValidSuffixReg(String validSuffixReg) {
        this.validSuffixReg = validSuffixReg;
    }

    public int getValidPrefixLength() {
        return validPrefixLength;
    }

    public void setValidPrefixLength(int validPrefixLength) {
        this.validPrefixLength = validPrefixLength;
    }

    public int getMaxConsecutiveInputIntervalTime() {
        return maxConsecutiveInputIntervalTime;
    }

    public void setMaxConsecutiveInputIntervalTime(int maxConsecutiveInputIntervalTime) {
        this.maxConsecutiveInputIntervalTime = maxConsecutiveInputIntervalTime;
    }

    public float getSimilarityPercentage() {
        return similarityPercentage;
    }

    public void setSimilarityPercentage(float similarityPercentage) {
        this.similarityPercentage = similarityPercentage;
    }

    public List<String> getAllowVersion() {
        return allowVersion;
    }

    public void setAllowVersion(List<String> allowVersion) {
        this.allowVersion = allowVersion;
    }

    public float getMaxPrefixOffsetPercent() {
        return maxPrefixOffsetPercent;
    }

    public void setMaxPrefixOffsetPercent(float maxPrefixOffsetPercent) {
        this.maxPrefixOffsetPercent = maxPrefixOffsetPercent;
    }

    public float getMaxSuffixOffsetPercent() {
        return maxSuffixOffsetPercent;
    }

    public void setMaxSuffixOffsetPercent(float maxSuffixOffsetPercent) {
        this.maxSuffixOffsetPercent = maxSuffixOffsetPercent;
    }

    public Map<String, List<String>> getAllUintTestFramework() {
        return allUintTestFramework;
    }

    public void setAllUintTestFramework(Map<String, List<String>> allUintTestFramework) {
        this.allUintTestFramework = allUintTestFramework;
    }

    public Integer getOpenedEditorCacheFileMinLine() {
        return openedEditorCacheFileMinLine;
    }

    public void setOpenedEditorCacheFileMinLine(Integer openedEditorCacheFileMinLine) {
        this.openedEditorCacheFileMinLine = openedEditorCacheFileMinLine;
    }

    public Integer getOpenedEditorCacheFileMaxLine() {
        return openedEditorCacheFileMaxLine;
    }

    public void setOpenedEditorCacheFileMaxLine(Integer openedEditorCacheFileMaxLine) {
        this.openedEditorCacheFileMaxLine = openedEditorCacheFileMaxLine;
    }

    public Integer getMaxCurrentOpenedEditorCacheUseSize() {
        return maxCurrentOpenedEditorCacheUseSize;
    }

    public void setMaxCurrentOpenedEditorCacheUseSize(Integer maxCurrentOpenedEditorCacheUseSize) {
        this.maxCurrentOpenedEditorCacheUseSize = maxCurrentOpenedEditorCacheUseSize;
    }

    /**
     * 获取代码提示颜色
     *
     * @return 颜色rgb值
     */
    public int getCodeHintColor() {
        if (useDefaultColor) {
            return PluginStaticConfig.DEFAULT_COLOR;
        } else {
            return colorState;
        }
    }

    /**
     * 设置代码提示颜色
     *
     * @param value 颜色rgb值
     */
    public void setCodeHintColor(int value) {
        colorState = value;
    }


    @Override
    public UserSetting getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull UserSetting state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static UserSetting getInstance() {
        return ApplicationManager.getApplication().getService(UserSetting.class);
    }


}
