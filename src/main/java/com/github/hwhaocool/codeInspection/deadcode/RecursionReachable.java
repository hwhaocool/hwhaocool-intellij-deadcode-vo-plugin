package com.github.hwhaocool.codeInspection.deadcode;

import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.GlobalJavaInspectionContext;
import com.intellij.codeInspection.reference.RefClass;

import java.util.Set;

public interface RecursionReachable {

    /**
     * 使用递归模式设置 Reachable
     * @param globalContext 上下文
     * @param refClass 当前搜索出来的具有 Reachable 的 refClass
     * @param processedRefclass 已经处理过的 refClass 集合
     * @author YellowTail
     * @since 2020-12-14
     */
    void setReachable(GlobalInspectionContext globalContext, RefClass refClass, final Set<RefClass> processedRefclass);

    /**
     * 设置 outTypeReference 为 Reachable
     * @param context
     * @param outTypeReference
     * @author YellowTail
     * @since 2020-12-14
     */
    default void addEntryPoint(final GlobalInspectionContext context, RefClass outTypeReference) {
        context.getExtension(GlobalJavaInspectionContext.CONTEXT).getEntryPointsManager(context.getRefManager()).addEntryPoint(outTypeReference, false);
    }
}
