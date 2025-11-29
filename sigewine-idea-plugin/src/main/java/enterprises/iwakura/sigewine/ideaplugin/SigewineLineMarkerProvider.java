package enterprises.iwakura.sigewine.ideaplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.icons.AllIcons.Gutter;
import com.intellij.icons.AllIcons.Nodes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullFactory;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Query;

public class SigewineLineMarkerProvider extends RelatedItemLineMarkerProvider {

    private static final String BEAN_ANNOTATION = "enterprises.iwakura.sigewine.core.annotations.Bean";
    private static final String ACCESSOR_CLASS = "enterprises.iwakura.sigewine.core.BeanAccessor";

    private static final Set<String> SERVICE_ANNOTATIONS = Set.of(
        BEAN_ANNOTATION,
        "enterprises.iwakura.sigewine.core.annotations.ClassWrapped"
    );

    @Override
    protected void collectNavigationMarkers(
        @NotNull PsiElement element,
        @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result
    ) {

        if (!(element instanceof PsiIdentifier)) {
            return;
        }

        PsiElement parent = element.getParent();

        if (parent instanceof PsiMethod) {
            handleProviderMethod(result, (PsiMethod) parent, element);
        } else if (parent instanceof PsiClass) {
            handleProviderClass(result, (PsiClass) parent, element);
        } else if (parent instanceof PsiField) {
            handleFieldInjection(result, (PsiField) parent, element);
        } else if (parent instanceof PsiParameter) {
            handleParameterInjection(result, (PsiParameter) parent, element);
        }
    }

    private void handleProviderMethod(
        Collection<? super RelatedItemLineMarkerInfo<?>> result,
        PsiMethod method,
        PsiElement nameIdentifier
    ) {
        if (method.hasAnnotation(BEAN_ANNOTATION)) {
            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(Gutter.ImplementedMethod)
                .setTooltipText("Navigate to injection points")
                .setPopupTitle("Sigewine Bean Usages")
                .setTargets(NotNullLazyValue.createValue(new NotNullFactory<Collection<? extends PsiElement>>() {
                    @Override
                    public @NotNull Collection<? extends PsiElement> create() {
                        return findInjectionPoints(method);
                    }
                }));
            result.add(builder.createLineMarkerInfo(nameIdentifier));
        }
    }

    private void handleProviderClass(
        Collection<? super RelatedItemLineMarkerInfo<?>> result,
        PsiClass psiClass,
        PsiElement nameIdentifier
    ) {
        if (psiClass.hasAnnotation(BEAN_ANNOTATION)) {
            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(Gutter.ImplementedMethod)
                .setTooltipText("Navigate to injection points")
                .setPopupTitle("Sigewine Bean Usages")
                .setTargets(NotNullLazyValue.createValue(new NotNullFactory<Collection<? extends PsiElement>>() {
                    @Override
                    public @NotNull Collection<? extends PsiElement> create() {
                        return findInjectionPoints(psiClass);
                    }
                }));
            result.add(builder.createLineMarkerInfo(nameIdentifier));
        }
    }

    private void handleFieldInjection(
        Collection<? super RelatedItemLineMarkerInfo<?>> result,
        PsiField field,
        PsiElement nameIdentifier
    ) {
        boolean isExplicitBean = field.hasAnnotation(BEAN_ANNOTATION);
        boolean isLombokStyle =
            field.hasModifierProperty(PsiModifier.FINAL) && isContainingClassBean(field.getContainingClass());

        if (isExplicitBean || isLombokStyle) {
            addNavigateToDefinitionMarker(result, field.getType(), field.getProject(), nameIdentifier);
        }
    }

    private void handleParameterInjection(
        Collection<? super RelatedItemLineMarkerInfo<?>> result,
        PsiParameter parameter,
        PsiElement nameIdentifier
    ) {
        if (!(parameter.getDeclarationScope() instanceof PsiMethod)) {
            return;
        }
        PsiMethod method = (PsiMethod) parameter.getDeclarationScope();

        if (method.isConstructor() || method.hasAnnotation(BEAN_ANNOTATION)) {
            addNavigateToDefinitionMarker(result, parameter.getType(), parameter.getProject(), nameIdentifier);
        }
    }

    private void addNavigateToDefinitionMarker(
        Collection<? super RelatedItemLineMarkerInfo<?>> result,
        PsiType type, Project project, PsiElement anchor
    ) {

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
            .create(AllIcons.Gutter.ImplementedMethod)
            .setTooltipText("Navigate to Sigewine bean definition")
            .setPopupTitle("Bean Definitions")
            .setTargets(NotNullLazyValue.createValue(new NotNullFactory<Collection<? extends PsiElement>>() {
                @Override
                public @NotNull Collection<? extends PsiElement> create() {
                    return findBeanDefinitions(type, project);
                }
            }));
        result.add(builder.createLineMarkerInfo(anchor));
    }

