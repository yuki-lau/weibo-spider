package cn.edu.zju.ccnt.spider.queue;

import java.util.LinkedList;

/**
 * 未访问的url队列
 * @author yuki
 *
 */
public class RepostUrlQueue {
	// 超链接队列
	public static LinkedList<String> repostUrlQueue = new LinkedList<String>();
	// 队列中对应最多的超链接数量
	public static final int MAX_SIZE = 10000;
	
	public synchronized static void addElement(String url){
		repostUrlQueue.add(url);
	}
	
	public synchronized static void addFirstElement(String url){
		repostUrlQueue.addFirst(url);
	}
	
	public synchronized static String outElement(){
		return repostUrlQueue.removeFirst();
	}
	
	public synchronized static boolean isEmpty(){
		return repostUrlQueue.isEmpty();
	}
	
	public static int size(){
		return repostUrlQueue.size();
	}
	
	public static boolean isContains(String url){
		return repostUrlQueue.contains(url);
	}
}
