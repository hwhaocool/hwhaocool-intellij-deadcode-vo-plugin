package com.github.hwhaocool.codeInspection.fromsdk.unusedSymbol;

import com.github.hwhaocool.codeInspection.deadcode.Constants;
import com.intellij.codeInspection.unusedSymbol.UnusedSymbolLocalInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author YellowTail
 * @since 2020-12-08
 */
public class UnusedSymbolLocalInspectionImpl extends UnusedSymbolLocalInspection  {

    @NonNls
    public static final String SHORT_NAME = Constants.SHORT_NAME;

    @NonNls
    public static final String UNUSED_PARAMETERS_SHORT_NAME = Constants.ALTERNATIVE_ID;

    @NonNls
    public static final String UNUSED_ID = Constants.SHORT_NAME;

    @Override
    public String getGroupDisplayName() {

        return "Declaration redundancy";
    }

    @Override
    @NotNull
    @NonNls
    public String getShortName() {
        return Constants.SHORT_NAME;
    }

    @Override
    @NotNull
    @NonNls
    public String getID() {
        return Constants.SHORT_NAME;
    }

    @Override
    public String getAlternativeID() {
        return Constants.ALTERNATIVE_ID;
    }

    // 构造方法
    public UnusedSymbolLocalInspectionImpl() {

        // 改变 一些选项的默认选择状态， 让窗口出来的时候，只有 class 一项是被选中的

        super.FIELD = false;
        super.LOCAL_VARIABLE = false;
        super.METHOD = false;
        super.INNER_CLASS = false;
        super.PARAMETER = false;

    }
}
