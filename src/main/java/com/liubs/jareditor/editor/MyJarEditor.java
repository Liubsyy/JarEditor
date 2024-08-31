package com.liubs.jareditor.editor;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.PsiErrorElementUtil;
import com.intellij.util.ui.JBUI;
import com.liubs.jareditor.decompile.MyDecompiler;
import com.liubs.jareditor.persistent.SDKSettingStorage;
import com.liubs.jareditor.sdk.JavacToolProvider;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.template.TemplateManager;
import com.liubs.jareditor.constant.ClassVersion;
import com.liubs.jareditor.util.CommandTools;
import com.liubs.jareditor.util.MyPathUtil;
import com.liubs.jareditor.util.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * @author Liubsyy
 * @date 2024/5/8
 */
public class MyJarEditor extends UserDataHolderBase implements FileEditor {
    private static final String SDK_DEFAULT = "SDK Default";
    private final Project project;
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final VirtualFile file;
    private final Editor editor;

    private JCheckBox needCompiled;
    private ComboBox<String> selectJDKComboBox;
    private ArrayList<String> javaHomes = new ArrayList<>();

    private ComboBox<String> selectVersionComboBox;

    private ArrayList<JComponent> compiledUIComponents = new ArrayList<>();

    //上次选中的sdk，保持状态
    private static String lastSelectItem = null;

    //编译打包相关逻辑
    private JarEditorCore jarEditorCore;

    //source jar相关的逻辑
    private SourceJarResolver sourceJarResolver;

    //是否从jar_edit_out目录导入的保存文件
    private boolean importFromSavedFile;

    @Nullable
    @Override
    public VirtualFile getFile() {
        return file;
    }

    public MyJarEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
        this.editor = createEditor();
        this.jarEditorCore = new JarEditorCore(project, file, editor);
        this.sourceJarResolver = new SourceJarResolver(project,file,editor);

        mainPanel.add(editor.getComponent(), BorderLayout.CENTER);

        // Create UI
        needCompiled = new JCheckBox("Compile");
        JButton saveButton = new JButton("Save");
        JButton rebuildJar = new JButton("Build Jar");
        JButton resetButton = new JButton("Reset");

        JPanel optPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        this.createActionToolBar(optPanel);
        this.addCompiledUI(optPanel);
        optPanel.add(needCompiled);
        optPanel.add(saveButton);
        optPanel.add(rebuildJar);
        optPanel.add(resetButton);

