package cn.edu.zju.ccnt.spider.parser.bean;

import org.jsoup.nodes.Document;

public class Page {
	private String content;
	private Document contentDoc;
	
	public Page(){
		
	}
	
	public Page(String content, Document contentDoc){
		this.content = content;
		this.contentDoc = contentDoc;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public Document getContentDoc() {
		return contentDoc;
	}
	
	public void setContentDoc(Document contentDoc) {
		this.contentDoc = contentDoc;
	}
}
