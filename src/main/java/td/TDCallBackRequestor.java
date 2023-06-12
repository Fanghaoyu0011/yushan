package td;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.credentials.utils.StringUtils;
import com.yushan.base.template.iface.IPropertyEngine;
import com.yushan.base.util.StringUtil;
import com.yushan.tech.batch.util.YushanAES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/td")
public class TDCallBackRequestor {

    private static final Logger logger = LoggerFactory.getLogger(TDCallBackRequestor.class);

    @RequestMapping(path = "/callback", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public JSONObject retrieve(@RequestBody JSONObject dataGet, final HttpServletRequest request) {
    	final String trade_id = StringUtil.getRandomNo();
        logger.info("{} TD腾云回调接口:{}", trade_id,dataGet.toJSONString());
        Map<String, String> params = new HashMap<>();
        params.put("message", dataGet.getString("message"));
        params.put("data", dataGet.getString("data"));

        logger.info("{} TD腾云回调完成", trade_id);
        JSONObject cl = new JSONObject();
        cl.put("retcode", "200");
        cl.put("retmsg", "success");
        return cl;
    }
}
