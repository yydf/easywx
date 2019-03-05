package cn.coder.easywx.core;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.util.JSONUtils;

public class MP extends Base {

	private static final Logger logger = LoggerFactory.getLogger(MP.class);
	private static final long TIME = 7100 * 1000;
	private static final String URL_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	private final String appId;
	private final String appSecret;
	private Payment _pay;
	private String _token;
	private long _tokenTime;

	public MP(String appId, String appSecret) {
		this.appId = appId;
		this.appSecret = appSecret;
	}

	public synchronized String getAccessToken() {
		if (_token == null || (System.currentTimeMillis() - _tokenTime) > TIME) {
			String json = getJSON(String.format(URL_TOKEN, appId, appSecret));
			logger.debug("[ACCESS_TOKEN]" + json);
			if (json != null && json.contains("\"access_token\"")) {
				_token = JSONUtils.getString(json, "access_token");
				_tokenTime = System.currentTimeMillis();
			}
		}
		return _token;
	}

	public void forPayment(String mchId, String apiKey, String callbackUrl) {
		this._pay = new Payment(this.appId, mchId, apiKey, callbackUrl);
	}
	
	public Payment pay() {
		if (this._pay == null)
			throw new NullPointerException("The payment can not be null");
		return _pay;
	}

	public interface MsgEvent {

		void doXml(String authXml);

		void doView(Map<String, Object> message);

		void doUnSubscribe(Map<String, Object> message);

		void doText(Map<String, Object> message);

		void doSubscribe(String eventKey, Map<String, Object> message);

		void doScan(String eventKey, Map<String, Object> message);

	}
}
