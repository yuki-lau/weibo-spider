package cn.edu.zju.ccnt.spider.worker;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.edu.zju.ccnt.spider.utils.Constants;

public class LoginWeibo {
	
	private static final Logger Log = Logger.getLogger(LoginWeibo.class.getName());
	public static final String LOGIN_HOST = "login.weibo.cn";
	public static final String LOGIN_URL = "http://login.weibo.cn/login/";
	public static final String PROFILE_URL = "http://weibo.cn/";
	private AbstractHttpClient client = new DefaultHttpClient();
	
	public String loginAndGetGsid(String username, String password){   
        String content = null;
        String gsid = null;
        
        // 由于在登录手机版微博时，会产生一次重定向的过程，因此登录过程分为POST和GET两次请求。
        // 此外POST时的password name是动态的，因此要先GET一次页面，获取password name，共三次请求。
        // 第一次，GET 页面，获取password name和action参数
        // 第二次，提交登录信息，根据返回头Location获取gsid
        // 第三次，将gid拼接到weibo.cn之后，访问首页，获取cookie
        try {
        	// 1. 创建GET请求，获取动态的Password Name和action参数，并设置PostForm值
	        String pwdName = null;
	        String action = null;
	        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
	        
	        // 创建GET请求
        	HttpGet pwdNameGet = new HttpGet(LOGIN_URL);
        	
        	// 设置请求头
        	pwdNameGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
    		
        	// 执行
        	HttpResponse pwdNameResponse = client.execute(pwdNameGet);
    		HttpEntity pwdNameEntity = pwdNameResponse.getEntity();	
    			
    		if(pwdNameEntity != null){
				content = EntityUtils.toString(pwdNameEntity, "UTF-8");
    			Document doc = Jsoup.parse(content);
    			Elements inputs = doc.getElementsByTag("input");
    			Element form = doc.getElementsByTag("form").get(0);
    			
    			for(int i = 0; i < inputs.size(); i++){
    				
    				Element input = inputs.get(i);
    				
    				// 从input组件中筛选出密码框，并获取password name
    				if(input.attr("type").equalsIgnoreCase("password") 
    						&& input.attr("name").startsWith("password_")){
    					pwdName = input.attr("name");
    				}
    				// 将隐藏域放入Form
    				else if(input.attr("type").equalsIgnoreCase("hidden")){
    					formParams.add(new BasicNameValuePair(input.attr("name"), input.attr("value")));
    				}
    			}
    			
    			// 从form中获取action参数
    			action = form.attr("action");
    		}
        	
    		if(pwdName == null){
    			Log.error("There is no password name! exit...");
    			return null;
    		}
    		
    		// 完成Form的数据填充
    		formParams.add(new BasicNameValuePair("mobile", username));
    		formParams.add(new BasicNameValuePair(pwdName, password));
    		formParams.add(new BasicNameValuePair("submit", "登录"));
    		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formParams, "UTF-8");
    		
	        // 2. 创建一个HTTP Post请求对象，提交登录信息
	        HttpPost loginPost = new HttpPost(LOGIN_URL + action);
	        
	        // 设置请求头
	        loginPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
	        loginPost.setHeader("Referer", LOGIN_URL);
	        loginPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        	// 设置请求数据  
	        loginPost.setEntity(formEntity);
	        
            // 执行post请求
        	HttpResponse response = client.execute(loginPost);
        	HttpEntity entity = response.getEntity();
        	if(entity != null){
        		entity.consumeContent();
			}
        	
            // 微博登录后会被重定向，获取重定向URL中的gisd，用于后续登录
        	String redirectStrParams = response.getLastHeader("Location").getValue().split("\\?")[1];
        	String[] paramArray = redirectStrParams.split("&");
        	for(int i = 0; i < paramArray.length; i++){
        		if(paramArray[i].startsWith("g=")){
        			gsid = "gsid=" + paramArray[i].split("=")[1];
        			break;
        		}
        	}	
        	
        	// 3. 创建一个HTTP GET请求对象
            HttpGet httpGet = new HttpGet(PROFILE_URL + "?" + gsid);	
    		
            // 设置HTTP GET头
            httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
            httpGet.setHeader("Referer", LOGIN_URL);
            
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
	
	public CookieStore loginAndGetCookie(String username, String password){   
        String content = null;
        String gsid = null;
        CookieStore cookie = null;
        
        // 由于在登录手机版微博时，会产生一次重定向的过程，因此登录过程分为POST和GET两次请求。
        // 此外POST时的password name是动态的，因此要先GET一次页面，获取password name，共三次请求。
        // 第一次，GET 页面，获取password name和action参数
        // 第二次，提交登录信息，根据返回头Location获取gsid
        // 第三次，将gid拼接到weibo.cn之后，访问首页，获取cookie
        try {
        	// 1. 创建GET请求，获取动态的Password Name和action参数，并设置PostForm值
	        String pwdName = null;
	        String action = null;
	        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
	        
	        // 创建GET请求
        	HttpGet pwdNameGet = new HttpGet(LOGIN_URL);
        	
        	// 设置请求头
        	pwdNameGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
    		
        	// 执行
        	HttpResponse pwdNameResponse = client.execute(pwdNameGet);
    		HttpEntity pwdNameEntity = pwdNameResponse.getEntity();	
    			
    		if(pwdNameEntity != null){
				content = EntityUtils.toString(pwdNameEntity, "UTF-8");
    			Document doc = Jsoup.parse(content);
    			Elements inputs = doc.getElementsByTag("input");
    			Element form = doc.getElementsByTag("form").get(0);
    			
    			for(int i = 0; i < inputs.size(); i++){
    				
    				Element input = inputs.get(i);
    				
    				// 从input组件中筛选出密码框，并获取password name
    				if(input.attr("type").equalsIgnoreCase("password") 
    						&& input.attr("name").startsWith("password_")){
    					pwdName = input.attr("name");
    				}
    				// 将隐藏域放入Form
    				else if(input.attr("type").equalsIgnoreCase("hidden")){
    					formParams.add(new BasicNameValuePair(input.attr("name"), input.attr("value")));
    				}
    			}
    			
    			// 从form中获取action参数
    			action = form.attr("action");
    		}
        	
    		if(pwdName == null){
    			Log.error("There is no password name! exit...");
    			return null;
    		}
    		
    		// 完成Form的数据填充
    		formParams.add(new BasicNameValuePair("mobile", username));
    		formParams.add(new BasicNameValuePair(pwdName, password));
    		formParams.add(new BasicNameValuePair("submit", "登录"));
    		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formParams, "UTF-8");
    		
	        // 2. 创建一个HTTP Post请求对象，提交登录信息
	        HttpPost loginPost = new HttpPost(LOGIN_URL + action);
	        
	        // 设置请求头
	        loginPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
	        loginPost.setHeader("Referer", LOGIN_URL);
	        loginPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        	// 设置请求数据  
	        loginPost.setEntity(formEntity);
	        
            // 执行post请求
        	HttpResponse response = client.execute(loginPost);
        	HttpEntity entity = response.getEntity();
        	if(entity != null){
        		entity.consumeContent();
			}
        	
            // 微博登录后会被重定向，获取重定向URL中的gisd，用于后续登录
        	String redirectStrParams = response.getLastHeader("Location").getValue().split("\\?")[1];
        	String[] paramArray = redirectStrParams.split("&");
        	for(int i = 0; i < paramArray.length; i++){
        		if(paramArray[i].startsWith("g=")){
        			gsid = "gsid=" + paramArray[i].split("=")[1];
        			break;
        		}
        	}	
        	
        	// 3. 创建一个HTTP GET请求对象
            HttpGet httpGet = new HttpGet(PROFILE_URL + "?" + gsid);	
    		
            // 设置HTTP GET头
            httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
            httpGet.setHeader("Referer", LOGIN_URL);
            
            // 执行get请求
        	response = client.execute(httpGet);
        	entity = response.getEntity();
        	if(entity != null){
				// 转化为文本信息, 设置爬取网页的字符集，防止乱码
            	content = EntityUtils.toString(entity, "UTF-8");
			}
        	
        	cookie = client.getCookieStore();
        	Log.info(">> login response content: \n" + content);
            Log.info(">> login response cookies: \n" + cookie.getCookies().toString());
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
        
        return cookie;
    } 
}
