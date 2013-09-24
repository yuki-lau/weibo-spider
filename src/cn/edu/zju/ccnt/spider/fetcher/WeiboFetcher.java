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

import cn.edu.zju.ccnt.spider.parser.WeiboParser;
import cn.edu.zju.ccnt.spider.parser.bean.Page;
import cn.edu.zju.ccnt.spider.queue.VisitedWeiboUrlQueue;
import cn.edu.zju.ccnt.spider.queue.WeiboUrlQueue;
import cn.edu.zju.ccnt.spider.utils.Constants;
import cn.edu.zju.ccnt.spider.utils.FetcherType;
import cn.edu.zju.ccnt.spider.utils.Utils;

public class WeiboFetcher {
	private static final Logger Log = Logger.getLogger(WeiboFetcher.class.getName());
	
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
				
				// 新浪抽风，孟非有几页不显示微博，也没pagelist但是之后几页又出现了！比如78页
				if((content.contains("<div class=\"c\">他还没发过微博.</div>") || content.contains("<div class=\"c\">她还没发过微博.</div>")) && (!url.contains("page=1&")) ){	
					url = url.split("&gsid")[0];
					Utils.handleAbnormalWeibo(content, url);
					return new Page(Constants.SYSTEM_EMPTY + "|" + url, null);
				}
				else{
					String returnMsg = Utils.checkContent(content, url, FetcherType.WEIBO);
					if(returnMsg != null){
						return new Page(returnMsg, null);
					}
				}

				// 将content字符串转换成Document对象
				contentDoc = WeiboParser.getPageDocument(content);
				// 判断是否符合下载网页源代码到本地的条件
				List<Element> weiboItems = WeiboParser.getGoalContent(contentDoc);
				if(weiboItems != null && weiboItems.size() > 0){
					WeiboParser.createFile(weiboItems, url);
				}				
			}
		}
		catch(Exception e){
			Log.error(e);
			
			// 处理超时，和请求忙相同
			url = url.split("&gsid")[0];
			Log.info(">> Put back url: " + url);
			WeiboUrlQueue.addFirstElement(url);
			return new Page(Constants.SYSTEM_BUSY, null);
		}
		
		VisitedWeiboUrlQueue.addElement(url);
		return new Page(content, contentDoc);
	}
}
