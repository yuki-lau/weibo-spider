package cn.edu.zju.ccnt.spider.parser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.edu.zju.ccnt.spider.parser.bean.Repost;
import cn.edu.zju.ccnt.spider.utils.DBConn;
import cn.edu.zju.ccnt.spider.utils.Utils;

public class RepostParser {
	private static final Logger Log = Logger.getLogger(RepostParser.class.getName());
	public static Connection conn = DBConn.getConnection();
	
	public static Document getPageDocument(String content){
		return Jsoup.parse(content);
	}
	
	// 截取网页网页源文件的目标内容
	public static List<Element> getGoalContent(Document doc) {
		List<Element> repostItems = new ArrayList<Element>();

		//转发的情况有所不同，每一条转发没有ID,class=c还包括:
		//原微博的内容和微博内容下的分界线，还有一头一尾的两个返回作者微博首页的链接
		//因此要过滤掉前三条和最后一条
		Elements elements = doc.getElementsByClass("c");
		for(int i = 0; i < elements.size(); i++){
			if(i==0 || i==1 || i==2 || i == elements.size() - 1){
				continue;
			}
			else {
				repostItems.add(elements.get(i));
			}
		}
		
		return repostItems;
	}

	// 解析每一条转发的结构，创建Repost对象
	public static Repost parse(Element repostEl, String weiboID){
		Repost repost = new Repost();
		try {
			//一部分人的ID并不是数字串，而是个性域名，这部分也尚待处理
			//如：/u/123245 和 /kaifulee
			String tempAuthor = repostEl.getElementsByAttribute("href").get(0).attr("href");
			repost.setAuthor(tempAuthor.substring(tempAuthor.lastIndexOf("/") + 1, tempAuthor.lastIndexOf("?")));
			
			//获取一条评论内的有效内容，包括@的人
			//因为内容和时间的标签和发布者的标签并列在一起，所以要截前后片段
			String tempContent = repostEl.toString();
			String tempContentString = tempContent.substring(tempContent.indexOf(">:") + 2,tempContent.indexOf("<span class="));
			repost.setContent(tempContentString.substring(0, tempContentString.indexOf("&nbsp")));
			
            //获取时间
			repost.setTime(Utils.parseDate(repostEl.getElementsByClass("ct").get(0).text().split("来自")[0]));

		}
		catch(Exception e){
			repost = null;
			Log.error("Not a valid repost item: " + repostEl);
		}
		
		return repost;
	}
		
	// 将抓取的微博信息保存至本地文件
 	public static void createFile(List<Element> repostItems, String urlPath) {
		String weiboID = Utils.getUserIdFromUrl(urlPath);
		
		// 解析每一条转发，提取各部分内容，并写入数据库
//		Connection conn = DBConn.getConnection();
		PreparedStatement ps;
		try{
			ps = conn.prepareStatement("INSERT INTO repost (weiboID, poster, content, postTime) VALUES (?, ?, ?, ?)");
			for(int i = 0; i < repostItems.size(); i++){
				Repost repost = RepostParser.parse(repostItems.get(i), weiboID);
				if(repost != null){
					ps.setString(1, weiboID);
					ps.setString(2, repost.getAuthor());
					ps.setString(3, repost.getContent());
					ps.setString(4, repost.getTime());
					ps.execute();
					Log.info("Succesfully Import One Repost:" + repost.getContent());
				}				
			}
			ps.close();
		} 
		catch (SQLException e) {
			Log.error(e);
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

}
