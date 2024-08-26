package com.liubs.jareditor.compile;

import com.liubs.jareditor.editor.LanguageType;

/**
 * SDK Default编译kotlin时，使用/plugins/Kotlin/kotlinc进行编译
 * @author Liubsyy
 * @date 2024/6/25
 */
public class MySDKDefaultKotlincCompiler extends MyKotlincCompiler{
    public MySDKDefaultKotlincCompiler(String javaHome) {
        super(javaHome + LanguageType.KOTLINC_SDK_DEFAULT.getSubCommandHome());
    }
}