    private Collection<PsiElement> findBeanDefinitions(PsiType type, Project project) {
        Set<PsiElement> results = new HashSet<>();

        PsiType targetType = unwrapType(type, project);
        if (targetType == null) {
            return results;
        }

        PsiClass targetClass = PsiTypesUtil.getPsiClass(targetType);
        if (targetClass == null) {
            return results;
        }

        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        PsiClass beanAnnotation = facade.findClass(BEAN_ANNOTATION, scope);

        if (beanAnnotation == null) {
            return results;
        }

        Collection<PsiClass> beanClasses = AnnotatedElementsSearch.searchPsiClasses(beanAnnotation, scope).findAll();
        for (PsiClass bean : beanClasses) {
            if (bean.isInheritor(targetClass, true) || isSameClass(bean, targetClass)) {
                results.add(bean);
            }
        }

        Collection<PsiMethod> beanMethods = AnnotatedElementsSearch.searchPsiMethods(beanAnnotation, scope).findAll();
        for (PsiMethod method : beanMethods) {
            PsiType returnType = method.getReturnType();
            if (returnType != null) {
                if (targetType.isAssignableFrom(returnType)) {
                    results.add(method);
                }
            }
        }

        return results;
    }

    private Collection<PsiElement> findInjectionPoints(PsiMethod method) {
        PsiType returnType = method.getReturnType();
        if (returnType == null) {
            return new ArrayList<>();
        }

        PsiClass returnTypeClass = PsiUtil.resolveClassInType(returnType);
        if (returnTypeClass == null) {
            return new ArrayList<>();
        }

        return findInjectionPoints(returnTypeClass);
    }

    private Collection<PsiElement> findInjectionPoints(PsiClass beanClass) {
        Set<PsiElement> injectionPoints = new HashSet<>();
        Project project = beanClass.getProject();
        GlobalSearchScope projectScope = GlobalSearchScope.projectScope(project);

        PsiType preciseBeanType = JavaPsiFacade.getElementFactory(project).createType(beanClass);

        Set<PsiClass> typesToSearch = new HashSet<>();
        typesToSearch.add(beanClass);
        typesToSearch.addAll(Arrays.asList(beanClass.getSupers()));

        for (PsiClass typeToSearch : typesToSearch) {
            if ("java.lang.Object".equals(typeToSearch.getQualifiedName())) {
                continue;
            }

            Query<PsiReference> search = ReferencesSearch.search(typeToSearch, projectScope);

            for (PsiReference reference : search) {
                PsiElement element = reference.getElement();

                PsiField field = PsiTreeUtil.getParentOfType(element, PsiField.class);
                if (field != null) {
                    PsiType fieldType = field.getType();

                    PsiType unwrappedFieldType = unwrapType(fieldType, project);

                    if (unwrappedFieldType.isAssignableFrom(preciseBeanType)) {
                        injectionPoints.add(field);
                    }
                    continue;
                }

                PsiParameter parameter = PsiTreeUtil.getParentOfType(element, PsiParameter.class);
                if (parameter != null && parameter.getDeclarationScope() instanceof PsiMethod) {
                    PsiMethod method = (PsiMethod) parameter.getDeclarationScope();
                    if (method.isConstructor() || method.hasAnnotation(BEAN_ANNOTATION)) {
                        PsiType paramType = parameter.getType();
                        PsiType unwrappedParamType = unwrapType(paramType, project);

                        if (unwrappedParamType.isAssignableFrom(preciseBeanType)) {
                            injectionPoints.add(parameter);
                        }
                    }
                }
            }
        }

        return injectionPoints;
    }

    private PsiType unwrapType(PsiType type, Project project) {
        if (!(type instanceof PsiClassType)) {
            return type;
        }

        PsiClassType classType = (PsiClassType) type;
        PsiClass psiClass = classType.resolve();
        if (psiClass == null) {
            return type;
        }

        String qName = psiClass.getQualifiedName();

        if ("java.util.List".equals(qName) || "java.util.Collection".equals(qName)) {
            PsiType[] parameters = classType.getParameters();
            if (parameters.length > 0) {
                return parameters[0];
            }
        }

        if (ACCESSOR_CLASS.equals(qName) || "BeanAccessor".equals(psiClass.getName())) {
            PsiType[] parameters = classType.getParameters();
            if (parameters.length > 0) {
                return parameters[0];
            }
        }

        return type;
    }

    private boolean isContainingClassBean(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        for (String annotation : SERVICE_ANNOTATIONS) {
            if (psiClass.hasAnnotation(annotation)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameClass(PsiClass c1, PsiClass c2) {
        return c1 != null && c2 != null &&
            c1.getQualifiedName() != null &&
            c1.getQualifiedName().equals(c2.getQualifiedName());
    }
}