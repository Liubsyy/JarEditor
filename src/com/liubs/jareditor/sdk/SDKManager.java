package com.liubs.jareditor.sdk;

import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/5/17
 */
public class SDKManager {

    public static class JDKItem{
        public String name;
        public String javaHome;
    }

    public static List<JDKItem> getAllJDKs(){
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        List<JDKItem> jdkItems = new ArrayList<>();
        for(Sdk sdk : allJdks) {
            JDKItem item = new JDKItem();
            item.name = sdk.getName();
            item.javaHome = sdk.getHomePath();
            jdkItems.add(item);
        }
        return jdkItems;
    }
}
