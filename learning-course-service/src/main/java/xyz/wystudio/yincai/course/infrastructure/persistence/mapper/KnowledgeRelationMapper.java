package xyz.wystudio.yincai.course.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.wystudio.yincai.course.infrastructure.persistence.entity.KnowledgeRelationEntity;

import java.util.List;

@Mapper
public interface KnowledgeRelationMapper extends BaseMapper<KnowledgeRelationEntity> {

    @Select("SELECT * FROM learning_course.knowledge_relation WHERE point_id = #{pointId} AND deleted = 0")
    List<KnowledgeRelationEntity> selectByPointId(@Param("pointId") Long pointId);
}
