package com.liubs.jareditor.template;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Liubsyy
 * @date 2024/5/14
 */
public enum TemplateType {
   JAVA_TEMPLATE("java","template/java.template",new JavaTextParser(),false),
   CLASS_TEMPLATE("class","template/java.template",new JavaTextParser(),false),
   XML_TEMPLATE("xml","template/xml.template", DefaultParser.INSTANCE,false),
   KOTLIN_TEMPLATE("kt","template/kotlin.template", new KotlinTextParser(),false),
   MANIFEST_TEMPLATE("MF","template/MANIFEST.template", DefaultParser.INSTANCE,true),



    ;


    private String fileExtension;
    private String templateFile;
    private ITextParser textParser;
    private boolean addContentWhenCreate;   //jar内新增文件时就写入文本模版

    private static final Map<String,TemplateType> templateTypeMap = new HashMap<>();
    static {
        for(TemplateType e : values()) {
            templateTypeMap.put(e.fileExtension,e);
        }
    }

    TemplateType(String fileExtension, String templateFile,ITextParser textParser,boolean addContentWhenCreate) {
        this.fileExtension = fileExtension;
        this.templateFile = templateFile;
        this.textParser = textParser;
        this.addContentWhenCreate = addContentWhenCreate;
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

    public boolean isAddContentWhenCreate() {
        return addContentWhenCreate;
    }
}
