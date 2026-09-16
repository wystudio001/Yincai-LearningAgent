package xyz.wystudio.yincai.observability;

import org.slf4j.MDC;

import java.util.UUID;

public class TraceIdHolder {

    private static final String TRACE_ID = "traceId";

    public static void set(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static String get() {
        String traceId = MDC.get(TRACE_ID);
        return traceId != null ? traceId : "";
    }

    public static String getOrGenerate() {
        String traceId = MDC.get(TRACE_ID);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().replace("-", "");
            MDC.put(TRACE_ID, traceId);
        }
        return traceId;
    }

    public static void clear() {
        MDC.remove(TRACE_ID);
    }
}
