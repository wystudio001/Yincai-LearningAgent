package xyz.wystudio.yincai.profile.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.wystudio.yincai.profile.infrastructure.persistence.entity.ProfileDimensionEntity;

import java.util.List;

@Mapper
public interface ProfileDimensionMapper extends BaseMapper<ProfileDimensionEntity> {

    @Select("SELECT * FROM learning_profile.profile_dimension WHERE profile_id = #{profileId} AND deleted = 0")
    List<ProfileDimensionEntity> selectByProfileId(@Param("profileId") Long profileId);
}
