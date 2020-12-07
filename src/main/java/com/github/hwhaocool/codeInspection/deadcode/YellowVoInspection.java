package com.github.hwhaocool.codeInspection.deadcode;

import com.github.hwhaocool.codeInspection.fromsdk.UnusedDeclarationInspectionBase;
import com.github.hwhaocool.codeInspection.fromsdk.UnusedDeclarationPresentation;
import com.github.hwhaocool.codeInspection.fromsdk.UnusedParametersInspection;
import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.CommonProblemDescriptor;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptionsProcessor;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.QuickFix;
import com.intellij.codeInspection.SuppressionUtil;
import com.intellij.codeInspection.ex.EntryPointsManagerImpl;
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.Tools;
import com.intellij.codeInspection.reference.EntryPoint;
import com.intellij.codeInspection.reference.RefClass;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.codeInspection.reference.RefEntity;
import com.intellij.codeInspection.reference.RefGraphAnnotator;
import com.intellij.codeInspection.reference.RefManager;
import com.intellij.codeInspection.reference.RefManagerImpl;
import com.intellij.codeInspection.reference.RefMethod;
import com.intellij.codeInspection.reference.RefVisitor;
import com.intellij.codeInspection.ui.InspectionToolPresentation;
import com.intellij.codeInspection.unusedSymbol.UnusedSymbolLocalInspection;
import com.intellij.codeInspection.unusedSymbol.UnusedSymbolLocalInspectionBase;
import com.intellij.java.JavaBundle;
import com.intellij.psi.JavaRecursiveElementWalkingVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLambdaExpression;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiResourceVariable;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.controlFlow.DefUseUtil;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.JBUI;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UClassInitializer;
import org.jetbrains.uast.UDeclaration;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UMethod;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 代码来自 com.intellij.codeInspection.deadCode.UnusedDeclarationInspection， 然后改一改
 * @author YellowTail
 * @since 2020-12-06
 */
public class YellowVoInspection extends UnusedDeclarationInspectionBase {

    private final UnusedParametersInspection myUnusedParameters = new UnusedParametersInspection();

    public YellowVoInspection() { }

    @TestOnly
    public YellowVoInspection(boolean enabledInEditor) {
        super(enabledInEditor);
    }

    @Nullable
    @Override
    public String getAlternativeID() {
        return Constants.ALTERNATIVE_ID;
    }

    @Override
    public String getShortName() {
        return Constants.SHORT_NAME;
    }

    @Override
    public void runInspection(@NotNull AnalysisScope scope,
                              @NotNull InspectionManager manager,
                              @NotNull GlobalInspectionContext globalContext,
                              @NotNull ProblemDescriptionsProcessor problemDescriptionsProcessor) {
        System.out.println("YellowVoInspection runInspection");

        if (myLocalInspectionBase.PARAMETER) {
            globalContext.getRefManager().iterate(new RefVisitor() {
                @Override public void visitElement(@NotNull RefEntity refEntity) {
                    if (!(refEntity instanceof RefMethod) ||
                            !globalContext.shouldCheck(refEntity, YellowVoInspection.this) ||
                            !UnusedDeclarationPresentation.compareVisibilities((RefMethod)refEntity, myLocalInspectionBase.getParameterVisibility())) {
                        return;
                    }
                    CommonProblemDescriptor[] descriptors = myUnusedParameters.checkElement(refEntity, scope, manager, globalContext, problemDescriptionsProcessor);
                    if (descriptors != null) {
                        problemDescriptionsProcessor.addProblemElement(refEntity, descriptors);
                    }
                }
            });
        }

        super.runInspection(scope, manager, globalContext, problemDescriptionsProcessor);
    }

    @Nullable
    @Override
    public RefGraphAnnotator getAnnotator(@NotNull RefManager refManager) {
        return new YellowVoInspection.UnusedVariablesGraphAnnotator(InspectionManager.getInstance(refManager.getProject()), refManager);
    }

    @Override
    public boolean queryExternalUsagesRequests(@NotNull InspectionManager manager,
                                               @NotNull GlobalInspectionContext globalContext,
                                               @NotNull ProblemDescriptionsProcessor problemDescriptionsProcessor) {

        System.out.println("YellowVoInspection queryExternalUsagesRequests");

        final boolean requests = super.queryExternalUsagesRequests(manager, globalContext, problemDescriptionsProcessor);
        if (!requests) {
            myUnusedParameters.queryExternalUsagesRequests(manager, globalContext, problemDescriptionsProcessor);
        }
        return requests;
    }

    @Nullable
    @Override
    public String getHint(@NotNull QuickFix fix) {
        return myUnusedParameters.getHint(fix);
    }

    @Nullable
    @Override
    public QuickFix getQuickFix(String hint) {
        return myUnusedParameters.getQuickFix(hint);
    }

    @Override
    protected UnusedSymbolLocalInspectionBase createUnusedSymbolLocalInspection() {
        //noinspection deprecation
        return new UnusedSymbolLocalInspection();
    }

