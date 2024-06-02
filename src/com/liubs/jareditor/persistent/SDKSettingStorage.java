package com.liubs.jareditor.persistent;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.liubs.jareditor.sdk.SDKManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/6/2
 */
@State(
        name = "JarEditorSDKSettings",
        storages = @Storage("JarEditorSDKSettings.xml")
)
public class SDKSettingStorage implements PersistentStateComponent<SDKSettingStorage> {
    private List<MyItem> mySdks = new ArrayList<>();

    @Nullable
    @Override
    public SDKSettingStorage getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SDKSettingStorage state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static SDKSettingStorage getInstance() {
        return ApplicationManager.getApplication().getService(SDKSettingStorage.class);
    }


    public List<MyItem> getMySdks() {
        if(mySdks.isEmpty()) {
            List<MyItem> defaultSDKs = new ArrayList<>();
            for(SDKManager.JDKItem jdkItem : SDKManager.getAllJDKs()){
                MyItem myItem =  new MyItem();
                myItem.setName(jdkItem.name);
                myItem.setPath(jdkItem.javaHome);
                defaultSDKs.add(myItem);
            }
            return defaultSDKs;
        }
        return mySdks;
    }

    public void setMySdks(List<MyItem> mySdks) {
        this.mySdks = mySdks;
    }

    @Tag("item")
    public static class MyItem {
        private String name;
        private String path;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}