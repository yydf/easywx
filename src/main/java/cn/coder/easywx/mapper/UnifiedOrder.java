package cn.coder.easywx.mapper;

public class UnifiedOrder {

	public String openid;
	public String out_trade_no;
	public Integer total_fee;
	public String body;
	public String spbill_create_ip;
	public String sub_mch_id;
	public String trade_type;
	
	public static UnifiedOrder test() {
		return test(null);
	}
	
	public static UnifiedOrder test(String openId) {
		UnifiedOrder order = new UnifiedOrder();
		order.out_trade_no = System.nanoTime() + "";
		order.total_fee = 1;
		if (openId != null)
			order.openid = openId;
		order.body = "test";
		order.spbill_create_ip = "127.0.0.1";
		return order;
	}

}
