package com.liubs.jareditor.persistent;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.liubs.jareditor.constant.PathConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Liubsyy
 * @date 2025/5/22
 */
@State(
        name = "JarEditorBackupStorage",
        storages = @Storage("JarEditorBackupStorage.xml")
)
public class BackupStorage implements PersistentStateComponent<BackupStorage> {
    private boolean enableBackup;
    private String backupPath;

    @Nullable
    @Override
    public BackupStorage getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull BackupStorage state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static BackupStorage getInstance() {
        return ApplicationManager.getApplication().getService(BackupStorage.class);
    }

    public boolean isEnableBackup() {
        return enableBackup;
    }

    public void setEnableBackup(boolean enableBackup) {
        this.enableBackup = enableBackup;
    }

    public String getBackupPath() {
        if(null == backupPath || backupPath.isEmpty()) {
            String home = System.getProperty("user.home");
            backupPath = home + PathConstant.DEFAULT_BACKUP_PATH;
        }
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }
}
