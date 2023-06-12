package postman2JSON.src.main.java;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class test {
    public static void main(String[] args) throws IOException {
        //建立自动上架的JSONobject
        JSONObject AutomaticListingObject = new JSONObject();
        try {
            //建立一个准备使用的HashMap
            Map<String, String> map = new HashMap<>();
            //导入需要解析的产品
            File file = new File("D:\\fhy\\lbw Copy.postman_collection.json");
            //将文件流转换为JSONObject的形式
            JSONObject JSONFile = readerMethod(file);
            //获取需要需要处理的json中的item部分
            JSONArray item = JSONFile.getJSONArray("item");
//            String execString = "";
//            String event = item.getJSONObject(0).getJSONArray("event").getJSONObject(0).getJSONObject("script").getJSONArray("exec").toString();
//            System.out.println("event:"+event);
//            for (int j = 0; j < event.size(); j++) {
//                JSONArray exec = event.getJSONObject(j).getJSONObject("script").getJSONArray("exec");
//                for (int k = 0; k < exec.size(); k++) {
//                    execString += exec.getString(k);
//                }
//                System.out.println("execString："+execString);
//            }
            //因为是一个JSONAray，所以需要循环获取每个数据
            //取出json中的前置操作放置到String中
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

                //url
//                try {
//                    String url = item.getJSONObject(i).getJSONObject("request").getJSONObject("url").getString("raw");
//                    if (method.equals("GET")) {
//                        url = url.substring(0, url.indexOf("?"));
//                        AutomaticListingObject.put("url", url);
//                    }
//                } catch (com.alibaba.fastjson.JSONException e) {eee
//                    String url = item.getJSONObject(i).getJSONObject("request").getString("url");
//                    if (method.equals("GET")) {
//                        url = url.substring(0, url.indexOf("?"));
//                        AutomaticListingObject.put("url", url);
//                    }jjaipostdaudwhedaer headerArray
//                }dasdaweectign3
                //header
                JSONArray headerArray = new JSONArray();
                if (method.equals("POST")) {
                    headerArray = testTiqu.postHeader(item.getJSONObject(i));
                    AutomaticListingObject.put("header", headerArray);
                } else {
                    headerArray = testTiqu.getHeader();
                    AutomaticListingObject.put("header", headerArray);
                }

                //body
                if (method.equals("POST")) {
                    JSONArray bodyArray = testTiqu.postBody(item.getJSONObject(i));
                    AutomaticListingObject.put("body", bodyArray);
                } else {
                    JSONArray bodyArray = testTiqu.getBody(item.getJSONObject(i));
                    AutomaticListingObject.put("body", bodyArray);
                }

                //sysVariable
                JSONArray sysVariableArray = testTiqu.sysVariable(item.getJSONObject(i));
                AutomaticListingObject.put("sysVariable", sysVariableArray);
            }

            //logSave
            JSONArray logSave = testTiqu.logSave();
            AutomaticListingObject.put("logSave", logSave);
            System.out.println(AutomaticListingObject.toString());

        } catch (Exception e) {
        }

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
