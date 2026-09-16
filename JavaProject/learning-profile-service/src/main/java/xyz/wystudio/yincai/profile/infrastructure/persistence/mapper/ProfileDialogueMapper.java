package xyz.wystudio.yincai.profile.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.wystudio.yincai.profile.infrastructure.persistence.entity.ProfileDialogueEntity;

import java.util.List;

@Mapper
public interface ProfileDialogueMapper extends BaseMapper<ProfileDialogueEntity> {

    @Select("SELECT * FROM learning_profile.profile_dialogue WHERE student_id = #{studentId} ORDER BY created_at ASC")
    List<ProfileDialogueEntity> selectByStudentId(@Param("studentId") Long studentId);
}
