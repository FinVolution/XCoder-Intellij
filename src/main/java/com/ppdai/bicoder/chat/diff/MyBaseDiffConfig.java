package com.ppdai.bicoder.chat.diff;

import com.intellij.openapi.util.Key;

import javax.swing.*;
import java.util.function.Consumer;

public class MyBaseDiffConfig {
    public static final Key<Boolean> NEED_ACCEPT_AND_REJECT = Key.create("diffNeedAcceptAndReject");
    public static final Key<Consumer<JComponent>> SET_RENDER_COMPONENT = Key.create("diffSetRenderComponent");
}
