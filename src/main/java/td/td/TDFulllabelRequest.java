package td.td;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wanda.credit.api.dto.DataSource;
import com.wanda.credit.base.Conts;
import com.wanda.credit.base.enums.CRSStatusEnum;
import com.wanda.credit.base.util.ExceptionUtil;
import com.wanda.credit.base.util.RequestHelper;
import com.wanda.credit.common.log.ds.DataSourceLogEngineUtil;
import com.wanda.credit.common.log.ds.vo.DataSourceLogVO;
import com.wanda.credit.common.util.ParamUtil;
import com.wanda.credit.extend.BaseDataSourceRequestor;
import com.wanda.credit.extend.iface.IDataSourceRequestor;

import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

import java.util.HashMap;
import java.util.Map;


@Component("ex_TDFullLabel_Inquire")
public class TDFulllabelRequest extends BaseDataSourceRequestor implements IDataSourceRequestor {
    private final static Logger logger=LoggerFactory.getLogger(TDFulllabelRequest.class) ;

    @Value("mobile,idCard,name,encryptionType,otype,filter")
    private String[] paramIds;

    @Override
    public String[] getParamIds(){
        return paramIds;
    }
    @Value("idCard,name,encryptionType")
    private String[] nullableIds;
    @Override
    public String[] getNullableIds(){
        return nullableIds;
    }

