package decompiler;

import com.liubs.jareditor.util.JarUtil;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.plugins.input.java.JavaInputPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * @author Liubsyy
 * @date 2026/2/13
 */
public class JadxDecompilerTest {
    public static String decompileClassFromJar(Path jarPath, String className) {
        File jarFile = jarPath.toFile();
        if (!jarFile.isFile() || !jarFile.getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Not a jar file: " + jarFile);
        }
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("className is blank");
        }

        JadxArgs args = new JadxArgs();
        args.setInputFile(jarFile);

        // 不落盘：不要调用 save()；outDir 可不设（有些版本内部可能仍会用到，设个临时目录也行）
        // args.setOutDir(new File(System.getProperty("java.io.tmpdir"), "jadx-out"));
        try (JadxDecompiler jadx = new JadxDecompiler(args)) {
            jadx.load();

            List<JavaClass> classes = jadx.getClasses();

            // 1) 优先按 fullName 精确匹配（通常就是你传的 "com.example.Foo"）
            Optional<JavaClass> hit = classes.stream()
                    .filter(c -> className.equals(c.getFullName()))
                    .findFirst();

            // 2) 有些情况下 fullName 可能不完全一致，再尝试“后缀匹配”
            if (hit.isEmpty()) {
                String suffix = "." + className;
                hit = classes.stream()
                        .filter(c -> {
                            String fn = c.getFullName();
                            return fn != null && (fn.equals(className) || fn.endsWith(suffix));
                        })
                        .findFirst();
            }

            JavaClass jc = hit.orElseThrow(() ->
                    new IllegalArgumentException("Class not found in jar: " + className));

            //直接拿源码字符串（不写文件）
            String code = jc.getCode();
            return code != null ? code : "";
        }
    }


    // demo
    public static void main(String[] args) {
        String src = decompileClassFromJar(Path.of("/Users/liubs/IdeaProjects/TestJarLib/lib/Test.jar"), "com.liubs.TestClass");
        System.out.println(src);
    }
}
