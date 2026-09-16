package xyz.wystudio.yincai.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCallRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String scene;           // 场景：PROFILE_EXTRACT, RESOURCE_GENERATE, TUTOR_ANSWER etc.
    private String promptTemplateCode;
    private String promptVersion;
    private Map<String, Object> variables;
    private String modelProviderCode;
    private boolean stream;
    private Long timeoutMs;
}
