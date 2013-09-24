package cn.edu.zju.ccnt.spider.worker;

import java.io.IOException;
import java.util.Date;

import org.apache.http.client.CookieStore;
import org.apache.log4j.Logger;

import cn.edu.zju.ccnt.spider.parser.bean.Account;
import cn.edu.zju.ccnt.spider.queue.AccountQueue;
import cn.edu.zju.ccnt.spider.utils.Constants;
import cn.edu.zju.ccnt.spider.utils.LogType;
import cn.edu.zju.ccnt.spider.utils.Utils;

public abstract class BasicWorker {
	private static final Logger Log = Logger.getLogger(BasicWorker.class.getName());
	
	protected String username = null;
	protected String password = null;
	
	/**
	 * 下载对应页面并分析出页面对应URL，放置在未访问队列中
	 * @param url
	 * 
	 * 返回值：被封账号/系统繁忙/OK
	 * 
	 */
	protected abstract String dataHandler(String url);
	
	/**
	 * 根据处理结果进行分析需要执行的动作，并返回合法的gsid
	 * @param result
	 * @param gsid
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	protected String process(String result, String gsid) throws InterruptedException, IOException{
		// 处理成功，未被冻结账号，停顿200ms，防止频繁访问被快速冻结账号
		if(result.equals(Constants.OK)){	
			Thread.sleep(200);
		}
		// 系统繁忙
		else if(result.equals(Constants.SYSTEM_BUSY)){
			Log.info(">> System busy, retry after 5s...");
			Thread.sleep(5 * 1000);
		}
		// 账号被冻结，切换账号继续执行
		else if(result.equals(Constants.ACCOUNT_FORBIDDEN)){
			Log.info(">> " + (new Date()).toString() + ": " + username + " account has been frozen!");						
			// 切换账号
			gsid = switchAccount();
			while(gsid == null){
				// 队列中的所有账号当前均不可用，停顿5分钟，再试
				Thread.sleep(5 * 60 * 1000);
				gsid = switchAccount();
			}
		}
		
		return gsid;
	}
	
	protected CookieStore process(String result, CookieStore cookie) throws InterruptedException, IOException{
		// 处理成功，未被冻结账号，停顿200ms，防止频繁访问被快速冻结账号
		if(result.equals(Constants.OK)){	
			Thread.sleep(200);
		}
		// 系统繁忙
		else if(result.equals(Constants.SYSTEM_BUSY)){
			Log.info(">> System busy, retry after 5s...");
			Thread.sleep(5 * 1000);
		}
		// 账号被冻结，切换账号继续执行
		else if(result.equals(Constants.ACCOUNT_FORBIDDEN)){
			Log.info(">> " + (new Date()).toString() + ": " + username + " account has been frozen!");						
			// 切换账号
			cookie = switchAccountForCookie();
			while(cookie == null){
				// 队列中的所有账号当前均不可用，停顿5分钟，再试
				Thread.sleep(5 * 60 * 1000);
				cookie = switchAccountForCookie();
			}
		}
		
		return cookie;
	}
	
	/**
	 * 根据用户名和密码登录微博手机版，并返回维护会话的gsid
	 * 登录失败时返回null
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public String login(String username, String password){
		return (new LoginWeibo()).loginAndGetGsid(username, password);
	}
	
	/**
	 * 根据用户名和密码登录微博手机版，并返回维护会话的cookie
	 * 登录失败时返回null
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public CookieStore loginForCookie(String username, String password){
		return (new LoginWeibo()).loginAndGetCookie(username, password);
	}
	
	/**
	 * 切换账户并登录
	 * @return
	 * @throws IOException 
	 */
	public String switchAccount() throws IOException{
		Account account = null;
		String gsid = null;
		do{
			// 从队列头取出一个账户，并将其添加到队尾等待下一次使用
			account = AccountQueue.outElement();
			AccountQueue.addElement(account);
			// 使用切换账号登录
			gsid = login(account.getUsername(), account.getPassword());
			// 登录成功，退出循环
			if(gsid != null){
				this.username = account.getUsername();
				this.password = account.getPassword();
				String logStr = "Switch to account: " + account.getUsername() + " success!";
				Utils.writeLog(LogType.SWITCH_ACCOUNT_LOG, logStr);
				break;
			}
			String logStr = "Switch to account: " + account.getUsername() + " failed!";
			Utils.writeLog(LogType.SWITCH_ACCOUNT_LOG, logStr);
		}
		// 如果取出的账号与当前的账号相同，则退出，表明队列中所有的账号都被试用一圈，均不可用
		while(!account.getUsername().equals(username));
		
		return gsid;
	}
	
	/**
	 * 切换账户并登录
	 * @return
	 * @throws IOException 
	 */
	public CookieStore switchAccountForCookie() throws IOException{
		Account account = null;
		CookieStore cookie = null;
		do{
			// 从队列头取出一个账户，并将其添加到队尾等待下一次使用
			account = AccountQueue.outElement();
			AccountQueue.addElement(account);
			// 使用切换账号登录
			cookie = loginForCookie(account.getUsername(), account.getPassword());
			// 登录成功，退出循环
			if(cookie != null){
				this.username = account.getUsername();
				this.password = account.getPassword();
				String logStr = "Switch to account: " + account.getUsername() + " success!";
				Utils.writeLog(LogType.SWITCH_ACCOUNT_LOG, logStr);
				break;
			}
			String logStr = "Switch to account: " + account.getUsername() + " failed!";
			Utils.writeLog(LogType.SWITCH_ACCOUNT_LOG, logStr);
		}
		// 如果取出的账号与当前的账号相同，则退出，表明队列中所有的账号都被试用一圈，均不可用
		while(!account.getUsername().equals(username));
		
		return cookie;
	}
	
}
