package Rss.rss;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class HttpFormBakUtil {
	private static final Logger log = LoggerFactory.getLogger(HttpFormBakUtil.class);
	public static String doPost(String url, Map<String, String> params, Map<String, String> header,int timeout) throws Exception {
        String body = null;
        try {
            // Post请求
             log.info("{} protocol: POST,url:{}", url);
            //System.out.println("protocol: POST,url:" + url);
            HttpPost httpPost = new HttpPost(url.trim());
            // 设置参数
            // log.debug("params:{} ", JSONUtil.toJsonStr(params));
            httpPost.setEntity(new UrlEncodedFormEntity(map2NameValuePairList(params), "UTF-8"));
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout).
   				 setSocketTimeout(timeout).setConnectTimeout(timeout).build();
   		 	httpPost.setConfig(requestConfig);
            // 设置Header
            if (header != null && !header.isEmpty()) {
                // log.debug(" header: {}", JSONUtil.toJsonStr(header));
             
                for (Iterator<Map.Entry<String, String>> it = header.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, String> entry = it.next();
                    httpPost.setHeader(new BasicHeader(entry.getKey(), entry.getValue()));
                }
            }
            // 发送请求,获取返回数据
            body = execute(httpPost);
        } catch (Exception e) {
            throw e;
        }
        return body;
    }

    private static String execute(HttpRequestBase requestBase) throws Exception {
        HttpClientBuilder builder = HttpClientBuilder.create();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, (a, b) -> true).build();
        LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext, (a, b) -> true);
        builder.setSSLSocketFactory(sslSF);
        builder.setSSLHostnameVerifier((a, b) -> true);
        CloseableHttpClient httpclient = builder.build();
        String body = null;
        try {
            CloseableHttpResponse response = httpclient.execute(requestBase);
            try {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    body = EntityUtils.toString(entity, "UTF-8");
                }
                EntityUtils.consume(entity);
            } catch (Exception e) {
                throw e;
            } finally {
                response.close();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            httpclient.close();
        }
        return body;
    }

    private static List<NameValuePair> map2NameValuePairList(Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Iterator<String> it = params.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (params.get(key) != null) {
                    String value = String.valueOf(params.get(key));
                    list.add(new BasicNameValuePair(key, value));
                }
            }
            return list;
        }
        return null;
    }
}
