package xyz.wystudio.yincai.profile.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.wystudio.yincai.profile.infrastructure.persistence.entity.ProfileVersionEntity;

import java.util.List;

@Mapper
public interface ProfileVersionMapper extends BaseMapper<ProfileVersionEntity> {

    @Select("SELECT * FROM learning_profile.profile_version WHERE profile_id = #{profileId} ORDER BY version_no DESC")
    List<ProfileVersionEntity> selectByProfileId(@Param("profileId") Long profileId);
}
