package cn.coder.easywx.mapper;

public final class Article {
	private static final String ARTICLE_JSON = "{\"title\":\"%s\",\"description\":\"%s\",\"url\":\"%s\",\"picurl\":\"%s\"}";

	public String title;
	public String description;
	public String url;
	public String picurl;

	public static Article bind(String _title, String desc, String _url, String _picurl) {
		Article art = new Article();
		art.title = _title;
		art.description = desc;
		art.url = _url;
		art.picurl = _picurl;
		return art;
	}

	@Override
	public String toString() {
		return String.format(ARTICLE_JSON, this.title, this.description, this.url, this.picurl);
	}
	
}
