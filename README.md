# easywx

环境
-------------
- JDK 7
- Servlet 3.1

如何使用
-----------------------

* 添加dependency到POM文件::

```
<dependency>
    <groupId>cn.4coder</groupId>
    <artifactId>easywx</artifactId>
    <version>0.0.1</version>
</dependency>
```

* 编码:

```
WXApi.forMP(appId, appSecret);
System.out.println(WXApi.mp().getAccessToken());
```