        needCompiled.setSelected("class".equals(file.getExtension()) || "kt".equals(file.getExtension()));
        compiledUIVisible(needCompiled.isSelected());

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        optPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,"JarEditor Tools"));

        JPanel optPanelWrapper = new JPanel(new BorderLayout());
        optPanelWrapper.add(optPanel,BorderLayout.CENTER);

        if(sourceJarResolver.isSourceJar()) {
            //source jar增加跳转到到class jar的链接
            optPanelWrapper.add(sourceJarResolver.createSourceJarNotification(),BorderLayout.NORTH);
        }else if(sourceJarResolver.isClassJarAndHasSourceFile()) {
            //class jar中如果有-source.jar，可以选中从-source.jar中导入代码
            optPanelWrapper.add(sourceJarResolver.getDecompiledOrSourceTextPanel(),BorderLayout.NORTH);
        }

        mainPanel.add(optPanelWrapper, BorderLayout.SOUTH);

        //add action listener
        needCompiled.addActionListener(e -> compiledUIVisible(needCompiled.isSelected()));
        saveButton.addActionListener(e -> saveChanges());
        rebuildJar.addActionListener(e -> buildJar());
        resetButton.addActionListener(e -> cancelChanges());
    }

    private void createActionToolBar(JPanel optPanel){
        AnAction jarEditorClear = ActionManager.getInstance().getAction("jarEditorClear");
        AnAction jarEditorSearch = ActionManager.getInstance().getAction("jarEditorSearch");
        AnAction classBytesTool = ActionManager.getInstance().getAction("classBytesTool");

        ArrayList<AnAction> actions = new ArrayList<>();
        if(null != jarEditorClear) {
            actions.add(jarEditorClear);
        }
        if(null != jarEditorSearch) {
            actions.add(jarEditorSearch);
        }
        if(null != classBytesTool) {
            actions.add(classBytesTool);
        }

        ActionToolbar myToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR,
                new DefaultActionGroup(actions), false);
        myToolBar.setOrientation(SwingConstants.HORIZONTAL); //设置工具栏为水平方向


        optPanel.add(myToolBar.getComponent());
    }

    private void compiledUIVisible(boolean visible){
        if("class".equals(file.getExtension()) && !visible){
            NoticeInfo.warning("Class file must be compiled !!!");
            needCompiled.setSelected(true);
            return;
        }
        if(sourceJarResolver.isSourceJar() && visible) {
            int response = Messages.showYesNoDialog(
                    project,
                    "You are editing a source jar, not class jar, are you sure you want to compile it ?",
                    "Compilation Confirmation",
                    Messages.getQuestionIcon()
            );

            if (response != Messages.YES) {
                needCompiled.setSelected(false);
                return;
            }
        }

        compiledUIComponents.forEach(c->{
            c.setVisible(visible);
        });
    }

    private void initSDKComboBox(){
        javaHomes.clear();
        selectJDKComboBox.removeAllItems();
        selectVersionComboBox.removeAllItems();

        selectJDKComboBox.addItem(SDK_DEFAULT);
        Set<String> allItems = new HashSet<>();
        javaHomes.add("");
        for(SDKSettingStorage.MyItem sdkItem : SDKSettingStorage.getMySdksDefaultProjectSdks()){
            allItems.add(sdkItem.getName());
            selectJDKComboBox.addItem(sdkItem.getName());
            javaHomes.add(sdkItem.getPath());
        }
        selectJDKComboBox.setSelectedItem(SDK_DEFAULT);
        try{
            if(null != lastSelectItem && allItems.contains(lastSelectItem)) {
                selectJDKComboBox.setSelectedItem(lastSelectItem);
            }
        }catch (Throwable ee) {
            selectJDKComboBox.setSelectedItem(SDK_DEFAULT);
        }

        //选择编译版本
        String classVersion = ClassVersion.detectClassVersion(file);
        int maxJdkVersion = -1;
        try{
            maxJdkVersion = JavacToolProvider.getMaxJdkVersion();
        }catch (Throwable eex){}

        if(maxJdkVersion < 0) {
            maxJdkVersion = 21;
        }
        for(int i=1;i<=maxJdkVersion;i++) {
            if(ClassVersion.ELDEN_VERSIONS.containsKey(i)) {
                selectVersionComboBox.addItem(ClassVersion.ELDEN_VERSIONS.get(i));
            }else {
                selectVersionComboBox.addItem(String.valueOf(i));
            }
        }
        if(StringUtils.isNotEmpty(classVersion)) {
            selectVersionComboBox.setSelectedItem(classVersion);
        }
    }

    private void addCompiledUI(JPanel buttonPanel){
        //select SDK
        selectJDKComboBox = new ComboBox<>(120);
        //select version
        selectVersionComboBox = new ComboBox<>(70);

        JLabel sdkLabel = new JLabel("<html><span style=\"color: #5799EE;\">SDK</span></html>");
        sdkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sdkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SDKSettingDialog dialog = new SDKSettingDialog();
                if(dialog.showAndGet()){
                    //持久化
                    SDKSettingStorage.getInstance().setMySdks(dialog.getAllItems());
                    SDKSettingStorage.getInstance().setGenDebugInfos(dialog.getGenDebugInfo());

                    //刷新最高jdk版本
                    int maxJavaVersion = SDKSettingStorage.getInstance().getMySdks().parallelStream().map(sdk->{
                        try{
                            String javacVersion = CommandTools.exec(sdk.getPath()+"/bin/javac","-version");
                            if(null != javacVersion) {
                                return JavacToolProvider.parseJavaVersion(javacVersion);
                            }
                        }catch (Throwable ex){}
                        return 8;
                    }).max(Integer::compareTo).orElse(8);
                    maxJavaVersion = Math.max(maxJavaVersion,JavacToolProvider.parseJavaVersion(System.getProperty("java.version"))); //当前IDEA运行的java版本

                    SDKSettingStorage.getInstance().setMaxJavaVersion(maxJavaVersion);

                    initSDKComboBox();
                }
            }
        });

        buttonPanel.add(sdkLabel);
        buttonPanel.add(selectJDKComboBox);

        JLabel compiled_version = new JLabel("Target");
        buttonPanel.add(compiled_version);
        buttonPanel.add(selectVersionComboBox);

        initSDKComboBox();
        selectJDKComboBox.addActionListener((e)-> lastSelectItem = (String) selectJDKComboBox.getSelectedItem());

        compiledUIComponents.add(sdkLabel);
        compiledUIComponents.add(selectJDKComboBox);
        compiledUIComponents.add(compiled_version);
        compiledUIComponents.add(selectVersionComboBox);
    }


    private Editor createEditor(){
        String decompiledText = getDecompiledText(project, file);

        String fileExtension = file.getExtension();
        Editor editor = null;

        if(StringUtils.isEmpty(decompiledText)) {
            decompiledText = TemplateManager.getText(fileExtension, file.getPath());
        }

        if(null != fileExtension) {
            String fileName = file.getName();
            if("class".equals(fileExtension)) {
                fileExtension = JavaFileType.DEFAULT_EXTENSION;
                fileName = fileName.replace(".class", ".java");
            }

            try{
                FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(fileExtension);

                LightVirtualFile virtualFile = new LightVirtualFile(fileName,
                        fileType, decompiledText);
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

                if(null != psiFile && null != psiFile.getVirtualFile()) {
                    //editor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, psiFile.getVirtualFile()), true);
                    editor = EditorFactory.getInstance().createEditor(psiFile.getViewProvider().getDocument(), project);

                    // springboot项目暂时先禁用语法检测
                    if(file.getPath().contains("BOOT-INF")) {
                        DaemonCodeAnalyzer daemonCodeAnalyzer = DaemonCodeAnalyzer.getInstance(project);
                        daemonCodeAnalyzer.setHighlightingEnabled(psiFile, false);
                    }

                }

            }catch (Throwable e) {
                e.printStackTrace();
            }
        }


        //default editor
        if(null == editor) {
            Document document = EditorFactory.getInstance().createDocument(decompiledText);
            editor = EditorFactory.getInstance().createEditor(document, project);
        }

        if(null != editor){
            if (editor instanceof EditorEx) {
                EditorEx editorEx = (EditorEx) editor;
                editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, file));
                editorEx.setCaretVisible(true);
                editorEx.setEmbeddedIntoDialogWrapper(true);
            }
        }

        return editor;
    }

    public static String getDecompiledText(Project project, VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null && !PsiErrorElementUtil.hasErrors(project, file)) {
            if(Objects.equals(file.getExtension(), "class")
                    && !"java".equalsIgnoreCase(psiFile.getLanguage().getDisplayName())) {
                String decompileText = MyDecompiler.decompileText(file);
                return StringUtils.isEmpty(decompileText) ? psiFile.getText() : decompileText;
            }
            return psiFile.getText(); //default decompiled text;
        }
        return "";
    }


    private void saveChanges() {
        if(needCompiled.isSelected()){
            String javaHome = null;
            if(selectJDKComboBox.getSelectedIndex()>0) {
                javaHome = javaHomes.get(selectJDKComboBox.getSelectedIndex());
            }
            jarEditorCore.compileCode(javaHome,(String) selectVersionComboBox.getSelectedItem());
        }else {
            jarEditorCore.saveResource();
        }
    }

    private void buildJar() {
        jarEditorCore.buildJar();
    }

    private void cancelChanges() {
        String decompiledText = getDecompiledText(project, file);
        Document document = editor.getDocument();

        // 使用 WriteCommandAction 修改文件内容
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.setText(decompiledText);
        });

        // 提交文档更改
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }

    public Editor getEditor(){
        return editor;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return editor.getContentComponent();
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "Jar Editor";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {}

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return file.isValid();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {}

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {}

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {
        try{
            EditorFactory.getInstance().releaseEditor(editor);
        }catch (Throwable e) {}

    }


    private boolean onceNotified = false;


    /**
     * 切换到编辑页时触发
     * 1.如果有Save的临时文件，弹出确认框可导入修改的内容
     */
    @Override
    public void selectNotify() {
        FileEditor.super.selectNotify();

        if(!onceNotified) {
            onceNotified = true;

            try{
                String jarEditOutput = MyPathUtil.getJarEditOutput(file.getPath());
                String entryPathFromJar = MyPathUtil.getEntryPathFromJar(file.getPath());

                String savePath = jarEditOutput+"/"+entryPathFromJar;

                //如果有保存的文件，弹出确认框可导入修改文本
                if(Files.exists(Paths.get(savePath))) {
                    if(Messages.YES == Messages.showYesNoDialog(project,
                            "This file was modified last time, do you need to import the changes ?",
                            "Import Confirmation",
                            Messages.getQuestionIcon())){
                        loadEditorContentFromSavedFile(savePath);
                    }
                }
            }catch (Throwable e) {
                e.printStackTrace();
            }

        }

    }


    public void loadEditorContentFromSavedFile(String savePath) throws IOException {
        final String newText;
        if("class".equals(file.getExtension())){
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(savePath.replace("\\","/"));
            if (virtualFile != null) {
                virtualFile.refresh(false, false);
            }
            newText = getDecompiledText(project, virtualFile);
        }else {
            newText = Files.readString(Paths.get(savePath));
        }

        if(StringUtils.isNotEmpty(newText)) {
            Document document = editor.getDocument();

            WriteCommandAction.runWriteCommandAction(project, () -> {
                document.setText(newText);
            });

            PsiDocumentManager.getInstance(project).commitDocument(document);
            importFromSavedFile = true;
        }
    }

    public boolean isImportFromSavedFile() {
        return importFromSavedFile;
    }
}
