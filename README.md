# JarEditor

<img src="./img/logo.png" width="80" height="80" />

JarEditor is an IDEA plugin that can modify the files in JAR directly without decompression, including class and resource files.

**Plugin marketplace** : [https://plugins.jetbrains.com/plugin/24397-jareditor](https://plugins.jetbrains.com/plugin/24397-jareditor)

## Features
- Edit class/resource files in JAR directly without decompression
- Add new file in JAR
- Delete file/dictionary in JAR

## Quick start

### 1. Insatall plugin from marketplace
**IDEA at least version 2020.3** , first install the plugin JarEditor from marketplace, the classes in the jar can be opened directly to see the decompiled code. External files can add dependencies through File->Project Structure->Libraries and then decompile the jar.

### 2. Edit and Save Jar
After installing the plugin JarEditor from marketplace, you can see a tab page to switch to Jar Editor in the .class decompiled file.

<img src="./img/JarEditor_whole.png" width="720" height="460" />

After clicking Jar Editor, you can directly modify the decompiled code. After modification, click **Save/Compile** to compile and save the currently modified java content. Then click **Build Jar** to write the compiled and saved class file into the Jar package.

Modifying the resource files in the jar package is also supported. The process is the same as the class file. After modification, you need to save it and then Build Jar.


### 3. Other operations of JarEditor
In the project view of the jar package, right-click to see JarEditor->Add/Delete and other operations, where you can add and delete files.

<img src="./img/JarEditor_add_delete.png" width="480" height="470" />


## Some mechanisms
- The JDK that the compilation depends on is the JDK of the project project. You can choose the target version of the compiled class. By default, it is consistent with the class version of the jar package.
- The classpath you depend on when compiling java is the dependency of the project. If the dependency package cannot be found, you can add the dependency.
- Save/Compile will save the modified files to the jar_edit subdirectory of the directory where the jar package is located. Build Jar will incrementally write the files in the jar_edit directory to the jar, and finally delete this directory.

