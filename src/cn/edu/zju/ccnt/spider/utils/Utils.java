package cn.edu.zju.ccnt.spider.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.edu.zju.ccnt.spider.parser.bean.Account;
import cn.edu.zju.ccnt.spider.queue.AbnormalAccountUrlQueue;
import cn.edu.zju.ccnt.spider.queue.AccountQueue;
import cn.edu.zju.ccnt.spider.queue.CommentUrlQueue;
import cn.edu.zju.ccnt.spider.queue.FollowUrlQueue;
import cn.edu.zju.ccnt.spider.queue.RepostUrlQueue;
import cn.edu.zju.ccnt.spider.queue.VisitedCommentUrlQueue;
import cn.edu.zju.ccnt.spider.queue.VisitedFollowUrlQueue;
import cn.edu.zju.ccnt.spider.queue.VisitedRepostUrlQueue;
import cn.edu.zju.ccnt.spider.queue.VisitedWeiboUrlQueue;
import cn.edu.zju.ccnt.spider.queue.WeiboUrlQueue;

public class Utils {
	private static SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final Logger Log = Logger.getLogger(Utils.class.getName());
	public static Connection conn = DBConn.getConnection();
	
	/**
	 * 检测字符串是否为null，或空字符串
	 * @param str
	 * @return
	 */
	public static boolean isEmptyStr(String str){
		if(str == null || str.trim().length() == 0){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 将微博时间字符串转换成yyyyMMddHHmmSS
	 * 微博时间字符串有：
	 * 		xx分钟前
	 * 		今天 11:53 
	 * 		07月09日 13:36
	 * 		2010-09-23 19:55:38
	 * 		
	 * @param weiboTimeStr
	 * @return
	 */
	public static String parseDate(String weiboTimeStr){
		
		Calendar currentTime = Calendar.getInstance();//使用默认时区和语言环境获得一个日历。 
		
		if(weiboTimeStr.contains("分钟前")){
			int minutes = Integer.parseInt(weiboTimeStr.split("分钟前")[0]);
			
			currentTime.add(Calendar.MINUTE, -minutes);//取当前日期的前一天. 
			
			return simpleDateTimeFormat.format(currentTime.getTime());
		}
		else if(weiboTimeStr.startsWith("今天")){
			String[] time = weiboTimeStr.split("天")[1].split(":");
			int hour = Integer.parseInt(time[0].substring(1));
			int minute = Integer.parseInt(time[1].substring(0, 2));
			
			currentTime.set(Calendar.HOUR_OF_DAY, hour);
			currentTime.set(Calendar.MINUTE, minute);
			
			return simpleDateTimeFormat.format(currentTime.getTime());
		}
		else if(weiboTimeStr.contains("月")){
			String[] time = weiboTimeStr.split("日")[1].split(":");
			int dayIndex = weiboTimeStr.indexOf("日") - 2;
			int month = Integer.parseInt(weiboTimeStr.substring(0, 2));
			int day = Integer.parseInt(weiboTimeStr.substring(dayIndex, dayIndex + 2));
			int hour = Integer.parseInt(time[0].substring(1));
			int minute = Integer.parseInt(time[1].substring(0, 2));
			
			currentTime.set(Calendar.MONTH, month - 1);
			currentTime.set(Calendar.DAY_OF_MONTH, day);
			currentTime.set(Calendar.HOUR_OF_DAY, hour);
			currentTime.set(Calendar.MINUTE, minute);
			
			return simpleDateTimeFormat.format(currentTime.getTime());
		}
		else if(weiboTimeStr.contains("-")){
			return weiboTimeStr.replace("-", "").replace(":", "").replace(" ", "").substring(0, 14);
		}
		else{
			Log.info(">> Error: Unknown time format - " + weiboTimeStr);
		}
		
		return null;
	}
	
	/**
	 * 根据logType将日志写入相应的文件
	 * @param logType
	 * @param logStr
	 */
	public static void writeLog(int logType, String logStr){
		// 选取log类型
		String filePath = null;
		switch(logType){
			case LogType.SWITCH_ACCOUNT_LOG:
				filePath = Constants.SWITCH_ACCOUNT_LOG_PATH;
				break;
			case LogType.COMMENT_LOG:
				filePath = Constants.COMMENT_LOG_PATH;
				break;
			case LogType.REPOST_LOG:
				filePath = Constants.REPOST_LOG_PATH;
				break;
			case LogType.WEIBO_LOG:
				filePath = Constants.ABNORMAL_WEIBO_PATH;
				break;
			default:
				return;
		}
		
		// 写入日志
		try {
			FileWriter fileWriter = new FileWriter(filePath, true);
			if(logType == LogType.WEIBO_LOG){
				fileWriter.write(logStr + "\r\n");
			}
			else{
				fileWriter.write((new Date()).toString() + ": " + logStr + "\r\n");
			}
	        fileWriter.flush();
			fileWriter.close();
		} 
		catch (IOException e) {
			Log.error(e);
		}
	}

	/**
	 * 将异常账号写入文件
	 * @param account
	 * @throws IOException
	 */
	public static void writeAbnormalAccount(String account) throws IOException{
		FileWriter fileWriter = new FileWriter(Constants.ABNORMAL_ACCOUNT_PATH, true);
        fileWriter.write(account + "\r\n");
        fileWriter.flush();
		fileWriter.close();
	}

	// 从url中解析出当前用户的ID
	public static String getUserIdFromUrl(String url) {
		int startIndex = url.lastIndexOf("/");
		int endIndex = url.indexOf("?");
		
		if(endIndex == -1){
			return url.substring(startIndex + 1); 
		}
		return url.substring(startIndex + 1,  endIndex);
	}
	
	// 从follow url中解析出当前用户的ID
	public static String getUserIdFromFollowUrl(String url) {
		int startIndex = 16;
		int endIndex = url.indexOf("/follow");

		return url.substring(startIndex,  endIndex);
	}
	
	// http://tp2.sinaimg.cn/2826608265/50/5667697175/1
	public static String getUserIdFromImgUrl(String url) {
		int startIndex = url.indexOf("sinaimg.cn/") + "sinaimg.cn/".length();
		String subStr = url.substring(startIndex);

		return subStr.substring(0, subStr.indexOf("/"));
	}
	
	/**
	 * 从login_account.txt中读取爬虫账号，作为账号队列
	 * 格式：account----email----password
	 */
	public static void readAccountFromFile(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(Constants.LOGIN_ACCOUNT_PATH));
			String accountLine = null;
			while(((accountLine = reader.readLine()) != null)){
				String[] account = accountLine.split("----");
				AccountQueue.addElement(new Account(account[0], account[2]));
			}
			reader.close();
		} 
		catch (FileNotFoundException e) {
			Log.error(e);
		}
		catch (IOException e) {
			Log.error(e);
		}
	}
	
