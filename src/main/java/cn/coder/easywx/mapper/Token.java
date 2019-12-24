package cn.coder.easywx.mapper;

public final class Token {

	private static final long TIME = 7100 * 1000;
	
	private long startTime;
	private String tokenStr;

	public Token(String token) {
		this.tokenStr = token; 
		this.startTime = System.currentTimeMillis();
	}

	public boolean passed() {
		return (System.currentTimeMillis() - startTime) > TIME;
	}

	public String value() {
		return this.tokenStr;
	}

}
