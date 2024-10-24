package com.ppdai.bicoder.chat.tests;

import com.intellij.openapi.util.Key;

import javax.swing.*;
import java.util.function.Consumer;

public class MyTestsSchemaConfig {
    public static final Key<Boolean> NEED_ACCEPT_AND_REJECT = Key.create("testsNeedAcceptAndReject");
    public static final Key<Consumer<JComponent>> SET_RENDER_COMPONENT = Key.create("testsSetRenderComponent");
}
