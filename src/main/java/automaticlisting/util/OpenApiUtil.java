package automaticlisting.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class OpenApiUtil {
    /**
     * 创建一个contype为urlencoded的header
     *
     * @return
     */
    public static JSONArray urlencoded() {
        JSONArray headerArray = new JSONArray();
        JSONObject contentType = new JSONObject();
        JSONObject timeout = new JSONObject();
        contentType.put("name", "content-type");
        contentType.put("value", "application/x-www-urlencoded");
        timeout.put("name", "timeout");
        timeout.put("value", "10000");
        headerArray.add(contentType);
        headerArray.add(timeout);
        return headerArray;
    }

    /**
     * 创建一个contype为json的header
     *
     * @return
     */
    public static JSONArray json() {
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

    public static JSONArray Postbody(JSONObject method, String methodString, String contentTpye) {
        if (contentTpye.equals("application/json")) {
            JSONArray bodyJsonArray = new JSONArray();
            JSONObject example = method.getJSONObject(methodString).getJSONObject("requestBody").getJSONObject("content")
                    .getJSONObject(contentTpye).getJSONObject("example");
            for (String key : example.keySet()) {
                JSONObject bodyJSON = new JSONObject();
                Object valueObject = example.get(key);
                String exampleString = example.getString(key);
                JSONObject values = JSONObject.parseObject(valueObject.toString());
                String value = values.getString("example");
                if (value == null) {
                    bodyJSON.put("value", "");
                } else {
                    bodyJSON.put("name", key);
                    if (value.substring(0, 1).equals("{")) {
                        value = value.replace("{{", "");
                        value = value.replaceAll("}", "");
                        bodyJSON.put("value", "$" + value);
                        bodyJSON.put("isExpression", "1");
                    } else {
                        bodyJSON.put("value", value);
                        bodyJSON.put("isExpression", "0");
                    }
                }
                bodyJSON.put("name", key);
                bodyJSON.put("desc", "");
                bodyJSON.put("order", "");
                bodyJSON.put("position", "param");
                bodyJSON.put("target", "");
                bodyJSON.put("isRequired", "1");
                bodyJsonArray.add(bodyJSON);
            }
            JSONArray parameters = method.getJSONObject(methodString).getJSONArray("parameters");
            for (int i = 0; i < parameters.size(); i++) {
                JSONObject sysVariable = new JSONObject();
                String headerName = parameters.getJSONObject(i).getString("name");
                String headerExample = parameters.getJSONObject(i).getString("example");
                sysVariable.put("name", headerName);
                sysVariable.put("value", headerExample);
                sysVariable.put("desc", "");
                sysVariable.put("order", "");
                sysVariable.put("position", "header");
                sysVariable.put("target", "");
                if (headerExample != null) {
                    if (headerExample.substring(0, 1).equals("{")) {
                        sysVariable.put("isExpression", "1");
                    } else {
                        sysVariable.put("isExpression", "0");
                    }
                } else {
                    sysVariable.put("isExpression", "0");
                }
                sysVariable.put("isRequired", "1");
                bodyJsonArray.add(sysVariable);
            }
            return bodyJsonArray;
        } else {
            JSONObject content = method.getJSONObject(methodString).getJSONObject("requestBody").getJSONObject("content");
            String contentTypeString = null;
            for (String key : content.keySet()) {
                contentTypeString = key;
            }
            JSONObject properties = content.getJSONObject(contentTypeString).getJSONObject("schema").getJSONObject("properties");
            JSONArray required = content.getJSONObject(contentTypeString).getJSONObject("schema").getJSONArray("required");

            JSONArray bodyJsonArray = new JSONArray();
            for (String key : properties.keySet()) {
                JSONObject bodyJSON = new JSONObject();
                Object value = properties.get(key);
                JSONObject values = JSONObject.parseObject(value.toString());
                String example = values.getString("example");
                if (example.substring(0, 1).equals("{")) {
                    example = example.replace("{{", "");
                    example = example.replaceAll("}", "");
                    bodyJSON.put("value", "$" + example);
                } else {
                    bodyJSON.put("value", example);
                }
                bodyJSON.put("name", key);
                bodyJSON.put("desc", "");
                bodyJSON.put("order", "");
                bodyJSON.put("position", "param");
                bodyJSON.put("target", "");
                bodyJSON.put("isExpression", "0");
                if (required.contains(key)) {
                    bodyJSON.put("isRequired", "1");
                } else {
                    bodyJSON.put("isRequired", "0");
                }
                bodyJsonArray.add(bodyJSON);
            }
            JSONArray parameters = method.getJSONObject(methodString).getJSONArray("parameters");
            for (int i = 0; i < parameters.size(); i++) {
                JSONObject sysVariable = new JSONObject();
                String headerName = parameters.getJSONObject(i).getString("name");
                String headerExample = parameters.getJSONObject(i).getString("example");
                sysVariable.put("name", headerName);
                sysVariable.put("value", headerExample);
                if (parameters.getJSONObject(i).getString("description") == null) {
                    sysVariable.put("desc", "");
                } else {
                    sysVariable.put("desc", parameters.getJSONObject(i).getString("description"));
                }
                sysVariable.put("order", "");
                sysVariable.put("position", "header");
                sysVariable.put("target", "");
                sysVariable.put("isExpression", "0");
                sysVariable.put("isRequired", "1");
                bodyJsonArray.add(sysVariable);
            }

            return bodyJsonArray;
        }
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
}
