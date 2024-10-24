package com.ppdai.bicoder.config;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件静态配置,属于插件中不会变化的配置
 *
 */
public class PluginStaticConfig {


    /**
     * 默认颜色为浅灰
     */
    public static final int DEFAULT_COLOR = JBColor.LIGHT_GRAY.getRGB();

    public static final String PLUGIN_NAME = "XCoder";

    public static final String REQUEST_HOST = "http://127.0.0.1:8080";

    public static final String COMPLETION_REQUEST_URL = "/v1/code/generate";

    public static final String CONFIG_URL = "/v1/code/get_configs";

    public static final String COMMON_CHAT_URL = "/v1/chat/sse";


    public static final String GENERATE_TESTS_URL = "/v1/unit_test/sse";
    public static final String EDIT_CODE_URL = "/v1/code_edit/sse";
    public static final String EXPLAIN_CODE_URL = "/v1/code_explain/sse";
    public static final String GENERATE_DOC_URL = "/v1/code_comment/sse";
    public static final String OPTIMIZE_CODE_URL = "/v1/code_optimize/sse";

    public static final int REQUEST_CONNECT_TIMEOUT = 10000;

    public static final int REQUEST_SOCKET_TIMEOUT = 10000;

    public static final int REQUEST_READ_TIMEOUT = 10000;

    public static final int REQUEST_WRITE_TIMEOUT = 10000;

    /**
     * 文件白名单,不在白名单内的文件不会触发补全
     */
    public static final List<String> DEFAULT_FILE_WHITELIST = List.of("JAVA", "Python", "Go", "Groovy", "Kotlin", "JSP", "JSON", "JavaScript", "PHP", "HTML", "PLAIN_TEXT", "Markdown", "Properties", "XML", "YAML", "protobuf");

    /**
     * 上下文白名单,不在白名单内的不会获取上下文
     */
    public static final List<String> CONTEXT_FILE_WHITELIST = List.of("JAVA", "Python", "Go");

    /**
     * 单测新展示模式白名单,不在白名单内的不会以单测模式展示单测，而是依然只显示在chat中
     */
    public static final List<String> TESTS_SCHEMA_WHITELIST = List.of("JAVA", "PYTHON", "GO");


    /**
     * 合法的生成模式
     */
    public static final Map<String, String> TEST_FILE_ROOT_PATH = new HashMap<>(Map.of("JAVA", "/src/test/java", "PYTHON", "/tests", "GO", "", "OTHER", ""));

    /**
     * CONTEXT_FILE_WHITELIST
     * 不合法的生成模式
     */
    public static final List<String> INVALID_GENERATE_SCHEME = List.of("NN?YN", "NN?YY", "YN?YN", "YN?YY", "NN?NN", "NN?NY");

    public static final Map<String, List<String>> DEFAULT_UNIT_TEST_FRAMEWORK =
            new HashMap<>(Map.of("JAVA", List.of("JUnit"), "Python", List.of("pytest", "unittest", "nose2"), "Go", List.of("testing", "goconvey", "testify", "gostub", "gomock", "gomonkey")));
    ;
    /**
     * 合法后缀正则
     */
    public static final String VALID_SUFFIX_REG = "^\\s*[)}\\]\"'`]*\\s*[:{;,]?\\s*$";

    /**
     * 至少需要的前缀长度
     */
    public static final int VALID_PREFIX_LENGTH = 5;


    /**
     * 连续输入最长拦截间隔
     */
    public static final int MAX_CONSECUTIVE_INPUT_INTERVAL_TIME = 600;

    /**
     * 连续输入最长拦截获取类定义间隔,主要用于避免一些快速输入大量消耗资源
     */
    public static final int MAX_CONSECUTIVE_GET_CLASS_DEFINE_INTERVAL_TIME = 100;

    /**
     * 代码比对相似度界限
     */
    public static final float SIMILARITY_PERCENTAGE = 0.5f;

