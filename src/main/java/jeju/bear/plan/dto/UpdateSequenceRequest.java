// src/main/java/com/jeju/bear/plan/dto/UpdateSequenceRequest.java
package jeju.bear.plan.dto;

import lombok.*;
import jakarta.validation.constraints.*;  // javax → jakarta로 변경
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSequenceRequest {
    @NotNull
    private Long tripDayId;

    @NotEmpty
    private List<Long> orderedDestinationIds;
}
