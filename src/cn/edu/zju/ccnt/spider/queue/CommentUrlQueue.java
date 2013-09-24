package cn.edu.zju.ccnt.spider.queue;

import java.util.LinkedList;

/**
 * 未访问的url队列
 * @author yuki
 *
 */
public class CommentUrlQueue {
	// 超链接队列
	public static LinkedList<String> commentUrlQueue = new LinkedList<String>();
	// 队列中对应最多的超链接数量
	public static final int MAX_SIZE = 10000;
	
	public synchronized static void addElement(String url){
		commentUrlQueue.add(url);
	}
	
	public synchronized static void addFirstElement(String url){
		commentUrlQueue.addFirst(url);
	}
	
	public synchronized static String outElement(){
		return commentUrlQueue.removeFirst();
	}
	
	public synchronized static boolean isEmpty(){
		return commentUrlQueue.isEmpty();
	}
	
	public static int size(){
		return commentUrlQueue.size();
	}
	
	public static boolean isContains(String url){
		return commentUrlQueue.contains(url);
	}
}
