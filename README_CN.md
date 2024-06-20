
<div align="center">
  <img align="center" src="./img/logo.png" width="100" height="100" />
</div>

<h2 align="center">JarEditor <sup><em>liubsyy</em></sup></h2>

<h4 align="center"><a href="./README.md">English</a> | <strong>简体中文</strong></h4>

[![License](https://img.shields.io/github/license/Liubsyy/JarEditor?color=blue)](./LICENSE)
[![downloads](https://img.shields.io/jetbrains/plugin/d/24397)](https://plugins.jetbrains.com/plugin/24397-jareditor)
[![release](https://img.shields.io/jetbrains/plugin/v/24397?label=version)](https://plugins.jetbrains.com/plugin/24397-jareditor)
![sdk](https://img.shields.io/badge/plugin%20sdk-IDEA%202020.3-red.svg)

无需解压直接编辑修改jar包内文件(class和resource)的IDEA插件

**Plugin marketplace** : [https://plugins.jetbrains.com/plugin/24397-jareditor](https://plugins.jetbrains.com/plugin/24397-jareditor)

## 特征
- 直接编辑jar包内class/resource文件，无需解压
- 添加/删除/重命名jar包内文件/文件夹
- 从剪贴板粘贴文件/文件夹
- 将文件/文件夹复制到剪贴板
- 支持springboot fatjar
- 支持kotlin

## 快速开始

### 1. 从插件市场安装插件
首先从市场安装插件 JarEditor，IDEA版本 >= **2020.3**

<img src="./img/JarEditor_install.png" width="800" height="606" />


### 2. 编辑并保存 Jar
安装完成后，在.class反编译文件中可以看到切换到Jar Editor的tab页。

`外部jar：File->Project Structure->Libraries->Add Library，然后就可以看到反编译的jar了。`

<img src="./img/JarEditor_main.png" width="800" height="506" />

修改完成后，点击**保存（编译）**，编译并保存当前修改的java内容。

最后点击**Build Jar**，将编译保存的类文件写入Jar包中。

修改jar包中的资源文件也是支持的。

### 3.JarEditor的其他操作
在jar包的项目视图中，右键可以看到**JarEditor->New/Delete**等操作，可以在jar内添加/删除/重命名/复制/粘贴文件。

<img src="./img/JarEditor_new_delete.png" width="550" height="454" />


## 一些机制
- 编译依赖的JDK是你的SDK列表中的JDK。您可以选择SDK和编译类的目标版本。
- 编译java时所依赖的classpath就是项目的依赖。如果找不到依赖包，可以添加依赖。
- Save/Compile会将修改后的文件保存到jar包所在目录的子目录**jar_edit_out**中。 Build Jar会将修改的文件增量写入jar中，最后删除这个目录。