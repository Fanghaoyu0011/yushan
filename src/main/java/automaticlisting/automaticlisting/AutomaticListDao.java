package automaticlisting.automaticlisting;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

public interface AutomaticListDao {
    /**
     * 插入自postman中查询得到的数据至数据库中
     * */
    @Insert("INSERT INTO cptdata.CPT_DATASOURCES(datasource_id,up_conf_modify,STATUS,source_app,company_name,CREATE_USER,DATASOURCE_NAME)" +
            "VALUES(#{dsid},#{modify},#{status},#{source_app},#{COMPANY_NAME},#{CREATE_USER},#{DATASOURCE_NAME})")
    int automaticList(@Param("dsid") String ds_id,@Param("modify")String modify,@Param("status") String status,@Param("source_app")
            String source_app,@Param("COMPANY_NAME") String company_name,@Param("CREATE_USER") String CREATE_USER,@Param("DATASOURCE_NAME") String DATASOURCE_NAME);
}
