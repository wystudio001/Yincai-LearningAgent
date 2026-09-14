package xyz.wystudio.yincai.course.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.wystudio.yincai.course.infrastructure.persistence.entity.KnowledgePointEntity;

@Mapper
public interface KnowledgePointMapper extends BaseMapper<KnowledgePointEntity> {
}
