package xyz.wystudio.yincai.resource.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.wystudio.yincai.resource.infrastructure.persistence.entity.ResourceFavoriteEntity;

@Mapper
public interface ResourceFavoriteMapper extends BaseMapper<ResourceFavoriteEntity> {
}
