package cn.coder.easywx.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.mapper.Token;
import cn.coder.easywx.util.JSONUtils;

/**
 * 企业号
 * @author YYDF
 *
 */
public final class Corporation extends Base {

	private static final Logger logger = LoggerFactory.getLogger(Corporation.class);
	private static final String URL_TOKEN = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";
	private final String corpid;
	private final String corpSecret;
	private Token _token;

	public Corporation(String corpid, String corpSecret) {
		this.corpid = corpid;
		this.corpSecret = corpSecret;
	}

	public synchronized String getAccessToken() {
		if (_token == null || _token.passed()) {
			String json = getJSON(String.format(URL_TOKEN, corpid, corpSecret));
			if (valid(json, "\"access_token\"")) {
				logger.debug("[ACCESS_TOKEN]" + json);
				_token = new Token(JSONUtils.getString(json, "access_token"));
			} else
				throw new NullPointerException("Not found the access_token");
		}
		return _token.value();
	}

}
