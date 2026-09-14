package xyz.wystudio.yincai.resource.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.wystudio.yincai.resource.infrastructure.persistence.entity.ResourcePackageEntity;

@Mapper
public interface ResourcePackageMapper extends BaseMapper<ResourcePackageEntity> {
}
