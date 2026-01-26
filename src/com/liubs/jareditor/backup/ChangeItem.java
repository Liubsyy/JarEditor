package com.liubs.jareditor.backup;

/**
 * @author Liubsyy
 * @date 2025/5/23
 */
public class ChangeItem {
    private int changeType;
    private String entry;

    public ChangeItem() {
    }

    public ChangeItem(int changeType, String entry) {
        this.changeType = changeType;
        this.entry = entry;
    }

    public int getChangeType() {
        return changeType;
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

}
