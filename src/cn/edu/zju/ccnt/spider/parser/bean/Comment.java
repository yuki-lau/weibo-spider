package cn.edu.zju.ccnt.spider.parser.bean;


public class Comment {
	private String id = null;
	private String author=null;
	private String time=null;
	private String content = null;
	
	
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}	
	
	
	public String getId() {
		return id;
	}
	
	
	
	public void setId(String id) {
		this.id = id;
	}
	
	
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	

	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	
//	public String toString(){
//		StringBuilder sb = new StringBuilder();
//		
//		sb.append("id:\t\t").append(id).append("\n")
//		  .append("poster:\t\t").append(poster).append("\n")
//		  .append("content:\t").append(content).append("\n")
//		  .append("hasPic:\t\t").append(hasPic).append("\n")
//		  .append("isRepost:\t").append(isRepost).append("\n");
//		
//		return sb.toString();
//	}
}