    @Override
    public Map<String, Object> request(String trade_id, DataSource ds){

        String reqUrl = propertyEngine.readById("ds_TDFullLabel_reqUrl");
        String apikey = propertyEngine.readById("ds_TD_apikey");
        String apitoken = propertyEngine.readById("ds_TD_apitoken");
        String tokenUrl = propertyEngine.readById("ds_TD_tokenUrl");

        //创建交易号
        String prefix = trade_id + " " + Conts.KEY_SYS_AGENT_HEADER;

        //交易开始时间
        long startTime = System.currentTimeMillis();


        logger.info("{} PID全量标签查询", prefix);

        // 组织返回对象
        Map<String, Object> rets = new HashMap<String, Object>();
        Map<String, String> headersParam = new HashMap<String, String>();
        Map<String, Object> reqsParam = new HashMap<String, Object>();
        Map<String, String> params = new HashMap<String, String>();


        // 交易日志信息数据
        DataSourceLogVO logObj = new DataSourceLogVO(trade_id);
        logObj.setReq_time(new Timestamp(System.currentTimeMillis()));
        logObj.setDs_id(ds.getId());
        logObj.setIncache("0");
        logObj.setState_code(DataSourceLogEngineUtil.TRADE_STATE_FAIL);
        logObj.setState_msg("交易失败");
        logObj.setReq_url(reqUrl);
        // 计费标签
        String resource_tag = Conts.TAG_SYS_ERROR;
        RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(3000)
				.setConnectTimeout(3000)
				.setSocketTimeout(3000).build();
        try {

            //获取token
            HashMap<String, String> paramsMap = new HashMap<>();
            paramsMap.put("apikey", apikey); // 替换成实际账号
            paramsMap.put("apitoken", apitoken); // 替换成实际密码
            String responseToken = RequestHelper.doGet(tokenUrl, paramsMap, false, requestConfig);
            JSONObject tokenJson = JSONObject.parseObject(responseToken);
            JSONObject tokenData = tokenJson.getJSONObject("data");
            String token = tokenData.getString("token");

            //获取参数
            String phone = ParamUtil.findValue(ds.getParams_in(), "mobile").toString();//手机号
            String idCard = ParamUtil.findValue(ds.getParams_in(), "idCard") != null ? ParamUtil.findValue(ds.getParams_in(), "idCard").toString() : "";//身份证号
            String name = ParamUtil.findValue(ds.getParams_in(), "name") != null ? ParamUtil.findValue(ds.getParams_in(), "name").toString() : "";//姓名
            String encryptionType = ParamUtil.findValue(ds.getParams_in(), "encryptionType") != null ? ParamUtil.findValue(ds.getParams_in(), "encryptionType").toString() : "";//加密类型： 01：明文 02：MD5，默认明文
            String otype = ParamUtil.findValue(ds.getParams_in(), "otype").toString();//输出标签类别，可支持多选
            String filter = ParamUtil.findValue(ds.getParams_in(), "filter").toString();//是否过滤权重0：不输出权重 1：输出权重
            //封装打印日志入参
            reqsParam.put("phone",phone);
            reqsParam.put("idCard",idCard);
            reqsParam.put("name",name);
            reqsParam.put("encryptionType",encryptionType);
            reqsParam.put("otype",otype);
            reqsParam.put("filter",filter);


            //封装入参
            params.put("bizOrderId",trade_id);
            params.put("phone",phone);
            params.put("idCard",idCard);
            params.put("name",name);
            params.put("encryptionType",encryptionType);
            params.put("otype",otype);
            params.put("filter",filter);

            //封装请求头
            headersParam.put("X-Access-Token",token);
            
            logger.info("{} 开始PID全量标签查询...", prefix);
            String response = RequestHelper.doGet(reqUrl, params, headersParam, false,requestConfig);
            logger.info("{} PID全量标签查询,返回内容：{}", prefix, response);

            JSONObject resJson = JSON.parseObject(response);
            
            //处理返回数据
            String code = resJson.getString("code");

            switch (code){
                case "2001":
                    logObj.setState_code(DataSourceLogEngineUtil.TRADE_STATE_SUCC);
                    logObj.setState_msg("交易成功");
//                    JSONObject proJson = proJson(resJson);
                    rets.put(Conts.KEY_RET_DATA, resJson.getJSONObject("data"));
                    //计费标签
                    resource_tag = Conts.TAG_TST_SUCCESS;
                    rets.put(Conts.KEY_RET_MSG, "采集成功!");
                    rets.put(Conts.KEY_RET_TAG, new String[]{resource_tag});
                    rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_SUCCESS);
                    break;
                case "1400":
                    resource_tag = Conts.TAG_SYS_ERROR;
                    rets.put(Conts.KEY_RET_TAG, new String[]{resource_tag});
                    rets.put(Conts.KEY_RET_MSG, "参数错误");
                    rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_FAILED_SYS_PARAM_INVALID);
                    break;
                case "2002":
                    resource_tag = Conts.TAG_SYS_ERROR;
                    rets.put(Conts.KEY_RET_TAG, new String[]{resource_tag});
                    rets.put(Conts.KEY_RET_MSG, "查无信息");
                    rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_WARN_DS_JIAO_NO_RESULT);
                    break;
                default:
                    resource_tag = Conts.TAG_SYS_ERROR;
                    rets.put(Conts.KEY_RET_TAG, new String[]{resource_tag});
                    rets.put(Conts.KEY_RET_MSG, "数据源查询异常!");
                    rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_FAILED_SYS_DS_EXCEPTION);
                    break;
            }
        }catch (Exception e){
            resource_tag = Conts.TAG_SYS_ERROR;
            rets.put(Conts.KEY_RET_STATUS, CRSStatusEnum.STATUS_FAILED_SYS_DS_EXCEPTION);
            rets.put(Conts.KEY_RET_MSG, "数据源查询异常!");
            logger.error("{} 数据源处理时异常：{}", prefix, ExceptionUtil.getTrace(e));
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
            logger.info("{} 保存ds Log开始...", prefix);
            executorDtoService.writeDsLog(trade_id, logObj, true);
            executorDtoService.writeDsParamIn(trade_id, reqsParam, logObj, true);
            logger.info("{} 保存ds Log结束", prefix);
        }
        logger.info("{}  PID全量标签查询 END,耗时：{}", prefix, System.currentTimeMillis() - startTime);
        return rets;
    }

  
    
}