	/**
	 * 数据库中读取用户账号，并生成第一页微博的url，放入WeiboUrlQueue
	 */
	public static synchronized void initializeWeiboUrl(){
		String querySql = "SELECT accountID FROM USER WHERE isFetched = 0 ORDER BY id LIMIT 1";
//		Connection conn = DBConn.getConnection();
		PreparedStatement ps = null;
		Statement st = null;
		ResultSet rs = null;
		String accountID = null;
		
		try {
			conn.setAutoCommit(false); 
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			
			st = conn.createStatement();
			rs = st.executeQuery(querySql);		
			if(rs.next()){
				accountID = rs.getString("accountID");
				ps = conn.prepareStatement("UPDATE USER SET isFetched = 1 WHERE accountID = ?");
				ps.setString(1, accountID);
				ps.execute();
				ps.close();
			}
			rs.close();
			st.close();
			
			conn.commit();
			if(accountID != null){
				// 提交成功后，再放入队列
				WeiboUrlQueue.addElement(Constants.WEIBO_BASE_STR + accountID + "?page=1");
			}
		} 
		catch (SQLException e) {
			Log.error(e);
			// 提交失败 roll back，并将放入队列的URL拿出来
			try {
				conn.rollback();
			} 
			catch (SQLException e1) {
				Log.error(e1);
			}
		}
		finally{
//			try {
//				conn.close();
//			} 
//			catch (SQLException e) {
//				Log.error(e);
//			}
		}
	}
	
