package cn.coder.easywx.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.util.JSONUtils;

/**
 * 开放平台
 * @author YYDF
 *
 */
public final class Open extends Base {

	private static final Logger logger = LoggerFactory.getLogger(Open.class);
	private static final String URL_OPENID = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
	private final String appId;
	private final String appSecret;

	public Open(String appId, String appSecret) {
		this.appId = appId;
		this.appSecret = appSecret;
	}

	public String getOpenId(String code) {
		String json = getJSON(String.format(URL_OPENID, appId, appSecret, code));
		logger.debug("[OPENID]" + json);
		if (valid(json, "openid"))
			return JSONUtils.getString(json, "openid");
		return null;
	}

}
