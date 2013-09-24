package cn.edu.zju.ccnt.spider.queue;

import java.util.LinkedList;

import cn.edu.zju.ccnt.spider.parser.bean.Account;

public class AccountQueue {
	public static LinkedList<Account> accountQueue = new LinkedList<Account>();
	
	public synchronized static void addElement(Account account){
		accountQueue.add(account);
	}
	
	public synchronized static Account outElement(){
		return accountQueue.removeFirst();
	}
	
	public synchronized static boolean isEmpty(){
		return accountQueue.isEmpty();
	}
	
	public static int size(){
		return accountQueue.size();
	}
	
	public static boolean isContains(Account account){
		return accountQueue.contains(account);
	}
}
