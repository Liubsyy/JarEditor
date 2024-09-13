package com.liubs.jareditor.bytestool.javassist;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.liubs.jareditor.editor.MyJarEditor;
import com.liubs.jareditor.sdk.MessageDialog;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.ExceptionUtil;
import com.liubs.jareditor.util.MyPathUtil;
import com.liubs.jareditor.util.PsiFileUtil;
import com.liubs.jareditor.util.StringUtils;
import javassist.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
    private java.util.List<TargetUnit> targets;

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

                if(Files.exists(Paths.get(savePath))){
                    this.javassistTool = new JavassistTool(project,Files.readAllBytes(Paths.get(savePath)));
                }
            }

            if(null == this.javassistTool){
                byte[] classBytes = VfsUtilCore.loadBytes(virtualFile);
                this.javassistTool = new JavassistTool(project, classBytes);
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

        JPanel mainPanel = new JPanel(new GridLayoutManager(8, 2));
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
        targetComboBox = new ComboBox<>(400);

        //加载字段/函数
        targets = new ArrayList<>();

        this.initTarget();


        mainPanel.add(targetLabel, new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(targetComboBox, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(600,-1), null));


        line++;

        operationLabel = new JLabel("Operation");
        operationComboBox = new ComboBox<>(400);
        operationComboBox.addItem("setBody");
        operationComboBox.addItem("insertBefore");
        operationComboBox.addItem("insertAfter");




        mainPanel.add(operationLabel, new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(operationComboBox, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(600,-1), null));


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
            MyJarEditor.handleTabKeyInEditor(project,importEditor);

            mainPanel.add(new JLabel("Import"), new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED, null, null, null));
            line++;

            mainPanel.add(importEditor.getComponent(), new GridConstraints(line, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK ,
                    GridConstraints.SIZEPOLICY_FIXED ,
                    null, new Dimension(700,100),new Dimension(-1,100)));

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
            MyJarEditor.handleTabKeyInEditor(project,editor);

            mainPanel.add(new JLabel("Code"), new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED, null, null, null));
            line++;

