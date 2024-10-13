package com.liubs.jareditor.jarbuild;

import java.nio.file.Path;
import java.util.jar.JarOutputStream;

/**
 * @author Liubsyy
 * @date 2024/10/13
 */
public interface ChangedItemCallBack {
    void writeStream(Path jarEditOutDir, JarOutputStream tempJarOutputStream);
}
