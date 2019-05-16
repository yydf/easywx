package cn.coder.easywx.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.util.JSONUtils;

public final class Corporation extends Base {

	private static final Logger logger = LoggerFactory.getLogger(Corporation.class);
	private static final long TIME = 7100 * 1000;
	private static final String URL_TOKEN = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";
	private final String corpid;
	private final String corpSecret;
	private String _token;
	private long _tokenTime;

	public Corporation(String corpid, String corpSecret) {
		this.corpid = corpid;
		this.corpSecret = corpSecret;
	}

	public synchronized String getAccessToken() {
		if (_token == null || (System.currentTimeMillis() - _tokenTime) > TIME) {
			String json = getJSON(String.format(URL_TOKEN, corpid, corpSecret));
			logger.debug("[ACCESS_TOKEN]" + json);
			if (json != null && json.contains("\"access_token\"")) {
				_token = JSONUtils.getString(json, "access_token");
				_tokenTime = System.currentTimeMillis();
			}
		}
		return _token;
	}

}
