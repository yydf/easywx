package cn.coder.easywx.mapper;

public class News {
	public String thumb_media_id;
	public String author;
	public String title = "test";
	public String content_source_url;
	public String content = "test";
	public String digest;
	public int show_cover_pic = 0;
	public int need_open_comment = 0;
	public int only_fans_can_comment = 0;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"thumb_media_id\":\"").append(thumb_media_id).append("\",");
		if (author != null)
			sb.append("\"author\":\"").append(author).append("\",");
		sb.append("\"title\":\"").append(title).append("\",");
		if (content_source_url != null)
			sb.append("\"content_source_url\":\"").append(content_source_url).append("\",");
		sb.append("\"content\":\"").append(content).append("\",");
		if (digest != null)
			sb.append("\"digest\":\"").append(digest).append("\",");
		sb.append("\"show_cover_pic\":");
		sb.append(show_cover_pic);
		sb.append(",\"need_open_comment\":");
		sb.append(need_open_comment);
		sb.append(",\"only_fans_can_comment\":");
		sb.append(only_fans_can_comment);
		sb.append("}");
		return sb.toString();
	}
}
