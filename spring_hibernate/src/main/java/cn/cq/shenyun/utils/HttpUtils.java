package cn.cq.shenyun.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.cq.shenyun.redis.impl.JedisApiImpl;



public class HttpUtils {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	/**
	 * 
	 * @param url 请求的url地址，
	 * @return Map,
	 * key:
	 * status:1为成功，0为异常，-1为链接超时
	 * response：如果status为1，该字段表示http服务端返回的数据
	 */
	public static Map<String,Object> getUrlContentByGet(String url){
		int status=0;
		String res="";
		int statusCode = -1;//连接返回状态值
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,  3000);//连接时间s
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,  3000);//数据传输时间6s
        try {
        	 HttpGet httpget = new HttpGet(url);

             // Execute HTTP request
             //System.out.println("executing request " + httpget.getURI());
             HttpResponse response = httpclient.execute(httpget);

             statusCode=response.getStatusLine().getStatusCode();
             if( response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
            	 
	             // Get hold of the response entity
	             HttpEntity entity = response.getEntity();
	
	             // If the response does not enclose an entity, there is no need
	             // to bother about connection release
	             if (entity != null) {
	            	 status=1;
	            	 res=EntityUtils.toString(entity).trim();
	            	 //System.out.println(res);
	            	
	             }
             }
//      } catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
			if(statusCode != HttpStatus.SC_OK){
				//res =ApplicationData.conn_out_time;
				status=-1;
				
			}
		} finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
//        String tt="";
//        JSONObject js=new JSONObject();
//        js.put("status", status);
//        js.put("response", "hh\"ab");
//        //js.put("response", "{\"access_token\":\"nam\"}");
//        System.out.println(js.toString());
//        return js.toString();
        
//        System.out.println("res=  "+res);
//        String aa=res.replaceAll("\"", "\\\\\"").replaceAll("\\{", "\\\\\\{").replaceAll("\\}", "\\\\\\}");
//        System.out.println("aa=  "+aa);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("status", status);
        map.put("response", res);
        //String returnjson="{\"status\":"+status+",\"response\":\""+aa+"\"}";
        return map;
	}
	public static void main(String[] arg){
		//String url="http://127.0.0.1:8083/nutz_mvc/login";
		//String url="http://123.125.115.37:8200/tbliveact/api/reduceTdou";
		String url="http://wxs6.kelepuzi.com/mobile.php?act=module&rid=12&name=ewei_comeon&do=help&weid=5";
		Map<String,String> map=new HashMap<String, String>();
		map.put("rid", "12");
		map.put("fansid", "281686");
		for (int i = 0; i <50 ; i++) {
			Map res=getUrlContentByPost(url,map);
			System.out.println(res);
		}


	}
	
	/**
	 * 
	 * @param url 请求的url
	 * @param http_par post传递的参数
	 * @return Map,
	 * key:
	 * status:1为成功，0为异常，-1为链接超时
	 * response：如果status为1，该字段表示http服务端返回的数据
	 */
	public static Map<String,Object> getUrlContentByPost(String url,Map<String,String> http_par){
		int status=0;
		String res="";
		int statusCode = -1;//连接返回状态值
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,  3000);//连接时间s
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,  3000);//数据传输时间6s
        try {
        	 
        	 HttpPost httppost=new HttpPost(url);
             // Execute HTTP request
             //System.out.println("executing request " + httpget.getURI());
        	 // 添加参数  
             List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();  
             //http_par.entrySet()
             if (http_par != null && http_par.keySet().size() > 0) {  
                 Iterator iterator = http_par.entrySet().iterator();  
                 while (iterator.hasNext()) {  
                     Entry entry = (Entry<String, String>) iterator.next();  
                     nvps.add(new BasicNameValuePair((String) entry.getKey(),(String) entry.getValue()));  
                 }  
             }  
             httppost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
             //httppost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
             
             HttpResponse response = httpclient.execute(httppost);

             statusCode=response.getStatusLine().getStatusCode();
             
             if( response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
            	 
	             // Get hold of the response entity
	             HttpEntity entity = response.getEntity();
	
	             // If the response does not enclose an entity, there is no need
	             // to bother about connection release
	             if (entity != null) {
	            	 status=1;
	            	 res=EntityUtils.toString(entity);
	            	 //System.out.println(res);
	            	
	             }
             }
//      } catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
			if(statusCode != HttpStatus.SC_OK){
				//res =ApplicationData.conn_out_time;
				status=-1;
				
			}
		} finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("status", status);
        map.put("response", res);

        return map;
	}
}
