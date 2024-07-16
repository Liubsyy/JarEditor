package com.liubs.jareditor.sdk;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Liubsyy
 * @date 2024/5/9
 */
public class ProjectDependency {

    public static List<VirtualFile> getDependentLib(Project project) {

        List<VirtualFile> virtualFiles = new ArrayList<>();
        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {
            // 获取模块的根模型
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

            // 获取模块的条目（类路径、库依赖等）
            for (OrderEntry orderEntry : moduleRootManager.getOrderEntries()) {
                if (orderEntry instanceof LibraryOrderEntry) {
                    LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) orderEntry;
                    Library library = libraryOrderEntry.getLibrary();
                    if (library != null) {
                        virtualFiles.addAll(Arrays.asList(library.getFiles(OrderRootType.CLASSES)));
                    }
                } else if (orderEntry instanceof ModuleSourceOrderEntry) {
                    virtualFiles.addAll(Arrays.asList(moduleRootManager.getSourceRoots()));
                } else if (orderEntry instanceof JdkOrderEntry) {
                    // 获取 JDK 的类路径依赖
                    Sdk sdk = ((JdkOrderEntry) orderEntry).getJdk();
                    if (sdk != null) {
                        virtualFiles.addAll(Arrays.asList(sdk.getRootProvider().getFiles(OrderRootType.CLASSES)));
                    }
                }
            }
        }

        return virtualFiles;
    }

    public static List<VirtualFile> getDependentJar(Project project) {

        List<VirtualFile> virtualFiles = new ArrayList<>();
        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {
            // 获取模块的根模型
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

            // 获取模块的条目（类路径、库依赖等）
            for (OrderEntry orderEntry : moduleRootManager.getOrderEntries()) {
                if (orderEntry instanceof LibraryOrderEntry) {
                    LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) orderEntry;
                    Library library = libraryOrderEntry.getLibrary();
                    if (library != null) {
                        VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
                        for(VirtualFile file : files) {
                            if("jar".equals(file.getExtension())){
                                virtualFiles.add(file);
                            }
                        }
                    }
                }
            }
        }
        return virtualFiles;
    }

}
