package automaticlisting;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.yushan.tech.batch.action.automaticlisting.util.OpenApiUtil;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * 支持OpenApi格式
 */

public class OpenApiListAction {

    public static JSONObject openApi(MultipartFile multipart) throws Exception {

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

        //url
        JSONObject paths = JSONFile.getJSONObject("paths");
        String path = null;
        for (String key : paths.keySet()) {
            path = key;
        }
        String url = null;
        JSONArray servers = JSONFile.getJSONArray("servers");
        for (int i = 0; i < servers.size(); i++) {
            url = servers.getJSONObject(i).getString("url");
        }
        AutomaticListingObject.put("url", url + path);

        //method
        JSONObject method = JSONFile.getJSONObject("paths").getJSONObject(path);
        String methodString = null;
        for (String key : method.keySet()) {
            methodString = key;
        }
        AutomaticListingObject.put("method", methodString);

        //header
        String contentTypeString = null;
        if (methodString.equals("post")) {
            JSONObject content = method.getJSONObject(methodString).getJSONObject("requestBody").getJSONObject("content");

            for (String key : content.keySet()) {
                contentTypeString = key;
            }
            if (contentTypeString.equals("application/json")) {
                JSONArray json = OpenApiUtil.json();
                AutomaticListingObject.put("header", json);
            } else {
                JSONArray urlencoded = OpenApiUtil.urlencoded();
                AutomaticListingObject.put("header", urlencoded);
            }
        } else {
            JSONArray urlencoded = OpenApiUtil.urlencoded();
            AutomaticListingObject.put("header", urlencoded);
        }

        //body
        if (methodString.equals("post")) {
            JSONObject content = method.getJSONObject(methodString).getJSONObject("requestBody").getJSONObject("content");
            String contentTypestring = null;
            for (String key : content.keySet()) {
                contentTypestring = key;
            }
            JSONObject properties = content.getJSONObject(contentTypestring).getJSONObject("schema").getJSONObject("properties");
            JSONArray bodyJsonArray = new JSONArray();
            for (String key : properties.keySet()) {
                JSONObject bodyJSON = new JSONObject();
                Object value = properties.get(key);
                JSONObject values = JSONObject.parseObject(value.toString());
                String example = values.getString("example");
                System.out.println("example:" + example);
                if (example != null) {
                    if (example.substring(0, 1).equals("{")) {
                        example = example.replace("{{", "");
                        example = example.replaceAll("}", "");
                        bodyJSON.put("value", "$" + example);
                    } else {
                        bodyJSON.put("value", example);
                    }
                } else {
                    bodyJSON.put("value", "");
                }
                bodyJSON.put("name", key);
                bodyJSON.put("desc", "");
                bodyJSON.put("order", "");
                bodyJSON.put("position", "param");
                bodyJSON.put("target", "");
                bodyJSON.put("isExpression", "0");
                bodyJSON.put("isRequired", "1");
                bodyJsonArray.add(bodyJSON);
            }
            JSONArray body = new JSONArray();
            body.add(bodyJsonArray);
            AutomaticListingObject.put("body", body);
        } else {
            JSONArray parameters = method.getJSONObject(methodString).getJSONArray("parameters");
            JSONArray sysVariableArray = new JSONArray();
            for (int i = 0; i < parameters.size(); i++) {
                JSONObject sysVariable = new JSONObject();
                String name = parameters.getJSONObject(i).getString("name");
                String example = parameters.getJSONObject(i).getString("example");
                String in = parameters.getJSONObject(i).getString("in");

                if (in.equals("header")) {
                    sysVariable.put("name", name);
                    sysVariable.put("value", example);
                    if (parameters.getJSONObject(i).getString("description") == null) {
                        sysVariable.put("desc", "");
                    } else {
                        sysVariable.put("desc", parameters.getJSONObject(i).getString("description"));
                    }
                    sysVariable.put("order", "");
                    sysVariable.put("position", "header");
                    sysVariable.put("target", "");

                    if (example.substring(0, 1).equals("{")) {
                        sysVariable.put("isExpression", "1");
                    } else {
                        sysVariable.put("isExpression", "0");
                    }
                    if (parameters.getJSONObject(i).get("required").equals(true)) {
                        sysVariable.put("isRequired", "1");
                    } else {
                        sysVariable.put("isRequired", "0");
                    }
                    sysVariableArray.add(sysVariable);
                } else if (in.equals("query")) {
                    sysVariable.put("name", name);
                    sysVariable.put("value", example);
                    sysVariable.put("desc", "");
                    sysVariable.put("order", "");
                    sysVariable.put("position", "url");
                    sysVariable.put("target", "");
                    sysVariable.put("isExpression", "0");
                    if (parameters.getJSONObject(i).get("required").equals(true)) {
                        sysVariable.put("isRequired", "1");
                    } else {
                        sysVariable.put("isRequired", "0");
                    }
                    sysVariableArray.add(sysVariable);
                }
            }
            AutomaticListingObject.put("body", sysVariableArray);
        }

        //logSave
        JSONArray logSave = OpenApiUtil.logSave();

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