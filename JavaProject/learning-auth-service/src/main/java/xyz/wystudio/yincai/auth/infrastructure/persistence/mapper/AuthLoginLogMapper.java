package xyz.wystudio.yincai.auth.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.wystudio.yincai.auth.infrastructure.persistence.entity.AuthLoginLogEntity;

@Mapper
public interface AuthLoginLogMapper extends BaseMapper<AuthLoginLogEntity> {
}
