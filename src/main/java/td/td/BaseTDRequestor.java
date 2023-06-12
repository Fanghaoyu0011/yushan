package td.td;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.wanda.credit.base.counter.GlobalCounter;
import com.wanda.credit.base.exception.ServiceException;
import com.wanda.credit.base.util.ExceptionUtil;
import com.wanda.credit.base.util.RequestHelper;
import com.wanda.credit.base.util.StringUtil;
import com.wanda.credit.common.template.iface.IPropertyEngine;
import com.wanda.credit.extend.BaseDataSourceRequestor;

/**
 * @author liunan
 */
/**
 * @author FANGHAOYU
 *
 */
public class BaseTDRequestor extends BaseDataSourceRequestor {

    private Logger logger = LoggerFactory.getLogger(BaseTDRequestor.class);
    public final String FACE_TOKEN_LIUNANV2_REDIS = "zjzhognan_token_yushan_redisID";

    @Autowired
	public IPropertyEngine propertyEngine;
    RequestConfig requestConfig = RequestConfig.custom()
			.setConnectionRequestTimeout(3000)
			.setConnectTimeout(3000)
			.setSocketTimeout(3000).build();
    /**
     * 获取权限token
     * @return 返回示例：
     * {
     * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
     * "expires_in": 2592000
     * }
     */
    public  String getAuth(String trade_id,boolean isGetNewToken) {
    	if(!isGetNewToken){
			try {
				String token =  GlobalCounter.getString(FACE_TOKEN_LIUNANV2_REDIS);				
				if(!StringUtil.isEmpty(token)){
					return token;
				}
			} catch (ServiceException e) {
				logger.error("{} 从redis获取token失败：{}",trade_id,e.getMessage());
			}
		}
		logger.info("{} 重新获取token获取开始...",trade_id);
        // 官网获取的 API Key 更新为你注册的
        String apikey = propertyEngine.readById("ds_TD_apikey");
        // 官网获取的 Secret Key 更新为你注册的
        String apitoken = propertyEngine.readById("ds_TD_apitoken");
        String reult = "";       
        try {
        	logger.info("{} TD标签查询获取token",trade_id);
    		reult = getAuth(apikey, apitoken);
    		logger.info("{} TD标签查询获取token:{}",trade_id,reult);
			GlobalCounter.setString(FACE_TOKEN_LIUNANV2_REDIS, reult);     	
		} catch (ServiceException e) {
			logger.error("{} token存储redis失败：{}",trade_id,ExceptionUtil.getTrace(e));
		}
        return reult;
    }
    /**
     * 更新权限token
     */
    public  String updateAuth(String trade_id,boolean isGetNewToken) {
    	if(!isGetNewToken){
			try {
				String token =  GlobalCounter.getString(FACE_TOKEN_LIUNANV2_REDIS);				
				if(!StringUtil.isEmpty(token)){
					return token;
				}
			} catch (ServiceException e) {
				logger.error("{} 从redis获取token失败：{}",trade_id,e.getMessage());
			}
		}
		logger.info("{} 更新token获取开始...",trade_id);
        // 官网获取的 API Key 更新为你注册的
        String apikey = propertyEngine.readById("ds_TD_apikey");
        // 官网获取的 Secret Key 更新为你注册的
        String apitoken = propertyEngine.readById("ds_TD_apitoken");
        String refreshToken = getrefreshToken(apikey, apitoken);
    	logger.info("{} TD标签查询获取refreshToken{}",trade_id,refreshToken);
    	
        String reult = "";  
        
        try {
        	logger.info("{} TD调用更新token接口",trade_id);
    		reult = updateAuth(apikey, refreshToken);
    		logger.info("{} TD调用更新token接口返回token:{}",trade_id,reult);
			GlobalCounter.setString(FACE_TOKEN_LIUNANV2_REDIS, reult);     	
		} catch (ServiceException e) {
			logger.error("{} token存储redis失败：{}",trade_id,ExceptionUtil.getTrace(e));
		}
        return reult;
    }
    /**
     * 获取API访问token
     */
    public  String getAuth(String apikey, String apitoken) {
        // 获取token地址
        String tokenUrl = propertyEngine.readById("ds_TD_tokenUrl");
        try {
        	HashMap<String, String> paramsMap = new HashMap<>();
            paramsMap.put("apikey", apikey); // 替换成实际账号
            paramsMap.put("apitoken", apitoken); // 替换成实际密码
        	String responseToken = RequestHelper.doGet(tokenUrl, paramsMap,false,requestConfig);
        	JSONObject tokenJson = JSONObject.parseObject(responseToken);
            JSONObject tokenData = tokenJson.getJSONObject("data");
            String token = tokenData.getString("token");
            return token;
        } catch (Exception e) {
        	logger.info(" 获取token失败00:{}", ExceptionUtil.getTrace(e));
        }
        return null;
    }
    /**
     * 获取API访问refreshToken
     */
    public  String getrefreshToken(String apikey, String apitoken) {
        // 获取token地址
        String tokenUrl = propertyEngine.readById("ds_TD_tokenUrl");
        try {
        	HashMap<String, String> paramsMap = new HashMap<>();
            paramsMap.put("apikey", apikey); // 替换成实际账号
            paramsMap.put("apitoken", apitoken); // 替换成实际密码
        	String responseToken = RequestHelper.doGet(tokenUrl, paramsMap,false,requestConfig);
        	JSONObject tokenJson = JSONObject.parseObject(responseToken);
            JSONObject tokenData = tokenJson.getJSONObject("data");
            String refreshToken = tokenData.getString("refreshToken");
            return refreshToken;
        } catch (Exception e) {
//        	logger.info("获取token失败！"+e.getMessage());
        	logger.info(" 获取token失败11:{}", ExceptionUtil.getTrace(e));
        }
        return null;
    }
    /**
     * 更新API访问token
     */
    public  String updateAuth(String apikey, String refreshToken) {

        try {
        	HashMap<String, String> paramsMap = new HashMap<>();
            paramsMap.put("apikey", apikey); // 替换成实际账号
            paramsMap.put("refreshToken", refreshToken); // 替换成实际密码
        	String responseToken = sendPost(refreshToken);
        	logger.info("更新接口返回值responseToken：{}",responseToken);
        	JSONObject tokenJson = JSONObject.parseObject(responseToken);
            JSONObject tokenData = tokenJson.getJSONObject("data");
        	logger.info("更新接口返回Data：tokenData：{}",tokenData);
            String token = tokenData.getString("token");
            
            return token;
        } catch (Exception e) {
        	logger.info(" 获取token失败22:{}", ExceptionUtil.getTrace(e));
        }
        return null;
    }
    /**
	 * 返回byte的数据大小对应的字符串
	 * @param size
	 * @return
	 */
	public static boolean formatStrSize(String str){
		if(str==null || str.length()==0)
			return true;
		long size = str.length();
		if(size>1024*1024){
			return true;
		}
		return false;
	}
	 /**
		 * 不设置请求数据类型直接请求post方法
		 * @param refreshToken
		 * @return
		 */
		public static String sendPost(String refreshToken){
			String postResponse = "";
	        try {
			String url = "https://api.talkingdata.com/authentication/api/refreshToken?apikey=b3dd2a1e5b63485dae6263101ebfe939&refreshToken="+refreshToken;
			 URL obj = new URL(url);
	            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	            con.setRequestMethod("POST"); // 设置请求方法为POST
	            con.setDoOutput(true);
	            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	            wr.flush();
	            wr.close();
	            int responseCode = con.getResponseCode();
	            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            String inputLine;
	            StringBuffer response = new StringBuffer();
	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            in.close();
	           postResponse = response.toString();
	           } catch (Exception e) {
	            System.out.println("Exception caught : " + e);
	        }
	        return postResponse;
	    }
}

