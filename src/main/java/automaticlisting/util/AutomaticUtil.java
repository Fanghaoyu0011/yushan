package automaticlisting.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yushan.tech.batch.action.automaticlisting.AutomaticListAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class AutomaticUtil {
    private final Logger logger = LoggerFactory.getLogger(AutomaticUtil.class);

    public static JSONArray getHeader() {
        JSONArray headerArray = new JSONArray();
        JSONObject contentType = new JSONObject();
        JSONObject timeout = new JSONObject();
        contentType.put("name", "content-type");
        contentType.put("value", "application/json");
        timeout.put("name", "timeout");
        timeout.put("value", "10000");
        headerArray.add(contentType);
        headerArray.add(timeout);
        return headerArray;
    }

    public static JSONArray postHeader(JSONObject jsonObject) {
        JSONArray headerArray = new JSONArray();
        JSONObject contentType = new JSONObject();
        JSONObject timeout = new JSONObject();
        if (jsonObject.getJSONObject("request").getJSONObject("body").getString("mode").equals("raw")) {
            contentType.put("name", "content-type");
            contentType.put("value", "application/json");
        } else {
            contentType.put("name", "content-type");
            contentType.put("value", "application/x-www-urlencoded");
        }
        timeout.put("name", "timeout");
        timeout.put("value", "10000");
        headerArray.add(contentType);
        headerArray.add(timeout);
        return headerArray;
    }

    public static JSONArray getBody(JSONObject jsonObject) {
        JSONArray params = jsonObject.getJSONObject("request").getJSONObject("url").getJSONArray("query");
        JSONArray queryArray = new JSONArray();
        for (int j = 0; j < params.size(); j++) {
            JSONObject queryJson = new JSONObject();
            String key = params.getJSONObject(j).getString("key");
            String value = params.getJSONObject(j).getString("value");
            queryJson.put("name", key);
            queryJson.put("value", value);
            queryJson.put("order", "");
            queryJson.put("position", "url");
            queryJson.put("target", "");
            if (params.getJSONObject(j).getString("description") == null) {
                queryJson.put("desc", "");
            } else {
                queryJson.put("desc", params.getJSONObject(j).getString("description"));
            }
            queryJson.put("isExpression", "0");
            queryJson.put("isRequired", "1");
            queryArray.add(queryJson);
        }
        return queryArray;
    }

    public static JSONArray postBody(JSONObject jsonObject) {
        JSONArray bodyArray = new JSONArray();
        String mode = jsonObject.getJSONObject("request").getJSONObject("body").getString("mode");
        if (mode.equals("formdata") || mode.equals("urlencoded")) {
            JSONArray params = jsonObject.getJSONObject("request").getJSONObject("body").getJSONArray(mode);
            for (int k = 0; k < params.size(); k++) {
                JSONObject bodyJson = new JSONObject();
                String key = params.getJSONObject(k).getString("key");
                String value = params.getJSONObject(k).getString("value");
                if (value.substring(0, 1).equals("{")) {
                    value = value.replaceAll("}", "");
                    value = value.replace("{{", "");
                    bodyJson.put("value", "$" + value);
                } else {
                    bodyJson.put("value", value);
                }
                String desc = params.getJSONObject(k).getString("description");
                bodyJson.put("name", key);
                bodyJson.put("order", "");
                bodyJson.put("position", "param");
                bodyJson.put("target", "");

                if (params.getJSONObject(k).getString("description") == null) {
                    bodyJson.put("desc", "");
                } else {
                    bodyJson.put("desc", params.getJSONObject(k).getString("description"));
                }
                if (value.equals("")) {
                    bodyJson.put("isExpression", "0");
                } else {
                    if (value.substring(0, 1).equals("{")) {
                        bodyJson.put("isExpression", "1");
                    } else {
                        bodyJson.put("isExpression", "0");
                    }
                }
                bodyJson.put("isRequired", "1");
                bodyArray.add(bodyJson);
            }
        } else {
            String params = jsonObject.getJSONObject("request").getJSONObject("body").getString(mode);
            JSONObject bodyJSON = JSON.parseObject(params);
            for (String key : bodyJSON.keySet()) {
                JSONObject bodyJSON1 = new JSONObject();
                Object value = bodyJSON.get(key);
                bodyJSON1.put("name", key);
                bodyJSON1.put("value", value);
                bodyJSON1.put("desc", "");
                bodyJSON1.put("order", "");
                bodyJSON1.put("position", "param");
                bodyJSON1.put("target", "");
                bodyJSON1.put("isExpression", "0");
                bodyJSON1.put("isRequired", "1");
                bodyArray.add(bodyJSON1);
            }
        }
        JSONArray sysVariable = jsonObject.getJSONObject("request").getJSONArray("header");
        JSONArray sysVariableArray = new JSONArray();
        for (int j = 0; j < sysVariable.size(); j++) {
            JSONObject sysVariableJson = new JSONObject();
            String key = sysVariable.getJSONObject(j).getString("key");
            String value = sysVariable.getJSONObject(j).getString("value");
            sysVariableJson.put("name", key);
            sysVariableJson.put("value", value);
            sysVariableJson.put("desc", "");
            sysVariableJson.put("order", "");
            sysVariableJson.put("position", "header");
            sysVariableJson.put("target", "");
            sysVariableJson.put("isExpression", "0");
            sysVariableJson.put("isRequired", "1");
            sysVariableArray.add(sysVariableJson);
            bodyArray.add(sysVariableJson);
        }
        return bodyArray;
    }

    public static JSONArray logSave() {
        JSONArray logSave = new JSONArray();
        JSONObject logsave = new JSONObject();
        logsave.put("path", "");
        logsave.put("position", "");
        logsave.put("log", "");
        logSave.add(logsave);
        return logSave;
    }

    public static JSONArray sysVariable(JSONObject jsonObject) {
        JSONArray sysVariable = jsonObject.getJSONObject("request").getJSONArray("header");
        JSONArray sysVariableArray = new JSONArray();
        for (int j = 0; j < sysVariable.size(); j++) {
            JSONObject sysVariableJson = new JSONObject();
            String key = sysVariable.getJSONObject(j).getString("key");
            String value = sysVariable.getJSONObject(j).getString("value");
            sysVariableJson.put("name", key);
            sysVariableJson.put("value", value);
            sysVariableJson.put("desc", "");
            sysVariableJson.put("order", "");
            sysVariableJson.put("position", "header");
            sysVariableJson.put("target", "");
            sysVariableJson.put("isExpression", "0");
            sysVariableJson.put("isRequired", "1");
            sysVariableArray.add(sysVariableJson);
        }
        return sysVariableArray;
    }

}
