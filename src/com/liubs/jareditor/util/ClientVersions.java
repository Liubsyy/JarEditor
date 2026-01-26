package com.liubs.jareditor.util;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

/**
 * @author Liubsyy
 * @date 2025-12-9
 */
public class ClientVersions {

    /**
     * 插件版本
     * @return
     */
    public static String getCurrentPluginVersion() {
        try{
            PluginId pluginId = PluginId.getId("com.liubs.jaredit");
            IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(pluginId);
            if (plugin != null) {
                return plugin.getVersion();
            }
        }catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }
}
