package xyz.wystudio.yincai.ai.application.agent;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProfileAgentService {

    public Map<String, Object> extractProfile(Long userId) {
        log.info("ProfileAgent extracting profile for userId: {}", userId);
        return Map.of("userId", userId,
                "status", "completed",
                "dimensions", List.of());
    }

    public Map<String, Object> analyzeLearningStyle(Long userId) {
        log.info("ProfileAgent analyzing learning style for userId: {}", userId);
        return Map.of("userId", userId,
                "learningStyle", "visual",
                "preferences", Map.of());
    }
}
