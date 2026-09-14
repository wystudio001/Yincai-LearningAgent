package xyz.wystudio.yincai.course.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.wystudio.yincai.course.infrastructure.persistence.entity.ChapterEntity;

import java.util.List;

@Mapper
public interface ChapterMapper extends BaseMapper<ChapterEntity> {

    @Select("SELECT * FROM learning_course.chapter WHERE course_id = #{courseId} AND deleted = 0 ORDER BY sort_order ASC")
    List<ChapterEntity> selectByCourseId(@Param("courseId") Long courseId);
}
