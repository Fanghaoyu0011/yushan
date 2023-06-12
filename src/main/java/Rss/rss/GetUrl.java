package Rss.rss;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetUrl {
	 public static String sendGet(String url) {
	        String result = "";
	        BufferedReader in = null;
	        try {
	            String urlNameString = url;
	            URL realUrl = new URL(urlNameString);
	            // 打开和URL之间的连接
	            URLConnection connection = realUrl.openConnection();
	            connection.setConnectTimeout(10000);
	            connection.setReadTimeout(10000);
	            connection.setRequestProperty("User-Agent", "Mozilla/4.76");
	            // 建立实际的连接
	            connection.connect();
	            // 定义 BufferedReader输入流来读取URL的响应
	            InputStream inStream = connection.getInputStream();
	            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	            byte[] buffer = new byte[1024];
	            int len = 0;
	            while ((len = inStream.read(buffer)) != -1) {
	                outStream.write(buffer, 0, len);
	            }
	            byte[] data = outStream.toByteArray();
	            outStream.close();
	            inStream.close();
	            result = new String(data);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        // 使用finally块来关闭输入流
	        finally {
	            try {
	                if (in != null) {
	                    in.close();
	                }
	            } catch (Exception e2) {
	                e2.printStackTrace();
	            }
	        }
	        return result;
	    }
}
