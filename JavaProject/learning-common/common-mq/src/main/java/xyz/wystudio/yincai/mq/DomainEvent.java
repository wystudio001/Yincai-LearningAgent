package xyz.wystudio.yincai.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private String sourceService;
    private Long tenantId;
    private String payload;
    private LocalDateTime occurredAt;
    private String traceId;

    public static DomainEvent of(String eventType, String sourceService, String payload) {
        return DomainEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .sourceService(sourceService)
                .payload(payload)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
