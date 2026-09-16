package xyz.wystudio.yincai.ai.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTask {

    private String taskId;
    private String type;
    private String status;
    private String inputContext;
    private String outputResult;
}
