package cn.edu.zju.ccnt.spider.worker.impl;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import cn.edu.zju.ccnt.spider.fetcher.WeiboFetcher;
import cn.edu.zju.ccnt.spider.parser.WeiboParser;
import cn.edu.zju.ccnt.spider.parser.bean.Account;
import cn.edu.zju.ccnt.spider.queue.AbnormalAccountUrlQueue;
import cn.edu.zju.ccnt.spider.queue.AccountQueue;
import cn.edu.zju.ccnt.spider.queue.VisitedWeiboUrlQueue;
import cn.edu.zju.ccnt.spider.queue.WeiboUrlQueue;
import cn.edu.zju.ccnt.spider.utils.Utils;
import cn.edu.zju.ccnt.spider.worker.BasicWorker;

/**
 * 从UrlQueue中取出url，下载页面，分析url，保存已访问rul
 * @author yuki
 *
 */
public class UrlAbnormalWeiboWorker extends BasicWorker implements Runnable {
	private static final Logger Log = Logger.getLogger(UrlAbnormalWeiboWorker.class.getName());
	/**
	 * 下载对应页面并分析出页面对应URL，放置在未访问队列中
	 * @param url
	 * 
	 * 返回值：被封账号/系统繁忙/OK
	 * 
	 */
	protected String dataHandler(String url){
		Log.info("-------------------");
		Log.info("抓取到：" + WeiboUrlQueue.size());
		Log.info("已处理：" + VisitedWeiboUrlQueue.size());
		Log.info("异常数：" + AbnormalAccountUrlQueue.size());
		Log.info("-------------------");
		
		return WeiboFetcher.getContentFromUrl(url).getContent();
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
				while(!WeiboUrlQueue.isEmpty()) {
					// 从队列中获取URL并处理
					result = dataHandler(WeiboUrlQueue.outElement() + "&" + gsid);
					
					// 针对处理结果进行处理：OK, SYSTEM_BUSY, ACCOUNT_FORBIDDEN
					gsid = process(result, gsid);

					// 没有新的URL了，从数据库中继续拿一个
					if(WeiboUrlQueue.isEmpty()){
						Log.info(">> All abnormal weibos have been fetched...");
						break;
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
			WeiboParser.conn.close();
			Utils.conn.close();
		} 
		catch (SQLException e) {
			Log.error(e);
		}
		
		Log.info("Spider stop...");
	}

}
