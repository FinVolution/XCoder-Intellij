package com.ppdai.bicoder.utils;

import com.goide.psi.GoFunctionDeclaration;
import com.goide.psi.GoType;
import com.goide.psi.GoTypeReferenceExpression;
import com.goide.psi.GoTypeSpec;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsRoot;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import com.ppdai.bicoder.cache.ProjectCache;
import com.ppdai.bicoder.model.ClassInfo;
import com.ppdai.bicoder.model.CodeBlockRange;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ppdai.bicoder.config.PluginStaticConfig.CONTEXT_FILE_WHITELIST;

/**
 * 信息获取工具类
 */
public class InfoUtils {

    private static final String Y = "Y";
    private static final String N = "N";
    private static final String QUESTION_MARK = "?";
    private static final Pattern multilineRegex = Pattern.compile("[^ \t\n{}]");

    public static final String NOT_IN_PROJECT = "notInProject";

    public static final String NOT_CLASS = "notClass";

    private static final String JAVA_METHOD_EMPTY_BODY = "{...}";
    private static final String PYTHON_METHOD_EMPTY_BODY = "...";
    private static final String FOUR_SPACE = "    ";


    private static final String LEFT_BRACE = "{";

    private static final String COLON = ":";

    /**
     * 获取idea信息
     *
     * @return idea信息
     */
    public static String getIdeaInfo() {
        return ApplicationInfo.getInstance().getFullApplicationName();
    }

    /**
     * 获取项目git url
     *
     * @param project 项目对象
     * @return git url
     */
    public static String getGitUrl(@NotNull Project project) {
        ProjectCache projectCache = project.getService(ProjectCache.class);
        String gitUrlCache = projectCache.getGitUrl();
        if (gitUrlCache != null) {
            return gitUrlCache;
        }
        ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
        VcsRoot[] vcsRoots = vcsManager.getAllVcsRoots();
        String gitUrl = null;
        for (VcsRoot vcsRoot : vcsRoots) {
            if (vcsRoot.getVcs() != null && "Git".equals(vcsRoot.getVcs().getName())) {
                VirtualFile path = vcsRoot.getPath();
                String localPath = path.getPath();
                // 打开Git仓库
                Repository repository;
                try (Git git = Git.open(new File(localPath))) {
                    repository = git.getRepository();
                    // 获取仓库的远程URL
                    gitUrl = repository.getConfig().getString("remote", "origin", "url");
                    break;
                } catch (IOException e) {
                    BiCoderLoggerUtils.getInstance(InfoUtils.class).warn("open git repository failed", e);
                }
            }
        }
        if (gitUrl == null) {
            BiCoderLoggerUtils.getInstance(InfoUtils.class).warn("can not find git repository");
        }
        projectCache.setGitUrl(gitUrl);
        return gitUrl;
    }

    /**
     * 获取相对路径
     *
     * @param virtualFile 文件对象
     * @param project     项目对象
     * @return 相对路径
     */
    @NotNull
    public static String getRelativePath(VirtualFile virtualFile, Project project) {
        String relativePath = null;
        try {
            VirtualFile contentRootForFile = ProjectUtil.guessProjectDir(project);
            if (contentRootForFile == null) {
                BiCoderLoggerUtils.getInstance(InfoUtils.class).warn("can not find contentRootForFile");
            } else {
                relativePath = VfsUtilCore.getRelativePath(virtualFile, contentRootForFile);
            }
        } catch (Exception e) {
            BiCoderLoggerUtils.getInstance(InfoUtils.class).warn("getRelativePath error", e);
        }
        if (relativePath == null) {
            relativePath = "unknown";
        }
        return relativePath;
    }

    /**
     * 获取相对路径(不包含文件名)
     *
     * @param project
     * @param file
     * @return
     */
    public static String getRelativePathNoContainFileName(Project project, VirtualFile file) {
        String relativePath = getRelativePath(file, project);

        // 去除文件名
        int lastIndex = relativePath.lastIndexOf("/");
        if (lastIndex != -1) {
            relativePath = relativePath.substring(0, lastIndex);
        } else {
            relativePath = "";
        }

        // 添加起始的斜杠
        relativePath = "/" + relativePath;

        return relativePath;
    }

