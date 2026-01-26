package com.liubs.jareditor.firstguide;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Project级别的生命周期，每个窗口都会触发
 */
public class MyStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        try{
            FirstGuideStartup.showGuideOnce(false);
        }catch (Throwable e) {
            e.printStackTrace();
        }

    }
}
