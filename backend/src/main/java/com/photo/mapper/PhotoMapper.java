package com.photo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.photo.entity.Photo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PhotoMapper extends BaseMapper<Photo> {
    
    @Select("SELECT DISTINCT camera_model FROM t_photo WHERE user_id = #{userId} AND camera_model IS NOT NULL AND camera_model != ''")
    List<String> selectDistinctCameraModels(@Param("userId") Long userId);

    @Update("UPDATE t_photo SET is_deleted = 0 WHERE id = #{photoId}")
    int restoreById(@Param("photoId") Long photoId);

    @Update("UPDATE t_photo SET is_deleted = 0 WHERE id IN (${photoIds})")
    int restoreByIds(@Param("photoIds") String photoIds);

    @Update("UPDATE t_photo SET is_deleted = 1 WHERE user_id = #{userId} AND is_deleted = 0")
    int deleteAllByUserId(@Param("userId") Long userId);

    @Update("UPDATE t_photo SET album_id = NULL WHERE id = #{photoId}")
    int removeAlbum(@Param("photoId") Long photoId);
}
