package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Liubsyy
 * @date 2024/6/26
 */
public interface MyIcons {
    Icon CLEAN = IconLoader.getIcon("/icons/clean.svg", MyIcons.class);
    Icon RESET = IconLoader.getIcon("/icons/reset.svg", MyIcons.class);
    Icon BYTES_TOOL = IconLoader.getIcon("/icons/T.png", MyIcons.class);
    Icon HAMMER = IconLoader.getIcon("/icons/hammer.png", MyIcons.class);
}
