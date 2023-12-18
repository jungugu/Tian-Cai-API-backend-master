# Tian Cai API 开发者文档

> 只要你阅读了这篇文档，你将会快速掌握，api的调用
>
> 作者：糯米

## 简介

Tian Cai API开放者文档是Tian Cai API接口开放平台是一个接口调用工具包，通过Tian Cai SDK即可轻松的将接口集成到您的项目中，实现更高效的开发和调用。

> 注意🔔️
>
> 1. 拥有Java开发环境
> 2. 熟悉Spring Boot
> 3. 熟悉 Maven

### 官网地址

Tian Cai API接口开放平台

### 我们的优势

1. **去客户端SDK支持：** 为了方便开发者集成接口到自己的代码中，平台提供了客户端SDK，使调用接口变得更加简单和便捷。
2. **简洁高效：** 稳定、安全、高效的接口调用服务，帮助您实现更快速、便捷的开发和调用体验。
3. **开发者文档和技术支持：** 平台提供了详细的开发者文档和技术支持，帮助开发者快速接入和发布接口，解决遇到的问题和困难。
4. **多样化的接口选择：** 丰富多样的接口供您选择，涵盖了各个领域的功能和服务，满足不同的需求。
5. **在线调试功能：** 您可以在平台上进行接口在线调试，快速验证接口的功能和效果，节省了开发调试的时间和工作量

## 安装

### Maven

在项目的pom.xml的dependencies中加入以下内容

```Maven
<dependency>
    <groupId>icu.qimuu</groupId>
    <artifactId>qi-api-sdk</artifactId>
    <version>0.0.4</version>
</dependency>
```

## 快速开始

### 安装

在项目中pom.xml的dependencies中加入一下内容

```
<dependency>
    <groupId>icu.qimuu</groupId>
    <artifactId>qi-api-sdk</artifactId>
    <version>0.0.4</version>
</dependency>
```

### 配置客户端

#### 1. 获取并配置开发者密钥

- 在个人信息中可以获取到开发者密钥

![image-20231214091643829](https://mi.nuomi.wiki/nuomituchuang/202312140916038.png)

#### 2.初始化客户端对象

- 通过配置文件注入对象

  - yml

    ```
    tiancaiapi:
      client:
        access-key: 你的accessKey
        secret-key: 你的secretKey
    ```

- 通过自动装配获取到客户端使用api服务

  ```
  @Resource
  private ApiService apiService;
  ```

#### 3.发起请求

- 调用apiService，每个接口都有对应的方法，只需要调用方法就能轻松的调用对应的接口

```java
try {
    PoisonousChickenSoupResponse poisonousChickenSoup = apiService.getPoisonousChickenSoup();
    System.out.println("poisonousChickenSoup = " + poisonousChickenSoup);
} catch (ApiException e) {
  log.error(e.getMessage());
}
```

