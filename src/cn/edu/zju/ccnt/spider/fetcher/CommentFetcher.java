package cn.edu.zju.ccnt.spider.fetcher;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import cn.edu.zju.ccnt.spider.parser.CommentParser;
import cn.edu.zju.ccnt.spider.parser.bean.Page;
import cn.edu.zju.ccnt.spider.queue.CommentUrlQueue;
import cn.edu.zju.ccnt.spider.queue.VisitedCommentUrlQueue;
import cn.edu.zju.ccnt.spider.utils.Constants;
import cn.edu.zju.ccnt.spider.utils.FetcherType;
import cn.edu.zju.ccnt.spider.utils.Utils;

public class CommentFetcher {
	private static final Logger Log = Logger.getLogger(CommentFetcher.class.getName());
	
	/**
	 * 根据url爬取网页内容
	 * @param url
	 * @return
	 */
	public static Page getContentFromUrl(String url){
		String content = null;
		Document contentDoc = null;
		
		// 设置GET超时时间
		HttpParams params = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
	    HttpConnectionParams.setSoTimeout(params, 10 * 1000);	    
		AbstractHttpClient httpClient = new DefaultHttpClient(params);
		HttpGet getHttp = new HttpGet(url);	
		// 设置HTTP Header
		getHttp.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
		HttpResponse response;
		
		try{
			// 获得信息载体
			response = httpClient.execute(getHttp);
			HttpEntity entity = response.getEntity();		  
			  
			if(entity != null){
				// 转化为文本信息, 设置爬取网页的字符集，防止乱码
				content = EntityUtils.toString(entity, "UTF-8");
				
				String returnMsg = Utils.checkContent(content, url, FetcherType.COMMENT);
				if(returnMsg != null){
					return new Page(returnMsg, null);
				}
				
				// 将content字符串转换成Document对象
				contentDoc = CommentParser.getPageDocument(content);
				// 取回这个页面所有的评论
				List<Element> commentItems = CommentParser.getGoalContent(contentDoc);
				if(commentItems != null && commentItems.size() > 0){
					CommentParser.createFile(commentItems, url);
				}
			}
		}
		catch(Exception e){
			Log.error(e);
			
			// 处理超时，和请求忙相同
			url = url.split("&gsid")[0];
			Log.info(">> Put back url: " + url);
			CommentUrlQueue.addFirstElement(url);
			return new Page(Constants.SYSTEM_BUSY, null);
		}
		
		VisitedCommentUrlQueue.addElement(url);
		
		return new Page(content, contentDoc);
	}
}

