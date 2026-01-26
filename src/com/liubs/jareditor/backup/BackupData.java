package com.liubs.jareditor.backup;

import java.util.Date;

/**
 * @author Liubsyy
 * @date 2025/5/23
 */
public class BackupData {
    private Date createTime;
    private String backupJar;
    private ChangeData changeData;

    public String getBackupJar() {
        return backupJar;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setBackupJar(String backupJar) {
        this.backupJar = backupJar;
    }

    public ChangeData getChangeData() {
        return changeData;
    }

    public void setChangeData(ChangeData changeData) {
        this.changeData = changeData;
    }
}
