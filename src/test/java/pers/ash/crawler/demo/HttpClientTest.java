package pers.ash.crawler.demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class HttpClientTest {

	private final String ENCODING = "utf-8";
	@Test
	public void testConnectSSL(){
		
	}
	
	@Test
	public void testPostData() throws Exception{
		//1.初始化客户端
		CloseableHttpClient httpClient = HttpClients.createDefault();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		
		//2.构造数据，发送请求
		HttpPost httpPost = new HttpPost("http://xsscd.xicp.net:85/oa/web/user/login.do"); 
		params.add(new BasicNameValuePair("userName", "xiaolan"));
		params.add(new BasicNameValuePair("password", "000000"));
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, ENCODING);
		httpPost.setEntity(formEntity);
		
		// 3.获取请求数据
		CloseableHttpResponse response = httpClient.execute(httpPost);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {
			print(response);
		}
		//3.1 301 302 303 307 redirect重定向
		if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
				|| statusCode == HttpStatus.SC_MOVED_TEMPORARILY
				|| statusCode == HttpStatus.SC_SEE_OTHER
				|| statusCode == HttpStatus.SC_TEMPORARY_REDIRECT){
			Header header = response.getFirstHeader("Location");
			String redirectUrl = header.getValue();
			if(StringUtils.isBlank(redirectUrl)){
				redirectUrl = "/";
			}
			response = httpClient.execute(new HttpPost(redirectUrl));
			print(response);
		}
		// 4.释放连接资源
		response.close();
		httpClient.close();
	}
	
	public void print(CloseableHttpResponse response) throws Exception{
		HttpEntity httpEntity = response.getEntity();
		if (httpEntity != null) {
			System.out.println("--------------------------------------");
			System.out.println("Response content: "
					+ EntityUtils.toString(httpEntity, ENCODING));
			System.out.println("--------------------------------------");
		}
	}
}
