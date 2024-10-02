package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Liubsyy
 * @date 2024/6/26
 */
public interface MyIcons {
    Icon KOTLIN_CLASS = IconLoader.getIcon("/icons/classKotlin.svg", MyIcons.class);
    Icon CLEAN = IconLoader.getIcon("/icons/clean.svg", MyIcons.class);
    Icon RESET = IconLoader.getIcon("/icons/reset.svg", MyIcons.class);
    Icon TOOL = IconLoader.getIcon("/icons/tool.svg", MyIcons.class);
}
