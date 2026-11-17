package com.liubs.jareditor.firstguide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Project级别的生命周期，每个窗口都会触发
 */
public class MyStartupActivity implements StartupActivity, DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        // 引导不依赖索引；DumbAware 启动活动可能在后台线程执行。
        // 将引导状态更新与窗口操作统一调度到 UI 线程。
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }
            try {
                FirstGuideStartup.showGuideOnce(false);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }
}
