package cn.coder.easywx.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.result.SignedURL;
import cn.coder.easywx.util.JSONUtils;
import cn.coder.easywx.util.SignUtils;

/**
 * 微信公众号接口实现
 * 
 * @author YYDF 2019-03-06
 *
 */
public class MP extends Base {

	private static final Logger logger = LoggerFactory.getLogger(MP.class);
	private static final long TIME = 7100 * 1000;
	private static final String URL_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	private static final String URL_JSTICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";
	private static final String URL_OPENID = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
	private final String appId;
	private final String appSecret;
	private Payment _pay;
	private String _token;
	private long _tokenTime;
	private String _jsticket;
	private long _tokenTime2;

	public MP(String appId, String appSecret) {
		this.appId = appId;
		this.appSecret = appSecret;
	}

	public synchronized String getAccessToken() {
		if (_token == null || (System.currentTimeMillis() - _tokenTime) > TIME) {
			String json = getJSON(String.format(URL_TOKEN, appId, appSecret));
			logger.debug("[ACCESS_TOKEN]" + json);
			if (valid(json, "access_token")) {
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

	/**
	 * 通过code获取用户openid
	 * 
	 * @param code
	 *            随机码
	 * @return openid
	 */
	public String getOpenId(String code) {
		String json = getJSON(String.format(URL_OPENID, appId, appSecret, code));
		logger.debug("[OPENID]" + json);
		if (valid(json, "openid"))
			return JSONUtils.getString(json, "openid");
		return null;
	}

	public synchronized String getJsapiTicket() {
		if (_jsticket == null || (System.currentTimeMillis() - _tokenTime2) > TIME) {
			String json = getJSON(String.format(URL_JSTICKET, getAccessToken()));
			logger.debug("[JSAPI_TICKET]" + json);
			if (valid(json, "ticket")) {
				_jsticket = JSONUtils.getString(json, "ticket");
				_tokenTime2 = System.currentTimeMillis();
			}
		}
		return _jsticket;
	}

	public SignedURL signRequestURL(String url) {
		try {
			String nonceStr = getRandamStr();
			long time = getTimestamp();
			String jsapiTicket = getJsapiTicket();
			StringBuilder signStr = new StringBuilder();
			signStr.append("jsapi_ticket=").append(jsapiTicket);
			signStr.append("&noncestr=").append(nonceStr);
			signStr.append("&timestamp=").append(time);
			signStr.append("&url=").append(url);
			if (logger.isDebugEnabled())
				logger.debug("Sign str:{}", signStr);
			SignedURL ticket = new SignedURL();
			ticket.url = url;
			ticket.appId = appId;
			ticket.nonceStr = nonceStr;
			ticket.timestamp = time;
			ticket.signature = SignUtils.SHA1(signStr.toString());
			ticket.jsapiTicket = jsapiTicket;
			return ticket;
		} catch (RuntimeException e) {
			logger.error("signRequestURL faild", e);
			return null;
		}
	}

	/**
	 * 微信菜单事件回调
	 * 
	 * @author YYDF
	 *
	 */
	public interface MsgEvent {

		BufferedReader getReader() throws IOException;

		void doView(Map<String, Object> message);

		void doUnSubscribe(Map<String, Object> message);

		void doText(Map<String, Object> message);

		void doSubscribe(String eventKey, Map<String, Object> message);

		void doScan(String eventKey, Map<String, Object> message);

		void doResponse(String xml);
	}
}
