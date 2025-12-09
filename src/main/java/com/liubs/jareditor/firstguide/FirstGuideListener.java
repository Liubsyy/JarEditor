package com.liubs.jareditor.firstguide;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import org.jetbrains.annotations.NotNull;

/**
 * @author Liubsyy
 * @date 2025/12/9
 */
public class FirstGuideListener implements PluginStateListener {
    @Override
    public void install(@NotNull IdeaPluginDescriptor ideaPluginDescriptor) {

        //安装完插件后弹出引导
        try{
            if(ideaPluginDescriptor.getPluginId().getIdString().equals("com.liubs.jaredit")) {
                FirstGuideStartup.run();
            }
        }catch (Throwable e){
            e.printStackTrace();
        }

    }

    @Override
    public void uninstall(@NotNull IdeaPluginDescriptor ideaPluginDescriptor) {

    }
}
