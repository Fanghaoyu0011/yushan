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

@Component("ex_xinshu_package")
public class XSCompanyPackage extends BaseJiSuSourceRequestor implements IDataSourceRequestor {

    private final static Logger logger = LoggerFactory.getLogger(XSCompanyPackage.class);

    @Value("name,creditcode,orgcode,regno")
    private String[] paramIds;

    public String[] getParamIds() {
        return paramIds;
    }

    @Value("name,creditcode,orgcode,regno")
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

        String reqUrl = propertyEngine.readById("ds_xinshu_checkDetails_url");
        logger.info("reqUrl---->{}",reqUrl);
        String apikey = propertyEngine.readById("ds_xinshu_checkDetails_apikey");
        logger.info("appkey---->{}",apikey);
        String security = propertyEngine.readById("ds_xinshu_checkDetails_security");
        logger.info("security---->{}",security);
 
//        String reqUrl = "http://api.xinshucredit.com/ws/ent/entInfo";
//        String apikey = "3D79161C69ED5B2C12EE62C8D840D962";
//        String security = "bqou8";
        
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
             String name = ParamUtil.findValue(ds.getParams_in(), "entName") != null ? ParamUtil.findValue(ds.getParams_in(), "entName").toString() : "";
             String creditcode = ParamUtil.findValue(ds.getParams_in(), "creditCode") != null ? ParamUtil.findValue(ds.getParams_in(), "creditCode").toString() : "";
             String orgcode = ParamUtil.findValue(ds.getParams_in(), "orgCode") != null ? ParamUtil.findValue(ds.getParams_in(), "orgCode").toString() : "";
             String regno = ParamUtil.findValue(ds.getParams_in(), "regNo") != null ? ParamUtil.findValue(ds.getParams_in(), "regNo").toString() : "";

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

                JSONObject respDate0 = json.getJSONObject("data");
                Map<String, Object> respDate = new HashMap<String, Object>();
       
                JSONArray PersonArray = respDate0.getJSONArray("PERSON");
                JSONArray PersonJs = new JSONArray();
                for(int i = 0;i<PersonArray.size();i++){
                	JSONObject PersonDate = PersonArray.getJSONObject(i);
                	PersonDate.remove("ENTNAME"); 
                	PersonJs.add(PersonDate);
                }
                respDate.put("PERSON", PersonJs);

                JSONArray ShareHolderArray = respDate0.getJSONArray("SHAREHOLDER");
                JSONArray ShareHolderJs = new JSONArray();
                for(int i = 0;i<ShareHolderArray.size();i++){
                    JSONObject ShareHolderDate = ShareHolderArray.getJSONObject(i);
                    ShareHolderDate.remove("COUNTRY");
                    ShareHolderJs.add(ShareHolderDate);
                }
                respDate.put("SHAREHOLDER", ShareHolderJs);

               
                JSONArray AlterArray = respDate0.getJSONArray("ALTER");
                JSONArray AlterJs = new JSONArray();
               
                for(int i = 0;i<AlterArray.size();i++){
                    JSONObject AlterDate = AlterArray.getJSONObject(i);
                    JSONObject AlterJSON = new JSONObject();
                    
                    AlterJSON.put("CHANGE_DATE", AlterDate.getString("ALTDATE"));
                    AlterJSON.put("CHANGE_ITEM", AlterDate.getString("ALTITEM"));
                    AlterJSON.put("BEFORE_CONTENT", AlterDate.getString("ALTBE"));
                    AlterJSON.put("AFTER_CONTENT", AlterDate.getString("ALTAF"));
                   
                    AlterJs.add(AlterJSON);
                }              
                respDate.put("CHANGERECORDS", AlterJs);
                
                
               JSONObject BasicDate = respDate0.getJSONObject("BASIC");
               BasicDate.remove("ORIREGNO");
               BasicDate.remove("ENTNAMEENG");
               BasicDate.remove("ENTID");
               BasicDate.put("ENTNAME_OLD", "");
               BasicDate.put("PHONE", "");	
               BasicDate.put("QRCODE_IMAGE_BASE64", "");
               BasicDate.put("ID", "");
               BasicDate.put("LOGOIMGNAME", "");
               respDate.put("BASIC", BasicDate);
            
               
               JSONArray BreaklawArray = respDate0.getJSONArray("BREAKLAW");
               JSONArray BreaklawJs = new JSONArray();
               for(int i = 0;i<BreaklawArray.size();i++){
                   JSONObject BreaklawDate = BreaklawArray.getJSONObject(i);
                   JSONObject BreaklawJSON = new JSONObject();

                   BreaklawJSON.put("IN_DATE", BreaklawDate.getString("INDATE"));
                   BreaklawJSON.put("IN_REASON", BreaklawDate.getString("INREASON"));
                   BreaklawJSON.put("IN_DEPARTMENT", BreaklawDate.getString("INDEPARTMENT"));
                   BreaklawJSON.put("OUT_DATE", BreaklawDate.getString("OUTDATE"));
                   BreaklawJSON.put("OUT_REASON", BreaklawDate.getString("OUTREASON"));
                   BreaklawJSON.put("OUT_DEPARTMENT", BreaklawDate.getString("OUTDEPARTMENT"));

                   BreaklawJs.add(BreaklawJSON);
                   }
               respDate.put("EXECUTION", BreaklawJs);

               
               JSONArray ExceptionListArray = respDate0.getJSONArray("EXCEPTIONLIST");
               JSONArray ExceptionListJs = new JSONArray();
               for(int i = 0;i<ExceptionListArray.size();i++){
                   JSONObject ExceptionListDate = ExceptionListArray.getJSONObject(i);
                   ExceptionListDate.remove("ENTNAME");
                   ExceptionListDate.remove("ENTTYPE");
                   ExceptionListDate.remove("REGNO");
                   ExceptionListDate.remove("SHXYDM");
                   ExceptionListDate.remove("YC_REGORG");
                   ExceptionListDate.remove("YR_REGORG");
                   ExceptionListJs.add(ExceptionListDate);
                   }
               respDate.put("ABNORMAL", ExceptionListJs);
               
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

                
                rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_SUCCESS);
                rets.put(Conts.KEY_RET_MSG, "交易成功!");
                rets.put(Conts.KEY_RET_DATA, "查询成功，无数据");
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
}