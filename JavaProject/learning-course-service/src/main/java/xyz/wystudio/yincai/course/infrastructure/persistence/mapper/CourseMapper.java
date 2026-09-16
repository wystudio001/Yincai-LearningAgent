package xyz.wystudio.yincai.course.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import xyz.wystudio.yincai.course.infrastructure.persistence.entity.CourseEntity;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<CourseEntity> {

    @Select("SELECT * FROM learning_course.course WHERE deleted = 0 ORDER BY created_at DESC")
    List<CourseEntity> listRecent();
}
