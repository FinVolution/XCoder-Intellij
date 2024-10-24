/*
 * Copyright 2023 John Phillips
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */

package com.ppdai.bicoder.renderer;

import com.intellij.ide.ui.AntialiasingType;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.FocusModeModel;
import com.intellij.openapi.editor.impl.FontInfo;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.openapi.util.Segment;
import com.intellij.ui.paint.EffectPainter;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.UIUtil;
import org.intellij.lang.annotations.JdkConstants;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * 代码提示渲染器
 */
public class CodeGenHintRenderer implements EditorCustomElementRenderer {
    private String text;

    public CodeGenHintRenderer(String text) {
        this.text = text;
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        return calcWidthInPixels(inlay.getEditor(), text);
    }

    protected TextAttributes getTextAttributes(Editor editor) {
        TextAttributesKey key = DefaultLanguageHighlighterColors.LINE_COMMENT;
        return editor.getColorsScheme().getAttributes(key);
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle r, @NotNull TextAttributes textAttributes) {
        Editor editor = inlay.getEditor();
        if (!(editor instanceof EditorImpl)) {
            return;
        }

        EditorImpl editorImpl = (EditorImpl) editor;
        Segment focusModeRange = editorImpl.getFocusModeRange();
        TextAttributes attributes = focusModeRange != null && (inlay.getOffset() <= focusModeRange.getStartOffset() || focusModeRange.getEndOffset() <= inlay.getOffset())
                ? editorImpl.getUserData(FocusModeModel.FOCUS_MODE_ATTRIBUTES)
                : getTextAttributes(editorImpl);

        paintHint(g, editorImpl, r, text, attributes, textAttributes);
    }

    private static int calcWidthInPixels(Editor editor, String text) {
        FontMetrics fontMetrics = getFontMetrics(editor);
        return calcHintTextWidth(text, fontMetrics);
    }

    public static void paintHint(
            Graphics g,
            EditorImpl editor,
            Rectangle r,
            String text,
            TextAttributes attributes,
            TextAttributes textAttributes
    ) {
        int ascent = editor.getAscent();
        int descent = editor.getDescent();
        Graphics2D g2d = (Graphics2D) g;

        if (text != null && attributes != null) {
            int gap = 0;
            Color backgroundColor = attributes.getBackgroundColor();
            if (backgroundColor != null) {
                float alpha = isInsufficientContrast(attributes, textAttributes) ? 1.0f : BACKGROUND_ALPHA;
                GraphicsConfig graphicsConfig = GraphicsUtil.setupAAPainting(g);
                GraphicsUtil.paintWithAlpha(g, alpha);
                g.setColor(backgroundColor);
                g.fillRoundRect(r.x + 2, r.y + gap, r.width - 4, r.height - gap * 2, 8, 8);
                graphicsConfig.restore();
            }
            Color foregroundColor = attributes.getForegroundColor();
            if (foregroundColor != null) {
                Object savedHint = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
                Shape savedClip = g.getClip();

                g.setColor(foregroundColor);
                g.setFont(getFont(editor));
                g2d.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        AntialiasingType.getKeyForCurrentScope(false)
                );
                g.clipRect(r.x + 3, r.y + 2, r.width - 6, r.height - 4);

                int startX = r.x + 2;
                int startY = r.y + ascent;
                g.drawString(text, startX, startY);
                g.setClip(savedClip);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, savedHint);
            }
        }
        Color effectColor = textAttributes.getEffectColor();
        EffectType effectType = textAttributes.getEffectType();
        if (effectColor != null) {
            g.setColor(effectColor);
            int xStart = r.x;
            int xEnd = r.x + r.width;
            int y = r.y + ascent;
            Font font = editor.getColorsScheme().getFont(EditorFontType.PLAIN);
            switch (effectType) {
                case LINE_UNDERSCORE:
                    EffectPainter.LINE_UNDERSCORE.paint(
                            g2d,
                            xStart,
                            y,
                            xEnd - xStart,
                            descent,
                            font
                    );
                    break;
                case BOLD_LINE_UNDERSCORE:
                    EffectPainter.BOLD_LINE_UNDERSCORE.paint(
                            g2d,
                            xStart,
                            y,
                            xEnd - xStart,
                            descent,
                            font
                    );
                    break;
                case STRIKEOUT:
                    EffectPainter.STRIKE_THROUGH.paint(
                            g2d,
                            xStart,
                            y,
                            xEnd - xStart,
                            editor.getCharHeight(),
                            font
                    );
                    break;
                case WAVE_UNDERSCORE:
                    EffectPainter.WAVE_UNDERSCORE.paint(
                            g2d,
                            xStart,
                            y,
                            xEnd - xStart,
                            descent,
                            font
                    );
                    break;
                case BOLD_DOTTED_LINE:
                    EffectPainter.BOLD_DOTTED_UNDERSCORE.paint(
                            g2d,
                            xStart,
                            y,
                            xEnd - xStart,
                            descent,
                            font
                    );
                    break;
                default:
                    break;
            }
        }
    }

    private static final float BACKGROUND_ALPHA = 0.55f;

    private static boolean isInsufficientContrast(TextAttributes attributes, TextAttributes surroundingAttributes) {
        Color backgroundUnderHint = surroundingAttributes.getBackgroundColor();
        if (backgroundUnderHint != null && attributes.getForegroundColor() != null) {
            Color backgroundBlended = srcOverBlend(attributes.getBackgroundColor(), backgroundUnderHint, BACKGROUND_ALPHA);

            double backgroundBlendedGrayed = toGray(backgroundBlended);
            double textGrayed = toGray(attributes.getForegroundColor());
            double delta = Math.abs(backgroundBlendedGrayed - textGrayed);
            return delta < 10;
        }
        return false;
    }

    private static double toGray(Color color) {
        return (0.30 * color.getRed()) + (0.59 * color.getGreen()) + (0.11 * color.getBlue());
    }

    private static Color srcOverBlend(Color foreground, Color background, float foregroundAlpha) {
        int r = (int) (foreground.getRed() * foregroundAlpha + background.getRed() * (1.0f - foregroundAlpha));
        int g = (int) (foreground.getGreen() * foregroundAlpha + background.getGreen() * (1.0f - foregroundAlpha));
        int b = (int) (foreground.getBlue() * foregroundAlpha + background.getBlue() * (1.0f - foregroundAlpha));
        return new Color(r, g, b);
    }

    public static FontMetrics getFontMetrics(Editor editor) {
        return FontInfo.getFontMetrics(getFont(editor), FontInfo.getFontRenderContext(editor.getContentComponent()));
    }

    private static Font getFont(Editor editor) {
        EditorColorsScheme colorsScheme = editor.getColorsScheme();
        TextAttributes attributes = editor.getColorsScheme().getAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT);

        int fontStyle = attributes != null ? attributes.getFontType() : Font.PLAIN;
        return UIUtil.getFontWithFallback(colorsScheme.getFont(forJavaStyle(fontStyle)));
    }

    protected static int calcHintTextWidth(String text, FontMetrics fontMetrics) {
        return text != null ? fontMetrics.stringWidth(text) + 14 : 0;
    }

    public static @NotNull
    EditorFontType forJavaStyle(@JdkConstants.FontStyle int style) {
        switch (style) {
            case Font.BOLD:
                return EditorFontType.BOLD;
            case Font.ITALIC:
                return EditorFontType.ITALIC;
            case Font.BOLD | Font.ITALIC:
                return EditorFontType.BOLD_ITALIC;
            default:
                return EditorFontType.PLAIN;
        }
    }

}