    /**
     * chat会话最大存储数量
     */
    public static final int MAX_CHAT_CONVERSATION_SIZE = 20;
    /**
     * chat单个会话message最大存储数量
     */
    public static final int MAX_CHAT_MESSAGE_SIZE = 50;

    /**
     * chat上下文文件最大大小
     */
    public static final int MAX_CONTEXT_FILE_SIZE = 10 * 1024 * 1024;


    /**
     * 最大已打开的编辑器缓存个数
     */
    public static final int MAX_CURRENT_OPENED_EDITOR_CACHE_SIZE = 21;

    /**
     * 最大已打开的编辑器缓存使用个数
     */
    public static final int MAX_CURRENT_OPENED_EDITOR_CACHE_USE_SIZE = 5;

    /**
     * 已打开的编辑器缓存文件最大文件大小,单位为字节
     */
    public static final int OPENED_EDITOR_CACHE_FILE_MAX_SIZE = 10 * 1024 * 1024;

    /**
     * 已打开的编辑器缓存文件最小行数
     */
    public static final int OPENED_EDITOR_CACHE_FILE_MIN_LINE = 10;

    /**
     * 已打开的编辑器缓存文件最大行数
     */
    public static final int OPENED_EDITOR_CACHE_FILE_MAX_LINE = 5000;

    /**
     * 已打开的编辑器缓存文件切分最小行数
     */
    public static final int OPENED_EDITOR_CACHE_FILE_SNIPPET_MIN_LINE = 30;

    /**
     * 已打开的编辑器缓存文件切分对比当前文件最小行数
     */
    public static final int OPENED_EDITOR_FILE_SNIPPET_COMPARE_MIN_LINE = 10;

    /**
     * 代码补全最大返回代码片段数
     */
    public static final int OPENED_EDITOR_CONTEXT_MAX_SNIPPET_COUNT = 4;

    /**
     * 末尾符号白名单
     */
    public static final List<String> END_SYMBOL_WHITELIST = List.of(";", "}", "]", ")", ":", ",", "'", "\"");


    /**
     * 前置符号黑名单
     */
    public static final List<String> PRE_SYMBOL_BLACKLIST = List.of(";");

    public static final Icon BI_CODER_ICON = IconLoader.findIcon("/icons/pluginIcon.svg", PluginStaticConfig.class);


    public static final Icon DISABLED_ICON = IconLoader.findIcon("/icons/pluginDisabledIcon.svg", PluginStaticConfig.class);

    public static final Icon LOADING_ICON = new AnimatedIcon.Default();

    public static final Icon TOOL_WINDOW_ICON = IconLoader.findIcon("/icons/toolWindowIcon.svg", PluginStaticConfig.class);

    public static final Icon SEND_ICON = IconLoader.findIcon("/icons/send.svg", PluginStaticConfig.class);

    public static final Icon ACCEPT_ICON = IconLoader.findIcon("/icons/accept.svg", PluginStaticConfig.class);

    public static final Icon WARNING_ICON = IconLoader.findIcon("/icons/waring.svg", PluginStaticConfig.class);

    public static final Icon INSERT_ICON = IconLoader.findIcon("/icons/insert_text.svg", PluginStaticConfig.class);

    public static final Icon COPY_ICON = IconLoader.findIcon("/icons/copy_text.svg", PluginStaticConfig.class);

    public static final Icon NEW_FILE_ICON = IconLoader.findIcon("/icons/new_file_text.svg", PluginStaticConfig.class);

    public static final Icon DIFF_CODE_ICON = IconLoader.findIcon("/icons/diff_text.svg", PluginStaticConfig.class);

    public static final Icon METHOD_HINT_ICON = IconLoader.findIcon("/icons/method_hint.svg", PluginStaticConfig.class);

    public static final Icon FILE_TYPE_ICON = IconLoader.findIcon("/icons/file_type.svg", PluginStaticConfig.class);


}
