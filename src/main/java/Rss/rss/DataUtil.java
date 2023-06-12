package Rss.rss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DataUtil {
    private static final Logger logger = LoggerFactory.getLogger(DataUtil.class);
    public static String changeData(String pudate) {
            String dtoDate = pudate;
            dtoDate = dtoDate.replace(",", "");
        try {
            logger.info("将字符串转化为date类型，格式2021-09-01 00:00:00");
            SimpleDateFormat format = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss z", Locale.ENGLISH);
            Date date = null;
            date = format.parse(dtoDate);
            String format1 = new SimpleDateFormat("yyyy-MM-dd").format(date);
            return format1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
