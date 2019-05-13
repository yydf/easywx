package cn.coder.easywx.result;

public class PayResult {

	public static final String SUCCESS = "<xml><return_code>SUCCESS</return_code><return_msg>OK</return_msg></xml>";
	public static final String FAIL = "<xml><return_code>FAIL</return_code><return_msg>ERROR</return_msg></xml>";

	// 订单号
	public String out_trade_no;
	public String out_refund_no;
	public String transaction_id;

	// 时间
	public String time_end;
	public String success_time;

	// 金额
	public String cash_fee;
	public String refund_fee;
	public String total_fee;

	// 商户
	public String mch_id;
	public String appid;
	public String openid;

	public String trade_type;
	public String bank_type;
	private final boolean refund;

	public PayResult(boolean refund) {
		this.refund = refund;
	}

	public boolean isRefund() {
		return this.refund;
	}

}
