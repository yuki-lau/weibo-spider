package cn.edu.zju.ccnt.spider.parser.bean;


public class Repost {
	private String id = null;
	private String author = null;
	private String time = null;
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

	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("id:\t\t").append(id).append("\n")
		  .append("author:\t\t").append(author).append("\n")
		  .append("content:\t").append(content).append("\n")
		  .append("time:\t\t").append(time).append("\n");
		
		return sb.toString();
	}
}

