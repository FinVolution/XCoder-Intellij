package com.ppdai.bicoder.model;

import com.ppdai.bicoder.config.UserSetting;

import java.util.*;

/**
 * 补全请求对象
 */
public class BiCoderCompletionRequest {

    /**
     * 唯一id
     */
    private String generateUUID;

    /**
     * 用户域账号
     */
    private String createUser;

    /**
     * 项目git地址
     */
    private String gitRepo;

    /**
     * 项目git分支
     */
    private String gitBranch;

    /**
     * idea信息
     */
    private String ideInfo;

    /**
     * 当前文件路径(相对于项目根目录)
     */
    private String codePath;

    /**
     * 当前文件语言类型
     */
    private String codeLanguage;

    /**
     * 光标前代码
     */
    private String codeBeforeCursor;

    /**
     * 光标后代码
     */
    private String codeAfterCursor;

    /**
     * 光标位置
     */
    private Integer cursorStartIdx;


    /**
     * 生成模式
     */
    private String generateType;

    /**
     * 模型
     */
    private String modelName;

    /**
     * 一个或多个标记序列，这些标记序列在生成的文本中不应出现。当模型生成文本时，它会一直生成，直到遇到stop序列中的标记为止，此时模型将停止生成并返回截至该标记的生成文本
     */
    private List<String> stopWords;

    /**
     * 模型温度值,用于控制生成代码的随机性和创造性,较高的温度值（接近1）会让输出更随机，而较低的温度值（接近0）则使输出更确定，更有可能选择最可能的输出。
     */
    private Float temperature;

    /**
     * stream 参数是一个可选参数，用于控制 API 返回值的格式，它只在 Completion 类的实例上可用。当 stream 参数设置为 True 时，API 将以流式响应的形式返回多个 Completion 实例，每个实例都是一个包含生成的文本和相关元数据的 JSON 对象。这个参数通常用于处理大量的生成文本或在后台运行的实时系统，而不是在单个 API 请求中返回一个完整的 Completion 实例
     */
    private Boolean stream;

    /**
     * 另一种控制输出随机性的参数,top_p参数的值介于0和1之间（包括0和1），越接近0，生成的文本将越保守，可能性较高的token将更有可能被选中；越接近1，生成的文本将越大胆，更多的token会被选中，这会导致生成的文本更加多样化
     */
    private Float topP;


    /**
     * 返回结果数量
     */
    private Integer resultSize;


    /**
     * 最大允许使用的token数
     */
    private Integer maxTokens;

    /**
     * 下一个缩进位置
     */
    private Integer nextIndent;

    /**
     * 是否使用缩进符做trim操作
     */
    private Boolean trimByIndentation;

    /**
     * 当前文件代码总行数
     */
    private int codeTotalLines;

    /**
     * 补全上下文
     */
    private List<CompletionContext> contexts;
    /**
     * 拓展参数
     */
    private Map<String, Object> extra;

    private BiCoderCompletionRequest(Builder builder) {
        this.generateUUID = builder.generateUUID;
        this.createUser = builder.username;
        this.gitRepo = builder.gitUrl;
        this.gitBranch = builder.gitBranch;
        this.ideInfo = builder.ideaInfo;
        this.codePath = builder.path;
        this.codeLanguage = builder.language;
        this.codeBeforeCursor = builder.beforeCursor;
        this.codeAfterCursor = builder.afterCursor;
        this.cursorStartIdx = builder.cursorOffset;
        this.generateType = builder.generateScheme;
        this.modelName = builder.modelName;
        this.stopWords = builder.stop;
        this.temperature = builder.temperature;
        this.stream = builder.stream;
        this.topP = builder.topP;
        this.resultSize = builder.resultSize;
        this.maxTokens = builder.maxCompletionTokens;
        this.nextIndent = builder.nextIndent;
        this.trimByIndentation = builder.trimByIndentation;
        this.codeTotalLines = builder.codeTotalLines;
        this.contexts = builder.contexts;
        this.extra = builder.extra;
    }

    /**
     * 静态内部类 Builder
     */

    public static class Builder {
        private final String generateUUID;
        private final String username;
        private String gitUrl;
        private String gitBranch;
        private String ideaInfo;
        private String path;
        private String language;
        private String beforeCursor;
        private String afterCursor;
        private Integer cursorOffset;
        private String generateScheme;
        private final String modelName;
        private final List<String> stop;
        private final Float temperature;
        private final Boolean stream;
        private final Float topP;
        private final Integer resultSize;
        private final Integer maxCompletionTokens;
        private Integer nextIndent;
        private final Boolean trimByIndentation;
        private int codeTotalLines;
        private List<CompletionContext> contexts;
        private Map<String, Object> extra;

        /**
         * 必填参数构造,仅暴露此构造
         */
        public Builder() {
            UserSetting userSetting = UserSetting.getInstance();
            this.generateUUID = UUID.randomUUID().toString();
            this.username = userSetting.getUsername();
            this.modelName = userSetting.getModelName();
            this.stop = userSetting.getStop();
            this.temperature = userSetting.getTemperature();
            this.stream = userSetting.getStream();
            this.topP = userSetting.getTopP();
            this.resultSize = userSetting.getResultSize();
            this.maxCompletionTokens = userSetting.getMaxCompletionTokens();
            this.trimByIndentation = userSetting.getTrimByIndentation();
        }

        public Builder gitUrl(String gitUrl) {
            this.gitUrl = gitUrl;
            return this;
        }

