package com.liubs.jareditor.bytestool.javassist;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.liubs.jareditor.editor.MyJarEditor;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyPathUtil;
import com.liubs.jareditor.util.PsiFileUtil;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * javassist面板
 * @author Liubsyy
 * @date 2024/8/27
 */
public class JavassistDialog extends DialogWrapper {

    private final Project project;
    private final VirtualFile virtualFile;
    private JavassistTool javassistTool;

    //需要从MyJarEditor中导入代码段到javassist编辑器editor
    private MyJarEditor myJarEditor;

    //UI组件
    private JRadioButton modifyRadio;
    private JRadioButton addRadio;
    private JRadioButton deleteRadio;
    private JLabel operationLabel;
    private ComboBox<TargetUnit> targetComboBox;
    private ComboBox<String> operationComboBox;

    //targets暂存
    private java.util.List<TargetUnit> targets;//

    //编辑器
    private Editor importEditor;
    private Editor editor;


    public JavassistDialog(@Nullable Project project, VirtualFile virtualFile, MyJarEditor myJarEditor) {
        super(true);
        this.project = project;
        this.virtualFile = virtualFile;
        this.myJarEditor = myJarEditor;
        try{
            if(myJarEditor.isImportFromSavedFile()) {
                String jarEditOutput = MyPathUtil.getJarEditOutput(virtualFile.getPath());
                String entryPathFromJar = MyPathUtil.getEntryPathFromJar(virtualFile.getPath());
                String savePath = jarEditOutput+"/"+entryPathFromJar;

                this.javassistTool = new JavassistTool(project,new FileInputStream(savePath));
            }else {
                this.javassistTool = new JavassistTool(project,virtualFile.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        init();
        setTitle("Javassist Tool");
        pack(); //调整窗口大小以适应其子组件
        setModal(false);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        JPanel mainPanel = new JPanel(new GridLayoutManager(9, 2));
        mainPanel.setPreferredSize(new Dimension(700, 500));

        String className = virtualFile.getName();
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile instanceof PsiClassOwner) {
            PsiClass[] classes = ((PsiClassOwner) psiFile).getClasses();
            for (PsiClass psiClass : classes) {
                className = psiClass.getQualifiedName();
                break;
            }
        }

        int line = 0;

        JLabel classNameLabel = new JLabel("Class");
        JLabel classNameValue = new JLabel(className);
        mainPanel.add(classNameLabel, new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(classNameValue, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));

        line++;

        JLabel actionLabel = new JLabel("Action");
        modifyRadio = new JRadioButton("Modify",true);
        addRadio = new JRadioButton("Add");
        deleteRadio = new JRadioButton("Delete");

        ButtonGroup group = new ButtonGroup();
        group.add(modifyRadio);
        group.add(addRadio);
        group.add(deleteRadio);

        JPanel radios = new JPanel();
        radios.add(modifyRadio);
        radios.add(Box.createHorizontalStrut(10));
        radios.add(addRadio);
        radios.add(Box.createHorizontalStrut(10));
        radios.add(deleteRadio);

        mainPanel.add(actionLabel, new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(radios, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));

        line++;

        JLabel targetLabel = new JLabel("Target");
        targetComboBox = new ComboBox<>();

        //加载字段/函数
        targets = new ArrayList<>();
        for(CtConstructor constructor : javassistTool.getConstructors()){
            try {
                targets.add(new TargetUnit(ISignature.Type.CONSTRUCTOR, new ConstructorSignature(constructor)));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }

        for(CtField ctField : javassistTool.getFields()){
            try {
                targets.add(new TargetUnit(ISignature.Type.FIELD, new FieldSignature(ctField)));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }

        for(CtMethod ctMethod : javassistTool.getMethods()){
            try {
                targets.add(new TargetUnit(ISignature.Type.METHOD, new MethodSignature(ctMethod)));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        for(TargetUnit targetUnit : targets) {
            targetComboBox.addItem(targetUnit);
        }



        mainPanel.add(targetLabel, new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(targetComboBox, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));


        line++;

        operationLabel = new JLabel("Operation");
        operationComboBox = new ComboBox<>();
        operationComboBox.addItem("setBody");
        operationComboBox.addItem("insertBefore");
        operationComboBox.addItem("insertAfter");




        mainPanel.add(operationLabel, new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(operationComboBox, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));


        line++;


        //Collection<PsiMethod> methods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);


//
//        LightVirtualFile virtualFile = new LightVirtualFile("fileName",
//                JavaFileType.INSTANCE, "");
//        PsiFile psiFileLight = PsiManager.getInstance(project).findFile(virtualFile);

//        this.editor = EditorFactory.getInstance().createEditor(psiFileLight.getViewProvider().getDocument(), project);
//        this.editor = EditorFactory.getInstance().createEditor(psiFileLight.getViewProvider().getDocument(), project);


        List<String> allImports = PsiFileUtil.getAllImports(getJarEditorPsiFile());
        this.importEditor = EditorFactory.getInstance().createEditor(
                EditorFactory.getInstance().createDocument(String.join("\n",allImports)), project);

        if(null != importEditor) {

            if (importEditor instanceof EditorEx) {
                EditorEx editorEx = (EditorEx) importEditor;
                editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, virtualFile));
                editorEx.setCaretVisible(true);
                editorEx.setEmbeddedIntoDialogWrapper(true);
            }

            mainPanel.add(new JLabel("Import"), new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED, null, null, null));
            line++;

            mainPanel.add(importEditor.getComponent(), new GridConstraints(line, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED, new Dimension(700,100), new Dimension(-1,100),new Dimension(-1,100)));

            line++;
        }

        this.editor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""), project);
        if(null != editor){
            if (editor instanceof EditorEx) {
                EditorEx editorEx = (EditorEx) editor;
                editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, virtualFile));
                editorEx.setCaretVisible(true);
                editorEx.setEmbeddedIntoDialogWrapper(true);
            }
            mainPanel.add(new JLabel("Code"), new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED, null, null, null));
            line++;

