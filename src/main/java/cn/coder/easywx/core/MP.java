package cn.coder.easywx.core;

import java.util.Map;

public class MP extends Base {

	public MP(String appId, String appSecret) {
		// TODO Auto-generated constructor stub
	}

	public interface MsgEvent {

		void doXml(String authXml);

		void doView(Map<String, Object> message);

		void doUnSubscribe(Map<String, Object> message);

		void doText(Map<String, Object> message);

		void doSubscribe(String eventKey, Map<String, Object> message);

		void doScan(String eventKey, Map<String, Object> message);

	}
}
