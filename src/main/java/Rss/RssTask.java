package Rss;


import com.yushan.tech.batch.util.rss.XmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author fanghaoyu
 * @desc 每周一调用产品
 */
@Component
public class RssTask {
    private final Logger logger = LoggerFactory.getLogger(RssTask.class);

    /**
     * 每天早上十点执行
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void rssService() {
        logger.info("开始执行rss中文源的定时任务");
        XmlService xs = new XmlService();
        ;
        String rssUrl = "https://www.williamlong.info/rss.xml";
//        String rssUrl1 = "https://feeds.dzone.com/big-data";
        String postUrl = "https://www.yushanshuju.com/ords/datatech/ofsi/add_news";
        logger.info("遍历rss订阅的网址:{}", rssUrl);
        logger.info("post请求的接口:{}", postUrl);
        xs.getBlogs(rssUrl, postUrl);
//        xs.getBlogs(rssUrl1, postUrl);
    }
}
