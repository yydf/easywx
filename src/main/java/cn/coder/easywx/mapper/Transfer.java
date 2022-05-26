package cn.coder.easywx.mapper;

public class Transfer {

	public String partner_trade_no;
	public String openid;
	public String check_name = "NO_CHECK";
	public String re_user_name;
	public int amount;
	public String desc;
	public String spbill_create_ip;
	public String payment_no;
	public String payment_time;

	// 只有失败的时候才赋值
	public String error;

	public static Transfer test(String openid) {
		Transfer t = new Transfer();
		t.amount = 30; // 最低3毛钱
		t.openid = openid;
		t.partner_trade_no = System.nanoTime() + "";
		t.desc = "测试提现";
		return t;
	}

}
