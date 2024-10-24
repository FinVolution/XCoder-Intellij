package com.ppdai.bicoder.chat.hints;

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.codeInsight.hints.presentation.SequencePresentation;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.utils.PsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PythonChatMethodHintProvider extends BaseChatMethodHintProvider {

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull ChatMethodHintSettings chatHintSettings, @NotNull InlayHintsSink inlayHintsSink) {
        return new FactoryInlayHintsCollector(editor) {
            @Override
            public boolean collect(@NotNull PsiElement element, @NotNull Editor editor, @NotNull InlayHintsSink sink) {
                UserSetting setting = UserSetting.getInstance();
                if (setting != null && !setting.getEnableMethodHint()) {
                    return true;
                }
                PsiElement prevSibling = element.getPrevSibling();
                if (!(prevSibling instanceof com.intellij.psi.PsiWhiteSpace) || !prevSibling.textContains('\n')) {
                    return true;
                }
                if (!PsiUtils.instanceOf(element,
                        "com.intellij.psi.PsiMethod", "com.jetbrains.python.psi.PyFunction")
                        || PsiUtils.instanceOf(element, "com.intellij.psi.PsiTypeParameter")) {
                    return true;
                }
                PresentationFactory factory = getFactory();
                Document document = editor.getDocument();
                int offset = PythonChatMethodHintProvider.getAnchorOffset(element);
                int line = document.getLineNumber(offset);
                int startOffset = document.getLineStartOffset(line);
                String linePrefix = editor.getDocument().getText(new TextRange(startOffset, offset));
                offset += PythonChatMethodHintProvider.this.findRealOffsetBySpace(editor, linePrefix);
                int column = offset - startOffset;
                SmartList<InlayPresentation> smartList = new SmartList();
                smartList.add(factory.textSpacePlaceholder(column, true));
                smartList.add(factory.smallScaledIcon(PluginStaticConfig.METHOD_HINT_ICON));
                smartList.add(factory.smallScaledIcon(AllIcons.Actions.FindAndShowNextMatchesSmall));
                smartList.add(factory.textSpacePlaceholder(1, true));
                SequencePresentation shiftedPresentation = new SequencePresentation(smartList);
                InlayPresentation finalPresentation = factory.referenceOnHover(shiftedPresentation, (event, translated) -> PythonChatMethodHintProvider.this.onClick(editor, element, event));
                sink.addBlockElement(startOffset, true, true, 300, finalPresentation);
                return true;
            }
        };
    }
}