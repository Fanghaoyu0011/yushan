package Rss.rss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


public class JsonUtil {
    public static String stringToJson(String string){
        JSONObject jsonObject =  JSON.parseObject(string);
        String data= jsonObject.getString("data");
        return data;
    }
}
