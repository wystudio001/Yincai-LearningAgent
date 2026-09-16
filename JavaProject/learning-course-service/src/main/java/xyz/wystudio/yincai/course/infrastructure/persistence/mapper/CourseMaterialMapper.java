package xyz.wystudio.yincai.course.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.wystudio.yincai.course.infrastructure.persistence.entity.CourseMaterialEntity;

@Mapper
public interface CourseMaterialMapper extends BaseMapper<CourseMaterialEntity> {
}
