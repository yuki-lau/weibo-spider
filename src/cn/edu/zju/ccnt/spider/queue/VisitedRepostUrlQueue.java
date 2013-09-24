package cn.edu.zju.ccnt.spider.queue;

import java.util.HashSet;

/**
 * 已访问url队列
 * @author yuki
 *
 */
public class VisitedRepostUrlQueue {
	public static HashSet<String> visitedRepostUrlQueue = new HashSet<String>();
	
	public synchronized static void addElement(String url){
		visitedRepostUrlQueue.add(url);
	}
	
	public synchronized static boolean isContains(String url){
		return visitedRepostUrlQueue.contains(url);
	}
	
	public synchronized static int size(){
		return visitedRepostUrlQueue.size();
	}
}