//            mainPanel.add(editor.getComponent(), new GridConstraints(line, 0, 1, 2, GridConstraints.ANCHOR_WEST,
//                    GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
//                    GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(700,300), null));

            mainPanel.add(editor.getComponent(), new GridConstraints(line, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK ,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK ,
                    null, new Dimension(700,300), null));


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

        //UI事件
        modifyRadio.addItemListener(this::actionRadioChange);
        addRadio.addItemListener(this::actionRadioChange);
        deleteRadio.addItemListener(this::actionRadioChange);

        targetComboBox.addActionListener(this::targetComboBoxSelect);
        operationComboBox.addActionListener(this::operationComboBoxSelect);

        if(targetComboBox.getItemCount() > 0){
            targetComboBox.setSelectedIndex(0);
        }

        return mainPanel;
    }


    @Override
    protected Action [] createActions() {
        Action saveBtn = new AbstractAction("Run") {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAndSave(e);
            }
        };

        Action buildJarBtn = new AbstractAction("Build Jar") {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildJar(e);
            }
        };
        Action closeButton = new AbstractAction("Close") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 点击按钮时关闭对话框
                close(DialogWrapper.OK_EXIT_CODE);
            }
        };

        return new Action[]{saveBtn,buildJarBtn,closeButton};
    }


    @Override
    public void dispose() {
        try{
            EditorFactory.getInstance().releaseEditor(importEditor);
        }catch (Throwable e) {}

        try{
            EditorFactory.getInstance().releaseEditor(editor);
        }catch (Throwable e) {}

        super.dispose();
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
                        || selectedItem.getType() == ISignature.Type.CONSTRUCTOR
                        || selectedItem.getType() == ISignature.Type.CLASS_INITIALIZER){
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
        updateTargetComboBox();
    }

    private void initTarget(){
        targets.clear();
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

        CtConstructor classInitializer = javassistTool.getClassInitializer();
        targets.add(new TargetUnit(ISignature.Type.CLASS_INITIALIZER,new ClassInitializerSignature(classInitializer)));

        for(CtMethod ctMethod : javassistTool.getMethods()){
            try {
                targets.add(new TargetUnit(ISignature.Type.METHOD, new MethodSignature(ctMethod)));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        TargetUnit selectedItem = (TargetUnit)targetComboBox.getSelectedItem();

        updateTargetComboBox();

        if(null != selectedItem) {
            for(TargetUnit targetUnit : targets) {
                if(targetUnit.equals(selectedItem)) {
                    targetComboBox.setSelectedItem(targetUnit);
                }
            }
        }
    }

    private void updateTargetComboBox(){
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
        if(modifyRadio.isSelected()) {
            if(selectedItem.getType() == ISignature.Type.CLASS_INITIALIZER && StringUtils.isEmpty(text)) {
                text = "static{\n\t//Insert static code here...\n}";
            }

            if("insertBefore".equals(operationComboBox.getSelectedItem())
                    || "insertAfter".equals(operationComboBox.getSelectedItem())) {
                text = "{\n}";
            }

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

    public void runAndSave(ActionEvent e){
        JavassistTool.Result result = null;
        String editorText = "";
        try{
            List<String> imports = new ArrayList<>();
            String[] editorTextTemp = new String[1];
            ApplicationManager.getApplication().runReadAction(() -> {
                imports.addAll(Arrays.stream(importEditor.getDocument().getText().split("\n"))
                        .collect(Collectors.toList()));
                editorTextTemp[0] = editor.getDocument().getText();
            });
            editorText = editorTextTemp[0];
            javassistTool.imports(imports);
        }catch (Exception importErr){
            importErr.printStackTrace();
        }

        if(modifyRadio.isSelected()) {
            TargetUnit targetUnit = (TargetUnit)targetComboBox.getSelectedItem();
            if(null == targetUnit) {
                return;
            }
            if(targetUnit.getType() == ISignature.Type.FIELD) {
                String text = editorText;
                result = javassistTool.modifyField((CtField) targetUnit.getTargetSignature().getMember(), text.trim());
            }else if(targetUnit.getType() == ISignature.Type.METHOD
                    ||  targetUnit.getType() == ISignature.Type.CONSTRUCTOR
                    ||  targetUnit.getType() == ISignature.Type.CLASS_INITIALIZER){
                String operation = (String)operationComboBox.getSelectedItem();
                String text = editorText;
                int i = text.indexOf('{');
                int j = text.lastIndexOf('}');
                if(i<0 ||j<0) {
                    return;
                }


                CtBehavior ctMember = (CtBehavior)targetUnit.getTargetSignature().getMember();
                if(targetUnit.getType() == ISignature.Type.CLASS_INITIALIZER) {
                    //静态代码块如果不存在先创建
                    if(null == ctMember) {
                        ctMember = javassistTool.createClassInitializer();
                    }
                }

                if("setBody".equals(operation)) {
                    result = javassistTool.setBody(ctMember, text.substring(i, j+1));
                }else if("insertBefore".equals(operation)) {
                    result = javassistTool.insertBefore(ctMember, text.substring(i, j+1));
                }else if("insertAfter".equals(operation)) {
                    result = javassistTool.insertAfter(ctMember, text.substring(i, j+1));
                }
            }
        }else if(addRadio.isSelected()) {
            TargetUnit targetUnit = (TargetUnit)targetComboBox.getSelectedItem();
            if(null == targetUnit) {
                return;
            }
            if(targetUnit.getType() == ISignature.Type.FIELD) {
                String text = editorText;
                result = javassistTool.addField(text.trim());
            }else if(targetUnit.getType() == ISignature.Type.METHOD) {
                String text = editorText;
                result = javassistTool.addMethod(text.trim());
            }else if(targetUnit.getType() == ISignature.Type.CONSTRUCTOR) {
                String text = editorText;
                result = javassistTool.addConstructor(text.trim());
            }
        }else if(deleteRadio.isSelected()){
            TargetUnit targetUnit = (TargetUnit)targetComboBox.getSelectedItem();
            if(null == targetUnit) {
                return;
            }
            if(targetUnit.getType() == ISignature.Type.FIELD) {
                result = javassistTool.deleteField((CtField) targetUnit.getTargetSignature().getMember());
            }else if(targetUnit.getType() == ISignature.Type.METHOD) {
                result = javassistTool.deleteMethod((CtMethod) targetUnit.getTargetSignature().getMember());
            }else if(targetUnit.getType() == ISignature.Type.CONSTRUCTOR) {
                result = javassistTool.deleteConstructor((CtConstructor) targetUnit.getTargetSignature().getMember());
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

                    //刷新target
                    if(javassistTool.refreshCache()){
                        if (DumbService.isDumb(project)) {
                            DumbService.getInstance(project).runWhenSmart(this::initTarget);
                        } else {
                            initTarget();
                        }
                    }

                    MessageDialog.showMessageDialog("Run success","Save success to: " + destinationPath);

                } catch (Exception ex) {
                    NoticeInfo.error ( "Error write file: " + ExceptionUtil.getExceptionTracing(ex));
                    MessageDialog.showErrorMessageDialog("Run fail","Error write file: " + ex.getMessage());

                }
            }else {
                NoticeInfo.error("Save err: %s",result.getErr());
                MessageDialog.showErrorMessageDialog("Run fail","Save err: "+result.getErr());

            }
        }

    }

    public void buildJar(ActionEvent e){
        myJarEditor.buildJar(jarBuildResult -> ApplicationManager.getApplication().invokeLater(() -> {
            if(jarBuildResult.isSuccess()) {
                MessageDialog.showMessageDialog("Build success","Build jar successfully!");
                close(DialogWrapper.OK_EXIT_CODE);
            }else {
                MessageDialog.showErrorMessageDialog("Build fail","Build jar err:"+jarBuildResult.getErr());
            }
        }));
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