    /**
     * 创建 选项面板
     * @author YellowTail
     * @since 2020-12-06
     */
    @Override
    public JComponent createOptionsPanel() {
        JTabbedPane tabs = new JBTabbedPane(SwingConstants.TOP);
        tabs.add(JavaBundle.message("tab.title.members.to.report"), ScrollPaneFactory.createScrollPane(myLocalInspectionBase.createOptionsPanel(), true));
        tabs.add(JavaBundle.message("tab.title.entry.points"), ScrollPaneFactory.createScrollPane(new YellowVoInspection.OptionsPanel(), true));
        return tabs;
    }

    private final class OptionsPanel extends JPanel {
        private final JCheckBox myMainsCheckbox;
        private final JCheckBox myAppletToEntries;
        private final JCheckBox myServletToEntries;
        private final JCheckBox myNonJavaCheckbox;

        private OptionsPanel() {
            super(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.weightx = 1;
            gc.weighty = 0;

            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.anchor = GridBagConstraints.NORTHWEST;
            gc.gridx = 0;
            gc.gridy = 0;
            gc.gridwidth = 2;
            add(new JBLabel(JavaBundle.message("label.unused.declaration.reachable.from.tests.option")), gc);
            gc.gridy++;

            final JBRadioButton asEntryPoint = new JBRadioButton(JavaBundle.message("radio.button.unused.declaration.used.option"), isTestEntryPoints());
            final JBRadioButton asUnused = new JBRadioButton(JavaBundle.message("radio.button.unused.declaration.unused.option"), !isTestEntryPoints());
            final ButtonGroup group = new ButtonGroup();
            group.add(asEntryPoint);
            group.add(asUnused);
            final ActionListener listener = e -> setTestEntryPoints(asEntryPoint.isSelected());
            asEntryPoint.addActionListener(listener);
            asUnused.addActionListener(listener);

            gc.gridwidth = 1;
            gc.weightx = 0;
            add(asEntryPoint, gc);
            gc.gridx = 1;
            gc.weightx = 1;
            add(asUnused, gc);

            gc.gridx = 0;
            gc.gridy++;

            gc.gridwidth = 2;
            add(new TitledSeparator(), gc);
            gc.gridy++;
            add(new JBLabel(JavaBundle.message("label.entry.points")), gc);
            gc.insets = JBUI.insets(5, 0, 0, 0);
            gc.gridy++;

            add(createBtnPanel(), gc);
            gc.gridy++;
            gc.insets = JBUI.insets(0, 5, 2, 0);

            myMainsCheckbox = new JCheckBox(JavaBundle.message("inspection.dead.code.option.main"));
            myMainsCheckbox.setSelected(ADD_MAINS_TO_ENTRIES);
            myMainsCheckbox.addActionListener(e -> ADD_MAINS_TO_ENTRIES = myMainsCheckbox.isSelected());


            add(myMainsCheckbox, gc);
            gc.gridy++;

            myAppletToEntries = new JCheckBox(JavaBundle.message("inspection.dead.code.option.applet"));
            myAppletToEntries.setSelected(ADD_APPLET_TO_ENTRIES);
            myAppletToEntries.addActionListener(e -> ADD_APPLET_TO_ENTRIES = myAppletToEntries.isSelected());
            add(myAppletToEntries, gc);
            gc.gridy++;

            myServletToEntries = new JCheckBox(JavaBundle.message("inspection.dead.code.option.servlet"));
            myServletToEntries.setSelected(ADD_SERVLET_TO_ENTRIES);
            myServletToEntries.addActionListener(e -> ADD_SERVLET_TO_ENTRIES = myServletToEntries.isSelected());
            add(myServletToEntries, gc);
            gc.gridy++;

            for (final EntryPoint extension : getExtensions()) {
                if (extension.showUI()) {
                    final JCheckBox extCheckbox = new JCheckBox(extension.getDisplayName());
                    extCheckbox.setSelected(extension.isSelected());
                    extCheckbox.addActionListener(e -> extension.setSelected(extCheckbox.isSelected()));
                    add(extCheckbox, gc);
                    gc.gridy++;
                }
            }

            myNonJavaCheckbox =
                    new JCheckBox(JavaBundle.message("inspection.dead.code.option.external"));
            myNonJavaCheckbox.setSelected(ADD_NONJAVA_TO_ENTRIES);
            myNonJavaCheckbox.addActionListener(e -> ADD_NONJAVA_TO_ENTRIES = myNonJavaCheckbox.isSelected());

            gc.weighty = 1;
            add(myNonJavaCheckbox, gc);
        }

        private JPanel createBtnPanel() {
            final JPanel btnPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.fill = GridBagConstraints.NONE;
            constraints.weightx = 0;
            btnPanel.add(EntryPointsManagerImpl.createConfigureClassPatternsButton(), constraints);
            constraints.gridx = 1;
            btnPanel.add(EntryPointsManagerImpl.createConfigureAnnotationsButton(), constraints);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1;
            btnPanel.add(Box.createHorizontalBox(), constraints);
            return btnPanel;
        }
    }

