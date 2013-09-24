package cn.edu.zju.ccnt.spider.handler;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.protocol.HttpContext;

public class ManualRedirectHandler implements RedirectHandler {

	@Override
	public URI getLocationURI(HttpResponse arg0, HttpContext arg1)
			throws ProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRedirectRequested(HttpResponse arg0, HttpContext arg1) {
		// 由于我们需要手动处理所有的redirect，所以直接return false 
		return false;
	} 
}
