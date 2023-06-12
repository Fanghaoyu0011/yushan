package postman2JSON.src.main.java;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class testTiqu {
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
        if (jsonObject.getJSONObject("request").getJSONObject("body").getString("mode") == "raw") {
            if (jsonObject.getJSONObject("request").getJSONObject("body").getJSONObject("options").getJSONObject("raw").getString("language") == "json") {
                contentType.put("name", "content-type");
                contentType.put("value", "application/json");
            }
        }
        contentType.put("name", "content-type");
        contentType.put("value", "application/x-www-urlencoded");
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
            queryJson.put("position", "param");
            queryJson.put("target", "");
            queryJson.put("desc", "");
            if (value.substring(0, 1).equals("{")) {
                queryJson.put("isExpression", "1");
            } else {
                queryJson.put("isExpression", "0");
            }
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
                String desc = params.getJSONObject(k).getString("description");
                bodyJson.put("name", key);
                bodyJson.put("value", value);
                bodyJson.put("order", "");
                bodyJson.put("position", "param");
                bodyJson.put("target", "");
                bodyJson.put("desc", desc);

                if (value.equals("")) {
                    bodyJson.put("isExpression", "0");
                }else {
                    if (value.substring(0, 1).equals("{")) {
                        bodyJson.put("isExpression", "1");
                    } else {
                        bodyJson.put("isExpression", "0");
                    }
                }
//                bodyJson.put("isExpression", "0");
                bodyJson.put("isRequired", "1");
                bodyArray.add(bodyJson);
            }
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
            sysVariableArray.add(sysVariableJson);
        }
        return sysVariableArray;
    }
}
