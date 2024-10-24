package com.ppdai.bicoder.utils;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testIntegration.GotoTestOrCodeHandler;
import com.intellij.testIntegration.TestFinderHelper;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.chat.model.ChatContextFile;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserProjectSetting;
import com.ppdai.bicoder.model.ContextSimilaritySnippets;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 上下文工具类
 *
 */
public class ContextUtils {

    /**
     * 根据当前文件获取单测文件
     *
     * @param codeLanguage 代码语言
     * @param virtualFile  当前文件
     */
    @Nullable
    @RequiresBackgroundThread
    public static ChatContextFile getTestFile(@NotNull Editor editor, @NotNull String codeLanguage, @NotNull VirtualFile virtualFile, @NotNull Project project) {
        //优先使用idea内置的语法树方法获取本文件的单测文件
        PsiFile psiFile = ReadAction.compute(() -> PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument()));
        if (psiFile != null) {
            PsiElement selectedElement = ReadAction.compute(() -> GotoTestOrCodeHandler.getSelectedElement(editor, psiFile));
            Boolean isTest = ReadAction.compute(() -> TestFinderHelper.isTest(selectedElement));
            if (!isTest) {
                Collection<PsiElement> tests =
                        ReadAction.compute(() -> TestFinderHelper.findTestsForClass(selectedElement));
                if (!tests.isEmpty()) {
                    List<PsiElement> candidates = new ArrayList<>(tests);
                    PsiElement test = candidates.get(0);
                    VirtualFile testFile = test.getContainingFile().getVirtualFile();
                    if (testFile != null) {
                        return new ChatContextFile(ChatContext.TYPE_FILE_LOCAL_TEST, testFile);
                    }
                }
            }
        }
        //没找到单测文件，那就根据文件名搜索单测文件
        switch (codeLanguage) {
            case "JAVA":
                com.intellij.openapi.module.Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
                return getJavaTestFile(virtualFile, project, module);
            case "PYTHON":
                return getPythonTestFile(virtualFile, project);
            case "GO":
                return getGolandTestFile(virtualFile, project);
            default:
                // 其他语言暂时不支持
                return null;
        }
    }

    /**
     * 获取java单测文件
     * 1. 优先文件名拼接test前后缀来搜寻已存在单测文件
     * 2. 如果搜索不到, 那就在同一个包下搜寻一个单测文件
     * 3. 如果搜索不到, 那就在一般的默认单测目录里搜寻一个单测文件
     *
     * @param virtualFile 当前文件
     */
    private static @Nullable ChatContextFile getJavaTestFile(@NotNull VirtualFile virtualFile, @NotNull Project project, @Nullable com.intellij.openapi.module.Module module) {
        // 搜索java单测文件
        String currentFileName = virtualFile.getNameWithoutExtension();
        String extension = virtualFile.getExtension();

        // 1. 优先文件名拼接test前后缀来搜寻已存在单测文件
        String testFileName = currentFileName + "Test." + extension;
        PsiFile[] testFiles = getVirtualFilesByName(testFileName, project, module);
        if (testFiles != null && testFiles.length > 0) {
            return new ChatContextFile(ChatContext.TYPE_FILE_LOCAL_TEST, testFiles[0].getVirtualFile());
        }

        testFileName = "Test" + currentFileName + "." + extension;
        testFiles = getVirtualFilesByName(testFileName, project, module);
        if (testFiles != null && testFiles.length > 0) {
            return new ChatContextFile(ChatContext.TYPE_FILE_LOCAL_TEST, testFiles[0].getVirtualFile());
        }

        // 2. 如果搜索不到, 那就在同一个包下搜寻一个单测文件
        String fullPath = virtualFile.getPath();
        // 去掉文件名, 并将目录改到test目录下
        String testPath = fullPath.substring(0, fullPath.lastIndexOf("/"));
        testPath = testPath.replace("src/main", "src/test");
        VirtualFile root = LocalFileSystem.getInstance().findFileByPath(testPath);
        VirtualFile firstFileWithTest = findFirstFileWithTest(root);
        if (firstFileWithTest != null) {
            return new ChatContextFile(ChatContext.TYPE_FILE_OTHER_TEST, firstFileWithTest);
        }

        // 3. 如果搜索不到, 那就在一般的默认单测目录(如果有配置则去配置目录里)里搜寻一个单测文件
        String testRootPath = fullPath.substring(0, fullPath.indexOf("/src/main/java/"));
        String testFileRootPath = UserProjectSetting.getInstance(project).getTestFileRootPath();
        if (StringUtils.isBlank(testFileRootPath)) {
            testFileRootPath = "/src/test/java";
        }
        testRootPath = testRootPath + testFileRootPath;
        VirtualFile testRoot = LocalFileSystem.getInstance().findFileByPath(testRootPath);
        VirtualFile firstFileWithTestRoot = findFirstFileWithTest(testRoot);
        if (firstFileWithTestRoot != null) {
            return new ChatContextFile(ChatContext.TYPE_FILE_OTHER_TEST, firstFileWithTestRoot);
        }
        return null;
    }


    /**
     * 获取python单测文件
     * 1. 优先文件名拼接test前后缀来搜寻已存在单测文件
     * 2. 如果搜索不到, 那就在一般的默认单测目录里搜寻一个单测文件
     *
     * @param virtualFile 当前文件
     */
    private static ChatContextFile getPythonTestFile(VirtualFile virtualFile, @NotNull Project project) {
        // 搜索python单测文件
        String currentFileName = virtualFile.getNameWithoutExtension();
        String extension = virtualFile.getExtension();

        // 1. 优先文件名拼接test前后缀来搜寻已存在单测文件
        String testFileName = "test_" + currentFileName + "." + extension;

        PsiFile[] testFiles = getVirtualFilesByName(testFileName, project);
        if (testFiles != null && testFiles.length > 0) {
            return new ChatContextFile(ChatContext.TYPE_FILE_LOCAL_TEST, testFiles[0].getVirtualFile());
        }

        testFileName = currentFileName + "_test." + extension;
        testFiles = getVirtualFilesByName(testFileName, project);
        if (testFiles != null && testFiles.length > 0) {
            return new ChatContextFile(ChatContext.TYPE_FILE_LOCAL_TEST, testFiles[0].getVirtualFile());
        }

        // 2. 如果搜索不到, 那就在一般的默认单测目录里(如果有配置则去配置目录里)搜寻一个单测文件
        String testFileRootPath = UserProjectSetting.getInstance(project).getTestFileRootPath();
        if (StringUtils.isBlank(testFileRootPath)) {
            testFileRootPath = "/tests";
        }
        String testRootPath = project.getBasePath() + testFileRootPath;
        VirtualFile testRoot = LocalFileSystem.getInstance().findFileByPath(testRootPath);
        VirtualFile firstFileWithTestRoot = findFirstFileWithTest(testRoot);
        if (firstFileWithTestRoot != null) {
            return new ChatContextFile(ChatContext.TYPE_FILE_OTHER_TEST, firstFileWithTestRoot);
        }
        return null;
    }


    /**
     * 获取goland单测文件
     * 1. 优先文件名拼接test前后缀来搜寻已存在单测文件
     * 2. 如果搜索不到, 那就在同一个文件夹下搜寻一个单测文件
     * 3. 如果搜索不到, 那就全局搜一下
     *
     * @param virtualFile 当前文件
     */
    private static ChatContextFile getGolandTestFile(VirtualFile virtualFile, @NotNull Project project) {
        // 搜索go单测文件
        String currentFileName = virtualFile.getNameWithoutExtension();
        String extension = virtualFile.getExtension();

        // 1. 优先文件名拼接test前后缀来搜寻同目录已存在单测文件
        String testFileName = currentFileName + "_test." + extension;
        VirtualFile localTestFile = findFileByFileNameInCurrentDirectory(virtualFile.getParent(), testFileName);
        if (localTestFile != null) {
            return new ChatContextFile(ChatContext.TYPE_FILE_LOCAL_TEST, localTestFile);
        }
        testFileName = "test_" + currentFileName + "." + extension;
        localTestFile = findFileByFileNameInCurrentDirectory(virtualFile.getParent(), testFileName);
        if (localTestFile != null) {
            return new ChatContextFile(ChatContext.TYPE_FILE_LOCAL_TEST, localTestFile);
        }

        //同模块搜索存在问题
//        PsiFile[] testFiles = getVirtualFilesByName(testFileName, project);
//        if (testFiles != null && testFiles.length > 0) {
//            return new ChatContextFile(ChatContext.TYPE_FILE_LOCAL_TEST, testFiles[0].getVirtualFile());
//        }
//
//        testFileName = "test_" + currentFileName + "." + extension;
//        testFiles = getVirtualFilesByName(testFileName, project);
//        if (testFiles != null && testFiles.length > 0) {
//            return new ChatContextFile(ChatContext.TYPE_FILE_LOCAL_TEST, testFiles[0].getVirtualFile());
//        }

        // 2. 如果搜索不到, 那就在同一个文件夹下搜寻一个单测文件
        VirtualFile parentPath = virtualFile.getParent();
        VirtualFile firstFileWithTest = findFirstFileWithTest(parentPath);
        if (firstFileWithTest != null) {
            return new ChatContextFile(ChatContext.TYPE_FILE_OTHER_TEST, firstFileWithTest);
        }

        // 3. 如果搜索不到, 那就全局搜一下(如果有配置则去配置目录里)
        String rootPath = project.getBasePath();
        if (rootPath == null) {
            return null;
        }
        String testFileRootPath = UserProjectSetting.getInstance(project).getTestFileRootPath();
        if (StringUtils.isNotBlank(testFileRootPath)) {
            rootPath = rootPath + testFileRootPath;
        }
        VirtualFile testRoot = LocalFileSystem.getInstance().findFileByPath(rootPath);
        VirtualFile firstFileWithTestRoot = findFirstFileWithTest(testRoot);
        if (firstFileWithTestRoot != null) {
            return new ChatContextFile(ChatContext.TYPE_FILE_OTHER_TEST, firstFileWithTestRoot);
        }
        return null;
    }

    @NotNull
    private static PsiFile[] getVirtualFilesByName(String testFileName, @NotNull Project project, @Nullable com.intellij.openapi.module.Module module) {
        if (module != null) {
            return ReadAction.compute(() -> FilenameIndex.getFilesByName(project, testFileName, GlobalSearchScope.moduleScope(module)));
        }
        return ReadAction.compute(() -> FilenameIndex.getFilesByName(project, testFileName, GlobalSearchScope.allScope(project)));
    }

    @NotNull
    private static PsiFile[] getVirtualFilesByName(String testFileName, @NotNull Project project) {
        return getVirtualFilesByName(testFileName, project, null);
    }

    /**
     * 从当前文件夹开始向下查找,找到第一个包含test的文件夹
     *
     * @param root 当前文件夹
     * @return 第一个包含test的文件夹
     */
    private static @Nullable VirtualFile findFirstFileWithTest(@Nullable VirtualFile root) {
        if (root == null) {
            return null;
        }
        Ref<VirtualFile> result = Ref.create();
        VfsUtilCore.visitChildrenRecursively(root, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (!file.isDirectory()) {
                    String fileNameLowerCase = file.getName().toLowerCase();
                    if (fileNameLowerCase.contains("test_") || fileNameLowerCase.contains("_test") || fileNameLowerCase.contains("test")) {
                        result.set(file);
                        return false;
                    }
                }
                return true;
            }
        });
        return result.get();
    }

    /**
     * 在当前文件夹开始向下查找,找到文件名一致的文件
     *
     * @param root 当前文件夹
     * @return 第一个包含test的文件夹
     */
    private static @Nullable VirtualFile findFileByFileNameInCurrentDirectory(@Nullable VirtualFile root, @NotNull String fileName) {
        if (root == null) {
            return null;
        }
        Ref<VirtualFile> result = Ref.create();
        VfsUtilCore.visitChildrenRecursively(root, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (!file.isDirectory()) {
                    String visitFileName = file.getName();
                    if (visitFileName.equals(fileName)) {
                        result.set(file);
                        return false;
                    }
                }
                return true;
            }
        });
        return result.get();
    }
}
