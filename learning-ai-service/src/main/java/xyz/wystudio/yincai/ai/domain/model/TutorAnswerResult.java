package xyz.wystudio.yincai.ai.domain.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorAnswerResult {

    private List<Map<String, String>> answerSegments;
}
