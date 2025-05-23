package com.liubs.jareditor.backup;

import java.util.List;

/**
 * @author Liubsyy
 * @date 2025/5/23
 */
public class ChangeData {
    private String createTime;
    private List<ChangeItem> changeList;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<ChangeItem> getChangeList() {
        return changeList;
    }

    public void setChangeList(List<ChangeItem> changeList) {
        this.changeList = changeList;
    }
}
