# easywx
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.4coder/easywx/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.4coder/easywx/)
[![GitHub release](https://img.shields.io/github/release/yydf/easywx.svg)](https://github.com/yydf/easywx/releases)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://raw.githubusercontent.com/yydf/easywx/master/LICENSE)
![Jar Size](https://img.shields.io/badge/jar--size-23.37k-blue.svg)

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
//注册公众号
WXApi.forMP(appId, appSecret);
System.out.println(WXApi.mp().getAccessToken());

//注册公众号支付
WXApi.mp().forPayment(mchId, apiKey, callbackUrl);
//生成预付单
System.out.println(WXApi.mpPay().createUnifiedOrder(UnifiedOrder.test()));
```
