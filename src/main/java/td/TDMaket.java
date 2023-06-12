package td;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
@RestController
@RequestMapping(value = "/td")
public class TDMaket {
    private final Logger logger = LoggerFactory.getLogger(TDMaket.class);
    public static String url = "https://api.talkingdata.com/tdmkaccount/authen/app/v3?apikey=b3dd2a1e5b63485dae6263101ebfe939&apitoken=13ba21a88a9b45cf96f67cba97cf7002";

    /**
     *
     * @param srcPath 文件传入FTP的地址
     * @param requestId 请求id
     * @param bn 异步服务编号 ，在服务文档或者服务介绍页中可查看
     * @param dstPath 结果文件存入地址，若
     * @return
     */
    @PostMapping("/maket")
    public String makert(@RequestParam(value = "srcPath") String srcPath,
                         @RequestParam(value = "requestId")String requestId,
                         @RequestParam(value = "bn")String bn,
                         @RequestParam(value = "dstPath")String dstPath){
        String task = "";
        try {
            logger.info("开始创建任务");
            task = createTask(srcPath,requestId,bn,dstPath);
//            JSONObject taskJson = JSONObject.parseObject(task);
//            taskId = taskJson.getString("data");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return task;
    }
    public static String createTask(String srcPath,String requestId,String bn,String dstPath) throws IOException {
        //获取token
        String msg = sendGet(url);
        JSONObject jsonObject = JSONObject.parseObject(msg);
        JSONObject data = jsonObject.getJSONObject("data");
        String token = data.getString("token");
        //调用方法
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("callbackURL","http://101.34.39.121:30013/app-yushan-tech-batch/td/callback")
                .addFormDataPart("taskType","PSCORES")
                .addFormDataPart("srcPath",srcPath)
                .addFormDataPart("requestId",requestId)
                .addFormDataPart("dstPath",dstPath)
                //dstPath一行若是不传该值，则拿taskId创建一个目录存放结果
                .build();
        Request request = new Request.Builder()
                .url("https://api.talkingdata.com/async/client/bn/"+bn)
                .method("POST", body)
                .addHeader("x-access-token", token)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/4.76");
            // 建立实际的连接
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            InputStream inStream = connection.getInputStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] data = outStream.toByteArray();
            outStream.close();
            inStream.close();
            result = new String(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }


}
