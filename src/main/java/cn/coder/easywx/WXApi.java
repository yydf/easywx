package cn.coder.easywx;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.easywx.core.Corporation;
import cn.coder.easywx.core.MP;
import cn.coder.easywx.core.MP.MsgEvent;
import cn.coder.easywx.core.MiniProgram;
import cn.coder.easywx.core.Open;
import cn.coder.easywx.core.Payment;

public class WXApi {

	private static final Logger logger = LoggerFactory.getLogger(WXApi.class);
	private static MP mp;
	private static Open open;
	private static MiniProgram miniProgram;
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

	public static Payment appPay() {
		Payment pay = miniProgram.pay();
		if (pay == null)
			throw new NullPointerException("The payment can not be null");
		return pay;
	}

	public static void auth(HttpServletRequest request, HttpServletResponse response, MsgEvent event) {
		// TODO Auto-generated method stub

	}

}
