package Rss;


import com.yushan.tech.batch.util.rss.XmlServiceEnglish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RssTaskEnglish {
    private final Logger logger = LoggerFactory.getLogger(RssTaskEnglish.class);
    /**
     * 每天早上十点执行
     */

    @Scheduled(cron = "0 0 14 * * ?")
    public void RssEnglishService(){
        logger.info("开始执行Rss英文源的定时任务");
        String EnglishRss = "https://blogs.dlapiper.com/privacymatters/feed";
        String EnglishRss1 = "https://www.datanami.com/feed/";
        XmlServiceEnglish.sendmail(EnglishRss);
        XmlServiceEnglish.sendmail(EnglishRss1);
    }
}