        public Builder gitBranch(String gitBranch) {
            this.gitBranch = gitBranch;
            return this;
        }

        public Builder ideaInfo(String ideaInfo) {
            this.ideaInfo = ideaInfo;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder beforeCursor(String beforeCursor) {
            this.beforeCursor = beforeCursor;
            return this;
        }

        public Builder cursorOffset(Integer cursorOffset) {
            this.cursorOffset = cursorOffset;
            return this;
        }

        public Builder afterCursor(String afterCursor) {
            this.afterCursor = afterCursor;
            return this;
        }

        public Builder generateScheme(String generateScheme) {
            this.generateScheme = generateScheme;
            return this;
        }

        public Builder nextIndent(Integer nextIndent) {
            this.nextIndent = nextIndent;
            return this;
        }

        public Builder codeTotalLines(int codeTotalLines) {
            this.codeTotalLines = codeTotalLines;
            return this;
        }

        public Builder completionContexts(List<CompletionContext> contexts) {
            this.contexts = contexts;
            return this;
        }

        public Builder extra(Map<String, Object> extra) {
            this.extra = extra;
            return this;
        }

        public Builder addExtra(String name, Object value) {
            if (this.extra == null) {
                this.extra = new HashMap<>();
            }
            this.extra.put(name, value);
            return this;
        }


        //构建一个实体
        public BiCoderCompletionRequest build() {
            return new BiCoderCompletionRequest(this);
        }
    }

    public String getGenerateUUID() {
        return generateUUID;
    }

    public void setGenerateUUID(String generateUUID) {
        this.generateUUID = generateUUID;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getGitRepo() {
        return gitRepo;
    }

    public void setGitRepo(String gitRepo) {
        this.gitRepo = gitRepo;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    public String getIdeInfo() {
        return ideInfo;
    }

    public void setIdeInfo(String ideInfo) {
        this.ideInfo = ideInfo;
    }

    public String getCodePath() {
        return codePath;
    }

    public void setCodePath(String codePath) {
        this.codePath = codePath;
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public void setCodeLanguage(String codeLanguage) {
        this.codeLanguage = codeLanguage;
    }

    public String getCodeBeforeCursor() {
        return codeBeforeCursor;
    }

    public void setCodeBeforeCursor(String codeBeforeCursor) {
        this.codeBeforeCursor = codeBeforeCursor;
    }

    public String getCodeAfterCursor() {
        return codeAfterCursor;
    }

    public void setCodeAfterCursor(String codeAfterCursor) {
        this.codeAfterCursor = codeAfterCursor;
    }

    public Integer getCursorStartIdx() {
        return cursorStartIdx;
    }

    public void setCursorStartIdx(Integer cursorStartIdx) {
        this.cursorStartIdx = cursorStartIdx;
    }

    public String getGenerateType() {
        return generateType;
    }

    public void setGenerateType(String generateType) {
        this.generateType = generateType;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getResultSize() {
        return resultSize;
    }

    public void setResultSize(Integer resultSize) {
        this.resultSize = resultSize;
    }

    public List<String> getStopWords() {
        return stopWords;
    }

    public void setStopWords(List<String> stopWords) {
        this.stopWords = stopWords;
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

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Integer getNextIndent() {
        return nextIndent;
    }

    public void setNextIndent(Integer nextIndent) {
        this.nextIndent = nextIndent;
    }

    public Boolean getTrimByIndentation() {
        return trimByIndentation;
    }

    public void setTrimByIndentation(Boolean trimByIndentation) {
        this.trimByIndentation = trimByIndentation;
    }

    public int getCodeTotalLines() {
        return codeTotalLines;
    }

    public void setCodeTotalLines(int codeTotalLines) {
        this.codeTotalLines = codeTotalLines;
    }

    public List<CompletionContext> getContexts() {
        return contexts;
    }

    public void setContexts(List<CompletionContext> contexts) {
        this.contexts = contexts;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BiCoderCompletionRequest{");
        sb.append("generateUUID='").append(generateUUID).append('\'');
        sb.append(", createUser='").append(createUser).append('\'');
        sb.append(", gitRepo='").append(gitRepo).append('\'');
        sb.append(", gitBranch='").append(gitBranch).append('\'');
        sb.append(", ideInfo='").append(ideInfo).append('\'');
        sb.append(", codePath='").append(codePath).append('\'');
        sb.append(", codeLanguage='").append(codeLanguage).append('\'');
        sb.append(", codeBeforeCursor='").append(codeBeforeCursor).append('\'');
        sb.append(", codeAfterCursor='").append(codeAfterCursor).append('\'');
        sb.append(", cursorStartIdx=").append(cursorStartIdx);
        sb.append(", generateType='").append(generateType).append('\'');
        sb.append(", modelName='").append(modelName).append('\'');
        sb.append(", stopWords=").append(stopWords);
        sb.append(", temperature=").append(temperature);
        sb.append(", stream=").append(stream);
        sb.append(", topP=").append(topP);
        sb.append(", resultSize=").append(resultSize);
        sb.append(", maxTokens=").append(maxTokens);
        sb.append(", nextIndent=").append(nextIndent);
        sb.append(", trimByIndentation=").append(trimByIndentation);
        sb.append(", codeTotalLines=").append(codeTotalLines);
        sb.append(", contexts=").append(contexts);
        sb.append(", extra=").append(extra);
        sb.append('}');
        return sb.toString();
    }
}
