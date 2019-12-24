package cn.coder.easywx.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.encrypt.AesException;
import cn.coder.easywx.encrypt.WXBizMsgCrypt;
import cn.coder.easywx.mapper.Token;
import cn.coder.easywx.util.JSONUtils;
import cn.coder.easywx.util.ObjectUtils;

public class Third extends Base {
	private static final Logger logger = LoggerFactory.getLogger(Third.class);

	private static final String API_COMPONENT_TOKEN = "https://api.weixin.qq.com/cgi-bin/component/api_component_token";
	private static final String API_CREATE_PREAUTHCODE = "https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode?component_access_token=%s";
	private static final String API_QUERY_AUTH = "https://api.weixin.qq.com/cgi-bin/component/api_query_auth?component_access_token=%s";
	private static final String API_AUTHORIZER_TOKEN = "https://api.weixin.qq.com/cgi-bin/component/api_authorizer_token?component_access_token=%s";

	private static final String POST_TOKEN = "{\"component_appid\":\"%s\",\"component_appsecret\":\"%s\",\"component_verify_ticket\":\"%s\"}";
	private static final String POST_APPID = "{\"component_appid\":\"%s\"}";
	private static final String POST_AUTHCODE = "{\"component_appid\":\"%s\",\"authorization_code\":\"%s\"}";
	private static final String POST_AUTHORIZER_TOKEN = "{\"component_appid\":\"%s\",\"authorizer_appid\":\"%s\",\"authorizer_refresh_token\":\"%s\"}";

	private final WXBizMsgCrypt msgCrypt;
	private final String appId;
	private final String appSecret;
	private String verifyTicket;
	private Token _token;
	private HashMap<String, Token> accessTokens;
	private HashMap<String, String> refreshTokens;

	public Third(String appId, String appSecret, String token, String msgKey) {
		this.appId = appId;
		this.appSecret = appSecret;
		this.msgCrypt = new WXBizMsgCrypt(token, msgKey, appId);
		this.verifyTicket = getVerifyTicket();
		this.refreshTokens = getRefreshTokens();
	}

	private synchronized String getComponentToken() {
		if (this.verifyTicket == null)
			throw new NullPointerException("Not found the component_verify_ticket");
		if (this._token == null || this._token.passed()) {
			String post = String.format(POST_TOKEN, appId, appSecret, this.verifyTicket);
			String json = postString(API_COMPONENT_TOKEN, post);
			if (valid(json, "component_access_token")) {
				logger.debug("[COMPONENT_ACCESS_TOKEN]" + json);
				this._token = new Token(JSONUtils.getString(json, "component_access_token"));
			} else
				throw new NullPointerException("Not found the component_access_token");
		}
		return _token.value();
	}

	public String getAccessToken(String appId2) {
		String refresh = this.refreshTokens.get(appId2);
		if (refresh == null)
			throw new NullPointerException("Not found the authorizer_refresh_token for '" + appId2 + "'");

		synchronized (this.accessTokens) {
			Token token = this.accessTokens.get(appId2);
			if (token == null || token.passed()) {
				String post = String.format(POST_AUTHORIZER_TOKEN, appId, appId2, refresh);
				String url = String.format(API_AUTHORIZER_TOKEN, getComponentToken());
				String json = postString(url, post);
				if (valid(json, "authorizer_access_token")) {
					logger.debug("[AUTHORIZER_ACCESS_TOKEN]" + json);
					token = new Token(JSONUtils.getString(json, "authorizer_access_token"));
					// 缓存
					this.accessTokens.put(appId2, token);
				} else
					throw new NullPointerException("Not found the component_access_token");
			}
			return token.value();
		}
	}

	public String getPreAuthCode() {
		String url = String.format(API_CREATE_PREAUTHCODE, getComponentToken());
		String json = postString(url, String.format(POST_APPID, appId));
		if (valid(json, "pre_auth_code"))
			return JSONUtils.getString(json, "pre_auth_code");
		throw new NullPointerException("Not found the pre_auth_code");
	}

	private String getVerifyTicket() {
		File file = new File("/home/zdwl/upload/ticket.txt");
		if (file.exists()) {
			FileInputStream fr;
			try {
				fr = new FileInputStream(file);
				byte[] temp = new byte[fr.available()];
				fr.read(temp);
				this.verifyTicket = new String(temp, "UTF-8");
				logger.debug("Read ticket:{}", this.verifyTicket);
			} catch (IOException e) {
				logger.warn("Read ticket faild", e);
			}
		}
		return null;
	}

	public void setVerifyTicket(String ticket) throws IOException {
		this.verifyTicket = ticket;
		FileWriter fw = new FileWriter(new File("/home/zdwl/upload/ticket.txt"), false);
		fw.write(ticket);
		fw.close();
		logger.debug("Save ticket:{}", this.verifyTicket);
	}

	public interface AuthorEvent {

		BufferedReader getReader() throws IOException;

		String getRequestParameter(String name);

		void authorized(HashMap<String, Object> map);

		void updateauthorized(HashMap<String, Object> map);

		void unauthorized(HashMap<String, Object> map);

		void doResponse(String str);
	}

	public interface ThirdEvent {
		BufferedReader getReader() throws IOException;

		String getRequestParameter(String name);

		void doText(String from, Map<String, Object> message);

		void doResponse(String str);
	}

	public String encryptMsg(String replyMsg) throws AesException {
		return this.msgCrypt.encryptMsg(replyMsg, getTimestamp() + "", getRandamStr());
	}

	public String decryptMsg(String msgSignature, String timeStamp, String nonce, String postData) throws AesException {
		return this.msgCrypt.decryptMsg(msgSignature, timeStamp, nonce, postData);
	}

	public String queryAuth(String authCode) {
		String url = String.format(API_QUERY_AUTH, getComponentToken());
		String json = postString(url, String.format(POST_AUTHCODE, this.appId, authCode));
		if (valid(json, "authorization_info")) {
			String appId = JSONUtils.getString(json, "authorizer_appid");
			String refreshToken = JSONUtils.getString(json, "authorizer_refresh_token");
			this.refreshTokens.put(appId, refreshToken);
			// 如果缓存成功，返回Appid
			if (!ObjectUtils.writeObject("/home/zdwl/upload/authors.data", this.refreshTokens))
				return appId;
		}
		throw new NullPointerException("Query auth faild for code '" + authCode + "'");
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String, String> getRefreshTokens() {
		Object obj = ObjectUtils.readObject("/home/zdwl/upload/authors.data");
		if (obj != null)
			return (HashMap<String, String>) obj;
		return new HashMap<>();
	}
}
