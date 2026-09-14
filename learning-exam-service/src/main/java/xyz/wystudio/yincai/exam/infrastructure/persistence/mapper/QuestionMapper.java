package xyz.wystudio.yincai.exam.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.wystudio.yincai.exam.infrastructure.persistence.entity.QuestionEntity;

@Mapper
public interface QuestionMapper extends BaseMapper<QuestionEntity> {
}
