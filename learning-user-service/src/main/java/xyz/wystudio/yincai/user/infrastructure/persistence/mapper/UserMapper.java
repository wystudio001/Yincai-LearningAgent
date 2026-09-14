package xyz.wystudio.yincai.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.wystudio.yincai.user.infrastructure.persistence.entity.UserEntity;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("SELECT * FROM learning_user.`user` WHERE username = #{username} AND deleted = 0")
    UserEntity selectByUsername(@Param("username") String username);
}
