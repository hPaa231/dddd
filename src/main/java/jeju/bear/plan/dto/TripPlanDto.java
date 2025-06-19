package jeju.bear.plan.dto;

import lombok.*;
import jeju.bear.plan.entity.TripPlan;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripPlanDto {
    private Long tripPlanId;
    private String planName;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private List<TripDayDto> days;

    public static TripPlanDto from(TripPlan plan) {
        return TripPlanDto.builder()
                .tripPlanId(plan.getTripPlanId())
                .planName(plan.getPlanName())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .days(plan.getDays().stream().map(TripDayDto::from).collect(Collectors.toList()))
                .build();
    }
}