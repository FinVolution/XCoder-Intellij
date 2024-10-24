# XCoder jetbrains插件

## 插件端安装
可以使用已打包好的插件包直接安装
具体目录在/src/main/resources/plugin/Open-XCoder-1.0.0.zip
支持idea版本为2022.2-2024.1

如果需要自己编译打包，请参考以下文档中的插件编译打包

## 插件使用
安装插件后,点击右下角的`XCoder`图标`打开配置页`,将`服务地址`换为部署的后端服务地址即可

## 插件编译打包
编译环境需要安装jdk17和gradle8.3

配置好环境变量后

在项目根目录执行以下命令
```bash
gradle buildPlugin
```

如果使用的是intellij idea编译打包，可以点击toolWindows栏中`Gradle`-`XCoder`-`Task`-`intellij`-`buildPlugin`

然后就可以在项目根目录下的`build/distributions`目录中找到插件安装包

##  XCoder后端服务部署

[`XCoder后端服务开源地址`](https://github.com/FinVolution/XCoder-Server)