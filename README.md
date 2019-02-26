# easywx

Summary
-------
A Simple Wechat Toolkit for Java.

Prerequisites
-------------
- JDK 7

Switching to easywx
-----------------------

* Add dependency to project in POM:

```
<dependency>
    <groupId>com.github.yydf</groupId>
    <artifactId>easywx</artifactId>
    <version>0.0.1</version>
</dependency>
```

* Coding:

```
WXApi.forMP(appId, appSecret);
System.out.println(WXApi.mp().getAccessToken());
```
