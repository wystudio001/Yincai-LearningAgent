package xyz.wystudio.yincai.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.wystudio.yincai.user.infrastructure.persistence.entity.RoleEntity;

@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {

    @Select("SELECT * FROM learning_user.role WHERE code = #{code} AND deleted = 0")
    RoleEntity selectByCode(@Param("code") String code);
}
