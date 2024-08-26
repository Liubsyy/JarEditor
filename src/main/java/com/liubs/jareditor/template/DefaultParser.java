package com.liubs.jareditor.template;

/**
 * @author Liubsyy
 * @date 2024/5/15
 */
public class DefaultParser implements ITextParser{
    public static DefaultParser INSTANCE = new DefaultParser();
    @Override
    public String[] parseParams(String filePath) {
        return new String[]{filePath};
    }
}
