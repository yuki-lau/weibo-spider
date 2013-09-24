package cn.edu.zju.ccnt.spider.worker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import cn.edu.zju.ccnt.spider.utils.Constants;

public class LoginWeiboBackup {
	private static final Logger Log = Logger.getLogger(LoginWeiboBackup.class.getName());
	public static final String DOMAIN = "http://weibo.cn";
	public static final String URL = DOMAIN + "/dpool/ttt/login.php";
	private AbstractHttpClient client = new DefaultHttpClient();
	
	public String loginAndGetCookie(String username, String password){   
        String content = null;
        String gsid = null;
        
        // 由于在登录手机版微博时，会产生一次重定向的过程，因此登录过程分为POST和GET两次请求
        // 第一次，提交登录信息，根据返回头Location获取gid
        // 第二次，将gid拼接到weibo.cn之后，访问首页，获取cookie
        try {
	        // 创建一个HTTP Post请求对象
	        HttpPost httpPost = new HttpPost(URL);
	        
			// 设置HttpPost头
	        httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
	        httpPost.setHeader("Referer", "http://weibo.cn/dpool/ttt/login.php");
	        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
	        
        	// 构造最简单的字符串数据 ，设置请求数据
        	StringEntity reqEntity = new StringEntity("uname=" + encodeAccount(username) + "&pwd=" + password + "&l=&scookie=on");    
            httpPost.setEntity(reqEntity);   

            // 执行post请求
        	HttpResponse response = client.execute(httpPost);
        	HttpEntity entity = response.getEntity();
        	if(entity != null){
        		entity.consumeContent();
			}
        	
            // 微博登录后会被重定向，获取重定向URL
        	String redirectStr = response.getLastHeader("Location").getValue();
        	gsid = redirectStr.substring(redirectStr.indexOf("?") + 1);
        	String redirectUrl = DOMAIN + "?" + gsid;
        	
        	// 创建一个HTTP GET请求对象
            HttpGet httpGet = new HttpGet(redirectUrl);	
    		
            // 设置HTTP GET头
            httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
            httpGet.setHeader("Referer", "http://weibo.cn/dpool/ttt/login.php");
            
            // 执行get请求
        	response = client.execute(httpGet);
        	entity = response.getEntity();
        	if(entity != null){
				// 转化为文本信息, 设置爬取网页的字符集，防止乱码
            	content = EntityUtils.toString(entity, "UTF-8");
			}
        	
        	Log.info(">> login response content: \n" + content);
            Log.info(">> login response cookies: \n" + client.getCookieStore().getCookies().toString());
        }
        catch(ClientProtocolException e){
        	Log.error(e);
			client.getConnectionManager().shutdown();
		}
        catch (UnsupportedEncodingException e) {
        	Log.error(e);
			client.getConnectionManager().shutdown();
		}    
		catch(Exception e){
			Log.error(e);
			client.getConnectionManager().shutdown();
		}
        
        // 账号被禁，跳转到微博广场，所以返回null
        if(content == null || content.contains(Constants.FORBIDDEN_PAGE_TITILE)){
        	return null;
        }
        
        return gsid;
    } 
	
	private static String encodeAccount(String account) {  
		String userName = "";  
		try {  
			userName = URLEncoder.encode(account, "UTF-8");  
		} 
		catch (UnsupportedEncodingException e) {   
			Log.error(e);
		}  
		return userName;  
	} 
}
