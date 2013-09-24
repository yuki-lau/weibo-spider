package cn.edu.zju.ccnt.spider.utils;

public class Constants {
	public static String ACCOUNT_FORBIDDEN = "forbidden";
	public static String SYSTEM_BUSY = "busy";
	public static String SYSTEM_EMPTY = "empty";
	public static String OK = "ok";
	
	public static String FORBIDDEN_PAGE = "http://weibo.cn/pub";
	public static String FORBIDDEN_PAGE_TITILE = "<title>微博广场</title>";
	
	// 抓取数据的本地存储路径，根目录
	public static String ROOT_DISK;
	public static String REPOST_LOG_PATH;
	public static String COMMENT_LOG_PATH;
	public static String SWITCH_ACCOUNT_LOG_PATH;
	public static String ACCOUNT_PATH;
	public static String ACCOUNT_RESULT_PATH;
	public static String LOGIN_ACCOUNT_PATH;
	public static String ABNORMAL_ACCOUNT_PATH;
	public static String ABNORMAL_WEIBO_PATH;
	public static String ABNORMAL_WEIBO_CLEANED_PATH;

	public static String REPOST_BASE_STR = "http://weibo.cn/repost/";
	public static String COMMENT_BASE_STR = "http://weibo.cn/comment/";	
	public static String WEIBO_BASE_STR = "http://weibo.cn/u/";
	
	// used for follow
	public static int LEVEL = 3;
	public static int FANS_NO_MORE_THAN = Integer.MAX_VALUE;
	
	
}
