package com.liubs.jareditor.firstguide;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import com.intellij.openapi.extensions.PluginId;
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
            PluginId pluginId = ideaPluginDescriptor.getPluginId();
            if(null != pluginId) {
                String idString = pluginId.getIdString();
                if("com.liubs.jaredit".equals(idString)) {
                    FirstGuideStartup.run();
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }

    }

    @Override
    public void uninstall(@NotNull IdeaPluginDescriptor ideaPluginDescriptor) {
    }
}
