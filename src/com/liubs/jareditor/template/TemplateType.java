package com.liubs.jareditor.template;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Liubsyy
 * @date 2024/5/14
 */
public enum TemplateType {
   JAVA_TEMPLATE("java","template/java.template",new JavaTextParser()),
   CLASS_TEMPLATE("class","template/java.template",new JavaTextParser()),
   XML_TEMPLATE("xml","template/xml.template", DefaultParser.INSTANCE),



    ;

    private String fileExtension;
    private String templateFile;
    private ITextParser textParser;

    private static final Map<String,TemplateType> templateTypeMap = new HashMap<>();
    static {
        for(TemplateType e : values()) {
            templateTypeMap.put(e.fileExtension,e);
        }
    }

    TemplateType(String fileExtension, String templateFile,ITextParser textParser) {
        this.fileExtension = fileExtension;
        this.templateFile = templateFile;
        this.textParser = textParser;
    }

    public static TemplateType getTemplateType(String fileExtension) {
        return templateTypeMap.get(fileExtension);
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public ITextParser getTextParser() {
        return textParser;
    }
}
