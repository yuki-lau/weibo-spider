package cn.edu.zju.ccnt.spider.handler;

import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import cn.edu.zju.ccnt.spider.parser.bean.Page;
import cn.edu.zju.ccnt.spider.queue.AbnormalAccountUrlQueue;
import cn.edu.zju.ccnt.spider.queue.CommentUrlQueue;
import cn.edu.zju.ccnt.spider.queue.FollowUrlQueue;
import cn.edu.zju.ccnt.spider.queue.RepostUrlQueue;
import cn.edu.zju.ccnt.spider.queue.VisitedCommentUrlQueue;
import cn.edu.zju.ccnt.spider.queue.VisitedFollowUrlQueue;
import cn.edu.zju.ccnt.spider.queue.VisitedRepostUrlQueue;
import cn.edu.zju.ccnt.spider.queue.VisitedWeiboUrlQueue;
import cn.edu.zju.ccnt.spider.queue.WeiboUrlQueue;
import cn.edu.zju.ccnt.spider.utils.Constants;

public class NextUrlHandler {
	
	public static final Logger Log = Logger.getLogger(NextUrlHandler.class.getName());

	/**
	 * 从抓取页面的content HTML中解析出下一页的URL，并添加至UrlQueue
	 * 
	 * @param content
	 * @return
	 */
	public static String addNextWeiboUrl(Page page){
		String content = page.getContent();
		Document doc = page.getContentDoc();
		
		// 请求weibo.cn返回entity为null
		if(content == null){
			return Constants.ACCOUNT_FORBIDDEN;
		}
		// 系统繁忙或账号被封
		if(content.equals(Constants.ACCOUNT_FORBIDDEN) || content.equals(Constants.SYSTEM_BUSY)){
			return content;
		}
		// 微博页面异常的显示“没有发布微博”，跳转至下一页继续处理
		else if(content.startsWith(Constants.SYSTEM_EMPTY)){
			Log.info(">> 当前页面显示“没有发布微博”：" + content);
		}
		// 正常
		else{
			//    WeiboFetcher L68: WeiboParser.getGoalContent 
			// -> WeiboParser L27：已经对content进行过一次Jsoup.parse了
			// Jsoup.parse是一个非常耗时的操作，因此通过传参的方式将这里的Jsoup.parse避免掉
			// Document doc = Jsoup.parse(content);	
			
			Element pageEl = doc.getElementById("pagelist");
			
			if(pageEl != null){
				List<Element> hrefEls = pageEl.getElementsByTag("a");
				for(Element el: hrefEls){
					if(el.toString().contains("下页")){
						WeiboUrlQueue.addElement("http://weibo.cn" + el.attr("href").split("&gsid=")[0]); 
						break;
					}
				}
				Log.info(">> progress of current user: " + pageEl.text());
			}
		}
		Log.info("-------------------");
		Log.info("抓取到：" + WeiboUrlQueue.size());
		Log.info("已处理：" + VisitedWeiboUrlQueue.size());
		Log.info("异常数：" + AbnormalAccountUrlQueue.size());
		Log.info("-------------------");
		
		return Constants.OK;
	}
	
	public static String addNextCommentUrl(Page page){
		String content = page.getContent();
		Document doc = page.getContentDoc();
		
		// 请求weibo.cn返回entity为null
		if(content == null){
			return Constants.ACCOUNT_FORBIDDEN;
		}
		// 系统繁忙或账号被封
		if(content.equals(Constants.ACCOUNT_FORBIDDEN) || content.equals(Constants.SYSTEM_BUSY)){
			return content;
		}
		
		Element pageEl = doc.getElementById("pagelist");
		if(pageEl != null){
			List<Element> hrefEls = pageEl.getElementsByTag("a");
			for(Element el: hrefEls){
				if(el.toString().contains("下页")){
					// 从href中解析出page的页码，牵涉到gsid会自动带上的问题，所以干净的解析出页码
					String[] hrefParts = el.attr("href").split("\\?");
					String pageNum = null;
					for(int i = 0; i < hrefParts.length; i++){
						if(hrefParts[i].contains("page=")){
							String[] params = hrefParts[i].split("&");
							for(int j = 0; j < params.length; j++){
								if(params[j].contains("page=")){
									pageNum = params[j].substring(5);
									break;
								}
							}
							break;
						}
					}
					String nextUrl = "http://weibo.cn" + hrefParts[0] + "?page=" + pageNum;
					CommentUrlQueue.addElement(nextUrl); 
					Log.info(">> Add next page: " + nextUrl);
					break;
				}
			}
		}
		
		Log.info("-------------------");
		Log.info("抓取到：" + CommentUrlQueue.size());
		Log.info("已处理：" + VisitedCommentUrlQueue.size());
		Log.info("异常数：" + AbnormalAccountUrlQueue.size());
		Log.info("-------------------");
		
		return Constants.OK;
	}

