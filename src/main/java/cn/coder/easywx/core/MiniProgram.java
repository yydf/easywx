package cn.coder.easywx.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.result.WXSession;
import cn.coder.easywx.util.JSONUtils;

public class MiniProgram extends Base {

	private static final Logger logger = LoggerFactory.getLogger(MiniProgram.class);
	private static final String URL_CODE2SESSION = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
	private Payment _pay;
	private final String appId;
	private final String appSecret;

	public MiniProgram(String appId, String appSecret) {
		this.appId = appId;
		this.appSecret = appSecret;
	}

	public void forPayment(String mchId, String apiKey, String callbackUrl) {
		this._pay = new Payment(this.appId, mchId, apiKey, callbackUrl);
	}

	public Payment pay() {
		if (this._pay == null)
			throw new NullPointerException("The payment can not be null");
		return _pay;
	}

	public WXSession code2Session(String code) {
		String json = getJSON(String.format(URL_CODE2SESSION, appId, appSecret, code));
		logger.debug("[code2Session]" + json);
		if (valid(json, "openid")) {
			WXSession session = new WXSession();
			session.openid = JSONUtils.getString(json, "openid");
			session.session_key = JSONUtils.getString(json, "session_key");
			session.unionid = JSONUtils.getString(json, "unionid");
			return session;
		}
		return null;
	}

}
