package xyz.wystudio.yincai.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCallResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String content;
    private String modelName;
    private String modelProvider;
    private long tokenIn;
    private long tokenOut;
    private long latencyMs;
    private boolean success;
    private String errorMessage;
}