            mainPanel.add(editor.getComponent(), new GridConstraints(line, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED, new Dimension(700,300), new Dimension(-1,300), new Dimension(-1,300)));

            line++;
        }

        /*
        // 设置编辑器的文件类型，例如 Java 或者 PlainText
//        FileType fileType = PlainTextFileType.INSTANCE;
        FileType fileType = JavaFileType.INSTANCE;

        // 创建一个虚拟文件作为编辑器的上下文（语法高亮需要）
        PsiFile psiFileLight = PsiFileFactory.getInstance(project)
                .createFileFromText("Dummy.java", fileType, "");


        // 创建 Editor
        editorTextField = new EditorTextField(psiFileLight.getViewProvider().getDocument(), project, fileType, false, false);
        editorTextField.setPreferredWidth(500);
        editor = editorTextField.getEditor();

        if (editor != null) {
            if (editor instanceof EditorEx) {
                EditorEx editorEx = (EditorEx) editor;
                editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project,
                        psiFileLight.getVirtualFile()));
                editorEx.setHorizontalScrollbarVisible(true);
                editorEx.setVerticalScrollbarVisible(true);
            }

        }
        mainPanel.add(editorTextField, new GridConstraints(line, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, new Dimension(500,300), null, null));

        line++;
*/


        JPanel optPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn  = new JButton("Save");
        JButton buildJarBtn  = new JButton("Build Jar");
        optPanel.add(saveBtn);
        optPanel.add(buildJarBtn);

        mainPanel.add(optPanel, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_EAST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));


        //UI事件
        modifyRadio.addItemListener(this::actionRadioChange);
        addRadio.addItemListener(this::actionRadioChange);
        deleteRadio.addItemListener(this::actionRadioChange);

        targetComboBox.addActionListener(this::targetComboBoxSelect);
        operationComboBox.addActionListener(this::operationComboBoxSelect);

        if(targetComboBox.getItemCount() > 0){
            targetComboBox.setSelectedIndex(0);
        }

        saveBtn.addActionListener(this::saveBtn);

        return mainPanel;
    }


    @Override
    protected Action [] createActions() {
        // Return an empty array to hide OK and Cancel buttons
        return new Action[0];
    }


    @Override
    public void doCancelAction() {
        super.doCancelAction();
        try{
            EditorFactory.getInstance().releaseEditor(importEditor);
        }catch (Throwable e) {}

        try{
            EditorFactory.getInstance().releaseEditor(editor);
        }catch (Throwable e) {}
    }

    private void setOperationVisible(){
        boolean showOperationUI = modifyRadio.isSelected();
        if(showOperationUI){
            TargetUnit selectedItem = (TargetUnit)targetComboBox.getSelectedItem();
            if(null != selectedItem) {
                if(selectedItem.getType() == ISignature.Type.METHOD
                        || selectedItem.getType() == ISignature.Type.CONSTRUCTOR){
                    showOperationUI = true;
                }else {
                    showOperationUI = false;
                }
            }else {
                showOperationUI = false;
            }
        }
        operationLabel.setVisible(showOperationUI);
        operationComboBox.setVisible(showOperationUI);
    }

    public void actionRadioChange(ItemEvent e){
        if(e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }

        setOperationVisible();

        targetComboBox.removeAllItems();
        if(modifyRadio.isSelected() || deleteRadio.isSelected()) {
            for(TargetUnit targetUnit : targets) {
                targetComboBox.addItem(targetUnit);
            }
        }else {
            targetComboBox.addItem(new TargetUnit(ISignature.Type.FIELD,null));
            targetComboBox.addItem(new TargetUnit(ISignature.Type.METHOD,null));
            targetComboBox.addItem(new TargetUnit(ISignature.Type.CONSTRUCTOR,null));
        }
    }


    public void targetComboBoxSelect(ActionEvent e){
        setOperationVisible();

        TargetUnit selectedItem = (TargetUnit)targetComboBox.getSelectedItem();
        if(null == selectedItem) {
            return;
        }

        editor.getDocument().setReadOnly(false);

        PsiFile psiFile = getJarEditorPsiFile();
        java.util.List<PsiElement> psiElements = PsiTreeUtil.findChildrenOfType(psiFile, PsiMember.class)
                    .stream().filter(selectedItem::isSameTarget).collect(Collectors.toList());
        String text = "" ;
        if(!psiElements.isEmpty() ){
            text = selectedItem.convertToJavassistCode(psiFile,psiElements.get(0));
        }
        if(modifyRadio.isSelected() & (
                "insertBefore".equals(operationComboBox.getSelectedItem())
                        || "insertAfter".equals(operationComboBox.getSelectedItem())
        )){
            text = "{\n}";
        }


        String finalText = text;
        // 使用 WriteCommandAction 修改文件内容
        Document document = editor.getDocument();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.setText(finalText);
        });

        // 提交文档更改
        PsiDocumentManager.getInstance(project).commitDocument(document);

        if(deleteRadio.isSelected()) {
            editor.getDocument().setReadOnly(true);
        }
    }

    public void operationComboBoxSelect(ActionEvent e){
        targetComboBoxSelect(e);
    }

    public void saveBtn(ActionEvent e){
        JavassistTool.Result result = null;
        try{
            javassistTool.imports(importEditor.getDocument().getText().split("\n"));
        }catch (Exception importErr){
            importErr.printStackTrace();
        }

        if(modifyRadio.isSelected()) {
            TargetUnit targetUnit = (TargetUnit)targetComboBox.getSelectedItem();
            if(null == targetUnit) {
                return;
            }
            if(targetUnit.getType() == ISignature.Type.FIELD) {
                String text = editor.getDocument().getText();
                result = javassistTool.modifyField((CtField) targetUnit.getTargetSignature().getMember(), text.trim());
            }else if(targetUnit.getType() == ISignature.Type.METHOD ||  targetUnit.getType() == ISignature.Type.CONSTRUCTOR){
                String operation = (String)operationComboBox.getSelectedItem();
                String text = editor.getDocument().getText();
                int i = text.indexOf('{');
                int j = text.lastIndexOf('}');
                if(i<0 ||j<0) {
                    return;
                }

                if("setBody".equals(operation)) {
                    result = javassistTool.setBody((CtMethod) targetUnit.getTargetSignature().getMember(), text.substring(i, j+1));
                }else if("insertBefore".equals(operation)) {
                    result = javassistTool.insertBefore((CtMethod) targetUnit.getTargetSignature().getMember(), text.substring(i, j+1));
                }else if("insertAfter".equals(operation)) {
                    result = javassistTool.insertAfter((CtMethod) targetUnit.getTargetSignature().getMember(), text.substring(i, j+1));
                }
            }
        }else if(addRadio.isSelected()) {
            TargetUnit targetUnit = (TargetUnit)targetComboBox.getSelectedItem();
            if(null == targetUnit) {
                return;
            }
            if(targetUnit.getType() == ISignature.Type.FIELD) {
                String text = editor.getDocument().getText();
                result = javassistTool.addField(text.trim());
            }
        }else if(deleteRadio.isSelected()){
            TargetUnit targetUnit = (TargetUnit)targetComboBox.getSelectedItem();
            if(null == targetUnit) {
                return;
            }
            if(targetUnit.getType() == ISignature.Type.FIELD) {
                result = javassistTool.deleteField((CtField) targetUnit.getTargetSignature().getMember());
            }
        }

        if(null != result) {
            if(result.isSuccess()) {
                String jarEditOutput = MyPathUtil.getJarEditOutput(virtualFile.getPath());
                String jarRelativePath = MyPathUtil.getEntryPathFromJar(virtualFile.getPath());
                try {
                    String destinationPath = Paths.get(jarEditOutput, jarRelativePath).toString();
                    File destinationFile = new File(destinationPath);
                    destinationFile.getParentFile().mkdirs();

                    Files.write(Paths.get(destinationPath),result.getBytes());
                    NoticeInfo.info("Save success to: " + destinationPath);

                    //刷新MyJarEditor中的源码加载
                    myJarEditor.loadEditorContentFromSavedFile(destinationPath);

                } catch (Exception ex) {
                    NoticeInfo.error ( "Error write file: " + ex.getMessage());
                }
            }else {
                NoticeInfo.error("Save err: %s",result.getErr());
            }
        }

    }

    private PsiFile getJarEditorPsiFile(){
        VirtualFile file = FileDocumentManager.getInstance()
                .getFile(myJarEditor.getEditor()
                        .getDocument());
        if(null == file) {
            return null;
        }
        return PsiManager.getInstance(project).findFile(file);
    }


}
