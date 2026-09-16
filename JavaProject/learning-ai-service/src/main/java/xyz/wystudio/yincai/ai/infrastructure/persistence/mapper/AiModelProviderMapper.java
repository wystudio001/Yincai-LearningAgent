package xyz.wystudio.yincai.ai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.wystudio.yincai.ai.infrastructure.persistence.entity.AiModelProviderEntity;

@Mapper
public interface AiModelProviderMapper extends BaseMapper<AiModelProviderEntity> {
}
