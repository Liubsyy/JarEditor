package com.liubs.jareditor.persistent;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Liubsyy
 * @date 2025/12/9
 */
@State(
        name = "JarEditorGuideStorage",
        storages = @Storage("JarEditorGuideStorage.xml")
)
public class GuideStorage implements PersistentStateComponent<GuideStorage> {

    private boolean showed;
    private String version;
    private int count = 0;

    @Nullable
    @Override
    public GuideStorage getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GuideStorage state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static GuideStorage getInstance() {
        return ApplicationManager.getApplication().getService(GuideStorage.class);
    }

    public boolean isShowed() {
        return showed;
    }

    public void setShowed(boolean showed) {
        this.showed = showed;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
