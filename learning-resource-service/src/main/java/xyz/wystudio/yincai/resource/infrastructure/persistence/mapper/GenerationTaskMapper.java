package xyz.wystudio.yincai.resource.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.wystudio.yincai.resource.infrastructure.persistence.entity.GenerationTaskEntity;

@Mapper
public interface GenerationTaskMapper extends BaseMapper<GenerationTaskEntity> {
}