	/**
	 * 数据库中读取用微博账号，并生成第一页评论的url，放入CommentUrlQueue
	 */
	public static synchronized void initializeCommentUrl(){
		String querySql = "SELECT weiboID FROM weibo WHERE isCommentFetched = 0 LIMIT 1";
//		Connection conn = DBConn.getConnection();
		PreparedStatement ps = null;
		Statement st = null;
		ResultSet rs = null;
		String weiboID = null;
		
		try {
			conn.setAutoCommit(false); 
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			
			st = conn.createStatement();
			rs = st.executeQuery(querySql);	
			if(rs.next()){
				weiboID = rs.getString("weiboID");
				ps = conn.prepareStatement("UPDATE weibo SET isCommentFetched = 1 WHERE weiboID = ?");
				ps.setString(1, weiboID);
				ps.execute();
				ps.close();
			}
			rs.close();
			st.close();
			
			conn.commit();
			if(weiboID != null){
				// 提交成功后，再放入队列
				CommentUrlQueue.addElement(Constants.COMMENT_BASE_STR + weiboID + "?page=1");
			}
		} 
		catch (SQLException e) {
			Log.error(e);
			// 提交失败 roll back，并将放入队列的URL拿出来
			try {
				conn.rollback();
			} 
			catch (SQLException e1) {
				Log.error(e1);
			}
		}
		finally{
//			try {
//				conn.close();
//			} 
//			catch (SQLException e) {
//				Log.error(e);
//			}
		}
	}
	
	/**
	 * 数据库中读取微博账号，并生成第一页转发的url，放入WeiboUrlQueue
	 */
	public static synchronized void initializeRepostUrl(){
		String querySql = "SELECT weiboID FROM weibo WHERE isRepostFetched = 0 LIMIT 1";
//		Connection conn = DBConn.getConnection();
		PreparedStatement ps = null;
		Statement st = null;
		ResultSet rs = null;
		String weiboID = null;
		
		try {
			conn.setAutoCommit(false); 
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			
			st = conn.createStatement();
			rs = st.executeQuery(querySql);		
			if(rs.next()){
				weiboID = rs.getString("weiboID");
				ps = conn.prepareStatement("UPDATE weibo SET isRepostFetched = 1 WHERE weiboID = ?");
				ps.setString(1, weiboID);
				ps.execute();
				ps.close();
			}
			rs.close();
			st.close();
			
			conn.commit();
			if(weiboID != null){
				// 提交成功后，再放入队列
				RepostUrlQueue.addElement(Constants.REPOST_BASE_STR + weiboID + "?page=1");
			}
		} 
		catch (SQLException e) {
			Log.error(e);
			// 提交失败 roll back，并将放入队列的URL拿出来
			try {
				conn.rollback();
			} 
			catch (SQLException e1) {
				Log.error(e1);
			}
		}
		finally{
//			try {
//				conn.close();
//			} 
//			catch (SQLException e) {
//				Log.error(e);
//			}
		}
	}

