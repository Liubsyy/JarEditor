package com.liubs.jareditor.search;

import com.intellij.find.EditorSearchSession;
import com.intellij.find.FindModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.liubs.jareditor.editor.MyFileEditorProvider;
import com.liubs.jareditor.editor.MyJarEditor;
import com.liubs.jareditor.editor.SourceJarResolver;
import com.liubs.jareditor.sdk.ProjectDependency;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 全局搜索
 * @author Liubsyy
 * @date 2024/7/17
 */
public class SearchAllJarPanel extends JPanel {
    private final Project project;

    private JTextField searchField;
    private JButton searchButton;
    private boolean isRunning;
    private DefaultListModel<SearchResultItem> searchResult;
    private volatile ProgressIndicator currentIndicator = null;
    private JCheckBox caseSensitiveCheckBox;
    private JCheckBox wholeWordsCheckBox;
    private JCheckBox regexCheckBox;

    public SearchAllJarPanel(Project project) {
        this.project = project;

        this.setLayout(new BorderLayout());
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchResult = new DefaultListModel<>();
        caseSensitiveCheckBox = new JCheckBox("Match Case", true);
        wholeWordsCheckBox = new JCheckBox("Words", false);
        regexCheckBox = new JCheckBox("Regex", false);

        searchButton.addActionListener(e -> runSearch());

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.add(caseSensitiveCheckBox);
        checkBoxPanel.add(wholeWordsCheckBox);
        checkBoxPanel.add(regexCheckBox);
        searchPanel.add(checkBoxPanel, BorderLayout.SOUTH);

        this.add(searchPanel, BorderLayout.NORTH);

        JBList<SearchResultItem> resultList = new JBList<>(searchResult);
        this.add(new JBScrollPane(resultList), BorderLayout.CENTER);

        resultList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int index = resultList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        SearchResultItem selectedPath = resultList.getModel().getElementAt(index);
                        VirtualFile openFile = selectedPath.getFile();
                        if (openFile != null) {
                            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                            FileEditor[] fileEditors = fileEditorManager.openFile(openFile, true);
                            for (FileEditor fileEditor : fileEditors) {
                                if (!(fileEditor instanceof MyJarEditor)) {
                                    continue;
                                }
                                //切换到JarEditor tab页
                                fileEditorManager.setSelectedEditor(openFile, MyFileEditorProvider.EDITOR_TYPE_ID);

                                final Editor editor = ((MyJarEditor) fileEditor).getEditor();

                                // 这里仅仅是定位到搜索的字符串，并非真正的搜索
                                FindModel findModel = new FindModel();
                                findModel.setStringToFind(searchField.getText());
                                findModel.setCaseSensitive(caseSensitiveCheckBox.isSelected());   //区分大小写
                                findModel.setWholeWordsOnly(wholeWordsCheckBox.isSelected());    //单词搜索
                                findModel.setRegularExpressions(regexCheckBox.isSelected());      //正则表达式

                                EditorSearchSession searchSession = EditorSearchSession.start(editor, findModel, project);
                                searchSession.searchForward(); // 向前搜索
                            }
                        }
                    }
                }
            }
        });

    }

    private void runSearch() {
        if (currentIndicator != null) {
            currentIndicator.cancel();
            currentIndicator = null;
        }

        isRunning = !isRunning;

        if (isRunning) {
            searchButton.setText("Stop");
        } else {
            searchButton.setText("Search");
            return;
        }

        String query = searchField.getText();
        if (query.isEmpty()) {
            searchResult.clear();
        } else {
            performSearch(query);
        }
    }

    private void performSearch(String query) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Searching Jar File", true) {
            public void run(@NotNull ProgressIndicator indicator) {
                currentIndicator = indicator;
                searchResult.clear();

                try {
                    isRunning = true;

                    ProjectDependency.getDependentJar(project)
                            .parallelStream()
                            .forEach(eachJar -> {
                                VirtualFile sourceJar = SourceJarResolver.findSourceJar(eachJar);
                                if (null != sourceJar) {
                                    eachJar = sourceJar;
                                }
                                VfsUtilCore.visitChildrenRecursively(eachJar, new VirtualFileVisitor<Void>() {
                                    @Override
                                    public boolean visitFile(@NotNull VirtualFile file) {
                                        if (indicator.isCanceled()) {
                                            return false;
                                        }

                                        if (file.isValid() && !file.isDirectory()) {
                                            ApplicationManager.getApplication().runReadAction(() -> {
                                                if (indicator.isCanceled()) {
                                                    return;
                                                }
                                                String allText = MyJarEditor.getDecompiledText(project, file);
                                                boolean caseSensitive = caseSensitiveCheckBox.isSelected();
                                                boolean wholeWords = wholeWordsCheckBox.isSelected();
                                                boolean regex = regexCheckBox.isSelected();

                                                if (regex) {
                                                    try {
                                                        Pattern pattern = Pattern.compile(query, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
                                                        if (pattern.matcher(allText).find()) {
                                                            addResult(file);
                                                        }
                                                    } catch (PatternSyntaxException e) {
                                                        // 正则表达式无效，忽略
                                                    }
                                                } else if (wholeWords) {
                                                    String wordBoundary = "\\b" + Pattern.quote(query) + "\\b";
                                                    Pattern pattern = Pattern.compile(wordBoundary, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
                                                    if (pattern.matcher(allText).find()) {
                                                        addResult(file);
                                                    }
                                                } else {
                                                    if (caseSensitive ? allText.contains(query) : allText.toLowerCase().contains(query.toLowerCase())) {
                                                        addResult(file);
                                                    }
                                                }
                                            });
                                        }

                                        return true;
                                    }
                                });
                            });

                } finally {
                    isRunning = false;
                    searchButton.setText("Search");
                }

            }

            private void addResult(VirtualFile file) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (null == currentIndicator ||currentIndicator.isCanceled()) {
                        return;
                    }
                    searchResult.addElement(new SearchResultItem(file, MyPathUtil.getEntryPathFromJar(file.getPath())));
                });
            }
        });
    }

    public void exit() {
        if (currentIndicator != null) {
            currentIndicator.cancel();
            currentIndicator = null;
        }
    }

}
