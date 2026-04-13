package com.photo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.photo.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    
    @Select("SELECT operation, COUNT(*) as count FROM t_operation_log WHERE user_id = #{userId} GROUP BY operation ORDER BY count DESC LIMIT 10")
    List<Map<String, Object>> selectOperationStats(@Param("userId") Long userId);
}
