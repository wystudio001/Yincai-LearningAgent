package xyz.wystudio.yincai.auth.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.wystudio.yincai.auth.infrastructure.persistence.entity.AuthUserEntity;

@Mapper
public interface AuthUserMapper extends BaseMapper<AuthUserEntity> {

    @Select("SELECT * FROM learning_auth.auth_user WHERE username = #{username} AND deleted = 0")
    AuthUserEntity selectByUsername(@Param("username") String username);
}