    /**
     * 获取文件语言类型
     *
     * @param virtualFile 文件对象
     * @return 文件语言类型
     */
    public static String getLanguage(VirtualFile virtualFile) {
        return virtualFile.getFileType().getName();
    }


    /**
     * 获取文件范围代码
     *
     * @param virtualFile 文件对象
     * @param startLine   开始行
     * @param endLine     结束行
     * @return 文件范围代码
     */
    public static String getLineRangeCode(@NotNull VirtualFile virtualFile, int startLine, int endLine) {
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document == null) {
            BiCoderLoggerUtils.getInstance(InfoUtils.class).warn("can not find document");
            return null;
        }

        return getRangeCode(document, startLine, endLine);
    }

    /**
     * 获取文件范围代码
     *
     * @param document  文档对象
     * @param startLine 开始行
     * @param endLine   结束行
     * @return 文件范围代码
     */
    public static String getLineRangeCode(@NotNull Document document, int startLine, int endLine) {
        int startOffset = document.getLineStartOffset(startLine);
        int endOffset = document.getLineEndOffset(endLine);
        return getRangeCode(document, startOffset, endOffset);
    }

    /**
     * 获取文件范围代码
     *
     * @param virtualFile 文件对象
     * @param startOffset 开始偏移量
     * @param endOffset   结束偏移量
     * @return
     */
    public static String getRangeCode(@NotNull VirtualFile virtualFile, int startOffset, int endOffset) {
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document == null) {
            BiCoderLoggerUtils.getInstance(InfoUtils.class).warn("can not find document");
            return null;
        }
        return document.getText(TextRange.create(startOffset, endOffset));
    }


    /**
     * 获取文件范围代码
     *
     * @param document    文档对象
     * @param startOffset 开始偏移量
     * @param endOffset   结束偏移量
     * @return
     */
    public static String getRangeCode(@NotNull Document document, int startOffset, int endOffset) {
        return document.getText(TextRange.create(startOffset, endOffset));
    }


    /**
     * 获取生成方案
     * 如果beforeCursor最后一个换行符前有实际意义内容,则字符串第一个字符为Y,否则为N,最后一个换行符后有实际意义内容,则字符串第二个字符为Y,否则为N,如果不存在换行符,则直接判断beforeCursor是否有实际意义内容
     * 如果afterCursor第一个换行符前有实际意义内容,则字符串第三个字符为Y,否则为N,第一个换行符后有实际意义内容,则字符串第四个字符为Y,否则为N,,如果不存在换行符,则直接判断afterCursor是否有实际意义内容
     * 样例:YY?NN,YY?YN,YY?NY,YY?YY
     *
     * @param beforeCursor 光标前的字符串
     * @param afterCursor  光标后的字符串
     * @return 生成方案
     */
    public static String getGenerateScheme(@NotNull String beforeCursor, @NotNull String afterCursor) {
        String firstChar = (beforeCursor.lastIndexOf('\n') >= 0 && beforeCursor.substring(0, beforeCursor.lastIndexOf('\n')).trim().length() > 0) ? Y : N;
        String secondChar = ((beforeCursor.lastIndexOf('\n') >= 0 && beforeCursor.substring(beforeCursor.lastIndexOf('\n')).trim().length() > 0) || (beforeCursor.lastIndexOf('\n') < 0 && beforeCursor.trim().length() > 0)) ? Y : N;
        String thirdChar = ((afterCursor.indexOf('\n') >= 0 && afterCursor.substring(0, afterCursor.indexOf('\n')).trim().length() > 0) || (afterCursor.lastIndexOf('\n') < 0 && afterCursor.trim().length() > 0)) ? Y : N;
        String fourthChar = (afterCursor.indexOf('\n') >= 0 && afterCursor.substring(afterCursor.indexOf('\n')).trim().length() > 0) ? Y : N;
        return firstChar + secondChar + QUESTION_MARK + thirdChar + fourthChar;
    }

    /**
     * 获取git分支名
     *
     * @param project 项目对象
     * @return git分支名
     */
    public static String getGitBranch(@NotNull Project project) {
        ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
        VcsRoot[] vcsRoots = vcsManager.getAllVcsRoots();
        String gitBranch = null;
        for (VcsRoot vcsRoot : vcsRoots) {
            if (vcsRoot.getVcs() != null && "Git".equals(vcsRoot.getVcs().getName())) {
                VirtualFile path = vcsRoot.getPath();
                String localPath = path.getPath();
                // 打开Git仓库
                Repository repository;
                try (Git git = Git.open(new File(localPath))) {
                    repository = git.getRepository();
                    // 获取仓库的远程URL
                    gitBranch = repository.getBranch();
                    break;
                } catch (IOException e) {
                    BiCoderLoggerUtils.getInstance(InfoUtils.class).warn("open git branch failed", e);
                }
            }
        }
        if (gitBranch == null) {
            BiCoderLoggerUtils.getInstance(InfoUtils.class).warn("can not find git branch");
        }
        return gitBranch;
    }

    /**
     * 判断应该生成多行,在光标所在折叠区域内,如果处于最后一个有效行,则生成多行,否则不生成多行
     *
     * @param focusedEditor        当前编辑器
     * @param exceptCursorPosition 光标位置
     * @param virtualFile          当前文件
     * @param generateScheme       生成方案
     * @return 是否生成多行
     */
    public static Boolean judgeMultiline(@NotNull Editor focusedEditor, int exceptCursorPosition, VirtualFile virtualFile, String generateScheme) {
        if ("YN?NY".equals(generateScheme)) {
            //获取当前文件所有折叠区域
            List<FoldRegion> result = getFoldRegions(focusedEditor, exceptCursorPosition);
            if (CollectionUtils.isNotEmpty(result)) {
                //获取当前光标所在最小折叠区域
                Optional<FoldRegion> minArea = result.stream().min(Comparator.comparingInt(FoldRegion::getEndOffset));
                if (minArea.isPresent()) {
                    FoldRegion foldRegion = minArea.get();
                    int endOffset = foldRegion.getEndOffset();
                    String rangeCode = InfoUtils.getRangeCode(virtualFile, exceptCursorPosition, endOffset);
                    if (StringUtils.isBlank(rangeCode)) {
                        return true;
                    }
                    Matcher matcher = multilineRegex.matcher(rangeCode);
                    return !(matcher.find());
                }
            }
            return false;
        } else {
            return null;
        }
    }

    @Nullable
    public static String getVersion() {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId("com.open.xcoder"));
        if (plugin != null) {
            return plugin.getVersion();
        }
        return "unknown";
    }


    /**
     * 获取当前光标所在折叠区域,如果不在方法内,则返回整个类的折叠区域
     *
     * @param language             语言
     * @param project              项目对象
     * @param document             文档对象
     * @param focusedEditor        当前编辑器
     * @param exceptCursorPosition 光标位置
     * @return 折叠区域
     */
    @RequiresBackgroundThread
    public static CodeBlockRange getCursorBlockRange(String language, Project project, @NotNull Document document, @NotNull Editor focusedEditor, int exceptCursorPosition) {
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        CodeBlockRange codeBlockRange = new CodeBlockRange();
        if (psiFile != null) {
            PsiElement elementAt = psiFile.findElementAt(exceptCursorPosition);
            TextRange range = null;
            try {
                if ("JAVA".equals(language)) {
                    PsiMethod method = PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class);
                    if (method == null) {
                        PsiClass psiClass = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
                        if (psiClass != null) {
                            range = psiClass.getTextRange();
                        }
                    } else {
                        range = method.getTextRange();
                    }
                } else if ("Python".equals(language)) {
                    PyFunction pyFunction = PsiTreeUtil.getParentOfType(elementAt, PyFunction.class);
                    if (pyFunction == null) {
                        PyClass pyClass = PsiTreeUtil.getParentOfType(elementAt, PyClass.class);
                        if (pyClass != null) {
                            range = pyClass.getTextRange();
                        }
                    } else {
                        range = pyFunction.getTextRange();
                    }
                } else if ("Go".equals(language)) {
                    GoFunctionDeclaration goFunction = PsiTreeUtil.getParentOfType(elementAt, GoFunctionDeclaration.class);
                    if (goFunction == null) {
                        GoTypeSpec goClass = PsiTreeUtil.getParentOfType(elementAt, GoTypeSpec.class);
                        if (goClass != null) {
                            range = goClass.getTextRange();
                        }
                    } else {
                        range = goFunction.getTextRange();
                    }
                }
            } catch (Throwable e) {
                //避免语法树类缺失导致的异常,do nothing
            }
            if (range != null) {
                codeBlockRange.setStartOffset(range.getStartOffset());
                codeBlockRange.setEndOffset(range.getEndOffset());
                return codeBlockRange;
            }
        } else {
            //获取当前文件所有折叠区域
            List<FoldRegion> result = getFoldRegions(focusedEditor, exceptCursorPosition);
            if (CollectionUtils.isNotEmpty(result)) {
                //获取当前光标所在最大折叠区域
                Optional<FoldRegion> maxArea = result.stream().max(Comparator.comparingInt(FoldRegion::getEndOffset));
                if (maxArea.isPresent()) {
                    FoldRegion foldRegion = maxArea.get();
                    int startOffset = foldRegion.getStartOffset();
                    int lineNumber = document.getLineNumber(startOffset);
                    int startLineStartOffset = document.getLineStartOffset(lineNumber);
                    codeBlockRange.setStartOffset(startLineStartOffset);
                    codeBlockRange.setEndOffset(foldRegion.getEndOffset());
                    return codeBlockRange;
                }
            }
        }
        codeBlockRange.setStartOffset(0);
        codeBlockRange.setEndOffset(focusedEditor.getDocument().getTextLength());
        return codeBlockRange;
    }

    /**
     * 取光标上一定行数的内容
     *
     * @param editor                编辑器
     * @param lineNumber            行数
     * @param currentCursorPosition 光标位置
     * @return 上边行数索引
     */
    public static @Nullable CodeBlockRange getSurroundingLines(@NotNull Editor editor, int lineNumber, int currentCursorPosition) {
        if (lineNumber > 0) {
            Document document = editor.getDocument();
            int currentLine = document.getLineNumber(currentCursorPosition);
            if (currentLine <= 0) {
                return null;
            }
            int startLine = Math.max(0, currentLine - lineNumber);
            int startOffset = document.getLineStartOffset(startLine);
            int endOffset = document.getLineEndOffset(currentLine);
            CodeBlockRange codeBlockRange = new CodeBlockRange();
            codeBlockRange.setStartOffset(startOffset);
            codeBlockRange.setEndOffset(endOffset);
            return codeBlockRange;
        }
        return null;
    }

    @RequiresBackgroundThread
    public static @Nullable CodeBlockRange getJustMethodCursorBlockRange(String language, Project project, @NotNull VirtualFile virtualFile, int exceptCursorPosition) {
        PsiFile psiFile = ApplicationManager.getApplication().runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(project).findFile(virtualFile));
        CodeBlockRange codeBlockRange = new CodeBlockRange();
        if (psiFile != null) {
            PsiElement elementAt = psiFile.findElementAt(exceptCursorPosition);
            TextRange range = null;
            try {
                if ("JAVA".equals(language)) {
                    PsiMethod method = ApplicationManager.getApplication().runReadAction((Computable<PsiMethod>) () -> PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class));
                    if (method != null) {
                        range = method.getTextRange();
                    }
                } else if ("Python".equals(language)) {
                    PyFunction pyFunction = ApplicationManager.getApplication().runReadAction((Computable<PyFunction>) () -> PsiTreeUtil.getParentOfType(elementAt, PyFunction.class));
                    if (pyFunction != null) {
                        range = pyFunction.getTextRange();
                    }
                } else if ("Go".equals(language)) {
                    GoFunctionDeclaration goFunction = ApplicationManager.getApplication().runReadAction((Computable<GoFunctionDeclaration>) () -> PsiTreeUtil.getParentOfType(elementAt, GoFunctionDeclaration.class));
                    if (goFunction != null) {
                        range = goFunction.getTextRange();
                    }
                }
            } catch (Throwable e) {
                //避免语法树类缺失导致的异常,do nothing
            }
            if (range != null) {
                codeBlockRange.setStartOffset(range.getStartOffset());
                codeBlockRange.setEndOffset(range.getEndOffset());
                return codeBlockRange;
            }
        }
        return null;
    }

    /**
     * 获取当前光标所在折叠区域
     *
     * @param focusedEditor        当前编辑器
     * @param exceptCursorPosition 光标位置
     * @return 折叠区域
     */
    @NotNull
    private static List<FoldRegion> getFoldRegions(@NotNull Editor focusedEditor, int exceptCursorPosition) {
        FoldRegion[] allRegions = focusedEditor.getFoldingModel().getAllFoldRegions();
        List<FoldRegion> result = new ArrayList<>();
        //获取当前光标所在折叠区域
        for (FoldRegion region : allRegions) {
            if (region.getStartOffset() <= exceptCursorPosition && region.getEndOffset() >= exceptCursorPosition) {
                result.add(region);
            }
        }
        return result;
    }

    /**
     * 获取当前类指定范围内的所有方法代码,并按类分组
     * 需要异步线程，不然容易阻塞UI线程，导致用户界面卡顿
     *
     * @param virtualFile 当前文件
     * @param startOffset 开始偏移量
     * @param endOffset   结束偏移量
     * @return 方法代码
     */
    @RequiresBackgroundThread
    public static Map<String, List<String>> getMethodCodeInCurrentFileRange(VirtualFile virtualFile, int startOffset, int endOffset, Project project) {
        //索引没有准备好,不做处理
        if (DumbService.isDumb(project)) {
            return null;
        }
        long start = System.currentTimeMillis();
        PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
        TextRange selection = new TextRange(startOffset, endOffset);
        PsiElement[] psiElements = PsiTreeUtil.collectElements(file, element -> selection.contains(element.getTextRange()));
        //map<类名,方法代码>
        Map<String, List<String>> methodCodeMap = new HashMap<>();
        for (PsiElement element : psiElements) {
            if (element instanceof PsiMethodCallExpression) {
                handleJavaMethod(file, (PsiMethodCallExpression) element, methodCodeMap);
            }
        }
        BiCoderLoggerUtils.getInstance(InfoUtils.class).info("getMethodCode cost:" + (System.currentTimeMillis() - start));
        return methodCodeMap;
    }

    /**
     * 处理java方法
     *
     * @param file          当前文件
     * @param element       方法调用元素
     * @param methodCodeMap 方法代码
     */
    private static void handleJavaMethod(PsiFile file, PsiMethodCallExpression element, Map<String, List<String>> methodCodeMap) {
        //获取方法调用的方法
        PsiMethod psiMethod = element.resolveMethod();
        try {
            if (psiMethod == null) {
                return;
            }
            //如果方法所在文件和当前文件相同,则跳过
            PsiFile containingFile = psiMethod.getContainingFile();
            if (containingFile == null || file.equals(containingFile)) {
                return;
            }
            PsiCodeBlock body = psiMethod.getBody();
            if (body != null) {
                String fileName = containingFile.getName();
                String text = psiMethod.getText();
                System.out.println("fileName:" + fileName + ",text:" + text);
                if (methodCodeMap.containsKey(fileName)) {
                    methodCodeMap.get(fileName).add(text);
                } else {
                    List<String> methodCodeList = new ArrayList<>();
                    methodCodeList.add(text);
                    methodCodeMap.put(fileName, methodCodeList);
                }
            }
        } catch (Exception e) {
            BiCoderLoggerUtils.getInstance(InfoUtils.class).warn("getMethodCode error", e);
        }
    }

    /**
     * 获取当前类指定范围内的所有类中的所有方法定义和属性定义,处理成简易的类定义
     * 需要异步线程，不然容易阻塞UI线程，导致用户界面卡顿
     *
     * @param language             语言
     * @param relativePath         相对路径
     * @param virtualFile          当前文件
     * @param document             文档对象
     * @param focusedEditor        当前编辑器
     * @param exceptCursorPosition 光标位置
     * @param project              项目对象
     */
    @RequiresBackgroundThread
    public static void getClassMethodDefineInCurrentFileRange(String language, String relativePath, @NotNull VirtualFile virtualFile, @NotNull Document document, @NotNull Editor focusedEditor, int exceptCursorPosition, @NotNull Project project) {
        //语言不在上下文白名单或索引没有准备好,不做处理
        if (!CONTEXT_FILE_WHITELIST.contains(language) || DumbService.isDumb(project)) {
            return;
        }
        CodeBlockRange cursorBlockRange = InfoUtils.getCursorBlockRange(language, project, document, focusedEditor, exceptCursorPosition);
        int startOffset = cursorBlockRange.getStartOffset();
        int endOffset = cursorBlockRange.getEndOffset();
        BiCoderLoggerUtils.getInstance(InfoUtils.class).info("getClassMethodDefineInCurrentFileRange start," + "relativePath:" + relativePath + ",startOffset:" + startOffset + ",endOffset:" + endOffset);
        long start = System.currentTimeMillis();
        try {
            //当editor的文件不变化时,代表当前文件不在项目中,不做处理
            Map<String, ClassInfo> classDefineMap = project.getService(ProjectCache.class).getClassDefineMap(relativePath);
            if (classDefineMap == null) {
                classDefineMap = new LinkedHashMap<>(8);
            }
            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
            TextRange selection = new TextRange(startOffset, endOffset);
            PsiElement[] psiElements = PsiTreeUtil.collectElements(file, element -> selection.contains(element.getTextRange()));
            // 对psiElements数组进行排序,离光标位置远的排在后面
            if (psiElements.length > 1) {
                Arrays.sort(psiElements, (e1, e2) -> {
                    int distance1 = Math.abs(e1.getTextRange().getStartOffset() - exceptCursorPosition);
                    int distance2 = Math.abs(e2.getTextRange().getStartOffset() - exceptCursorPosition);
                    return Integer.compare(distance1, distance2);
                });
            }
            for (PsiElement element : psiElements) {
                try {
                    if (element instanceof PsiTypeElement) {
                        handleJavaClass((PsiTypeElement) element, classDefineMap, project);
                    }
                } catch (Throwable e) {
                    //避免java语法树类缺失导致的异常,do nothing
                }
                try {
                    if (element instanceof PyReferenceExpression) {
                        handlePythonClass((PyReferenceExpression) element, classDefineMap, project);
                    }
                } catch (Throwable e) {
                    //避免python语法树类缺失导致的异常,do nothing
                }
                try {
                    if (element instanceof GoType) {
                        handleGolandClass((GoType) element, classDefineMap, project);
                    }
                } catch (Throwable e) {
                    //避免go语法树类缺失导致的异常,do nothing
                }
            }
            project.getService(ProjectCache.class).addClassDefineMap(relativePath, classDefineMap);
        } catch (Exception e) {
            project.getService(ProjectCache.class).clearClassDefineMap();
            BiCoderLoggerUtils.getInstance(InfoUtils.class).warn("getClassMethodDefineInCurrentFileRange error", e);
        }
        BiCoderLoggerUtils.getInstance(InfoUtils.class).info("getClassMethodDefineInCurrentFileRange cost:" + (System.currentTimeMillis() - start));
    }

    /**
     * 处理java类
     *
     * @param element              类型元素
     * @param classMethodDefineMap 类方法定义
     * @param project              项目对象
     */
    private static void handleJavaClass(PsiTypeElement element, Map<String, ClassInfo> classMethodDefineMap, @NotNull Project project) {
        //根据全限定名判断类存在则跳过
        String canonicalText = element.getType().getCanonicalText();
        ClassInfo classInfo = classMethodDefineMap.get(canonicalText);
        if (classInfo != null && StringUtils.isNotBlank(classInfo.getClassText())) {
            return;
        }
        PsiJavaCodeReferenceElement innermostComponentReferenceElement = element.getInnermostComponentReferenceElement();
        if (innermostComponentReferenceElement != null) {
            PsiElement resolve = innermostComponentReferenceElement.resolve();
            if (resolve instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) resolve;
                //获取全限定名
                String fileName = psiClass.getQualifiedName();
                if (fileName == null) {
                    return;
                }
                if (!PsiManager.getInstance(project).isInProject(psiClass)) {
                    //需要存储一个标识,代表这个类不在当前项目中，以免下次进入处理
                    classMethodDefineMap.put(fileName, new ClassInfo(null, null, NOT_IN_PROJECT));
                    return;
                }
                VirtualFile virtualFile = psiClass.getContainingFile().getVirtualFile();
                String relativePath = InfoUtils.getRelativePath(virtualFile, project);
                String className = psiClass.getName();
                String classText = psiClass.getText();
                classMethodDefineMap.put(fileName, new ClassInfo(relativePath, className, classText));
                return;
            }
        }
        //需要存储一个标识,代表当前对象不是类或不可获得，避免重复处理耗费性能
        classMethodDefineMap.put(canonicalText, new ClassInfo(null, null, NOT_CLASS));
    }

    /**
     * 简化java类代码
     *
     * @param psiClass java类
     * @return 简化后的类定义
     */
    @NotNull
    private static String simplifyJavaClass(@NotNull PsiClass psiClass) {
        //获得类的主体左花括号信息
        PsiElement lBrace = psiClass.getLBrace();
        StringBuilder classDefinition = new StringBuilder();
        if (lBrace == null) {
            //代表这个类没有主体,不做后续处理
            classDefinition.append(psiClass.getText());
        } else {
            classDefinition.append(psiClass.getText(), 0, lBrace.getStartOffsetInParent()).append(LEFT_BRACE);
            PsiField[] allFields = psiClass.getFields();
            for (PsiField field : allFields) {
                String fieldDefine = field.getText();
                classDefinition.append("\n").append(fieldDefine);
            }

            //获取类的所有方法
            PsiMethod[] methods = psiClass.getMethods();
            for (PsiMethod method : methods) {
                //私有方法忽视
                if (method.hasModifierProperty(PsiModifier.PRIVATE)) {
                    continue;
                }
                String methodDefine = method.getText();
                PsiCodeBlock body = method.getBody();
                if (body != null) {
                    int startOffsetInParent = body.getStartOffsetInParent();
                    if (startOffsetInParent > 0) {
                        methodDefine = methodDefine.substring(0, startOffsetInParent);
                        methodDefine = methodDefine + JAVA_METHOD_EMPTY_BODY;
                    } else if (startOffsetInParent == 0) {
                        //如果等于0,代表这个方法是代理自动生成,比如lombok,需要特殊处理
                        methodDefine = methodDefine.substring(0, methodDefine.indexOf(LEFT_BRACE));
                        methodDefine = methodDefine + JAVA_METHOD_EMPTY_BODY;
                    }
                    //如果小于0,代表这个方法没有主体,不做处理
                }
                classDefinition.append("\n").append(methodDefine);
            }
            classDefinition.append("\n}");
        }
        return classDefinition.toString();
    }

    /**
     * 处理python类
     *
     * @param element              类元素
     * @param classMethodDefineMap 类方法定义
     * @param project              项目对象
     */
    private static void handlePythonClass(PyReferenceExpression element, Map<String, ClassInfo> classMethodDefineMap, @NotNull Project project) {
        //根据名称判断存在则跳过,当前类中应用名称不会重复
        String name = element.getName();
        ClassInfo classInfo = classMethodDefineMap.get(name);
        if (classInfo != null && StringUtils.isNotBlank(classInfo.getClassText())) {
            return;
        }
        PsiElement resolve = element.getReference().resolve();
        if (resolve != null) {
            if (resolve instanceof PyClass) {
                PyClass pyClass = (PyClass) resolve;
                if (!PsiManager.getInstance(project).isInProject(pyClass)) {
                    //需要存储一个标识,代表这个类不在当前项目中，避免重复处理耗费性能
                    classMethodDefineMap.put(name, new ClassInfo(null, null, NOT_IN_PROJECT));
                    return;
                }
                VirtualFile virtualFile = pyClass.getContainingFile().getVirtualFile();
                String relativePath = InfoUtils.getRelativePath(virtualFile, project);
                String className = pyClass.getName();
                String classText = pyClass.getText();
                classMethodDefineMap.put(name, new ClassInfo(relativePath, className, classText));
                return;
            }
        }
        //需要存储一个标识,代表当前对象不是类或不可获得，避免重复处理耗费性能
        classMethodDefineMap.put(name, new ClassInfo(null, null, NOT_CLASS));
    }

    /**
     * 简化python类代码
     * todo 目前处理存在缩进问题,需要后续处理
     *
     * @param pyClass python类
     * @return 简化后的类定义
     */
    @NotNull
    private static String simplifyPythonClass(PyClass pyClass) {
        String pyClassText = pyClass.getText();
        StringBuilder classDefinition = new StringBuilder();
        classDefinition.append(pyClassText, 0, pyClassText.indexOf(COLON)).append(COLON);
        List<PyTargetExpression> classAttributes = pyClass.getClassAttributes();
        for (PyTargetExpression classAttribute : classAttributes) {
            String attributeDefine = classAttribute.getText();
            classDefinition.append("\n").append(attributeDefine);
        }
        PyFunction[] methods = pyClass.getMethods();
        for (PyFunction pyFunction : methods) {
            //私有方法忽视,其他方法去除方法体
            if (!PyFunction.ProtectionLevel.PRIVATE.equals(pyFunction.getProtectionLevel())) {
                String methodDefine = pyFunction.getText();
                int colonIndex = methodDefine.indexOf(COLON);
                //如果有冒号,代表这个方法有主体,需要去除主体
                if (colonIndex > 0) {
                    methodDefine = methodDefine.substring(0, methodDefine.indexOf(COLON)) + COLON;
                    methodDefine = methodDefine + PYTHON_METHOD_EMPTY_BODY;
                }
                classDefinition.append("\n").append(FOUR_SPACE).append(methodDefine);
            }
        }
        return classDefinition.toString();
    }

    /**
     * todo 处理goland类,IC版本暂时不支持
     *
     * @param element              类型元素
     * @param classMethodDefineMap 类方法定义
     * @param project              项目对象
     */
    private static void handleGolandClass(GoType element, Map<String, ClassInfo> classMethodDefineMap, @NotNull Project project) {
        //根据名称判断存在则跳过,当前类中应用名称不会重复
        String name = element.getText();
        ClassInfo classInfo = classMethodDefineMap.get(name);
        if (classInfo != null && StringUtils.isNotBlank(classInfo.getClassText())) {
            return;
        }
        GoTypeReferenceExpression typeReferenceExpression = element.getTypeReferenceExpression();
        if (typeReferenceExpression != null) {
            PsiElement resolve = typeReferenceExpression.resolve();
            if (resolve != null) {
                if (resolve instanceof GoTypeSpec) {
                    GoTypeSpec goTypeSpec = (GoTypeSpec) resolve;
                    if (!PsiManager.getInstance(project).isInProject(goTypeSpec)) {
                        //需要存储一个标识,代表这个类不在当前项目中，避免重复处理耗费性能
                        classMethodDefineMap.put(name, new ClassInfo(null, null, NOT_IN_PROJECT));
                        return;
                    }
                    VirtualFile virtualFile = goTypeSpec.getContainingFile().getVirtualFile();
                    String relativePath = InfoUtils.getRelativePath(virtualFile, project);
                    String className = goTypeSpec.getName();
                    String classText = goTypeSpec.getText();
                    classMethodDefineMap.put(name, new ClassInfo(relativePath, className, classText));
                    return;
                }
            }
        }
        //需要存储一个标识,代表当前对象不是类或不可获得，避免重复处理耗费性能
        classMethodDefineMap.put(name, new ClassInfo(null, null, NOT_CLASS));
    }
}
