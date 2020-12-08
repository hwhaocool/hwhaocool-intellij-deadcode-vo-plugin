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
}
