package cn.coder.easywx.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.mapper.Article;
import cn.coder.easywx.mapper.News;
import cn.coder.easywx.mapper.SignedURL;
import cn.coder.easywx.util.JSONUtils;
import cn.coder.easywx.util.SignUtils;

/**
 * 微信公众号接口实现
 * 
 * @author YYDF 2019-03-06
 *
 */
public final class MP extends Base {
	private static final Logger logger = LoggerFactory.getLogger(MP.class);

	private final String appId;
	private final String appSecret;
	private Payment _pay;
	private String _token;
	private long _tokenTime;
	private String _jsticket;
	private long _tokenTime2;

	private static final long TIME = 7100 * 1000;

	private static final String URL_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	private static final String URL_JSTICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";
	private static final String URL_OPENID = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
	private static final String URL_USER_INFO = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN";
	private static final String URL_CREATE_MENU = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s";
	private static final String URL_CREATE_QRCODE = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s";
	private static final String URL_CREATE_TAG = "https://api.weixin.qq.com/cgi-bin/tags/create?access_token=%s";
	private static final String URL_SELF_MENU = "https://api.weixin.qq.com/cgi-bin/menu/addconditional?access_token=%s";
	private static final String URL_SEND_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";
	private static final String URL_CUSTOM_SEND = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=%s";
	private static final String URL_BIND_TAG = "https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token=%s";
	private static final String URL_UNBIND_TAG = "https://api.weixin.qq.com/cgi-bin/tags/members/batchuntagging?access_token=%s";
	private static final String URL_UPLOADIMG = "https://api.weixin.qq.com/cgi-bin/media/uploadimg?access_token=%s";
	private static final String URL_MEDIA_ADDNEWS = "https://api.weixin.qq.com/cgi-bin/material/add_news?access_token=%s";
	private static final String URL_MEDIA_UPDATENEWS = "https://api.weixin.qq.com/cgi-bin/material/update_news?access_token=%s";
	private static final String URL_PREVIEW_NEWS = "https://api.weixin.qq.com/cgi-bin/message/mass/preview?access_token=%s";
	private static final String URL_ADD_MATERIAL = "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=%s&type=%s";
	private static final String URL_SENDALL_NEWS = "https://api.weixin.qq.com/cgi-bin/message/mass/sendall?access_token=%s";

