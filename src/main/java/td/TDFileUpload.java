package td;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yushan.tech.batch.dao.BatchStatusDao;

import okhttp3.*;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/td")

public class TDFileUpload {
    private final Logger logger = LoggerFactory.getLogger(TDFileUpload.class);

    @Autowired
    private BatchStatusDao batchNodao;

    public static String ftpHost = "114.67.248.208";
    public static String ftpUserName = "02_ys_2t_360";
    public static String ftpPassword = "pmkfhqwxzbvJyjej";
    public static int ftpPort = 5021;
    public static String url = "https://api.talkingdata.com/tdmkaccount/authen/app/v3?apikey=b3dd2a1e5b63485dae6263101ebfe939&apitoken=13ba21a88a9b45cf96f67cba97cf7002";


    /**
     * 上传文件至FTP服务器
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "batch_id") String batch_id,
                                   @RequestParam(value = "bn") String bn
    ) {

        String prefix = batch_id; //流水号标识
        String fileName = file.getOriginalFilename();
        String FTPPath = "";
        try {
            //将文件上传至本地文件
            logger.info("{}将文件上传至本地文件", prefix);
            byte[] bytes = file.getBytes();
            Path path = Paths.get("/u01/tmp/yushan/file" + fileName);//文件存储地址

            String StringPath = path.toString();

            logger.info("{}文件储存本地地址为{}", prefix, StringPath);
            Files.write(path, bytes);
            //将文件从本地上传至FTP服务器中
            logger.info("{}开始上传文件到FTP服务器中");

            FTPPath = fileToFTP(StringPath);
            logger.info("{}文件储存FTP地址为{}", prefix, FTPPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("{}开始创建结果地址", prefix);
        FTPClient ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
        logger.info("{}创建结果地址，地址为{}", prefix, FTPPath);

        createDir(ftpClient, FTPPath);
        logger.info("{}文件上传完成，开始进行数据处理", prefix);
        String task = "";
        try {
            logger.info("开始创建数据处理任务");
            task = createTask(FTPPath, prefix, bn);
            JSONObject json = JSON.parseObject(task);
            String taskId = json.getString("data");
            batchNodao.batchStatus("/"+taskId, taskId, "1");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return task;
    }

    public static String fileToFTP(String pathname) {
        String FTPPath = "";
        String host = "114.67.239.138";
        int port = 5021;
        String userName = "02_ys_2t_360";
        String password = "zmmr4g6ncwyKweue";
        File file = new File(pathname);
        // ftp
        FTPClient ftpClient = new FTPClient();
        // 设置连接使用的字符编码。必须在建立连接之前设置。
        ftpClient.setControlEncoding("UTF-8");
        try {
            // 连接服务端
            ftpClient.connect(host, port);
            System.out.println("连接服务器" + host + ":" + port);

            // ftp操作可能会返回一些响应信息，可以打印出来看看
            showServerReply(ftpClient);

            // 尝试连接后，检查响应码以确认成功
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                disconnectQuietly(ftpClient);
                System.out.println("服务器拒绝连接");

            }
        } catch (IOException e) {
            disconnectQuietly(ftpClient);
            System.out.println("连接ftp失败");
            e.printStackTrace();

        }

        try {
            // 登录ftp
            boolean success = ftpClient.login(userName, password);
            if (!success) {
                ftpClient.logout();
                System.out.println("客户端登录失败");

            }
            System.out.println("客户端登录成功");
            // 大部分情况，上传文件时，需要设置这两项
            // 设置文件传输类型为二进制文件类型
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // 设置被动模式
            ftpClient.enterLocalPassiveMode();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH");
            Date date = new Date(System.currentTimeMillis());
            createDir(ftpClient, formatter.format(date));
            FTPPath = formatter.format(date);
            boolean done = false;

            try (final InputStream input = new FileInputStream(file)) {

                // 设置上传到ftp上使用的文件名和路径
                String remote = "/" + formatter.format(date) + "/" + file.getName();
                System.out.println(remote);
                // 上传文件
                done = ftpClient.storeFile(remote, input);
            }
            if (done) {
                System.out.println("上传文件" + file.getName() + "成功");
                // ftpClient.completePendingCommand();
            } else {
                System.out.println("上传文件" + file.getName() + "失败");
                showServerReply(ftpClient);
            }

            ftpClient.noop(); // check that control connection is working OK
            ftpClient.logout();
        } catch (FTPConnectionClosedException e) {
            System.out.println("服务端关闭连接");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("客户端登录或操作失败");
            e.printStackTrace();
        } finally {

            disconnectQuietly(ftpClient);
        }
        return "/"+FTPPath;
    }

    /**
     * 断开ftp连接
     */
    public static void disconnectQuietly(FTPClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException ex) {
                // do nothing
            }
        }
    }

    /**
     * 打印服务器返回信息
     */
    public static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("服务端响应信息: " + aReply);
            }
        }
    }

    /**
     * 创建文件夹
     *
     * @param ftpClient
     * @param dirname
     */
    public static void createDir(FTPClient ftpClient, String dirname) {
        try {
            ftpClient.makeDirectory(dirname);
            System.out.println("在目标服务器上成功建立了文件夹: " + dirname);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * 获取FTPClient对象
     *
     * @param ftpHost     FTP主机服务器
     * @param ftpPassword FTP 登录密码
     * @param ftpUserName FTP登录用户名
     * @param ftpPort     FTP端口 默认为21
     * @return
     */
    public static FTPClient getFTPClient(String ftpHost, String ftpUserName,
                                         String ftpPassword, int ftpPort) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(ftpHost, ftpPort);// 连接FTP服务器
            ftpClient.login(ftpUserName, ftpPassword);// 登陆FTP服务器
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.disconnect();
            } else {

            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ftpClient;
    }

    public static String createTask(String srcPath, String requestId, String bn) throws IOException {
        //获取token
        String msg = sendGet(url);
        JSONObject jsonObject = JSONObject.parseObject(msg);
        JSONObject data = jsonObject.getJSONObject("data");
        String token = data.getString("token");
        //调用方法
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("callbackURL", "http://101.34.39.121:30013/app-yushan-tech-batch/td/callback")
                .addFormDataPart("taskType", "PSCORES")
                .addFormDataPart("srcPath", srcPath)
                .addFormDataPart("requestId", requestId)
//                .addFormDataPart("dstPath",dstPath)
                //dstPath一行若是不传该值，则拿taskId创建一个目录存放结果
                .build();
        Request request = new Request.Builder()
                .url("https://api.talkingdata.com/async/client/bn/" + bn)
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
