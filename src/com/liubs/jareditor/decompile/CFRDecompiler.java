package com.liubs.jareditor.decompile;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.liubs.jareditor.constant.PathConstant;
import com.liubs.jareditor.sdk.ProjectDependency;
import com.liubs.jareditor.util.JavaFileUtil;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.util.getopt.OptionsImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CFR反编译器
 * @author Liubsyy
 * @date 2024/10/8
 */
public class CFRDecompiler implements IDecompiler{

    public String decompile(Project project,Map<String,byte[]> classBytesMap) {
        final StringBuilder sb = new StringBuilder();

        OutputSinkFactory mySink = new OutputSinkFactory() {
            @Override
            public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> collection) {
                return Arrays.asList(SinkClass.STRING, SinkClass.DECOMPILED,
                        SinkClass.DECOMPILED_MULTIVER,SinkClass.EXCEPTION_MESSAGE);
            }

            @Override
            public <T> Sink<T> getSink(final SinkType sinkType, final SinkClass sinkClass) {
                return sinkable -> {
                    if (sinkType != SinkType.PROGRESS) {
                        sb.append(sinkable);
                    }
                };
            }
        };

        HashMap<String, String> options = new HashMap<>();
        options.put("hideutf", "false");
        options.put("trackbytecodeloc", "true");
        options.put(OptionsImpl.EXTRA_CLASS_PATH.getName(),  ProjectDependency.getDependentLib(project).stream()
                .map(c-> PathUtil.getLocalPath(c.getPath()))
                .collect(Collectors.joining(String.valueOf(File.pathSeparatorChar))));

        CfrDriver driver = new CfrDriver.Builder()
                .withOptions(options)
                .withClassFileSource(new ClassFileSourceImpl(OptionsImpl.getFactory().create(options)){
                    @Override
                    public Pair<byte[], String> getClassFileContent(String classPath) throws IOException {
                        if(classBytesMap.containsKey(classPath)) {
                            return new Pair<>(classBytesMap.get(classPath),classPath);
                        }
                        return super.getClassFileContent(classPath);
                    }
                })
                .withOutputSink(mySink)
                .build();

        List<String> toAnalyse = new ArrayList<>(classBytesMap.keySet());
        driver.analyse(toAnalyse);

        return sb.toString();
    }

    @Override
    public String decompile(Project project, VirtualFile virtualFile) {
        try {
            Map<String,byte[]> classBytesMap = new HashMap<>();
            if(virtualFile.getPath().contains("jar!/")) {
                String path = virtualFile.getPath().split("jar!/")[1];
                classBytesMap.put(path, VfsUtilCore.loadBytes(virtualFile));
            }else {
                List<String> fullClassFiles = JavaFileUtil.getFullClassFiles(virtualFile.getPath());
                for(String path : fullClassFiles) {
                    byte[] bytes = Files.readAllBytes(Paths.get(path));
                    path = path.split(PathConstant.TEMP_SUFFIX+"/"+ PathConstant.JAR_EDIT_CLASS_PATH+"/")[1];
                    classBytesMap.put(path,bytes);
                }
            }
            return decompile(project,classBytesMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