	private static final String POST_SENCE = "{\"expire_seconds\": %s, \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": %s}}}";
	private static final String POST_LIMIT_SENCE = "{\"action_name\": \"QR_LIMIT_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": %s}}}";
	private static final String POST_STR_SENCE = "{\"expire_seconds\": %s, \"action_name\": \"QR_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \"%s\"}}}";
	private static final String POST_LIMIT_STR_SENCE = "{\"action_name\": \"QR_LIMIT_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \"%s\"}}}";
	private static final String POST_SEND_TEXT = "{\"touser\":\"%s\",\"msgtype\":\"text\",\"text\":{\"content\":\"%s\"}}";
	private static final String POST_SEND_ARTICLE = "{\"touser\":\"%s\",\"msgtype\":\"news\",\"news\":{\"articles\":[%s]}}";
	private static final String POST_NEWS = "{\"articles\":[%s]}";
	private static final String POST_UPDATE_NEWS = "{\"media_id\":\"%s\",\"index\":%s,\"articles\":%s}";
	private static final String POST_PREVIEW_NEWS = "{\"towxname\":\"%s\",\"mpnews\":{\"media_id\":\"%s\"},\"msgtype\":\"mpnews\"}";
	private static final String POST_SENDALL_NEWS_TAG = "{\"filter\":{\"is_to_all\":false,\"tag_id\":%s},\"mpnews\":{\"media_id\":\"%s\"},\"msgtype\":\"mpnews\",\"send_ignore_reprint\":1}";
	private static final String POST_SENDALL_NEWS = "{\"filter\":{\"is_to_all\":true},\"mpnews\":{\"media_id\":\"%s\"},\"msgtype\":\"mpnews\",\"send_ignore_reprint\":1}";

	public MP(String appId, String appSecret) {
		this.appId = appId;
		this.appSecret = appSecret;
	}

	public synchronized String getAccessToken() {
		if (_token == null || (System.currentTimeMillis() - _tokenTime) > TIME) {
			String json = getJSON(String.format(URL_TOKEN, appId, appSecret));
			if (valid(json, "access_token")) {
				logger.debug("[ACCESS_TOKEN]" + json);
				_token = JSONUtils.getString(json, "access_token");
				_tokenTime = System.currentTimeMillis();
			} else
				throw new NullPointerException("Not found the access_token");
		}
		return _token;
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

	public String getUserInfo(String openId) {
		String json = getJSON(String.format(URL_USER_INFO, getAccessToken(), openId));
		logger.debug("[USER_INFO]" + json);
		if (valid(json, "subscribe"))
			return json;
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
	 * 发送模板消息
	 * 
	 * @param templateJson
	 *            模板内容
	 * @return 发送成功的msgid
	 */
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

	public void bindTag(String openId, int tagId) {
		String postStr = String.format("{\"openid_list\":[\"%s\"],\"tagid\":%d}", openId, tagId);
		String json = postString(String.format(URL_BIND_TAG, getAccessToken()), postStr);
		logger.debug("[BIND_TAG]{}", json);
	}

	public void unBindTag(String openId, int tagId) {
		String postStr = String.format("{\"openid_list\":[\"%s\"],\"tagid\":%d}", openId, tagId);
		String json = postString(String.format(URL_UNBIND_TAG, getAccessToken()), postStr);
		logger.debug("[UNBIND_TAG]{}", json);
	}

	public String createQrcode(Object scene) {
		return createQrcode(scene, 0);
	}

	public String createQrcode(Object scene, int expireSeconds) {
		String postStr;
		if (scene instanceof String) {
			if (expireSeconds > 0)
				postStr = String.format(POST_STR_SENCE, expireSeconds, scene);
			else
				postStr = String.format(POST_LIMIT_STR_SENCE, scene);
		} else {
			if (expireSeconds > 0)
				postStr = String.format(POST_SENCE, expireSeconds, scene);
			else
				postStr = String.format(POST_LIMIT_SENCE, scene);
		}
		String json = postString(String.format(URL_CREATE_QRCODE, getAccessToken()), postStr);
		logger.debug("[CREATE_QRCODE]{}", json);
		if (valid(json, "ticket"))
			return JSONUtils.getString(json, "ticket");
		return null;
	}

	public Long createTag(String tag) {
		String postStr = String.format("{\"tag\":{\"name\":\"%s\"}}", tag);
		String json = postString(String.format(URL_CREATE_TAG, getAccessToken()), postStr);
		logger.debug("[CREATE_TAG]{}", json);
		if (valid(json, "id"))
			return JSONUtils.getLong(json, "id");
		return null;
	}

	public void createMenu(String menuStr) {
		String json = postString(String.format(URL_CREATE_MENU, getAccessToken()), menuStr);
		logger.debug("[CREATE_MENU]{}", json);
	}

	public Long createSelfMenu(String menuStr) {
		String json = postString(String.format(URL_SELF_MENU, getAccessToken()), menuStr);
		logger.debug("[SELF_MENU]{}", json);
		if (valid(json, "menuid"))
			return JSONUtils.getLong(json, "menuid");
		return null;
	}

	public void sendText(String openId, String text) {
		String postStr = String.format(POST_SEND_TEXT, openId, text);
		String json = postString(String.format(URL_CUSTOM_SEND, getAccessToken()), postStr);
		logger.debug("[SEND_TEXT]{}", json);
	}

	public void sendArticle(String openId, Article article) {
		String postStr = String.format(POST_SEND_ARTICLE, openId, article);
		String json = postString(String.format(URL_CUSTOM_SEND, getAccessToken()), postStr);
		logger.debug("[SEND_ARTICLE]{}", json);
	}

	public String uploadImg(String url) {
		try {
			String json = postFile(String.format(URL_UPLOADIMG, getAccessToken()), new URL(url).openStream());
			logger.debug("[UPLOAD_IMG]{}", json);
			if (valid(json, "url"))
				return JSONUtils.getString(json, "url");
		} catch (IOException e) {
			logger.error("Upload img faild", e);
		}
		return null;
	}

	/**
	 * 新增一个永久素材
	 * 
	 * @param type
	 *            image图片，voice语音，video视频，thumb缩略图
	 * @param inputFile
	 *            文件流
	 * @return 上传成功的media_id，失败返回null
	 */
	public String addMaterial(String type, InputStream inputFile) {
		String json = postFile(String.format(URL_ADD_MATERIAL, getAccessToken(), type), inputFile);
		logger.debug("[ADD_MATERIAL]{}", json);
		if (valid(json, "media_id"))
			return JSONUtils.getString(json, "media_id");
		return null;
	}

	/**
	 * 上传一个永久的图文素材
	 * 
	 * @param news
	 *            图文的内容
	 * @return 上传成功后的media_id，失败返回null
	 */
	public String addNews(List<News> news) {
		for (News item : news) {
			if (item.thumb_media_id.startsWith("http://") || item.thumb_media_id.startsWith("https://")) {
				try {
					item.thumb_media_id = addMaterial("image", new URL(item.thumb_media_id).openStream());
				} catch (IOException e) {
					throw new RuntimeException("add material faild", e);
				}
				if (item.thumb_media_id == null)
					throw new RuntimeException("Convert thumb_media_id from url faild");
			}
		}
		String postStr = String.format(POST_NEWS, toJson(news));
		String json = postString(String.format(URL_MEDIA_ADDNEWS, getAccessToken()), postStr);
		logger.debug("[ADD_NEWS]{}", json);
		if (valid(json, "media_id"))
			return JSONUtils.getString(json, "media_id");
		return null;
	}

	public boolean updateNews(String mediaId, int index, News item) {
		if (item.thumb_media_id.startsWith("http://") || item.thumb_media_id.startsWith("https://")) {
			try {
				item.thumb_media_id = addMaterial("image", new URL(item.thumb_media_id).openStream());
			} catch (IOException e) {
				throw new RuntimeException("add material faild", e);
			}
			if (item.thumb_media_id == null)
				throw new RuntimeException("Convert thumb_media_id from url faild");
		}
		String postStr = String.format(POST_UPDATE_NEWS, mediaId, index, item.toString());
		String json = postString(String.format(URL_MEDIA_UPDATENEWS, getAccessToken()), postStr);
		logger.debug("[UPDATE_NEWS]{}", json);
		return json != null && JSONUtils.getLong(json, "errcode") == 0L;
	}

	/**
	 * 预览图文消息
	 * 
	 * @param mediaId
	 *            图文消息的media_id
	 * @param wxName
	 *            要预览用户的微信名
	 * @return 是否成功调用
	 */
	public boolean previewNews(String mediaId, String wxName) {
		String postStr = String.format(POST_PREVIEW_NEWS, wxName, mediaId);
		String json = postString(String.format(URL_PREVIEW_NEWS, getAccessToken()), postStr);
		logger.debug("[PREVIEW_NEWS]{}", json);
		return json != null && JSONUtils.getLong(json, "errcode") == 0L;
	}

	/**
	 * 群发图文消息
	 * 
	 * @param mediaId
	 *            图文消息的media_id
	 * @return 是否发送成功
	 */
	public boolean sendAllNews(String mediaId) {
		return sendAllNews(mediaId, null);
	}

	/**
	 * 通过用户标签群发图文消息
	 * 
	 * @param mediaId
	 *            图文消息的media_id
	 * @param tag
	 *            要发送的用户标签
	 * @return 是否发送成功
	 */
	public boolean sendAllNews(String mediaId, String tag) {
		String postStr;
		if (tag == null || tag.length() == 0)
			postStr = String.format(POST_SENDALL_NEWS, mediaId);
		else
			postStr = String.format(POST_SENDALL_NEWS_TAG, tag, mediaId);
		String json = postString(String.format(URL_SENDALL_NEWS, getAccessToken()), postStr);
		logger.debug("[SEND_ALL]{}", json);
		return json != null && JSONUtils.getLong(json, "errcode") == 0L;
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

		void doResponse(String xml) throws IOException;
	}
}
