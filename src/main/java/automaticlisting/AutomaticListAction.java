package automaticlisting;


import com.alibaba.fastjson.JSONObject;

import com.yushan.tech.batch.action.xinqihang.util.Base64;
import com.yushan.tech.batch.dao.automaticlisting.AutomaticListDao;
import com.yushan.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 支持postman,openApi,apiFox.
 */
@Controller
@RequestMapping(value = "/automatic")
public class AutomaticListAction {
    private final Logger logger = LoggerFactory.getLogger(AutomaticListAction.class);
    @Autowired
    private AutomaticListDao dao;

    //    @PostMapping("/listing")
    @RequestMapping(value = "listing", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject Automatic(final HttpServletResponse response,
                                final HttpServletRequest request,
                                @RequestParam(value = "file") MultipartFile multipart,
                                @RequestParam(value = "ds_id") String ds_id,
                                @RequestParam(value = "company_name", required = false) String company_name,
                                @RequestParam(value = "creator", required = false) String creator,
                                @RequestParam(value = "datasource_name", required = false) String datasource_name,
                                @RequestParam(value = "type", defaultValue = "postman") String type
    ) {
        logger.info("ds_id：{}", ds_id);
        logger.info("company_name：{}", company_name);
        logger.info("creator：{}", creator);
        logger.info("datasource_name：{}", datasource_name);
        logger.info("type：{}", type);
        JSONObject ret = new JSONObject();
        JSONObject AutomaticListingObject = new JSONObject();
        type = type.toLowerCase();
        try {
            if (type.equals("postman")) {
                AutomaticListingObject = PostManMethod.Postman(multipart);
            } else if (type.equals("openapi")) {
                AutomaticListingObject = OpenApiListAction.openApi(multipart);
            }else if (type.equals("apifox")){
                AutomaticListingObject = ApifoxMethod.apiFox(multipart);
            }
            ret.put("retmsg", "success");
            ret.put("retcode", "000000");
        } catch (Exception e) {
            logger.info("文件转换出错", ExceptionUtil.getTrace(e));
            ret.put("retmsg", "文件转换出错");
            ret.put("retcode", "200000");
            return ret;
        }
        if (creator == null || creator.isEmpty()) {
            if (company_name == null || company_name.isEmpty()) {
                if (datasource_name == null || datasource_name.isEmpty()) {
                    int i = dao.automaticList(ds_id, AutomaticListingObject.toString(), "0", "supplier", "", "CPDB_APP", "");
                    logger.info("插入数据库结果为{}", i);
                }
            }
        }
        if (creator == null || creator.isEmpty()) {
            if (company_name == null || company_name.isEmpty()) {
                if (datasource_name != null) {
                    byte[] decode = Base64.decode(datasource_name);
                    String datasourceName = new String(decode);
                    int i = dao.automaticList(ds_id, AutomaticListingObject.toString(), "0", "supplier", "", "CPDB_APP", datasourceName);
                    logger.info("插入数据库结果为{}", i);
                }
            }
        }
        if (creator == null || creator.isEmpty()) {
            if (company_name != null) {
                byte[] decode = Base64.decode(company_name);
                String companyName = new String(decode);
                logger.info("插入的公司名称为：{}", companyName);
                if (datasource_name == null || datasource_name.isEmpty()) {
                    int i = dao.automaticList(ds_id, AutomaticListingObject.toString(), "0", "supplier", companyName, "CPDB_APP", "");
                    logger.info("插入数据库结果为{}", i);
                }
            }
        }
        if (company_name == null || company_name.isEmpty()) {
            if (creator != null) {
                if (datasource_name != null) {
                    byte[] decode = Base64.decode(datasource_name);
                    String datasourceName = new String(decode);
                    int i = dao.automaticList(ds_id, AutomaticListingObject.toString(), "0", "supplier", "", creator, datasourceName);
                    logger.info("插入数据库结果为{}", i);
                }
            }
        }
        if (company_name != null & creator != null && datasource_name != null) {
            byte[] decode = Base64.decode(company_name);
            String companyName = new String(decode);
            logger.info("插入的公司名称为：{}", companyName);
            byte[] decode_datasource = Base64.decode(datasource_name);
            String datasourceName = new String(decode_datasource);
            int i = dao.automaticList(ds_id, AutomaticListingObject.toString(), "0", "supplier", companyName, creator, datasourceName);
            logger.info("插入数据库结果为{}", i);
        }
        if (company_name != null) {
            byte[] decode = Base64.decode(company_name);
            String companyName = new String(decode);
            if (creator == null || creator.isEmpty()) {
                if (datasource_name != null) {
                    byte[] decode_datasource = Base64.decode(datasource_name);
                    String datasourceName = new String(decode_datasource);
                    int i = dao.automaticList(ds_id, AutomaticListingObject.toString(), "0", "supplier", companyName, "CPDB_APP", datasourceName);
                    logger.info("插入数据库结果为{}", i);
                }
            }
        }
        if (company_name != null) {
            byte[] decode = Base64.decode(company_name);
            String companyName = new String(decode);
            if (creator != null) {
                if (datasource_name == null || creator.isEmpty()) {
                    int i = dao.automaticList(ds_id, AutomaticListingObject.toString(), "0", "supplier", companyName, creator, "");
                    logger.info("插入数据库结果为{}", i);
                }
            }
        }
        if (company_name == null || company_name.isEmpty()) {
            if (creator != null) {
                if (datasource_name == null || creator.isEmpty()) {
                    int i = dao.automaticList(ds_id, AutomaticListingObject.toString(), "0", "supplier", "", creator, "");
                    logger.info("插入数据库结果为{}", i);
                }
            }
        }
        return ret;
    }
}
