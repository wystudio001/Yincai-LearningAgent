package xyz.wystudio.yincai.notify.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.wystudio.yincai.notify.infrastructure.persistence.entity.NotificationTemplateEntity;

@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplateEntity> {
}
