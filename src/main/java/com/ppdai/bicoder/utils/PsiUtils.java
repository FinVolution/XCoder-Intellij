package com.ppdai.bicoder.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PsiUtils {

    private static final Set<String> INVALID_METHOD_NAMES = new HashSet<>(Arrays.asList("equals", "hashCode", "toString"));

    public static boolean instanceOf(Object obj, String... possibleClassNames) {
        if (obj == null || possibleClassNames == null) {
            return false;
        }
        String objClassName = obj.getClass().getName();
        for (String className : possibleClassNames) {
            try {
                if (className.equals(objClassName)) {
                    return true;
                }
                Class<?> clazz = Class.forName(className);
                if (clazz.isInstance(obj)) {
                    return true;
                }
            } catch (Exception e) {
                BiCoderLoggerUtils.getInstance(PsiUtils.class).debug("fail to instanceOf Class:" + className);
            }
        }
        return false;
    }

    public static boolean isInvalidJavaMethod(@NotNull PsiElement element) {
        PsiMethod method = (PsiMethod) element;
        if (INVALID_METHOD_NAMES.contains(method.getName())) {
            return true;
        }
        if (method.isConstructor()) {
            return true;
        }
        if (method.hasModifierProperty("abstract")) {
            return true;
        }
        if (!PsiUtils.instanceOf(method.getParent(), "com.intellij.psi.PsiClass")) {
            return false;
        }
        PsiClass clazz = (PsiClass) method.getParent();
        PsiField[] fields = clazz.getAllFields();
        if (fields.length == 0) {
            return false;
        }
        String methodName = method.getName().toLowerCase(Locale.ROOT);
        for (PsiField field : fields) {
            String fieldName = field.getName().toLowerCase(Locale.ROOT);
            String setMethodName = "set" + fieldName;
            String getMethodName = "get" + fieldName;
            String isMethodName = "is" + fieldName;
            if (setMethodName.equals(methodName) || getMethodName
                    .equals(methodName) || isMethodName
                    .equals(methodName) || methodName
                    .equals(fieldName)) {
                return true;
            }
        }
        return false;
    }


}
