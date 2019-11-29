package cn.coder.easywx;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.core.Corporation;
import cn.coder.easywx.core.MP;
import cn.coder.easywx.core.MP.MsgEvent;
import cn.coder.easywx.core.MiniProgram;
import cn.coder.easywx.core.Open;
import cn.coder.easywx.core.Payment;
import cn.coder.easywx.util.XMLUtils;

public class WXApi {

	private static final Logger logger = LoggerFactory.getLogger(WXApi.class);
	// 公众号
	private static MP mp;
	// 开放平台
	private static Open open;
	// 小程序
	private static MiniProgram miniProgram;
	// 企业号
	private static Corporation corp;

	public static MP forMP(String appId, String appSecret) {
		mp = new MP(appId, appSecret);
		if (logger.isDebugEnabled())
			logger.debug("Register for MP success.");
		return mp;
	}

	public static Open forOpen(String appId, String appSecret) {
		open = new Open(appId, appSecret);
		if (logger.isDebugEnabled())
			logger.debug("Register for open success.");
		return open;
	}

	public static MiniProgram forProgram(String appId, String appSecret) {
		miniProgram = new MiniProgram(appId, appSecret);
		if (logger.isDebugEnabled())
			logger.debug("Register for mini program success.");
		return miniProgram;
	}

	public static Corporation forCorporation(String corpid, String corpSecret) {
		corp = new Corporation(corpid, corpSecret);
		if (logger.isDebugEnabled())
			logger.debug("Register for corporation success.");
		return corp;
	}

	public static MP mp() {
		if (mp == null)
			throw new NullPointerException("The mp can not be null");
		return mp;
	}

	public static Open open() {
		if (open == null)
			throw new NullPointerException("The open can not be null");
		return open;
	}

	public static MiniProgram app() {
		if (miniProgram == null)
			throw new NullPointerException("The mini program can not be null");
		return miniProgram;
	}

	public static Corporation corp() {
		if (corp == null)
			throw new NullPointerException("The corporation can not be null");
		return corp;
	}

	public static Payment mpPay() {
		Payment pay = mp.pay();
		if (pay == null)
			throw new NullPointerException("The payment can not be null");
		return pay;
	}

	public static Payment appPay() {
		Payment pay = miniProgram.pay();
		if (pay == null)
			throw new NullPointerException("The payment can not be null");
		return pay;
	}

	public static void auth(String method, MsgEvent msgEvent) {
		try {
			if ("POST".equals(method)) {
				String authXml = XMLUtils.deserialize(msgEvent.getReader());
				logger.debug("[XML]" + authXml);
				HashMap<String, Object> map = XMLUtils.doXMLParse(authXml);
				String toUserName = map.get("ToUserName") + "";
				final String fromUserName = map.get("FromUserName") + "";
				String msgType = map.get("MsgType") + "";
				Map<String, Object> message = new HashMap<>();
				message.put("FromUserName", toUserName);
				message.put("ToUserName", fromUserName);
				// 对文本消息进行处理
				if ("text".equals(msgType)) {
					msgEvent.doText(message);
				} else if ("event".equals(msgType)) {
					String event = map.get("Event") + "";
					String eventKey = map.get("EventKey") + "";
					if (event.equals("subscribe")) {
						msgEvent.doSubscribe(eventKey, message);
					} else if (event.equals("unsubscribe")) {
						msgEvent.doUnSubscribe(message);
					} else if (event.equals("SCAN")) {
						msgEvent.doScan(eventKey, message);
					} else if (event.equals("VIEW")) {
						msgEvent.doView(message);
					}
				}
				if (message.size() > 0) {
					message.put("CreateTime", new Date().getTime());
					msgEvent.doResponse(XMLUtils.toXML(message));
				} else
					msgEvent.doResponse("");
			} else {
				msgEvent.doResponse("echostr");
			}
		} catch (IOException e) {
			logger.error("Wechat auth faild", e);
		}
	}

}
