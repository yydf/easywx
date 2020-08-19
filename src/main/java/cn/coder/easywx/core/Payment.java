package cn.coder.easywx.core;

import java.io.BufferedReader;
import java.security.Provider;
import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.mapper.PayResult;
import cn.coder.easywx.mapper.Redpack;
import cn.coder.easywx.mapper.RedpackStatus;
import cn.coder.easywx.mapper.RefundOrder;
import cn.coder.easywx.mapper.Transfer;
import cn.coder.easywx.mapper.UnifiedOrder;
import cn.coder.easywx.util.SignUtils;
import cn.coder.easywx.util.XMLUtils;

public final class Payment extends Base {
	private static final Logger logger = LoggerFactory.getLogger(Payment.class);
	private static final String URL_CREATE_UNIFIEDORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	private static final String URL_REFUNDORDER = "https://api.mch.weixin.qq.com/secapi/pay/refund";
	private static final String URL_SEND_REDPACK = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
	private static final String URL_REDPACK_STATUS = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gethbinfo";
	private static final String URL_TRANSFERS = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
	private final String apiKey;
	private final String mchId;
	private final String notifyUrl;
	private final String appId;
	private SSLSocketFactory ssl;
	private Provider securityProvider;

	public Payment(String appId, String mchId, String apiKey, String callbackUrl) {
		this.appId = appId;
		this.mchId = mchId;
		this.apiKey = apiKey;
		this.notifyUrl = callbackUrl;
	}

	/**
	 * 设置加密对象</br>
	 * <code>public static SSLSocketFactory getSSLSocketFactory(String p12, String p12Pass) {</br>
		try {</br>
			KeyStore ks = KeyStore.getInstance("PKCS12");</br>
			char[] password = p12Pass.toCharArray();</br>
			InputStream inputStream = WebClient.class.getClassLoader().getResourceAsStream(p12);</br>
			ks.load(inputStream, password);</br>
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());</br>
			kmf.init(ks, password);</br>
			SSLContext ssl = SSLContext.getInstance("TLS");</br>
			ssl.init(kmf.getKeyManagers(), null, null);</br>
			return ssl.getSocketFactory();</br>
		} catch (Exception e) {</br>
			logger.error("加载证书失败" + p12, e);</br>
			return null;</br>
		}</br>
	}</code>
	 * 
	 * @param ssl
	 * @return
	 */
	public Payment setSSLSocketFactory(SSLSocketFactory ssl) {
		this.ssl = ssl;
		return this;
	}

	public void setSecurityProvider(Provider provider) {
		// 添加BouncyCastleProvider，用于解密
		Security.addProvider(provider);
		this.securityProvider = provider;
	}

	public PayResult callback(BufferedReader reader) {
		String xml = XMLUtils.deserialize(reader);
		logger.debug("[Wechat]:" + xml);
		return callback(xml);
	}

