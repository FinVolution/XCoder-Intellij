package com.ppdai.bicoder.chat.edit;

import com.intellij.openapi.util.Key;

import javax.swing.*;
import java.util.function.Consumer;

public class MyEditConfig {
    public static final Key<Boolean> NEED_ACCEPT_AND_REJECT = Key.create("needAcceptAndReject");
    public static final Key<Consumer<JComponent>> SET_RENDER_COMPONENT = Key.create("setRenderComponent");
}
