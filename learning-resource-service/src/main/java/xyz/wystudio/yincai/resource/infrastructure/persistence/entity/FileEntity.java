package xyz.wystudio.yincai.resource.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_resource.file")
public class FileEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private String originalName;

    private String fileType;

    private Long fileSize;

    private String storagePath;

    private String status;

    private Long uploadedBy;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;
}