    private class UnusedVariablesGraphAnnotator extends RefGraphAnnotator {
        private final InspectionManager myInspectionManager;
        private final GlobalInspectionContextImpl myContext;
        private final Map<String, Tools> myTools;

        UnusedVariablesGraphAnnotator(InspectionManager inspectionManager, RefManager refManager) {
            myInspectionManager = inspectionManager;
            myContext = (GlobalInspectionContextImpl)((RefManagerImpl)refManager).getContext();
            myTools = myContext.getTools();
        }

        @Override
        public void onReferencesBuild(RefElement refElement) {
            System.out.println("YellowVoInspection UnusedVariablesGraphAnnotator onReferencesBuild");

            if (refElement instanceof RefClass) {
                UClass uClass = ((RefClass)refElement).getUastElement();
                if (uClass != null) {
                    for (UClassInitializer initializer : uClass.getInitializers()) {
                        findUnusedLocalVariables(initializer.getUastBody(), refElement);
                    }
                }
            }
            else if (refElement instanceof RefMethod) {
                UDeclaration element = ((RefMethod)refElement).getUastElement();
                if (element instanceof UMethod) {
                    UExpression body = ((UMethod)element).getUastBody();
                    if (body != null) {
                        findUnusedLocalVariables(body, refElement);
                    }
                }
            }
        }

        private void findUnusedLocalVariables(UExpression body, RefElement refElement) {
            if (body == null) return;
            PsiCodeBlock bodySourcePsi = ObjectUtils.tryCast(body.getSourcePsi(), PsiCodeBlock.class);
            if (bodySourcePsi == null) return;
            Tools tools = myTools.get(getShortName());
            if (!tools.isEnabled(bodySourcePsi)) return;
            InspectionToolWrapper toolWrapper = tools.getInspectionTool(bodySourcePsi);
            InspectionToolPresentation presentation = myContext.getPresentation(toolWrapper);
            if (((YellowVoInspection)toolWrapper.getTool()).getSharedLocalInspectionTool().LOCAL_VARIABLE) {
                List<CommonProblemDescriptor> descriptors = new ArrayList<>();
                findUnusedLocalVariablesInCodeBlock(bodySourcePsi, descriptors);
                if (!descriptors.isEmpty()) {
                    presentation.addProblemElement(refElement, descriptors.toArray(CommonProblemDescriptor.EMPTY_ARRAY));
                }
            }
        }

        private void findUnusedLocalVariablesInCodeBlock(@NotNull PsiCodeBlock codeBlock, @NotNull List<CommonProblemDescriptor> descriptors) {
            Set<PsiVariable> usedVariables = new THashSet<>();
            List<DefUseUtil.Info> unusedDefs = DefUseUtil.getUnusedDefs(codeBlock, usedVariables);
            if (unusedDefs != null && !unusedDefs.isEmpty()) {
                for (DefUseUtil.Info varDefInfo : unusedDefs) {
                    PsiElement parent = varDefInfo.getContext();
                    PsiVariable psiVariable = varDefInfo.getVariable();
                    if (parent instanceof PsiDeclarationStatement || parent instanceof PsiResourceVariable) {
                        if (!varDefInfo.isRead() && !SuppressionUtil.inspectionResultSuppressed(psiVariable, YellowVoInspection.this)) {
                            descriptors.add(createProblemDescriptor(psiVariable));
                        }
                    }
                }
            }
            codeBlock.accept(new JavaRecursiveElementWalkingVisitor() {
                @Override
                public void visitClass(PsiClass aClass) {
                    // prevent going to local classes
                }

                @Override
                public void visitLambdaExpression(PsiLambdaExpression lambdaExpr) {
                    PsiCodeBlock lambdaBody = ObjectUtils.tryCast(lambdaExpr.getBody(), PsiCodeBlock.class);
                    if (lambdaBody != null) {
                        findUnusedLocalVariablesInCodeBlock(lambdaBody, descriptors);
                    }
                }

                @Override
                public void visitLocalVariable(PsiLocalVariable variable) {
                    if (!usedVariables.contains(variable) && variable.getInitializer() == null &&
                            !SuppressionUtil.inspectionResultSuppressed(variable, YellowVoInspection.this)) {
                        descriptors.add(createProblemDescriptor(variable));
                    }
                }
            });
        }

        private ProblemDescriptor createProblemDescriptor(PsiVariable psiVariable) {
            PsiElement toHighlight = ObjectUtils.notNull(psiVariable.getNameIdentifier(), psiVariable);
            return myInspectionManager.createProblemDescriptor(
                    toHighlight,
                    JavaBundle.message("inspection.unused.assignment.problem.descriptor1", "<code>#ref</code> #loc"), (LocalQuickFix)null,
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL, false);
        }
    }
}
