package td;

import com.yushan.tech.batch.action.MainDataServiceAction;
import com.yushan.tech.batch.dao.BatchStatusDao;

import oracle.jdbc.proxy.annotation.Post;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;


@RestController
@RequestMapping(value = "/td")
public class TDFileDownload {
    private final Logger logger = LoggerFactory.getLogger(TDFileDownload.class);

    @Autowired
    private BatchStatusDao batchNodao;
    
    public static String ftpHost = "114.67.239.138";
    public static String ftpUserName = "02_ys_2t_360";
    public static String ftpPassword = "zmmr4g6ncwyKweue";
    public static String localPath = "/u01/tmp/yushan/downfile/";
    public static int ftpPort = 5021;

    @PostMapping("/download")
    public void download(
            @RequestParam(value = "dstPath") String dstPath,
            @RequestParam(value = "taskId") String taskId,final HttpServletResponse response) {
        String ftpPath = dstPath;//结果目录地址
        logger.info("开始进行结果下载,目录地址为{}", dstPath);
        String fileName = taskId + "_0.csv";//下载的文件名taskId_0.csv
        logger.info("开始进行结果下载,文件名为{}", fileName);
        downloadFtpFile(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpPath, localPath, fileName);
        logger.info("文件下载完毕，开始吐出");
        File file = new File(localPath + fileName);
        try {
            downTemplate(file,response);
            logger.info("文件吐出完毕");
            batchNodao.updateStatus(dstPath);
        } catch (IOException e) {
            e.printStackTrace();
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

    /*
     * 从FTP服务器下载文件
     *
     * @param ftpHost FTP IP地址
     *
     * @param ftpUserName FTP 用户名
     *
     * @param ftpPassword FTP用户名密码
     *
     * @param ftpPort FTP端口
     *
     * @param ftpPath FTP服务器中文件所在路径
     *
     * @param localPath 下载到本地的位置
     *
     * @param fileName 文件名称
     */
    public static void downloadFtpFile(String ftpHost, String ftpUserName,
                                       String ftpPassword, int ftpPort, String ftpPath, String localPath,
                                       String fileName) {

        FTPClient ftpClient = null;

        try {
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpPath);

            File localFile = new File(localPath + File.separatorChar + fileName);
            OutputStream os = new FileOutputStream(localFile);
            ftpClient.retrieveFile(fileName, os);
            os.close();
            ftpClient.logout();

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            e.printStackTrace();
        }
    }

    private static void downTemplate(File theFile,
                                     HttpServletResponse response) throws IOException {
        String fileName = theFile.getName();
        InputStream ipt = new BufferedInputStream(new FileInputStream(theFile.getPath()));
        byte[] bs = new byte[ipt.available()];
        ipt.read(bs);
        ipt.close();
        response.reset();
        response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.replaceAll(" ", "").getBytes(StandardCharsets.UTF_8)));
        response.addHeader("Content-Length", "" + theFile.length());
        OutputStream opt = new BufferedOutputStream(response.getOutputStream());
        response.setContentType("application/octet-stream");
        opt.write(bs);
        opt.flush();
        opt.close();
    }
}
