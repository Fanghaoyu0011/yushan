package xinshu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wanda.credit.api.dto.DataSource;
import com.wanda.credit.base.Conts;
import com.wanda.credit.base.enums.CRSStatusEnum;
import com.wanda.credit.base.util.*;
import com.wanda.credit.common.log.ds.DataSourceLogEngineUtil;
import com.wanda.credit.common.log.ds.vo.DataSourceLogVO;
import com.wanda.credit.common.util.ParamUtil;
import com.wanda.credit.extend.client.jisu.BaseJiSuSourceRequestor;
import com.wanda.credit.extend.iface.IDataSourceRequestor;

import javassist.bytecode.Descriptor.Iterator;

import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author 李彬彬
 * Time 2022-02-28
 * source 信数科技
 * 企业主关联核验
 */

@Component("ex_xinshu_detailsCheck")
public class XSCompanyDetailsCheck extends BaseJiSuSourceRequestor implements IDataSourceRequestor {

    private final static Logger logger = LoggerFactory.getLogger(XSCompanyPackage.class);

    @Value("name,creditcode,orgcode,regno,enttype")
    private String[] paramIds;

    public String[] getParamIds() {
        return paramIds;
    }

    @Value("name,creditcode,orgcode,regno,enttype")
    private String[] nullableIds;

    @Override
    public String[] getNullableIds() {
        return nullableIds;
    }
    @Override
    public Map<String, Object> request(String trade_id, DataSource ds) throws UnsupportedEncodingException {
        String prefix = trade_id + " " + Conts.KEY_SYS_AGENT_HEADER;
        // 记录交易开始时间
        long startTime = System.currentTimeMillis();
        logger.info("{}:信数数据源[企业信息查询]...启动", prefix);

        String reqUrl = propertyEngine.readById("ds_xinshu_detailsCheck_url");
        logger.info("reqUrl---->{}",reqUrl);
        String apikey = propertyEngine.readById("ds_xinshu_detailsCheck_apikey");
        logger.info("appkey---->{}",apikey);
        String security = propertyEngine.readById("ds_xinshu_detailsCheck_security");
        logger.info("security---->{}",security);       
        // 组织返回对象
        Map<String, Object> rets = new HashMap<String, Object>();
        Map<String, Object> reqparam = new HashMap<String, Object>();
        // 交易日志信息数据
        DataSourceLogVO logObj = new DataSourceLogVO(trade_id);
        logObj.setReq_time(new Timestamp(System.currentTimeMillis()));
        logObj.setDs_id(ds.getId());
        logObj.setIncache("0");
        logObj.setState_code(DataSourceLogEngineUtil.TRADE_STATE_FAIL);
        logObj.setState_msg("交易成功");
        logObj.setReq_url(reqUrl);
        // 计费标签
        String resource_tag = Conts.TAG_SYS_ERROR;
        try{
        	 logger.info("{} 开始解析传入的参数" , prefix);
             //获取请求参数
             String name = ParamUtil.findValue(ds.getParams_in(), "name") != null ? ParamUtil.findValue(ds.getParams_in(), "name").toString() : "";
             String creditcode = ParamUtil.findValue(ds.getParams_in(), "creditcode") != null ? ParamUtil.findValue(ds.getParams_in(), "creditcode").toString() : "";
             String orgcode = ParamUtil.findValue(ds.getParams_in(), "orgcode") != null ? ParamUtil.findValue(ds.getParams_in(), "orgcode").toString() : "";
             String regno = ParamUtil.findValue(ds.getParams_in(), "regno") != null ? ParamUtil.findValue(ds.getParams_in(), "regno").toString() : "";
             reqparam.put("name", name);
             reqparam.put("creditcode", creditcode);
             reqparam.put("orgcode", orgcode);
             reqparam.put("regno", regno);
             //判断所有参数是否为空
             if("".equals(name)&&"".equals(creditcode)&&"".equals(orgcode)&&"".equals(regno)){
                 resource_tag = Conts.TAG_SYS_ERROR;
                 rets.put(Conts.KEY_RET_TAG, new String[]{resource_tag});
                 rets.put(Conts.KEY_RET_MSG, "入参为空");
                 rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_FAILED_SYS_PARAM_INVALID);
                 return rets;
             }
            //获取当前时间
            Date date = new Date();
            String format = "yyyyMMdd";
            SimpleDateFormat ft = new SimpleDateFormat (format);
            String currDate = ft.format(date);
            logger.info("{}:请求格式化日期:{}", currDate);

            //获取sign
            String sign = MD5.ecodeByMD5(security+currDate);
            //logger.info("{}:请求sign明文:{}", security+currDate);
            //封装请求参数
            Map<String,String> params = new HashMap<String,String>();
            params.put("apikey",apikey);
            params.put("sign",sign);
            if(!StringUtil.isEmpty(regno)){
            	params.put("regNo", regno);
    			logger.info("{} 入参regno:{}", prefix,regno);
    		}
    		if(!StringUtil.isEmpty(orgcode)){
    			params.put("orgCode", orgcode);
    			logger.info("{} 入参orgcode:{}", prefix,orgcode);
    		}			
    		if(!StringUtil.isEmpty(name)){
    			params.put("entName", name);
    			logger.info("{} 入参name:{}", prefix,name);
    		}
    		if(!StringUtil.isEmpty(creditcode)){				
    			params.put("creditCode", creditcode);
    			logger.info("{} 入参creditCode:{}", prefix,creditcode);
            logger.info("{}:请求url:{}", trade_id, reqUrl);
    		}
            //创建请求头
            Map headers = new HashMap<String,String>();
            String result = RequestHelper.doPost(reqUrl,params,headers,null, ContentType.APPLICATION_FORM_URLENCODED,false);
            logger.info("{}:信数企业四要素核验调用成功:{}", trade_id, result);

            //判断获取结果是否为空
            if (StringUtil.isEmpty(result)) {
                logger.info("{} http请求返回内容为空：{}" , prefix , result);
                rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_FAILED_SYS_DS_EXCEPTION);
                rets.put(Conts.KEY_RET_MSG, CRSStatusEnum.STATUS_FAILED_SYS_DS_EXCEPTION.getRet_msg());
                rets.put(Conts.KEY_RET_TAG, new String[]{resource_tag});
                logObj.setState_code(DataSourceLogEngineUtil.TRADE_STATE_TIMEOUT);
                logObj.setState_msg("请求超时");
                return rets;
            }
            
