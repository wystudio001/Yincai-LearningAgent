package xyz.wystudio.yincai.ai.application.agent;

import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CoordinatorAgentService {

    public Map<String, Object> orchestrate(Map<String, Object> context) {
        log.info("CoordinatorAgent orchestrating with context: {}", context);
        return Map.of("status", "completed");
    }
}
