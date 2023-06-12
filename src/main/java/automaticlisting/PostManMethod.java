package automaticlisting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yushan.tech.batch.action.automaticlisting.util.AutomaticUtil;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;

public class PostManMethod {
    public static JSONObject Postman(MultipartFile multipart) throws Exception {
        //建立自动上架的JSONobject
        JSONObject AutomaticListingObject = new JSONObject();
        JSONObject ret = new JSONObject();
        //将multipart文件转换为file文件流形式
        String fileName = multipart.getOriginalFilename();
        File tempFile = File.createTempFile("temp", null);
        multipart.transferTo(tempFile);
        File file = new File(tempFile.getAbsolutePath());

        //将文件流转换为JSONObject的形式
        JSONObject JSONFile = readerMethod(file);

        //获取需要需要处理的json中的item部分
        JSONArray item = JSONFile.getJSONArray("item");

        //因为是一个JSONAray，所以需要循环获取每个数据
        for (int i = 0; i < item.size(); i++) {
            //method
            String method = item.getJSONObject(i).getJSONObject("request").getString("method");
            AutomaticListingObject.put("method", method.toLowerCase());

            //url
            String Firsturl = item.getJSONObject(i).getJSONObject("request").getString("url");
            String url;
            if (Firsturl.substring(0, 1).equals("h")) {
                url = item.getJSONObject(i).getJSONObject("request").getString("url");
                if (method.equals("GET")) {
                    url = url.substring(0, url.indexOf("?"));
                }
            } else {
                url = item.getJSONObject(i).getJSONObject("request").getJSONObject("url").getString("raw");
                if (method.equals("GET")) {
                    url = url.substring(0, url.indexOf("?"));
                }
            }
            AutomaticListingObject.put("url", url);

            //header
            JSONArray headerArray = new JSONArray();
            if (method.equals("POST")) {
                headerArray = AutomaticUtil.postHeader(item.getJSONObject(i));
                AutomaticListingObject.put("header", headerArray);
            } else {
                headerArray = AutomaticUtil.getHeader();
                AutomaticListingObject.put("header", headerArray);
            }

            //body
            if (method.equals("POST")) {
                JSONArray bodyArray = AutomaticUtil.postBody(item.getJSONObject(i));
                AutomaticListingObject.put("body", bodyArray);
            } else {
                JSONArray bodyArray = AutomaticUtil.getBody(item.getJSONObject(i));
                AutomaticListingObject.put("body", bodyArray);
            }
        }

        //logSave
        JSONArray logSave = AutomaticUtil.logSave();
        AutomaticListingObject.put("logSave", logSave);
        ret.put("retcode", "000000");
        ret.put("retmsg", "success");

        return AutomaticListingObject;
    }

    private static JSONObject readerMethod(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        Reader reader = new InputStreamReader(new FileInputStream(file), "Utf-8");
        int ch = 0;
        StringBuffer sb = new StringBuffer();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        fileReader.close();
        reader.close();
        String jsonStr = sb.toString();
        return JSON.parseObject(jsonStr);
    }
}
