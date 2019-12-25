package cn.coder.easywx.core;

import java.io.BufferedReader;
import java.io.File;
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
	private File cacheTicketFile;
	private File cacheAuthorsFile;
	private HashMap<String, Token> accessTokens = new HashMap<>();
	private HashMap<String, String> refreshTokens;

	public Third(String appId, String appSecret, String token, String msgKey) {
		this.appId = appId;
		this.appSecret = appSecret;
		this.msgCrypt = new WXBizMsgCrypt(token, msgKey, appId);
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

	private static String getVerifyTicket(File file) {
		String ticket = ObjectUtils.readString(file);
		if (ticket != null) {
			if (logger.isDebugEnabled())
				logger.debug("Read ticket from cache:{}", file.getAbsolutePath());
			return ticket;
		}
		return null;
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

		void doText(String openId, Map<String, Object> message);

		void subscribe(String openId, String eventKey, Map<String, Object> message);

		void click(String openId, String eventKey, Map<String, Object> message);

		void scan(String openId, String eventKey, Map<String, Object> message);

		void view(String openId, Map<String, Object> message);

		void unsubscribe(String openId, Map<String, Object> message);

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
			logger.debug("[AUTHORIZATION_INFO]{}", json);
			String appId = JSONUtils.getString(json, "authorizer_appid");
			String refreshToken = JSONUtils.getString(json, "authorizer_refresh_token");
			this.refreshTokens.put(appId, refreshToken);
			// 如果缓存成功，返回Appid
			if (ObjectUtils.writeObject(this.cacheAuthorsFile, this.refreshTokens)) {
				String accessToken = JSONUtils.getString(json, "authorizer_access_token");
				logger.debug("[AUTHORIZER_ACCESS_TOKEN]{}", accessToken);
				this.accessTokens.put(appId, new Token(accessToken));
				return appId;
			}
		}
		throw new NullPointerException("Query auth faild for code '" + authCode + "'");
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String, String> getRefreshTokens(File file) {
		Object obj = ObjectUtils.readObject(file);
		if (obj != null) {
			logger.debug("Load RefreshTokens from cache '{}'", file.getAbsolutePath());
			return (HashMap<String, String>) obj;
		}
		HashMap<String, String> data = new HashMap<>();
		data.put("wx43d50e2df5d92093", "refreshtoken@@@Ox4Y_yuQKr_8Oqjp7sjWFPfdhUJfcth2JfjbQVyGaAU");
		data.put("wx570bc396a51b8ff8", "refreshtoken@@@DwaDOitIyn-8tzu80Y1BuR2sAnE8Io5P8C9zo2Bp4hA");
		return data;
	}

	public MP mp(String appId2) {
		return new MP(getAccessToken(appId2));
	}

	public void forCache(String ticketPath, String authorsPath) {
		this.cacheTicketFile = new File(ticketPath);
		this.cacheAuthorsFile = new File(authorsPath);
		this.verifyTicket = getVerifyTicket(this.cacheTicketFile);
		this.refreshTokens = getRefreshTokens(this.cacheAuthorsFile);
	}

	public void setVerifyTicket(String ticket) {
		if (ticket.equals(this.verifyTicket))
			return;
		if (ObjectUtils.writeString(this.cacheTicketFile, ticket)) {
			this.verifyTicket = ticket;
			logger.debug("Cached ticket:{}", this.verifyTicket);
		}
	}
}
