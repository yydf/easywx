# easywx
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.4coder/easywx/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.4coder/easywx/)
[![GitHub release](https://img.shields.io/github/release/yydf/easywx.svg)](https://github.com/yydf/easywx/releases)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://raw.githubusercontent.com/yydf/easywx/master/LICENSE)
![Jar Size](https://img.shields.io/badge/jar--size-31.8k-blue.svg)

环境
-------------
- JDK 7

如何使用
-----------------------

* 添加dependency到POM文件::

```
<dependency>
    <groupId>cn.4coder</groupId>
    <artifactId>easywx</artifactId>
    <version>0.0.3</version>
</dependency>
```
* 如果用到微信的退款功能，需添加bcprov进行解密
```
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk16</artifactId>
    <version>1.46</version>
</dependency>
```

* 编码:

```
//注册公众号
WXApi.forMP(appId, appSecret);
System.out.println(WXApi.mp().getAccessToken());
System.out.println(WXApi.mp().createQrcode("test"));

//注册公众号支付
WXApi.mp().forPayment(mchId, apiKey, callbackUrl);
//生成预付单
System.out.println(WXApi.mpPay().createUnifiedOrder(UnifiedOrder.test()));

//获取支付的通知
@Request(value = "/callback", method = HttpMethod.POST)
public String callback() {
	try {
		//如果有退款通知，则添加
		Security.addProvider(new BouncyCastleProvider());
		PayResult result = WXApi.appPay().callback(request.getReader());
		if (result != null) {
			//service.updateStatus(result);
			return PayResult.SUCCESS;
		}
	} catch (IOException e) {
		logger.error("Callback faild", e);
	}
	return PayResult.FAIL;
}
```
