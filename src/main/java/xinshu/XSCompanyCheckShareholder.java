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

@Component("ex_xinshu_checkShareholder")
public class XSCompanyCheckShareholder extends BaseJiSuSourceRequestor implements IDataSourceRequestor {

    private final static Logger logger = LoggerFactory.getLogger(XSCompanyCheckShareholder.class);
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

        String reqUrl = propertyEngine.readById("ds_xinshu_checkShareholder_url");
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

                JSONObject respDate = json.getJSONObject("data");
                
                 
                 if (respDate.containsKey("BASIC")) {
                	 JSONObject basic = respDate.getJSONObject("BASIC");
                     basic.put("ENTID", "");
                     basic.put("ORGCODES", "");

				}else if (respDate.containsKey("BASICLIST")){
					JSONArray BasicListArray = respDate.getJSONArray("BASICLIST");
	                JSONArray BasicListJs = new JSONArray();
	               //获取数组中的数据并遍历处理
	                for(int i = 0;i<BasicListArray.size();i++){
	                    JSONObject BasicListDate = BasicListArray.getJSONObject(i);
	                    BasicListDate.put("ENTNAME_OLD", "");
	                    BasicListDate.put("ENTID", "");
	                    BasicListDate.put("ORGCODES", "");
	                    BasicListDate.remove("ID");
	                    BasicListDate.put("REGNO", "");
	                   //将处理后的数据增加到一个新的JSON数据里
	                    BasicListJs.add(BasicListDate);
	                }              
	                respDate.put("BASICLIST", BasicListJs);
				}
               if (respDate.containsKey("SHAREHOLDER")) {
            	   //获取SHAREHOLDER子节点中的数据(其为一个数组)
                   JSONArray ShareHolderArray = respDate.getJSONArray("SHAREHOLDER");
                   JSONArray ShareHolderJs = new JSONArray();
                  //获取数组中的数据并遍历处理
                   for(int i = 0;i<ShareHolderArray.size();i++){
                       JSONObject ShareHolderDate = ShareHolderArray.getJSONObject(i);
                       ShareHolderDate.put("CONFORMCODE", "");
                       ShareHolderDate.put("CURRENCYCODE", "");
                       ShareHolderDate.put("INVTYPECODE", "");
                       ShareHolderDate.put("REGNO", "");
                       ShareHolderDate.put("CREDITCODE", "");

                      //将处理后的数据增加到一个新的JSON数据里
                       ShareHolderJs.add(ShareHolderDate);
                   }              
                   respDate.put("SHAREHOLDER", ShareHolderJs);                  
			}
               JSONObject resultObject = new JSONObject();
               resultObject.put("found", respDate.getIntValue("found"));
               respDate.remove("found");
               resultObject.put("result", respDate);
             
                //收费标签
                resource_tag = Conts.TAG_TST_SUCCESS;
                logObj.setState_code(DataSourceLogEngineUtil.TRADE_STATE_SUCC);
                logObj.setState_msg("交易成功");
                rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_SUCCESS);
                rets.put(Conts.KEY_RET_DATA, resultObject);
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