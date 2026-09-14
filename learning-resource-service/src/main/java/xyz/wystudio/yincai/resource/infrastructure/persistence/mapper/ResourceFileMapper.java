package xyz.wystudio.yincai.resource.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.wystudio.yincai.resource.infrastructure.persistence.entity.ResourceFileEntity;

@Mapper
public interface ResourceFileMapper extends BaseMapper<ResourceFileEntity> {
}
