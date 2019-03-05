package cn.coder.easywx.core;

import java.io.BufferedReader;
import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.result.PayResult;
import cn.coder.easywx.result.RefundOrder;
import cn.coder.easywx.result.UnifiedOrder;
import cn.coder.easywx.util.SignUtils;
import cn.coder.easywx.util.XMLUtils;

public class Payment extends Base {
	private static final Logger logger = LoggerFactory.getLogger(Payment.class);
	private static final String URL_CREATE_UNIFIEDORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	private static final String URL_REFUNDORDER = "https://api.mch.weixin.qq.com/secapi/pay/refund";
	private final String apiKey;
	private final String mchId;
	private final String notifyUrl;
	private final String appId;

	public Payment(String appId, String mchId, String apiKey, String callbackUrl) {
		this.appId = appId;
		this.mchId = mchId;
		this.apiKey = apiKey;
		this.notifyUrl = callbackUrl;
	}

	public PayResult callback(BufferedReader reader) {
		String xml = XMLUtils.deserialize(reader);
		logger.debug("[Wechat]:" + xml);
		HashMap<String, Object> result = XMLUtils.doXMLParse(xml);
		// 退款通知
		if (result.containsKey("req_info")) {
			byte[] data = Base64.getDecoder().decode(result.get("req_info").toString());
			byte[] key = SignUtils.encodeByMD5(this.apiKey).toLowerCase().getBytes();
			Security.addProvider(new BouncyCastleProvider());
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
				msg.mch_id = getValue(result, "mch_id");
				msg.openid = getValue(result, "openid");
				msg.trade_type = getValue(result, "trade_type");
				// 商户订单号
				msg.out_trade_no = getValue(result, "out_trade_no");
				msg.time_end = getValue(result, "time_end");
				msg.cash_fee = getValue(result, "cash_fee");
				return msg;
			}
			logger.debug("Check sign faild");
		}
		return null;
	}

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

	public boolean refundCash(RefundOrder order, SSLSocketFactory ssl) {
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

			String return_xml = postString(URL_REFUNDORDER, ssl, XMLUtils.toXML(map));
			logger.debug("[XML]" + return_xml);
			Map<String, Object> result = XMLUtils.doXMLParse(return_xml);
			String returnCode = getValue(result, "return_code");
			String resultCode = getValue(result, "result_code");
			return ("SUCCESS".equals(returnCode)) && ("SUCCESS".equals(resultCode));
		} catch (Exception e) {
			logger.error("Refund cash faild", e);
			return false;
		}
	}

}
