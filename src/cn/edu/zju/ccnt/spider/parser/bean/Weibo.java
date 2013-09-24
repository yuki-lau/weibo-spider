package cn.edu.zju.ccnt.spider.parser.bean;

public class Weibo {
	private String id = null;
	private String poster = null;
	private String content = null;
	private String postTime = null;
	private boolean hasPic = false;
	private boolean isRepost = false;
	
	
	public String getPostTime() {
		return postTime;
	}

	public void setPostTime(String postTime) {
		this.postTime = postTime;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getPoster() {
		return poster;
	}
	
	public void setPoster(String poster) {
		this.poster = poster;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public boolean isHasPic() {
		return hasPic;
	}

	public void setHasPic(boolean hasPic) {
		this.hasPic = hasPic;
	}

	public boolean isRepost() {
		return isRepost;
	}

	public void setRepost(boolean isRepost) {
		this.isRepost = isRepost;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("id:\t\t").append(id).append("\n")
		  .append("poster:\t\t").append(poster).append("\n")
		  .append("content:\t").append(content).append("\n")
		  .append("hasPic:\t\t").append(hasPic).append("\n")
		  .append("isRepost:\t").append(isRepost).append("\n");
		
		return sb.toString();
	}
}