	/**
	 * 从account.txt中读取用户账号，并生成用户主页的url，放入AccountInfoUrlQueue
	 */
	public static void initializeAbnormalWeiboUrl(){
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.ABNORMAL_WEIBO_CLEANED_PATH),"utf-8"));

			String accountLine = null;		
			while((accountLine = reader.readLine()) != null){
				WeiboUrlQueue.addElement(accountLine);
			}
			reader.close();
		}
		catch(IOException e){
			Log.error(e);
		}
	}
	
	/**
	 * 从follower_id.txt中读取用户ID，作为关注关系的初始ID列表
	 */
//	public static void initializeFollowUrl(){
//		try{
//			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.FOLLOWER_ID_PATH), "utf-8"));
//
//			String follower = null;		
//			while((follower = reader.readLine()) != null){
//				FollowUrlQueue.addElement("http://weibo.cn/" + follower + "/follow");
//			}
//			reader.close();
//		}
//		catch(IOException e){
//			Log.error(e);
//		}
//	}
	public static int initializeFollowUrl(){
//		String querySql = "SELECT follower, level FROM follower WHERE isFetched = 0 LIMIT 1";
		String querySql = "SELECT follower, LEVEL FROM follower WHERE isFetched = 0 ORDER BY LEVEL ASC LIMIT 1";
		PreparedStatement ps = null;
		Statement st = null;
		ResultSet rs = null;
		String followerID = null;
		int level = Integer.MAX_VALUE;
		
		try {
			// 获取本轮follower，level
			conn.setAutoCommit(false); 
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			
			st = conn.createStatement();
			rs = st.executeQuery(querySql);		
			if(rs.next()){
				followerID = rs.getString("follower");
				level = rs.getInt("level");
				ps = conn.prepareStatement("UPDATE follower SET isFetched = 1 WHERE follower = ?");
				ps.setString(1, followerID);
				ps.execute();
				ps.close();
			}
			rs.close();
			st.close();
			
			conn.commit();
			
			// 当本轮level < Constants.LEVEL，才添加队列URL
			if(level < Constants.LEVEL){
				FollowUrlQueue.addElement("http://weibo.cn/" + followerID + "/follow");
			}
		} 
		catch (SQLException e) {
			Log.error(e);
			
			// 提交失败 roll back
			try {
				conn.rollback();
			} 
			catch (SQLException e1) {
				Log.error(e1);
			}
		}
		finally{

		}
		
		return level;
	}
	
	public static synchronized void addNextLevelFollower(int currentLevel){
		
		String querySql = "SELECT DISTINCT followee FROM follow WHERE LEVEL = ? AND followee NOT IN(SELECT DISTINCT follower FROM follow )";
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = conn.prepareStatement(querySql);
			ps.setInt(1, currentLevel);
			rs = ps.executeQuery();
			while(rs.next()){
				FollowUrlQueue.addElement("http://weibo.cn/" + rs.getString("followee") + "/follow");
			}
			rs.close();
			ps.close();
		} 
		catch (SQLException e) {
			Log.error(e);
		}
		finally{

		}
	}
	
	public static String checkContent(String content, String url, int fetcherType) throws IOException{
		String returnMsg = null;
		// 检测当前访问的用户是否为异常账号
		if(content.contains("<div class=\"me\">抱歉，您当前访问的用户状态异常，暂时无法访问。</div>") 
				|| content.contains("<div class=\"me\">用户不存在哦!</div>")
				|| content.contains("<div class=\"me\">请求页不存在")){
			
			AbnormalAccountUrlQueue.addElement(url);
			
			Log.info(">> 当前访问的用户是异常账号: " + content);
			Log.info("-----------------------------------");
			
			if(fetcherType == FetcherType.COMMENT){
				Utils.writeAbnormalAccount(Utils.getUserIdFromUrl(url));
				Log.info("抓取到的连接数：" + CommentUrlQueue.size());
				Log.info("已处理的页面数：" + VisitedCommentUrlQueue.size());
			}
			else if(fetcherType == FetcherType.REPOST){
				Utils.writeAbnormalAccount(Utils.getUserIdFromUrl(url));
				Log.info("抓取到的连接数：" + RepostUrlQueue.size());
				Log.info("已处理的页面数：" + VisitedRepostUrlQueue.size());
			}
			else if(fetcherType == FetcherType.WEIBO){
				Utils.writeAbnormalAccount(Utils.getUserIdFromUrl(url));
				Log.info("抓取到的连接数：" + WeiboUrlQueue.size());
				Log.info("已处理的页面数：" + VisitedWeiboUrlQueue.size());
			}
			else if(fetcherType == FetcherType.FOLLOW){
				Utils.writeAbnormalAccount(Utils.getUserIdFromFollowUrl(url));
				Log.info("抓取到的连接数：" + FollowUrlQueue.size());
				Log.info("已处理的页面数：" + VisitedFollowUrlQueue.size());
			}

			Log.info("异常账号数         ：" + AbnormalAccountUrlQueue.size());
			Log.info("----------------------------------");
			returnMsg = Constants.OK;
		}
		// 检测账号是否被冻结
		else if(content.contains(Constants.FORBIDDEN_PAGE_TITILE) 
				|| content.contains("<div class=\"c\">你的微博账号出现异常被暂时冻结!<br/>完成以下操作即可激活你的微博。<br/></div>") 
				|| content.contains("<div class=\"c\">抱歉，你的帐号存在异常，暂时无法访问。<br/>") 
				|| content.contains("<div class=\"c\">您的帐号存在异常，暂时无法访问。<br/>")
				|| content.contains("<div class=\"c\">您的微博帐号出现异常被暂时冻结。<br/>")
				|| content.contains("<div class=\"c\">完成验证后即可开始微博之旅：</div>")){
			// 被暂时冻结账号了，当前url没有处理，移除原gsid，返回队列头部，并返回forbidden
			url = url.split("&gsid")[0];
			Log.info(">> Put back url: " + url);
			Log.info(">> 当前账号被冻结: " + content);
			
			if(fetcherType == FetcherType.COMMENT){
				CommentUrlQueue.addFirstElement(url);
			}
			else if(fetcherType == FetcherType.REPOST){
				RepostUrlQueue.addFirstElement(url);
			}
			else if(fetcherType == FetcherType.WEIBO){
				WeiboUrlQueue.addFirstElement(url);
			}
			else if(fetcherType == FetcherType.FOLLOW){
				FollowUrlQueue.addFirstElement(url);
			}
			
			returnMsg = Constants.ACCOUNT_FORBIDDEN;
		}
		// 监测系统繁忙错误
		else if(content.contains("<div class=\"me\">系统繁忙,请稍后再试!</div>")){					
			// 系统繁忙，当前url没有处理，移除原gsid，返回队列头部，并返回busy
			url = url.split("&gsid")[0];
			Log.info(">> Put back url: " + url);
			Log.info(">> 系统繁忙: " + content);
			
			if(fetcherType == FetcherType.COMMENT){
				CommentUrlQueue.addFirstElement(url);
			}
			else if(fetcherType == FetcherType.REPOST){
				RepostUrlQueue.addFirstElement(url);
			}
			else if(fetcherType == FetcherType.WEIBO){
				WeiboUrlQueue.addFirstElement(url);
			}

			returnMsg = Constants.SYSTEM_BUSY;
		}

		return returnMsg;
	}
	
	public static void handleAbnormalWeibo(String content, String url){
		String[] urlParts = url.split("page=");
		int page = Integer.parseInt(urlParts[1]);
		int weiboNum = Integer.parseInt(content.split("<div class=\"tip2\"><span class=\"tc\">微博\\[")[1].split("\\]")[0]);
		
		if(page * 10 >= weiboNum){
			return;
		}
		else{
			Utils.writeLog(LogType.WEIBO_LOG, url);
			String nextUrl = urlParts[0] + (page + 1);
			WeiboUrlQueue.addElement(nextUrl); 
		}
		
	}
}
