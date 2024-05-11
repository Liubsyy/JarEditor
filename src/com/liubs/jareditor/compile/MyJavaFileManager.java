package com.liubs.jareditor.compile;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/5/9
 */
class MyJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    private final String outputDirectory;
    private final List<String> compiledFiles;

    protected MyJavaFileManager(StandardJavaFileManager fileManager, String outputDirectory) {
        super(fileManager);
        this.outputDirectory = outputDirectory;
        this.compiledFiles = new ArrayList<>();
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        Path outputDir = Paths.get(outputDirectory);
        if (Files.notExists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        String relativePath = className.replace('.', File.separatorChar) + kind.extension;
        Path outputPath = outputDir.resolve(relativePath);
        Files.createDirectories(outputPath.getParent());

        compiledFiles.add(outputPath.toString());
        return new SimpleJavaFileObject(outputPath.toUri(), kind) {
            @Override
            public OutputStream openOutputStream() throws IOException {
                return Files.newOutputStream(outputPath);
            }
        };
    }

    public List<String> getCompiledFiles() {
        return compiledFiles;
    }

}
