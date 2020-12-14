package com.github.hwhaocool.codeInspection.deadcode;

import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.reference.RefClass;
import com.intellij.codeInspection.reference.RefClassImpl;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.codeInspection.reference.RefEntity;
import com.intellij.codeInspection.reference.RefFieldImpl;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.impl.source.PsiFieldImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * 通过属性 递归设置 Reachable
 * @author YellowTail
 * @since 2020-12-14
 */
public class RecursionField implements RecursionReachable {

    public void setReachable(final GlobalInspectionContext globalContext, final RefClass refClass, final Set<RefClass> processedRefclass) {

        directField(globalContext, refClass, processedRefclass);

        genericField(globalContext, refClass, processedRefclass);
    }

    /**
     * 直接寻找 refClass 的 字段， 字段为 T field, T就是当前project的类
     * @param globalContext
     * @param refClass
     * @param processedRefclass
     * @author YellowTail
     * @since 2020-12-14
     */
    private void directField(final GlobalInspectionContext globalContext, final RefClass refClass, final Set<RefClass> processedRefclass) {

        // 得到字段的 class 类型
        for (RefClass outTypeReference : refClass.getOutTypeReferences()) {

            process(globalContext, refClass, processedRefclass, outTypeReference);
        }

    }

    /**
     * 寻找 refClass 的泛型字段， 字段为 R &lt;T&gt; field, R就是集合之类的可以包含泛型的对象，如List Map等， T就是当前project的类
     * @param globalContext
     * @param refClass
     * @param processedRefclass
     * @author YellowTail
     * @since 2020-12-14
     */
    private void genericField(final GlobalInspectionContext globalContext, final RefClass refClass, final Set<RefClass> processedRefclass) {

        List<RefEntity> children = refClass.getChildren();

        for (RefEntity refEntity : children) {

            if (! (refEntity instanceof RefFieldImpl)) {
                //不是字段，跳过
                continue;
            }

            // 1. 得到 psi
            PsiElement psiElement = RefFieldImpl.class.cast(refEntity).getPsiElement();

            PsiType psiFieldType = PsiFieldImpl.class.cast(psiElement).getType();

            if (!(psiFieldType instanceof PsiClassReferenceType)) {
                //跳过
                continue;
            }

            // 2. 通过 psi 得到 参数列表，列表里就有 T
            // https://stackoverflow.com/a/36443859
            @NotNull PsiType[] parameters = PsiClassReferenceType.class.cast(psiFieldType).getParameters();

            for (PsiType parameter : parameters) {

                try {
                    PsiClassReferenceType cast = PsiClassReferenceType.class.cast(parameter);
                    PsiClass resolve = cast.resolve();

                    RefElement reference = globalContext.getRefManager().getReference(resolve);

                    if (!(reference instanceof RefClass)) {
                        //跳过
                        continue;
                    }

                    RefClass genericRefClass = RefClassImpl.class.cast(reference);

                    process(globalContext, refClass, processedRefclass, genericRefClass);


                } catch (Exception e) {
                    System.out.println(e);
                }

            }
        }
    }

    private void process(final GlobalInspectionContext globalContext, final RefClass refClass, final Set<RefClass> processedRefclass, final RefClass outTypeReference) {
        if (refClass.equals(outTypeReference)) {
            //如果是自己的话，添加到已分析里面，且跳过当前，继续下一次循环
            processedRefclass.add(outTypeReference);
            return;
        }

        if (processedRefclass.contains(outTypeReference)) {
            //已经处理了，直接下一个
            return;
        }
        processedRefclass.add(outTypeReference);

        //设置为 Reachable
        addEntryPoint(globalContext, outTypeReference);

        // 递归处理
        setReachable(globalContext, outTypeReference, processedRefclass);
    }
}