	public static String addNextRepostUrl(Page page){
		String content = page.getContent();
		Document doc = page.getContentDoc();
		
		// 请求weibo.cn返回entity为null
		if(content == null){
			return Constants.ACCOUNT_FORBIDDEN;
		}
		// 系统繁忙或账号被封
		if(content.equals(Constants.ACCOUNT_FORBIDDEN) || content.equals(Constants.SYSTEM_BUSY)){
			return content;
		}
		
		Element pageEl = doc.getElementById("pagelist");	
		if(pageEl != null){
			List<Element> hrefEls = pageEl.getElementsByTag("a");
			for(Element el: hrefEls){
				if(el.toString().contains("下页")){
					// 从href中解析出page的页码，牵涉到gsid会自动带上的问题，所以干净的解析出页码
					String[] hrefParts = el.attr("href").split("\\?");
					String pageNum = null;
					for(int i = 0; i < hrefParts.length; i++){
						if(hrefParts[i].contains("page=")){
							String[] params = hrefParts[i].split("&");
							for(int j = 0; j < params.length; j++){
								if(params[j].contains("page=")){
									pageNum = params[j].substring(5);
									break;
								}
							}
							break;
						}
					}
					String nextUrl = "http://weibo.cn" + hrefParts[0] + "?page=" + pageNum;
					RepostUrlQueue.addElement(nextUrl); 
					Log.info(">> Add next page: " + nextUrl);
					break;
				}
			}
		}
		
		Log.info("-------------------");
		Log.info("抓取到：" + RepostUrlQueue.size());
		Log.info("已处理：" + VisitedRepostUrlQueue.size());
		Log.info("异常数：" + AbnormalAccountUrlQueue.size());
		Log.info("-------------------");
		
		return Constants.OK;
	}
	
	public static String addNextFollowUrl(Page page){
		String content = page.getContent();
		Document doc = page.getContentDoc();
		
		// 请求weibo.cn返回entity为null
		if(content == null){
			return Constants.ACCOUNT_FORBIDDEN;
		}
		// 系统繁忙或账号被封
		if(content.equals(Constants.ACCOUNT_FORBIDDEN) || content.equals(Constants.SYSTEM_BUSY)){
			return content;
		}
		
		if(doc != null){
			Element pageEl = doc.getElementById("pagelist");	
			if(pageEl != null){
				List<Element> hrefEls = pageEl.getElementsByTag("a");
				for(Element el: hrefEls){
					if(el.toString().contains("下页")){
						// 从href中解析出page的页码，牵涉到gsid会自动带上的问题，所以干净的解析出页码
						String[] hrefParts = el.attr("href").split("\\?");
						String pageNum = null;
						for(int i = 0; i < hrefParts.length; i++){
							if(hrefParts[i].contains("page=")){
								String[] params = hrefParts[i].split("&");
								for(int j = 0; j < params.length; j++){
									if(params[j].contains("page=")){
										pageNum = params[j].substring(5);
										break;
									}
								}
								break;
							}
						}
						String nextUrl = "http://weibo.cn" + hrefParts[0] + "?page=" + pageNum;
						FollowUrlQueue.addElement(nextUrl); 
						Log.info(">> Add next page: " + nextUrl);
						break;
					}
				}
			}
		}
		Log.info("-------------------");
		Log.info("抓取到：" + FollowUrlQueue.size());
		Log.info("已处理：" + VisitedFollowUrlQueue.size());
		Log.info("异常数：" + AbnormalAccountUrlQueue.size());
		Log.info("-------------------");
		
		return Constants.OK;
	}
}
