package com.liubs.jareditor.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Liubsyy
 * @date 2024/5/14
 */
public class TemplateManager {

    private static final Map<TemplateType,String> templateMap = new HashMap<>();

    static {
        try{
            initTemplates();
        }catch (Throwable e){}
    }

    public static void initTemplates() {
        ClassLoader classLoader = TemplateType.class.getClassLoader();

        for(TemplateType templateType : TemplateType.values()) {
            try (InputStream inputStream = classLoader.getResourceAsStream(templateType.getTemplateFile())) {
                templateMap.put(templateType,new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getText(String fileExtension,String filePath) {
        if(null == fileExtension || null == filePath) {
            return "";
        }
        TemplateType templateType = TemplateType.getTemplateType(fileExtension);
        if(null != templateType){
            String text = templateMap.get(templateType);
            String []params = templateType.getTextParser().parseParams(filePath);
            for(int i = 0;i<params.length;i++) {
                text = text.replace("$"+ (i + 1) +"$", params[i]);
            }
            return text;
        }
        return "";
    }

    public static boolean isAddContentWhenCreate(String fileExtension) {
        if(null == fileExtension){
            return false;
        }
        TemplateType templateType = TemplateType.getTemplateType(fileExtension);
        return null != templateType && templateType.isAddContentWhenCreate();
    }

}