	public PayResult callback(String xml) {
		try {

			HashMap<String, Object> result = XMLUtils.doXMLParse(xml);
			if (result == null)
				return null;
			// 退款通知
			if (result.containsKey("req_info")) {
				if (securityProvider == null)
					throw new NullPointerException("The BouncyCastleProvider can not be null");
				byte[] data = Base64.getDecoder().decode(result.get("req_info").toString());
				byte[] key = SignUtils.encodeByMD5(this.apiKey).toLowerCase().getBytes("utf-8");
				String str = SignUtils.decryptData(data, key);
				logger.debug("[Wechat]" + str);
				if (str != null) {
					Map<String, Object> result2 = XMLUtils.doXMLParse(str);
					// 如果退款成功
					if ("SUCCESS".equals(getValue(result2, "refund_status"))) {
						PayResult msg = new PayResult(true);
						msg.mch_id = getValue(result, "mch_id");
						msg.appid = getValue(result, "appid");
						// 商户订单号
						msg.out_trade_no = getValue(result2, "out_trade_no");
						msg.out_refund_no = getValue(result2, "out_refund_no");
						msg.success_time = getValue(result2, "success_time");
						msg.refund_fee = getValue(result2, "refund_fee");
						return msg;
					}
				}
				return null;
			}
			String returnCode = getValue(result, "return_code");
			String resultCode = getValue(result, "result_code");
			if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
				// 签名验证
				String sign = result.remove("sign").toString();
				if (SignUtils.getSign(result, this.apiKey).equals(sign)) {
					PayResult msg = new PayResult(false);
					msg.appid = getValue(result, "appid");
					msg.openid = getValue(result, "openid");
					msg.mch_id = getValue(result, "mch_id");
					msg.bank_type = getValue(result, "bank_type");
					msg.trade_type = getValue(result, "trade_type");
					// 商户订单号
					msg.out_trade_no = getValue(result, "out_trade_no");
					msg.transaction_id = getValue(result, "transaction_id");
					msg.time_end = getValue(result, "time_end");
					msg.cash_fee = getValue(result, "cash_fee");
					msg.total_fee = getValue(result, "total_fee");
					return msg;
				}
				logger.debug("Check sign faild");
			}
		} catch (Exception e) {
			logger.error("Receive wechat callback faild", e);
		}
		return null;
	}

	/**
	 * 创建预付单
	 * 
	 * @param order
	 * @return 预付单所需参数
	 */
	public Map<String, Object> createUnifiedOrder(UnifiedOrder order) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("appid", this.appId);// 应用ID
		map.put("mch_id", this.mchId);// 商户号
		// map.put("device_info", "");//设备号
		map.put("nonce_str", getRandamStr());// 随机字符串
		if (order.body.length() > 64)
			map.put("body", order.body.substring(0, 64));// 商品描述
		else
			map.put("body", order.body);// 商品描述
		// map.put("detail", "");//商品详情
		// map.put("attach", "");//附加数据
		map.put("out_trade_no", order.out_trade_no);// 商户订单号
		// map.put("fee_type", "");//货币类型
		map.put("total_fee", order.total_fee);// 总金额(单位分)
		map.put("spbill_create_ip", order.spbill_create_ip);// 客户端IP
		// map.put("time_start", "");//交易起始时间
		// map.put("time_expire", "");//交易结束时间
		// map.put("goods_tag", "");//商品标记
		map.put("notify_url", this.notifyUrl);// 通知地址
		if (order.sub_mch_id != null)
			map.put("sub_mch_id", order.sub_mch_id);// 子商户订单号

		// 交易类型
		if (order.trade_type != null)
			map.put("trade_type", order.trade_type);
		else {
			if (order.openid == null)
				map.put("trade_type", "APP");
			else {
				map.put("trade_type", "JSAPI");
				map.put("openid", order.openid);
			}
		}

		map.put("sign", SignUtils.getSign(map, this.apiKey));// 签名
		// map.put("limit_pay", "");// 指定支付方式

		// post调取方法
		String return_xml = postString(URL_CREATE_UNIFIEDORDER, XMLUtils.toXML(map));
		logger.debug("[UnifiedOrder]" + return_xml);
		Map<String, Object> result = XMLUtils.doXMLParse(return_xml);
		String returnCode = getValue(result, "return_code");
		String resultCode = getValue(result, "result_code");
		if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
			HashMap<String, Object> maplast = new HashMap<>();
			String tradeType = getValue(result, "trade_type");
			String prepayid = getValue(result, "prepay_id");
			if ("NATIVE".equals(tradeType)) {
				maplast.put("appid", this.appId);
				maplast.put("partnerid", this.mchId);
				maplast.put("prepayid", prepayid);
				maplast.put("code_url", getValue(result, "code_url"));
				return maplast;
			}
			if (order.openid == null) {
				maplast.put("appid", this.appId);
				maplast.put("noncestr", getRandamStr());
				maplast.put("partnerid", this.mchId);
				maplast.put("prepayid", prepayid);
				maplast.put("timestamp", getTimestamp());
				maplast.put("package", "Sign=WXPay");
				maplast.put("sign", SignUtils.getSign(maplast, this.apiKey));
			} else {
				maplast.put("appId", this.appId);
				maplast.put("nonceStr", getRandamStr());
				maplast.put("package", "prepay_id=" + prepayid);
				maplast.put("timeStamp", getTimestamp());
				maplast.put("signType", "MD5");
				maplast.put("paySign", SignUtils.getSign(maplast, this.apiKey));
			}
			return maplast;
		}
		return null;
	}

	/**
	 * 退款申请</br>
	 * <code>final Transfer tf = new Transfer();</br>
			tf.amount = getMoney();</br>
			tf.desc = "红包提现";</br>
			tf.openid = openId;</br>
			tf.partner_trade_no = getNo();</br>
			tf.check_name = "NO_CHECK";</br>
			tf.spbill_create_ip = getRemoteAddr();</code>
	 * 
	 * @param order
	 *            退款条件
	 * @return 申请是否成功
	 */
	public boolean refundCash(RefundOrder order) {
		if (ssl == null)
			throw new NullPointerException("The ssl can not be null");
		try {
			HashMap<String, Object> map = new HashMap<>();
			map.put("appid", this.appId);
			map.put("mch_id", this.mchId);
			map.put("nonce_str", getRandamStr());
			map.put("out_trade_no", order.out_trade_no);
			map.put("out_refund_no", order.out_refund_no);
			map.put("total_fee", Integer.valueOf(order.total_fee));
			map.put("refund_fee", Integer.valueOf(order.refund_fee));
			map.put("notify_url", this.notifyUrl);

			map.put("sign", SignUtils.getSign(map, this.apiKey));

			return getWechatResult(URL_REFUNDORDER, this.ssl, map);
		} catch (Exception e) {
			logger.error("Refund cash faild", e);
			return false;
		}
	}

	/**
	 * 发送微信红包
	 * 
	 * @param redpack
	 *            发送红包参数
	 * @return 调用微信接口是否成功
	 */
	public boolean sendRedpack(Redpack redpack) {
		if (ssl == null)
			throw new NullPointerException("The ssl can not be null");
		try {
			HashMap<String, Object> map = new HashMap<>();
			map.put("wxappid", appId);// 应用ID
			map.put("mch_id", mchId);// 商户号
			map.put("nonce_str", getRandamStr());// 随机字符串
			map.put("mch_billno", redpack.mch_billno);// 商户订单号
			map.put("send_name", redpack.send_name);
			map.put("re_openid", redpack.re_openid);
			map.put("total_amount", redpack.total_amount);// 总金额(单位分)
			map.put("total_num", 1);// 总金额(单位分)
			map.put("wishing", redpack.wishing); // 红包祝福语
			map.put("client_ip", redpack.client_ip); // 调用接口的机器Ip地址
			map.put("act_name", redpack.act_name);

			// 增加签名
			map.put("sign", SignUtils.getSign(map, this.apiKey));// 签名

			if (getWechatResult(URL_SEND_REDPACK, this.ssl, map)) {
				redpack.send_listid = getValue(map, "send_listid");
				return true;
			}
		} catch (Exception e) {
			logger.error("Send redpack faild:", e);
		}
		return false;
	}

	/**
	 * 获取红包的发放状态
	 * 
	 * @param billNo
	 *            订单号
	 * @return
	 */
	public RedpackStatus redpackStatus(String billNo) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			map.put("appid", appId);// 应用ID
			map.put("mch_id", mchId);// 商户号
			map.put("nonce_str", getRandamStr());// 随机字符串
			map.put("mch_billno", billNo);// 商户订单号
			map.put("bill_type", "MCHT"); // 订单类型

			// 增加签名
			map.put("sign", SignUtils.getSign(map, this.apiKey));// 签名

			// post调取方法
			if (getWechatResult(URL_REDPACK_STATUS, this.ssl, map)) {
				RedpackStatus status = new RedpackStatus();
				status.status = getValue(map, "status");
				status.total_num = getValue(map, "total_num");
				status.total_amount = getValue(map, "total_amount");
				status.reason = getValue(map, "reason");
				status.send_time = getValue(map, "send_time");
				status.refund_time = getValue(map, "refund_time");
				status.refund_amount = getValue(map, "refund_amount");
				status.openid = getValue(map, "openid");
				status.amount = getValue(map, "amount");
				status.rcv_time = getValue(map, "rcv_time");
				return status;
			}
		} catch (Exception e) {
			logger.error("获取红包状态失败", e);
		}
		return null;
	}

	/**
	 * 企业付款到零钱
	 * 
	 * @param tf
	 *            接口所需参数对象
	 * @return 是否成功
	 */
	public boolean transferCash(Transfer tf) {
		if (ssl == null)
			throw new NullPointerException("The ssl can not be null");
		try {
			HashMap<String, Object> map = new HashMap<>();
			map.put("mch_appid", appId);// 应用ID
			map.put("mchid", mchId);// 商户号
			map.put("nonce_str", getRandamStr());// 随机字符串
			map.put("partner_trade_no", tf.partner_trade_no);// 商户订单号
			map.put("openid", tf.openid);
			map.put("check_name", tf.check_name);
			if ("FORCE_CHECK".equals(tf.check_name))
				map.put("re_user_name", tf.re_user_name);
			map.put("amount", tf.amount);// 总金额(单位分)
			map.put("desc", tf.desc);// 总金额(单位分)
			map.put("spbill_create_ip", tf.spbill_create_ip); // 调用接口的机器Ip地址

			// 增加签名
			map.put("sign", SignUtils.getSign(map, this.apiKey));// 签名

			// 如果执行成功
			if (getWechatResult(URL_TRANSFERS, ssl, map)) {
				tf.payment_no = getValue(map, "payment_no");
				tf.payment_time = getValue(map, "payment_time");
				return true;
			}
			tf.error = getValue(map, "return_msg");
		} catch (Exception e) {
			tf.error = e.getMessage();
			logger.error("Transfers faild", e);
		}
		return false;
	}

}
