package xyz.wystudio.yincai.profile.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.wystudio.yincai.profile.infrastructure.persistence.entity.StudentProfileEntity;

import java.util.List;

@Mapper
public interface StudentProfileMapper extends BaseMapper<StudentProfileEntity> {

    @Select("SELECT * FROM learning_profile.student_profile WHERE student_id = #{studentId} AND deleted = 0")
    List<StudentProfileEntity> selectByStudentId(@Param("studentId") Long studentId);
}