            JSONObject json = JSON.parseObject(result);
            
            String code = json.getString("rc");
            logger.info("返回结果为{}",json);
            //判断状态码查询成功
            if(code.equals("0000")){ //000000

            	
            	
            	logger.info("开始包装参数");
                JSONObject respDate = json.getJSONObject("data");       
                
               //获取ALTER子节点中的数据(其为一个数组)
                JSONArray AlterArray = respDate.getJSONArray("ALTER");
                JSONArray AlterJs = new JSONArray();
               //获取数组中的数据并遍历处理
                for(int i = 0;i<AlterArray.size();i++){
                    JSONObject AlterDate = AlterArray.getJSONObject(i);
                    AlterDate.remove("ALTITEMCODE");
                   //将处理后的数据增加到一个新的JSON数据里
                    AlterJs.add(AlterDate);
                }              
                respDate.put("ALTER", AlterJs);
                
                //获取MORTGAGEREG子节点中的一个数据(其为一个数组)
                JSONArray MortgageregArray = respDate.getJSONArray("MORTGAGEREG");
                JSONArray MortgageregJs = new JSONArray();
               //获取数组中的数据并遍历处理
                for(int i = 0;i<MortgageregArray.size();i++){
                    JSONObject MortgageregDate = MortgageregArray.getJSONObject(i);
                    MortgageregDate.remove("ALTITEMCODE");
                   //将处理后的数据增加到一个新的JSON数据里
                    MortgageregJs.add(MortgageregDate);
                }              
                respDate.put("MORTGAGEREG", MortgageregJs);
                
              //获取SHAREHOLDER子节点中的一个数据(其为一个数组)
                JSONArray ShareholderArray = respDate.getJSONArray("SHAREHOLDER");
                JSONArray ShareholderJs = new JSONArray();
               //获取数组中的数据并遍历处理
                for(int i = 0;i<ShareholderArray.size();i++){
                    JSONObject ShareholderDate = ShareholderArray.getJSONObject(i);
                    String[] keys = {"COUNTRY"};
                    RemoveKV(keys, ShareholderDate);
                   //将处理后的数据增加到一个新的JSON数据里
                    ShareholderJs.add(ShareholderDate);
                }              
                respDate.put("SHAREHOLDER", ShareholderJs);

               JSONObject BasicDate = respDate.getJSONObject("BASIC");
               String[] keys = {"ENTNAMEENG"};
               RemoveKV(keys, BasicDate);
               
             //将处理后的数据增加到一个新的JSON数据里
               respDate.put("BASIC", BasicDate);
               respDate.put("LISTEDCOMPINFO",new JSONObject());
               respDate.put("BASICLIST", null);
               
             //STOCKPAWN子节点
               respDate.put("STOCKPAWN", new JSONArray());
             //RELATEDPUNISHED子节点
               respDate.remove("RELATEDPUNISHED");
               respDate.put("RELATEDPUNISHED", new JSONArray());
             //RELATEDPUNISHBREAK子节点
               respDate.remove("RELATEDPUNISHBREAK");
               respDate.put("RELATEDPUNISHBREAK", new JSONArray());
               
           	logger.info("参数包装成功");
                //收费标签
                resource_tag = Conts.TAG_TST_SUCCESS;
                logObj.setState_code(DataSourceLogEngineUtil.TRADE_STATE_SUCC);
                logObj.setState_msg("交易成功");
                rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_SUCCESS);
                rets.put(Conts.KEY_RET_DATA, respDate);
                rets.put(Conts.KEY_RET_MSG, "交易成功!");
                rets.put(Conts.KEY_RET_TAG, new String[]{resource_tag});
            }else if(code.equals("0001")){
                //收费标签
                resource_tag = Conts.TAG_TST_SUCCESS;
                logObj.setState_code(DataSourceLogEngineUtil.TRADE_STATE_SUCC);
                JSONObject resData = new JSONObject();
                resData.put("ALTER", null);
                resData.put("MORTGAGECAN", null);
                resData.put("FRINV", null);
                resData.put("FILIATION", null);
                resData.put("STOCKPAWN", null);
                resData.put("MORTGAGEREG", null);
                resData.put("FRPOSITION", null);
                resData.put("SHARESFROST", null);
                resData.put("ENTINV", null);
                resData.put("RELATEDPUNISHED", null);
                resData.put("MORTGAGEDEBT", null);
                resData.put("MORTGAGEPAWN", null);
                resData.put("SHAREHOLDER", null);
                resData.put("PUNISHBREAK", null);
                resData.put("ENTCASEBASEINFO", null);
                resData.put("PUNISHED", null);
                resData.put("PERSON", null);
                resData.put("MORTGAGEBASIC", null);
                resData.put("MORTGAGEPER", null);
                resData.put("STOCKPAWNALT", null);
                resData.put("BASICLIST", null);
                resData.put("RELATEDPUNISHBREAK", null);
                resData.put("MORTGAGEALT", null);
                resData.put("STOCKPAWNREV", null);
                rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_SUCCESS);
                rets.put(Conts.KEY_RET_MSG, "交易成功!");
                rets.put(Conts.KEY_RET_DATA, resData);
                rets.put(Conts.KEY_RET_TAG, new String[]{resource_tag});
            }else {
                //计费标签
                resource_tag = Conts.TAG_SYS_ERROR;
                rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_FAILED_SYS_DS_EXCEPTION);
                rets.put(Conts.KEY_RET_MSG, "查询失败："+json.getString("msg"));
                rets.put(Conts.KEY_RET_TAG, new String[]{resource_tag});
                return rets;
            }
        }catch (Exception e){
            resource_tag = Conts.TAG_SYS_ERROR;
            rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_FAILED_SYS_DS_EXCEPTION);
            rets.put(Conts.KEY_RET_MSG, "数据源查询异常!");
            logger.error("{} 数据源处理时异常：{}",prefix, ExceptionUtil.getTrace(e));
            if (ExceptionUtil.isTimeoutException(e)) {
                resource_tag = Conts.TAG_SYS_TIMEOUT;
                logObj.setState_code(DataSourceLogEngineUtil.TRADE_STATE_TIMEOUT);
            } else {
                logObj.setState_code(DataSourceLogEngineUtil.TRADE_STATE_FAIL);
                logObj.setState_msg("数据源处理时异常! 详细信息:" + e.getMessage());
            }
            rets.put(Conts.KEY_RET_TAG, new String[]{resource_tag});
        }finally {
            logObj.setRsp_time(new Timestamp(System.currentTimeMillis()));
            logObj.setTag(resource_tag);
            logger.info("{} 保存ds Log开始..." ,prefix);
            executorDtoService.writeDsLog(trade_id,logObj,true);
            executorDtoService.writeDsParamIn(trade_id, reqparam, logObj,true);
            logger.info("{} 保存ds Log结束" ,prefix);
        }
        logger.info("{}  信数-企业四要素核验查询 END,耗时：{}" ,prefix , System.currentTimeMillis() - startTime);
        return rets; 	
        }
    public static boolean RemoveKV(String[] keys,JSONObject JSONObjectName) {
    	for(int i =0;i<keys.length;i++){
    		JSONObjectName.remove(keys[i]);
    	}
    	return true;
	}
}