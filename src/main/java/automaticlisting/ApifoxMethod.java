package automaticlisting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class ApifoxMethod {
    public static JSONObject apiFox(MultipartFile multipart) throws Exception {
//建立自动上架的JSONobject
        JSONObject AutomaticListingObject = new JSONObject();
        JSONObject ret = new JSONObject();
        //提前准备一个bodyArray
        JSONArray bodyArray = new JSONArray();
        //将multipart文件转换为file文件流形式
        String fileName = multipart.getOriginalFilename();
        File tempFile = File.createTempFile("temp", null);
        multipart.transferTo(tempFile);
        File file = new File(tempFile.getAbsolutePath());

        //将文件流转换为JSONObject的形式
        JSONObject JSONFile = readerMethod(file);


        //url+method
        JSONArray apiCollection = JSONFile.getJSONArray("apiCollection");
        JSONArray environments = JSONFile.getJSONArray("environments");
        String baseUrl = "";
        for (int i = 0; i < environments.size(); i++) {
            baseUrl = environments.getJSONObject(i).getString("baseUrl");
        }
        for (int i = 0; i < apiCollection.size(); i++) {
            JSONArray items = apiCollection.getJSONObject(i).getJSONArray("items");
            for (int j = 0; j < items.size(); j++) {
                String method = items.getJSONObject(j).getJSONObject("api").getString("method");
                //method
                AutomaticListingObject.put("method", method);
                String path = items.getJSONObject(j).getJSONObject("api").getString("path");
                String url = "";
                if (method.equals("post")) {
                    url = baseUrl + path;
                } else {
                    url = path;
                }
                //url
                AutomaticListingObject.put("url", url);
                //body
                JSONArray header = items.getJSONObject(j).getJSONObject("api").getJSONObject("parameters").getJSONArray("header");
                for (int k = 0; k < header.size(); k++) {
                    JSONObject bodyJSON = new JSONObject();
                    String name = header.getJSONObject(k).getString("name");
                    String value = header.getJSONObject(k).getString("example");
                    bodyJSON.put("name", name);
                    bodyJSON.put("value", value);
                    if (header.getJSONObject(k).getString("description").isEmpty()) {
                        bodyJSON.put("desc", "");
                    } else {
                        bodyJSON.put("desc", header.getJSONObject(k).getString("description"));
                    }
                    if (header.getJSONObject(k).getString("required").equals(true)) {
                        bodyJSON.put("isRequired", "1");
                    } else {
                        bodyJSON.put("isRequired", "0");
                    }
                    bodyJSON.put("order", "");
                    bodyJSON.put("position", "header");
                    bodyJSON.put("target", "");
                    if (value.substring(0, 1).equals("{")) {
                        bodyJSON.put("isExpression", "1");
                    } else {
                        bodyJSON.put("isExpression", "0");
                    }
                    bodyArray.add(bodyJSON);
                }
                JSONArray query = items.getJSONObject(j).getJSONObject("api").getJSONObject("parameters").getJSONArray("query");
                for (int k = 0; k < query.size(); k++) {
                    JSONObject bodyJSON = new JSONObject();
                    String name = query.getJSONObject(k).getString("name");
                    String value = query.getJSONObject(k).getString("example");
                    bodyJSON.put("name", name);
                    bodyJSON.put("value", value);
                    if (query.getJSONObject(k).getString("description").isEmpty()) {
                        bodyJSON.put("desc", "");
                    } else {
                        bodyJSON.put("desc", query.getJSONObject(k).getString("description"));
                    }
                    if (query.getJSONObject(k).getString("required").equals(true)) {
                        bodyJSON.put("isRequired", "1");
                    } else {
                        bodyJSON.put("isRequired", "0");
                    }
                    bodyJSON.put("order", "");
                    bodyJSON.put("position", "url");
                    bodyJSON.put("target", "");
                    if (value.substring(0, 1).equals("{")) {
                        bodyJSON.put("isExpression", "1");
                    } else {
                        bodyJSON.put("isExpression", "0");
                    }
                    bodyArray.add(bodyJSON);
                }
                //header
                String contentTypeString = items.getJSONObject(j).getJSONObject("api").getJSONObject("requestBody").getString("type");
                if (contentTypeString.equals("application/json")) {
                    JSONArray headerArray = new JSONArray();
                    JSONObject contentType = new JSONObject();
                    JSONObject timeout = new JSONObject();
                    contentType.put("name", "content-type");
                    contentType.put("value", "application/json");
                    timeout.put("name", "timeout");
                    timeout.put("value", "10000");
                    headerArray.add(contentType);
                    headerArray.add(timeout);
                    AutomaticListingObject.put("header", headerArray);
                } else {
                    JSONArray headerArray = new JSONArray();
                    JSONObject contentType = new JSONObject();
                    JSONObject timeout = new JSONObject();
                    contentType.put("name", "content-type");
                    contentType.put("value", "application/x-www-urlencoded");
                    timeout.put("name", "timeout");
                    timeout.put("value", "10000");
                    headerArray.add(contentType);
                    headerArray.add(timeout);
                    AutomaticListingObject.put("header", headerArray);
                }
                //body
                JSONArray parameters = items.getJSONObject(i).getJSONObject("api").getJSONObject("requestBody").getJSONArray("parameters");
                for (int k = 0; k < parameters.size(); k++) {
                    JSONObject bodyJSON = new JSONObject();
                    String name = parameters.getJSONObject(k).getString("name");
                    String value = parameters.getJSONObject(k).getString("example");
                    bodyJSON.put("name", name);
                    bodyJSON.put("value", value);
                    if (parameters.getJSONObject(k).getString("description").isEmpty()) {
                        bodyJSON.put("desc", "");
                    } else {
                        bodyJSON.put("desc", parameters.getJSONObject(k).getString("description"));
                    }
                    if (parameters.getJSONObject(k).getString("required").equals(true)) {
                        bodyJSON.put("isRequired", "1");
                    } else {
                        bodyJSON.put("isRequired", "0");
                    }
                    bodyJSON.put("order", "");
                    bodyJSON.put("position", "param");
                    bodyJSON.put("target", "");
                    if (value.substring(0, 1).equals("{")) {
                        bodyJSON.put("isExpression", "1");
                    } else {
                        bodyJSON.put("isExpression", "0");
                    }
                    bodyArray.add(bodyJSON);
                }
            }
        }
        AutomaticListingObject.put("body", bodyArray);
        //logSave
        JSONArray logSave = new JSONArray();
        JSONObject logsave = new JSONObject();
        logsave.put("path", "");
        logsave.put("position", "");
        logsave.put("log", "");
        logSave.add(logsave);
        AutomaticListingObject.put("logSave", logSave);
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
