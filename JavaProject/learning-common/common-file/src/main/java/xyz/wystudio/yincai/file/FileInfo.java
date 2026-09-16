package xyz.wystudio.yincai.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long fileId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String storagePath;
    private String downloadUrl;
    private String previewUrl;
}
