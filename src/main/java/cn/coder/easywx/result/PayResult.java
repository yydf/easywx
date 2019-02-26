package cn.coder.easywx.result;

public class PayResult {

	public static final String SUCCESS = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
	public static final String FAIL = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理通知消息失败]]></return_msg></xml>";
	
	public String out_trade_no;
	public String out_refund_no;
	public String refund_fee;
	public String transaction_id;
	public String time_end;
	public String cash_fee;
	public String mch_id;
	public String appid;
	public String success_time;
	public String openid;
	public String trade_type;
	private boolean refund;
	
	public PayResult(boolean refund) {
		this.refund = refund;
	}

	public boolean isRefund() {
		return this.refund;
	}

}
