package cn.coder.easywx.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.mapper.Token;
import cn.coder.easywx.mapper.WXSession;
import cn.coder.easywx.util.JSONUtils;

/**
 * 微信小程序
 * @author YYDF
 *
 */
public final class MiniProgram extends Base {

	private static final Logger logger = LoggerFactory.getLogger(MiniProgram.class);
	private static final String URL_CODE2SESSION = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
	private static final String URL_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	private static final String URL_WXACODE = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=%s";
	private static final String URL_SEND_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token=%s";
	
	private static final String POST_WXACODE_STR = "{\"scene\": \"%s\", \"page\": \"%s\", \"width\": 430}";
	
	private Token _token;
	private Payment _pay;
	private final String appId;
	private final String appSecret;

	public MiniProgram(String appId, String appSecret) {
		this.appId = appId;
		this.appSecret = appSecret;
	}

	public Payment forPayment(String mchId, String apiKey, String callbackUrl) {
		this._pay = new Payment(this.appId, mchId, apiKey, callbackUrl);
		return this._pay;
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

	public synchronized String getAccessToken() {
		if (_token == null || _token.passed()) {
			String json = getJSON(String.format(URL_TOKEN, appId, appSecret));
			if (valid(json, "access_token")) {
				logger.debug("[ACCESS_TOKEN]" + json);
				_token = new Token(JSONUtils.getString(json, "access_token"));
			} else
				throw new NullPointerException("Not found the access_token");
		}
		return _token.value();
	}
	
	public byte[] createWxacode(String sceneStr, String path) {
		String postStr = String.format(POST_WXACODE_STR, sceneStr, path);
		return download(String.format(URL_WXACODE, getAccessToken()), postStr);
	}

	public Long sendTemplate(String templateJson) {
		String postUrl = String.format(URL_SEND_TEMPLATE, getAccessToken());
		String json = postString(postUrl, templateJson);
		logger.debug("[SEND_TEMPLATE]" + json);
		if (valid(json, "msgid"))
			return JSONUtils.getLong(json, "msgid");
		// 判断是不是token无效，发送模板消息会误报token无效
		if (invalidToken(json)) {
			_token = null;// 清空缓存token
			return sendTemplate(templateJson); // 重新发送
		}
		return null;
	}
}
