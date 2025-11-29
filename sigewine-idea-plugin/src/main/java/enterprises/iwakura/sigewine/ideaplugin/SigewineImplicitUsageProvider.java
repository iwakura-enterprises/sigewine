package enterprises.iwakura.sigewine.ideaplugin;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiParameter;

import org.jetbrains.annotations.NotNull;

public class SigewineImplicitUsageProvider implements ImplicitUsageProvider {

    private static final String BEAN_ANNOTATION = "enterprises.iwakura.sigewine.core.annotations.Bean";

    @Override
    public boolean isImplicitUsage(@NotNull PsiElement element) {
        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            return method.hasAnnotation(BEAN_ANNOTATION);
        }
        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            return psiClass.hasAnnotation(BEAN_ANNOTATION);
        }
        if (element instanceof PsiParameter) {
            PsiParameter parameter = (PsiParameter) element;
            if (parameter.getDeclarationScope() instanceof PsiMethod) {
                PsiMethod method = (PsiMethod) parameter.getDeclarationScope();
                return method.isConstructor();
            }
        }
        return false;
    }

    @Override
    public boolean isImplicitRead(@NotNull PsiElement element) {
        if (element instanceof PsiField) {
            PsiField field = (PsiField) element;
            return field.hasAnnotation(BEAN_ANNOTATION);
        }
        return false;
    }

    @Override
    public boolean isImplicitWrite(@NotNull PsiElement element) {
        if (element instanceof PsiField) {
            PsiField field = (PsiField) element;
            return field.hasAnnotation(BEAN_ANNOTATION);
        }
        return false;
    }
}