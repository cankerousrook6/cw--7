package kg.attractor.payment_system.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ErrorResponseBody {
    private String title;
    private Integer status;
    private LocalDateTime timestamp;
    private Map<String, List<String>> reasons;
}
