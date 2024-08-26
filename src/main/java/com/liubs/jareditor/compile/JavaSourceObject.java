package com.liubs.jareditor.compile;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * @author Liubsyy
 * @date 2024/5/9
 */
class JavaSourceObject extends SimpleJavaFileObject {
    private final String sourceCode;

    protected JavaSourceObject(String className, String sourceCode) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = sourceCode;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return sourceCode;
    }
}