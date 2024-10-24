package com.ppdai.bicoder.chat;

import com.intellij.ui.JBColor;
import com.ppdai.bicoder.utils.ThemeUtils;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.OrderedListItem;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ResponseNodeRenderer implements NodeRenderer {

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        return Set.of(
                new NodeRenderingHandler<>(Paragraph.class, this::renderParagraph),
                new NodeRenderingHandler<>(Code.class, this::renderCode)
        );
    }

    private void renderCode(Code node, NodeRendererContext context, HtmlWriter html) {
        html.attr("style", "color: " + ThemeUtils.getRGB(new JBColor(0x00627A, 0xCC7832)) + "; font-family:JetBrains Mono; font-size:10px;");
        context.delegateRender();
    }

    private void renderParagraph(Paragraph node, NodeRendererContext context, HtmlWriter html) {
        if (node.getParent() instanceof BulletListItem || node.getParent() instanceof OrderedListItem) {
            html.attr("style", "margin: 0; padding:0; font-family:Microsoft YaHei; font-size:10px; line-height: 3;");
        } else {
            html.attr("style", "margin-top: 4px; margin-bottom: 4px; font-family:Microsoft YaHei; font-size:10px; line-height: 3;");
        }
        context.delegateRender();
    }

    public static class Factory implements NodeRendererFactory {

        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new ResponseNodeRenderer();
        }
    }
}