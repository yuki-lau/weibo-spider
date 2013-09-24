package cn.edu.zju.ccnt.spider.worker.impl;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import cn.edu.zju.ccnt.spider.fetcher.RepostFetcher;
import cn.edu.zju.ccnt.spider.handler.NextUrlHandler;
import cn.edu.zju.ccnt.spider.parser.RepostParser;
import cn.edu.zju.ccnt.spider.parser.bean.Account;
import cn.edu.zju.ccnt.spider.queue.AccountQueue;
import cn.edu.zju.ccnt.spider.queue.RepostUrlQueue;
import cn.edu.zju.ccnt.spider.utils.Utils;
import cn.edu.zju.ccnt.spider.worker.BasicWorker;

/**
 * 从UrlQueue中取出url，下载页面，分析url，保存已访问rul
 * @author yuki
 *
 */
public class UrlRepostWorker extends BasicWorker implements Runnable {
	private static final Logger Log = Logger.getLogger(UrlRepostWorker.class.getName());
	/**
	 * 下载对应页面并分析出页面对应URL，放置在未访问队列中
	 * @param url
	 * 
	 * 返回值：被封账号/系统繁忙/OK
	 */
	protected String dataHandler(String url){
		return NextUrlHandler.addNextRepostUrl(RepostFetcher.getContentFromUrl(url));
	}
	
	@Override
	public void run() {
		// 首先获取账号并登录
		Account account = AccountQueue.outElement();
		AccountQueue.addElement(account);
		this.username = account.getUsername();
		this.password = account.getPassword();
		
		// 使用账号登录
		String gsid = login(username, password);
		String result = null;
		
		try {
			// 若登录失败，则执行一轮切换账户的操作，如果还失败，则退出
			if(gsid == null){
				gsid = switchAccount();
			}
			
			// 登录成功
			if(gsid != null) {
				// 当URL队列不为空时，从未访问队列中取出url进行分析
				while(!RepostUrlQueue.isEmpty()) {
					// 从队列中获取URL并处理
					result = dataHandler(RepostUrlQueue.outElement() + "&" + gsid);
					
					// 针对处理结果进行处理：OK, SYSTEM_BUSY, ACCOUNT_FORBIDDEN
					gsid = process(result, gsid);

					// 没有新的URL了，从数据库中继续拿一个
					if(RepostUrlQueue.isEmpty()){
						// 仍为空，从数据库中取
						if(RepostUrlQueue.isEmpty()){
							Log.info(">> Add new repost Url...");
							Utils.initializeRepostUrl();
							
							// 拿完还是空，退出爬虫
							if(RepostUrlQueue.isEmpty()){
								Log.info(">> All reposts of all weibos have been fetched...");
								break;
							}
						}
					}
				}
			}
			else{
				Log.info(">> " + username + " login failed!");
			}
		}
		catch (InterruptedException e) {
			Log.error(e);
		}
		catch (IOException e) {
			Log.error(e);
		}
		
		// 关闭数据库连接
		try {
			RepostParser.conn.close();
			Utils.conn.close();
		} 
		catch (SQLException e) {
			Log.error(e);
		}
		
		Log.info("Spider stop...");
	}
}
